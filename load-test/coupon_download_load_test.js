import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';
import exec from 'k6/execution';
import { check, fail } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const COUPON_ID = Number(__ENV.COUPON_ID);
const RATE_PER_SECOND = Number(__ENV.RATE_PER_SECOND || '1000');
const DURATION_SECONDS = Number(__ENV.DURATION_SECONDS || '30');
const EXPECTED_SUCCESS_COUNT = Number(__ENV.EXPECTED_SUCCESS_COUNT || '3000');
const TOKEN_SECRET = __ENV.TOKEN_SECRET;
const EXPECTED_REQUESTS = RATE_PER_SECOND * DURATION_SECONDS;
const EXPECTED_SOLD_OUT_COUNT = EXPECTED_REQUESTS - EXPECTED_SUCCESS_COUNT;

// 회원당 한 번만 발급할 수 있으므로 각 iteration은 서로 다른 실제 회원 ID를 사용한다.
// 별도 파일을 지정하지 않으면 준비 SQL과 동일한 연속 ID를 필요한 수만큼 생성한다.
const memberIds = new SharedArray('coupon download member IDs', () => {
    if (__ENV.MEMBER_IDS_FILE) {
        const rows = JSON.parse(open(__ENV.MEMBER_IDS_FILE));
        return rows.map((row) => String(row.member_id ?? row.memberId ?? row));
    }
    return Array.from({ length: EXPECTED_REQUESTS }, (_, index) =>
        addToDecimalString('900000000000000000', index + 1));
});

const downloadRequests = new Counter('coupon_download_requests');
const downloadSuccesses = new Counter('coupon_download_successes');
const downloadSoldOuts = new Counter('coupon_download_sold_outs');
const downloadUnexpectedFailures = new Counter('coupon_download_unexpected_failures');
const downloadUnexpectedErrorRate = new Rate('coupon_download_unexpected_error_rate');
const downloadResponseTime = new Trend('coupon_download_response_time_ms', true);

export const options = {
    scenarios: {
        coupon_download_1000_per_second: {
            // 응답 속도와 관계없이 매초 지정한 수의 iteration을 시작한다.
            executor: 'constant-arrival-rate',
            rate: RATE_PER_SECOND,
            timeUnit: '1s',
            duration: `${DURATION_SECONDS}s`,
            preAllocatedVUs: Number(__ENV.PRE_ALLOCATED_VUS || '1000'),
            maxVUs: Number(__ENV.MAX_VUS || '3000'),
            gracefulStop: '30s',
        },
    },
    thresholds: {
        coupon_download_requests: [`count==${EXPECTED_REQUESTS}`],
        coupon_download_successes: [`count==${EXPECTED_SUCCESS_COUNT}`],
        coupon_download_sold_outs: [`count==${EXPECTED_SOLD_OUT_COUNT}`],
        coupon_download_unexpected_failures: ['count==0'],
        coupon_download_unexpected_error_rate: ['rate==0'],
        coupon_download_response_time_ms: ['p(95)<2000', 'p(99)<5000'],
        dropped_iterations: ['count==0'],
    },
    // handleSummary가 실제 p99 값을 받을 수 있도록 k6 요약 통계에 명시합니다.
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

export function setup() {
    if (!Number.isInteger(COUPON_ID) || COUPON_ID <= 0) {
        fail('COUPON_ID 환경변수에 테스트할 쿠폰 ID를 입력하세요.');
    }
    if (!TOKEN_SECRET) {
        fail('TOKEN_SECRET 환경변수는 서버의 token.secret과 같아야 합니다.');
    }
    if (!Number.isInteger(RATE_PER_SECOND) || RATE_PER_SECOND <= 0
        || !Number.isInteger(DURATION_SECONDS) || DURATION_SECONDS <= 0) {
        fail('RATE_PER_SECOND와 DURATION_SECONDS는 1 이상의 정수여야 합니다.');
    }
    if (!Number.isInteger(EXPECTED_SUCCESS_COUNT) || EXPECTED_SUCCESS_COUNT <= 0
        || EXPECTED_SUCCESS_COUNT >= EXPECTED_REQUESTS) {
        fail('EXPECTED_SUCCESS_COUNT는 1 이상이며 전체 요청 수보다 작아야 합니다.');
    }
    if (memberIds.length < EXPECTED_REQUESTS) {
        fail(`서로 다른 회원 ID가 ${EXPECTED_REQUESTS}개 필요합니다. 현재=${memberIds.length}`);
    }

    // 공개 목록으로 게시/유효기간 및 테스트 시작 전 재고를 검증한다.
    const response = http.get(`${BASE_URL}/v1/coupons`, {
        tags: { endpoint: 'GET /v1/coupons (setup)' },
        timeout: '10s',
    });
    if (response.status !== 200) {
        fail(`쿠폰 목록 조회 실패: status=${response.status}, body=${response.body}`);
    }

    const coupon = response.json().find((item) => Number(item.couponId) === COUPON_ID);
    if (!coupon) {
        fail(`couponId=${COUPON_ID}가 현재 게시 및 유효기간 내에 없거나 재고가 소진되었습니다.`);
    }
    if (Number(coupon.remainingStock) !== EXPECTED_SUCCESS_COUNT) {
        fail(`테스트 시작 전 잔여 재고가 기대 성공 건수와 같아야 합니다. ` +
            `기대=${EXPECTED_SUCCESS_COUNT}, 실제=${coupon.remainingStock}`);
    }

    return {
        startedAt: new Date().toISOString(),
        initialRemainingStock: Number(coupon.remainingStock),
    };
}

export default function () {
    const index = exec.scenario.iterationInTest;
    const memberId = memberIds[index];

    if (memberId === undefined) {
        downloadUnexpectedFailures.add(1);
        downloadUnexpectedErrorRate.add(true);
        fail(`iteration ${index}에 대응하는 member_id가 없습니다.`);
    }

    const response = http.post(`${BASE_URL}/v1/coupons/${COUPON_ID}/download`, null, {
        headers: {
            Authorization: `Bearer ${createJwt(memberId)}`,
            'Content-Type': 'application/json',
        },
        tags: { endpoint: 'POST /v1/coupons/{couponId}/download' },
        timeout: '30s',
    });

    downloadRequests.add(1);
    downloadResponseTime.add(response.timings.duration);

    const body = parseBody(response);
    const issued = response.status === 200
        && Number(body?.couponId) === COUPON_ID
        // Snowflake 값은 JavaScript 안전 정수 범위를 넘을 수 있으므로 존재 여부만 검사한다.
        && body?.memberCouponId !== undefined
        && body?.memberCouponId !== null;
    const soldOut = response.status === 409 && body?.errorCode === 'EC7';
    const expectedOutcome = check(response, {
        '발급 성공 또는 EC7 재고 소진 응답이다': () => issued || soldOut,
    });

    downloadUnexpectedErrorRate.add(!expectedOutcome);
    if (issued) {
        downloadSuccesses.add(1);
    } else if (soldOut) {
        downloadSoldOuts.add(1);
    } else {
        downloadUnexpectedFailures.add(1);
        console.error(`index=${index}, memberId=${memberId}, status=${response.status}, body=${response.body}`);
    }
}

export function teardown(data) {
    console.log(`쿠폰 다운로드 부하테스트 완료: couponId=${COUPON_ID}, 시작=${data.startedAt}`);
    console.log(`예상 발급=${EXPECTED_SUCCESS_COUNT}, 예상 품절=${EXPECTED_SOLD_OUT_COUNT}, ` +
        `테스트 전 잔여 재고=${data.initialRemainingStock}`);
    console.log('load-test/coupon_download_verify.sql로 coupon 및 member_coupon 정합성을 확인하세요.');
}

export function handleSummary(data) {
    const succeeded = metric(data, 'coupon_download_successes', 'count');
    const soldOut = metric(data, 'coupon_download_sold_outs', 'count');
    const unexpectedFailures = metric(data, 'coupon_download_unexpected_failures', 'count');
    const summary = {
        test: {
            couponId: COUPON_ID,
            ratePerSecond: RATE_PER_SECOND,
            durationSeconds: DURATION_SECONDS,
            targetRequests: EXPECTED_REQUESTS,
            expectedSuccesses: EXPECTED_SUCCESS_COUNT,
            expectedSoldOuts: EXPECTED_SOLD_OUT_COUNT,
        },
        requests: {
            sent: metric(data, 'coupon_download_requests', 'count'),
            succeeded,
            soldOut,
            unexpectedFailures,
            dropped: metric(data, 'dropped_iterations', 'count'),
            achievedRatePerSecond: rounded(metric(data, 'coupon_download_requests', 'rate')),
        },
        result: {
            overIssued: Math.max(0, succeeded - EXPECTED_SUCCESS_COUNT),
            successCountMatches: succeeded === EXPECTED_SUCCESS_COUNT,
            hasUnexpectedFailure: unexpectedFailures > 0,
        },
        responseTimeMs: {
            average: rounded(metric(data, 'coupon_download_response_time_ms', 'avg')),
            median: rounded(metric(data, 'coupon_download_response_time_ms', 'med')),
            p90: rounded(metric(data, 'coupon_download_response_time_ms', 'p(90)')),
            p95: rounded(metric(data, 'coupon_download_response_time_ms', 'p(95)')),
            p99: rounded(metric(data, 'coupon_download_response_time_ms', 'p(99)')),
            max: rounded(metric(data, 'coupon_download_response_time_ms', 'max')),
        },
    };

    const json = JSON.stringify(summary, null, 2);
    return {
        stdout: `\n${json}\n`,
        'load-test/coupon-download-summary.json': `${json}\n`,
    };
}

function createJwt(memberId) {
    const now = Math.floor(Date.now() / 1000);
    const header = encoding.b64encode(JSON.stringify({ alg: 'HS256', typ: 'JWT' }), 'rawurl');
    const payload = encoding.b64encode(JSON.stringify({ sub: memberId, iat: now, exp: now + 3600 }), 'rawurl');
    const unsignedToken = `${header}.${payload}`;
    const signature = crypto.hmac('sha256', TOKEN_SECRET, unsignedToken, 'base64rawurl');
    return `${unsignedToken}.${signature}`;
}

function parseBody(response) {
    try {
        return response.json();
    } catch (_) {
        return null;
    }
}

function metric(data, metricName, field) {
    return data.metrics[metricName]?.values?.[field] ?? 0;
}

function rounded(number) {
    return Math.round(number * 100) / 100;
}

// Snowflake 크기의 ID를 JavaScript Number로 변환하면 정밀도가 손실되므로 문자열 상태로 더합니다.
function addToDecimalString(base, amount) {
    const digits = base.split('').map(Number);
    let carry = amount;
    for (let index = digits.length - 1; index >= 0 && carry > 0; index--) {
        const sum = digits[index] + (carry % 10);
        digits[index] = sum % 10;
        carry = Math.floor(carry / 10) + Math.floor(sum / 10);
    }
    if (carry > 0) digits.unshift(...String(carry).split('').map(Number));
    return digits.join('');
}

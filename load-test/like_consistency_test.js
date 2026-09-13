import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';
import exec from 'k6/execution';
import { check, fail } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ITEM_ID = Number(__ENV.ITEM_ID || '60471');
const TOKEN_SECRET = 'my-very-long-secret-key-1234567890123456';
const BYPASS_JWT = __ENV.BYPASS_JWT === 'true';
const EXPECTED_REQUESTS = 5000;

const memberIds = new SharedArray('member IDs', () => {
    const rows = JSON.parse(open(__ENV.MEMBER_IDS_FILE || './member-ids.json'));
    return rows.map((row) => String(row.member_id ?? row.memberId ?? row));
});

const likeRequests = new Counter('like_requests');
const likeSuccesses = new Counter('like_successes');
const likeFailures = new Counter('like_failures');
const likeResponseTime = new Trend('like_response_time_ms', true);
const likeErrorRate = new Rate('like_error_rate');

export const options = {
    scenarios: {
        five_thousand_members_like: {
            executor: 'constant-arrival-rate',
            rate: 500,
            timeUnit: '1s',
            duration: '10s',
            preAllocatedVUs: 1000,
            maxVUs: 5000,
            gracefulStop: '30s',
        },
    },
    thresholds: {
        like_requests: [`count==${EXPECTED_REQUESTS}`],
        like_successes: [`count==${EXPECTED_REQUESTS}`],
        like_failures: ['count==0'],
        like_error_rate: ['rate==0'],
        like_response_time_ms: ['p(95)<500', 'p(99)<1000'],
        dropped_iterations: ['count==0'],
    },
};

export function setup() {
    if (!BYPASS_JWT && !TOKEN_SECRET) {
        fail('TOKEN_SECRET 환경변수가 필요합니다. application.yml의 token.secret과 같은 값을 넣으세요.');
    }
    if (memberIds.length !== EXPECTED_REQUESTS) {
        fail(`member-ids.json에는 정확히 ${EXPECTED_REQUESTS}개의 실제 member_id가 필요합니다. 현재=${memberIds.length}`);
    }

    return { startedAt: new Date().toISOString() };
}

export default function () {
    const index = exec.scenario.iterationInTest;
    const memberId = memberIds[index];

    if (memberId === undefined) {
        likeFailures.add(1);
        likeErrorRate.add(true);
        fail(`iteration ${index}에 대응하는 member_id가 없습니다.`);
    }

    const headers = BYPASS_JWT
        ? {
            'X-Load-Test-Member-Id': memberId,
            'Content-Type': 'application/json',
        }
        : {
            Authorization: `Bearer ${createJwt(memberId)}`,
            'Content-Type': 'application/json',
        };
    const response = http.post(`${BASE_URL}/v1/like/${ITEM_ID}`, null, {
        headers,
        tags: { endpoint: 'POST /v1/like/{itemId}' },
        timeout: '30s',
    });

    likeRequests.add(1);
    likeResponseTime.add(response.timings.duration);

    const success = check(response, {
        '좋아요 응답이 200이다': (res) => res.status === 200,
        '응답 itemId가 일치한다': (res) => parseBody(res)?.itemId === ITEM_ID,
    });

    likeErrorRate.add(!success);
    if (success) {
        likeSuccesses.add(1);
    } else {
        likeFailures.add(1);
        console.error(`memberId=${memberId}, status=${response.status}, body=${response.body}`);
    }
}

export function teardown(data) {
    console.log(`좋아요 부하 테스트 완료: itemId=${ITEM_ID}, 시작=${data.startedAt}`);
    console.log('이제 like_consistency_verify.sql을 실행해 DB의 like_count=5000인지 확인하세요.');
}

export function handleSummary(data) {
    const sent = value(data, 'like_requests', 'count');
    const succeeded = value(data, 'like_successes', 'count');
    const failed = value(data, 'like_failures', 'count');
    const dropped = value(data, 'dropped_iterations', 'count');

    const summary = {
        test: {
            itemId: ITEM_ID,
            targetRequests: EXPECTED_REQUESTS,
            ratePerSecond: 500,
            durationSeconds: 10,
            jwtBypassed: BYPASS_JWT,
        },
        requests: {
            sent,
            succeeded,
            failed,
            dropped,
            httpFailureRate: value(data, 'http_req_failed', 'rate'),
        },
        responseTimeMs: {
            average: rounded(value(data, 'like_response_time_ms', 'avg')),
            median: rounded(value(data, 'like_response_time_ms', 'med')),
            p90: rounded(value(data, 'like_response_time_ms', 'p(90)')),
            p95: rounded(value(data, 'like_response_time_ms', 'p(95)')),
            p99: rounded(value(data, 'like_response_time_ms', 'p(99)')),
            max: rounded(value(data, 'like_response_time_ms', 'max')),
        },
        consistency: {
            expectedLikeCount: EXPECTED_REQUESTS,
            allRequestsSent: sent === EXPECTED_REQUESTS && dropped === 0,
            allRequestsSucceeded: succeeded === EXPECTED_REQUESTS && failed === 0,
            databaseCheck: 'Run load-test/like_consistency_verify.sql after the queue is empty',
            redisKeys: [
                `hot-book::book::${ITEM_ID}::like-count`,
                `hot-book::book::${ITEM_ID}::like-count::version`,
            ],
        },
    };

    const json = JSON.stringify(summary, null, 2);
    return {
        stdout: `\n${json}\n`,
        'load-test/like-test-summary.json': `${json}\n`,
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

function value(data, metricName, field) {
    return data.metrics[metricName]?.values?.[field] ?? 0;
}

function rounded(number) {
    return Math.round(number * 100) / 100;
}

/**
 * bkrc 프로젝트 부하 테스트
 * 대상 API: GET /v1/aladin/books (전체 도서 목록 조회)
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// ── 커스텀 메트릭 ──────────────────────────────────────────
const errorRate      = new Rate('error_rate');       // 에러 비율
const responseTime   = new Trend('response_time_ms'); // 응답시간 트렌드
const successCounter = new Counter('success_count');  // 성공 횟수

// ── 설정 ──────────────────────────────────────────────────
const BASE_URL  = __ENV.BASE_URL  || 'http://localhost:8080';
const USE_CACHE = __ENV.USE_CACHE !== 'false'; // 기본값: true

// ── 부하 시나리오 (3단계) ──────────────────────────────────
export const options = {
    stages: [
        { duration: '30s', target: 10  }, // 1단계: 워밍업 (0 → 10 VU)
        { duration: '1m',  target: 50  }, // 2단계: 본 테스트 (50 VU 유지)
        { duration: '30s', target: 100 }, // 3단계: 스파이크 (100 VU)
        { duration: '30s', target: 0   }, // 4단계: 쿨다운
    ],

    // 성공 기준 (이 기준을 충족하지 못하면 테스트 실패)
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95%의 요청이 500ms 이하
        http_req_failed:   ['rate<0.01'],  // 에러율 1% 미만
        error_rate:        ['rate<0.01'],
    },
};

// ── 메인 테스트 함수 ───────────────────────────────────────
export default function () {

    // 캐시 무효화 헤더 (캐시 없이 테스트할 때 사용)
    const headers = USE_CACHE
        ? { 'Content-Type': 'application/json' }
        : { 'Content-Type': 'application/json', 'Cache-Control': 'no-cache' };

    const res = http.get(`${BASE_URL}/v1/aladin/books`, { headers });

    // ── 응답 검증 ──
    const success = check(res, {
        '✅ status 200':        (r) => r.status === 200,
        '✅ 응답 1초 이하':      (r) => r.timings.duration < 1000,
        '✅ body가 비어있지 않음': (r) => r.body && r.body.length > 0,
        '✅ count 필드 존재':    (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.count !== undefined;
            } catch {
                return false;
            }
        },
    });

    // ── 메트릭 기록 ──
    errorRate.add(!success);
    responseTime.add(res.timings.duration);
    if (success) successCounter.add(1);

    // 유저 간 요청 간격 (실제 사용 패턴 시뮬레이션)
    sleep(Math.random() * 1 + 0.5); // 0.5 ~ 1.5초 랜덤 대기
}

// ── 테스트 시작 전 1회 실행 ────────────────────────────────
export function setup() {
    console.log(`\n========================================`);
    console.log(`  bkrc 부하 테스트 시작`);
    console.log(`  대상: ${BASE_URL}/v1/aladin/books`);
    console.log(`========================================\n`);

    // 서버 상태 확인
    const res = http.get(`${BASE_URL}/v1/aladin/books`);
    if (res.status !== 200) {
        console.error(`❌ 서버 응답 이상: ${res.status}`);
    } else {
        console.log(`✅ 서버 정상 응답 확인 (${res.timings.duration}ms)\n`);
    }
}

// ── 테스트 종료 후 1회 실행 ────────────────────────────────
export function teardown() {
    console.log('\n========================================');
    console.log('  테스트 완료!');
    console.log('  아래 지표를 이력서에 활용하세요:');
    console.log('  - http_req_duration (p95, avg)');
    console.log('  - http_reqs (TPS = iterations/s)');
    console.log('  - error_rate');
    console.log('========================================\n');
}
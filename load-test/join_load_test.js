import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },   // 점진적 증가
        { duration: '1m',  target: 200 },  // 최대 부하
        { duration: '30s', target: 0 },    // 감소
    ],
};

export default function () {
    http.post('http://localhost:8080/v1/member', JSON.stringify({
        loginId: `user_${Math.random()}`,
        password: 'test1234!',
        passwordCheck: 'test1234!',
    }), { headers: { 'Content-Type': 'application/json' } });

    sleep(0.1);
}
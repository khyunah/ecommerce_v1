import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 10 },
        { duration: '30s', target: 30 },
    ],
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)', 'p(99.9)'],
};

export default function () {
    const baseUrl = 'http://localhost:8080';

    const headers = {
        'X-USER-ID': '1'
    };

    // 제품 페이징 조회 테스트
    let response = http.get(`${baseUrl}/api/v1/products?page=0&size=10&sortType=LATEST`, { headers });

    check(response, {
        'status is 200': (r) => r.status === 200
    });

    sleep(1);
}
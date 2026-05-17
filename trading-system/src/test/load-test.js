import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        // Test: Different traders each time - avoids pending order limit
        throughput_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 100 },
                { duration: '20s', target: 500 },
                { duration: '20s', target: 1000 },
                { duration: '10s', target: 0 },
            ],
        }
    },
};

export default function() {
    // Unique trader for each request - avoids pending order limit
    const traderId = `T_${__VU}_${__ITER}_${Date.now()}`;

    const payload = JSON.stringify({
        traderId: traderId,
        stock: 'AAPL',
        sector: 'TECH',
        quantity: 10,
        side: 'BUY'
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const res = http.post('http://localhost:8080/orders', payload, params);

    // Only succeed if order was placed (201) or if pending limit? Actually with unique traders, should always succeed
    check(res, {
        'order placed': (r) => r.status === 201,
    });

    sleep(0.05); // Small delay to avoid overwhelming
}
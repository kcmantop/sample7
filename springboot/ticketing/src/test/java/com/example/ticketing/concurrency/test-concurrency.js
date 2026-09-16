import http from 'k6/http';
import { check } from 'k6';

export const options = {
  // 동시에 1,000명의 유저가 순간적으로 요청 전송
  vus: 1000,
  duration: '10s',
};

export default function () {
  const url = 'http://localhost:8080/api/reservations';
  const payload = JSON.stringify({
    seatId: 1,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-Queue-Token': 'test-active-token',
    },
  };

  const res = http.post(url, payload, params);

  // 성공(200) 또는 예매 실패/락 실패(400, 409, 500) 응답 상태 체크
  check(res, {
    'is status 200 or failure': (r) => r.status === 200 || r.status === 409 || r.status === 400,
  });
}
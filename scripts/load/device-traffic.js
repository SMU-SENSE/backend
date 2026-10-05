import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const tokens = (__ENV.DEVICE_TOKENS || '').split(',').map((value) => value.trim()).filter(Boolean);
const users = Number(__ENV.USERS || 1);
const duration = __ENV.DURATION || '10m';
const heartbeatSeconds = Number(__ENV.HEARTBEAT_SECONDS || 60);
const locationSeconds = Number(__ENV.LOCATION_SECONDS || 10);
const sensorSeconds = Number(__ENV.SENSOR_SECONDS || 5);

if (tokens.length === 0) {
  throw new Error('DEVICE_TOKENS is required. Use comma-separated tokens issued by the pairing flow.');
}

export const options = {
  scenarios: {
    deviceTraffic: { executor: 'constant-vus', vus: users, duration },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
};

let lastHeartbeatAt = 0;
let lastLocationAt = 0;
let lastSensorAt = 0;

function params() {
  const token = tokens[(__VU - 1) % tokens.length];
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    tags: { simulated_user: String((__VU - 1) % users) },
  };
}

function heartbeat() {
  const response = http.post(`${baseUrl}/api/v1/device/heartbeat`, null, params());
  check(response, { 'heartbeat 200': (result) => result.status === 200 });
}

function location() {
  const body = JSON.stringify({ latitude: 37.6026, longitude: 126.9553, accuracyMeters: 15 });
  const response = http.post(`${baseUrl}/api/v1/device/locations`, body, params());
  check(response, { 'location 200': (result) => result.status === 200 });
}

function sensor() {
  const body = JSON.stringify({ type: 'HEART_RATE', numericValue: 80 });
  const response = http.post(`${baseUrl}/api/v1/device/sensor-events`, body, params());
  check(response, { 'sensor 200': (result) => result.status === 200 });
}

export default function deviceTraffic() {
  const now = Date.now();

  if (now - lastHeartbeatAt >= heartbeatSeconds * 1000) {
    heartbeat();
    lastHeartbeatAt = now;
  }
  if (now - lastLocationAt >= locationSeconds * 1000) {
    location();
    lastLocationAt = now;
  }
  if (now - lastSensorAt >= sensorSeconds * 1000) {
    sensor();
    lastSensorAt = now;
  }

  sleep(1);
}

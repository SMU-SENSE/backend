# 말모아 Traffic 분석 및 부하 테스트

## 현재 구현된 계측 기반

- Spring Boot Actuator
- Micrometer와 Prometheus 형식 Metric
- URI·HTTP Method·Status별 요청 수 및 응답시간
- JVM, CPU, Memory, DB Connection Pool Metric
- 현재 SSE 연결 수
- SSE 누적 Open/Close 수
- Event 유형별 SSE 발행 수

주요 Metric:

```text
http_server_requests_seconds_count
http_server_requests_seconds_sum
http_server_requests_seconds_max
malmoa_sse_connections_active
malmoa_sse_connections_opened_total
malmoa_sse_connections_closed_total
malmoa_sse_events_published_total
jvm_memory_used_bytes
process_cpu_usage
hikaricp_connections_active
```

`GET /actuator/health`와 `GET /actuator/info`는 공개된다. `/actuator/metrics/**`와 `/actuator/prometheus`는 기본적으로 인증이 필요하다. Prometheus가 Private Network에서 수집할 때만 `PUBLIC_METRICS_ENABLED=true`로 설정하고, Public Internet에는 노출하지 않는다.

## 1차 부하 테스트 실행

### 준비

1. 테스트 전용 AAC 사용자와 Device를 Pairing한다.
2. 각 가상 사용자에 대응하는 Device Token을 준비한다.
3. 운영 개인정보와 운영 DB를 사용하지 않는다.
4. k6를 설치한다. 도구 선정은 최종 확정이 아니며 현재 Script는 1차 기준선 측정용이다.

### 1명 기준

```powershell
$env:BASE_URL="http://localhost:8080"
$env:DEVICE_TOKENS="token-for-test-device"
$env:USERS="1"
$env:DURATION="10m"
$env:HEARTBEAT_SECONDS="60"
$env:LOCATION_SECONDS="10"
$env:SENSOR_SECONDS="5"
k6 run scripts/load/device-traffic.js
```

### 10 / 100 / 1,000명

`USERS`와 `DEVICE_TOKENS`를 늘려 단계적으로 실행한다. 한 Token을 많은 사용자처럼 공유하면 실제 인증·DB 접근 패턴을 재현하지 못하므로 가능한 한 사용자별 Token을 사용한다.

```powershell
$env:USERS="10"   # 이후 100, 1000
$env:DEVICE_TOKENS="token-1,token-2,token-3"
k6 run scripts/load/device-traffic.js
```

## 반드시 따로 측정할 시나리오

현재 k6 Script는 Heartbeat, Location, Heart-rate API의 기준 부하를 만든다. 다음은 후속 Scenario로 분리한다.

- SSE 10/100/1,000 동시 연결과 재연결 폭주
- Card Usage와 STT Event 쓰기
- 보호자 Board/Report 읽기
- 실제 AI Provider 연결 후 AI 응답시간·오류율·비용
- Safe-zone 외부 위치가 반복될 때 Alert 적재량
- Routine Scheduler의 다중 Instance 중복 실행

## 결과 기록 Template

| 항목 | 1명 | 10명 | 100명 | 1,000명 |
|---|---:|---:|---:|---:|
| 평균 Requests/sec |  |  |  |  |
| P95 latency |  |  |  |  |
| P99 latency |  |  |  |  |
| Error rate |  |  |  |  |
| Network in/out |  |  |  |  |
| CPU peak |  |  |  |  |
| Memory peak |  |  |  |  |
| DB active connections |  |  |  |  |
| SSE active connections |  |  |  |  |

## 통과 기준 초안

아래 값은 제품의 최종 SLO가 아니라 첫 측정을 위한 임시 기준이다.

- Error rate < 1%
- P95 < 500ms
- P99 < 1,000ms
- DB Connection Pool 고갈 없음
- SSE 연결 수가 종료 후 원래 값으로 복귀
- 부하 종료 후 Memory가 지속 증가하지 않음

측정 결과를 얻은 뒤 실제 사용성과 배포 사양을 바탕으로 기준을 확정한다.

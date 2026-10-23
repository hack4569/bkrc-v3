# ELK 로컬 로그 수집

Spring Boot 파일 로그(`logs/bkrc.YYYY-MM-DD.log`) → Logstash → Elasticsearch → Kibana.
Elastic Stack 세 구성 요소는 9.5.5로 고정합니다.

## 실행

프로젝트 루트에서 실행합니다. Docker에 여유 메모리 4GB 이상을 권장합니다.

```sh
mkdir -p logs
docker compose -f docker-compose.elk.yml up -d
docker compose -f docker-compose.elk.yml ps
```

IDE에서 프로젝트 루트를 작업 디렉터리로 지정하고 애플리케이션을 실행하면
`./logs`의 로그를 자동으로 수집합니다. 기존 로그도 최초 실행 시 읽으며,
읽은 위치는 `logstash-data` 볼륨에 보관해 재시작 시 이어서 수집합니다.
예외 스택 트레이스는 앞의 로그와 하나의 문서로 묶습니다.

Docker 애플리케이션은 기존 Compose의 `./logs:/app/logs` 마운트를 사용합니다.
기존 운영 Compose 실행 시 사용하던 환경 파일 옵션을 유지한 채 재빌드합니다.

```sh
docker compose --env-file .env --env-file n8n/.env up -d --build bkrc
```

ELK는 별도 Compose 프로젝트로 실행하므로 n8n 환경 변수 없이도 실행할 수 있습니다.

## 조회

- Kibana: http://localhost:5601
- Elasticsearch: http://localhost:9200
- 현재 환경에는 `BKRC Logs` Data View를 생성해 두었습니다. 새 환경에서는 아래 절차로 생성합니다.
- Kibana의 **Stack Management → Data Views → Create data view**에서
  `bkrc-logs-*`를 지정하고 시간 필드로 `@timestamp`를 선택합니다.
- **Discover**에서 `service.name : "bkrc"`, `log.level : "ERROR"`,
  `trace.id : "요청 traceId"` 등으로 검색합니다.
- 로그의 시간대는 Asia/Seoul로 해석하고 Elasticsearch에는 UTC로 저장합니다.
  과거 로그는 Discover의 조회 시간 범위를 넓혀 확인합니다.

```sh
docker compose -f docker-compose.elk.yml logs --tail=100 logstash
curl 'http://localhost:9200/_cat/indices/bkrc-logs-*?v'
```

## 종료 및 데이터

```sh
docker compose -f docker-compose.elk.yml down
```

일반 종료는 인덱스와 수집 위치를 보존합니다. `down -v`는 두 볼륨의 데이터를
삭제하며, 다음 실행 시 남아 있는 파일 로그를 다시 수집합니다.
파일 로그는 일별로 회전하며 30일 / 전체 3GB 제한을 적용합니다.
Elasticsearch 인덱스는 자동 삭제하지 않으므로 장기 사용 시 ILM 보존 정책을 설정하세요.

이 구성은 인증 없이 localhost로만 HTTP 포트를 공개하는 로컬 개발용입니다.
공개 서버에서 사용하려면 인증과 TLS, 접근 제어를 먼저 구성해야 합니다.
Linux에서 `vm.max_map_count` 관련 오류가 발생하면 호스트 설정을
Elastic Docker 설치 문서의 요구 값으로 조정한 뒤 재시작하세요.

공식 문서: https://www.elastic.co/docs/reference/logstash/docker

# n8n 코드 리뷰 서버 설치

이 디렉터리는 `bkrc-v3`의 자동 코드 리뷰용 n8n 서버 설정을 설명한다. n8n의 워크플로 데이터는 애플리케이션 DB와 분리된 PostgreSQL 볼륨에 저장된다.

## 1. 환경변수 생성

저장소 루트에서 다음을 실행한다.

```bash
cp n8n/.env.example n8n/.env
openssl rand -hex 32
openssl rand -hex 32
```

첫 번째 난수는 `N8N_DB_PASSWORD`, 두 번째 난수는 `N8N_ENCRYPTION_KEY`에 넣는다. 암호화 키를 잃으면 n8n에 저장한 GitHub/OpenAI credential을 복호화할 수 없으므로 별도로 백업한다.

## 2. 로컬에서 실행

```bash
docker compose --env-file n8n/.env up -d n8n-postgres n8n
docker compose --env-file n8n/.env ps
docker compose --env-file n8n/.env logs -f n8n
```

브라우저에서 `http://localhost:5678`을 열고 owner 계정을 만든다.

이 상태는 워크플로 편집과 수동 실행에는 사용할 수 있지만 GitHub가 `localhost`로 webhook을 보낼 수 없으므로 GitHub Trigger는 동작하지 않는다.

## 3. GitHub webhook용 공개 주소 설정

운영 서버에서는 n8n 앞에 Nginx, Caddy 또는 Cloudflare Tunnel을 두고 HTTPS 도메인을 연결한다. 예를 들어 도메인이 `n8n.example.com`이면 `n8n/.env`를 다음처럼 바꾼다.

```dotenv
N8N_HOST=n8n.example.com
N8N_PROTOCOL=https
N8N_WEBHOOK_URL=https://n8n.example.com/
N8N_EDITOR_BASE_URL=https://n8n.example.com/
```

변경 후 컨테이너를 다시 만든다.

```bash
docker compose --env-file n8n/.env up -d --force-recreate n8n
```

외부에서 `https://n8n.example.com`에 접속되는지 확인한 뒤 GitHub Trigger를 활성화한다.

## 4. n8n credential 생성

n8n의 **Credentials** 메뉴에서 다음 credential을 만든다.

1. GitHub API credential
   - 가능하면 리뷰 전용 GitHub App 또는 봇 계정을 사용한다.
   - Contents: read
   - Pull requests: read/write
   - Checks: read
2. OpenAI API credential
   - API key를 워크플로 또는 `.env`에 직접 입력하지 않는다.

## 5. 코드 리뷰 워크플로 생성 순서

아래 순서로 노드를 연결한다.

```text
GitHub Trigger (Pull Request)
  -> Filter Review Event
  -> Normalize PR Context
  -> Get PR Files
  -> Get Development Guide
  -> Build Review Input
  -> OpenAI Review
  -> Validate Review JSON
  -> Create GitHub Review
```

### GitHub Trigger

- Owner: `hack4569`
- Repository: `bkrc-v3`
- Event: `Pull Request`

`opened`, `reopened`, `synchronize`, `ready_for_review` action만 통과시키고 draft PR은 제외한다.

### 리뷰 입력

GitHub API로 다음 데이터를 수집한다.

- PR 제목과 설명
- base/head SHA
- `GET /repos/hack4569/bkrc-v3/pulls/{pull_number}/files`
- head SHA의 `개발가이드.md`
- 필요하면 `build.gradle`과 `ArchitectureTest.java`
- Jenkins 또는 GitHub Check 결과

모델의 system prompt와 JSON 출력 형식은 저장소 루트의 `개발가이드.md` 9장을 그대로 사용한다. 가이드 내용을 워크플로에 복사해 고정하지 말고 GitHub Contents API로 읽으면 규칙 변경이 자동 반영된다.

### GitHub 리뷰 게시

최종 결과는 다음 API에 한 번의 review로 등록한다.

```text
POST /repos/hack4569/bkrc-v3/pulls/{pull_number}/reviews
```

판정 매핑은 다음과 같다.

| 모델 판정 | GitHub event |
| --- | --- |
| `approve` | `APPROVE` |
| `request_changes` | `REQUEST_CHANGES` |
| `needs_context` | `COMMENT` |

새 코드에 대한 inline comment는 `path`, `line`, `side: RIGHT`를 사용한다. 모델이 반환한 줄이 실제 diff의 변경 줄인지 검증하고, 그렇지 않은 finding은 리뷰 본문에만 포함한다.

## 6. 운영 점검

```bash
docker compose --env-file n8n/.env ps
docker compose --env-file n8n/.env logs --tail=200 n8n
docker compose --env-file n8n/.env exec n8n n8n audit
```

데이터 백업 대상은 `n8n-data`와 `n8n-postgres-data` 볼륨이다. n8n 컨테이너에 프로젝트 디렉터리나 `/var/run/docker.sock`을 마운트하지 않는다. 코드와 diff는 GitHub API로 읽는다.

## 7. 업데이트

운영에서는 `N8N_VERSION=latest`를 계속 사용하지 않는다. 테스트한 버전으로 고정한 후 다음 순서로 업데이트한다.

```bash
docker compose --env-file n8n/.env pull n8n
docker compose --env-file n8n/.env up -d n8n
docker compose --env-file n8n/.env logs --tail=200 n8n
```

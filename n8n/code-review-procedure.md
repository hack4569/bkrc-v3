# PR 이벤트 필터

GitHub에서 전달된 PR 이벤트 중 코드리뷰가 필요한 이벤트만 다음 노드로 전달한다.

## 통과하는 이벤트

| 이벤트 | 의미 |
| --- | --- |
| `opened` | PR 생성 |
| `reopened` | 닫힌 PR 다시 열기 |
| `synchronize` | PR 브랜치에 새 커밋 push |
| `ready_for_review` | Draft PR을 리뷰 가능 상태로 변경 |

라벨·담당자 변경처럼 코드가 바뀌지 않는 이벤트와 Draft PR은 제외한다.

## 주요 검증

- Pull Request payload인지 확인한다.
- 저장소가 `hack4569/bkrc-v3`인지 확인한다.
- Draft PR은 제외한다.
- 허용된 이벤트만 통과시킨다.

조건을 만족하지 않으면 빈 배열을 반환하므로 이후 GitHub API 조회와 OpenAI 호출이 실행되지 않는다.

## 정규화 결과

복잡한 GitHub payload에서 리뷰에 필요한 값만 추출한다.

```json
{
  "owner": "hack4569",
  "repo": "bkrc-v3",
  "pullNumber": 1,
  "title": "PR 제목",
  "description": "PR 설명",
  "author": "작성자",
  "baseSha": "변경 전 기준 커밋",
  "headSha": "PR 최신 커밋"
}
```

- `baseSha`: 변경 전 개발가이드와 기준 코드 조회에 사용한다.
- `headSha`: PR 최신 코드 식별과 리뷰 등록에 사용한다.

## 처리 흐름

```text
GitHub PR 이벤트
  → 이벤트 종류 확인
  → 저장소 및 Draft 여부 확인
  → 필요한 값만 추출
  → 다음 리뷰 노드로 전달
```

# PR 변경 파일 조회

PR 번호를 이용해 GitHub API에서 실제 변경 파일과 diff를 가져온다.

```http
GET /repos/{owner}/{repo}/pulls/{pullNumber}/files
```

## 주요 결과

| 필드 | 의미 |
| --- | --- |
| `filename` | 변경된 파일 경로 |
| `status` | `added`, `modified`, `removed` 등 변경 종류 |
| `additions` | 추가된 줄 수 |
| `deletions` | 삭제된 줄 수 |
| `changes` | 전체 변경 줄 수 |
| `patch` | 실제 변경 내용인 unified diff |

GitHub Trigger에는 PR 기본 정보만 들어 있으므로, AI가 실제 코드 변경을 리뷰하려면 이 노드에서 가져온 `patch`가 필요하다.

```text
PR 번호 확인
  → GitHub API 호출
  → 변경 파일과 diff 조회
  → AI 코드리뷰 입력으로 전달
```

# 변경 파일 정리

GitHub에서 조회한 여러 변경 파일을 AI가 사용하기 좋은 하나의 `files` 배열로 정리한다.

## 주요 역할

- 빌드 결과물, 바이너리, 이미지처럼 리뷰할 필요가 없는 파일을 제외한다.
- 파일 경로, 변경 종류, 변경 줄 수와 실제 diff만 추출한다.
- 파일별 결과를 하나의 `files` 배열로 통합한다.
- diff가 없는 파일은 `missingPatchFiles`에 기록한다.

## 변경 코드 전달

파일 이름만 전달하는 것이 아니라 `patch`를 통해 실제 변경 코드와 주변 문맥도 전달한다.

```json
{
  "filename": "src/main/java/Example.java",
  "status": "modified",
  "additions": 2,
  "deletions": 1,
  "patch": "@@ -10,4 +10,5 @@\n-oldCode();\n+newCode();\n+addedCode();",
  "patchMissing": false
}
```

`patch`에는 다음 정보가 포함된다.

- 변경 전후 줄 번호
- `-`로 표시된 삭제 코드
- `+`로 표시된 추가 코드
- 변경 주변의 문맥 코드

파일 전체 내용은 기본적으로 전달하지 않는다. 대형 파일이나 바이너리처럼 GitHub가 diff를 제공하지 않으면 `patchMissing`을 `true`로 표시하고 AI가 내용을 추측하지 않도록 한다.

```text
파일별 GitHub API 결과
  → 불필요한 파일 제외
  → 변경 코드와 파일 정보 추출
  → files 배열로 통합
  → 개발가이드 및 AI 리뷰 노드로 전달
```

# 개발가이드 조회

AI 코드리뷰의 판단 기준인 `개발가이드.md`를 GitHub에서 가져온다.

```http
GET /repos/{owner}/{repo}/contents/개발가이드.md?ref={baseSha}
```

## 핵심 역할

- 프로젝트의 아키텍처, 코딩, 테스트 규칙을 리뷰 기준으로 제공한다.
- PR 변경 전 기준인 `baseSha`에서 가이드를 조회한다.
- PR이 개발가이드를 함께 수정해 리뷰 기준을 약화시키는 것을 방지한다.

GitHub는 파일 내용을 Base64로 반환한다.

```json
{
  "name": "개발가이드.md",
  "encoding": "base64",
  "content": "IyBDaGFwdGVyc29mdS..."
}
```

다음 `리뷰 입력 생성` 노드에서 `content`를 일반 문자열로 디코딩한다.

```text
baseSha 확인
  → 개발가이드 조회
  → Base64 content 반환
  → 리뷰 입력 생성 노드에서 디코딩
```

# 리뷰 입력 생성

앞선 노드에서 수집한 변경 코드와 개발가이드를 하나의 AI 리뷰 입력으로 결합한다.

## 핵심 역할

- Base64로 전달된 개발가이드를 일반 문자열로 디코딩한다.
- PR 제목, 설명, base/head SHA를 포함한다.
- `files[].patch`에 있는 실제 변경 코드를 포함한다.
- diff가 없는 파일 목록을 포함한다.
- 개발가이드가 없거나 디코딩에 실패하면 AI 호출 전에 중단한다.

```json
{
  "title": "PR 제목",
  "description": "PR 설명",
  "baseSha": "변경 전 커밋",
  "headSha": "PR 최신 커밋",
  "files": [
    {
      "filename": "src/main/java/Example.java",
      "patch": "@@ ..."
    }
  ],
  "missingPatchFiles": [],
  "developmentGuide": "# Chaptersofu 개발 가이드..."
}
```

이 노드는 AI를 직접 호출하지 않는다. 다음 OpenAI 노드가 아래 내용을 프롬프트에 넣어 실제 리뷰를 수행한다.

```javascript
{{ $json.developmentGuide }}
{{ JSON.stringify($json.files, null, 2) }}
```

```text
PR 정보
+ 변경 코드 patch
+ 개발가이드 전문
  → AI 전달용 JSON 생성
  → OpenAI 리뷰 노드에 전달
```

# GitHub 리뷰 등록

AI가 생성한 리뷰 결과를 실제 GitHub PR Review로 게시한다.

```http
POST /repos/{owner}/{repo}/pulls/{pullNumber}/reviews
```

## 판정 매핑

| AI 판정 | GitHub Review |
| --- | --- |
| `approve` | `APPROVE` |
| `request_changes` | `REQUEST_CHANGES` |
| `needs_context` | `COMMENT` |

## 주요 역할

- 리뷰 요약을 PR 본문에 게시한다.
- finding을 해당 파일과 변경 줄에 inline comment로 등록한다.
- `headSha`를 사용해 리뷰 대상 커밋을 지정한다.

```json
{
  "commit_id": "PR의 headSha",
  "event": "REQUEST_CHANGES",
  "body": "자동 코드리뷰 요약",
  "comments": [
    {
      "path": "src/main/java/Example.java",
      "line": 25,
      "side": "RIGHT",
      "body": "[P1] 권한 검증이 누락되었습니다."
    }
  ]
}
```

`line`은 실제 PR diff에 포함된 새 파일의 줄이어야 한다. 변경되지 않은 줄을 지정하면 GitHub가 `422 Validation failed`를 반환할 수 있다.

```text
AI 리뷰 결과
  → GitHub Review 형식으로 변환
  → 종합 의견과 inline comment 등록
```

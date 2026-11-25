#!/bin/bash
# PostToolUse(Write) 훅: Claude가 src/main/java에 새 Java 클래스를 만들면 대응 테스트가 있는지 확인하고,
# 없으면 Claude에게 테스트 작성을 지시한다.
# - 훅 자체는 테스트를 생성하지 않는다. 테스트 품질은 프로젝트 맥락을 아는 Claude가 책임지도록 지시만 전달한다.
# - decision=block은 이미 끝난 파일 생성을 취소하지 않고, reason을 Claude에게 다음 작업으로 전달하는 용도다.
# - Claude의 Write 도구로 만든 파일에만 동작한다. IDE에서 직접 만든 파일은 감지하지 않는다.

input=$(cat)

# 파일 경로는 tool_response에 정규화된 값이 있으면 우선 사용한다.
file=$(printf '%s' "$input" | jq -r '.tool_response.filePath // .tool_input.file_path // empty')

# Write는 새 파일 생성과 기존 파일 덮어쓰기를 모두 처리하므로, 덮어쓰기(update)는 대상에서 제외한다.
write_type=$(printf '%s' "$input" | jq -r '.tool_response.type // empty')
[ "$write_type" = "update" ] && exit 0

# main 소스의 Java 파일만 대상으로 한다. 테스트 파일을 만들 때 다시 실행되어도 여기서 걸러져 무한 반복되지 않는다.
[[ "$file" == */src/main/java/*.java ]] || exit 0
[ -f "$file" ] || exit 0

name=$(basename "$file" .java)

# 로직이 없어 단위 테스트 가치가 낮은 클래스는 제외한다. 팀 기준에 맞게 조정한다.
if [[ "$file" == */dto/* || "$file" == */config/* || "$file" == */provided/* \
      || "$name" == *Exception || "$name" == *Application || "$name" == *Constants ]]; then
  exit 0
fi

# 인터페이스, enum, record, 애노테이션 선언은 테스트할 동작이 거의 없으므로 제외한다.
if grep -Eq '^(public[[:space:]]+)?(sealed[[:space:]]+)?(interface|enum|record|@interface)[[:space:]]' "$file"; then
  exit 0
fi

# src/main/java/a/b/Foo.java → src/test/java/a/b/FooTest.java
# 주의: macOS 기본 bash 3.2는 ${var/\/a\//b} 치환에서 '\/'를 그대로 남기므로, 접두/접미 제거 방식으로 경로를 조립한다.
project_root="${file%%/src/main/java/*}"
relative_path="${file#*/src/main/java/}"
test_file="${project_root}/src/test/java/${relative_path%.java}Test.java"

# 이미 테스트가 있으면 덮어쓰지 않도록 아무것도 하지 않는다.
[ -f "$test_file" ] && exit 0

reason="새 클래스 ${name}에 대한 테스트가 없습니다. 구현을 마친 뒤 ${test_file} 를 작성하세요.
- JUnit5 + AssertJ를 사용하고, 테스트 메서드 이름은 한국어 snake 형식(예: 검색_결과가_없으면_빈_목록을_반환한다)으로 작성합니다.
- 외부 API 클라이언트는 MockRestServiceServer 패턴(Yes24ClientTest 참고)을 따르고, 의존성은 Mockito 등으로 격리합니다.
- 정상 경로와 실패/경계 경로를 함께 검증하고, AGENTS.md 규칙대로 테스트 의도를 한국어 주석으로 남깁니다.
- 작성 후 ./gradlew test --tests '*${name}Test' 로 실행해 결과를 보고합니다.
- 테스트할 로직이 없는 클래스라면 이유를 사용자에게 설명하고 건너뛰어도 됩니다."

jq -n --arg r "$reason" '{decision: "block", reason: $r}'

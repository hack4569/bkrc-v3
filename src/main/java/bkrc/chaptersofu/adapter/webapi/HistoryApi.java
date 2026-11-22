package bkrc.chaptersofu.adapter.webapi;

import bkrc.chaptersofu.application.history.provided.HistoryRegister;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "히스토리 (History)", description = "사용자 열람 이력 관리 API")
@RestController
@RequiredArgsConstructor
@Validated
public class HistoryApi {
    private final HistoryRegister historyRegister;

    @PostMapping("/v1/history")
    public void saveHistory(
            @AuthenticationPrincipal Long memberId,
            @RequestParam @NotNull(message = "bookId는 필수입니다.") Long bookId) {
        // 외부 itemId가 아닌 내부 bookId를 저장해 판매처 간 ID 충돌을 방지한다.
        historyRegister.saveHistory(bookId, memberId);
    }
}

package com.bkrc.bkrcv3.adapter.webapi;

import com.bkrc.bkrcv3.history.application.provided.HistoryRegister;
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
            @RequestParam @NotNull(message = "itemId는 필수입니다.") Integer itemId) {
        historyRegister.saveHistory(itemId, memberId);
    }
}

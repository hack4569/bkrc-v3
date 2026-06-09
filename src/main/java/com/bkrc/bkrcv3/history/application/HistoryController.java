package com.bkrc.bkrcv3.history.application;

import com.bkrc.bkrcv3.common.shared.Snowflake;
import com.bkrc.bkrcv3.history.entity.History;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "히스토리 (History)", description = "사용자 열람 이력 관리 API")
@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class HistoryController {

    private final HistoryService historyService;

    @PostMapping("/v1/history")
    public void saveHistory(
            @AuthenticationPrincipal Long memberId,
            @RequestParam @NotNull(message = "itemId는 필수입니다.") Integer itemId
    ) {
        historyService.saveHistory(itemId, memberId);
    }
}

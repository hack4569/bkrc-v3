package com.bkrc.bkrcv3.history.application;

import com.bkrc.bkrcv3.history.entity.History;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoryResponse {
    private Long historyId;
    private String loginId;
    private int itemId;
    private LocalDateTime createdAt;

    public static HistoryResponse of(History history) {
        HistoryResponse response = new HistoryResponse();
        response.setLoginId(history.getLoginId());
        return response;
    }
}

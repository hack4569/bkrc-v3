package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import org.springframework.stereotype.Component;

/**
 * Aladin 도메인 엔티티 ↔ 애플리케이션 DTO 변환.
 * 변환 책임을 application 레이어에 두어 도메인 엔티티가 DTO에 의존하지 않도록 한다.
 */
@Component
public class AladinMapper {

    public AladinBookResponse toResponse(AladinBook book) {
        return book == null ? null : AladinBookResponse.from(book);
    }
}

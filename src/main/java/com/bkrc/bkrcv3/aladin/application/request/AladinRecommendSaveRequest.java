package com.bkrc.bkrcv3.aladin.application.request;

import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import lombok.Builder;

import java.util.List;

@Builder
public record AladinRecommendSaveRequest(List<AladinBook> newAladinBooks) {
}

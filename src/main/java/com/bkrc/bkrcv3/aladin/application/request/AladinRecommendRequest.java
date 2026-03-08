package com.bkrc.bkrcv3.aladin.application.request;

import com.bkrc.bkrcv3.aladin.entity.AladinBook;

import java.util.List;

public record AladinRecommendRequest(List<AladinBook> newAladinBooks) {
}

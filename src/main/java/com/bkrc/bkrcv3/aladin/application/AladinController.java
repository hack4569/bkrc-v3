package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendForUserRequest;
import com.bkrc.bkrcv3.aladin.application.request.AladinRecommendSaveRequest;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.application.response.AladinBookPageResponse;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.history.entity.History;
import com.bkrc.bkrcv3.member.application.response.RecommendView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AladinController {
    private final AladinService aladinService;

    @GetMapping("/v1/aladin/books/recommend/user")
    public List<RecommendView> getRecommendbooksForUser(@AuthenticationPrincipal String loginId, AladinRecommendForUserRequest request) {
        List<RecommendView> recommendViewList = aladinService.getRecommendBooksForUser(loginId, request);
        return recommendViewList;
    }

    @GetMapping("/v1/aladin/books")
    public AladinBookPageResponse getAllBooks() {
        return aladinService.findAll();
    }

    @PostMapping("/v1/aladin/books/recommend")
    public List<AladinBook> addRecommendBook(@RequestBody AladinRecommendSaveRequest request) {
        return aladinService.saveNewAladinBooks(request);
    }
}

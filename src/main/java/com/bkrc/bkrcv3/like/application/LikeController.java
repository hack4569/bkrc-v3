package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.like.application.response.LikeResponse;
import com.bkrc.bkrcv3.like.entity.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/v1/like/{itemId}")
    public LikeResponse likeAction(@AuthenticationPrincipal String loginId, @PathVariable("itemId") int itemId) {

        Like likeResult = likeService.like(Like.create(itemId, loginId));
        return LikeResponse.from(likeResult);
    }
}

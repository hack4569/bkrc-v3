package com.bkrc.bkrcv3.like.application.response;

import com.bkrc.bkrcv3.like.entity.Like;


public record LikeResponse(Integer itemId, String loginId){
    public static LikeResponse from(Like like){
        return new LikeResponse(like.getItemId(), like.getLoginId());
    }
}

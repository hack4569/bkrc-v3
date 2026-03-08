package com.bkrc.bkrcv3.aladin.application.request;

import com.bkrc.bkrcv3.common.constants.RcmdConst;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AladinRequest {
    private int maxResults = 100;
    private int showBooksCount = RcmdConst.SHOW_BOOKS_COUNT;
    private long memberId;
    private String itemId;
    @Builder.Default
    private String itemIdType = "ISBN13";
    private int start = 1;
    private String cover;
    @Builder.Default
    private String searchTarget = "Book";
    @Builder.Default
    private String querytype = "Bestseller";
    private String filterType;
    @Builder.Default
    private String output = "js";
    @Builder.Default
    private String version = "20131101";
    private String query;
    @Builder.Default
    private String optResult = "ebookList,usedList,reviewList,fulldescription,fulldescription2,phraseList,mdrecommend,toc";

    public MultiValueMap<String, String> getApiParamMap(){
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        try {
            Object obj = this;
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if(!ObjectUtils.isEmpty(field.get(obj))){
                    map.set(field.getName(),field.get(obj).toString());
                }
            }
        }
        catch (Exception e)
        {
            log.error("AladinApiParam getApiParamMap error : {}", e.getMessage(), e);
        }
        return map;
    }

//    public static AladinRequest create(RecommendRequest recommendRequest){
//        AladinRequest aladinRequest = AladinRequest.builder()
//                .querytype(recommendRequest.getQueryType())
//                .start(recommendRequest.getStart())
//                .build();
//        return aladinRequest;
//    }
    public static AladinRequest create(String itemId){
        AladinRequest aladinRequest = AladinRequest.builder()
                .itemId(itemId)
                .build();
        return aladinRequest;
    }

    public void nextPage() {
        start+= 1;
    }
}

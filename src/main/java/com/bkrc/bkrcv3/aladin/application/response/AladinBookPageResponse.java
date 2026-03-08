package com.bkrc.bkrcv3.aladin.application.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AladinBookPageResponse {

    private List<AladinBookResponse> aladinBookResponseList;
    private int count;

    public static AladinBookPageResponse of(final List<AladinBookResponse> aladinBookResponseList, final int count) {
        AladinBookPageResponse aladinBookPageResponse = new AladinBookPageResponse();
        aladinBookPageResponse.aladinBookResponseList = aladinBookResponseList;
        aladinBookPageResponse.count = count;
        return aladinBookPageResponse;
    }
}

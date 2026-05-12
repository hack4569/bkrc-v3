package com.bkrc.bkrcv3.aladin.application.response;

import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AladinResponse {
    private Integer version;
    private String logo;
    private String title;
    private String link;
    private String pubDate;
    private Integer totalResults;
    private Integer startIndex;
    private Integer itemsPerPage;
    private String query;
    private Integer searchCategoryId;
    private String searchCategoryName;
    private List<AladinBook> item;
}

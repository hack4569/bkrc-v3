package com.bkrc.bkrcv3.aladin.entity;

public enum QueryType {
    ITEM_NEW_ALL("ItemNewAll", "신간 전체 리스트"),
    ITEM_NEW_SPECIAL("ItemNewSpecial", "주목할 만한 신간 리스트"),
    ITEM_EDITOR_CHOICE("ItemEditorChoice", "편집자 추천 리스트"),
    BEST_SELLER("Bestseller", "베스트셀러"),
    BLOG_BEST("BlogBest", "블로거 베스트셀러");

    private final String queryType;
    private final String description;

    QueryType(String queryType, String description) {
        this.queryType = queryType;
        this.description = description;
    }

    public String getQueryType() {
        return queryType;
    }
}

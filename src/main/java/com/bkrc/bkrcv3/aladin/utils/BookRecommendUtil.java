package com.bkrc.bkrcv3.aladin.utils;

import org.springframework.util.StringUtils;

public class BookRecommendUtil {
    public static String filterStr(String originStr){
        String filteredContent = "";

        if(StringUtils.hasText(originStr)){
            //filteredContent =originStr.replaceAll("<[^>]*>","");
            //모든 태그제거
            filteredContent =originStr.replaceAll("<[^>]*>","");

        }

        return filteredContent;
    }
}

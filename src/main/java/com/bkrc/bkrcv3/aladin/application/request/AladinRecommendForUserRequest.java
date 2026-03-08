package com.bkrc.bkrcv3.aladin.application.request;

import com.bkrc.bkrcv3.history.application.HistoryResponse;
import com.bkrc.bkrcv3.history.entity.History;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AladinRecommendForUserRequest {

        //private CategoryDto categoryDto;
        private List<History> histories;
        private HashSet<Integer> cids;
        private String queryType = "bestseller";
        private int start;

//        public static RecommendRequest create(CategoryService categoryService) {
//            RecommendRequest request = new RecommendRequest();
//            List<Category> categories = categoryService.findAcceptedCategories();
//            request.setCids(categories.stream().map(Category::getCid).collect(Collectors.toCollection(HashSet::new)));
//            return request;
//        }
//
//        public void nextPage() {
//            start+= 1;
//        }
}

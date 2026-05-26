package com.bkrc.bkrcv3.aladin;

import com.bkrc.bkrcv3.aladin.application.AladinService;
import com.bkrc.bkrcv3.aladin.application.request.AladinRequest;
import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import com.bkrc.bkrcv3.aladin.entity.AladinConstants;
import com.bkrc.bkrcv3.aladin.entity.QueryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public record AladinBookTestFixture(AladinService aladinService) {
    public List<AladinBook> getAladinBooksForTest() {
        return aladinService.getAladinItemList(
                AladinRequest.builder()
                        .querytype(QueryType.BEST_SELLER.getQueryType())
                        .maxResults(AladinConstants.ITEM_LIST_PAGE)
                        .start(1).build()
        );
    }
}

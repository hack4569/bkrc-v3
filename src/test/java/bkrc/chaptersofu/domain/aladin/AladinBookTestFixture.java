package bkrc.chaptersofu.domain.aladin;

import bkrc.chaptersofu.application.aladin.AladinService;
import bkrc.chaptersofu.application.aladin.provided.AladinRequest;
import bkrc.chaptersofu.domain.aladin.AladinBook;
import bkrc.chaptersofu.domain.aladin.AladinConstants;
import bkrc.chaptersofu.domain.aladin.QueryType;
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

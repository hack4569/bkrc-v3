package bkrc.chaptersofu.application.bkrc;

import bkrc.chaptersofu.application.bkrc.provided.BkrcFinder;
import bkrc.chaptersofu.application.bkrc.provided.BkrcRequest;
import bkrc.chaptersofu.application.required.Ai;
import bkrc.chaptersofu.application.required.BookInfo;
import bkrc.chaptersofu.domain.aladin.*;
import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.common.constants.RcmdConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BkrcService implements BkrcFinder {
    private final BookInfo bookInfo;
    private final Ai ai;
    private final BookRepository bookRepository;

    @Override
    public List<Book> getBestSeller(BkrcRequest request) {
        return bookInfo.getBestSellerBooks(
                request.getMaxResults(),
                request.getStart()
        );
    }

    @Override
    public Book getBook(Long bookId) {
        // 인기 도서에 저장된 ID가 삭제된 경우 호출부에서 제외할 수 있도록 null을 반환한다.
        return bookRepository.findById(bookId).orElse(null);
    }

    @Override
    public List<Book> searchBooks(String query) {
        // 외부 도서 공급자와 무관한 공통 도서 타입으로 검색 결과를 처리한다.
        List<Book> books = bookInfo.searchBooks(query);
        if (CollectionUtils.isEmpty(books)) {
            return List.of();
        }

        return books;
    }

    @Override
    public Book settingBookDetail(String isbn13) {
        var bookDetail = bookInfo.getBookDetail(isbn13);
        //코멘트 세팅
        this.filterContentsByAi(bookDetail);
        return bookDetail;
    }

    //ai 적용
    private void filterContentsByAi(Book bookDetail) {
        List<BookComment> bookCommentList = new ArrayList<>();

        String description = bookDetail.getDescForRecommend();
        if (StringUtils.hasText(description)) {
            var summary = ai.summarizeDescriptions(description);
            if (StringUtils.hasText(summary.overview())) {
                bookCommentList.add(BookComment.create(summary.overview(), "description"));
            }
            if (StringUtils.hasText(summary.insight())) {
                bookCommentList.add(BookComment.create(summary.insight(), "descriptionInsight"));
            }
        }

        List<MdRecommend> mdRecommendList = bookDetail.getSubInfo().getMdRecommendList();
        if (!ObjectUtils.isEmpty(mdRecommendList)) {
            for (MdRecommend mdRecommend : mdRecommendList) {
                bookCommentList.add(BookComment.create(ai.filteringContent(mdRecommend.getComment()), "mdRecommend"));
            }
        }

        List<String> recommendations = ai.getRecommend(bookDetail.getTitle());
        if (!CollectionUtils.isEmpty(recommendations)) {
            bookCommentList.add(BookComment.create(String.join("<br>", recommendations), "aiRecommend"));
        }

        List<Phrase> phraseList = bookDetail.getSubInfo().getPhraseList();
        if (!ObjectUtils.isEmpty(phraseList)) {
            for (int i = 1; i < phraseList.size(); i++) {
                String filteredPhrase = stripHtmlTags(phraseList.get(i).getPhrase());
                if (!StringUtils.hasText(filteredPhrase)) continue;

                String[] phrases = filteredPhrase.split("\\.");
                StringBuilder phraseContent = new StringBuilder();
                int phraseCount = Math.min(phrases.length, RcmdConst.paragraphSlide);
                for (int j = 0; j < phraseCount; j++) {
                    phraseContent.append(phrases[j]).append(". ");
                }
                bookCommentList.add(BookComment.create(phraseContent.toString(), "phrase"));
            }
        }

        String toc = bookDetail.getSubInfo().getToc();
        if (StringUtils.hasText(toc)) {
            String filteredToc = toc.replaceAll("<(/)?([pP]*)(\\s[pP]*=[^>]*)?(\\s)*(/)?>", "");
            bookCommentList.add(BookComment.create(filteredToc, "toc"));
        }

        bookDetail.settingBookCommentList(bookCommentList);
    }

    private String stripHtmlTags(String value) {
        if (!StringUtils.hasText(value)) return "";
        return value.replaceAll("<[^>]*>", "");
    }
}

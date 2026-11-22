package bkrc.chaptersofu.application.bkrc;

import bkrc.chaptersofu.domain.book.Book;
import bkrc.chaptersofu.domain.book.BookMall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByMallNameAndItemId(BookMall mallName, Integer itemId);

    @Query("SELECT DISTINCT a FROM Book a LEFT JOIN FETCH a.bookCommentList")
    // 공통 Book 엔티티 조회 결과를 그대로 반환해 공급자 전용 타입 변환을 제거한다.
    List<Book> findAllWithBookComments();

    @Query("SELECT a.isbn13 FROM Book a")
    Set<String> findAllIsbn13s();

    @Query("SELECT DISTINCT a FROM Book a LEFT JOIN FETCH a.bookCommentList WHERE a.bookId IN :bookIds")
    List<Book> findAllByBookIdIn(@Param("bookIds") Collection<Long> bookIds);
}

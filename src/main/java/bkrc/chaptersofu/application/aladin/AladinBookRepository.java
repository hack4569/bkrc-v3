package bkrc.chaptersofu.application.aladin;

import bkrc.chaptersofu.domain.aladin.AladinBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AladinBookRepository extends JpaRepository<AladinBook, Integer> {

    // 코멘트 조회와 ISBN 중복 확인은 공통 BookRepository로 이전되었으므로 레거시 파생 쿼리 메서드는 두지 않는다.
    // 주의: @Query 없이 메서드를 추가하면 Spring Data가 이름으로 쿼리를 만들다 앱 기동이 실패한다.

//    @Query("SELECT DISTINCT a FROM AladinBook a LEFT JOIN FETCH a.bookCommentList WHERE a.itemId IN :itemIds")
//    List<AladinBook> findAllByItemIdIn(@Param("itemIds") Collection<Integer> itemIds);
}

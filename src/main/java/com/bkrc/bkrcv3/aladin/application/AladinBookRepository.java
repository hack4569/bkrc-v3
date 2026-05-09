package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.entity.AladinBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AladinBookRepository extends JpaRepository<AladinBook, Integer> {

    @Query("SELECT DISTINCT a FROM AladinBook a LEFT JOIN FETCH a.bookCommentList ORDER BY a.itemId DESC")
    List<AladinBook> findAllWithBookComments();

    @Query("SELECT a.isbn13 FROM AladinBook a")
    Set<String> findAllIsbn13s();
}

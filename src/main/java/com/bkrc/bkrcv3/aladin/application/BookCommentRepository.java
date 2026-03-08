package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.entity.BookComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {
}

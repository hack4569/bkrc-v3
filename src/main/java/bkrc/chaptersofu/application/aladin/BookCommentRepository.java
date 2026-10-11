package bkrc.chaptersofu.application.aladin;

import bkrc.chaptersofu.domain.aladin.BookComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {
}

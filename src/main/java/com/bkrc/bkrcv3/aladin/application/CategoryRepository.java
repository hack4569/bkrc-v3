package com.bkrc.bkrcv3.aladin.application;

import com.bkrc.bkrcv3.aladin.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findCategoriesByDepth1In(List<String> depth1s);
}

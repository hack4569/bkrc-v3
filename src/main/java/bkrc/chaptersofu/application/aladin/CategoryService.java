package bkrc.chaptersofu.application.aladin;

import bkrc.chaptersofu.domain.aladin.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final Environment environment;

    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories", key = "'accepted'")
    public List<Category> findAcceptedCategories() {
        String aladinAcceptCategory = environment.getProperty("aladin.accept-category-depth1");
        List<String> depth1s = Arrays.stream(aladinAcceptCategory.split(",")).collect(Collectors.toList());
        List<Category> categories = categoryRepository.findCategoriesByDepth1In(depth1s);
        return categories;
    }
}

package bkrc.chaptersofu.application.aladin;

import bkrc.chaptersofu.domain.aladin.Category;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CategoryServiceTest {
    @Test
    void 설정된_대분류로_허용_카테고리를_조회한다() {
        Environment environment = mock(Environment.class);
        CategoryRepository repository = mock(CategoryRepository.class);
        CategoryService service = new CategoryService(environment, repository);
        List<Category> categories = List.of(Category.builder().depth1("소설").build());
        when(environment.getProperty("aladin.accept-category-depth1")).thenReturn("소설,경제경영");
        when(repository.findCategoriesByDepth1In(List.of("소설", "경제경영"))).thenReturn(categories);

        assertThat(service.findAcceptedCategories()).isSameAs(categories);
        verify(repository).findCategoriesByDepth1In(List.of("소설", "경제경영"));
    }
}

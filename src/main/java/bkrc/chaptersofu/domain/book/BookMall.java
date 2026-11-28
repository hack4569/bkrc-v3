package bkrc.chaptersofu.domain.book;

/**
 * 외부 도서 정보를 제공한 판매처를 구분한다.
 * 동일한 itemId가 판매처마다 존재할 수 있으므로 영속 식별자와 함께 중복 방지 기준으로 사용한다.
 */
public enum BookMall {
    ALADIN,
    YES24
}

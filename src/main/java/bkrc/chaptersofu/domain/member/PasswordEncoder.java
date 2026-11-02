package bkrc.chaptersofu.domain.member;

// 도메인은 암호화 알고리즘과 Spring Security를 알지 않고 이 계약만 사용합니다.
public interface PasswordEncoder {
    String hashPassword(String plainTextPassword);

    boolean checkPassword(String plainTextPassword, String hashedPassword);
}

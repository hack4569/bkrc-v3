package bkrc.chaptersofu.adapter.security;

import bkrc.chaptersofu.domain.member.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

// 하나의 빈이 도메인과 Spring Security 양쪽 계약을 구현해 가입과 로그인 방식을 일치시킵니다.
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder,
        org.springframework.security.crypto.password.PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // 기존 BCrypt 비용을 유지하여 비밀번호 저장 방식이 바뀌지 않도록 합니다.
        return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(12));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }

    @Override
    public String hashPassword(String plainTextPassword) {
        return encode(plainTextPassword);
    }

    @Override
    public boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return matches(plainTextPassword, hashedPassword);
    }
}

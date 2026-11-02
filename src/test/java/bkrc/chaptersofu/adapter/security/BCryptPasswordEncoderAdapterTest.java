package bkrc.chaptersofu.adapter.security;

import bkrc.chaptersofu.domain.member.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.bcrypt.BCrypt;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordEncoderAdapterTest {
    @Test
    void 도메인과_로그인이_같은_빈으로_비밀번호를_처리한다() {
        // 두 계약이 같은 빈으로 연결되고 가입 시 만든 해시로 로그인할 수 있는지 검증합니다.
        try (var context = new AnnotationConfigApplicationContext(BCryptPasswordEncoderAdapter.class)) {
            PasswordEncoder domainEncoder = context.getBean(PasswordEncoder.class);
            var securityEncoder = context.getBean(org.springframework.security.crypto.password.PasswordEncoder.class);

            assertThat(domainEncoder).isSameAs(securityEncoder);
            String hash = domainEncoder.hashPassword("password");
            assertThat(hash).startsWith("$2a$12$");
            assertThat(securityEncoder.matches("password", hash)).isTrue();
            assertThat(domainEncoder.checkPassword("wrong-password", hash)).isFalse();
        }
    }

    @Test
    void 기존_BCrypt_해시도_검증할_수_있다() {
        // 기존 구현이 생성한 해시를 새 어댑터에서도 그대로 검증할 수 있어야 합니다.
        String existingHash = BCrypt.hashpw("password", BCrypt.gensalt(12));

        assertThat(new BCryptPasswordEncoderAdapter().checkPassword("password", existingHash)).isTrue();
    }
}

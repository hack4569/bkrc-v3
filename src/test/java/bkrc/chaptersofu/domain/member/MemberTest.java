package bkrc.chaptersofu.domain.member;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MemberTest {
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void 회원을_등록할_때_비밀번호를_해시한다() {
        when(passwordEncoder.hashPassword("password")).thenReturn("hashed");

        Member member = Member.register(1L, "member", "password", passwordEncoder);

        assertThat(member.getMemberId()).isEqualTo(1L);
        assertThat(member.getLoginId()).isEqualTo("member");
        assertThat(member.getPassword()).isEqualTo("hashed");
    }

    @Test
    void 비밀번호를_수정할_때_새_비밀번호를_해시한다() {
        when(passwordEncoder.hashPassword("old")).thenReturn("old-hash");
        when(passwordEncoder.hashPassword("new")).thenReturn("new-hash");
        Member member = Member.register(1L, "member", "old", passwordEncoder);

        member.modify("new", passwordEncoder);

        assertThat(member.getPassword()).isEqualTo("new-hash");
    }

    @Test
    void 입력한_비밀번호가_저장된_해시와_일치하는지_확인한다() {
        when(passwordEncoder.hashPassword("password")).thenReturn("hashed");
        when(passwordEncoder.checkPassword("password", "hashed")).thenReturn(true);
        Member member = Member.register(1L, "member", "password", passwordEncoder);

        assertThat(member.checkPassword("password", passwordEncoder)).isTrue();
    }
}

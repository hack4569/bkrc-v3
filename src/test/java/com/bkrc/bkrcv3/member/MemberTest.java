package com.bkrc.bkrcv3.member;

import com.bkrc.bkrcv3.config.TestConfig;
import com.bkrc.bkrcv3.exception.BusinessException;
import com.bkrc.bkrcv3.member.application.UserService;
import com.bkrc.bkrcv3.member.application.request.MemberModifyRequest;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.dto.MemberDto;
import com.bkrc.bkrcv3.member.entity.Member;
import com.bkrc.bkrcv3.member.entity.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestConfig.class)
@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class MemberTest {
//    @Autowired
//    UserService userService;
//    @Autowired
//    PasswordEncoder passwordEncoder;
//
//    @Test
//    void registerMemberTest() {
//        MemberRegisterRequest request = new MemberRegisterRequest("lsh123", "1234", "1234");
//        Member member = userService.saveMember(request);
//
//        assertThat(member.getMemberId()).isNotNull();
//    }
//
//    @Test
//    void duplicateLoginIdFail() {
//        MemberRegisterRequest request = new MemberRegisterRequest("lsh1234", "1234", "1234");
//        userService.saveMember(request);
//        MemberRegisterRequest request2 = new MemberRegisterRequest("lsh1234", "1234", "1234");
//
//        assertThatThrownBy( () -> userService.saveMember(request2))
//                .isInstanceOf(BusinessException.class);
//    }
//
//    @Test
//    void findMemberTest() {
//        MemberRegisterRequest request = new MemberRegisterRequest("lsh1234", "1234", "1234");
//        Member member = userService.saveMember(request);
//        MemberDto findMember = userService.getMemberByLoginId(member.getLoginId());
//        assertThat(findMember.getMemberId()).isEqualTo(member.getMemberId());
//    }
//
//    @Test
//    void modifyMemberTest() {
//        MemberRegisterRequest saveRequest = new MemberRegisterRequest("lsh123", "1234", "1234");
//        userService.saveMember(saveRequest);
//
//        MemberModifyRequest updateRequest = new MemberModifyRequest("lsh123", "1234", "12345", "12345");
//        var updatedMember = userService.modifyMember(updateRequest.loginId(), updateRequest);
//
//        // isEqualTo 대신 matches로 검증
//        assertThat(passwordEncoder.matches(updateRequest.newPassword(), updatedMember.getPassword())).isTrue();
//    }
}

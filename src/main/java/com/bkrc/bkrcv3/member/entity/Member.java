package com.bkrc.bkrcv3.member.entity;

import com.bkrc.bkrcv3.common.shared.BaseEntity;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name="MEMBER")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @NotEmpty
    @Column(unique = true)
    private String loginId;
    @NotEmpty
    private String password;
    private String memberType;
    private String sessionId;

    @Column(length = 20)
    private String queryType;

    @Column(length = 20)
    private String fiterType;

//    public Boolean checkPassword(String reqPssword, PasswordEncoder passwordEncoder) {
//        if (passwordEncoder.hashPassword(reqPssword).equals(this.password)) {
//            return true;
//        } else {
//            return false;
//        }
//    }
    public static Member register(String loginId, String password, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.loginId = loginId;
        member.password = passwordEncoder.hashPassword(password);
        return member;
    }

    public static Member registerForModify(String loginId, String password, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.loginId = loginId;
        member.password = passwordEncoder.hashPassword(password);
        member.setUpdated(LocalDateTime.now());
        return member;
    }


    public boolean checkPassword(String passwordReq, PasswordEncoder passwordEncoder) {
        return passwordEncoder.checkPassword(passwordReq, this.password);
    }
}

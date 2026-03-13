package com.bkrc.bkrcv3.member.entity;

import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;


@Entity
@Table(name="MEMBER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long memberId;

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

    public static Member register(String loginId, String password, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.loginId = loginId;
        member.password = passwordEncoder.hashPassword(password);
        return member;
    }


    public boolean checkPassword(String passwordReq, PasswordEncoder passwordEncoder) {
        return passwordEncoder.checkPassword(passwordReq, this.password);
    }
}

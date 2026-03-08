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
    @Column(name="member_id")
    private long memberId;

    @NotEmpty
    private String loginId;

    @NotEmpty
    private String password;

    @Column(name = "member_type")
    private String memberType;
    @Column(name="session_id")
    private String sessionId;

    @Column(length = 20, name = "query_type")
    private String queryType;

    @Column(length = 20, name = "filter_type")
    private String fiterType;

    public static Member register(MemberRegisterRequest createRequest, PasswordEncoder passwordEncoder) {
        Member member = new Member();

        member.loginId = createRequest.loginId();
        member.password = passwordEncoder.hashPassword(createRequest.password());

        return member;
    }

    public boolean checkPassword(String passwordReq, PasswordEncoder passwordEncoder) {
        return passwordEncoder.checkPassword(passwordReq, this.password);
    }
}

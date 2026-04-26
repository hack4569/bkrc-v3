package com.bkrc.bkrcv3.member.entity;

import com.bkrc.bkrcv3.common.shared.BaseEntity;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;


@Schema(description = "회원 엔티티")
@Entity
@Table(name="MEMBER")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {
    @Schema(description = "회원 고유 ID (Snowflake)")
    @Id
    private Long memberId;

    @Schema(description = "로그인 ID (유일값)", example = "user123")
    @NotEmpty
    @Column(unique = true)
    private String loginId;

    @Schema(description = "암호화된 비밀번호")
    @NotEmpty
    private String password;

    @Schema(description = "회원 유형", example = "NORMAL")
    private String memberType;

    @Schema(description = "세션 ID")
    private String sessionId;

    @Schema(description = "도서 조회 유형 (bestseller 등)", example = "bestseller")
    @Column(length = 20)
    private String queryType;

    @Schema(description = "도서 필터 유형", example = "IT")
    @Column(length = 20)
    private String fiterType;

//    public Boolean checkPassword(String reqPssword, PasswordEncoder passwordEncoder) {
//        if (passwordEncoder.hashPassword(reqPssword).equals(this.password)) {
//            return true;
//        } else {
//            return false;
//        }
//    }
    public static Member register(Long id, String loginId, String password, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.memberId = id;
        member.loginId = loginId;
        member.password = passwordEncoder.hashPassword(password);
        return member;
    }
    public static Member register(String loginId, String password, PasswordEncoder passwordEncoder) {
        Member member = new Member();

        return member;
    }

    public void modify(String password, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.hashPassword(password);
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

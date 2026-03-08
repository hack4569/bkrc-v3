package com.bkrc.bkrcv3.member.dto;

import lombok.Data;

@Data
public class MemberDto {
    private long memberId;
    private String loginId;
    private String password;
}

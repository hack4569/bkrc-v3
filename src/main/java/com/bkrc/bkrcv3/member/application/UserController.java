package com.bkrc.bkrcv3.member.application;

import com.bkrc.bkrcv3.member.application.request.MemberModifyRequest;
import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.application.response.MemberModifyResponse;
import com.bkrc.bkrcv3.member.application.response.MemberRegisterResponse;
import com.bkrc.bkrcv3.member.entity.PasswordEncoder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/v1/member")
    public MemberRegisterResponse register(@RequestBody @Valid MemberRegisterRequest request) {
        var member = userService.saveMember(request);
        var registeredMember = MemberRegisterResponse.of(member);
        return registeredMember;
    }

    @PutMapping("/v1/member/{loginId}")
    public MemberModifyResponse update(@PathVariable String loginId, @RequestBody @Valid MemberModifyRequest request) {
        var response = userService.modifyMember(loginId, request);
        return MemberModifyResponse.of(response);
    }
}

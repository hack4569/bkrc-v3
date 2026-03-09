package com.bkrc.bkrcv3.member.application;

import com.bkrc.bkrcv3.member.application.request.MemberRegisterRequest;
import com.bkrc.bkrcv3.member.application.response.MemberRegisterResponse;
import com.bkrc.bkrcv3.member.entity.PasswordEncoder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

}

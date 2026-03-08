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
//    @GetMapping("/login")
//    public String login(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
//                        LoginForm loginForm) {
//
//        return "user/login";
//    }
//
//    @PostMapping("/login")
//    public String login(@Valid @ModelAttribute LoginForm form,
//                        BindingResult bindingResult,
//                        HttpServletRequest request,
//                        HttpServletResponse response) throws Exception{
//
//        if (bindingResult.hasErrors()) {
//            return "user/login";
//        }
//        Member loginMember = loginService.login(form.getLoginId());
//        if (loginMember == null || !loginMember.checkPassword(form.getPassword(), passwordEncoder)) {
//            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
//            ScriptUtils.alert(response,"아이디 또는 비밀번호가 맞지 않습니다.");
//            return "user/login";
//        }
//        HttpSession session = request.getSession();
//        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
//        session.setMaxInactiveInterval(1800);
//        SessionUtils.setMemberId(loginMember.getId());
//        if (form.isAutoLogin()) {
//            //세션 아이디 저장
//            loginMember.setSessionId(session.getId());
//            memberRepository.save(loginMember);
//            //쿠키저장
//            Cookie loginCookie = new Cookie(SessionConst.LOGIN_MEMBER, session.getId());
//            loginCookie.setMaxAge(60*60*24*7);
//            loginCookie.setPath("/");
//            response.addCookie(loginCookie);
//        }
//        return "redirect:/";
//    }
//    @GetMapping("/join")
//    public String join(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember
//        , @ModelAttribute("form") MemberRegisterRequest form) {
//
//        return "user/join";
//    }
//
//    @PostMapping("/join")
//    public String joinAction(@Validated @ModelAttribute("form") MemberRegisterRequest form, BindingResult bindingResult, HttpServletResponse response) {
//        try {
//            if (!bindingResult.hasErrors()) {
//                Member member = memberRepository.findMemberByLoginId(form.loginId()).orElse(null);
//                if ( member != null) {
//                    //ScriptUtils.alert(response, form.getLoginId() + " 아이디는 이미 존재합니다.");
//                    bindingResult.reject("joinedMember");
//                }
//                if (!member.checkPassword(form.password(), passwordEncoder)) {
//                    //ScriptUtils.alert(response, "비밀번호가 일치하지 않습니다.");
//                    bindingResult.reject("passwordNotEquals");
//                }
//            }
//
//            if (bindingResult.hasErrors()) {
//                return "user/join";
//            }
//
//            Member member = Member.register(form, passwordEncoder);
//
//            loginService.saveMember(member);
//            //response.sendRedirect("/user/login");
//            //ScriptUtils.alertAndRedirect(response, "회원가입이 완료되었습니다. 좋은 책 찾길 바랍니다^^", "/users/login");
//        } catch (Exception e) {
//            log.error("joinAction error = {}", e.getMessage(), e);
//            return "user/join";
//        }
//        return "redirect:/user/login?msg=joinSuccessMsg";
//    }
//
//    @GetMapping("/logout")
//    public String logout(HttpServletRequest request, HttpServletResponse response) {
//        //기존에 세션이 있으면 가져오고, 없으면 생성하지 않음.
//        HttpSession session = request.getSession(false);
//        if (session != null) {
//            Cookie loginCookie = WebUtils.getCookie(request, SessionConst.LOGIN_MEMBER);
//            if (loginCookie != null) {
//                loginCookie.setPath("/");
//                loginCookie.setMaxAge(0);
//                response.addCookie(loginCookie);
//                Member member = memberRepository.findMemberBySessionId(session.getId()).orElse(null);
//                if (member != null) {
//                    member.setSessionId(null);
//                    memberRepository.save(member);
//                    session.invalidate();
//                }
//            }
//        }
//        return "redirect:/";
//    }
//
//    @GetMapping("/mypage")
//    public String mypage(@Login Member loginMember, Model model, HttpServletResponse response) throws Exception{
//        if (loginMember == null) {
//            ScriptUtils.alert(response,"로그인이 필요합니다.");
//            return "user/login";
//        }
//
//        SpringUtils.addAttributes(
//                model,
//                Map.of("loginMember", loginMember)
//        );
//        return "user/mypage";
//    }

}

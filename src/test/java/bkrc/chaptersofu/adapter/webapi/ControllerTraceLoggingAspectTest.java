package bkrc.chaptersofu.adapter.webapi;

import bkrc.chaptersofu.application.member.provided.MemberRegisterRequest;
import bkrc.chaptersofu.application.recommendation.provided.UpdateRecommendationRequest;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(OutputCaptureExtension.class)
class ControllerTraceLoggingAspectTest {

    private Logger aspectLogger;
    private Level previousLevel;
    private TraceTestController controller;

    @BeforeEach
    void setUp() {
        aspectLogger = (Logger) LoggerFactory.getLogger(ControllerTraceLoggingAspect.class);
        previousLevel = aspectLogger.getLevel();
        aspectLogger.setLevel(Level.TRACE);

        ControllerArgumentSanitizer sanitizer = new ControllerArgumentSanitizer(new ObjectMapper());
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new TraceTestController());
        proxyFactory.addAspect(new ControllerTraceLoggingAspect(sanitizer));
        controller = proxyFactory.getProxy();
    }

    @AfterEach
    void restoreLogLevel() {
        aspectLogger.setLevel(previousLevel);
    }

    @Test
    void 비밀번호_Authorization_업로드파일을_제외하고_요청값을_TRACE로_기록한다(CapturedOutput output) {
        MemberRegisterRequest request = new MemberRegisterRequest(
                "member-login-id",
                "secret-password",
                "secret-password-check"
        );
        MultipartFile file = mock(MultipartFile.class);

        controller.register(request, "Bearer secret-jwt", file);

        assertThat(output.getOut())
                .contains("CONTROLLER_REQUEST")
                .contains("controller=TraceTestController")
                .contains("handler=register")
                .contains("member-login-id")
                .contains("<redacted>")
                .contains("<excluded>")
                .doesNotContain("secret-password")
                .doesNotContain("secret-password-check")
                .doesNotContain("Bearer secret-jwt")
                .contains("CONTROLLER_RESPONSE")
                .contains("resultType=String");
    }

    @Test
    void 추천_내용은_제외하고_기타_식별값만_TRACE로_기록한다(CapturedOutput output) {
        controller.updateRecommendation(10L, 20, new UpdateRecommendationRequest("긴 추천 내용"));

        assertThat(output.getOut())
                .contains("memberId\":10")
                .contains("itemId\":20")
                .contains("recommendation\":\"<redacted>")
                .doesNotContain("긴 추천 내용");
    }

    @RestController
    static class TraceTestController {

        String register(MemberRegisterRequest request, String authorization, MultipartFile file) {
            return "created";
        }

        void updateRecommendation(Long memberId, Integer itemId, UpdateRecommendationRequest request) {
        }
    }
}

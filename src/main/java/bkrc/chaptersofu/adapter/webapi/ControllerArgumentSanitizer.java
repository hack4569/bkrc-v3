package bkrc.chaptersofu.adapter.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Locale;
import java.util.Set;

/** Controller 인자를 로그에 기록하기 전에 비밀 값과 대용량 값을 제거합니다. */
@Component
@RequiredArgsConstructor
class ControllerArgumentSanitizer {

    private static final String REDACTED = "<redacted>";
    private static final String EXCLUDED = "<excluded>";
    private static final int MAX_ARGUMENTS_LENGTH = 2_000;
    private static final Set<String> SENSITIVE_FIELD_NAMES = Set.of(
            "password", "passwordcheck", "originpassword", "newpassword", "newpasswordcheck",
            "token", "jwt", "authorization", "cookie", "cookies", "session", "sessionid",
            "prompt", "prompts", "message", "messages", "recommendation", "content"
    );

    private final ObjectMapper objectMapper;

    String sanitize(String[] parameterNames, Object[] arguments) {
        ObjectNode sanitizedArguments = objectMapper.createObjectNode();

        for (int index = 0; index < arguments.length; index++) {
            String parameterName = parameterName(parameterNames, index);
            Object argument = arguments[index];

            if (isExcludedType(argument)) {
                sanitizedArguments.put(parameterName, EXCLUDED);
            } else if (isSensitive(parameterName)) {
                sanitizedArguments.put(parameterName, REDACTED);
            } else {
                sanitizedArguments.set(parameterName, sanitizeNode(objectMapper.valueToTree(argument)));
            }
        }

        return limitLength(sanitizedArguments.toString());
    }

    private JsonNode sanitizeNode(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return node;
        }

        if (node.isObject()) {
            ObjectNode sanitized = objectMapper.createObjectNode();
            node.properties().forEach(field -> {
                if (isSensitive(field.getKey())) {
                    sanitized.put(field.getKey(), REDACTED);
                } else {
                    sanitized.set(field.getKey(), sanitizeNode(field.getValue()));
                }
            });
            return sanitized;
        }

        if (node.isArray()) {
            ArrayNode sanitized = objectMapper.createArrayNode();
            node.forEach(value -> sanitized.add(sanitizeNode(value)));
            return sanitized;
        }

        return node;
    }

    private boolean isExcludedType(Object argument) {
        return argument instanceof ServletRequest
                || argument instanceof ServletResponse
                || argument instanceof HttpSession
                || argument instanceof Cookie
                || argument instanceof Cookie[]
                || argument instanceof MultipartFile
                || argument instanceof MultipartFile[]
                || argument instanceof Authentication
                || argument instanceof Principal
                || argument instanceof byte[];
    }

    private boolean isSensitive(String name) {
        if (name == null) {
            return false;
        }
        String normalized = name.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_FIELD_NAMES.contains(normalized)
                || normalized.contains("password")
                || normalized.contains("authorization")
                || normalized.contains("sessionid");
    }

    private String parameterName(String[] parameterNames, int index) {
        if (parameterNames != null && index < parameterNames.length) {
            return parameterNames[index];
        }
        return "arg" + index;
    }

    private String limitLength(String value) {
        if (value.length() <= MAX_ARGUMENTS_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_ARGUMENTS_LENGTH) + "...<truncated>";
    }
}

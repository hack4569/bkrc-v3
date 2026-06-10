package com.bkrc.bkrcv3.member.entity;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder implements org.springframework.security.crypto.password.PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(12));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }

    public String hashPassword(String plainTextPassword) {
        return encode(plainTextPassword);
    }

    public boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return matches(plainTextPassword, hashedPassword);
    }
}

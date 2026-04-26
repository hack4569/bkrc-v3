package com.bkrc.bkrcv3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class BkrcV3Application {
    public static void main(String[] args) {
        SpringApplication.run(BkrcV3Application.class, args);
    }
}

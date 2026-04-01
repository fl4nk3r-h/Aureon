package com.aureon.backend.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate(int length) {
        int bound = (int) Math.pow(10, length);
        int otp   = RANDOM.nextInt(bound);
        return String.format("%0" + length + "d", otp);
    }
}

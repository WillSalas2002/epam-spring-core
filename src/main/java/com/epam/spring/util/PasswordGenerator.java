package com.epam.spring.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class PasswordGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$^&*";
    private final Random random = new SecureRandom();

    public String generatePassword() {
        StringBuilder sb = new StringBuilder();
        int length = CHARS.length() - 1;
        for (int i = 0; i < 10; i++) {
            sb.append(CHARS.charAt(random.nextInt(length)));
        }
        return sb.toString();
    }
}
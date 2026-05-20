package com.bkrc.bkrcv3;

public class GeneratorForTest {
    public static String generateLoginId() {
        return java.util.UUID.randomUUID() + "tester";
    }

    public static String generatePassword() {
        return "pass" + java.util.UUID.randomUUID().toString().substring(0, 8) + "!";
    }
}

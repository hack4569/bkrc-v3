package bkrc.chaptersofu.adapter.tests;

import bkrc.chaptersofu.*;

public class GeneratorForTest {
    public static String generateLoginId() {
        return java.util.UUID.randomUUID() + "tester";
    }

    public static String generatePassword() {
        return "pass" + java.util.UUID.randomUUID().toString().substring(0, 8) + "!";
    }

    public static int generateItemId() {
        return (int) (Math.random() * 1000);
    }
}

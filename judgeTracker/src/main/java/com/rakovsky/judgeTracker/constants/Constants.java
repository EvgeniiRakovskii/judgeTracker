package com.rakovsky.judgeTracker.constants;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final Map<String, String> BOT_COMMANDS;

    static {
        BOT_COMMANDS = new HashMap<>();
        BOT_COMMANDS.put("/info", "Привет! Я слежу за изменениями в судебных делах");
        BOT_COMMANDS.put("/examples", "example");

    }

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

}

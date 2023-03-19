package com.rakovsky.judgeTracker.constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Constants {

    public static final Map<String, String> BOT_COMMANDS;
    public static final Set<String> WHITE_LIST_USERS;

    public static final long CHAT_ID = -764133074;
    //TODO move to property
    public static final String PATH_TO_LOCAL_EXCEL = "C:\\Users\\RayS\\IdeaProjects\\judgeTracker\\judgeTracker\\cases.xlsx";

    static {
        BOT_COMMANDS = new HashMap<>();
        BOT_COMMANDS.put("/info", "Привет! Я слежу за изменениями в судебных делах");
        BOT_COMMANDS.put("/upload", "Отправьте мне excel файл, в котором таблица начинается сразу с угла \n" +
                "*колонка А* это custom name\n" +
                "*колонка B* это ссылка на дело\n" +
                "*колонка С* это номер кейса\n");

        WHITE_LIST_USERS = new HashSet<>();
        WHITE_LIST_USERS.add("groolexx");
        WHITE_LIST_USERS.add("evrakovskii");

    }

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

}

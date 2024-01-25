package com.rakovsky.judgeTracker.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.rakovsky.judgeTracker.constants.Constants;
import com.rakovsky.judgeTracker.service.CheckingChangesService;
import com.rakovsky.judgeTracker.service.TelegramBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.rakovsky.judgeTracker.constants.Constants.CHAT_ID;
import static com.rakovsky.judgeTracker.constants.Constants.WHITE_LIST_USERS;


@Component
public class LawyerHelperBot {

    private static final String EXCEL_FILE_FORMAT = "xlsx";
    private static final String BOT_TOKEN = "BOT_TOKEN";
    private static final String BOT_NAME = "BOT_NAME";
    private static boolean canUploadFile;
    private static final Logger logger = LoggerFactory.getLogger(LawyerHelperBot.class);
    // Creating bot passing the token received from @BotFather
    private final TelegramBot bot = new TelegramBot(System.getenv(BOT_TOKEN));
    @Autowired
    private TelegramBotService telegramBotService;
    @Autowired
    private CheckingChangesService checkingChangesService;


    public void serve() {

        // Registering for updates
        bot.setUpdatesListener(updates -> {
            updates.stream().filter(update -> update.message() != null && WHITE_LIST_USERS.contains(update.message().from().username())).forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });


    }

    private void process(Update update) {
        Message message = update.message() == null ? update.channelPost() : update.message();
        if (message == null) {
            return;
        }

        SendMessage sendMessage = null;
        long chatId = message.chat().id();

        if (message.text() != null) {
            logger.info("User {} is sending \"{}\" in chat {}", message.from().username(), message.text(), message.chat().id());
            String messageToBot = message.text().replace(System.getenv(BOT_NAME), "");
            if (messageToBot.equals("/start_check")) {
                sendMessage = new SendMessage(chatId, Constants.BOT_COMMANDS.get(messageToBot.toLowerCase()));
                bot.execute(sendMessage);
                sendResultMessage(checkingChangesService.checkChanges());
                return;
            } else if (Constants.BOT_COMMANDS.containsKey(messageToBot.toLowerCase())) {
                sendMessage = new SendMessage(chatId, Constants.BOT_COMMANDS.get(messageToBot.toLowerCase()));
                if(messageToBot.equals("/upload")) canUploadFile = true;
            } else if (messageToBot.equals("/get_cases")) {
                bot.execute(telegramBotService.getExcelWithCases(chatId));
                return;
            }
        }

        if (message.document() != null && message.document().fileName().contains(EXCEL_FILE_FORMAT) && canUploadFile) {
            logger.info(String.format("User %s is uploading %s in chat %d", message.from().username(), message.document().fileName(), message.chat().id()));

            try {
                GetFile request = new GetFile(message.document().fileId());
                File file = bot.execute(request).file();
                Set<String> unsuccessful = telegramBotService.updateCasesByExcel(bot.getFullFilePath(file));

                sendMessage = new SendMessage(chatId, "File received \n" + (unsuccessful.isEmpty() ? "" : ("Problem with \n" + unsuccessful))).parseMode(ParseMode.MarkdownV2);
            } catch (Exception e) {
                sendMessage = new SendMessage(chatId, e.getMessage());
            }
            canUploadFile = false;
        }

        if (sendMessage != null) {
            bot.execute(sendMessage);
        }
    }

    public void sendResultMessage(Set<String> differences, Set<String> unsuccessful) {
        String resultMessage = telegramBotService.getResultMessage(differences, unsuccessful);

        SendMessage sendMessage = new SendMessage(CHAT_ID, resultMessage).parseMode(ParseMode.MarkdownV2);

        bot.execute(sendMessage);
    }

    public void sendResultMessage(Set<String> differences) {

        String resultMessage = telegramBotService.getResultMessage(differences);


        SendMessage sendMessage = new SendMessage(CHAT_ID, resultMessage).parseMode(ParseMode.MarkdownV2);


        bot.execute(sendMessage);
    }

}
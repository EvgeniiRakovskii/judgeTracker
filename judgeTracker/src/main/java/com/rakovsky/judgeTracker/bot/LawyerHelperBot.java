package com.rakovsky.judgeTracker.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.rakovsky.judgeTracker.constants.Constants;
import com.rakovsky.judgeTracker.service.CourtService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Set;

import static com.rakovsky.judgeTracker.constants.Constants.CHAT_ID;
import static com.rakovsky.judgeTracker.constants.Constants.WHITE_LIST_USERS;


@Component
public class LawyerHelperBot {

    @Autowired
    private CourtService courtService;

    private static final Logger logger = LoggerFactory.getLogger(LawyerHelperBot.class);

    // Creating bot passing the token received from @BotFather
    private final TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));

    public void serve() {

        // Registering for updates
        bot.setUpdatesListener(updates -> {
            updates.stream().filter(update -> WHITE_LIST_USERS.contains(update.message().from().username())).forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });


    }


    //TODO excel parser validation
    //
    private void process(Update update) {
        Message message = update.message() == null ? update.channelPost() : update.message();
        SendMessage sendMessage = null;

        if (message != null && message.text() != null) {
            String messageToBot = message.text().replace(System.getenv("BOT_NAME"), "");
            long chatId = message.chat().id();
            if (Constants.BOT_COMMANDS.containsKey(messageToBot.toLowerCase())) {
                sendMessage = new SendMessage(chatId, Constants.BOT_COMMANDS.get(messageToBot.toLowerCase()));
            }
        }
        if(message != null && message.document()!=null && message.document().fileName().contains("xlsx")) {
            GetFile request = new GetFile(message.document().fileId());
            GetFileResponse getFileResponse = bot.execute(request);

            File file = getFileResponse.file(); // com.pengrad.telegrambot.model.File
            String fullPath = bot.getFullFilePath(file);

            try {

                java.io.File realFile = new java.io.File("C:\\Users\\RayS\\IdeaProjects\\judgeTracker\\judgeTracker\\cases.xlsx");
                FileUtils.copyURLToFile(new URL(fullPath), realFile);
                courtService.updateCasesByExcel();
                sendMessage = new SendMessage(message.chat().id(), "File received");
            }
            catch (Exception e) {
                sendMessage = new SendMessage(message.chat().id(), e.getMessage());
            }


        }
        if (sendMessage != null) {
            sendMessage.parseMode(ParseMode.MarkdownV2);
            bot.execute(sendMessage);
        }
    }

    public void sendResultMessage(Set<String> differences, Set<String> unsuccessful) {
        StringBuilder message = new StringBuilder("The research is finished");
        message.append("\n\n");

        if (!differences.isEmpty()) {
            message.append("Found difference in : \n");
            differences.forEach(message::append);
        } else {
            message.append("No differences found \n");
        }
        message.append("\n");

        if (!unsuccessful.isEmpty()) {
            message.append("Trouble with : \n");
            unsuccessful.forEach(message::append);
        } else {
            message.append("All cases have been reviewed");
        }


        SendMessage sendMessage = new SendMessage(CHAT_ID, message.toString());

        sendMessage.parseMode(ParseMode.MarkdownV2);

        bot.execute(sendMessage);
    }

    public void sendUpdateMessage(Set<String> unsuccessful) {
        // разница:, неудачно: + текст, мб через стринг билдер?
        StringBuilder message = new StringBuilder("The updating is finished");
        message.append("\n\n");

        if (!unsuccessful.isEmpty()) {
            message.append("Trouble with : \n");
            unsuccessful.forEach(message::append);
        } else {
            message.append("All new cases have been updated");
        }


        SendMessage sendMessage = new SendMessage(CHAT_ID, message.toString());

        sendMessage.parseMode(ParseMode.MarkdownV2);

        bot.execute(sendMessage);
    }

}
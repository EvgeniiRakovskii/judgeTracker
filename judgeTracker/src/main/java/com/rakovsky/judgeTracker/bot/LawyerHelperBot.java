package com.rakovsky.judgeTracker.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.rakovsky.judgeTracker.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.rakovsky.judgeTracker.constants.Constants.CHAT_ID;


@Component
public class LawyerHelperBot {

    private static final Logger logger = LoggerFactory.getLogger(LawyerHelperBot.class);

    // Creating bot passing the token received from @BotFather
    private final TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));

    public void serve() {

        // Registering for updates
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });


    }

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
        if (sendMessage != null) {
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
package com.rakovsky.judgeTracker.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.rakovsky.judgeTracker.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class LawyerHelperBot  {

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
            String messageToBot = message.text().replace(System.getenv("BOT_NAME"),"");
            long chatId = message.chat().id();
            sendMessage = new SendMessage(chatId, Constants.BOT_COMMANDS.getOrDefault(messageToBot.toLowerCase(), "test"));
        }
        if (sendMessage != null) {
            bot.execute(sendMessage);
        }
    }

    public void sendMessage(String message, long chatId) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        bot.execute(sendMessage);
    }


}
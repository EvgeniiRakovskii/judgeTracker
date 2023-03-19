package com.rakovsky.judgeTracker;

import com.rakovsky.judgeTracker.bot.LawyerHelperBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableJpaRepositories("com.rakovsky.judgeTracker.repository")
@EntityScan("com.rakovsky.judgeTracker.model")
@SpringBootApplication
@EnableScheduling
public class JudgeTrackerApplication implements CommandLineRunner {

    @Autowired
    private LawyerHelperBot lawyerHelperBot;

    //TODO TESTS
    //TODO excel parser validation
    //TODO get DB cases in excel by bot
    //TODO user table with name from telegram + lang + chatId
    //TODO add user to table with cases
    //TODO localisation
    public static void main(String[] args) {
        SpringApplication.run(JudgeTrackerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        lawyerHelperBot.serve();
    }


}

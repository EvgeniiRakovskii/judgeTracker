package com.rakovsky.judgeTracker.scheduler;

import com.rakovsky.judgeTracker.bot.LawyerHelperBot;
import com.rakovsky.judgeTracker.service.CheckingChangesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.*;

//@EnableAsync
@Component
public class ScheduledTask {

    @Autowired
    private LawyerHelperBot lawyerHelperBot;
    @Autowired
    private CheckingChangesService checkingChangesService;

   //@Scheduled(cron = "0 00 22 * * WED", zone = "Europe/Paris")
    // @Scheduled(cron = "0 15 00 * * Thu", zone = "Europe/Paris")
    public void checkDifference() {
        Set<String> differences = checkingChangesService.checkChanges();
        lawyerHelperBot.sendResultMessage(differences);
    }

}

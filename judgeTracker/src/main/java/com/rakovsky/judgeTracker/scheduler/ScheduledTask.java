package com.rakovsky.judgeTracker.scheduler;

import com.rakovsky.judgeTracker.bot.LawyerHelperBot;
import com.rakovsky.judgeTracker.model.CourtCase;
import com.rakovsky.judgeTracker.service.CourtService;
import com.rakovsky.judgeTracker.service.WebPageService;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@EnableAsync
@Component
public class ScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
    @Autowired
    private CourtService courtService;
    @Autowired
    private WebPageService webPageService;
    @Autowired
    private LawyerHelperBot lawyerHelperBot;


    @Scheduled(cron = "*/10 * * * * *", zone = "Europe/Paris")
    //@Scheduled(cron = "0 0 17 * * *", zone = "Europe/Paris")
    public void checkDifference() {
        logger.info("Start checking difference");
        // TODO CREATE 2 THREAD. One is only for primorskii + random delay + random User-Agent
        List<CourtCase> cases = courtService.getAllCases().stream().sorted().toList();

        Set<String> differences = new TreeSet<>();
        Set<String> unsuccessful = new TreeSet<>();

        for (CourtCase courtCase : cases) {
            try {
                logger.info(courtCase.toString());
                Document casePage = webPageService.getCasePageWithDelay(courtCase);
                int numberOfColumns = webPageService.getNumberOfColumn(casePage);
                String tableInfo = webPageService.getTableInfo(casePage);

                if(tableInfo.equals(courtCase.getMotionOfCase())) {
                    continue;
                }

                if(StringUtils.hasText(courtCase.getMotionOfCase())) {
                    differences.add(String.format(" [%s](%s) \n", courtCase.getCustomName(), courtCase.getUrlForCase()));
                }

                courtCase.setNumberOfColumn(numberOfColumns);
                courtCase.setMotionOfCase(tableInfo);
                courtService.saveCourtCase(courtCase);

            } catch (Exception e) {
                logger.error(e.getMessage() + " " + courtCase);
                unsuccessful.add(String.format(" [%s](%s) \n", courtCase.getCustomName(), courtCase.getUrlForCase()));
            }
        }

        differences.forEach(System.out::println);
        unsuccessful.forEach(System.out::println);
        lawyerHelperBot.sendResultMessage(differences, unsuccessful);
    }

}

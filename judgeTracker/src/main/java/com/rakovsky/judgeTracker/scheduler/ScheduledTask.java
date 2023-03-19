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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.stream.Collectors.toCollection;

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

    @Async
    //@Scheduled(cron = "*/10 * * * * *", zone = "Europe/Paris")
    @Scheduled(cron = "0 0 17 * * *", zone = "Europe/Paris")
    public void checkDifference() {
        logger.info("Start checking difference");
        //приморский суд отдельно + всё остальное, дергаем запросы с разными хедерами параллельно + потоко независемый
        List<CourtCase> cases = courtService.getAllCases().stream().sorted().toList();

        Set<String> differences = new TreeSet<>();
        Set<String> unsuccessful = new TreeSet<>();

        for (CourtCase courtCase : cases) {
            try {
                logger.info(courtCase.toString());
                Document casePage = webPageService.getCasePageWithDelay(courtCase);
                int numberOfColumns = casePage.getElementsByClass("tabs").get(0).childNodeSize();
                Elements elements = casePage.getElementsByAttributeValueStarting("id", "cont");
                String tableInfo;

                if (webPageService.havePdfAct(casePage)) {
                    tableInfo = elements.stream().filter(element -> element.getElementsByAttributeValueStarting("id", "cont_doc").isEmpty()).
                            collect(toCollection(Elements::new)).text();

                } else {
                    tableInfo = elements.text();
                }
                String tableInfoWithoutTag = tableInfo.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").replaceAll("\\d", "");

                // разница в таблицах
                if (courtCase.getNumberOfColumn() != null && StringUtils.hasText(courtCase.getMotionOfCase())) {

                    if (!courtCase.getMotionOfCase().equals(tableInfoWithoutTag)) {
                        differences.add(String.format(" [%s](%s) \n", courtCase.getCustomName(), courtCase.getUrlForCase()));
                    }

                    courtCase.setNumberOfColumn(numberOfColumns);
                    courtCase.setMotionOfCase(tableInfoWithoutTag);
                    courtService.saveCourtCase(courtCase);
                }
            } catch (Exception e) {
                logger.error(e.getMessage() + " " + courtCase);
                unsuccessful.add(String.format(" [%s](%s) %s \n", courtCase.getCustomName(), courtCase.getUrlForCase(), e.getMessage()));
            }
        }

        //TODO change to sortedList (sort by customName) + log
        differences.forEach(System.out::println);
        unsuccessful.forEach(System.out::println);
        lawyerHelperBot.sendResultMessage(differences, unsuccessful);
    }

}

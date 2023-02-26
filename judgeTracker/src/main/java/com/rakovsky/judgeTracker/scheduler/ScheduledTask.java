package com.rakovsky.judgeTracker.scheduler;

import com.rakovsky.judgeTracker.bot.LawyerHelperBot;
import com.rakovsky.judgeTracker.model.CourtCase;
import com.rakovsky.judgeTracker.service.CourtService;
import com.rakovsky.judgeTracker.service.WebPageService;
import com.rakovsky.judgeTracker.service.parser.ExcelParser;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

@EnableAsync
@Component
public class ScheduledTask {

    @Autowired
    private CourtService courtService;
    @Autowired
    private ExcelParser excelParser;
    @Autowired
    private WebPageService webPageService;
    @Autowired
    private LawyerHelperBot lawyerHelperBot;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTask.class);


    //@Async
    @Scheduled(cron = "*/10 * * * * *", zone = "Europe/Paris")
    //@Scheduled(cron = "0 0 12 * * *", zone = "Europe/Paris")
    public void checkDifference() throws IOException {
        logger.info("Start checking difference");
        List<CourtCase> cases = courtService.getAllCases().stream().sorted().filter(courtCase -> courtCase.getCustomName().equals("Быкановы – Сэтл Инвест") || courtCase.getCustomName().equals("Смирнова – Сэтл Сити")).toList();
        Set<String> differences = new HashSet<>();
        Set<String> unsuccessful = new HashSet<>();

        for (CourtCase courtCase:cases) {
            try {
                logger.info(courtCase.toString());
                Document casePage = webPageService.getCasePageWithDelay(courtCase);
                int numberOfColumns = casePage.getElementsByClass("tabs").get(0).childNodeSize();
                Elements elements = casePage.getElementsByAttributeValueStarting("id","cont");
                String tableInfo;

                if(webPageService.havePdfAct(casePage)) {
                    tableInfo = elements.stream().filter(element -> element.getElementsByAttributeValueStarting("id","cont_doc").isEmpty()).
                            collect(toCollection(Elements::new)).text();

                } else {
                    tableInfo = elements.text();
                }
                String tableInfoWithoutTag = tableInfo.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").replaceAll("\\d","");

                // разница в таблицах
                if (courtCase.getNumberOfColumn() != null && StringUtils.hasText(courtCase.getMotionOfCase())) {

                    if (courtCase.getNumberOfColumn().equals(numberOfColumns)
                            && courtCase.getMotionOfCase().equals(tableInfoWithoutTag)) {
                        continue;
                    }

                    differences.add(String.format(" [%s](%s) \n", courtCase.getCustomName(), courtCase.getUrlForCase()));
                }

                courtCase.setNumberOfColumn(numberOfColumns);
                courtCase.setMotionOfCase(tableInfoWithoutTag);
                courtService.saveCourtCase(courtCase);

            } catch (Exception e) {
                logger.error(e.getMessage() + " " + courtCase);
                //может в сам объект и там метод получить ссылку для неудачной?
                unsuccessful.add(String.format(" [%s](%s)", courtCase.getCustomName(), courtCase.getUrlForCase()));
            }
        }
        differences.forEach(System.out::println);
        unsuccessful.forEach(System.out::println);
        lawyerHelperBot.sendResultMessage(differences, unsuccessful);
    }

}

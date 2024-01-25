package com.rakovsky.judgeTracker.service;

import com.rakovsky.judgeTracker.bot.LawyerHelperBot;
import com.rakovsky.judgeTracker.model.CourtCase;
import com.rakovsky.judgeTracker.scheduler.ScheduledTask;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class CheckingChangesService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
    @Autowired
    private CourtService courtService;
    @Autowired
    private WebPageService webPageService;

    public Set<String> checkChanges() {
        logger.info("Start checking changes");
        List<CourtCase> cases = courtService.getAllCases();
        //cases = cases.stream().filter(courtCase -> !courtCase.getCustomName().contains("Грудин")).toList();
        List<CourtCase> primorskiiJudgeCases = cases.stream().filter(courtCase -> courtCase.getUrlForCase().startsWith("https://primorsky--spb")).toList();
        List<CourtCase> anotherJudgeCases = cases.stream().filter(courtCase -> !courtCase.getUrlForCase().startsWith("https://primorsky--spb")).toList();

        CompletableFuture<Set<String>> primorskiiResult = CompletableFuture.supplyAsync(() -> checkDifferenceAsync(primorskiiJudgeCases));
        CompletableFuture<Set<String>> anotherResult = CompletableFuture.supplyAsync(() -> checkDifferenceAsync(anotherJudgeCases));

        CompletableFuture<Set<String>> commonResult = primorskiiResult
                .thenCombine(anotherResult, (primorskii, another) -> {
                    primorskii.addAll(another);
                    return primorskii;
                });

        Set<String> differences;
        try {
            differences = commonResult.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        logger.info("Разница:");
        differences.forEach(System.out::println);
        return differences;
    }


    private Set<String> checkDifferenceAsync(List<CourtCase> cases) {
        Set<String> differences = new TreeSet<>();
        List<CourtCase> updatedCases = new ArrayList<>();
        List<CourtCase> errorCases = new ArrayList<>();

        //разделить на 2 метода, первая проверка + в другом только ошибки ,тогда уйдут сложности с условиями
        for (int i = 0; i < 2; i++) {

            if(i!=0 && errorCases.isEmpty()){
                break;
            }

            if(!errorCases.isEmpty()){
                cases = errorCases;
                try {
                    //TimeUnit.MINUTES.sleep(1);
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (CourtCase courtCase : new ArrayList<>(cases)) {
                try {
                    logger.info(courtCase.toString());
                    Document casePage = webPageService.getCasePageWithDelay(courtCase);
                    int numberOfColumns = webPageService.getNumberOfColumn(casePage);
                    String tableInfo = webPageService.getTableInfo(casePage);

                    if (tableInfo.equals(courtCase.getMotionOfCase())) {
                        if(i!=0){
                            errorCases.remove(courtCase);
                        }
                        continue;
                    }

                    // если есть текст, то есть разница
                    if (StringUtils.hasText(courtCase.getMotionOfCase())) {
                        differences.add(String.format(" [%s](%s) \n", courtCase.getCustomName(), courtCase.getUrlForCase()));
                        logger.info("Разница в деле: " + courtCase);

                    }

                    // в любом случае сохраняем, мб null
                    courtCase.setNumberOfColumn(numberOfColumns);
                    courtCase.setMotionOfCase(tableInfo);

                    updatedCases.add(courtCase);
                    if(i!=0){
                        errorCases.remove(courtCase);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage() + " " + courtCase);
                    if(i==0) {
                        errorCases.add(courtCase);
                    }
                    //differences.add(String.format("error on [%s](%s) \n", courtCase.getCustomName(), courtCase.getUrlForCase()));
                    //добавляем в новый список
                }
            }
        }

        errorCases.forEach(errorCase-> differences.add(String.format("error on [%s](%s) \n", errorCase.getCustomName(), errorCase.getUrlForCase())));
        courtService.saveCases(updatedCases);
        return differences;
    }
}

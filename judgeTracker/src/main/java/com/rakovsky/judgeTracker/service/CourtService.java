package com.rakovsky.judgeTracker.service;

import com.rakovsky.judgeTracker.model.CourtCase;
import com.rakovsky.judgeTracker.repository.CourtRepository;
import com.rakovsky.judgeTracker.service.parser.ExcelParser;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

@Service
public class CourtService {

    private static final Logger logger = LoggerFactory.getLogger(CourtService.class);

    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private ExcelParser excelParser;
    @Autowired
    private WebPageService webPageService;

    public List<CourtCase> getAllCases() {
        return courtRepository.findAll();
    }

    public void saveCourtCase(CourtCase courtCase) {
        courtRepository.save(courtCase);
    }

    public void saveCases(List<CourtCase> courtCases) {
        if (!courtCases.isEmpty()) {
            courtRepository.saveAll(courtCases);
        }
    }

    public Set<String> updateCasesByExcel() throws IOException {

        List<CourtCase> newCases = excelParser.getCourtCasesFromExcel("C:\\Users\\RayS\\IdeaProjects\\judgeTracker\\judgeTracker\\cases.xlsx");
        saveCases(newCases);
        logger.info("Start to get info for new cases");
        Set<String> unsuccessful = new HashSet<>();

        for (CourtCase courtCase : newCases) {
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

                courtCase.setNumberOfColumn(numberOfColumns);
                courtCase.setMotionOfCase(tableInfoWithoutTag);
                saveCourtCase(courtCase);

            } catch (Exception e) {
                logger.error(e.getMessage() + " " + courtCase);
                //может в сам объект и там метод получить ссылку для неудачной?
                unsuccessful.add(String.format(" [%s](%s)", courtCase.getCustomName(), courtCase.getUrlForCase()));
            }
        }
        return unsuccessful;

    }
}

package com.rakovsky.judgeTracker.service;

import com.rakovsky.judgeTracker.model.CourtCase;
import com.rakovsky.judgeTracker.service.parser.ExcelParser;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.rakovsky.judgeTracker.constants.Constants.PATH_TO_LOCAL_EXCEL;

@Service
public class TelegramBotService {

    private static final Logger logger = LoggerFactory.getLogger(CourtService.class);
    @Autowired
    private ExcelParser excelParser;
    @Autowired
    private WebPageService webPageService;
    @Autowired
    private CourtService courtService;

    public Set<String> updateCasesByExcel(String fullFilePath) throws IOException, InvalidFormatException {
        java.io.File localFile = new java.io.File(PATH_TO_LOCAL_EXCEL);
        FileUtils.copyURLToFile(new URL(fullFilePath), localFile);
        Set<String> unsuccessful = new HashSet<>();

        try(Workbook workbook = new XSSFWorkbook(localFile)) {

            changeCustomNames(workbook);
            deleteCases(workbook);
            List<CourtCase> newCases = excelParser.getNewCourtCases(workbook);

            if (newCases.isEmpty()) {
                return unsuccessful;
            }
            courtService.saveCases(newCases);

            logger.info("Start to get info for new cases");

            for (CourtCase courtCase : newCases) {
                try {
                    logger.info(courtCase.toString());
                    Document casePage = webPageService.getCasePageWithDelay(courtCase);
                    int numberOfColumns = webPageService.getNumberOfColumn(casePage);
                    String tableInfo = webPageService.getTableInfo(casePage);

                    courtCase.setNumberOfColumn(numberOfColumns);
                    courtCase.setMotionOfCase(tableInfo);
                    courtService.saveCourtCase(courtCase);

                } catch (Exception e) {
                    logger.error(e.getMessage() + " " + courtCase);
                    //может в сам объект и там метод получить ссылку для неудачной?
                    unsuccessful.add(String.format(" [%s](%s) \n", courtCase.getCustomName(), courtCase.getUrlForCase()));
                }
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }


        return unsuccessful;

    }

    public void changeCustomNames(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(1);

        for (Row row : sheet) {
            if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isEmpty()) {
                break;
            }

            CourtCase courtCase = courtService.findByCaseNumber(row.getCell(3).getStringCellValue());

            if (courtCase != null && courtCase.getCustomName().compareTo(row.getCell(1).getStringCellValue()) != 0) {
                courtCase.setCustomName(row.getCell(1).getStringCellValue());
                courtService.saveCourtCase(courtCase);
            }

        }
    }

    public void deleteCases(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(2);

        for (Row row : sheet) {
            if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isEmpty()) {
                break;
            }

            CourtCase courtCase = courtService.findByCaseNumber(row.getCell(2).getStringCellValue());

            if (courtCase != null) {
                courtService.deleteCourtCase(courtCase);
            }

        }
    }

    public String getResultMessage(Set<String> differences, Set<String> unsuccessful) {
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
        return message.toString();
    }

    public String getResultMessage(Set<String> differences) {
        StringBuilder message = new StringBuilder("The research is finished");
        message.append("\n\n");
        if (!differences.isEmpty()) {
            message.append("Found difference in : \n");
            differences.forEach(message::append);
        } else {
            message.append("No differences found \n");
        }

        return message.toString();
    }
}

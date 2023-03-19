package com.rakovsky.judgeTracker.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.rakovsky.judgeTracker.model.CourtCase;
import com.rakovsky.judgeTracker.service.parser.ExcelParser;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.rakovsky.judgeTracker.constants.Constants.PATH_TO_LOCAL_EXCEL;
import static java.util.stream.Collectors.toCollection;

@Service
public class TelegramBotService {

    private static final Logger logger = LoggerFactory.getLogger(CourtService.class);
    @Autowired
    private ExcelParser excelParser;
    @Autowired
    private WebPageService webPageService;
    @Autowired
    private CourtService courtService;

    public void getExcelAndSaveItLocal(TelegramBot bot, String fileId) throws IOException {
        GetFile request = new GetFile(fileId);
        GetFileResponse getFileResponse = bot.execute(request);

        File file = getFileResponse.file(); // com.pengrad.telegrambot.model.File
        java.io.File localFile = new java.io.File(PATH_TO_LOCAL_EXCEL);
        FileUtils.copyURLToFile(new URL(bot.getFullFilePath(file)), localFile);
    }


    public Set<String> updateCasesByExcel() throws IOException {
        List<CourtCase> newCases = excelParser.getNewCourtCases(PATH_TO_LOCAL_EXCEL);

        courtService.saveCases(newCases);
        changeCustomNames();

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
                courtService.saveCourtCase(courtCase);

            } catch (Exception e) {
                logger.error(e.getMessage() + " " + courtCase);
                //может в сам объект и там метод получить ссылку для неудачной?
                unsuccessful.add(String.format(" [%s](%s) \n", courtCase.getCustomName(), courtCase.getUrlForCase()));
            }
        }


        return unsuccessful;

    }

    public void changeCustomNames() throws IOException {
        FileInputStream file = new FileInputStream(PATH_TO_LOCAL_EXCEL);
        Workbook workbook = new XSSFWorkbook(file);

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
}

package com.rakovsky.judgeTracker.service.parser;

import com.rakovsky.judgeTracker.model.CourtCase;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelParser {

    public List<CourtCase> getCourtCasesFromExcel(String path) throws IOException {
        FileInputStream file = new FileInputStream(path);
        Workbook workbook = new XSSFWorkbook(file);
        List<CourtCase> courtCases = new ArrayList<>();

        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if(row.getCell(0)==null){
                break;
            }
            String customName = row.getCell(0).getStringCellValue();
            String url = row.getCell(1).getStringCellValue();
            String caseNumber = row.getCell(2).getStringCellValue();

            CourtCase courtCase = new CourtCase(
                    customName,
                    url,
                    caseNumber);
            courtCases.add(courtCase);

        }
        return courtCases;
    }
}

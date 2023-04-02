package com.rakovsky.judgeTracker.service.parser;

import com.rakovsky.judgeTracker.model.CourtCase;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelParser {

    public List<CourtCase> getNewCourtCases(Workbook workbook) {
        List<CourtCase> courtCases = new ArrayList<>();

        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isEmpty()) {
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

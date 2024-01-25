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
            String caseNumber = row.getCell(2).getStringCellValue().trim();
            int numberOfColumns = 0;
            String motionOfCase = null;
            if(row.getCell(3)!=null) numberOfColumns = (int) row.getCell(3).getNumericCellValue();
            if(row.getCell(4)!=null) motionOfCase = row.getCell(4).getStringCellValue();

            CourtCase courtCase;
            if(numberOfColumns!=0 && motionOfCase!=null && !motionOfCase.isEmpty()) {
                courtCase = new CourtCase(
                        customName,
                        url,
                        caseNumber,
                        numberOfColumns,
                        motionOfCase);
            } else {
                courtCase = new CourtCase(
                        customName,
                        url,
                        caseNumber);
            }
            courtCases.add(courtCase);

        }
        return courtCases;
    }
}

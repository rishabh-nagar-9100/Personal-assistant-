package com.jarvis.dsa.service;

import com.jarvis.auth.model.User;
import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.dsa.model.DsaQuestion;
import com.jarvis.dsa.model.DsaStatus;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class DsaExcelParserService {

    public List<DsaQuestion> parseExcelSheet(User user, InputStream inputStream) throws Exception {
        List<DsaQuestion> questions = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            boolean isFirstRow = true;
            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false; // Skip header row
                    continue;
                }

                String title = getCellValue(row.getCell(0));
                if (title == null || title.isBlank()) {
                    continue; // Skip empty rows
                }

                String topic = getCellValue(row.getCell(1));
                if (topic == null || topic.isBlank()) {
                    topic = "General";
                }

                String difficultyStr = getCellValue(row.getCell(2));
                DsaDifficulty difficulty = parseDifficulty(difficultyStr);

                String sourceLink = getCellValue(row.getCell(3));

                String statusStr = getCellValue(row.getCell(4));
                DsaStatus status = parseStatus(statusStr);

                DsaQuestion question = new DsaQuestion(user, title.trim(), topic.trim(), difficulty, sourceLink, status);
                questions.add(question);
            }
        }

        return questions;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        return null;
    }

    private DsaDifficulty parseDifficulty(String str) {
        if (str == null) return DsaDifficulty.MEDIUM;
        try {
            return DsaDifficulty.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return DsaDifficulty.MEDIUM;
        }
    }

    private DsaStatus parseStatus(String str) {
        if (str == null) return DsaStatus.NOT_STARTED;
        try {
            return DsaStatus.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return DsaStatus.NOT_STARTED;
        }
    }
}

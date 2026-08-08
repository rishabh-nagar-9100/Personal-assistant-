package com.jarvis.dsa.service;

import com.jarvis.auth.model.User;
import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.dsa.model.DsaQuestion;
import com.jarvis.dsa.model.DsaStatus;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DsaExcelParserServiceTest {

    private DsaExcelParserService parserService;
    private User testUser;

    @BeforeEach
    void setUp() {
        parserService = new DsaExcelParserService();
        testUser = new User(UUID.randomUUID(), "dsauser@example.com");
    }

    @Test
    @DisplayName("Should parse valid Excel sheet into DsaQuestion entities")
    void testParseExcelSheet() throws Exception {
        byte[] excelBytes = createSampleExcelBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(excelBytes);

        List<DsaQuestion> questions = parserService.parseExcelSheet(testUser, inputStream);

        assertEquals(2, questions.size());

        DsaQuestion q1 = questions.get(0);
        assertEquals("Two Sum", q1.getTitle());
        assertEquals("Arrays", q1.getTopic());
        assertEquals(DsaDifficulty.EASY, q1.getDifficulty());
        assertEquals("https://leetcode.com/problems/two-sum", q1.getSourceLink());
        assertEquals(DsaStatus.NOT_STARTED, q1.getStatus());

        DsaQuestion q2 = questions.get(1);
        assertEquals("Lowest Common Ancestor", q2.getTitle());
        assertEquals("Trees", q2.getTopic());
        assertEquals(DsaDifficulty.MEDIUM, q2.getDifficulty());
        assertEquals("https://leetcode.com/problems/lowest-common-ancestor", q2.getSourceLink());
        assertEquals(DsaStatus.SOLVED, q2.getStatus());
    }

    private byte[] createSampleExcelBytes() throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("DSA Sheet");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Title");
            header.createCell(1).setCellValue("Topic");
            header.createCell(2).setCellValue("Difficulty");
            header.createCell(3).setCellValue("Source Link");
            header.createCell(4).setCellValue("Status");

            // Row 1
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Two Sum");
            r1.createCell(1).setCellValue("Arrays");
            r1.createCell(2).setCellValue("EASY");
            r1.createCell(3).setCellValue("https://leetcode.com/problems/two-sum");
            r1.createCell(4).setCellValue("NOT_STARTED");

            // Row 2
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Lowest Common Ancestor");
            r2.createCell(1).setCellValue("Trees");
            r2.createCell(2).setCellValue("MEDIUM");
            r2.createCell(3).setCellValue("https://leetcode.com/problems/lowest-common-ancestor");
            r2.createCell(4).setCellValue("SOLVED");

            workbook.write(out);
            return out.toByteArray();
        }
    }
}

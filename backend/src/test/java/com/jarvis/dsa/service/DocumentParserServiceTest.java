package com.jarvis.dsa.service;

import com.jarvis.auth.model.User;
import com.jarvis.auth.repository.UserRepository;
import com.jarvis.dsa.dto.ExcelImportResponse;
import com.jarvis.dsa.repository.DsaQuestionRepository;
import com.jarvis.topic.model.Subject;
import com.jarvis.topic.repository.SubjectRepository;
import com.jarvis.topic.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentParserServiceTest {

    @Mock private DsaQuestionRepository dsaQuestionRepository;
    @Mock private com.jarvis.practice.repository.PracticeQuestionRepository practiceQuestionRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private UserRepository userRepository;

    private DocumentParserService documentParserService;
    private User testUser;

    @BeforeEach
    void setUp() {
        documentParserService = new DocumentParserService(
                dsaQuestionRepository, practiceQuestionRepository, subjectRepository, topicRepository, userRepository
        );
        testUser = new User(UUID.randomUUID(), "rishabh@example.com");

        lenient().when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        lenient().when(subjectRepository.save(any(Subject.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("Should parse PDF syllabus text and extract topics and subjects")
    void testParsePdfDocument() throws Exception {
        String pdfText = "Subject: Operating Systems\n" +
                "• Process Synchronization\n" +
                "• Virtual Memory Paging\n" +
                "• CPU Scheduling Algorithms";

        // Create mock PDF text file
        MockMultipartFile file = new MockMultipartFile(
                "file", "Operating_Systems_Syllabus.pdf", "application/pdf", createMockPdfBytes(pdfText)
        );

        when(subjectRepository.findByUserIdOrderByNameAsc(any())).thenReturn(List.of());
        when(topicRepository.findBySubjectIdOrderByNameAsc(any())).thenReturn(List.of());

        ExcelImportResponse response = documentParserService.parseAndImportDocument(testUser, file);

        assertNotNull(response);
        assertTrue(response.importedCount() > 0, "Should import parsed topics from PDF");
        assertTrue(response.message().contains("PDF"), "Message should mention PDF parsing");
    }

    @Test
    @DisplayName("Should parse 7-column Excel sheet with Subject, Topic, Title, Problem #, Difficulty, Status, and Link")
    void testParse7ColumnExcelWorkbook() throws Exception {
        byte[] excelBytes;
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("StudyPlan");
            // Row 0: Header
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Subject");
            header.createCell(1).setCellValue("Topic");
            header.createCell(2).setCellValue("Problem Short Description / Title");
            header.createCell(3).setCellValue("Problem Number");
            header.createCell(4).setCellValue("Difficulty Level");
            header.createCell(5).setCellValue("Progress Level / Status");
            header.createCell(6).setCellValue("Question Link");

            // Row 1: Problem 1
            org.apache.poi.ss.usermodel.Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Operating Systems");
            row1.createCell(1).setCellValue("Deadlocks");
            row1.createCell(2).setCellValue("Banker's Algorithm");
            row1.createCell(3).setCellValue("#101");
            row1.createCell(4).setCellValue("Hard");
            row1.createCell(5).setCellValue("Solved");
            row1.createCell(6).setCellValue("https://example.com/deadlocks");

            // Row 2: Problem 2
            org.apache.poi.ss.usermodel.Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("DBMS");
            row2.createCell(1).setCellValue("Indexing");
            row2.createCell(2).setCellValue("Second Highest Salary");
            row2.createCell(3).setCellValue("#175");
            row2.createCell(4).setCellValue("Medium");
            row2.createCell(5).setCellValue("Needs Revision");
            row2.createCell(6).setCellValue("https://leetcode.com/problems/second-highest-salary");

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            workbook.write(out);
            excelBytes = out.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "Study_Questions.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes
        );

        when(subjectRepository.findByUserIdOrderByNameAsc(any())).thenReturn(List.of());
        when(topicRepository.findBySubjectIdOrderByNameAsc(any())).thenReturn(List.of());
        when(practiceQuestionRepository.findByUserIdOrderByTitleAsc(any())).thenReturn(List.of());
        when(dsaQuestionRepository.findByUserIdOrderByTopicAscTitleAsc(any())).thenReturn(List.of());

        ExcelImportResponse response = documentParserService.parseAndImportDocument(testUser, file);

        assertNotNull(response);
        assertEquals(2, response.importedCount(), "Should import 2 items from 7-column Excel");
        assertEquals(2, response.totalRows(), "Should detect 2 data rows");
    }

    private byte[] createMockPdfBytes(String textContent) throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);
            try (org.apache.pdfbox.pdmodel.PDPageContentStream contents = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
                contents.beginText();
                contents.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 12);
                contents.newLineAtOffset(100, 700);
                for (String line : textContent.split("\n")) {
                    contents.showText(line.replaceAll("[^\\x00-\\x7F]", ""));
                    contents.newLineAtOffset(0, -15);
                }
                contents.endText();
            }
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
}

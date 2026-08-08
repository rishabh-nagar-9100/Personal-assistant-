package com.jarvis.dsa.service;

import com.jarvis.auth.model.User;
import com.jarvis.auth.repository.UserRepository;
import com.jarvis.dsa.dto.ExcelImportResponse;
import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.dsa.model.DsaQuestion;
import com.jarvis.dsa.model.DsaStatus;
import com.jarvis.dsa.repository.DsaQuestionRepository;
import com.jarvis.practice.model.PracticeCategoryType;
import com.jarvis.practice.model.PracticeQuestion;
import com.jarvis.practice.repository.PracticeQuestionRepository;
import com.jarvis.topic.model.Subject;
import com.jarvis.topic.model.Topic;
import com.jarvis.topic.repository.SubjectRepository;
import com.jarvis.topic.repository.TopicRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentParserService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParserService.class);

    private final DsaQuestionRepository dsaQuestionRepository;
    private final PracticeQuestionRepository practiceQuestionRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public DocumentParserService(DsaQuestionRepository dsaQuestionRepository,
                                 PracticeQuestionRepository practiceQuestionRepository,
                                 SubjectRepository subjectRepository,
                                 TopicRepository topicRepository,
                                 UserRepository userRepository) {
        this.dsaQuestionRepository = dsaQuestionRepository;
        this.practiceQuestionRepository = practiceQuestionRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExcelImportResponse parseAndImportDocument(User user, MultipartFile file) throws Exception {
        return parseAndImportDocument(user, file, null, null);
    }

    @Transactional
    public ExcelImportResponse parseAndImportDocument(User user, MultipartFile file, String targetSubjectName, String targetCategory) throws Exception {
        User managedUser = userRepository.findById(user.getId()).orElse(user);
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        if (fileName.endsWith(".pdf") || "application/pdf".equals(file.getContentType())) {
            return parsePdfDocument(managedUser, file, targetSubjectName, targetCategory);
        } else {
            return parseExcelWorkbook(managedUser, file, targetSubjectName, targetCategory);
        }
    }

    private final org.apache.poi.ss.usermodel.DataFormatter dataFormatter = new org.apache.poi.ss.usermodel.DataFormatter();

    private ExcelImportResponse parsePdfDocument(User user, MultipartFile file, String targetSubjectName, String targetCategory) throws Exception {
        int importedCount = 0;
        int totalItems = 0;

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            String defaultSub = (targetSubjectName != null && !targetSubjectName.isBlank()) 
                    ? targetSubjectName.trim() 
                    : extractFileNameWithoutExtension(file.getOriginalFilename());
            Subject currentSubject = findOrCreateSubject(user, defaultSub);

            String[] tokens = text.split("(?=[\\n\\r]|\\s*[\\*•\\-]\\s*|;)");

            for (String rawToken : tokens) {
                String token = rawToken.replaceAll("[\\*•\\-\\r\\n]", " ").replaceAll("\\s+", " ").trim();
                if (token.length() < 3) continue;

                totalItems++;

                if (token.toLowerCase().startsWith("subject:") || token.toLowerCase().startsWith("unit ")) {
                    String subName = token.replaceAll("(?i)^(subject:|unit \\d+:?)\\s*", "").trim();
                    if (!subName.isBlank() && (targetSubjectName == null || targetSubjectName.isBlank())) {
                        currentSubject = findOrCreateSubject(user, subName);
                    }
                    continue;
                }

                PracticeCategoryType cat = detectCategory(file.getOriginalFilename(), currentSubject.getName(), token, targetCategory);
                createQuestionAndTopic(user, currentSubject, token, token, null, DsaDifficulty.MEDIUM, null, DsaStatus.NOT_STARTED, cat);
                importedCount++;
            }
        }

        return new ExcelImportResponse(totalItems, importedCount, 0,
                "Successfully parsed PDF document. Imported " + importedCount + " topics & practice questions under subject '" + targetSubjectName + "'.");
    }

    private ExcelImportResponse parseExcelWorkbook(User user, MultipartFile file, String targetSubjectName, String targetCategory) throws Exception {
        int totalRows = 0;
        int importedCount = 0;
        int skippedCount = 0;

        String fileSubjectName = (targetSubjectName != null && !targetSubjectName.isBlank()) 
                ? targetSubjectName.trim() 
                : extractFileNameWithoutExtension(file.getOriginalFilename());
        Subject defaultSubject = findOrCreateSubject(user, fileSubjectName);

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Dynamic header auto-detection
            int subjectCol = -1, topicCol = -1, descCol = -1, numCol = -1, diffCol = -1, statusCol = -1, linkCol = -1;

            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    String val = getCellValue(cell);
                    if (val == null) continue;
                    String lower = val.toLowerCase().trim();
                    int idx = cell.getColumnIndex();

                    if (lower.contains("link") || lower.contains("url") || lower.contains("source") || lower.contains("href")) {
                        linkCol = idx;
                    } else if (lower.contains("progress") || lower.contains("status") || lower.contains("state")) {
                        statusCol = idx;
                    } else if (lower.contains("diff") || lower.contains("level")) {
                        diffCol = idx;
                    } else if (lower.contains("number") || lower.contains("num") || lower.contains("code") || lower.contains("#") || lower.contains("problem #")) {
                        numCol = idx;
                    } else if (lower.contains("topic") || lower.contains("unit") || lower.contains("section") || lower.contains("chapter")) {
                        topicCol = idx;
                    } else if (lower.contains("subject") || lower.contains("course")) {
                        subjectCol = idx;
                    } else if (lower.contains("desc") || lower.contains("title") || lower.contains("problem") || lower.contains("name") || lower.contains("question")) {
                        descCol = idx;
                    }
                }
            }

            // Fallback to positional indices if headers not recognized
            if (descCol == -1 && subjectCol == -1 && topicCol == -1) {
                subjectCol = 0;
                topicCol = 1;
                descCol = 2;
                numCol = 3;
                diffCol = 4;
                statusCol = 5;
                linkCol = 6;
            }

            boolean isFirstRow = true;
            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false; // Skip header row
                    continue;
                }
                totalRows++;

                String subVal = subjectCol != -1 ? getCellValue(row.getCell(subjectCol)) : null;
                String topicVal = topicCol != -1 ? getCellValue(row.getCell(topicCol)) : null;
                String descVal = descCol != -1 ? getCellValue(row.getCell(descCol)) : null;
                String numVal = numCol != -1 ? getCellValue(row.getCell(numCol)) : null;
                String diffVal = diffCol != -1 ? getCellValue(row.getCell(diffCol)) : null;
                String statusVal = statusCol != -1 ? getCellValue(row.getCell(statusCol)) : null;
                String linkVal = linkCol != -1 ? getCellValue(row.getCell(linkCol)) : null;

                // Title fallback logic
                String mainText = descVal;
                if (mainText == null || mainText.isBlank()) mainText = subVal;
                if (mainText == null || mainText.isBlank()) mainText = topicVal;

                if (mainText == null || mainText.isBlank()) {
                    skippedCount++;
                    continue;
                }

                // Clean title and problem number
                String title = mainText.trim();
                String problemNum = numVal != null && !numVal.isBlank() ? numVal.trim() : null;
                if (problemNum != null && !problemNum.startsWith("#") && problemNum.matches("\\d+")) {
                    problemNum = "#" + problemNum;
                }

                String finalSubName = (targetSubjectName != null && !targetSubjectName.isBlank()) 
                        ? targetSubjectName.trim() 
                        : (subVal != null && !subVal.isBlank() ? subVal.trim() : fileSubjectName);
                String finalTopicName = topicVal != null && !topicVal.isBlank() ? topicVal.trim() : "General";

                Subject targetSubject = findOrCreateSubject(user, finalSubName);
                DsaDifficulty diff = parseDifficulty(diffVal);
                DsaStatus status = parseStatus(statusVal);
                PracticeCategoryType cat = detectCategory(file.getOriginalFilename(), finalSubName, finalTopicName, targetCategory);

                createQuestionAndTopic(user, targetSubject, finalTopicName, title, problemNum, diff, linkVal, status, cat);
                importedCount++;
            }
        }

        return new ExcelImportResponse(totalRows, importedCount, skippedCount,
                "Successfully parsed Excel document. Imported " + importedCount + " items for subject '" + fileSubjectName + "'.");
    }

    private PracticeCategoryType detectCategory(String fileName, String subjectName, String topicName, String explicitCategory) {
        if (explicitCategory != null && !explicitCategory.isBlank()) {
            try {
                return PracticeCategoryType.valueOf(explicitCategory.trim().toUpperCase());
            } catch (Exception e) {}
        }

        String combined = ((fileName != null ? fileName : "") + " " + (subjectName != null ? subjectName : "") + " " + (topicName != null ? topicName : "")).toLowerCase();
        
        if (combined.contains("sql") || combined.contains("query") || combined.contains("database") || combined.contains("schema") || combined.contains("mysql") || combined.contains("postgres")) {
            return PracticeCategoryType.SQL;
        }
        if (combined.contains("aptitude") || combined.contains("quant") || combined.contains("reasoning") || combined.contains("math") || combined.contains("logic")) {
            return PracticeCategoryType.APTITUDE;
        }
        return PracticeCategoryType.DSA;
    }

    private Subject findOrCreateSubject(User user, String name) {
        String trimmed = name.trim();
        List<Subject> subjects = subjectRepository.findByUserIdOrderByNameAsc(user.getId());
        Optional<Subject> existing = subjects.stream()
                .filter(s -> s.getName().equalsIgnoreCase(trimmed))
                .findFirst();

        if (existing.isPresent()) {
            return existing.get();
        }
        Subject subject = new Subject(user, trimmed);
        return subjectRepository.save(subject);
    }

    private void createTopicIfMissing(Subject subject, String topicName) {
        String trimmed = topicName.trim();
        List<Topic> existingTopics = topicRepository.findBySubjectIdOrderByNameAsc(subject.getId());
        boolean exists = existingTopics.stream().anyMatch(t -> t.getName().equalsIgnoreCase(trimmed));

        if (!exists) {
            Topic topic = new Topic(subject, trimmed);
            topicRepository.save(topic);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        String val = dataFormatter.formatCellValue(cell);
        return val != null && !val.isBlank() ? val.trim() : null;
    }

    private void createQuestionAndTopic(User user, Subject subject, String topicName, String title, String problemNum, DsaDifficulty diff, String link, DsaStatus status, PracticeCategoryType category) {
        createTopicIfMissing(subject, topicName);

        // Save into PracticeQuestionRepository
        List<PracticeQuestion> existingPractice = practiceQuestionRepository.findByUserIdOrderByTitleAsc(user.getId());
        Optional<PracticeQuestion> pOpt = existingPractice.stream()
                .filter(pq -> pq.getTitle().equalsIgnoreCase(title.trim()) || (problemNum != null && problemNum.equalsIgnoreCase(pq.getProblemNumber())))
                .findFirst();

        if (pOpt.isEmpty()) {
            PracticeQuestion pq = new PracticeQuestion(user, category, topicName, title.trim(), diff);
            pq.setSubject(subject);
            pq.setSubjectName(subject.getName());
            pq.setProblemNumber(problemNum);
            pq.setSourceLink(link);
            pq.setStatus(status);
            practiceQuestionRepository.save(pq);
        } else {
            PracticeQuestion pq = pOpt.get();
            pq.setSubject(subject);
            pq.setSubjectName(subject.getName());
            if (problemNum != null) pq.setProblemNumber(problemNum);
            if (link != null) pq.setSourceLink(link);
            if (status != null) pq.setStatus(status);
            practiceQuestionRepository.save(pq);
        }

        // Also save into DsaQuestionRepository if category is DSA
        List<DsaQuestion> existingDsa = dsaQuestionRepository.findByUserIdOrderByTopicAscTitleAsc(user.getId());
        Optional<DsaQuestion> dOpt = existingDsa.stream()
                .filter(dq -> dq.getTitle().equalsIgnoreCase(title.trim()) || (problemNum != null && problemNum.equalsIgnoreCase(dq.getProblemNumber())))
                .findFirst();

        if (dOpt.isEmpty()) {
            DsaQuestion dq = new DsaQuestion(user, title.trim(), topicName, diff, link, status);
            dq.setSubject(subject);
            dq.setProblemNumber(problemNum);
            dsaQuestionRepository.save(dq);
        } else {
            DsaQuestion dq = dOpt.get();
            dq.setSubject(subject);
            if (problemNum != null) dq.setProblemNumber(problemNum);
            if (link != null) dq.setSourceLink(link);
            if (status != null) dq.setStatus(status);
            dsaQuestionRepository.save(dq);
        }
    }

    private DsaDifficulty parseDifficulty(String str) {
        if (str == null) return DsaDifficulty.MEDIUM;
        String s = str.trim().toUpperCase();
        if (s.contains("EASY")) return DsaDifficulty.EASY;
        if (s.contains("HARD")) return DsaDifficulty.HARD;
        return DsaDifficulty.MEDIUM;
    }

    private DsaStatus parseStatus(String str) {
        if (str == null) return DsaStatus.NOT_STARTED;
        String s = str.trim().toUpperCase();
        if (s.contains("SOLVED") || s.contains("DONE") || s.contains("COMPLETED")) return DsaStatus.SOLVED;
        if (s.contains("PROGRESS") || s.contains("DOING") || s.contains("IN_PROGRESS")) return DsaStatus.IN_PROGRESS;
        if (s.contains("REVISION") || s.contains("NEED")) return DsaStatus.NEEDS_REVISION;
        return DsaStatus.NOT_STARTED;
    }

    private String extractFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) return "General Study";
        int idx = fileName.lastIndexOf('.');
        if (idx > 0) {
            String name = fileName.substring(0, idx).replaceAll("[_\\-]", " ");
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return fileName;
    }
}

package com.jarvis.practice.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.dsa.dto.ReviewDsaQuestionRequest;
import com.jarvis.practice.dto.CreatePracticeQuestionRequest;
import com.jarvis.practice.dto.PracticeQuestionResponse;
import com.jarvis.practice.dto.QuotaConfigRequest;
import com.jarvis.practice.dto.QuotaConfigResponse;
import com.jarvis.practice.dto.TodayQuotaResponse;
import com.jarvis.practice.model.PracticeCategoryType;
import com.jarvis.practice.service.PracticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/practice")
public class PracticeController {

    private final PracticeService practiceService;
    private final UserService userService;
    private final com.jarvis.dsa.service.DocumentParserService documentParserService;

    public PracticeController(PracticeService practiceService, UserService userService, com.jarvis.dsa.service.DocumentParserService documentParserService) {
        this.practiceService = practiceService;
        this.userService = userService;
        this.documentParserService = documentParserService;
    }

    @GetMapping("/quota-config")
    public ResponseEntity<QuotaConfigResponse> getQuotaConfig(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        QuotaConfigResponse response = practiceService.getQuotaConfig(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/quota-config")
    public ResponseEntity<QuotaConfigResponse> updateQuotaConfig(JwtAuthenticationToken authToken,
                                                                 @RequestBody QuotaConfigRequest request) {
        User user = userService.getOrCreateUser(authToken);
        QuotaConfigResponse response = practiceService.updateQuotaConfig(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today-quota")
    public ResponseEntity<TodayQuotaResponse> getTodayQuota(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        TodayQuotaResponse response = practiceService.getTodayQuotaStatus(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/questions")
    public ResponseEntity<PracticeQuestionResponse> createQuestion(JwtAuthenticationToken authToken,
                                                                   @RequestBody CreatePracticeQuestionRequest request) {
        User user = userService.getOrCreateUser(authToken);
        PracticeQuestionResponse response = practiceService.createPracticeQuestion(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/questions")
    public ResponseEntity<List<PracticeQuestionResponse>> getQuestions(JwtAuthenticationToken authToken,
                                                                        @RequestParam PracticeCategoryType categoryType) {
        User user = userService.getOrCreateUser(authToken);
        List<PracticeQuestionResponse> responses = practiceService.getPracticeQuestions(user, categoryType);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/questions/{id}/review")
    public ResponseEntity<PracticeQuestionResponse> reviewQuestion(JwtAuthenticationToken authToken,
                                                                   @PathVariable UUID id,
                                                                   @RequestBody ReviewDsaQuestionRequest request) {
        User user = userService.getOrCreateUser(authToken);
        PracticeQuestionResponse response = practiceService.reviewPracticeQuestion(user, id, request.quality());
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.PatchMapping("/questions/{id}/status")
    public ResponseEntity<PracticeQuestionResponse> updateStatus(JwtAuthenticationToken authToken,
                                                                 @PathVariable UUID id,
                                                                 @RequestParam com.jarvis.dsa.model.DsaStatus status) {
        User user = userService.getOrCreateUser(authToken);
        PracticeQuestionResponse response = practiceService.updateQuestionStatus(user, id, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<com.jarvis.dsa.dto.ExcelImportResponse> importDocument(JwtAuthenticationToken authToken,
                                                                                @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                                                                @RequestParam(value = "subjectName", required = false) String subjectName,
                                                                                @RequestParam(value = "categoryType", required = false) String categoryType) throws Exception {
        User user = userService.getOrCreateUser(authToken);
        com.jarvis.dsa.dto.ExcelImportResponse response = documentParserService.parseAndImportDocument(user, file, subjectName, categoryType);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(JwtAuthenticationToken authToken,
                                               @PathVariable UUID id) {
        User user = userService.getOrCreateUser(authToken);
        practiceService.deletePracticeQuestion(user, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAllQuestions(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        practiceService.clearAllQuestions(user);
        return ResponseEntity.noContent().build();
    }
}

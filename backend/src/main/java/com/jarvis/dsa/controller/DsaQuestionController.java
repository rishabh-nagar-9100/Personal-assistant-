package com.jarvis.dsa.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.dsa.dto.CreateDsaQuestionRequest;
import com.jarvis.dsa.dto.DsaQuestionResponse;
import com.jarvis.dsa.dto.ExcelImportResponse;
import com.jarvis.dsa.dto.ReviewDsaQuestionRequest;
import com.jarvis.dsa.service.DsaQuestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dsa")
public class DsaQuestionController {

    private final DsaQuestionService dsaQuestionService;
    private final UserService userService;

    public DsaQuestionController(DsaQuestionService dsaQuestionService, UserService userService) {
        this.dsaQuestionService = dsaQuestionService;
        this.userService = userService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExcelImportResponse> importExcel(JwtAuthenticationToken authToken,
                                                           @RequestParam("file") MultipartFile file,
                                                           @RequestParam(value = "subjectName", required = false) String subjectName,
                                                           @RequestParam(value = "categoryType", required = false) String categoryType) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ExcelImportResponse(0, 0, 0, "Uploaded file is empty. Please select a valid .xlsx, .xls, or .pdf document."));
            }
            User user = userService.getOrCreateUser(authToken);
            ExcelImportResponse response = dsaQuestionService.importExcelSheet(user, file, subjectName, categoryType);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ExcelImportResponse(0, 0, 0, "Could not parse document: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<DsaQuestionResponse> createQuestion(JwtAuthenticationToken authToken,
                                                               @RequestBody CreateDsaQuestionRequest request) {
        User user = userService.getOrCreateUser(authToken);
        DsaQuestionResponse response = dsaQuestionService.createQuestion(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DsaQuestionResponse>> getUserQuestions(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        List<DsaQuestionResponse> responses = dsaQuestionService.getUserQuestions(user);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<DsaQuestionResponse> reviewQuestion(JwtAuthenticationToken authToken,
                                                              @PathVariable UUID id,
                                                              @RequestBody ReviewDsaQuestionRequest request) {
        User user = userService.getOrCreateUser(authToken);
        DsaQuestionResponse response = dsaQuestionService.reviewQuestion(user, id, request.quality());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    public ResponseEntity<List<DsaQuestionResponse>> getTodayQuestions(JwtAuthenticationToken authToken,
                                                                        @RequestParam(defaultValue = "5") int limit) {
        User user = userService.getOrCreateUser(authToken);
        List<DsaQuestionResponse> responses = dsaQuestionService.getTodayQuestions(user, limit);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(JwtAuthenticationToken authToken,
                                               @PathVariable UUID id) {
        User user = userService.getOrCreateUser(authToken);
        dsaQuestionService.deleteQuestion(user, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> clearAllQuestions(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        dsaQuestionService.clearAllQuestions(user);
        return ResponseEntity.noContent().build();
    }
}

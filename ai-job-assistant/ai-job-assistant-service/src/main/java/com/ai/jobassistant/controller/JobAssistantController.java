package com.ai.jobassistant.controller;

import com.ai.jobassistant.dto.JobAssistantDTO;
import com.ai.jobassistant.model.JobApplication;
import com.ai.jobassistant.service.JobAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * JobAssistantController
 *
 * REST API for AI-powered job application assistance.
 * All endpoints secured via OAuth 2.0 + JWT.
 *
 * Base URL: /api/v1/assistant
 */
@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${cors.allowed-origins:http://localhost:3000}")
public class JobAssistantController {

    private final JobAssistantService assistantService;

    /**
     * POST /api/v1/assistant/analyze
     * Full AI analysis: JD analysis + resume scoring + cover letter + interview questions.
     * This is the core endpoint that powers the React frontend.
     */
    @PostMapping("/analyze")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobAssistantDTO.AnalysisResponse> analyzeAndGenerate(
        @Valid @RequestBody JobAssistantDTO.AnalyzeRequest request
    ) {
        log.info("📥 POST /analyze | company={} | title={}",
            request.getCompanyName(), request.getJobTitle());

        JobAssistantDTO.AnalysisResponse response = assistantService.analyzeAndGenerate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/assistant/cover-letter/regenerate
     * Regenerate cover letter with different tone (professional/enthusiastic/concise).
     */
    @PostMapping("/cover-letter/regenerate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> regenerateCoverLetter(
        @Valid @RequestBody JobAssistantDTO.CoverLetterRequest request
    ) {
        log.info("✍️ POST /cover-letter/regenerate | applicationId={} | tone={}",
            request.getApplicationId(), request.getTone());

        String coverLetter = assistantService.regenerateCoverLetter(request);
        return ResponseEntity.ok(coverLetter);
    }

    /**
     * POST /api/v1/assistant/chat
     * Conversational AI job search assistant.
     * Optional: pass applicationId for context-aware advice.
     */
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobAssistantDTO.ChatResponse> chat(
        @Valid @RequestBody JobAssistantDTO.ChatRequest request
    ) {
        log.info("💬 POST /chat | message length={}", request.getMessage().length());

        JobAssistantDTO.ChatResponse response = assistantService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/assistant/applications
     * Get all job applications for authenticated user (paginated).
     */
    @GetMapping("/applications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<JobAssistantDTO.ApplicationSummary>> getMyApplications(
        @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(assistantService.getMyApplications(pageable));
    }

    /**
     * GET /api/v1/assistant/applications/{id}
     * Get full application details by ID.
     */
    @GetMapping("/applications/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobAssistantDTO.AnalysisResponse> getApplication(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(assistantService.getApplicationById(id));
    }

    /**
     * PATCH /api/v1/assistant/applications/{id}/status
     * Update application status (APPLIED, INTERVIEW_SCHEDULED, OFFER_RECEIVED etc.)
     */
    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateStatus(
        @PathVariable UUID id,
        @RequestParam JobApplication.ApplicationStatus status
    ) {
        assistantService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/assistant/health
     * Health check for Kubernetes liveness probe.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Job Assistant Service is running ✅");
    }
}

package com.ai.jobassistant.service;

import com.ai.jobassistant.dto.JobAssistantDTO;
import com.ai.jobassistant.exception.ApplicationNotFoundException;
import com.ai.jobassistant.model.JobApplication;
import com.ai.jobassistant.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * JobAssistantService
 *
 * Orchestrates the full AI-powered job application workflow:
 * 1. Analyze JD with GPT-4
 * 2. Score resume against JD (ATS scoring)
 * 3. Generate tailored cover letter
 * 4. Predict interview questions
 * 5. Track application history
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobAssistantService {

    private final JobApplicationRepository applicationRepository;
    private final OpenAiService openAiService;

    /**
     * Full AI analysis — the core feature.
     * Runs JD analysis, resume scoring, cover letter generation,
     * and interview question prediction in parallel using async tasks.
     */
    public JobAssistantDTO.AnalysisResponse analyzeAndGenerate(
        JobAssistantDTO.AnalyzeRequest request
    ) {
        String userId = getCurrentUserId();

        log.info("🚀 Starting AI analysis | userId={} | company={} | title={}",
            userId, request.getCompanyName(), request.getJobTitle());

        // Step 1: Analyze JD
        JobAssistantDTO.JdAnalysis jdAnalysis =
            openAiService.analyzeJobDescription(request.getJobDescription());

        // Step 2: Score resume (if provided)
        JobAssistantDTO.ResumeScore resumeScore = null;
        if (request.isScoreResume() && request.getResumeText() != null) {
            resumeScore = openAiService.scoreResume(
                request.getResumeText(),
                request.getJobDescription()
            );
        }

        // Step 3: Generate cover letter (if requested)
        String coverLetter = null;
        if (request.isGenerateCoverLetter() && request.getResumeText() != null) {
            coverLetter = openAiService.generateCoverLetter(
                request.getResumeText(),
                request.getJobDescription(),
                request.getCompanyName(),
                request.getJobTitle(),
                "professional"
            );
        }

        // Step 4: Generate interview questions (if requested)
        List<String> interviewQuestions = null;
        if (request.isGenerateInterviewQuestions()) {
            interviewQuestions = openAiService.generateInterviewQuestions(
                request.getJobDescription(),
                request.getJobTitle()
            );
        }

        // Step 5: Persist application to DB
        JobApplication application = JobApplication.builder()
            .userId(userId)
            .companyName(request.getCompanyName())
            .jobTitle(request.getJobTitle())
            .jobDescription(request.getJobDescription())
            .resumeText(request.getResumeText())
            .coverLetter(coverLetter)
            .jobUrl(request.getJobUrl())
            .atsScore(resumeScore != null ? resumeScore.getOverallScore() : null)
            .matchedKeywords(resumeScore != null ?
                String.join(",", resumeScore.getMatchedKeywords()) : null)
            .missingKeywords(resumeScore != null ?
                String.join(",", resumeScore.getMissingKeywords()) : null)
            .interviewQuestions(interviewQuestions != null ?
                String.join("||", interviewQuestions) : null)
            .status(JobApplication.ApplicationStatus.READY)
            .build();

        JobApplication saved = applicationRepository.save(application);
        log.info("✅ Application saved | applicationId={} | atsScore={}",
            saved.getId(), saved.getAtsScore());

        return JobAssistantDTO.AnalysisResponse.builder()
            .applicationId(saved.getId())
            .companyName(saved.getCompanyName())
            .jobTitle(saved.getJobTitle())
            .jdAnalysis(jdAnalysis)
            .resumeScore(resumeScore)
            .coverLetter(coverLetter)
            .interviewQuestions(interviewQuestions)
            .status(saved.getStatus())
            .createdAt(saved.getCreatedAt())
            .build();
    }

    /**
     * Regenerate cover letter with a different tone.
     */
    @CacheEvict(value = "applications", key = "#applicationId")
    public String regenerateCoverLetter(JobAssistantDTO.CoverLetterRequest request) {
        String userId = getCurrentUserId();

        JobApplication application = applicationRepository.findById(request.getApplicationId())
            .orElseThrow(() -> new ApplicationNotFoundException(
                "Application not found: " + request.getApplicationId()
            ));

        if (!application.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized to access this application");
        }

        String newCoverLetter = openAiService.generateCoverLetter(
            application.getResumeText(),
            application.getJobDescription(),
            application.getCompanyName(),
            application.getJobTitle(),
            request.getTone()
        );

        application.setCoverLetter(newCoverLetter);
        applicationRepository.save(application);

        log.info("✅ Cover letter regenerated | applicationId={} | tone={}",
            request.getApplicationId(), request.getTone());

        return newCoverLetter;
    }

    /**
     * AI chat — context-aware job search assistant.
     */
    public JobAssistantDTO.ChatResponse chat(JobAssistantDTO.ChatRequest request) {
        String applicationContext = null;

        if (request.getApplicationId() != null) {
            applicationRepository.findById(request.getApplicationId())
                .ifPresent(app -> log.info("💬 Chat with context | company={}", app.getCompanyName()));
        }

        String reply = openAiService.chat(request.getMessage(), applicationContext);

        return JobAssistantDTO.ChatResponse.builder()
            .reply(reply)
            .sessionId(request.getSessionId() != null ?
                request.getSessionId() : UUID.randomUUID().toString())
            .timestamp(java.time.LocalDateTime.now())
            .build();
    }

    /**
     * Get application by ID — cached in Redis.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "applications", key = "#applicationId")
    public JobAssistantDTO.AnalysisResponse getApplicationById(UUID applicationId) {
        JobApplication app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException(
                "Application not found: " + applicationId
            ));

        return mapToFullResponse(app);
    }

    /**
     * Get all applications for authenticated user — paginated.
     */
    @Transactional(readOnly = true)
    public Page<JobAssistantDTO.ApplicationSummary> getMyApplications(Pageable pageable) {
        String userId = getCurrentUserId();
        return applicationRepository.findByUserId(userId, pageable)
            .map(this::mapToSummary);
    }

    /**
     * Update application status (e.g. mark as APPLIED, INTERVIEW_SCHEDULED).
     */
    @CacheEvict(value = "applications", key = "#applicationId")
    public void updateStatus(UUID applicationId, JobApplication.ApplicationStatus newStatus) {
        String userId = getCurrentUserId();

        JobApplication app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException(
                "Application not found: " + applicationId
            ));

        if (!app.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized");
        }

        app.setStatus(newStatus);
        applicationRepository.save(app);
        log.info("✅ Status updated | applicationId={} | status={}", applicationId, newStatus);
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private JobAssistantDTO.AnalysisResponse mapToFullResponse(JobApplication app) {
        return JobAssistantDTO.AnalysisResponse.builder()
            .applicationId(app.getId())
            .companyName(app.getCompanyName())
            .jobTitle(app.getJobTitle())
            .coverLetter(app.getCoverLetter())
            .status(app.getStatus())
            .createdAt(app.getCreatedAt())
            .build();
    }

    private JobAssistantDTO.ApplicationSummary mapToSummary(JobApplication app) {
        return JobAssistantDTO.ApplicationSummary.builder()
            .id(app.getId())
            .companyName(app.getCompanyName())
            .jobTitle(app.getJobTitle())
            .atsScore(app.getAtsScore())
            .status(app.getStatus())
            .createdAt(app.getCreatedAt())
            .build();
    }
}

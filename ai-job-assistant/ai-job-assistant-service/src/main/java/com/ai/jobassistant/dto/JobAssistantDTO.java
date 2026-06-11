package com.ai.jobassistant.dto;

import com.ai.jobassistant.model.JobApplication;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Objects for AI Job Assistant API
 */
public class JobAssistantDTO {

    /**
     * Request to analyze a JD and generate AI content
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AnalyzeRequest {

        @NotBlank(message = "Company name is required")
        private String companyName;

        @NotBlank(message = "Job title is required")
        private String jobTitle;

        @NotBlank(message = "Job description is required")
        @Size(min = 100, max = 10000, message = "JD must be between 100 and 10000 characters")
        private String jobDescription;

        @Size(max = 10000, message = "Resume text too long")
        private String resumeText;

        private String jobUrl;
        private boolean generateCoverLetter = true;
        private boolean generateInterviewQuestions = true;
        private boolean scoreResume = true;
    }

    /**
     * Full AI analysis response
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AnalysisResponse {
        private UUID applicationId;
        private String companyName;
        private String jobTitle;
        private JdAnalysis jdAnalysis;
        private ResumeScore resumeScore;
        private String coverLetter;
        private List<String> interviewQuestions;
        private JobApplication.ApplicationStatus status;
        private LocalDateTime createdAt;
    }

    /**
     * JD analysis breakdown
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class JdAnalysis {
        private List<String> requiredSkills;
        private List<String> preferredSkills;
        private String experienceLevel;     // Junior / Mid / Senior / Staff
        private String roleType;            // Backend / Full Stack / DevOps etc
        private List<String> keyResponsibilities;
        private List<String> techStack;
        private String companyCulture;
        private String salaryRange;         // if mentioned
    }

    /**
     * Resume match score against JD
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResumeScore {
        private Integer overallScore;       // 0-100 ATS score
        private Integer skillsMatchScore;   // 0-100
        private Integer experienceScore;    // 0-100
        private List<String> matchedKeywords;
        private List<String> missingKeywords;
        private List<String> suggestions;   // How to improve resume for this JD
        private String verdict;             // "Strong Match" / "Good Match" / "Needs Work"
    }

    /**
     * Cover letter generation request
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CoverLetterRequest {
        @NotNull
        private UUID applicationId;

        private String tone;      // "professional", "enthusiastic", "concise"
        private String highlight; // specific achievement to highlight
    }

    /**
     * Chat message for conversational AI
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChatRequest {
        @NotBlank
        private String message;

        private UUID applicationId; // optional context
        private String sessionId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChatResponse {
        private String reply;
        private String sessionId;
        private LocalDateTime timestamp;
    }

    /**
     * Application list item
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApplicationSummary {
        private UUID id;
        private String companyName;
        private String jobTitle;
        private Integer atsScore;
        private JobApplication.ApplicationStatus status;
        private LocalDateTime createdAt;
    }
}

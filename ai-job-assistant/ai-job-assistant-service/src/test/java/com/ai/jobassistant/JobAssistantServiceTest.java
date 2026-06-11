package com.ai.jobassistant;

import com.ai.jobassistant.dto.JobAssistantDTO;
import com.ai.jobassistant.model.JobApplication;
import com.ai.jobassistant.repository.JobApplicationRepository;
import com.ai.jobassistant.service.JobAssistantService;
import com.ai.jobassistant.service.OpenAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobAssistantService Unit Tests")
class JobAssistantServiceTest {

    @Mock private JobApplicationRepository applicationRepository;
    @Mock private OpenAiService openAiService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks private JobAssistantService assistantService;

    private static final String USER_ID = "user-789";

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(USER_ID);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should run full AI analysis and save application")
    void shouldRunFullAnalysisAndSave() {
        JobAssistantDTO.AnalyzeRequest request = JobAssistantDTO.AnalyzeRequest.builder()
            .companyName("Google")
            .jobTitle("Senior Software Engineer")
            .jobDescription("We are looking for a Senior Software Engineer with expertise in " +
                "Java, Spring Boot, Kubernetes, and distributed systems to join our team...")
            .resumeText("Phani Varma Dantuluri - Senior Full Stack Java Engineer at Bank of America...")
            .generateCoverLetter(true)
            .generateInterviewQuestions(true)
            .scoreResume(true)
            .build();

        JobAssistantDTO.JdAnalysis mockJdAnalysis = JobAssistantDTO.JdAnalysis.builder()
            .requiredSkills(List.of("Java", "Spring Boot", "Kubernetes"))
            .preferredSkills(List.of("Go", "Terraform"))
            .experienceLevel("Senior")
            .roleType("Backend Engineer")
            .techStack(List.of("Java", "Kubernetes", "GCP"))
            .build();

        JobAssistantDTO.ResumeScore mockScore = JobAssistantDTO.ResumeScore.builder()
            .overallScore(85)
            .skillsMatchScore(90)
            .experienceScore(80)
            .matchedKeywords(List.of("Java", "Spring Boot", "Kubernetes"))
            .missingKeywords(List.of("Go", "GCP"))
            .suggestions(List.of("Add GCP certifications", "Mention distributed systems projects"))
            .verdict("Strong Match")
            .build();

        JobApplication savedApp = JobApplication.builder()
            .id(UUID.randomUUID())
            .userId(USER_ID)
            .companyName("Google")
            .jobTitle("Senior Software Engineer")
            .atsScore(85)
            .status(JobApplication.ApplicationStatus.READY)
            .build();

        when(openAiService.analyzeJobDescription(anyString())).thenReturn(mockJdAnalysis);
        when(openAiService.scoreResume(anyString(), anyString())).thenReturn(mockScore);
        when(openAiService.generateCoverLetter(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn("Dear Hiring Manager, I am excited to apply for the Senior Software Engineer role at Google...");
        when(openAiService.generateInterviewQuestions(anyString(), anyString()))
            .thenReturn(List.of(
                "Describe your experience with distributed systems at scale.",
                "How would you design a high-availability microservice?",
                "Tell me about a time you improved system performance significantly."
            ));
        when(applicationRepository.save(any())).thenReturn(savedApp);

        JobAssistantDTO.AnalysisResponse response = assistantService.analyzeAndGenerate(request);

        assertThat(response).isNotNull();
        assertThat(response.getCompanyName()).isEqualTo("Google");
        assertThat(response.getJdAnalysis()).isNotNull();
        assertThat(response.getResumeScore().getOverallScore()).isEqualTo(85);
        assertThat(response.getResumeScore().getVerdict()).isEqualTo("Strong Match");
        assertThat(response.getCoverLetter()).isNotBlank();
        assertThat(response.getInterviewQuestions()).hasSize(3);

        verify(openAiService).analyzeJobDescription(anyString());
        verify(openAiService).scoreResume(anyString(), anyString());
        verify(openAiService).generateCoverLetter(anyString(), anyString(), eq("Google"), anyString(), anyString());
        verify(applicationRepository).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("Should handle AI chat with context")
    void shouldHandleAiChat() {
        JobAssistantDTO.ChatRequest chatRequest = JobAssistantDTO.ChatRequest.builder()
            .message("How should I prepare for a Java system design interview at Google?")
            .sessionId("session-001")
            .build();

        when(openAiService.chat(anyString(), any()))
            .thenReturn("For Google's system design interviews, focus on scalability, " +
                "distributed systems principles, and be ready to discuss trade-offs...");

        JobAssistantDTO.ChatResponse response = assistantService.chat(chatRequest);

        assertThat(response).isNotNull();
        assertThat(response.getReply()).isNotBlank();
        assertThat(response.getSessionId()).isEqualTo("session-001");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should skip resume scoring when resume text not provided")
    void shouldSkipResumeScoringWhenNoResume() {
        JobAssistantDTO.AnalyzeRequest request = JobAssistantDTO.AnalyzeRequest.builder()
            .companyName("Amazon")
            .jobTitle("Staff Engineer")
            .jobDescription("Amazon is seeking a Staff Engineer with strong Java and AWS expertise...")
            .resumeText(null) // No resume provided
            .generateCoverLetter(false)
            .generateInterviewQuestions(true)
            .scoreResume(true)
            .build();

        JobAssistantDTO.JdAnalysis mockAnalysis = JobAssistantDTO.JdAnalysis.builder()
            .requiredSkills(List.of("Java", "AWS", "System Design"))
            .experienceLevel("Staff")
            .build();

        JobApplication savedApp = JobApplication.builder()
            .id(UUID.randomUUID())
            .userId(USER_ID)
            .companyName("Amazon")
            .status(JobApplication.ApplicationStatus.READY)
            .build();

        when(openAiService.analyzeJobDescription(anyString())).thenReturn(mockAnalysis);
        when(openAiService.generateInterviewQuestions(anyString(), anyString()))
            .thenReturn(List.of("How do you design for fault tolerance on AWS?"));
        when(applicationRepository.save(any())).thenReturn(savedApp);

        JobAssistantDTO.AnalysisResponse response = assistantService.analyzeAndGenerate(request);

        assertThat(response.getResumeScore()).isNull();
        assertThat(response.getCoverLetter()).isNull();
        verify(openAiService, never()).scoreResume(anyString(), anyString());
        verify(openAiService, never()).generateCoverLetter(anyString(), anyString(), anyString(), anyString(), anyString());
    }
}

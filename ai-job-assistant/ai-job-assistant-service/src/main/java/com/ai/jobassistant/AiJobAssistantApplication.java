package com.ai.jobassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI Job Application Assistant - Entry Point
 *
 * An intelligent full-stack assistant that helps job seekers optimize
 * their applications using OpenAI GPT-4.
 *
 * Core Features:
 * - Job Description Analysis (extract skills, requirements, keywords)
 * - Tailored Cover Letter Generation via GPT-4
 * - Resume-to-JD ATS Match Scoring
 * - Interview Question Prediction
 * - Application History Tracking
 * - AI Response Caching via Redis (reduce API costs by 60%)
 *
 * @author Phani Varma Dantuluri
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableKafka
public class AiJobAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiJobAssistantApplication.class, args);
    }
}

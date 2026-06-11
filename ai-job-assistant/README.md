# 🤖 AI Job Application Assistant

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![OpenAI](https://img.shields.io/badge/OpenAI_GPT--4-412991?style=for-the-badge&logo=openai&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)

> An intelligent full-stack AI assistant that helps software engineers optimize job applications using OpenAI GPT-4. Paste a job description and get instant ATS scoring, tailored cover letters, and predicted interview questions.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔍 **JD Analysis** | GPT-4 extracts required skills, tech stack, experience level, culture signals |
| 📊 **ATS Scoring** | Resume-to-JD match score (0-100) with matched/missing keywords |
| ✍️ **Cover Letter** | Personalized GPT-4 cover letters mirroring JD language |
| 🎯 **Interview Prep** | 15 predicted questions: technical, behavioral, system design |
| 💬 **AI Chat** | Context-aware career coach chatbot |
| 📁 **Application Tracker** | Track all applications with status history |
| ⚡ **Redis Caching** | AI responses cached — reduces OpenAI API costs by ~60% |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│             React Frontend (Port 3000)           │
│   Analyze JD  │  View Results  │  AI Chat       │
└───────────────────────┬─────────────────────────┘
                        │ REST API
                        ▼
┌─────────────────────────────────────────────────┐
│         Spring Boot Service (Port 8083)          │
│                                                  │
│  JobAssistantController                          │
│       ↓                                          │
│  JobAssistantService                             │
│       ↓                                          │
│  OpenAiService ──────────────► OpenAI GPT-4 API │
│       ↓                                          │
│  Redis Cache (TTL: 1-24hrs)                      │
│       ↓                                          │
│  PostgreSQL (AWS RDS)                            │
└─────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────┐
│              Apache Kafka Event Bus              │
│  application.analyzed · cover-letter.generated  │
└─────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

**Backend:** Java 17 · Spring Boot 3 · Spring Security · OAuth 2.0 · JWT · WebFlux

**AI:** OpenAI GPT-4 Turbo API · Prompt Engineering · JSON-structured responses

**Frontend:** React 18 · Axios · CSS-in-JS

**Infrastructure:** Docker · Kubernetes · AWS EKS · Terraform · Helm

**Data:** PostgreSQL (AWS RDS) · Redis (response caching) · Apache Kafka

**DevOps:** Jenkins CI/CD · SonarQube · Prometheus · Grafana

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- OpenAI API Key

### Run with Docker Compose

```bash
git clone https://github.com/phanivarma008/ai-job-application-assistant.git
cd ai-job-application-assistant

# Add your OpenAI API key
echo "OPENAI_API_KEY=your-key-here" > .env

# Start all services
docker-compose up -d

# Frontend: http://localhost:3000
# Backend:  http://localhost:8083
```

---

## 📡 API Endpoints

```
POST   /api/v1/assistant/analyze                  - Full AI analysis
POST   /api/v1/assistant/cover-letter/regenerate  - Regenerate cover letter
POST   /api/v1/assistant/chat                     - AI career coach chat
GET    /api/v1/assistant/applications             - Get all applications
GET    /api/v1/assistant/applications/{id}        - Get application by ID
PATCH  /api/v1/assistant/applications/{id}/status - Update status
```

---

## 📊 Sample API Request

```json
POST /api/v1/assistant/analyze

{
  "companyName": "Google",
  "jobTitle": "Senior Software Engineer",
  "jobDescription": "We are looking for a Senior SWE with expertise in Java, Kubernetes...",
  "resumeText": "Phani Varma Dantuluri - Senior Full Stack Java Engineer at Bank of America...",
  "generateCoverLetter": true,
  "generateInterviewQuestions": true,
  "scoreResume": true
}
```

**Response:**
```json
{
  "applicationId": "uuid",
  "jdAnalysis": {
    "requiredSkills": ["Java", "Kubernetes", "Distributed Systems"],
    "experienceLevel": "Senior",
    "techStack": ["Java", "Go", "GCP", "Kubernetes"]
  },
  "resumeScore": {
    "overallScore": 87,
    "verdict": "Strong Match",
    "matchedKeywords": ["Java", "Kubernetes", "Spring Boot"],
    "missingKeywords": ["Go", "GCP"]
  },
  "coverLetter": "Dear Hiring Manager...",
  "interviewQuestions": [
    "Design a distributed rate limiter at Google scale...",
    "Tell me about a time you improved system performance by 40%..."
  ]
}
```

---

## 👨‍💻 Author

**Phani Varma Dantuluri**
Senior Full Stack Java Engineer @ Bank of America

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/dantuluriphanivarma)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:dantuluriphani2@gmail.com)

---

> ⭐ Star this repo if it helped you land your dream job!

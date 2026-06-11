import { useState } from "react";
import axios from "axios";

const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8083/api/v1";

/**
 * AI Job Application Assistant - React Frontend
 * 
 * Features:
 * - Paste JD → Get instant AI analysis
 * - ATS Score with matched/missing keywords
 * - GPT-4 generated cover letter
 * - Predicted interview questions
 * - Chat with AI career coach
 */
export default function App() {
  const [tab, setTab] = useState("analyze");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [chatMessages, setChatMessages] = useState([
    { role: "assistant", text: "Hi! I'm your AI career coach. Paste a job description and I'll help you optimize your application! 🚀" }
  ]);
  const [chatInput, setChatInput] = useState("");

  const [form, setForm] = useState({
    companyName: "",
    jobTitle: "",
    jobDescription: "",
    resumeText: "",
    jobUrl: "",
    generateCoverLetter: true,
    generateInterviewQuestions: true,
    scoreResume: true,
  });

  const handleAnalyze = async () => {
    if (!form.companyName || !form.jobTitle || !form.jobDescription) {
      setError("Company, Job Title and Job Description are required.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const { data } = await axios.post(`${API_BASE}/assistant/analyze`, form, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
      });
      setResult(data);
      setTab("results");
    } catch (err) {
      setError(err.response?.data?.message || "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleChat = async () => {
    if (!chatInput.trim()) return;
    const userMsg = { role: "user", text: chatInput };
    setChatMessages(prev => [...prev, userMsg]);
    setChatInput("");
    try {
      const { data } = await axios.post(`${API_BASE}/assistant/chat`,
        { message: chatInput, applicationId: result?.applicationId },
        { headers: { Authorization: `Bearer ${localStorage.getItem("token")}` } }
      );
      setChatMessages(prev => [...prev, { role: "assistant", text: data.reply }]);
    } catch {
      setChatMessages(prev => [...prev, { role: "assistant", text: "Sorry, AI is temporarily unavailable." }]);
    }
  };

  const getScoreColor = (score) => {
    if (score >= 80) return "#43D98C";
    if (score >= 60) return "#F5C842";
    return "#E31837";
  };

  return (
    <div style={{ fontFamily: "Inter, sans-serif", background: "#0A0C10", minHeight: "100vh", color: "#F0F2F5" }}>

      {/* Header */}
      <div style={{ background: "#111318", borderBottom: "1px solid rgba(255,255,255,0.08)", padding: "16px 32px", display: "flex", alignItems: "center", gap: "12px" }}>
        <span style={{ fontSize: "24px" }}>🤖</span>
        <div>
          <div style={{ fontWeight: 700, fontSize: "18px" }}>AI Job Application Assistant</div>
          <div style={{ fontSize: "12px", color: "#7A8494" }}>Powered by GPT-4 · Spring Boot · Redis</div>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: "flex", gap: "0", background: "#111318", borderBottom: "1px solid rgba(255,255,255,0.08)", padding: "0 32px" }}>
        {["analyze", "results", "chat"].map(t => (
          <button key={t} onClick={() => setTab(t)} style={{
            padding: "12px 24px", border: "none", background: "transparent",
            color: tab === t ? "#4FC3F7" : "#7A8494",
            borderBottom: tab === t ? "2px solid #4FC3F7" : "2px solid transparent",
            cursor: "pointer", fontSize: "14px", fontWeight: 600, textTransform: "capitalize"
          }}>
            {t === "analyze" ? "📋 Analyze JD" : t === "results" ? "📊 Results" : "💬 AI Chat"}
          </button>
        ))}
      </div>

      <div style={{ maxWidth: "900px", margin: "0 auto", padding: "32px" }}>

        {/* ── ANALYZE TAB ── */}
        {tab === "analyze" && (
          <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
              <div>
                <label style={{ fontSize: "12px", color: "#7A8494", display: "block", marginBottom: "6px" }}>COMPANY NAME *</label>
                <input value={form.companyName} onChange={e => setForm({...form, companyName: e.target.value})}
                  placeholder="e.g. Google, JPMorgan, Amazon"
                  style={{ width: "100%", padding: "10px 14px", background: "#111318", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "8px", color: "#F0F2F5", fontSize: "14px" }} />
              </div>
              <div>
                <label style={{ fontSize: "12px", color: "#7A8494", display: "block", marginBottom: "6px" }}>JOB TITLE *</label>
                <input value={form.jobTitle} onChange={e => setForm({...form, jobTitle: e.target.value})}
                  placeholder="e.g. Senior Software Engineer"
                  style={{ width: "100%", padding: "10px 14px", background: "#111318", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "8px", color: "#F0F2F5", fontSize: "14px" }} />
              </div>
            </div>

            <div>
              <label style={{ fontSize: "12px", color: "#7A8494", display: "block", marginBottom: "6px" }}>JOB DESCRIPTION * (paste the full JD)</label>
              <textarea value={form.jobDescription} onChange={e => setForm({...form, jobDescription: e.target.value})}
                rows={8} placeholder="Paste the complete job description here..."
                style={{ width: "100%", padding: "12px 14px", background: "#111318", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "8px", color: "#F0F2F5", fontSize: "13px", resize: "vertical" }} />
            </div>

            <div>
              <label style={{ fontSize: "12px", color: "#7A8494", display: "block", marginBottom: "6px" }}>YOUR RESUME TEXT (for ATS scoring)</label>
              <textarea value={form.resumeText} onChange={e => setForm({...form, resumeText: e.target.value})}
                rows={6} placeholder="Paste your resume text here for ATS match scoring..."
                style={{ width: "100%", padding: "12px 14px", background: "#111318", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "8px", color: "#F0F2F5", fontSize: "13px", resize: "vertical" }} />
            </div>

            <div style={{ display: "flex", gap: "16px" }}>
              {[["generateCoverLetter", "✍️ Cover Letter"], ["generateInterviewQuestions", "🎯 Interview Questions"], ["scoreResume", "📊 ATS Score"]].map(([key, label]) => (
                <label key={key} style={{ display: "flex", alignItems: "center", gap: "8px", cursor: "pointer", fontSize: "13px", color: form[key] ? "#4FC3F7" : "#7A8494" }}>
                  <input type="checkbox" checked={form[key]} onChange={e => setForm({...form, [key]: e.target.checked})} />
                  {label}
                </label>
              ))}
            </div>

            {error && <div style={{ padding: "12px 16px", background: "rgba(227,24,55,0.1)", border: "1px solid rgba(227,24,55,0.3)", borderRadius: "8px", color: "#E31837", fontSize: "13px" }}>{error}</div>}

            <button onClick={handleAnalyze} disabled={loading}
              style={{ padding: "14px 28px", background: loading ? "#333" : "#4FC3F7", color: "#0A0C10", border: "none", borderRadius: "8px", fontWeight: 700, fontSize: "15px", cursor: loading ? "not-allowed" : "pointer" }}>
              {loading ? "🤖 Analyzing with GPT-4..." : "🚀 Analyze & Generate"}
            </button>
          </div>
        )}

        {/* ── RESULTS TAB ── */}
        {tab === "results" && result && (
          <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>

            {/* ATS Score */}
            {result.resumeScore && (
              <div style={{ background: "#111318", border: "1px solid rgba(255,255,255,0.08)", borderRadius: "12px", padding: "24px" }}>
                <div style={{ fontSize: "12px", color: "#7A8494", letterSpacing: "0.1em", marginBottom: "16px" }}>ATS MATCH SCORE</div>
                <div style={{ display: "flex", alignItems: "center", gap: "20px" }}>
                  <div style={{ fontSize: "56px", fontWeight: 800, color: getScoreColor(result.resumeScore.overallScore) }}>
                    {result.resumeScore.overallScore}
                  </div>
                  <div>
                    <div style={{ fontSize: "18px", fontWeight: 700, color: getScoreColor(result.resumeScore.overallScore) }}>{result.resumeScore.verdict}</div>
                    <div style={{ fontSize: "12px", color: "#7A8494", marginTop: "4px" }}>Skills: {result.resumeScore.skillsMatchScore}% · Experience: {result.resumeScore.experienceScore}%</div>
                  </div>
                </div>
                <div style={{ display: "flex", gap: "12px", marginTop: "16px", flexWrap: "wrap" }}>
                  {result.resumeScore.matchedKeywords?.map(k => (
                    <span key={k} style={{ padding: "3px 10px", background: "rgba(67,217,140,0.1)", border: "1px solid rgba(67,217,140,0.3)", borderRadius: "4px", fontSize: "11px", color: "#43D98C" }}>✓ {k}</span>
                  ))}
                  {result.resumeScore.missingKeywords?.map(k => (
                    <span key={k} style={{ padding: "3px 10px", background: "rgba(227,24,55,0.1)", border: "1px solid rgba(227,24,55,0.3)", borderRadius: "4px", fontSize: "11px", color: "#E31837" }}>✗ {k}</span>
                  ))}
                </div>
              </div>
            )}

            {/* Cover Letter */}
            {result.coverLetter && (
              <div style={{ background: "#111318", border: "1px solid rgba(255,255,255,0.08)", borderRadius: "12px", padding: "24px" }}>
                <div style={{ fontSize: "12px", color: "#7A8494", letterSpacing: "0.1em", marginBottom: "16px" }}>AI-GENERATED COVER LETTER</div>
                <pre style={{ whiteSpace: "pre-wrap", fontSize: "13px", color: "#C8CDD6", lineHeight: 1.7, fontFamily: "inherit" }}>{result.coverLetter}</pre>
                <button onClick={() => navigator.clipboard.writeText(result.coverLetter)}
                  style={{ marginTop: "12px", padding: "8px 16px", background: "rgba(79,195,247,0.1)", border: "1px solid rgba(79,195,247,0.3)", borderRadius: "6px", color: "#4FC3F7", cursor: "pointer", fontSize: "12px" }}>
                  📋 Copy to Clipboard
                </button>
              </div>
            )}

            {/* Interview Questions */}
            {result.interviewQuestions && (
              <div style={{ background: "#111318", border: "1px solid rgba(255,255,255,0.08)", borderRadius: "12px", padding: "24px" }}>
                <div style={{ fontSize: "12px", color: "#7A8494", letterSpacing: "0.1em", marginBottom: "16px" }}>PREDICTED INTERVIEW QUESTIONS</div>
                {result.interviewQuestions.map((q, i) => (
                  <div key={i} style={{ padding: "12px 16px", background: "#0A0C10", borderRadius: "8px", marginBottom: "8px", fontSize: "13px", color: "#C8CDD6", display: "flex", gap: "12px" }}>
                    <span style={{ color: "#4FC3F7", fontWeight: 700, minWidth: "24px" }}>{i + 1}.</span>
                    {q}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {tab === "results" && !result && (
          <div style={{ textAlign: "center", padding: "60px", color: "#7A8494" }}>
            <div style={{ fontSize: "48px", marginBottom: "16px" }}>📊</div>
            <div>No results yet. Go to the Analyze tab and paste a job description.</div>
          </div>
        )}

        {/* ── CHAT TAB ── */}
        {tab === "chat" && (
          <div style={{ display: "flex", flexDirection: "column", height: "500px" }}>
            <div style={{ flex: 1, overflowY: "auto", display: "flex", flexDirection: "column", gap: "12px", marginBottom: "16px" }}>
              {chatMessages.map((msg, i) => (
                <div key={i} style={{ display: "flex", justifyContent: msg.role === "user" ? "flex-end" : "flex-start" }}>
                  <div style={{
                    maxWidth: "70%", padding: "12px 16px", borderRadius: "12px",
                    background: msg.role === "user" ? "#4FC3F7" : "#111318",
                    color: msg.role === "user" ? "#0A0C10" : "#F0F2F5",
                    border: msg.role === "assistant" ? "1px solid rgba(255,255,255,0.08)" : "none",
                    fontSize: "14px", lineHeight: 1.6
                  }}>
                    {msg.text}
                  </div>
                </div>
              ))}
            </div>
            <div style={{ display: "flex", gap: "12px" }}>
              <input value={chatInput} onChange={e => setChatInput(e.target.value)}
                onKeyDown={e => e.key === "Enter" && handleChat()}
                placeholder="Ask your AI career coach anything..."
                style={{ flex: 1, padding: "12px 16px", background: "#111318", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "8px", color: "#F0F2F5", fontSize: "14px" }} />
              <button onClick={handleChat}
                style={{ padding: "12px 20px", background: "#4FC3F7", color: "#0A0C10", border: "none", borderRadius: "8px", fontWeight: 700, cursor: "pointer" }}>
                Send
              </button>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}

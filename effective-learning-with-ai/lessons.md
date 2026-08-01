# Lesson Plan: Building Effective AI Tutoring Systems

*Source: `research/effective-learning-with-ai/report.md`*
*Each lesson → one HTML file in `lessons/`, one reference doc in `reference/`*

---

## Module 1 — Learning Science Foundations
*Why it matters first: architecture decisions only make sense if you understand what learning actually is.*

### Lesson 1: The Two Enemies of Learning (and how AI solves them)
**File:** `lessons/0001-two-enemies-of-learning.html`
**Key concepts:** Fluency vs storage strength · retrieval practice · spaced repetition
**Source paper:** arxiv 2309.13060 — 51 students, GPT-3 + neural student model, +15 percentile points
**Skill:** Given a learning scenario, identify whether the design builds fluency or storage strength, and redesign for storage
**Reference doc:** `reference/retrieval-practice-cheatsheet.html`

### Lesson 2: Zone of Proximal Development — Teaching at the Edge
**File:** `lessons/0002-zone-of-proximal-development.html`
**Key concepts:** ZPD · adaptive scaffolding · Evidence-Centered Design · Social Cognitive Theory
**Source paper:** arxiv 2508.01503 — Inquizzitor (AAAI 2026), human-AI hybrid intelligence
**Skill:** Given a student's current state, identify whether a prompt is in ZPD, too easy, or too hard
**Reference doc:** `reference/zpd-scaffold-glossary.html`

### Lesson 3: The Learning-Performance Paradox
**File:** `lessons/0003-learning-performance-paradox.html`
**Key concepts:** Cognitive growth vs task output · AI companions vs assistants · transfer of learning
**Source paper:** arxiv 2605.04816 — five case studies across educational levels
**Skill:** Classify a design decision as "helps the learner think" vs "thinks for the learner"
**Reference doc:** *(add to `reference/zpd-scaffold-glossary.html`)*

---

## Module 2 — Technical Architecture
*The "how to build it" module — components, retrieval, and planning.*

### Lesson 4: Knowledge Graph-Enhanced RAG (KG-RAG)
**File:** `lessons/0004-kg-rag-architecture.html`
**Key concepts:** RAG vs KG-RAG · conceptual relationships · semantic drift
**Source paper:** arxiv 2311.17696 — 35% assessment score increase (n=76, p<0.001)
**Skill:** Sketch the flow of a student query through a KG-RAG pipeline, labeling each component
**Reference doc:** `reference/kg-rag-architecture-diagram.html`

### Lesson 5: Dynamic Knowledge Tracing with RPKT
**File:** `lessons/0005-rpkt-knowledge-tracing.html`
**Key concepts:** Unknown unknowns · prerequisite extraction · recursive knowledge tracing · learning paths
**Source paper:** arxiv 2508.11892 — RPKT, no pre-built curriculum needed
**Skill:** Given a student question and a knowledge gap, trace the prerequisite chain 2–3 levels deep
**Reference doc:** `reference/knowledge-tracing-algorithms.html`

### Lesson 6: Multi-Agent Learning Path Planning (MALPP)
**File:** `lessons/0006-multi-agent-learning-path.html`
**Key concepts:** Learner Analytics Agent · Path Planning Agent · Reflection Agent · Cognitive Load Theory
**Source paper:** arxiv 2601.17346 — MALPP on MOOCCubeX, outperforms 7 LLM baselines
**Skill:** Assign responsibilities across the three MALPP agents for a given tutoring scenario
**Reference doc:** *(extend `reference/kg-rag-architecture-diagram.html` with agent roles)*

---

## Module 3 — Personalization and Student Modeling
*Moving from "one prompt fits all" to a system that knows its student.*

### Lesson 7: TASA — Three Dimensions of a Student Model
**File:** `lessons/0007-tasa-student-model.html`
**Key concepts:** Persona (proficiency profile) · event memory log · forgetting curve · knowledge tracing
**Source paper:** arxiv 2511.15163 — TASA, superior outcomes in math tutoring
**Skill:** Design a student model schema capturing the three TASA dimensions for a given domain
**Reference doc:** `reference/student-model-schema.html`

### Lesson 8: Closing the Diagnose → Recommend → Reassess Loop
**File:** `lessons/0008-diagnose-recommend-reassess.html`
**Key concepts:** Skill Gap Agent · Recommender Agent · misconception detection · closed feedback loop
**Source paper:** arxiv 2601.15551 — ALIGNAgent, precision 0.87–0.90 on two CS courses
**Skill:** Trace a student error through the full DRR loop, identifying which agent acts at each step
**Reference doc:** *(extend `reference/student-model-schema.html` with DRR loop)*

### Lesson 9: Personalization via Learning Styles and Socratic Dialogue
**File:** `lessons/0009-pace-learning-styles.html`
**Key concepts:** Felder-Silverman model · Socratic method · persona simulation · personalized datasets
**Source paper:** arxiv 2502.12633 — PACE, outperforms baselines in motivation and outcomes
**Skill:** Rewrite a direct-answer response as a Socratic exchange that guides without giving the answer
**Reference doc:** *(extend `reference/zpd-scaffold-glossary.html`)*

---

## Module 4 — Evaluation and State of the Art
*How do you know if your tutoring system actually works?*

### Lesson 10: What Off-the-Shelf LLMs Can't Do (Yet)
**File:** `lessons/0010-llm-tutoring-limits.html`
**Key concepts:** Adaptivity benchmarking · student knowledge modeling gap · open-ended questioning
**Source paper:** arxiv 2504.05570 — Llama3/GPT-4o vs 75 ITS scenarios
**Skill:** Given a tutoring interaction transcript, identify which adaptive behaviors are missing
**Reference doc:** `reference/itsbenchmark-checklist.html`

### Lesson 11: Evaluating with TutorGym
**File:** `lessons/0011-tutorgym-evaluation.html`
**Key concepts:** Interactive evaluation vs final-answer benchmarks · hint generation · 223 ITS domains
**Source paper:** arxiv 2505.01563 — TutorGym testbed
**Skill:** Describe how you would run a TutorGym-style evaluation for a custom tutoring domain
**Reference doc:** *(extend `reference/itsbenchmark-checklist.html`)*

### Lesson 12: GraphMASAL — The Current State of the Art
**File:** `lessons/0012-graphmasal-architecture.html`
**Key concepts:** Dynamic KG + LangGraph agents + two-stage neural IR · Diagnostician/Planner/Tutor
**Source paper:** arxiv 2511.11035 — GraphMASAL, blinded automated evaluation wins
**Skill:** Compare your own architecture sketch (from prior lessons) to GraphMASAL — what's missing?
**Reference doc:** *(extend `reference/kg-rag-architecture-diagram.html`)*

### Lesson 13: Synthetic Students with BEAGLE
**File:** `lessons/0013-beagle-synthetic-students.html`
**Key concepts:** Self-Regulated Learning theory · Bayesian Knowledge Tracing · flaw injection · semi-Markov model
**Source paper:** arxiv 2602.13280 — BEAGLE, 52.8% human/AI discrimination accuracy (≈ chance)
**Skill:** Design a synthetic student scenario to stress-test one part of a tutoring system
**Reference doc:** *(extend `reference/knowledge-tracing-algorithms.html`)*

---

## Suggested Teaching Order

```
1 → 2 → 3   (why learning is hard)
4 → 5 → 6   (how to build the knowledge layer)
7 → 8 → 9   (how to model and serve the student)
10 → 11 → 12 → 13  (how to evaluate and iterate)
```

Start with **Lesson 1** — it reframes every design decision that follows.

---

## Reference Documents to Build

| File | Contents |
|------|----------|
| `reference/retrieval-practice-cheatsheet.html` | Spacing curves, retrieval intervals, interleaving rules |
| `reference/zpd-scaffold-glossary.html` | ZPD, scaffolding, Evidence-Centered Design, Socratic method |
| `reference/kg-rag-architecture-diagram.html` | Pipeline diagram: query → KG lookup → LLM generation |
| `reference/knowledge-tracing-algorithms.html` | BKT, DKT, RPKT comparison table |
| `reference/student-model-schema.html` | Proficiency profile + event log + forgetting curve schema |
| `reference/itsbenchmark-checklist.html` | Checklist of adaptive behaviors a tutor must exhibit |

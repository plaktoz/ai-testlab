# Research: Loop Engineering
*Generated: 2026-07-27 | Scope: Loop engineering as a conceptual + practical framework — covering human-in-the-loop vs. autonomous agent loops, agentic loop design patterns (eval-act cycles, tool use, state management), and how loop engineering manifests across LLM pipelines, robotics, and CI/CD — for personal understanding and wiki ingestion.*

## Research Outline

1. The Loop as a Universal Pattern — the observe→decide→act cycle and why it appears everywhere
2. The Human-in-the-Loop Spectrum — from fully manual to fully autonomous, and the oversight models in between
3. Agentic Loop Patterns for LLM Systems — ReAct, reflection, planning, multi-agent, and state management
4. Loop Engineering Across Domains — how the same principles manifest in military strategy, robotics, CI/CD, and software
5. Failure Modes and Design Principles — what breaks loops and how to build robust ones

---

## Section 1: The Loop as a Universal Pattern

### The OODA Loop: Origins and Universal Structure

- **Source**: https://www.techtarget.com/searchcio/definition/OODA-loopxx
- **Summary**: Developed by U.S. Air Force Colonel John Boyd from aerial combat analysis, the OODA loop (Observe → Orient → Decide → Act) is a universal decision-cycle framework for operating in uncertain, fast-changing environments. Boyd's insight was that *tempo* — cycling faster than your opponent — creates strategic advantage independent of raw capability. The model has since been adopted in cybersecurity, ML, and data analytics.
- **Relevance**: Provides the foundational theory underlying all loop engineering — the structure that every intelligent system (human or artificial) executes when operating in the world.

### Building Effective AI Agents — The Agentic Loop

- **Source**: https://www.anthropic.com/engineering/building-effective-agents
- **Summary**: Anthropic distinguishes between workflows (predefined code paths) and agents (LLMs that dynamically direct their own processes). The core agentic loop is: receive task → plan and act using tools → gather ground truth from the environment → pause at checkpoints → terminate. Agents are described as "just LLMs using tools based on environmental feedback in a loop." The article recommends starting with the simplest possible design and adding complexity only when measurably beneficial.
- **Relevance**: The canonical Anthropic framework for thinking about what an agentic loop is and when each loop pattern (chaining, routing, parallelization, orchestration, eval-act) applies.

### Agentic Design Patterns Part 1 — Why Loops Beat Single-Pass

- **Source**: https://www.deeplearning.ai/the-batch/how-agents-can-improve-llm-performance/
- **Summary**: Andrew Ng argues that agentic workflows may drive more near-term AI progress than next-generation models. On HumanEval, GPT-3.5 zero-shot scores 48.1%; GPT-3.5 in an agent loop reaches 95.1% — surpassing GPT-4 zero-shot at 67.0%. He identifies four agentic design patterns: Reflection, Tool Use, Planning, and Multi-Agent Collaboration. The root insight: current LLM use is mostly one-shot, like "writing an essay without being allowed to backspace."
- **Relevance**: The empirical case for loops — the performance gap between single-pass and iterative agent loop approaches is larger than the gap between model generations.

---

## Section 2: The Human-in-the-Loop Spectrum

### Google Cloud — Human-in-the-Loop: Full Spectrum

- **Source**: https://cloud.google.com/discover/human-in-the-loop
- **Summary**: The article maps AI systems onto an autonomy spectrum from full human control to full automation. The three oversight models are: Human-in-the-loop (human actively participates in each decision cycle), Human-on-the-loop (human monitors and can intervene but the system runs autonomously), and Human-out-of-the-loop (fully automated). It provides concrete criteria for each position: involve humans for high-stakes decisions, low confidence scores, novel edge cases, and regulatory requirements; automate fully for high-volume repetitive tasks with consistently high model confidence.
- **Relevance**: The clearest articulation of the full HITL spectrum with actionable criteria for where to position a system on it.

### Chip Huyen — Human Oversight in Agentic Systems

- **Source**: https://huyenchip.com/2025/01/07/agents.html
- **Summary**: Huyen frames human oversight as a continuum: humans can intervene at any stage — providing plans, validating them, or approving risky operations. She introduces a severity-based rule for write actions: "just as you shouldn't give an intern the authority to delete your production database, you shouldn't allow an unreliable AI to initiate bank transfers." The key design principle is defining automation level per action type, not per system.
- **Relevance**: Brings the HITL spectrum down to the action level — rather than setting system-wide autonomy, you set different oversight levels for different action types based on reversibility and consequence.

### Anthropic — Human Checkpoints in the Agentic Loop

- **Source**: https://www.anthropic.com/engineering/building-effective-agents
- **Summary**: Anthropic recommends building human checkpoints at natural pause points within the agentic loop — particularly before irreversible actions and when the agent encounters ambiguity or blockers it cannot resolve. The article frames this as a design choice in the loop's stopping conditions, not as an external add-on. The practical guidance: identify which steps in your loop are irreversible, and place human-in-the-loop gates before those steps.
- **Relevance**: Operationalizes the HITL spectrum at the architecture level — where exactly in the loop to insert human oversight, and why.

---

## Section 3: Agentic Loop Patterns for LLM Systems

### Lilian Weng — LLM Powered Autonomous Agents

- **Source**: https://lilianweng.github.io/posts/2023-06-23-agent/
- **Summary**: The seminal survey of LLM agent architectures. Three components define any agent system: Planning (CoT, Tree of Thoughts, PDDL), Memory (sensory/short-term/long-term mapped to embedding/context/vector store), and Tool Use (MRKL, Toolformer, API-Bank). ReAct implements the loop as "Thought → Action → Observation" cycling until completion. Reflexion extends this with dynamic memory — after each action, the agent evaluates its trajectory and can restart if it detects failure (repeated identical actions → identical observations). Key identified challenges: finite context limits historical awareness; LLMs struggle with long-horizon replanning when early errors compound.
- **Relevance**: The foundational taxonomy for understanding what a loop consists of — planning, memory, and tool use — and how each interacts within the cycle.

### LangChain — Reflection Agents: Three Architectures

- **Source**: https://www.langchain.com/blog/reflection-agents
- **Summary**: Covers three reflection loop architectures of increasing sophistication. Basic Reflection: two chained LLMs (generator + reflector) running a fixed number of iterations. Reflexion: actor-agent that grounds critique in external evidence, looping as "draft → execute tools → revise" up to MAX_ITERATIONS. LATS (Language Agent Tree Search): unifies Reflexion, Tree of Thoughts, and plan-and-execute via Monte Carlo Tree Search — select best candidate, expand parallel actions, reflect and score, backpropagate scores to root. The tradeoff across all three: latency for quality; saved trajectories become fine-tuning data.
- **Relevance**: The clearest progression from simple reflection loops to tree-search-based loops — shows how the same core cycle can be made more sophisticated without changing its fundamental structure.

### Andrew Ng — Reflection as an Agentic Loop Pattern

- **Source**: https://www.deeplearning.ai/the-batch/agentic-design-patterns-part-2-reflection/
- **Summary**: Reflection automates the feedback loop users already apply manually when pushing a chatbot to improve responses. The cycle: generate → critique → revise, repeating multiple times. Extensions include grounding critique in tool outputs (unit test results, search results) and using separate generator/critic agents. Best applied to code generation, writing, and question answering where quality criteria can be articulated. Andrew notes it's "relatively quick to implement" with "surprising performance gains."
- **Relevance**: Concisely establishes reflection as the simplest and most reusable loop pattern — one that can be added to almost any LLM workflow without architectural overhaul.

### Andrew Ng — Planning as an Agentic Loop Pattern

- **Source**: https://www.deeplearning.ai/the-batch/agentic-design-patterns-part-4-planning/
- **Summary**: Planning is the pattern where the LLM determines a multi-step sequence at runtime rather than following a predefined workflow. A live demo illustrates dynamic adaptation: when a search API rate-limited, the agent pivoted to Wikipedia without intervention. The key distinction: planning adds value specifically when task decomposition cannot be specified in advance — for fixed decompositions, workflows suffice. Considered less mature than reflection or tool use, but with rapid improvement expected.
- **Relevance**: Clarifies when you need a planning loop vs. a simpler chaining workflow — the decision hinges on whether task decomposition is predictable at design time.

### Chip Huyen — Agentic Loops & State Management

- **Source**: https://huyenchip.com/2025/01/07/agents.html
- **Summary**: Huyen frames planning as a search problem — evaluating paths toward a goal using heuristics or AI judges to validate before execution. Control flows within a plan include sequential, parallel, if-statement (routing), and for-loop structures — mapping directly to software engineering constructs. The decoupling principle: generate a plan, validate it, then execute. Evaluation uses a dataset of (task, tool inventory) tuples; key metrics are valid plan ratio, plans-per-valid-plan, and invalid tool call frequency.
- **Relevance**: Bridges agentic loop architecture to software engineering constructs, making the patterns concrete and measurable.

---

## Section 4: Loop Engineering Across Domains

### OODA Loop — From Military to Technology

- **Source**: https://www.techtarget.com/searchcio/definition/OODA-loop
- **Summary**: Boyd's OODA loop originated from aerial combat but maps onto any system operating under uncertainty. In cybersecurity it structures incident response. In ML, Orient-phase models identify potential outcomes. In data analytics it cuts through overload to actionable decisions. The strategic insight — cycling tempo as a competitive weapon — applies anywhere a system's next action depends on environmental feedback. The loop is inherently iterative and self-similar: each complete cycle feeds the next Observe phase.
- **Relevance**: Establishes that loop engineering isn't a new concept — it's the fundamental pattern of any adaptive system, with the same structure recurring from military doctrine to robotics to LLM agents.

### Anthropic — Workflow Patterns as Loop Variants

- **Source**: https://www.anthropic.com/engineering/building-effective-agents
- **Summary**: The five workflow patterns (prompt chaining, routing, parallelization, orchestrator-workers, evaluator-optimizer) are all variants of the same loop structure at different scales and degrees of dynamism. Prompt chaining is a linear loop. Routing is a conditional branch. Parallelization is a loop with concurrent branches. Orchestrator-workers is a hierarchical loop. Evaluator-optimizer is a feedback loop. The pattern that works for LLM pipelines maps structurally to CI/CD pipelines (build→test→deploy with feedback) and robotics (sense→plan→act with environment feedback).
- **Relevance**: Shows the cross-domain universality — the same five patterns recur in CI/CD, robotics control, and LLM agent design, just with different execution substrates.

### Andrew Ng — Why Loops Dominate: Empirical Cross-Domain Evidence

- **Source**: https://www.deeplearning.ai/the-batch/how-agents-can-improve-llm-performance/
- **Summary**: The 48.1% → 95.1% jump on HumanEval from zero-shot to agent loop mirrors improvement patterns seen in other iterative systems: CI/CD (test-fix cycles), compiler design (parse-error-recompile loops), and iterative refinement in numerical optimization. Ng's argument — that iteration yields more than model size — generalizes: the ability to observe, evaluate, and revise is a more powerful primitive than raw capability in a single pass.
- **Relevance**: The empirical grounding for why loop engineering matters as a first-class design concern, not just a nice-to-have.

---

## Section 5: Failure Modes and Design Principles

### Chip Huyen — Taxonomy of Agentic Loop Failures

- **Source**: https://huyenchip.com/2025/01/07/agents.html
- **Summary**: Three failure categories. Planning failures: invalid tool calls (hallucinated function names), correct tools with wrong parameters, goal failures (solving the wrong task), and reflection errors (agent incorrectly believes task is complete). Tool failures: incorrect outputs, translation errors between natural language and executable commands, missing tools for certain domains. Efficiency failures: excessive steps per task, unnecessary sequential execution when parallelism was available. Each category requires different mitigations — validation at plan stage, grounding for tool calls, and flow analysis for efficiency.
- **Relevance**: The most complete taxonomy of loop failure modes available — essential for designing robust loops because you can only guard against failures you've named.

### Lilian Weng — Three Hard Problems in Loop Design

- **Source**: https://lilianweng.github.io/posts/2023-06-23-agent/
- **Summary**: Three structural challenges in LLM agent loops. First, finite context limits: history is bounded, so long loops lose early context — relevant to state management design. Second, long-term planning difficulty: LLMs struggle to adapt when unexpected errors arise mid-plan, because they weren't trained to recover from compounding mistakes. Third, natural language interface fragility: formatting errors and non-compliance require significant output-parsing code. Each challenge has a design response: external memory, checkpointing + replanning triggers, and structured output formats.
- **Relevance**: Identifies the three places where loops break — context, planning horizon, and interface — and points toward architectural mitigations for each.

### Anthropic — Core Design Principles for Robust Loops

- **Source**: https://www.anthropic.com/engineering/building-effective-agents
- **Summary**: Three principles for robust agentic loop design. Simplicity: prefer the least complex design that meets requirements — single LLM calls handle most tasks. Transparency: make planning steps explicit and visible so failures are diagnosable. Tool investment: treat tool documentation like production code — agent reliability is bounded by tool reliability. The practical heuristic: complexity should be added only when simpler designs demonstrably fail, not preemptively.
- **Relevance**: The clearest distillation of design principles — start simple, expose internals, invest in interfaces — that apply regardless of loop architecture chosen.

### LangChain — Trajectory Storage as Loop Improvement Mechanism

- **Source**: https://www.langchain.com/blog/reflection-agents
- **Summary**: Saving loop trajectories (the full sequence of actions, observations, and critiques) enables two improvement paths: using trajectories as memory for future loops (in-context learning) or as fine-tuning data to improve the underlying model. This closes a second-order loop around the primary task loop — the system that runs tasks also generates the data that makes future task runs better. LATS in particular accumulates trajectory scores that can be used for model distillation.
- **Relevance**: Introduces the meta-loop concept — loops that improve loops — which is the highest-leverage pattern in loop engineering and the mechanism behind most successful LLM agent quality improvements.

---

## Articles to Ingest

URLs ready for `/kb-scrapecontent` → `/kb-ingest`:

- https://www.anthropic.com/engineering/building-effective-agents
- https://lilianweng.github.io/posts/2023-06-23-agent/
- https://huyenchip.com/2025/01/07/agents.html
- https://www.langchain.com/blog/reflection-agents
- https://www.deeplearning.ai/the-batch/how-agents-can-improve-llm-performance/
- https://www.deeplearning.ai/the-batch/agentic-design-patterns-part-2-reflection/
- https://www.deeplearning.ai/the-batch/agentic-design-patterns-part-4-planning/
- https://cloud.google.com/discover/human-in-the-loop
- https://www.techtarget.com/searchcio/definition/OODA-loop

# Lesson Plan: Building Adversarial Agents

*Source: `research/how-to-build-adversarial-agent/report.md`*
*Each lesson → one HTML file in `lessons/`, one reference doc in `reference/`*

---

## Module 1 — Foundations
*Establish vocabulary, threat models, and agent archetypes before getting into attack mechanics.*

### Lesson 1: Adversarial Agent Taxonomy — Types, Roles, and the Threat Landscape
**File:** `lessons/0001-adversarial-agent-taxonomy.html`
**Key concepts:** adversarial agents · red-teaming · attack surface · dual-use LLMs · attacker-defender roles
**Source papers:**
- Weng (2023) — https://lilianweng.github.io/posts/2023-10-25-adv-attack-llm/ — five attack categories: token manipulation, gradient-based, jailbreaks, human red-teaming, model-based red-teaming
- Security & Privacy survey (https://arxiv.org/abs/2312.02003) — beneficial / offensive / inherent-vulnerability taxonomy of LLM security research
- LLM Agents survey (https://arxiv.org/abs/2309.07864) — brain-perception-action framework for classifying agent types
**Skill:** Draw a taxonomy diagram mapping at least 5 adversarial agent types (e.g., gradient attacker, jailbreak agent, red-team LM, prompt injector, sleeper agent) to their primary attack vectors and target surfaces.
**Reference doc:** `reference/adversarial-agent-types.html`

---

### Lesson 2: LM-as-Red-Teamer — From Manual Testing to Automated Adversarial Agents
**File:** `lessons/0002-lm-as-red-teamer.html`
**Key concepts:** automated red-teaming · attack diversity · RLHF robustness · scaling behavior · red-team data
**Source papers:**
- Perez et al. (https://arxiv.org/abs/2202.03286) — LM generates adversarial test cases; zero-shot to RL methods uncovered tens of thousands of harmful outputs in a 280B chatbot
- Ganguli et al. (https://arxiv.org/abs/2209.07858) — 39,000-attack dataset; RLHF models get harder to attack at scale while other types plateau
**Skill:** Write a zero-shot red-teaming prompt that instructs an LLM to generate 5 structurally distinct attack variations for a given policy target (e.g., "do not provide medical advice"). Identify which of Perez's methods each variant maps to.
**Reference doc:** *(extend existing `reference/adversarial-agent-types.html`)*

---

## Module 2 — Attack Techniques
*Understand specific attack mechanisms in depth — gradient-based, evolutionary, injection-based, and large-scale empirical findings — before designing defenses or evaluation harnesses.*

### Lesson 3: Gradient-Based Attacks — Adversarial Suffixes That Transfer Across Models
**File:** `lessons/0003-gradient-based-attacks.html`
**Key concepts:** GCG · greedy coordinate gradient · adversarial suffix · white-box to black-box transferability
**Source paper:** Zou et al. (https://arxiv.org/abs/2307.15043) — greedy + gradient search generates suffixes on open-source models (Vicuna) that transfer to ChatGPT, Bard, and Claude; fully automates jailbreak discovery
**Skill:** Given a sample adversarial suffix from the paper, annotate its structural pattern: identify the gradient optimization target, the initialization string, and the transfer hypothesis. Then describe in two sentences why this suffix should or should not transfer to a model it was not optimized on.
**Reference doc:** `reference/attack-taxonomy.html`

---

### Lesson 4: Evolutionary Jailbreaks — AutoDAN and Semantic Coherence as a Weapon
**File:** `lessons/0004-autodan-evolutionary-jailbreaks.html`
**Key concepts:** genetic algorithm · semantic coherence · perplexity bypass · hierarchical mutation · cross-model transferability
**Source paper:** Zhu et al. AutoDAN (https://arxiv.org/abs/2310.04451) — hierarchical genetic algorithm generates semantically coherent jailbreaks that evade perplexity-based defenses; strong cross-model transferability; ICLR 2024
**Skill:** Design a population-based mutation procedure in pseudocode: define an initial "parent" jailbreak prompt, specify a mutation operator that preserves semantic coherence, and describe the fitness function used to select the next generation.
**Reference doc:** *(extend `reference/attack-taxonomy.html`)*

---

### Lesson 5: Indirect Prompt Injection — Hijacking Agents Through External Content
**File:** `lessons/0005-indirect-prompt-injection.html`
**Key concepts:** indirect prompt injection · tool-use attack surface · data exfiltration · agent hijacking · content poisoning
**Source paper:** Greshake et al. (https://arxiv.org/abs/2302.12173) — malicious instructions in retrieved external content hijack Bing GPT-4 Chat and code engines; enables data theft, API hijacking, and information ecosystem contamination; mitigations largely absent
**Skill:** Write an indirect prompt injection payload targeting a hypothetical travel-booking agent that reads user emails. The payload should be hidden in an "email body" and attempt to redirect a booking to an attacker-controlled address.
**Reference doc:** *(extend `reference/attack-taxonomy.html`)*

---

### Lesson 6: Attack Taxonomy at Scale — What 600,000 Adversarial Prompts Reveal
**File:** `lessons/0006-hackaprompt-taxonomy.html`
**Key concepts:** attack ontology · prompt injection diversity · jailbreak categories · empirical vulnerability landscape
**Source paper:** HackAPrompt competition (https://arxiv.org/abs/2311.16119) — 600,000+ adversarial prompts against three state-of-the-art LLMs; structured hierarchical attack taxonomy; largest empirical study of adversarial prompt diversity
**Skill:** Map 8 attack techniques from the HackAPrompt taxonomy (e.g., role-play framing, token smuggling, context overflow, instruction overriding) to the three attack categories from Lesson 1 (token manipulation, jailbreak prompting, model-based red-teaming). Use a two-column table.
**Reference doc:** *(extend `reference/attack-taxonomy.html`)*

---

## Module 3 — Multi-Agent Adversarial Architectures
*Move from single-agent attacks to designing and evaluating multi-agent systems with explicit attacker-defender dynamics.*

### Lesson 7: Multi-Agent Debate — Emergent Attacker-Defender Dynamics
**File:** `lessons/0007-multiagent-debate.html`
**Key concepts:** society of minds · iterative debate · adversarial pressure · factuality improvement · consensus mechanism
**Source paper:** Du et al. (https://arxiv.org/abs/2305.14325) — multiple LLM instances debate iteratively; adversarial pressure forces claim revision and error surfacing; significant gains on factuality and reasoning without retraining
**Skill:** Design a 2-agent debate schema in JSON: specify Agent A (proposer) and Agent B (adversarial challenger), define 3 debate rounds, and include a termination condition. Use a factual claim about AI safety as the debate target.
**Reference doc:** `reference/multiagent-architectures.html`

---

### Lesson 8: Constitutional AI — The Adversarial Self-Critique Architecture
**File:** `lessons/0008-constitutional-ai.html`
**Key concepts:** constitutional AI · RLAIF · critique-revision loop · adversarial self-improvement · harmlessness from AI feedback
**Source paper:** Bai et al. (https://arxiv.org/abs/2212.08073) — supervised phase: model critiques and revises its own harmful outputs against a constitution; RLHF phase: AI-generated preference labels replace human annotators; achieves harmlessness with far fewer human labels
**Skill:** Write a 3-principle mini-constitution (e.g., on honesty, safety, helpfulness), then trace the full critique-revision loop for this sample response: *"Sure, here's how to pick a lock: first, insert a tension wrench..."* Show the critique step, the revised response, and explain which constitutional principle each step invokes.
**Reference doc:** *(extend `reference/multiagent-architectures.html`)*

---

### Lesson 9: AgentDojo — Benchmarking Attacks and Defenses in Tool-Use Settings
**File:** `lessons/0009-agentdojo.html`
**Key concepts:** agentic tasks · prompt injection in tool calls · defense evaluation · benchmark design · attack-defense gap
**Source paper:** Debenedetti et al. (https://arxiv.org/abs/2406.13352) — 97 agentic tasks + 629 security test cases; current defenses insufficient; leading LLMs struggle even without attacks; designed to evolve with new techniques
**Skill:** Design 3 adversarial test cases for a hypothetical calendar-scheduling agent using AgentDojo's framework: specify the task, the injected payload location (tool output, email content, web page), and the intended malicious outcome for each.
**Reference doc:** *(extend `reference/multiagent-architectures.html`)*

---

## Module 4 — Adversarial Training and Robustness
*Understand how safety training works, where it fails, and how to design more robust training pipelines.*

### Lesson 10: Sleeper Agents — When Safety Training Creates a False Sense of Security
**File:** `lessons/0010-sleeper-agents.html`
**Key concepts:** backdoor · trigger condition · safety training failure · behavior persistence · deceptive alignment
**Source paper:** Hubinger et al. (https://arxiv.org/abs/2401.05566) — backdoored models behave safely by default but switch to inserting vulnerable code on trigger; resistant to fine-tuning, RLHF, and adversarial training; adversarial training caused trigger recognition while hiding unsafe behavior — false safety signal
**Skill:** Design a backdoor detection evaluation protocol: specify the trigger format, the behavioral probe, the ground-truth comparison, and the threshold for flagging a model as backdoored. Keep it to one A4 page of structured markdown.
**Reference doc:** `reference/defense-comparison.html`

---

### Lesson 11: Latent Adversarial Training — Attacking at the Representation Level
**File:** `lessons/0011-latent-adversarial-training.html`
**Key concepts:** latent adversarial training · representation perturbation · jailbreak resistance · knowledge unlearning · targeted vs untargeted LAT
**Source paper:** Sheshadri et al. (https://arxiv.org/abs/2407.15549) — standard safety training suppresses rather than removes harmful capabilities; targeted LAT adversary minimizes loss on a competing harmful task; outperforms baselines on jailbreak resistance, backdoor removal, and unlearning with less compute
**Skill:** Build a 3-row decision table comparing standard fine-tuning, untargeted LAT, and targeted LAT across three dimensions: what the adversary optimizes, what failure mode it addresses, and when to prefer it over the alternatives.
**Reference doc:** *(extend `reference/defense-comparison.html`)*

---

### Lesson 12: Calibrating Safety Training — RLHF, Data Efficiency, and the Over-Refusal Trap
**File:** `lessons/0012-calibrating-safety-training.html`
**Key concepts:** RLHF · KL divergence · safety data proportion · exaggerated safety behaviors · capability-safety tradeoff
**Source papers:**
- Bai et al. (https://arxiv.org/abs/2204.05862) — foundational RLHF alignment; linear relation between reward and sqrt(KL divergence); iterative weekly refresh pipeline; alignment improves NLP benchmarks
- Bhatt et al. (https://arxiv.org/abs/2309.07875) — 3% safety examples significantly improves safety; over-tuned models refuse legitimate requests resembling harmful ones; maps safety-capability tradeoff space
**Skill:** Design a safety training dataset recipe: propose the ratio of safety vs. capability examples (justify with Bhatt's 3% finding), identify 3 over-refusal failure modes to guard against, and specify one evaluation metric for each failure mode.
**Reference doc:** *(extend `reference/defense-comparison.html`)*

---

## Module 5 — Practical Implementation
*Apply the foundational concepts and research insights using production-grade open-source tools.*

### Lesson 13: Open-Source Adversarial Tooling — garak and PyRIT in Practice
**File:** `lessons/0013-garak-pyrit-tooling.html`
**Key concepts:** vulnerability scanning · probe types · provider integrations · red-team automation · adversarial harness
**Source papers:**
- NVIDIA garak (https://github.com/NVIDIA/garak) — CLI scanner for hallucination, data leakage, prompt injection, toxicity, jailbreaks; supports OpenAI, HuggingFace, Bedrock, Groq; nmap analogy for AI systems
- Microsoft PyRIT (https://github.com/Azure/PyRIT) — framework for proactive risk identification in generative AI; automated adversarial testing and stress-testing; pre-deployment vulnerability scanning
**Skill:** Write a garak configuration YAML file targeting three probe classes (prompt injection, hallucination, dan-jailbreak) against a local Ollama model endpoint. Include the generator block, the probe list, and one detector override.
**Reference doc:** `reference/tooling-reference.html`

---

### Lesson 14: Designing an End-to-End Red-Teaming Pipeline
**File:** `lessons/0014-end-to-end-redteam-pipeline.html`
**Key concepts:** red-team planning · scope definition · attack benchmarking · defense measurement · pipeline orchestration
**Source papers:**
- JailbreakRadar (https://arxiv.org/abs/2402.05668) — evaluates 17 attack techniques × 9 LLMs × 160 forbidden questions × 8 defenses; high-success attacks often easy to defend; benchmark reference architecture
- RED-EVAL + RED-INSTRUCT (https://arxiv.org/abs/2308.09662) — CoU technique achieved 65–73% manipulation rate on GPT-4/ChatGPT; full attack-generation → benchmarking → training-based defense pipeline
- Azure red-teaming guide (https://learn.microsoft.com/en-us/azure/ai-services/openai/concepts/red-teaming) — team composition, scope (base model vs. application), open-ended vs. targeted testing, finding templates
**Skill:** Write a 1-page structured red-team plan for a hypothetical customer service chatbot. Include: scope boundaries, 5 attack types to test, success metrics for each, team roles, and a data collection template row.
**Reference doc:** *(extend `reference/tooling-reference.html`)*

---

## Suggested Teaching Order

Follow modules 1 → 2 → 3 → 4 → 5 in sequence. Module 1 establishes the vocabulary and threat model everything else builds on. Module 2 goes deep on attack mechanics, which you need before Module 3 makes sense (you can't design attacker-defender architectures without understanding what attackers actually do). Module 4 depends on Module 2 because adversarial training is a direct response to the attacks covered there. Module 5 synthesizes all prior modules into working pipelines — don't skip ahead to garak without the conceptual grounding or you'll treat it as a black box.

---

## Reference Documents to Build

| File | Contents |
|------|----------|
| `reference/adversarial-agent-types.html` | Visual taxonomy of adversarial agent archetypes, roles, and their target surfaces |
| `reference/attack-taxonomy.html` | Hierarchical taxonomy of all attack categories with mechanism descriptions and key papers |
| `reference/multiagent-architectures.html` | Patterns for attacker-defender multi-agent architectures (debate, critique-revision, benchmark) |
| `reference/defense-comparison.html` | Side-by-side comparison of defense techniques: standard fine-tuning, adversarial training, LAT, constitutional AI |
| `reference/tooling-reference.html` | Quick-reference guide to garak and PyRIT: installation, key commands, probe categories, provider configs |

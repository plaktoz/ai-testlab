# Resources

## Knowledge

- **"Building Effective Agents"** (Anthropic Engineering, Dec 2024) — https://www.anthropic.com/research/building-effective-agents
  The definitive practical reference for agent architectures. Covers the core taxonomy (workflows vs agents), all major patterns (prompt chaining, routing, parallelization, orchestrator-workers, evaluator-optimizer), and design principles. Start here. Return to it after each lesson.

- **"ReAct: Synergizing Reasoning and Acting in Language Models"** (Yao et al., 2022) — https://arxiv.org/abs/2210.03629
  The foundational paper that formalized the Thought/Action/Observation loop. Short and dense. Essential for understanding why interleaved reasoning and action outperforms action-only or reasoning-only approaches. Read after the first lesson.

- **"Cognitive Architectures for Language Agents" (CoALA)** (Sumers, Yao, Narasimhan, Griffiths, 2023) — https://arxiv.org/abs/2309.02427
  The most comprehensive survey paper on agent architectures. Introduces a unified framework: memory (in-context, external, episodic, semantic, procedural) × action space (storage, process, reasoning, environment) × decision cycle. Dense, worth reading slowly. Best reached after lessons 3–4.

- **Anthropic Claude Tool Use Documentation** — https://docs.anthropic.com/en/docs/build-with-claude/tool-use
  The authoritative reference for how tool calling works at the API level: defining tools, parallel tool calls, error handling, the request/response cycle. Essential when reasoning about the mechanics underlying any agent.

- **Model Context Protocol (MCP) Specification** — https://modelcontextprotocol.io/introduction
  The emerging standard for agent-tool communication. Understand this to see where agent architecture is heading at the protocol level — standardized tool servers, resources, prompts.

- **"Attention Is All You Need"** (Vaswani et al., 2017) — https://arxiv.org/abs/1706.03762
  Foundational transformer paper. Scope-limited here: only relevant if lessons touch on why LLMs have the properties that make agentic patterns work (context window as working memory, etc.).

## Wisdom (Communities)

- **Anthropic Discord** — https://discord.gg/anthropic
  Active community around Claude, MCP, and the Claude Agent SDK. Good for current-state questions and real-world implementation patterns.

- **r/MachineLearning** — https://reddit.com/r/MachineLearning
  High-signal for academic papers and architecture discussions. Noisy for production advice.

- **Latent Space podcast / newsletter** — https://www.latent.space
  High-quality long-form technical interviews with practitioners building agents. Good for wisdom-level patterns that don't appear in papers.

## Gaps

- No well-moderated, practitioner-focused community specifically for multi-agent system design (as of mid-2025)
- Limited peer-reviewed benchmarks for production-grade agentic reliability (AgentBench and similar are research-focused)

# Glossary — Building Great Skills

This glossary defines the domain model for skill construction, where a skill's purpose is to extract determinism from a stochastic system.

---

## Language Terms

**Predictability** — The root virtue: a skill should produce the same *process* every run, not necessarily identical output. "A brainstorming skill should *predictably* diverge."

**Model-Invoked** — A skill retaining its `description` field, making it discoverable by the agent autonomously while remaining human-accessible too. Pays a permanent **context load** in exchange.

**User-Invoked** — A skill with its description stripped — invisible to the agent, reachable only by a human typing its name. Costs zero context load but cannot be fired by other skills.

**Description** — The machine-readable trigger whose mere presence determines invocation axis: keep it → model-invoked; delete it → user-invoked.

**Context Pointer** — A reference in the agent's context naming out-of-context material plus retrieval conditions. Wording determines *when* the agent reaches, making weak wording a variance bug.

**Context Load** — Token and attention cost a model-invoked skill's always-loaded description imposes on the context window.

**Cognitive Load** — Cost imposed on the *human* by user-invoked skills — what they must remember. "Not a cost to minimise: it is the price of human agency."

**Granularity** — How finely skills are divided. More model-invoked skills spend context load; more user-invoked skills spend cognitive load.

**Router Skill** — A user-invoked skill naming your other user-invoked skills and when to reach for each — one thing to remember instead of many.

**Information Hierarchy** — Content ranked by immediacy of need, across three rungs: steps (in-file, primary), in-file reference (secondary), disclosed reference (behind a context pointer).

**Co-location** — Keeping related material together so reading one part brings neighbors along — the within-file companion to the information hierarchy.

**Branch** — A distinct invocation case causing different execution paths through a skill.

**Progressive Disclosure** — Moving reference behind context pointers to keep the top of the hierarchy legible. "Not primarily a token optimisation; it is how the information hierarchy is protected."

**Steps** — The ordered agent actions forming a skill's primary content tier when present. Every step ends on a completion criterion.

**Completion Criterion** — The done-condition for a unit of work, with two axes: *clarity* (resists premature completion) and *demand* (sets legwork depth).

**Post-Completion Steps** — Later steps visible to the agent that exert forward pull toward premature completion.

**Legwork** — Below-step-level work the agent does autonomously — reading files, exploring code — raised by strong leading words or exhaustive completion criteria.

**Reference** — On-demand material (definitions, facts, examples) secondary to steps when steps exist, or the entire content when they don't.

**External Reference** — Reference living outside the skill system entirely: no description, no steps, not invocable — shareable by any skill including user-invoked ones.

**Leading Word** — A compact pretrained concept (*Leitwort*) that encodes a behavioral principle in minimal tokens by recruiting existing model priors. Serves predictability in both execution and invocation.

**Single Source of Truth** — Each meaning in exactly one authoritative place. Duplication is its violation.

**Relevance** — Whether a line still bears on what the skill does. Distinct from no-op: relevance asks about task-bearing, not behavior-changing.

---

## Failure Modes

**Premature Completion** — Ending a step before genuine completion because attention slips toward being done. Requires steps to occur; resist first by sharpening the completion criterion.

**Duplication** — The same meaning given more than one home, costing maintenance tokens and inflating prominence past actual rank.

**Sediment** — Stale, irrelevant content accumulating because adding feels safe and removing feels risky — the default fate of any skill without pruning discipline.

**Sprawl** — A skill simply too long, independent of staleness or repetition. Cure: push reference behind pointers; split by branch or sequence.

**No-Op** — An instruction the model follows by default anyway. "The test: does a line change behaviour versus the default?" Model-relative, not reader-relative — settled by running the skill.

---
name: writing-great-skills
description: Reference for vocabulary and principles for creating predictable, well-written skills.
disable-model-invocation: true
---

A skill wrests determinism from a stochastic system. **Predictability** — consistent *process*, not identical output — is the root virtue.

See [GLOSSARY.md](./GLOSSARY.md) for domain model definitions.

---

## Invocation Types

**Model-invoked**: Keeps a description; agent can fire it autonomously. Costs *context load* (description sits in window every turn).

**User-invoked**: Set `disable-model-invocation: true`. Only you can call it. Zero context load, but costs *cognitive load* — you must remember it exists.

When user-invoked skills pile up past memory, a **router skill** centralizes them.

---

## Description Writing

- Front-load the skill's leading word
- One trigger per branch — synonyms for the same branch are duplication
- Cut identity already covered in the body

---

## Information Hierarchy (ranked by immediacy)

1. **In-skill step** — ordered actions with checkable completion criteria
2. **In-skill reference** — rules/definitions consulted on demand
3. **External reference** — pushed out via context pointer, loaded only when needed

**Progressive disclosure** moves material down the ladder to keep the top legible.

---

## Leading Words

Compact pretrained concepts (e.g., *tracer bullets*, *fog of war*) that anchor behavior in fewer tokens. They serve predictability twice: anchoring *execution* in the body and *invocation* in the description.

Example collapse: "fast, deterministic, low-overhead" → *tight*

---

## Failure Modes

| Mode | Cause | Fix |
|---|---|---|
| Premature completion | Vague criterion | Sharpen criterion first; split if irreducibly fuzzy |
| Duplication | Same meaning in multiple places | Single source of truth |
| Sediment | Stale layers accumulating | Active pruning discipline |
| Sprawl | Too long even with live content | Disclose reference behind pointers |
| No-op | Line model obeys by default | Stronger leading word or delete |

---

## Pruning Rule

Run the no-op test sentence by sentence. "Does it change behavior versus default?" If not — delete the whole sentence, don't trim it.

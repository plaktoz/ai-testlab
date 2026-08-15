# Pipeline State

<!-- This file is the shared blackboard for the multi-agent pipeline.
     Agents READ what came before them. Agents APPEND their output.
     Never overwrite or delete prior sections. -->

## Session Info

- Started: <!-- orchestrator fills this in -->
- Task: <!-- orchestrator fills this in -->
- Config: agent-config.yml

---

## Gate 0: Execution Plan

**Status:** `pending` <!-- orchestrator updates to: approved | rejected -->

**Classification:** <!-- feature | bug | refactor | design | deployment -->

**Roles Activated:** <!-- list -->

**Designer Activated:** <!-- yes | no -->

**Execution Sequence:**

<!-- Orchestrator writes the step-by-step plan here before Gate 0 is presented to the human. -->

---

## Gate 1: Spec & Acceptance Criteria

**Status:** `pending` <!-- analyst/orchestrator updates to: approved | rejected -->

**Analyst Output:**

<!-- Analyst writes the spec here after invoking their assigned skill. -->

**Acceptance Criteria:**

<!-- Analyst writes numbered acceptance criteria here. -->

---

## Gate 2: Design Approval

**Status:** `pending` <!-- only present when Designer is activated -->

**Designer Output:**

- Preview: `design-preview.html` (open in browser to review)
- Notes: <!-- Designer writes design decisions, component list, UX notes -->

---

## Feature & Task Breakdown

<!-- Architect writes this table after reading Gate 1 spec (and Gate 2 design if present). -->

| ID | Feature | Task | Dependencies | Status |
|---|---|---|---|---|
| F1.T1 | | | none | `open` |

**Legend:** Status values: `open` → `in_progress` → `closed` | `⛔ BLOCKED` = has unresolved dependencies

---

## Tests

<!-- Tester Phase 1 writes here BEFORE Coder writes any code. -->

**Unit Tests:**

```
<!-- test function names and what each tests -->
```

**Integration Tests:**

```
<!-- integration test names and what flows they cover -->
```

**Test File Locations:**
<!-- list of test file paths -->

---

## Code Artifacts

<!-- Coder writes source file locations here after implementation. -->

| File | Purpose | Task ID |
|---|---|---|
| | | |

---

## Test Results

<!-- Tester Phase 2 writes here after running tests against Coder's output. -->

**Gate 3 Status:** `pending` <!-- orchestrator updates to: approved | rejected -->

**Retry Count:** 0 / <!-- max_tester_retries from agent-config.yml -->

**Unit Tests:** <!-- X/Y passed -->

**Integration Tests:** <!-- X/Y passed -->

**Failures:**

```
<!-- test name, failure reason, line number -->
```

**Tester Recommendation:** <!-- deploy | do not deploy | escalate to human -->

---

## Deployment

<!-- Deployer writes here after Gate 3 approval. -->

**Status:** <!-- pending | complete | failed -->

**Deploy Log:**

```
<!-- shell output from deploy commands -->
```

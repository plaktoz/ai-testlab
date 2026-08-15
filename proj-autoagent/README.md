# Autonomous Multi-Agent Development Pipeline

A Claude Code project that turns a single AI session into a structured, multi-role development pipeline — with human approval gates at every critical step.

---

## What It Does

You describe a task. The **Orchestrator** (Claude Code, guided by `CLAUDE.md`) breaks it into a structured plan and routes it through specialized roles:

```
User Task → Orchestrator → Analyst → Designer* → Architect → Tester → Coder → Tester → Deployer
                                    (* only for UI tasks)
```

Human approval gates pause the pipeline at the spec, design, and test stages before any irreversible step proceeds.

---

## Prerequisites

- [Claude Code](https://claude.ai/code) CLI installed
- Python 3.9+ (for config validation)
- An Anthropic API key

---

## Setup

**1. Set your API key**

```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

**2. Install Python dependencies**

```bash
pip install pyyaml
```

**3. Validate the config**

```bash
python scripts/validate_config.py
```

You should see: `✓ agent-config.yml is valid`

**4. Open Claude Code in this directory**

```bash
claude
```

---

## First-Time Setup (New Project)

If no `pipeline-state.md` exists with prior work, run the project init wizard before giving any task:

```
/proj-init
```

This configures the `deploy` section of `agent-config.yml` for your specific project (container runtime, registry, target environment, etc.).

---

## Running the Pipeline

### Give a task

Just describe what you want built. Examples:

```
Add a user authentication flow with login and registration pages.
```

```
Fix the bug where the search results page crashes on empty queries.
```

```
Refactor the payment module to use the new Stripe SDK.
```

### Follow the gates

The Orchestrator will stop at each gate and ask for your explicit approval before continuing:

| Gate | What you review | Prompt |
|------|----------------|--------|
| **Gate 0** | Execution plan — roles, sequence, parallel tasks | `yes` to proceed |
| **Gate 1** | Spec and acceptance criteria from the Analyst | `yes` to proceed |
| **Gate 2** | UI mockup in `design-preview.html` *(UI tasks only)* | `yes` to proceed |
| **Gate 3** | Test results — unit + integration pass/fail counts | `yes` to deploy |

Type `yes` to advance, or describe what to change and the relevant role will revise and re-present.

---

## Key Files

| File | Purpose |
|------|---------|
| `agent-config.yml` | Central config — models, roles, tools, skills, deploy settings |
| `CLAUDE.md` | Orchestrator instructions (read by Claude Code on every session start) |
| `pipeline-state.md` | Shared blackboard — all agent outputs live here |
| `pipeline-log.md` | Append-only log of every agent action |
| `design-preview.html` | Generated UI mockup *(created when Designer is activated)* |
| `scripts/validate_config.py` | Validates `agent-config.yml` structure |

---

## Configuring `agent-config.yml`

### Change a role's model

```yaml
roles:
  coder:
    model: claude-opus-4-8   # upgrade to a more capable model
```

### Enable/disable parallel task execution

```yaml
pipeline:
  parallel_execution: true   # independent tasks run concurrently
```

### Set the TDD retry limit

```yaml
pipeline:
  max_tester_retries: 3   # Orchestrator escalates to you after 3 Coder failures
```

### Configure deployment

```yaml
deploy:
  container_runtime: docker       # docker | podman | none
  registry: ghcr.io               # docker.io | ghcr.io | local | none
  target_environment: staging     # local | staging | production
  build_tool: dockerfile          # dockerfile | compose | none
  pre_deploy_checks:
    - tests
    - lint
```

After editing, always re-run:

```bash
python scripts/validate_config.py
```

---

## How the TDD Loop Works

Tests are written **before** any code is written:

1. **Tester Phase 1** reads the spec and writes unit + integration tests
2. **Coder** reads the tests and writes code to make them pass
3. **Tester Phase 2** runs all tests and reports results
4. On failure, the failure report goes back to Coder (retry counter increments)
5. If retries hit `max_tester_retries`, the Orchestrator escalates to you

---

## Resuming an Interrupted Session

If you close Claude Code mid-pipeline, reopen it in the same directory:

```bash
claude
```

The Orchestrator reads `pipeline-state.md` on startup and announces:

```
Resuming pipeline from: [last completed step]
```

It will offer to continue from where it left off.

---

## Roles Reference

| Role | Model (default) | Activated for |
|------|----------------|---------------|
| Orchestrator | claude-opus-4-8 | Every task |
| Analyst | claude-sonnet-5 | Every task |
| Designer | claude-sonnet-5 | UI/UX tasks only |
| Architect | claude-opus-4-8 | Features, refactors |
| Coder | claude-sonnet-5 | Every task with code |
| Tester | claude-haiku-4-5 | Every task with code |
| Deployer | claude-haiku-4-5 | After Gate 3 approval |

Models can be changed per-role in `agent-config.yml`.

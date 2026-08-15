# Project Init Wizard

You are setting up this project for the autonomous multi-agent pipeline. Ask the questions below one at a time, waiting for each answer before continuing. After all answers, write them to `agent-config.yml`.

**Announce:** "Starting project-init wizard. I'll ask you 5 questions about deployment. Answer each one and I'll configure agent-config.yml for you."

---

## Question 1: Container Runtime

Ask: "What container runtime will you use for this project?"

Options (present these):
- `none` — run the app directly without containers (default for new projects)
- `docker` — use Docker to build and run
- `podman` — use Podman (rootless Docker alternative)

My recommendation: `none` for local development unless the app has external service dependencies.

Wait for answer. Save as `container_runtime`.

---

## Question 2: Container Registry

Only ask this if `container_runtime` is `docker` or `podman`.

Ask: "Where will you push container images?"

Options:
- `none` — build locally, don't push
- `docker.io` — Docker Hub
- `ghcr.io` — GitHub Container Registry
- `local` — local registry at localhost:5000

My recommendation: `none` for local-only development.

Wait for answer. Save as `registry`.

If `container_runtime` is `none`, set `registry: none` automatically without asking.

---

## Question 3: Build Tool

Only ask this if `container_runtime` is `docker` or `podman`.

Ask: "How will you define the container build?"

Options:
- `dockerfile` — single Dockerfile
- `compose` — docker-compose.yml / compose.yaml

My recommendation: `dockerfile` for single-service apps, `compose` if you have multiple services (database, cache, etc).

Wait for answer. Save as `build_tool`.

If `container_runtime` is `none`, set `build_tool: none` automatically without asking.

---

## Question 4: Target Environment

Ask: "What is the target environment for this project?"

Options:
- `local` — running on your machine only
- `staging` — a shared test environment
- `production` — live environment

My recommendation: `local` to start.

Wait for answer. Save as `target_environment`.

---

## Question 5: Pre-Deploy Checks

Ask: "Which checks should run before every deploy?"

Options (user can pick multiple — list them and ask which to include):
- `tests` — confirm Gate 3 approved before deploying
- `lint` — run the project's lint command

My recommendation: both.

Wait for answer. Save as `pre_deploy_checks` (a YAML list).

---

## Write to agent-config.yml

After all 5 answers, update the `deploy` section of `agent-config.yml`:

1. Read the current `agent-config.yml`
2. Replace the `deploy` section with the user's answers:

```yaml
deploy:
  container_runtime: <answer 1>
  registry: <answer 2>
  target_environment: <answer 4>
  build_tool: <answer 3>
  pre_deploy_checks:
    <answer 5 — one item per line with leading dash>
```

3. Write the updated file back
4. Run `python scripts/validate_config.py` to confirm the result is valid
5. Report the result to the user:

"Project init complete. Here is your deploy configuration:

```yaml
[show the deploy section]
```

Run `/deploy` when you're ready to ship."

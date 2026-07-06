# MISSION.md Format

A `MISSION.md` file at the workspace root captures *why* the user is learning a topic, guiding all teaching decisions.

## Template

```markdown
## Why

<concrete real-world goal, 1-3 sentences — avoid "to understand X", focus on outcomes>

## Success looks like

- <observable, specific thing the user will be able to do>
- <another observable outcome>

## Constraints

- <time, budget, preferences, or anything limiting the approach>

## Out of scope

- <adjacent topics deliberately excluded>
```

## Rules

- One mission per workspace. Multiple unrelated goals = multiple workspaces.
- Concrete over abstract: "Ship a Rust CLI to my team" beats "learn Rust."
- Interview the user before writing if the goal is unclear.
- Update when goals shift — a stale mission causes drift.
- Keep it brief: if it exceeds one screen, it has stopped being a compass.

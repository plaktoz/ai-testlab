# Learning Record Format

Learning records live in `./learning-records/` using sequential naming like `0001-slug.md`. They function as teaching equivalents of ADRs.

## Template

```markdown
# <title: what was learned or established>

<1-3 sentences on what was learned and why it matters going forward>
```

## Optional Sections

- Status frontmatter (`active | superseded by LR-NNNN`)
- Evidence of demonstrated understanding
- Implications for future sessions

## When to Write One

1. User demonstrated genuine understanding (not just exposure)
2. User disclosed prior knowledge — including claimed depth
3. A misconception was corrected (high-value: predicts future stumbling blocks)
4. The learning mission itself shifted

## What Doesn't Qualify

- Material merely covered without evidence of learning
- Terms already defined in the glossary
- Session activity logs

## Supersession

When understanding deepens or corrects an earlier record, mark the old one `Status: superseded by LR-NNNN` rather than deleting it — the history of how understanding evolved is itself useful signal.

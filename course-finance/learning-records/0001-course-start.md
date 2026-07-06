---
title: Course Start — Finance for a 5-Year-Old
date: 2026-07-06
status: complete
---

## What was taught

The full Money School curriculum was generated in one session. All 7 lessons plus a course index and glossary are ready to use.

## Curriculum generated

| # | Lesson | Key skill |
|---|--------|-----------|
| 1 | What is Money? | Understand money as a medium of exchange |
| 2 | Coins and Bills | Identify coins by name and value |
| 3 | Earning Money | Connect work → reward |
| 4 | Needs vs Wants | Distinguish necessities from luxuries |
| 5 | Saving vs Spending | Apply the Golden Rule (save some always) |
| 6 | Saving for a Goal | Plan, persist, delay gratification |
| 7 | Spend · Save · Give | Operate the three-jar system |

## Learner profile

- Age: 5 years old
- Approach: visual, story-based, interactive, short sessions (10–15 min)
- Character guide: Zara the koala appears in every story section

## Key insights

- **Concrete-operational stage**: All abstractions are anchored to physical coins and visible animations. No abstract banking or credit concepts.
- **Narrative continuity**: Zara is used throughout as a consistent character so each lesson feels like the next chapter, not a standalone.
- **Declarative quiz engine**: `assets/quiz.js` reads `data-correct` (0-based index) from `.quiz-block` elements, making it trivial to add new questions.
- **Interactives per lesson**: flip cards (L2), chore picker (L3), sorting game (L4), piggy bank simulator (L5), goal tracker (L6), jar fill simulator (L7) — each using vanilla JS inline.

## Files created

```
assets/style.css          shared stylesheet + coin/bill visuals
assets/quiz.js            declarative quiz engine
lessons/index.html        course homepage (all 7 lessons listed)
lessons/0001-what-is-money.html
lessons/0002-coins-and-bills.html
lessons/0003-earning-money.html
lessons/0004-needs-vs-wants.html
lessons/0005-saving-vs-spending.html
lessons/0006-saving-for-a-goal.html
lessons/0007-spend-save-give.html
reference/glossary.html   18 finance terms with lesson cross-links
```

## Next steps

- Work through lessons 1–7 one per session with the child
- After each lesson, ask the real-life challenge question aloud
- Revisit the glossary when a term is forgotten
- Add a `learning-records/0002-*.md` after first lesson to track quiz scores

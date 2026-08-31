# High-Fidelity UI Design Specification v1.1 — English AI Coach

**Project:** English AI Coach  
**Document:** High-Fidelity UI Design Specification  
**Version:** 1.1  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-29

---

# 1. Final Visual Direction

```text
Clean
Friendly
Modern
Calm
Focused
Motivating
Trustworthy
```

Base theme:

```text
Light
Neutral background
White surfaces
Friendly blue/indigo primary
Green success
Amber warning
Red error
Blue info
```

---

# 2. Home Final Hierarchy

Home is a decision center, not a dashboard full of competing actions.

Order:

```text
Greeting
 ↓
Today's Plan
 ↓
Continue Learning  ← PRIMARY
 ↓
Review Summary
 ↓
New Words Summary
 ↓
Personalized Practice
 ↓
Streak
```

---

# 3. Home High-Fidelity Specification

Header:

```text
Good evening, Alex 👋                      Avatar
```

Today's Plan card:

```text
Today's learning

16 / 20 min
████████░░ 80%

[ Continue Learning ]
```

Review card:

```text
⚠ Review

18 planned today
4 high priority

[ Start Review ]
```

New word card:

```text
✦ New words

7 planned today
Recommended for your Travel goal

[ Learn New Words ]
```

Personalized Practice:

```text
✦ Personalized Practice

Based on your weak words

[ Practice Now ]
```

Streak:

```text
🔥 12 day streak
```

---

# 4. Home CTA Rules

Only:

```text
Continue Learning
```

is visually primary.

Secondary actions:

```text
Start Review
Learn New Words
Practice Now
```

They use secondary/card actions.

---

# 5. Daily Plan Visual Semantics

The Daily Plan card represents the current personalized workload.

Example:

```text
New     7
Review 18
Quiz    5
```

Optional:

```text
Estimated 20 min
```

Do not display raw algorithm data.

---

# 6. Review Visual Semantics

Review tab displays due workload.

Example:

```text
32 words currently due
```

Daily Plan may select only:

```text
18 for today's workload
```

Use different copy to prevent confusion.

---

# 7. Flashcard Final Design

Front:

```text
┌─────────────────────────────────┐
│ <                       4 / 18  │
│                                 │
│                                 │
│             abandon             │
│         /əˈbændən/              │
│                                 │
│              🔊                 │
│                                 │
│       [ Show meaning ]          │
│                                 │
└─────────────────────────────────┘
```

Back:

```text
┌─────────────────────────────────┐
│ abandon                         │
│ /əˈbændən/                  🔊   │
│                                 │
│ to leave completely             │
│ từ bỏ                           │
│                                 │
│ He abandoned the project.       │
│ Anh ấy đã từ bỏ dự án.          │
│                                 │
│ How difficult was this?         │
│                                 │
│ [Forgot] [Hard] [Difficult]     │
│ [Okay]  [Easy] [Very Easy]      │
└─────────────────────────────────┘
```

---

# 8. Answer Quality Visual Design

Six equal-weight choices are acceptable initially:

```text
Forgot
Hard
Difficult
Okay
Easy
Very Easy
```

Use:

```text
2-row layout
```

rather than tiny horizontal controls.

If usability test shows confusion, simplify copy while preserving the internal 0–5 mapping.

---

# 9. SRS Feedback UI

Success:

```text
Nice work!

Next review
in 17 days
```

Difficult:

```text
Let's review this one soon.

Next review
tomorrow
```

Never phrase scheduling as a memory guarantee.

---

# 10. Review High-Fidelity

```text
Review

32 words currently due

[All] [High Risk] [Weak]

┌───────────────────────────────┐
│ ⚠ High priority               │
│ negotiate                     │
│ Accuracy 42%                  │
│ Missed 3 times recently       │
│ [Practice]                    │
└───────────────────────────────┘
```

---

# 11. Personalized Exercise High-Fidelity

```text
✦ Practice for you

Based on your weak words

He decided to _____ the project.

[A. abandon]
[B. purchase]
[C. improve]
[D. organize]
```

Feedback:

Correct:

```text
✓ Correct
Nice work!
[Continue]
```

Incorrect:

```text
Not quite
Let's review this word once more.
[Continue]
```

---

# 12. Progress High-Fidelity

First viewport:

```text
320 Words Learned
140 Mastered

Accuracy
84.5%
█████████░

Learning Time
21h
```

Secondary:

```text
Accuracy trend
Weak Words
Learning History
```

---

# 13. Weak Words High-Fidelity

```text
negotiate
Accuracy 42%

High weakness

You've missed it 3 times recently.

[Practice]
```

Reason is more important than raw score alone.

---

# 14. Navigation High-Fidelity

Bottom navigation:

```text
Home
Learn
Review
Progress
Profile
```

Selected:

```text
Icon
Label
Soft primary container
```

Unselected:

```text
Muted icon
Muted label
```

---

# 15. Loading

Home:

```text
Header skeleton
Progress skeleton
Card skeleton
Card skeleton
```

Avoid blank white screens.

---

# 16. Empty

Review:

```text
🎉

You're all caught up!

No words need review right now.
```

---

# 17. Error

```text
Couldn't load today's learning plan.

[Retry]
```

---

# 18. Offline — Official V1

```text
No internet connection

Please reconnect to continue learning.

[Retry]
```

Do not show:

```text
Syncing...
Waiting to sync...
Your progress will sync later...
```

because V1 does not implement offline learning synchronization.

---

# 19. Connectivity Visual Rules

Online:

```text
Normal learning UI
```

Offline while attempting learning:

```text
Block mutation
Show Offline State
```

Read-only cached vocabulary may remain accessible.

---

# 20. AI Visual Rules

Use:

```text
✦
Recommended for you
Personalized
Based on your practice
High priority
```

Do not use:

```text
LLM
ML confidence
Model score
Inference
Prompt
```

on learner screens.

---

# 21. Motion

Button press:

```text
100–150ms
```

Flashcard flip:

```text
250–350ms
```

Progress:

```text
300–500ms
```

Navigation:

```text
150–250ms
```

Motion remains subtle.

---

# 22. Accessibility

```text
Touch target ≥ 44–48dp
Dynamic text where possible
High contrast
Screen-reader labels
Content descriptions
Focus order
Correctness not represented only by color
IPA supported
```

---

# 23. Responsive

```text
<360dp
→ compact spacing

360–480dp
→ standard

>480dp
→ centered content
```

Max content width on larger devices:

```text
560–640dp
```

---

# 24. Final High-Fidelity P0

```text
Home
Review
Flashcard
Personalized Exercise
Progress
Onboarding
Placement Test
Login
Profile
```

---

# 25. High-Fidelity Acceptance Criteria

```text
[ ] Home has one visually dominant CTA.
[ ] Home communicates today's workload.
[ ] Review count clearly differs from Daily Plan count.
[ ] New Words quantity comes from Daily Plan.
[ ] Flashcard is uncluttered.
[ ] Answer Quality can be selected without ambiguity.
[ ] SRS feedback is understandable.
[ ] Personalized Exercise visibly communicates why it exists.
[ ] Progress is scannable.
[ ] Offline UI does not promise sync.
[ ] Error states provide recovery.
[ ] Accessibility requirements are reflected.
```

---

# 26. Final Handoff

```text
High-Fidelity UI
 ↓
Interactive Prototype
 ↓
Usability Test
 ↓
Freeze V1
 ↓
Android Java implementation
```

---

# Reconciled V1 Contract Binding

UI behavior must bind to Android Technical Specification v1.1 and API/OpenAPI v1.4. Daily Plan is a stable persisted snapshot (including aggregate QUIZ item); assessment/SRS/personalization/gamification are backend authoritative; V1 is online-first; notification uses FCM/device preferences; all learner-facing copy is Vietnamese.

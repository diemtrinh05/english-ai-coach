# English AI Coach — Approved Baseline Manifest

**Status:** APPROVED BASELINE  
**Reconciled:** 2026-08-31  
**Decision set:** BR-001..BR-024 integrated

## Canonical source-of-truth chain

1. `docs/requirements/English_AI_Coach_SRS_v1.2.md`
2. `docs/database/English_AI_Coach_Database_Schema_v1.6.md`
3. `docs/architecture/English_AI_Coach_System_Architecture_v1.3.md`
4. `docs/ai/English_AI_Coach_AI_Personalization_Specification_v1.3.md`
5. `docs/api/English_AI_Coach_API_Specification_v1.4.md`
6. `docs/api/English_AI_Coach_OpenAPI_Swagger_v1_4.md`
7. `docs/technical/English_AI_Coach_Technical_Specification_v1.2.md`
8. `docs/technical/English_AI_Coach_Backend_Technical_Specification_v1.3.md`
9. `docs/mobile/English_AI_Coach_Android_Java_Technical_Specification_v1_1.md`
10. `docs/flutter/English_AI_Coach_Flutter_Technical_Specification_v1.1.md` (future V2 contract alignment)
11. `docs/admin/English_AI_Coach_Admin_Web_Technical_Specification_v1.1.md`
12. UI/UX, design, high-fidelity and prototype documents bind to the above contracts.

`docs/reconciliation/` is historical decision provenance, not an active override.

## Locked implementation invariants

- answerQuality 0..5; backend derives correctness; DB enforces `is_correct = (answer_quality >= 3)`.
- Optimistic lock code: `CONCURRENT_UPDATE`.
- Idempotency reuse code: `IDEMPOTENCY_KEY_REUSE`; body eventId only; 30-day retention.
- Daily workload increase guard +20%; decrease guard -30%.
- 5 failed logins → ~5 minute temporary lock.
- V1 online-first, no offline learning synchronization.
- Reusable AI requires review; personalized real-time exercise does not require per-result review.
- Admin = React + TypeScript + Vite.
- AI Personalization V1 is deterministic/rule-based.

## Gate state

Documentation reconciliation is complete. Run `python tools/baseline_audit.py` before implementation/merge. Architecture, Database, Security and QA reviewer passes remain required by the project workflow for code changes.

#!/usr/bin/env python3
from pathlib import Path
import re, sys, yaml
ROOT=Path(__file__).resolve().parents[1]
issues=[]

def fail(code,detail): issues.append((code,detail))

# Required files
required=[
'docs/requirements/English_AI_Coach_SRS_v1.2.md',
'docs/database/English_AI_Coach_Database_Schema_v1.6.md',
'docs/architecture/English_AI_Coach_System_Architecture_v1.3.md',
'docs/ai/English_AI_Coach_AI_Personalization_Specification_v1.3.md',
'docs/api/English_AI_Coach_API_Specification_v1.4.md',
'docs/api/English_AI_Coach_OpenAPI_Swagger_v1_4.md',
'docs/technical/English_AI_Coach_Technical_Specification_v1.2.md',
'docs/technical/English_AI_Coach_Backend_Technical_Specification_v1.3.md',
'docs/mobile/English_AI_Coach_Android_Java_Technical_Specification_v1_1.md',
'docs/flutter/English_AI_Coach_Flutter_Technical_Specification_v1.1.md',
'docs/admin/English_AI_Coach_Admin_Web_Technical_Specification_v1.1.md']
for r in required:
    if not (ROOT/r).exists(): fail('MISSING_BASELINE',r)

# Governance guard hẹp cho Database / Flyway gate trong PR template.
pr_template_path=ROOT/'.github/PULL_REQUEST_TEMPLATE.md'
if not pr_template_path.exists():
    fail('MISSING_PR_TEMPLATE','.github/PULL_REQUEST_TEMPLATE.md')
else:
    pr_template=pr_template_path.read_text(encoding='utf-8')
    pr_database_markers={
        'PR_DB_REVIEWER_GATE':'Database Reviewer is mandatory when any Database / Flyway impact is declared.',
        'PR_SCHEMA_FLYWAY_DECLARATION':'Corresponding Flyway migration (required for every production schema change):',
        'PR_SCHEMA_FLYWAY_EXCEPTION':'Schema-impacting PR without a Flyway migration — explicit exception reason (Database Reviewer approval required):',
        'PR_DB_VALIDATION_EVIDENCE':'Database validation evidence (required for every database-impacting PR):',
    }
    for code,marker in pr_database_markers.items():
        if marker not in pr_template: fail(code,'missing from .github/PULL_REQUEST_TEMPLATE.md')

    # Governance guard hẹp cho Security Reviewer gate; kiểm tra theo ngữ nghĩa
    # trong đúng section để không khóa template vào một câu chữ tuyệt đối.
    security_section_match=re.search(r'^### Security\s*$([\s\S]*?)(?=^## Tests\s*$)',pr_template,re.M)
    reviewer_section_match=re.search(r'^## Required Reviewers\s*$([\s\S]*?)(?=^## Backward Compatibility\s*$)',pr_template,re.M)
    if not security_section_match:
        fail('PR_SECURITY_SECTION','missing Security section from .github/PULL_REQUEST_TEMPLATE.md')
    else:
        security_section=security_section_match.group(1)
        security_impact_options=[
            'Authentication / authorization',
            'Secrets / credentials',
            'Idempotency / concurrency',
            'AI / external provider',
            'Push token / personal data',
        ]
        for option in security_impact_options:
            if option not in security_section:
                fail('PR_SECURITY_IMPACT_OPTION',f'missing security impact option: {option}')
        if not re.search(r'No security-sensitive change[^\n]*mutually exclusive',security_section,re.I):
            fail('PR_SECURITY_NO_IMPACT_EXCLUSIVE','No security-sensitive change must be mutually exclusive with security impact options')
        if not re.search(r'any security-sensitive impact[\s\S]{0,160}Security Reviewer approval is required',security_section,re.I):
            fail('PR_SECURITY_REVIEWER_GATE','security-sensitive impact must require Security Reviewer approval')
    if not reviewer_section_match:
        fail('PR_REQUIRED_REVIEWERS_SECTION','missing Required Reviewers section from .github/PULL_REQUEST_TEMPLATE.md')
    else:
        reviewer_section=reviewer_section_match.group(1)
        if not re.search(r'Security Reviewer[^\n]{0,120}mandatory[^\n]{0,120}security-sensitive impact',reviewer_section,re.I):
            fail('PR_SECURITY_REVIEWER_MANDATORY','Required Reviewers must make Security Reviewer mandatory for security-sensitive impact')
        if not re.search(r'generic omitted-reviewer[^\n]{0,240}(?:does not permit|cannot)[^\n]{0,160}Security Reviewer omission[^\n]{0,160}security-sensitive impact',reviewer_section,re.I):
            fail('PR_SECURITY_OMISSION_BYPASS','generic omitted-reviewer explanation must not permit Security Reviewer omission')

expected_headers={
'docs/architecture/English_AI_Coach_System_Architecture_v1.3.md':'# System Architecture v1.3 — English AI Coach',
'docs/ai/English_AI_Coach_AI_Personalization_Specification_v1.3.md':'# AI Personalization Specification v1.3 — English AI Coach',
'docs/api/English_AI_Coach_API_Specification_v1.4.md':'# API Specification v1.4 — English AI Coach',
'docs/api/English_AI_Coach_OpenAPI_Swagger_v1_4.md':'# OpenAPI / Swagger YAML v1.4 — English AI Coach',
'docs/technical/English_AI_Coach_Technical_Specification_v1.2.md':'# Technical Specification v1.2 — English AI Coach',
'docs/technical/English_AI_Coach_Backend_Technical_Specification_v1.3.md':'# Backend Technical Specification v1.3 — English AI Coach',
'docs/mobile/English_AI_Coach_Android_Java_Technical_Specification_v1_1.md':'# Android Java Technical Specification v1.1 — English AI Coach',
'docs/flutter/English_AI_Coach_Flutter_Technical_Specification_v1.1.md':'# Flutter Technical Specification v1.1 — English AI Coach',
'docs/admin/English_AI_Coach_Admin_Web_Technical_Specification_v1.1.md':'# Admin Web Technical Specification v1.1 — English AI Coach',
}
for rel,header in expected_headers.items():
    p=ROOT/rel
    if p.exists() and not p.read_text(encoding='utf-8').startswith(header): fail('VERSION_HEADER_MISMATCH',rel)

# General hygiene (exclude historical reconciliation)
for p in ROOT.rglob('*.md'):
    rel=p.relative_to(ROOT).as_posix()
    if rel.startswith('docs/reconciliation/'): continue
    s=p.read_text(encoding='utf-8')
    if 'filecite' in s: fail('FILECITE_ARTIFACT',rel)
    if '/api/v1/admin/ai-usage' in s: fail('STALE_AI_USAGE_ROUTE',rel)
    if re.search(r'POST\s+/personalized-exercise\b',s): fail('STALE_PERSONALIZED_ROUTE',rel)
    if 'Idempotency-Key' in s: fail('IDEMPOTENCY_HEADER_ACTIVE',rel)
    if 'Admin Web: Java Spring Boot' in s: fail('STALE_ADMIN_STACK',rel)
    stale_refs=[
      'OpenAPI / Swagger           v1.3', 'OpenAPI / Swagger v1.3',
      'Database Schema             v1.5', 'Database Schema v1.5',
      'System Architecture         v1.2', 'System Architecture v1.2',
      'AI Personalization          v1.2', 'AI Personalization Specification v1.1',
      'API Specification           v1.3', 'API Specification v1.3',
      'Technical Specification     v1.1', 'Technical Specification v1.0',
      'Backend Technical Spec      v1.2', 'Android Java Technical Spec v1.0',
      'English_AI_Coach_Database_Schema_v1.1.md',
      'Android Technical Specification v1.2', 'Android Java Technical Specification v1.2',
      'Flutter Technical Specification v1.2', 'Admin Web Technical Specification v1.2'
    ]
    for token in stale_refs:
        if token in s: fail('STALE_BASELINE_REFERENCE',f'{rel}: {token}')
    status=re.search(r'^\*\*Status:\*\*\s*(.+?)\s*$',s,re.M)
    if status and not status.group(1).startswith('APPROVED BASELINE'):
        fail('NONCANONICAL_STATUS',f'{rel}: {status.group(1)}')
    # duplicate numbered H1
    nums=[]
    for line in s.splitlines():
        m=re.match(r'^#\s+(\d+)\.\s+',line)
        if m: nums.append(int(m.group(1)))
    if len(nums)!=len(set(nums)): fail('DUPLICATE_H1_NUMBER',rel)

# Ngăn hướng dẫn idempotency active quay lại flow bắt exception do race.
# Reconciliation đã ARCHIVED — INTEGRATED được bỏ qua để giữ provenance lịch sử.
forbidden_idempotency_guidance=[
    re.compile(r'catch/inspect (?:database |duplicate-key )?conflict',re.I),
    re.compile(r'catch unique violation',re.I),
    re.compile(r'DataIntegrityViolationException'),
    re.compile(r'application must handle the database duplicate-key conflict',re.I),
    re.compile(r'duplicate-key race is an expected control-flow case',re.I),
    re.compile(r'use (?:a )?unique-constraint exception as (?:the )?normal duplicate-claim control flow',re.I),
    re.compile(r'catch a duplicate-key exception and continue work',re.I),
]
negated_guidance=re.compile(
    r'(?:do\s+(?:\*\*)?not|must\s+not|không(?:\s+còn)?|no\s+longer|removed|prohibited|forbidden|reject)',
    re.I,
)
for p in ROOT.rglob('*.md'):
    rel=p.relative_to(ROOT).as_posix()
    if rel.startswith('docs/reconciliation/'):
        continue
    s=p.read_text(encoding='utf-8')
    if re.search(r'^\*\*Status:\*\*\s*(?:ARCHIVED|SUPERSEDED)\b',s,re.M):
        continue
    for line_no,line in enumerate(s.splitlines(),start=1):
        for pattern in forbidden_idempotency_guidance:
            for match in pattern.finditer(line):
                # Cho phép câu cấm rõ ràng như "Do not catch ..." trong guidance canonical.
                if negated_guidance.search(line[:match.start()]):
                    continue
                fail('STALE_IDEMPOTENCY_EXCEPTION_FLOW',f'{rel}:{line_no}: {match.group(0)}')

# DB invariants
db=(ROOT/'docs/database/English_AI_Coach_Database_Schema_v1.6.md').read_text(encoding='utf-8')
for name in ['assessment_items','goal_topics','daily_plan_items','user_devices','notification_preferences']:
    if name not in db: fail('DB_MISSING_TABLE',name)
if '| answer_quality | SMALLINT | NOT NULL |' not in db: fail('DB_ANSWER_QUALITY_NULLABLE','session_attempts')
if 'CHECK(is_correct = (answer_quality >= 3))' not in db: fail('DB_CORRECTNESS_CHECK','missing')
if 'ON CONFLICT (event_id) DO NOTHING' not in db: fail('DB_IDEMPOTENCY_CLAIM','missing')
if '> **Tổng số bảng:** 34' not in db: fail('DB_TABLE_COUNT_METADATA','expected 34')
actual_tables=re.findall(r'^##\s+\d+\.\d+\.\s+([a-z][a-z0-9_]*)',db,re.M)
if len(actual_tables)!=34 or len(set(actual_tables))!=34:
    fail('DB_ACTUAL_TABLE_COUNT',f'expected 34 unique table definitions, got {len(actual_tables)} / {len(set(actual_tables))} unique')

# BR-001..BR-024 authoritative integration fingerprints.
texts={
 'rules':(ROOT/'docs/PROJECT_RULES.md').read_text(encoding='utf-8'),
 'srs':(ROOT/'docs/requirements/English_AI_Coach_SRS_v1.2.md').read_text(encoding='utf-8'),
 'db':db,
 'arch':(ROOT/'docs/architecture/English_AI_Coach_System_Architecture_v1.3.md').read_text(encoding='utf-8'),
 'ai':(ROOT/'docs/ai/English_AI_Coach_AI_Personalization_Specification_v1.3.md').read_text(encoding='utf-8'),
 'api':(ROOT/'docs/api/English_AI_Coach_API_Specification_v1.4.md').read_text(encoding='utf-8'),
 'backend':(ROOT/'docs/technical/English_AI_Coach_Backend_Technical_Specification_v1.3.md').read_text(encoding='utf-8'),
 'antigravity':(ROOT/'docs/agents/ANTIGRAVITY_FRONTEND_LEAD.md').read_text(encoding='utf-8'),
 'admin':(ROOT/'docs/admin/English_AI_Coach_Admin_Web_Technical_Specification_v1.1.md').read_text(encoding='utf-8'),
}
def require_fingerprint(br,key,needle):
    if needle not in texts[key]: fail('BR_INTEGRATION_GAP',f'{br} missing in {key}: {needle}')
require_fingerprint('BR-001','srs','**Admin Web:** React + TypeScript + Vite')
require_fingerprint('BR-003','rules','single-locale Vietnamese (`vi-VN`)')
require_fingerprint('BR-003','antigravity','Android `strings.xml`')
require_fingerprint('BR-004','ai','Constants: min 20, max 50, block size 4')
require_fingerprint('BR-005','db','## 6.3. assessment_items')
require_fingerprint('BR-006','ai','NEW first accepted attempt → LEARNING')
require_fingerprint('BR-007','ai','weakness_score = .40*error_rate + .20*normalized_response_time + .20*low_quality_rate + .20*recent_failure_rate')
require_fingerprint('BR-008','ai','risk_score = .45*time_pressure + .25*error_rate + .15*quality_penalty + .10*repetition_penalty + .05*response_penalty')
require_fingerprint('BR-009','ai','## Deterministic recommendation')
require_fingerprint('BR-010','db','## 5.3. goal_topics')
require_fingerprint('BR-011','ai','Raw change HIGH +10%, LOW -20%, otherwise 0%, with hard guard +20%/-30%')
require_fingerprint('BR-012','db','daily_plan_items ⭐')
require_fingerprint('BR-012','srs','Daily Plan is a persisted snapshot')
require_fingerprint('BR-013','srs','Daily Plan completed = 50 XP')
require_fingerprint('BR-014','db','user_devices ⭐')
require_fingerprint('BR-014','db','notification_preferences ⭐')
require_fingerprint('BR-016','backend','ON CONFLICT (event_id) DO NOTHING')
require_fingerprint('BR-017','api','V1 is synchronous and returns **201 Created**')
require_fingerprint('BR-018','admin','AI CEFR Suggestion is Future/V2')
require_fingerprint('BR-019','api','GET /api/v1/admin/statistics/ai-usage')
require_fingerprint('BR-020','api','POST /api/v1/learning/personalized-exercise')
require_fingerprint('BR-022','db','CREATE UPDATE ACTIVATE DEACTIVATE APPROVE REJECT PUBLISH UNPUBLISH LOCK_USER UNLOCK_USER')

# OpenAPI parse + contract
opfile=ROOT/'docs/api/English_AI_Coach_OpenAPI_Swagger_v1_4.md'
s=opfile.read_text(encoding='utf-8'); m=re.search(r'```yaml\n(.*?)\n```',s,re.S)
if not m: fail('OPENAPI_BLOCK','missing')
else:
    try: d=yaml.safe_load(m.group(1))
    except Exception as e: fail('OPENAPI_PARSE',str(e)); d=None
    if d:
        if d.get('openapi')!='3.1.0': fail('OPENAPI_VERSION',d.get('openapi'))
        if d.get('info',{}).get('version')!='1.4.0': fail('API_INFO_VERSION',d.get('info',{}).get('version'))
        if 'IdempotencyKeyOptional' in d.get('components',{}).get('parameters',{}): fail('OPENAPI_IDEMPOTENCY_HEADER','present')
        if 'AsyncJobResponse' in d.get('components',{}).get('schemas',{}): fail('OPENAPI_ASYNC_JOB','present')
        if '202' in d['paths']['/admin/ai-content/generate']['post']['responses']: fail('OPENAPI_AI_202','present')
        for path in ['/devices/{installationId}/push-token','/devices/{installationId}','/notification-preferences']:
            if path not in d['paths']: fail('OPENAPI_MISSING_PATH',path)
        expected={
          '/assessments':'StartAssessmentRequest',
          '/assessments/{assessmentId}/answers':'SubmitAssessmentAnswerRequest',
          '/learning/sessions':'StartLearningSessionRequest',
          '/learning/sessions/{sessionId}/complete':'EventIdRequest',
          '/learning/attempts':'SubmitLearningAttemptRequest',
          '/learning/today/complete':'EventIdRequest',
          '/quizzes/{quizId}/attempts':'StartQuizAttemptRequest',
          '/quiz-attempts/{attemptId}/answers':'SubmitQuizAnswerRequest',
          '/quiz-attempts/{attemptId}/complete':'CompleteQuizAttemptRequest',
          '/learning/personalized-exercise':'PersonalizedExerciseRequest',
          '/admin/ai-content/generate':'GenerateAiContentRequest'}
        schemas=d['components']['schemas']
        for path,name in expected.items():
            if 'eventId' not in schemas.get(name,{}).get('required',[]): fail('OPENAPI_EVENTID_REQUIRED',f'{path} -> {name}')
        dpi=schemas.get('DailyPlanItem',{})
        if 'vocabularyId' in dpi.get('required',[]): fail('OPENAPI_DAILYPLAN_VOCAB_REQUIRED','must be nullable/not required')
        if 'itemId' not in dpi.get('required',[]): fail('OPENAPI_DAILYPLAN_ITEM_ID','missing')
        # All internal refs must resolve and operationIds must be unique.
        refs=[]
        def walk(x):
            if isinstance(x,dict):
                for k,v in x.items():
                    if k=='$ref': refs.append(v)
                    else: walk(v)
            elif isinstance(x,list):
                for v in x: walk(v)
        walk(d)
        for ref in refs:
            if not ref.startswith('#/'): continue
            cur=d
            try:
                for part in ref[2:].split('/'):
                    part=part.replace('~1','/').replace('~0','~')
                    cur=cur[part]
            except Exception:
                fail('OPENAPI_BAD_REF',ref)
        operation_ids=[]
        for path,item in d.get('paths',{}).items():
            for method in ['get','post','put','patch','delete']:
                if method in item:
                    opid=item[method].get('operationId')
                    if opid: operation_ids.append(opid)
        if len(operation_ids)!=len(set(operation_ids)): fail('OPENAPI_DUPLICATE_OPERATION_ID','duplicate operationId')
        if len(d.get('paths',{}))!=72: fail('OPENAPI_PATH_COUNT',len(d.get('paths',{})))
        if len(operation_ids)!=76: fail('OPENAPI_OPERATION_COUNT',len(operation_ids))

# Exact Markdown document references must resolve to a file in the reconciled tree.
all_md_names={p.name for p in ROOT.rglob('*.md')}
for p in ROOT.rglob('*.md'):
    rel=p.relative_to(ROOT).as_posix()
    if rel.startswith('docs/reconciliation/'): continue
    text=p.read_text(encoding='utf-8')
    for match in re.finditer(r'((?:English_AI_Coach|PROJECT_RULES|AGENTS|REVIEW_TEMPLATE)[A-Za-z0-9_.-]*\.md)',text):
        name=match.group(1)
        if name not in all_md_names: fail('BROKEN_MD_REFERENCE',f'{rel}: {name}')

# API spec ↔ OpenAPI explicit operation parity
api_text=(ROOT/'docs/api/English_AI_Coach_API_Specification_v1.4.md').read_text(encoding='utf-8')
api_ops=set()
for mm in re.finditer(r'^(GET|POST|PUT|PATCH|DELETE)\s+(/api/v1/[^\s`]+)',api_text,re.M):
    method,path=mm.groups(); path=path.split('?')[0].rstrip('.,'); api_ops.add((method.lower(),path.removeprefix('/api/v1')))
if 'd' in locals() and d:
    openapi_ops={(method,path) for path,item in d['paths'].items() for method in ['get','post','put','patch','delete'] if method in item}
    if api_ops != openapi_ops:
        fail('API_OPENAPI_PARITY',f'missing_in_openapi={sorted(api_ops-openapi_ops)} extra_in_openapi={sorted(openapi_ops-api_ops)}')

if issues:
    print(f'BASELINE AUDIT: FAIL ({len(issues)} issues)')
    for c,d in issues: print(f'- {c}: {d}')
    sys.exit(1)
print('BASELINE AUDIT: PASS')
print('Canonical baseline files present; OpenAPI parses; BR contract checks passed.')

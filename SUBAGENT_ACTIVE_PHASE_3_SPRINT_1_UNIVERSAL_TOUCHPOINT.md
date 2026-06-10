# SUBAGENT ACTIVE DIRECTIVE
## Phase 3 Sprint 1 - Universal Profile UI & Touchpoints

### Execution Mode
Local implementation task

### Important
This is a **local coding task on the current machine**.

Do **not**:
- create a branch
- create a PR
- push anything

Work directly in the current workspace, keep scope tight, and report code/build status only.

---

## Mandatory Encoding Rule

You must follow:

- `D:\DoAn_MB1\ENCODING_PROTOCOL_SUBAGENT.md`

This rule is mandatory for this batch and all future batches.

Do not diagnose encoding breakage from terminal output alone.
Do not bulk-rewrite source files with deprecated UTF-8 fix scripts.
Do not claim encoding is fixed unless UTF-8 content was verified by script or byte-level check.

---

## Context

The product and architecture plan for Phase 3 has been formalized in:

- `docs/PHASE_3_DIGITAL_CLINIC_PLAN.md`

That document is now the source of truth for this implementation batch.

We are starting with **Sprint 1 only**:

`Universal Profile UI & Touchpoints`

Do not jump ahead into appointment engine, private consultation provisioning, or RBAC refactor in this batch.

---

## Product Goal

Implement the first working slice of the **Universal Touchpoint** concept:

> Any Doctor avatar rendered in the app must be clickable and route the user into a unified Doctor Profile screen.

In this batch, the goal is:

`Doctor Avatar -> Doctor Profile`

Only the touchpoint and doctor profile layer.  
Do **not** implement full booking workflow yet unless minimal scaffolding is required for navigation.

---

## Scope

### In scope
1. Doctor profile screen
2. Reusable avatar/touchpoint component or helper
3. Wiring doctor avatar click targets in existing social/community surfaces
4. Navigation route for doctor profile
5. Backend public doctor profile API if missing
6. Frontend data model + repository wiring for doctor profile retrieval
7. Basic loading/error states

### Out of scope
- appointment creation engine
- doctor workspace triage
- private consultation room
- admin monitor
- group moderation role redesign
- payment, ratings, advanced scheduling

---

## Strict Product Requirements

### 1. Universal Touchpoint behavior
At minimum, the following doctor-origin surfaces must become clickable if they render a doctor identity:

- community wiki article author row
- community/group-post author row where author is a doctor
- group-post comments where author is a doctor
- any existing doctor identity surface inside specialty/community UI if already present and easy to wire safely

Non-doctor users must **not** route to doctor profile.

### 2. Doctor Profile screen
Create a dedicated Doctor Profile screen with a clean medical-professional layout.

The screen must show, when data exists:
- full name
- specialty
- hospital/clinic name
- avatar
- verification/eKYC status badge
- short bio/about if available
- relevant community/clinic entry point if already available in repo

If some fields do not yet exist end-to-end, render gracefully with honest fallback text. Do not fake data.

### 3. CTA policy
You may include a clearly disabled or placeholder booking CTA only if the repo is not ready for booking flow yet.

If you do this:
- the CTA text must be honest
- no misleading “works now” illusion
- no fake success flow

Preferred behavior:
- show the button
- route nowhere yet or show a clear “coming soon” notice

### 4. Verified doctor treatment
Doctors should be visually distinguished with a professional verification marker.

Do not invent a noisy design. Reuse the existing verified pattern if one already exists in community/wiki UI.

---

## Technical Requirements

### 1. Backend API
Check whether a doctor public profile endpoint already exists.

If not, implement a minimal safe endpoint, for example:
- `GET /api/v1/doctors/{id}/profile`

The response should contain only public-facing fields needed by the UI.

Do not expose sensitive admin-only or internal verification artifacts.

### 2. Frontend architecture
Follow existing app patterns:
- existing navigation key system
- existing repository/api layers
- existing Compose screen structure

Do not introduce a parallel navigation architecture.

### 3. Touchpoint abstraction
If the same “doctor avatar click -> profile” logic is duplicated in multiple places, create a small reusable abstraction.

But keep it modest. Do not over-engineer.

### 4. Safe fallback behavior
If a surface only has author name and role but no author ID, do not fake navigation.

Either:
- leave it non-clickable, and report that limitation
- or wire it only where the doctor user ID is truly available

Honesty is more important than broad but broken coverage.

---

## Target Deliverables

### Backend
Potential touched files may include:
- doctor profile controller/service/dto files
- existing community/article response DTOs if doctor IDs are missing and needed for touchpoints

### Frontend
Potential touched files may include:
- navigation keys / navigation graph
- community wiki screen
- group-post screens/comments
- reusable avatar/touchpoint component
- doctor profile screen
- repository/api/model files

---

## UX Rules

### Must do
- profile feels like a real professional surface, not a placeholder card dump
- clickable doctor identity is visually understandable
- loading/error states are clean
- back navigation returns correctly to previous screen

### Must not do
- do not hijack all avatars universally
- do not make non-doctor users open doctor profile
- do not create fake booking success flows
- do not break current community/group-post/chat flows

---

## Verification Requirements

### Automated
#### Backend
```powershell
cd D:\DoAn_MB1\CareNest\backend
.\mvnw.cmd compile
```

#### Frontend
```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

### Code-level manual verification
You must explicitly verify:
- doctor author rows now route correctly where doctor IDs exist
- non-doctor rows do not route
- navigation back-stack remains sane
- no unrelated QA docs or coordination files were modified

---

## Reporting Format

When done, report back using exactly this structure:

### 1. Batch Status
- `Code-level complete`
- or `Blocked`

### 2. Files Changed
List all touched files

### 3. Backend Changes
- endpoint(s)
- dto/model changes
- any response enrichment needed for doctor touchpoints

### 4. Frontend Changes
- route added
- screen added
- touchpoints wired
- fallback behavior for missing doctor IDs

### 5. Coverage Achieved
List exactly which surfaces now support:
- doctor avatar -> doctor profile

And which surfaces still do not, with reason.

### 6. Remaining Gaps
Be honest about what Sprint 1 did not include.

### 7. Build Result
- backend compile result
- frontend assemble result

Do not add branch/PR notes. This task is local-only.

# SUBAGENT ACTIVE DIRECTIVE
## Phase 3 Planning & Documentation - Digital Clinic Ecosystem

### Mission Type
Documentation-only branch task

### Important Scope Lock
This task is **not** a runtime QA task and **not** a product implementation task.

You are only authorized to:
- write the Phase 3 architecture/plan markdown file
- create the docs branch
- commit that docs file
- push the docs branch
- prepare the PR

You are **not** authorized to:
- modify app code
- modify backend logic
- mix this task with current QA/fix batches
- change local coordination `.md` files unrelated to this task

---

## Source Directive

Use the following directive as authoritative:

> [TECH LEAD DIRECTIVE: PHASE 3 PLANNING & DOCUMENTATION - DIGITAL CLINIC ECOSYSTEM]
>
> [CONTEXT]
> We have finalized the architectural vision for Phase 3 of CareNest: The Digital Clinic Ecosystem.
> The core philosophy is the "Universal Touchpoint": Any Doctor's Avatar across the app (in the Social Feed, Comments, or Specialty Public Chats) must be clickable and route the user directly to a unified Doctor Profile (Phòng khám số).
> From this profile, users can initiate a Booking Request (ONLINE_CHAT or OFFLINE_CLINIC). The system follows a "Doctor-Controlled Request-Approve" model. Once an ONLINE_CHAT appointment is approved and active, the system provisions a strict 1-on-1 PRIVATE_CONSULTATION chat room.
>
> [MISSION]
> Act as a Technical Product Manager. Your task is to document this entire Phase 3 Master Plan into a comprehensive Markdown file, commit it to a new feature branch, and prepare it for a Pull Request into develop.
>
> [STRICT EXECUTION PROTOCOL (STEP-BY-STEP)]
>
> Step 1: Create the Documentation File
>
> Create a new file at: docs/PHASE_3_DIGITAL_CLINIC_PLAN.md
>
> The Markdown file MUST strictly contain the following sections:
>
> 1. Executive Summary: Explain the "Universal Touchpoint" concept (Avatar -> Profile -> Booking -> Consultation).
>
> 2. The Chat Room Typology: Document the 3 distinct types of chat to avoid data spaghetti:
>
> SPECIALTY_PUBLIC (Open community, linked to Specialty ID).
>
> DOCTOR_CLINIC (Semi-public QA room, linked to Doctor ID).
>
> PRIVATE_CONSULTATION (Strictly 1-on-1, encrypted, generated only from an active Appointment ID).
>
> 3. The Request-Approve Workflow: Document the state machine for Appointments (PENDING -> APPROVED/REJECTED -> ACTIVE -> COMPLETED) and the Doctor Workspace triage logic.
>
> 4. Governance & RBAC Architecture: Document the enterprise-grade separation of System Role vs Group Role, strict group creation workflow, admin monitor boundaries, audit-log obligations, freeze/soft-delete policy, and doctor lifecycle impact.
>
> 5. Sprint Execution Plan: Break down the development into 4 sprints:
>
> Sprint 1: Universal Profile UI & Touchpoints.
>
> Sprint 2: Appointment Engine & Database Schema.
>
> Sprint 3: Doctor Workspace (Request Management).
>
> Sprint 4: Private Consultation Room Initialization.
>
> Step 2: Git Execution & Branching
>
> Execute: git checkout -b docs/phase3-digital-clinic-architecture
>
> Execute: git add docs/PHASE_3_DIGITAL_CLINIC_PLAN.md
>
> Execute: git commit -m "docs: formalize Phase 3 digital clinic architecture and sprint plan"
>
> Execute: git push -u origin docs/phase3-digital-clinic-architecture
>
> Step 3: Pull Request Creation
>
> If you have GitHub CLI (gh) installed, execute: gh pr create --base develop --head docs/phase3-digital-clinic-architecture --title "Docs: Phase 3 Digital Clinic Architecture Plan" --body "This PR introduces the official product and architectural documentation for Phase 3, encompassing Universal Touchpoints, the Booking Engine, and Private Consultation logic. Please review."
>
> If you cannot use gh CLI, output the exact URL or manual steps for me to click and create the PR on GitHub.
>
> [OUTPUT REQUIREMENT]
> Acknowledge this context. Autonomously write the .md file with highly professional formatting, execute the branch switch, commit, and push. Report back ONLY when the push is successful and the PR is ready for my review.

---

## Additional Guardrails From Lead

### 1. Documentation quality bar
The output file must read like a serious product-architecture plan, not a casual note dump.

Required qualities:
- clear section hierarchy
- clean product language
- explicit system boundaries
- explicit data ownership by entity/room type/workflow state
- no vague filler

### 2. Do not under-document the flow
In `Executive Summary`, you must explain the end-to-end loop clearly:

`Doctor Avatar -> Doctor Profile -> Booking Request -> Doctor Approval -> Consultation Room`

Do not stop at "Doctor Profile".

### 3. Chat taxonomy must prevent future confusion
In `The Chat Room Typology`, you must explicitly explain:
- why each room type exists
- what primary key/domain object it is anchored to
- who can enter it
- what it is not for

This section must reduce future architectural spaghetti, not just list names.

### 4. Appointment workflow must be written as a real state machine
Do not merely list states.

You must explain:
- how a request is created
- who can move it forward
- what each transition means
- what condition creates a `PRIVATE_CONSULTATION`
- what closes or completes the flow

### 5. Sprint planning must be execution-friendly
Each sprint must include:
- objective
- key deliverables
- dependencies
- acceptance focus

Keep it concise but implementation-aware.

### 6. Governance & RBAC Architecture is now mandatory
The Phase 3 markdown file must contain a dedicated section named:

`Governance & RBAC Architecture`

This section must explicitly document the following architecture rules:

#### a. The Two-Question Rule
- `System Role` answers: `Who is this person in the system?`
- `Group Role` answers: `What can this person do in this specific group?`

Required values:
- `System Role`: `USER`, `DOCTOR`, `ADMIN`
- `Group Role`: `HOST`, `MODERATOR`, `MEMBER`

#### b. Group creation workflow with strict entity separation
- only `DOCTOR` accounts with active, non-expired eKYC may create requests
- request must declare target type:
  - `SPECIALTY_PUBLIC`
  - `DOCTOR_CLINIC`
- must explicitly document that:
  - `group_creation_requests` is a separate table
  - `chat_groups` contains only approved/active groups
- must explicitly state:
  - do **not** collapse both concepts into one table with status flags

#### c. Admin monitor boundaries
Must define:
- Group Moderation
- Chat Audit
- Role Override Tool

Must explicitly state:
- `PRIVATE_CONSULTATION` content is not readable by Admin
- public/semi-public content is only exposed for audit through user report or safety trigger

#### d. Audit log requirements
Must explicitly require a dedicated `audit_logs` table.

Minimum fields to document:
- `actor_user_id`
- `target_entity_type`
- `target_entity_id`
- `action_type`
- `reason`
- `metadata_json`
- `created_at`

Must clearly state:
- all override/freeze/mute/suspend/dissolve actions must be logged
- automated policy actions must also emit audit logs

#### e. Data retention and evidence preservation
Must explicitly document:
- no hard delete for violating groups
- use freeze / soft delete strategy
- examples:
  - `is_frozen`
  - `deleted_at`

#### f. Doctor lifecycle impact
Must explicitly document automated behavior when:
- eKYC expires
- doctor account is suspended

Expected system consequences:
- freeze owned `DOCTOR_CLINIC` groups
- remove or invalidate ownership where policy requires
- downgrade `HOST` / `MODERATOR` role in other groups according to policy
- emit audit log entries for these automated actions

#### g. Terminology rule
Must use production-grade terms only:
- `Mute Member`
- `Suspend Posting`
- `Restrict Messaging`

Do not use slang or jokey internal wording in the final doc.

### 7. Do not touch unrelated local docs
Do not modify any local coordination files like:
- `BATCH-*`
- `SUBAGENT_ACTIVE_*`
- roadmap files

Only create:
- `docs/PHASE_3_DIGITAL_CLINIC_PLAN.md`

and the git/PR artifacts required for this docs task.

### 8. Branch hygiene
This is a docs-only branch.

Before commit:
- ensure only the target docs file is staged
- do not stage unrelated dirty files in the worktree

### 9. PR body quality
If using `gh pr create`, keep the title exactly:

`Docs: Phase 3 Digital Clinic Architecture Plan`

And ensure the PR body communicates:
- this is the official Phase 3 plan
- it covers Universal Touchpoints
- it defines Booking Engine flow
- it defines Private Consultation logic

---

## Expected Deliverables

### Required file
- `docs/PHASE_3_DIGITAL_CLINIC_PLAN.md`

### Required git outcome
- branch: `docs/phase3-digital-clinic-architecture`
- commit: `docs: formalize Phase 3 digital clinic architecture and sprint plan`
- pushed to origin

### Required review outcome
- PR created against `develop`
- or exact manual PR URL/steps if `gh` is unavailable

---

## Final Reporting Format

When done, report back using exactly this structure:

### 1. Status
- `Push successful`
- or `Blocked`

### 2. File Created
- exact path

### 3. Branch
- branch name

### 4. Commit
- commit SHA
- commit message

### 5. Push Result
- success/failure

### 6. PR
- PR URL
- or exact manual creation URL/steps

### 7. Notes
- only if something was blocked or required fallback behavior

Do not add long narrative if everything succeeded.

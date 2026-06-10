# BATCH-02-QA-HARDENING

## Purpose
This batch exists to perform the **next strict QA / bug-fix discovery pass** for the Moderated Group Posts MVP after code-level completion.

This is **not** a feature-expansion batch.
This is **not** a redesign batch.
This is **not** a generic community sweep.

The only goal is to validate the real moderated-post flow in the app, identify actual product defects, and prepare a clean bug list for targeted fixes.

---

# Scope

Only test and report on these moderated-post flows:

1. `Cá»™ng Ä‘á»“ng -> Há»™i nhÃ³m -> Tháº£o luáº­n`
2. Public approved-post feed
3. Create post -> `PENDING_APPROVAL`
4. `BÃ i cá»§a tÃ´i`
5. Host moderation queue
6. Admin moderation queue
7. Approve flow
8. Reject flow with required reason
9. Moderator visibility rules
10. Separation between `Tháº£o luáº­n` and `VÃ o chat`

Do **not** expand into:
- OCR
- AI chat
- family chat
- broad community/chat redesign
- unrelated admin workspace
- polish-only edits not tied to a moderated-post defect

---

# Runtime Preconditions

Before executing this batch, confirm:

1. Backend is running
2. Database is running
3. Frontend debug build is installed on a real device or emulator
4. At least four usable identities exist, or the closest practical subset is documented:
   - `Member account`
   - `Host account`
   - `Admin account`
   - `Doctor but not HOST` account, if available
5. At least one target health group exists and is reachable from the app
6. The target group has a real `HOST`

If a precondition is missing, mark the affected cases as `Not Executed` and say exactly why.

---

# Critical PM Clarifications

These are locked for this batch:

1. `HOST` and `ADMIN` are the only moderators for this MVP
2. Non-host doctors must **not** see moderation controls
3. New posts must enter `PENDING_APPROVAL`
4. Public group feed must show only `APPROVED`
5. Rejection reason is mandatory
6. `Tháº£o luáº­n` and `VÃ o chat` must remain separate flows

Also:
- previously reported DEF-01 about reject reason being sent incorrectly from the frontend is **not a valid bug** against the current repo
- do not re-report that issue unless you have new runtime evidence that contradicts the current implementation

---

# Required Test Setup Declaration

You must explicitly report:

- Member account:
- Host account:
- Admin account:
- Non-host doctor account:
- Target group name:
- Target group id:
- Device / emulator:
- Build installed:
- Backend base URL:

Do not write vague phrases like `tested with available data`.

---

# Test Case Format

For every case below, report:

- `Status:` `Pass`, `Fail`, or `Not Executed`
- `Severity:` `P0`, `P1`, `P2`, `P3`, or `N/A`
- `Evidence:` what you actually observed
- `Notes:` one short paragraph

If a case fails, describe:
- what the user did
- what actually happened
- what should have happened

If a case is not executed, say exactly what blocked it.

---

# Required Test Cases

## GQ-01 - Entry path separation
**Goal:** Verify `Tháº£o luáº­n` opens moderated posts and `VÃ o chat` opens realtime chat.

### Steps
1. Open `Cá»™ng Ä‘á»“ng`
2. Open `Há»™i nhÃ³m`
3. Pick a visible group
4. Tap `Tháº£o luáº­n`
5. Return
6. Tap `VÃ o chat`

### Expected
- `Tháº£o luáº­n` opens the moderated post flow
- `VÃ o chat` opens realtime chat
- The two entry points are not ambiguous

---

## GQ-02 - Approved-only feed for member
**Goal:** Verify a normal member sees only approved posts in the public feed.

### Steps
1. Log in as member
2. Open `Tháº£o luáº­n`
3. Inspect `BÃ i viáº¿t`

### Expected
- Only approved posts are visible
- Pending/rejected posts from other users are not shown publicly

---

## GQ-03 - Create post as member
**Goal:** Verify creating a post sends it into moderation instead of publishing instantly.

### Steps
1. Log in as member
2. Open `Tháº£o luáº­n`
3. Create a new post
4. Submit it

### Expected
- Submission succeeds truthfully
- Post does not instantly appear in the public approved feed
- User receives clear pending-state feedback

---

## GQ-04 - My posts status visibility
**Goal:** Verify the author can see their own post status.

### Steps
1. After creating a post as member
2. Open `BÃ i cá»§a tÃ´i`

### Expected
- The new post is visible
- Status is shown as `Chá» duyá»‡t`

---

## GQ-05 - Member cannot moderate
**Goal:** Verify a normal member does not see moderation controls.

### Steps
1. Log in as member
2. Open `Tháº£o luáº­n`

### Expected
- No pending queue tab
- No approve/reject controls

---

## GQ-06 - Non-host doctor cannot moderate
**Goal:** Verify a doctor who is not the target group `HOST` does not see moderation controls.

### Steps
1. Log in as non-host doctor, if such an account exists
2. Open `Tháº£o luáº­n`

### Expected
- No pending queue tab
- No approve/reject controls

If no such account exists, mark `Not Executed`.

---

## GQ-07 - Host can moderate
**Goal:** Verify the target group `HOST` can see the moderation queue and actions.

### Steps
1. Log in as host
2. Open `Tháº£o luáº­n`

### Expected
- Pending queue is visible
- Approve/reject controls are present

---

## GQ-08 - Approve flow
**Goal:** Verify host approval moves a pending post into the public feed.

### Steps
1. Create a pending post as member
2. Log in as host
3. Open the pending queue
4. Approve the post
5. Return to the public feed
6. Optionally verify again from member account

### Expected
- Post leaves pending queue
- Post appears in approved feed
- Author-visible status updates truthfully

---

## GQ-09 - Reject flow with blank reason blocked
**Goal:** Verify blank rejection reason is blocked in the real flow.

### Steps
1. Create another pending post as member
2. Log in as host or admin
3. Open pending queue
4. Attempt reject with blank or whitespace-only reason

### Expected
- Reject is blocked
- User is told a reason is required

---

## GQ-10 - Reject flow with real reason
**Goal:** Verify rejection with a valid reason succeeds and is visible to the author.

### Steps
1. Use the same or another pending post
2. Reject with a real reason
3. Log back in as member
4. Open `BÃ i cá»§a tÃ´i`

### Expected
- Status becomes `Bá»‹ tá»« chá»‘i`
- Rejection reason is visible to the author

---

## GQ-11 - Admin moderation path
**Goal:** Verify admin has equivalent moderation powers.

### Steps
1. Log in as admin
2. Open the same group
3. Open moderation queue if pending items exist
4. Approve or reject a pending item

### Expected
- Admin can moderate successfully

---

## GQ-12 - Join-to-interact rule
**Goal:** Verify the app truthfully enforces the implemented group-interaction rule.

### Steps
1. Use an account not yet joined to the target group, if available
2. Open the group and attempt to interact with posting flow

### Expected
- Behavior matches the real implemented rule
- If joining is required, the app says so clearly

If not testable, mark `Not Executed`.

---

# Bug Classification Rules

Use these categories:

1. `Flow blocker`
2. `Role / permission defect`
3. `Data truth defect`
4. `UX / IA defect`
5. `Polish / wording defect`

Do not collapse everything into one list without categorization.

---

# Hard Rules

1. Do not claim runtime pass for a case you did not execute
2. Do not use build success as runtime evidence
3. Do not re-report DEF-01 unless new runtime evidence truly contradicts the current repo
4. Do not invent backend bugs from tooling display artifacts alone
5. Do not broaden into implementation work in this batch
6. Do not mark the feature complete if role-gating or moderation-state truth still fails at runtime

---

# Required Report Format

Return in this exact shape:

## 1. Batch Status
Choose one:
- `Pass with no confirmed blockers`
- `Pass with minor issues`
- `Failed with confirmed defects`
- `Blocked by environment`

## 2. Runtime Setup
- Member account:
- Host account:
- Admin account:
- Non-host doctor account:
- Target group:
- Device / emulator:
- Backend status:

## 3. Test Case Results
Report `GQ-01` through `GQ-12`

## 4. Confirmed Defects
List only real confirmed defects

## 5. Non-Defects / Corrected Assumptions
Explicitly list any previously suspected issue that turned out not to be a real bug

## 6. Remaining Gaps
Be honest and concrete

## 7. Final Recommendation
Choose one:
- `Ready for targeted bug-fix pass`
- `Ready for broader moderated-post QA`
- `Blocked by environment`

---

# Final Expectation

This batch should leave us with a **truthful runtime picture** of the Moderated Group Posts MVP:
- what actually works
- what actually fails
- what needs fixing next

No fluff. No optimistic wording. No scope drift.

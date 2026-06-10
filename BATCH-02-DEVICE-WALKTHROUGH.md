# BATCH-02-DEVICE-WALKTHROUGH

## Purpose
This batch exists to validate the **real in-app moderated group post experience** on a physical device or emulator.

The previous report gave us useful **API/runtime evidence**, but that is not the same as a clean user-facing walkthrough.

This batch is now focused on the **remaining unproven device cases** after partial host-side validation.

---

# Already Verified In-App

The following already have positive device evidence and should only be re-tested if a regression is suspected:

- Host sees the `Chờ duyệt` tab
- Host can approve a pending post
- Approved post disappears from the pending queue
- Approved post appears in the public feed
- Host can safely log out and return to the login screen

---

# Remaining Scope

Only validate these remaining flows:

1. Member-side `Bài của tôi` after host approval
2. Non-host doctor visibility restrictions in the app
3. Admin moderation path in the app
4. Rejection reason visibility in the app if a reject case is executed

Do **not** expand into:
- OCR
- AI chat
- family chat
- generic community polish
- backend redesign
- speculative fixes before a real UI/runtime defect is seen

---

# Preconditions

Before running this batch, confirm:

1. Backend is running
2. Database is running
3. Frontend debug build is installed on a physical device or emulator
4. The following accounts are available, or the missing ones are explicitly noted:
   - `Member account`
   - `Host account`
   - `Admin account`
   - `Non-host doctor account`
5. A target health group exists and is reachable in the app
6. At least one approved post and one pending post are available if the chosen cases need them

If any precondition is missing, mark the affected cases `Not Executed`.

---

# PM-Locked Truths

These remain non-negotiable:

1. `HOST` and `ADMIN` are the only moderators for this MVP
2. Non-host doctors must not see moderation UI
3. New posts must enter `PENDING_APPROVAL`
4. Public feed must show only `APPROVED`
5. Rejection reason is mandatory
6. `Thảo luận` and `Vào chat` must stay clearly separated

Also:
- DEF-01 from the previous report is **not** a real defect and must not be re-reported unless new contradictory runtime evidence appears
- DEF-02 is only a tooling/display note, not a product blocker

---

# Required Runtime Setup Declaration

You must report:

- Member account:
- Host account:
- Admin account:
- Non-host doctor account:
- Target group name:
- Target group id:
- Device / emulator:
- Build installed:
- Backend base URL:

Do not write `existing data` without naming the actual accounts/group used.

---

# Test Case Format

For every case below, report:

- `Status:` `Pass`, `Fail`, or `Not Executed`
- `Severity:` `P0`, `P1`, `P2`, `P3`, or `N/A`
- `Evidence:` what was actually observed in the app
- `Notes:` one short paragraph

If a case fails, include:
- what the user did
- what happened
- what should have happened

---

# Required Test Cases

## GD-05R - Member `Bài của tôi` after approval
**Goal:** Verify the member sees the approved status in the app after host approval.

### Steps
1. Log in as member
2. Open the target group post flow
3. Open `Bài của tôi`

### Expected
- The previously approved post is visible
- Its status is shown as approved in the app

---

## GD-07R - Non-host doctor cannot moderate
**Goal:** Verify a doctor without HOST role does not see moderation UI.

### Steps
1. Log in as non-host doctor
2. Open `Thảo luận`

### Expected
- No moderation queue tab
- No approve/reject controls
- Public post view remains visible if access is allowed

If unavailable, mark `Not Executed`.

---

## GD-11R - Admin moderation path
**Goal:** Verify admin can perform moderation successfully in the app.

### Steps
1. Log in as admin
2. Open the same group
3. Open moderation queue if pending items exist
4. Approve or reject a pending post

### Expected
- Admin can access the moderation queue
- Admin can complete moderation actions successfully

---

## GD-10R - Reject reason visibility
**Goal:** Verify a rejection reason is visible to the author in the app if a reject case is executed.

### Steps
1. Create or identify a post that gets rejected with a valid reason
2. Log in as the author
3. Open `Bài của tôi`

### Expected
- The rejected post is visible
- The rejection reason is visible in the app

If no reject case is executed, mark `Not Executed`.

---

# Bug Classification

Use these categories:

1. `Flow blocker`
2. `Role / permission defect`
3. `Data truth defect`
4. `UX / IA defect`
5. `Polish / wording defect`

---

# Hard Rules

1. Do not claim device/runtime success for a case you did not execute
2. Do not use API-only evidence as proof of UI behavior
3. Do not re-report DEF-01 unless new contradictory runtime evidence exists
4. Do not open coding scope in this batch
5. Do not mark the feature complete if role-gating or visible moderation-state truth still fails in the app

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
Report:
- `GD-05R`
- `GD-07R`
- `GD-11R`
- `GD-10R` if executed

## 4. Confirmed Defects
List only real confirmed defects

## 5. Non-Defects / Corrected Assumptions
List previously suspected issues that proved not to be real bugs

## 6. Remaining Gaps
Be honest and specific

## 7. Final Recommendation
Choose one:
- `Ready for targeted bug-fix pass`
- `Ready for broader moderated-post QA`
- `Blocked by environment`

---

# Final Expectation

This batch should answer one simple question truthfully:

**Does the remaining real app/device behavior for moderated group posts still hold for member, non-host doctor, and admin roles?**
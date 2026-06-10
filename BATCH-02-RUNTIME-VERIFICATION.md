# BATCH-02-RUNTIME-VERIFICATION

## Purpose
This batch exists to perform **strict runtime verification** for the Moderated Group Posts MVP.

This is **not** a coding batch.
This is **not** a wording/polish batch.
This is **not** a “looks good from code review” batch.

The only goal is to verify whether the real runtime behavior of the new moderated group-posting flow matches the PM-locked product requirements.

---

# Scope

Only test these flows:

1. `Cộng đồng -> Hội nhóm -> Thảo luận`
2. Group approved-post feed
3. Create post -> pending moderation
4. `Bài của tôi`
5. Moderator pending queue
6. Approve flow
7. Reject flow with required reason

Do **not** expand scope into:
- OCR
- AI chat
- family chat
- realtime community chat redesign
- admin workspace unrelated to group-post moderation
- broad UI polish

---

# Runtime Preconditions

Before executing any test case, confirm:

1. Backend is running
2. Database is running
3. Frontend debug build is installed on a real device or emulator
4. At least **three usable identities** exist:
   - `Member account`
   - `Host account`
   - `Admin account`
5. At least one health group exists and is reachable from the app
6. At least one group has a valid `HOST`

If these are not true, mark the affected cases as `Not Executed` and say exactly why.

---

# Test Data Requirements

You must explicitly state which account and group were used:

- Member account:
- Host account:
- Admin account:
- Target group name:
- Target group id:

Do not write “tested with existing data” without identifying the actual runtime setup.

---

# Test Case Format

For every test case below, report:

- `Status:` `Pass`, `Fail`, or `Not Executed`
- `Severity:` `P0`, `P1`, `P2`, `P3`, or `N/A`
- `Evidence:` what you actually observed
- `Notes:` one short paragraph

If a test is not executed, say exactly what blocked it.

---

# Test Cases

## GP-01 — Group post entry path
**Goal:** Verify that `Thảo luận` opens the moderated group-post flow, not realtime chat.

### Steps
1. Open `Cộng đồng`
2. Open `Hội nhóm`
3. Pick a visible group
4. Tap `Thảo luận`

### Expected
- Opens the moderated post detail flow
- Does **not** open the realtime chat room
- Group name/context is correct

---

## GP-02 — Realtime chat separation
**Goal:** Verify that `Vào chat` still opens the realtime chat flow and remains separated from moderated posts.

### Steps
1. On the same group card, tap `Vào chat`

### Expected
- Opens realtime chat
- Does **not** open moderated posts

---

## GP-03 — Approved-only public feed
**Goal:** Verify that a normal member sees only approved posts in the public group feed.

### Steps
1. Log in as member
2. Open `Thảo luận` for a group
3. Observe `Bài viết`

### Expected
- Only approved posts appear
- Pending/rejected posts from other users do not appear publicly

---

## GP-04 — Create post as member
**Goal:** Verify that creating a post sends it into moderation rather than making it immediately public.

### Steps
1. Log in as member
2. Open `Thảo luận`
3. Create a post
4. Submit the post

### Expected
- Success flow is shown truthfully
- The post is created as `PENDING_APPROVAL`
- It does **not** instantly appear in the public approved feed

---

## GP-05 — My posts status view
**Goal:** Verify that the author can see their own moderated post states.

### Steps
1. After creating a member post
2. Open `Bài của tôi`

### Expected
- Newly submitted post appears
- Status is shown as `Chờ duyệt`

---

## GP-06 — Moderator visibility: member account
**Goal:** Verify that a normal member cannot see pending moderation UI.

### Steps
1. Log in as member
2. Open `Thảo luận`

### Expected
- No moderation queue tab
- No approve/reject buttons

---

## GP-07 — Moderator visibility: non-host doctor account
**Goal:** Verify that a doctor who is **not** the group host does not see moderation UI.

### Steps
1. Log in as doctor account that is not `HOST` for the target group
2. Open `Thảo luận`

### Expected
- No moderation queue tab
- No approve/reject buttons

If you do not have such an account, mark `Not Executed`.

---

## GP-08 — Moderator visibility: host account
**Goal:** Verify that the group host can moderate.

### Steps
1. Log in as the host account for the target group
2. Open `Thảo luận`

### Expected
- `Chờ duyệt` / moderation queue is visible
- Approve/reject controls are present

---

## GP-09 — Approve flow
**Goal:** Verify that approving a pending post moves it into the public feed.

### Steps
1. Log in as host or admin
2. Open pending queue
3. Approve the member-created pending post
4. Return to public feed
5. Optionally verify again from member account

### Expected
- Post leaves pending queue
- Post appears in approved feed
- Member can observe updated status

---

## GP-10 — Reject flow with reason
**Goal:** Verify that rejecting a post requires a reason and updates author-visible state.

### Steps
1. Create another pending post as member
2. Log in as host/admin
3. Open pending queue
4. Try to reject with blank reason
5. Then reject with a real reason

### Expected
- Blank reason is blocked
- Real reason succeeds
- Post status becomes `Bị từ chối`
- Reason is visible to the author in `Bài của tôi`

---

## GP-11 — Admin moderation path
**Goal:** Verify that admin can perform the same moderation actions as host.

### Steps
1. Log in as admin
2. Open the same group
3. Open moderation queue if pending items exist
4. Approve or reject an item

### Expected
- Admin can moderate successfully

---

## GP-12 — Membership interaction rule
**Goal:** Verify that the user must join the group before interacting, if that is the enforced runtime rule.

### Steps
1. Use an account not yet joined to the target group, if available
2. Open the group and try to access posting flow

### Expected
- Behavior matches the implemented business rule truthfully
- If join is required, the app should say so clearly

If not testable, mark `Not Executed`.

---

# Severity Rules

Use these severity levels:

- `P0` = core flow blocked / crash / wrong data exposure
- `P1` = major behavior incorrect but workaround exists
- `P2` = secondary flow issue, not blocking MVP
- `P3` = polish issue only

Examples:
- pending post visible in public feed = `P0`
- member can see moderation controls = `P0`
- reject with blank reason still succeeds = `P0`
- wrong label/copy but flow still works = `P3`

---

# Hard Reporting Rules

You must not:
- claim `Pass` without actually running the flow
- use build success as runtime evidence
- hide missing accounts/test data behind vague wording
- call the batch complete if key cases are `Not Executed`
- collapse member/host/admin testing into one generic statement

If the environment is missing the right accounts or seeded group roles, say so directly.

---

# Final Report Format

Return your runtime QA result in this exact structure:

## 1. Runtime Verification Status
Choose one:
- `Pass with no blocking issues`
- `Pass with non-blocking issues`
- `Blocked by environment`
- `Fail`

## 2. Environment Summary
- Date:
- Tester:
- Device / Emulator:
- Android version:
- Backend running:
- Database running:
- Redis running:
- Target group:
- Accounts used:

## 3. Group Post Runtime Results
List results for:
- GP-01
- GP-02
- GP-03
- GP-04
- GP-05
- GP-06
- GP-07
- GP-08
- GP-09
- GP-10
- GP-11
- GP-12

## 4. Defects Found
List each defect with:
- severity
- title
- exact flow
- what happened
- expected behavior

## 5. Final Recommendation
Choose one:
- `Batch 02 is ready for targeted bug-fix pass`
- `Batch 02 is blocked and needs environment/data preparation`
- `Batch 02 has blocker defects and must not proceed`

---

# Final Reminder

This batch is for **real runtime truth**.

We are not trying to prove that the code is elegant.
We are trying to prove whether the moderated group posting feature actually behaves correctly for:
- member
- host
- admin

If that truth is incomplete, report it plainly.

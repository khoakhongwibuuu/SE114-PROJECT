# BATCH-02-MODERATED-GROUP-POSTS-MVP

## Batch Status
`Issued - Final completion pass required`

## Date
`2026-06-08`

## Owner
`Subagent`

## Product Direction
This batch introduces a new independent product capability:

**Moderated health-group posting**

Users can join a health group and submit posts, but new posts must be approved by a group host or an admin before they appear in the public group feed.

This is a **PM-locked direction**.
Do not redesign the feature.
Do not reinterpret it into something else.
Do not drift into unrelated modules.

---

# Current PM Verdict

The batch is **close and structurally correct**, but it is **not yet considered complete**.

What is already true in the repo:
- post lifecycle exists
- approved-only public feed exists
- author status view exists
- moderation queue exists
- navigation wiring into the real app exists
- backend rejection reason validation exists

What still must be finished before the batch can be considered code-level complete:
1. clean UTF-8 / mojibake in touched user-facing surfaces
2. clean product terminology so moderated posts are not described like chat messages
3. sanity-harden the real app flow and role-gating behavior
4. confirm backend rule integrity after the latest changes

This file now defines that final completion pass.

---

# Core Principle

**Runnable correct flow first.**

For this batch, prioritize:
1. working business flow
2. truthful state transitions
3. correct role gating
4. clean user-facing wording
5. buildable frontend/backend integration

Do **not** spend the batch on polish-first work, animation, deep redesign, or broad refactors.

---

# Implementation Rule

Use the existing `feature/community` module and surrounding backend/community structures.

Do **not** create a parallel feature stack unless absolutely blocked.

Prefer:
- extending existing models
- extending existing API contracts
- extending existing community screens/viewmodels/repositories

Do not invent a brand-new parallel architecture.

---

# PM-Locked Product Decisions

These decisions are final for this batch:

1. Every new group post enters `PENDING_APPROVAL`
2. Only `APPROVED` posts appear in the public group feed
3. Rejection requires a reason
4. Authors can see their own post statuses
5. Approvers are:
   - `HOST`
   - `ADMIN`
6. MVP should stay public-group friendly:
   - users can discover groups
   - users must join to interact
7. Editing/resubmission sophistication can wait
8. This batch is **not** runtime QA closure

Do **not** reintroduce `DOCTOR_MODERATOR` for this batch.

---

# MVP Flow That Must Exist Before This Batch Can Be Accepted

## User Flow
1. User opens `Cộng đồng`
2. User opens a group
3. User sees the public approved posts for that group
4. User joins the group if needed
5. User creates a post in that group
6. The submitted post does **not** appear immediately in the public feed
7. The user can open a `Bài của tôi` / equivalent status view and see:
   - `Chờ duyệt`
   - `Đã duyệt`
   - `Bị từ chối`
8. If rejected, the user can see the rejection reason

## Moderator Flow
1. A host or admin opens a pending queue
2. They can inspect pending group posts
3. They can:
   - approve
   - reject
4. Rejection requires a reason
5. After approval, the post appears in the public group feed

If these two flows are not runnable from the real app, the batch is not done.

---

# Final Completion Pass Requirements

## Step 1: Clean UTF-8 / Mojibake in All Touched Moderated-Post Surfaces

Sweep and fix user-facing text in every touched group-post/community surface, including at minimum:
- `SocialGroupsPane.kt`
- `CommunityScreen.kt`
- `CreateGroupPostScreen.kt`
- `GroupPostDetailScreen.kt`
- `GroupPostDetailViewModel.kt`
- `GroupPostPanes.kt`
- any touched backend controller/service messages returning user-facing Vietnamese text

Required:
- no mojibake
- no broken UTF-8
- no stray `\\uXXXX` escapes unless absolutely necessary
- all visible Vietnamese copy must read naturally

## Step 2: Finish Product Terminology Cleanup

This feature is **moderated group posting**, not realtime group chat.

Clean any remaining misleading naming or wording where it affects product meaning:
- user-facing messages must use:
  - `bài viết`
  - `chờ duyệt`
  - `đã duyệt`
  - `bị từ chối`
  - `lý do từ chối`
- do not leave misleading “tin nhắn vào nhóm” wording inside the moderated post flow
- internal legacy method names such as `sendPost(...)` are acceptable for this batch only if user-facing behavior is not misleading

## Step 3: Sanity-Harden the Real App Flow

Verify and correct the real route chain:
- `MainScreen`
- `CommunityScreen`
- `SocialGroupsPane`
- `NavigationKeys`
- `Navigation`

Required final behavior:
- `Thảo luận` opens the moderated group post flow
- `Vào chat` opens the realtime group chat flow
- the two flows remain strictly separated
- no ambiguous CTA behavior

## Step 4: Sanity-Harden Moderator Visibility

Keep the PM-locked rule:
- only `HOST` and `ADMIN` are moderators for this batch

Required:
- `PENDING` moderation UI must not appear for normal members
- it must not appear for non-host doctors
- if you keep using `preview().myRole`, the UI must not flash incorrect moderator tabs before role resolution completes

## Step 5: Final Backend Rule Integrity Pass

Re-confirm backend truth:
- every new group post starts as `PENDING_APPROVAL`
- approved feed returns only `APPROVED`
- rejection reason is mandatory server-side
- my-posts endpoint returns author-owned posts regardless of moderation status
- pending queue is restricted to `HOST` / `ADMIN`

Do not redesign the lifecycle. Only harden and correct.

---

# Explicitly Out of Scope

Do not drift into these:
- OCR
- AI chat
- family chat redesign
- community realtime chat redesign
- advanced analytics
- trusted contributor system
- polls/events/live features
- full editing/resubmission lifecycle if not needed for MVP
- heavy visual polish
- full runtime QA closure

---

# Execution Order

Follow this order:

1. Clean user-facing UTF-8/copy in the touched moderated-post surfaces
2. Sanity-check real route wiring
3. Sanity-check moderator gating behavior
4. Re-confirm backend lifecycle rules
5. Build frontend and backend

Do not start by redesigning screens.

---

# Acceptance Criteria

This batch is acceptable only if all of the following are true:

1. A member can submit a post inside a group
2. That post is stored as `PENDING_APPROVAL`
3. That post does not instantly appear in the public approved feed
4. The author can see the submitted post in a status view
5. A host or admin can approve the post
6. After approval, the post appears in the public group feed
7. A moderator can reject the post with a required reason
8. The author can see the rejected status and reason
9. `Thảo luận` opens the moderated-post flow
10. `Vào chat` opens the realtime chat flow
11. Frontend build succeeds
12. Backend build/compile succeeds
13. Touched user-facing moderated-post/community surfaces are clean UTF-8

---

# Non-Negotiable Constraints

1. Do not claim runtime closure in this batch
2. Do not over-report polish
3. Do not fake moderation with local-only state while backend truth disagrees
4. Do not expose pending posts in the public feed for ordinary members
5. Do not hide rejection reasons from authors
6. Do not create duplicate parallel group/post models unless blocked
7. Do not reintroduce non-host doctors as moderators for this MVP

---

# Required Report Format

Return your result in this exact structure:

## 1. Batch Status
Choose one:
- `Code-level complete`
- `Partially complete`
- `Blocked`

## 2. Files Changed
List every touched file

## 3. UTF-8 / Copy Cleanup
List exactly what text/copy problems were fixed

## 4. Flow Integrity Confirmation
Confirm:
- `Thảo luận` path
- `Vào chat` path
- separation between posts and realtime chat

## 5. Moderator Gating Confirmation
Explain exactly how `HOST` / `ADMIN` only visibility is enforced

## 6. Backend Rule Confirmation
Confirm:
- `PENDING_APPROVAL`
- approved-only public feed
- mandatory rejection reason

## 7. Remaining Gaps
Be honest and specific

## 8. Build Result
Include:
- frontend build result
- backend build result

---

# Build Commands

Frontend:
```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

Backend:
```powershell
cd D:\DoAn_MB1\CareNest\backend
.\mvnw.cmd compile
```

---

# Final Reminder

This pass is about **making the product flow truly ready for targeted QA**.

If the flow runs correctly but the UI is plain, that is acceptable.
If the UI looks polished but mojibake remains, moderator gating is misleading, or the routing is ambiguous, that is **not** acceptable.

# TECH LEAD DIRECTIVE: EPIC 2 - PHASE 6 - SOCIAL QA, STABILIZATION & PARITY PASS

## Context

Epic 2 has now reached the point where the main social feature stack exists in code:

- Phase 1: domain models and API contracts
- Phase 2: paging/data engine
- Phase 3: feed UI shell
- Phase 4: post detail and nested comments UI
- Phase 5: social interaction logic for likes, comments, and replies

This phase is not about building new major features.

This is the **stabilization, QA, and parity pass** for the Social Groups system.

The product intent that must guide this phase is:

- smooth infinite feed
- trustworthy doctor identity
- responsive like/comment/reply interactions
- nested comments that stay readable on mobile
- no UI leakage, no fake success, no silent failure

## Mission

Act as a Senior Android QA Stabilization Engineer and Social Feature Finisher.

Your task is to review the current Epic 2 social implementation and close the most important correctness, parity, and stability gaps before Epic 2 is considered complete.

Do not pivot into unrelated app areas.

Do not redesign the social UI from scratch.

## Strict Execution Protocol

### Step 1 - Audit the Current Social Surface

Review the current social feature end to end:

- `SocialFeedScreen.kt`
- `PostCard.kt`
- `PostDetailScreen.kt`
- `CommentItem.kt`
- `CommentInputBar.kt`
- `SocialFeedViewModel.kt`
- `PostDetailViewModel.kt`
- `SocialRepositoryImpl.kt`
- `SocialApi.kt`

Your first job is to identify the remaining real gaps in:

- runtime stability
- pagination correctness
- mutation consistency
- UI truthfulness
- UTF-8 cleanliness
- mobile readability for nested comments

### Step 2 - Stabilize Pagination and Refresh Behavior

Audit and harden:

- feed paging
- comments paging
- `refresh()` usage after like/comment/reply
- append error handling
- empty state correctness

Required outcomes:

- no obvious duplicate refresh loops
- no stale count/state mismatches after interactions
- no fragile timing/race patterns between mutation and paging refresh

### Step 3 - Doctor Identity & Trust Consistency

Verify doctor badge parity everywhere it should appear:

- feed post header
- post detail header
- comment items

Required outcomes:

- `AuthorRole.DOCTOR` renders consistently
- no screens lose the doctor badge in nested comment paths
- no fake badge rendering for non-doctor roles

### Step 4 - Nested Comment UX Pass

Audit reply-thread behavior on mobile:

- indentation
- reply target clarity
- comment input state
- reply dismissal behavior
- long-text readability

Required outcomes:

- replies remain readable at 1-2 levels deep
- no obvious visual breakage from indentation
- comment input does not become confusing when switching between normal comment and reply mode

### Step 5 - UTF-8 & String Truthfulness Sweep

This is mandatory.

Clean any remaining mojibake or misleading copy in social scope.

Targets include at minimum:

- `SocialFeedScreen.kt`
- `PostDetailScreen.kt`
- `PostDetailViewModel.kt`
- `CommentItem.kt`
- `CommentInputBar.kt`
- any other touched social files

Rules:

- all Vietnamese strings must be UTF-8 safe
- error/success strings must describe the real state
- do not claim success before the server confirms success

### Step 6 - Replace Any Remaining “Almost Mock” Behavior

Audit the social experience for anything that still feels half-real:

- placeholder toasts
- fake state transitions
- counters that update without true server backing
- silent failure paths

Required outcomes:

- like/comment/reply must either be real and consistent, or clearly fail with feedback
- do not leave any “pretend success” behavior in the social feature

### Step 7 - Verification

Required build verification:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

You must also report a **manual runtime verification matrix** for social scope only:

1. feed initial load
2. feed append load
3. feed append retry after error
4. doctor badge on post
5. opening post detail
6. loading comments
7. replying to a comment
8. like from feed
9. like from post detail
10. creating a top-level comment
11. creating a nested reply
12. comment count/header consistency after mutation

You do not need to physically tap a device, but you must reason concretely from the code and identify what still requires manual runtime confirmation.

## Required Delivery Format

Your response back must include:

### 1. Social QA Status

One of:

- `Accepted for closure`
- `Accepted with minor residual gaps`
- `Not ready for closure`

### 2. Files Changed

List exact files changed.

### 3. Fix Summary

List what you stabilized or corrected.

### 4. Remaining Gaps

List any real remaining blockers or residual product gaps honestly.

### 5. Build Result

Include the build result from `assembleDebug`.

## Hard Rules

- do not touch Epic 1 admin code
- do not touch unrelated family/chat/medical flows
- do not update `EPIC_PROGRESS_TRACKER.md`
- do not create or commit local planning files
- do not declare Epic 2 complete if the social feature still contains unstable or misleading behavior

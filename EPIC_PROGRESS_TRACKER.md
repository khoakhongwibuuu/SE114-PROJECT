# CareNest Epic Progress Tracker

> Living document for migration progress, active scope, and context handoff.
> Last updated: 2026-06-04

## Purpose

This file exists to keep the team aligned on:

- what each Epic is trying to achieve
- what has already been completed
- what is currently in progress
- what is still blocked, mocked, or intentionally deferred
- which commits are the key checkpoints to remember

This is the first file to read before starting a new implementation batch, QA pass, or subagent handoff.

---

## Project Context

CareNest is being migrated from a legacy React Native codebase to a native Android application built with Kotlin and Jetpack Compose.

The migration is being executed in epics. Each epic is treated as a structured delivery unit with:

- architecture decisions
- implementation phases
- QA/stabilization passes
- technical debt cleanup

The current repository state includes:

- a migrated user-facing app shell
- a dedicated admin workspace
- a new social data foundation and paging engine

---

# Epic 1 - Admin Workspace (CMS)

## Epic Goal

Build a dedicated Admin workspace with strict RBAC separation from the normal USER/DOCTOR app experience.

The intended architecture for Epic 1 was:

- root role-based routing
- dedicated Admin shell
- visually distinct Admin theme
- 4 admin pillars:
  - Dashboard
  - User Management
  - eKYC Pipeline
  - Content Moderation

## Current Status

**Status:** Completed  
**Confidence:** High for implementation scope, suitable for continued QA  
**Epic State:** Closed for current migration phase

## What Was Completed

### 1. Root RBAC Routing

- `SecureSessionManager` role is read at the root
- `ADMIN` routes into a dedicated admin workspace
- `USER` and `DOCTOR` route into the standard app
- admin route guarding was hardened so unauthorized access does not silently leak into the admin shell

### 2. Dedicated Admin Workspace

- separate `AdminMainScreen`
- visually distinct admin shell
- dedicated bottom navigation
- isolated admin surface instead of mixed profile-entry admin actions

### 3. Admin Dashboard

- dashboard stats pipeline implemented
- dashboard screen present and localized
- shell integrated into admin bottom nav

### 4. User Management

- Paging 3 implemented for admin user list
- search integrated into paging flow
- optimistic ban/unban updates implemented

### 5. Admin eKYC Pipeline

- legacy admin verification screen removed
- new single source of truth is the admin workspace eKYC screen
- approve / reject / revoke flows implemented
- rejection reason flow enforced
- local state updated immediately after actions

### 6. Content Moderation

- moderation pipeline implemented for reports
- delete and dismiss actions implemented
- post and comment moderation parity added
- optimistic hide-on-action pattern implemented

### 7. Epic 1 Polish / Debt Cleanup

- legacy admin route and duplicate eKYC path removed
- admin labels translated
- mojibake/UTF-8 issues cleaned in admin scope
- route guarding enforced

## Key Commits

- `8170eb0` - `feat(admin): implement User Management with Paging 3 and ban/unban logic`
- `81ec32d` - `feat(admin): finalize eKYC approval/rejection pipeline in Admin workspace`
- `2832b0a` - `feat(admin): implement Content Moderation pipeline for user reports`
- `304e63f` - `refactor(admin): eradicate legacy eKYC screen and unwire from Profile`
- `987db54` - `feat(admin): achieve full moderation parity with comment deletion support`
- `7617fb2` - `style(admin): enforce strict route guarding, translate labels, and fix mojibake`

## Remaining Notes / Debt

Epic 1 is considered complete for the current migration milestone, but a future polish pass may still revisit:

- visual refinement of admin analytics cards
- richer moderation metrics
- more formal backend module organization for admin contracts

These are no longer blockers for Epic 1 closure.

## Recommended Rule Going Forward

Do not rebuild admin features from profile shortcuts or parallel legacy screens again.

Any future admin work should extend the dedicated Admin workspace only.

---

# Epic 2 - Social Groups (Facebook-style Feed & Interactions)

## Epic Goal

Build a true social group system with:

- groups
- posts
- nested comments
- reactions
- paginated feeds
- scalable data flows safe for long scroll sessions

This Epic is intentionally being built in layers:

1. contracts and domain foundation
2. paging/data engine
3. feed UI and interaction surfaces
4. stabilization and parity refinement

## Current Status

**Status:** In Progress  
**Current Phase:** Phase 3 completed  
**Next Phase:** Phase 4 - Post Detail & Nested Comments UI

## Phase Breakdown

### Phase 1 - Social Data Foundation & Contracts

**Status:** Completed

Completed:

- domain models created:
  - `Group`
  - `Post`
  - `Comment`
  - `Reaction`
- supporting enums added:
  - `GroupPrivacyType`
  - `AuthorRole`
  - `ReactionType`
- paginated wrapper defined
- `SocialApi` created with pagination-aware endpoints
- `SocialRepository` interface created

Key commits:

- `89f3742` - `feat(social): define domain models for groups, posts, and nested comments`
- `3c168e7` - `chore(social): establish SocialApi contracts with pagination parameters`
- `5be0c3f` - `feat(social): define SocialRepository interfaces for data layer`

### Phase 2 - Data Engine & Paging Pipeline

**Status:** Completed

Completed:

- `PostPagingSource` implemented
- `CommentPagingSource` implemented
- `SocialRepositoryImpl` implemented
- repository upgraded to return `Flow<PagingData<...>>` for posts/comments
- `SocialFeedViewModel` exposes cached post feed flow
- `PostDetailViewModel` exposes cached comment flow

Key commits:

- `8bf72ca` - `feat(social): implement Post and Comment PagingSource classes`
- `4d762fd` - `feat(social): implement SocialRepositoryImpl with Pager flows`
- `2e47c48` - `feat(social): establish ViewModels exposing cached PagingData flows`

### Phase 3 - Feed UI Shell

**Status:** Completed

Completed:

- group feed screen using `LazyPagingItems<Post>` (`SocialFeedScreen.kt`)
- stateless post cards (`PostCard.kt`)
- doctor badge rendering based on `AuthorRole.DOCTOR`
- image strip/grid rendering using Coil's `AsyncImage`
- like/comment CTA shell with click lambdas and count displays
- loading / empty / retry states handled gracefully for initial loading, initial error, pagination loading, and pagination error

Key commits:

- `56236da` - `feat(social): create stateless PostCard component with Doctor badge support`
- `933648e` - `feat(social): implement SocialFeedScreen with LazyPagingItems and LoadState handling`

### Phase 4 - Post Detail & Nested Comments UI

**Status:** Not Started

Planned scope:

- post detail screen
- nested comments list
- reply thread support via `parentCommentId`
- comment pagination
- reaction/comment count updates

### Phase 5 - Social Interaction Logic

**Status:** Not Started

Planned scope:

- react to post
- optimistic reaction updates
- comment creation
- reply creation
- feed refresh after mutation

### Phase 6 - QA, Stability & Parity Pass

**Status:** Not Started

Planned scope:

- runtime stabilization
- UTF-8 sweep
- API truthfulness audit
- pagination edge-case audit
- memory/scroll behavior validation

## Current Social Architecture Snapshot

### Domain

- `feature/social/domain/model/SocialModels.kt`
- `feature/social/domain/repository/SocialRepository.kt`

### Data

- `feature/social/data/remote/SocialApi.kt`
- `feature/social/data/paging/PostPagingSource.kt`
- `feature/social/data/paging/CommentPagingSource.kt`
- `feature/social/data/repository/SocialRepositoryImpl.kt`

### Presentation Foundation

- `feature/social/presentation/SocialFeedViewModel.kt`
- `feature/social/presentation/PostDetailViewModel.kt`

## Open Gaps for Epic 2

These are the main things still missing before Epic 2 can be called complete:

- no Compose feed UI yet
- no post detail / nested comment UI yet
- no user interaction shell yet
- no optimistic reaction strategy yet
- no mutation refresh strategy proven in runtime
- no stabilization pass yet

## Risks / Watchouts

- backend pagination shape must stay aligned with `PaginatedResponse<T>`
- nested comments can become a source of recursion and UI state bugs if reply loading is rushed
- reaction state must distinguish server truth from local optimistic state
- UTF-8 discipline remains mandatory for Vietnamese strings in all social UI work

---

# Active Roadmap Summary

## Closed

- Epic 1 - Admin Workspace

## Active

- Epic 2 - Social Groups

## Likely Next Execution Order

1. Epic 2 - Phase 3 - Feed UI shell
2. Epic 2 - Phase 4 - Post detail and nested comments UI
3. Epic 2 - Phase 5 - Reactions and interaction mutations
4. Epic 2 - Phase 6 - QA / stabilization / parity pass

---

# How To Update This File

Whenever a meaningful batch is completed, update the following sections:

## 1. Update Status

Change:

- `Status`
- `Current Phase`
- `Next Phase`

## 2. Append Key Commits

Add new commit hashes and messages under the relevant Epic / Phase section.

## 3. Move Work Across States

For each phase:

- `Not Started` -> `In Progress`
- `In Progress` -> `Completed`

## 4. Record Scope Truthfully

Always distinguish between:

- implemented
- mocked
- partially wired
- blocked
- deferred

Do not mark a phase complete if it only compiles but has not met its actual intended scope.

## 5. Preserve Context

If a major architectural decision is made, add a note here so future contributors do not unknowingly reverse it.

Examples:

- "Admin eKYC must only live inside AdminMainScreen"
- "Social posts/comments paging is repository-owned, not UI-owned"
- "UTF-8 Vietnamese text must be verified before QA handoff"

---

# Quick Start For The Next Developer

If you are picking up this codebase fresh:

1. Read this file first.
2. Read `DEVELOPER_HANDBOOK.md` for broader architecture.
3. Inspect the latest Epic commit checkpoints listed above.
4. Continue only from the declared `Next Phase`, not from assumptions.


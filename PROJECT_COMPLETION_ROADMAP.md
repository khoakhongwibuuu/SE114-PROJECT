# CareNest Project Completion Roadmap

## Document Purpose
This document is the working coordination source for completing the CareNest product.

It is designed to:
- keep product scope stable
- keep engineering work prioritized by real user impact
- prevent context loss across future sessions
- define clear acceptance criteria before a phase can be considered done

This is a living document. Update it only when the actual repo state changes.

## Scope Exclusions
The following features are explicitly excluded from the current product-completion critical path:
- OCR productionization
- AI chat / AI Care production intelligence

These may remain as truthful placeholders or reduced-scope UX surfaces until the main product is stable.

---

# Overall Status

| Area | Status | Notes |
|---|---|---|
| Epic 1 - Admin Workspace | Complete | Operationally present; still subject to runtime QA like the rest of the app |
| Epic 2 - Social Groups | Code-level complete | Needs runtime environment validation for true closure |
| Epic 3 - Navigation / IA Refactor | In Progress | Core structure exists; family chat reattached; needs runtime validation and surrounding stabilization |
| Epic 4 - Moderated Group Posts | Runtime QA In Progress | Code-level flow is wired; runtime/API evidence is partly positive; targeted app/device QA and bug-fix pass is next |
| Product Completion | In Progress | Main goal now is end-to-end usability excluding OCR and AI chat |

---

# Current Coordinated Batch

**Batch ID:** `BATCH-02-DEVICE-WALKTHROUGH`  
**Issued:** `2026-06-09`  
**Owner:** `Subagent`  
**Primary Objective:** Finish the remaining real app/device walkthrough validation for Moderated Group Posts before opening any targeted bug-fix pass.

## Batch Scope
- verify member-side `Bài của tôi` after host approval
- verify non-host doctor cannot see moderation UI in the app
- verify admin moderation path in the app
- verify rejection reason visibility in the app if a reject case is executed
- record only real defects observed in the walkthrough

## Explicitly Out of Scope
- OCR implementation or OCR productionization
- AI Care production intelligence
- family chat redesign
- realtime community chat redesign
- new epics
- broad refactors outside moderated group posting

## Latest Review Update
- Reviewed moderated-group-post runtime/API report against repo state on `2026-06-09`
- Current Epic 4 verdict: `Code-level complete, targeted runtime QA still required`
- Confirmed from the report and repo together:
  - no currently confirmed blocking defect was found in the API-level moderation flow
  - previously reported DEF-01 remains invalid; reject reason is already sent correctly as a query param from the frontend
  - DEF-02 remains only an environment/tooling display note, not a product bug
  - host/admin moderation behavior, approved-only feed behavior, and rejection-reason enforcement all have positive API/runtime evidence
- Important limit of the current evidence:
  - GP-01 and GP-02 were only confirmed at code-level routing, not by full UI/device walkthrough
  - GP-12 was not executed, so the non-member interaction rule is still not runtime-proven
  - partial device evidence now proves the host-side moderation path, but member/admin/non-host-doctor app states are still pending

## Known Cross-Cutting User-Observed Defects
- back navigation is still unstable in some flows:
  - instead of returning to the immediate previous screen, the app can jump unpredictably to Home or unrelated chat/room screens
- some chat/room flows are still in a non-loading or half-loading state during real use
- these are product-level stabilization issues and must be treated as higher priority than additional UX enrichment

---

# Next Queued Batch

**Batch ID:** `BATCH-04-NAV-BACKSTACK-ROOM-STABILIZATION`  
**Issued:** `2026-06-09`  
**Owner:** `Subagent`  
**Primary Objective:** Stabilize broken back navigation and room loading behavior before further UX expansion work continues.

## Batch Scope
- reproduce incorrect back-stack behavior from real app flows
- fix navigation so back returns to the true previous screen instead of jumping to Home or unrelated rooms
- identify and fix room screens that fail to load reliably
- validate family chat rooms, community chat rooms, and moderated group post rooms against the corrected navigation flow
- rebuild and re-test after each fix pass

## Explicitly Out of Scope
- structured UX enrichment for group posts
- speculative fixes without reproduction evidence
- broad feature redesign
- OCR
- AI chat productionization
- unrelated modules outside navigation/chat/community loading stabilization

---
## Runtime Verification Attempt
- Runtime verification report received on `2026-06-08`
- Result: `Blocked by environment`
- Reported blockers:
  - backend not running
  - database not running
  - Redis not running
  - websocket not reachable
  - no emulator / physical-device execution environment available to the tester
- Consequence:
  - all `FC-*` and `CG-*` runtime cases remain `Not Executed`
- Batch 01 cannot be judged beyond code-level hardening

---

## Environment Unblock Update
- Environment unblock report received on `2026-06-08`
- Current verdict: `Ready to begin runtime QA`
- Independently confirmed by Codex:
  - backend HTTP on `localhost:8080` is reachable
  - Swagger UI responds with `200 OK`
  - websocket endpoint responds on `/api/v1/ws` with `426 Upgrade Required`, confirming the handler is listening
- Reported by subagent but not independently re-verified by Codex due local permission restrictions:
  - Docker containers for PostgreSQL and Redis are healthy
  - physical Android device is connected
  - debug APK was installed successfully
- Practical outcome:
  - Batch 00 can be treated as functionally complete
  - Batch 01 runtime verification can now proceed

---

# Status Legend

- `Not Started`
- `In Progress`
- `Blocked`
- `Code-Level Complete`
- `Runtime QA Pending`
- `Complete`

---

# Owner Legend

- `Codex` = direct implementation/review inside this workspace
- `Subagent` = delegated execution batch
- `User Runtime QA` = physical-device/manual validation by the user
- `Shared` = requires both implementation and real runtime confirmation

---

# Phase 1 - Navigation, Family Hub, and Family Chat

**Status:** `Runtime QA Pending`  
**Owner:** `Codex`  
**Priority:** `P0`

## Goal
Lock the appâ€™s core information architecture so users can reliably find:
- family management
- medicine
- family conversation
- community
- inbox

## Target Structure
- Bottom nav:
  - `Trang chá»§`
  - `Gia Ä‘Ã¬nh`
  - `Cá»™ng Ä‘á»“ng`
  - `Tin nháº¯n`
  - `TÃ´i`
- Family hub:
  - `ThÃ nh viÃªn`
  - `Tá»§ thuá»‘c`
  - `TrÃ² chuyá»‡n`
- Inbox:
  - `AI Care`
  - `BÃ¡c sÄ©`

## Checklist
- [x] Remove standalone `Thuá»‘c` bottom-nav tab
- [x] Keep exactly 5 bottom-nav pillars
- [x] Move medicine access under `Gia Ä‘Ã¬nh`
- [x] Refactor Family hub to 3 tabs
- [x] Remove family chat from Inbox
- [x] Keep Inbox communication-only
- [x] Reattach real family chat infrastructure inside `Gia Ä‘Ã¬nh -> TrÃ² chuyá»‡n`
- [x] Prevent unnecessary state wipe when revisiting the same loaded family chat
- [x] Bound reconnect retries for family chat
- [ ] Verify active family switching updates family chat correctly
- [ ] Verify no dead routes remain from old family/inbox structure
- [ ] Verify runtime behavior on device/emulator

## Acceptance Criteria
- User can understand where each communication channel belongs without ambiguity
- Family chat is accessible only from `Gia Ä‘Ã¬nh -> TrÃ² chuyá»‡n`
- Inbox no longer contains family or community group navigation
- Switching families changes the conversation context correctly
- No crashes or dead-end navigation paths remain in the refactored structure

---

# Phase 2 - Community Group Chat Stabilization

**Status:** `Runtime QA Pending`  
**Owner:** `Shared`  
**Priority:** `P0`

## Goal
Make community group chat rooms genuinely usable, not just visually present.

## Checklist
- [x] Restore group entry from `Cá»™ng Ä‘á»“ng -> Há»™i nhÃ³m`
- [x] Ensure group cards route into real chat rooms
- [x] Keep community chat route separate from family chat
- [x] Improve fallback UX so saved-via-fallback does not look like a hard error
- [x] Filter optimistic echo duplication in community chat room
- [x] Bound reconnect retries for community chat
- [ ] Verify websocket connect / reconnect against running backend
- [ ] Verify send-message behavior on device
- [ ] Verify room load history on device
- [ ] Verify keyboard/composer behavior on device
- [ ] Verify leave/report/moderation actions do not crash

## Acceptance Criteria
- Clicking a group card opens the correct room every time
- Room history loads without force-close
- Sending a message either succeeds or shows a truthful fallback state
- Reconnect behavior does not mislead the user into thinking the room is broken
- No blocker remains in the core group-chat path

---

# Phase 3 - Social Feed, Post Detail, and Interaction Truthfulness

**Status:** `Code-Level Complete`  
**Owner:** `Shared`  
**Priority:** `P0`

## Goal
Ensure social interaction behaves truthfully with server-backed state.

## Checklist
- [x] Establish paged feed data pipeline
- [x] Establish paged post-detail comments pipeline
- [x] Implement feed UI
- [x] Implement post detail and nested comments UI
- [x] Add like interaction logic
- [x] Add comment / reply creation logic
- [x] Add doctor badge rendering
- [x] Add `likedByMe` backing state in social model
- [x] Remove obviously fake mutation UI for like/comment/reply
- [ ] Verify like state persists correctly at runtime
- [ ] Verify comment/reply persistence at runtime
- [ ] Verify header counts remain correct after runtime refresh
- [ ] Verify doctor badge consistency across real API data

## Acceptance Criteria
- Feed and post-detail screens agree on liked state
- Like/comment/reply do not leave stale counts or stale local state
- Doctor identity markers remain consistent across all social surfaces
- No fake â€œsuccessfulâ€ social interaction remains when backend truth disagrees

---

# Phase 3A - Moderated Group Posts MVP

**Status:** `Runtime QA Pending`  
**Owner:** `Shared`  
**Priority:** `P0`

## Goal
Ship the first runnable version of health-group posting where content is submitted by members and approved by a group host or an admin before becoming publicly visible.

## Product Decisions Locked
- Every new group post enters `PENDING_APPROVAL`
- Only `APPROVED` posts appear in the public group feed
- Rejection requires a reason
- Authors can see their own post statuses
- Approvers are `HOST` and `ADMIN`
- Use the existing `feature/community` module; do not create a parallel feature stack

## Checklist
- [x] Extend group-post domain model to support moderation status and review metadata
- [x] Extend community API/repository contracts for:
  - create group post
  - fetch approved posts
  - fetch my group posts
  - fetch pending posts
  - approve post
  - reject post
- [x] Build a runnable group detail flow with:
  - public approved feed
  - create-post entry
  - my-posts/status view
- [x] Build a minimal moderation queue for host/admin
- [x] Require rejection reason in the UI and backend request
- [x] Ensure approved-only filtering is truthful in the user-facing feed
- [x] Keep the UX explicit that newly submitted posts are waiting for approval
- [x] Build frontend and backend successfully
- [x] Wire `Thảo luận` into the real app flow
- [x] Keep `Thảo luận` and `Vào chat` separated
- [ ] Verify runtime member flow on app/device
- [x] Verify runtime host moderation flow on app/device
- [ ] Verify runtime admin moderation flow on app/device
- [ ] Verify member/non-host doctor do not see moderation UI at runtime

## Acceptance Criteria
- A member can submit a post inside a group without it appearing immediately in the public feed
- The author can see that post as `Chờ duyệt`
- A host or admin can approve the post
- After approval, the post appears in the public group feed
- A moderator can reject with a reason, and the author can see the rejected status and reason
- `Thảo luận` and `Vào chat` remain clearly separated in the real app

## Latest Verified State
- Code-level complete on `2026-06-08`
- Runtime/API verification report reviewed on `2026-06-09`
- No currently confirmed blocking defect was found in the API-level moderation flow
- One previously suspected defect was invalid and dismissed:
  - DEF-01 (`reject reason sent incorrectly from frontend`) does not match the current repo
- Partial device walkthrough evidence recorded on `2026-06-09`:
  - host sees the `Chờ duyệt` tab in-app
  - host approve flow succeeds in-app
  - approved post appears in the public feed in-app
- Remaining work is now member/admin/non-host-doctor walkthrough validation plus any bug-fix findings that arise from that walkthrough

---
# Phase 4 - Family Management and Medicine Core Flows

**Status:** `In Progress`  
**Owner:** `Shared`  
**Priority:** `P0`

## Goal
Make the family and medication system work as a usable household workflow.

## Checklist
- [x] Place medicine under Family hub
- [x] Fix â€œno medicine cabinetâ€ to behave like an empty state rather than a failure
- [ ] Verify create family flow
- [ ] Verify join family flow
- [ ] Verify invitation handling flow
- [ ] Verify active family switching across app modules
- [ ] Verify add medicine flow
- [ ] Verify medicine schedule flow
- [ ] Verify add schedule flow
- [ ] Verify edit/delete medicine actions if present
- [ ] Verify all medicine routes are reachable only through the new Family hub

## Acceptance Criteria
- Users can create or join a family without broken state
- Family lists and names remain correct across screens
- Medicine cabinet behaves like a real feature, not a broken entry point
- Core medication save/edit/schedule flows are stable

---

# Phase 5 - Profile, Health Profile, and Appointment Flows

**Status:** `In Progress`  
**Owner:** `Shared`  
**Priority:** `P0`

## Goal
Stabilize user identity, health profiles, and appointment-related flows.

## Checklist
- [x] Fix profile state to load current user instead of stale mock state
- [x] Fix family member naming source to reduce cross-screen inconsistency
- [x] Add dynamic role refresh via `/auth/me`
- [ ] Verify profile editing across multiple accounts
- [ ] Verify health profile details loading for family members
- [ ] Verify medical info save/update flow
- [ ] Verify appointment list load
- [ ] Verify appointment creation / booking entry path
- [ ] Verify doctor role unlocks the correct UI after runtime refresh

## Acceptance Criteria
- Switching accounts no longer leaks profile data across users
- Profile and health data save to the correct owner
- Appointment flows do not crash on null or mismatched backend data
- Role refresh visibly changes the right UI without forcing logout/login

---

# Phase 6 - Home Dashboard and Notifications

**Status:** `In Progress`  
**Owner:** `Shared`  
**Priority:** `P1`

## Goal
Turn the home/dashboard experience into a truthful operational summary rather than a decorative shell.

## Checklist
- [x] Align shortcut icon set more closely with RN intent
- [ ] Verify home summary cards and tasks against live data
- [ ] Verify dashboard family/member switching behavior
- [ ] Verify unread notification counts
- [ ] Verify notification list screen
- [ ] Verify deep links from home to destination screens
- [ ] Review dashboard copy and empty states for honesty

## Acceptance Criteria
- Home reflects real data or truthful empty/fallback states
- Counts and shortcuts lead somewhere valid
- Notification flows do not feel stale or disconnected from the rest of the app

---

# Phase 7 - Admin Operational Pass

**Status:** `Code-Level Complete`  
**Owner:** `Shared`  
**Priority:** `P1`

## Goal
Ensure the admin workspace is operational enough to support moderation and doctor verification.

## Checklist
- [x] Dedicated admin shell exists
- [x] Root role routing to admin exists
- [x] Paging-based user management exists
- [x] eKYC admin flow exists
- [x] moderation post/comment actions exist
- [x] legacy admin eKYC route was purged
- [ ] Verify admin routing on runtime
- [ ] Verify user ban/unban behavior on runtime
- [ ] Verify doctor verification actions on runtime
- [ ] Verify moderation flow on runtime

## Acceptance Criteria
- Admin users are routed to the right shell
- User management, eKYC, and moderation are not merely compile-complete but actually operable
- No admin-only functionality leaks into the user shell

---

# Phase 8 - Product Honesty and Placeholder Policy

**Status:** `In Progress`  
**Owner:** `Codex`  
**Priority:** `P1`

## Goal
Make every unfinished feature truthful and non-misleading.

## Included Features
- AI Care
- OCR
- Doctor messaging if still placeholder-only
- Any simulated banner, mock data view, or redirected feature shell

## Checklist
- [ ] Identify all remaining placeholders
- [ ] Classify each placeholder as:
  - safe placeholder
  - misleading placeholder
  - should be hidden
- [ ] Rewrite placeholder copy where needed
- [ ] Remove UI wording that over-promises real capability
- [ ] Ensure placeholder CTAs do not point to nonexistent destinations

## Acceptance Criteria
- Users can tell what is real vs. preview vs. still under construction
- No unfinished flow pretends to be production-ready
- Placeholder surfaces do not harm trust

---

# Phase 9 - Runtime Environment Unblock and Full QA

**Status:** `In Progress`  
**Owner:** `Shared`
**Priority:** `P0`

## Goal
Move from code-level confidence to runtime-backed product confidence.

## Current Blockers
- some runtime dependencies were previously unavailable, but the current environment is now reported provisioned well enough to begin runtime QA
- Codex independently verified backend HTTP and websocket reachability; Docker/device status was reported by subagent and still awaits direct confirmation outside local permission restrictions

## Checklist
- [ ] Bring backend up locally in a reproducible way
- [ ] Ensure DB and Redis are available
- [ ] Verify auth, family, community, social, and admin endpoints against running backend
- [ ] Test websocket flows with active backend
- [ ] Execute focused app walkthrough on device/emulator
- [ ] Record findings by severity and affected flow

## Acceptance Criteria
- Product-critical flows have runtime evidence
- Remaining issues are categorized honestly
- We can distinguish code-level completeness from actual release readiness

---

# Immediate Execution Order

1. Stabilize back navigation and room loading behavior across active community/chat flows
2. Finish the remaining **Phase 3A** device walkthrough cases for moderated group posts
3. Apply only the bug fixes discovered by those walkthroughs
4. Resume **Phase 1** runtime validation
5. Resume **Phase 2** runtime stabilization
6. Validate **Phase 3** interaction truthfulness on live runtime
7. Complete **Phase 4** family + medicine core flow testing/fixes
8. Stabilize **Phase 5** profile + appointment flows
9. Tighten **Phase 6** dashboard/notifications
10. Run **Phase 7** admin operational validation
11. Enforce **Phase 8** product honesty rules
12. Continue **Phase 9** broad runtime QA
---

# Release Gate Definition

The product can only be considered functionally ready for a serious stabilization/release pass when:
- no P0 crash/blocker remains in family, community, social, medicine, profile, or admin core flows
- runtime evidence exists for the main flows
- unfinished features are presented honestly
- user identity and family context remain consistent across screens
- chat and social interactions no longer depend on misleading UI states

---

# Update Rules

When updating this file:
- do not mark a phase `Complete` based only on compile success
- do not mark a phase `Runtime QA Pending` as `Complete` without runtime evidence
- keep notes short and factual
- prefer changing status only after repo changes or verified findings
- do not use this file for speculation; use it only for current product truth






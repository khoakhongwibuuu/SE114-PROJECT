# BATCH-04-NAV-BACKSTACK-AND-ROOM-LOADING

## Status
`Issued`

## Date
`2026-06-09`

## Owner
`Subagent`

## PM Direction
Back navigation and room loading are now treated as **stabilization blockers**.

We have real user evidence that:
- pressing back can jump to the wrong screen
- some chat/room flows do not load correctly

These defects are more urgent than additional UX enrichment.

This batch exists to make navigation behavior trustworthy and room loading reliable.

---

# Product Goal

Ensure that:

1. Back navigation returns to the **actual immediate previous screen**
2. The app does **not** jump to Home or unrelated rooms unexpectedly
3. Family chat rooms, community chat rooms, and moderated group-post screens load reliably when entered from real app flows

---

# Strict Scope

This batch includes:

1. Back-stack correctness for active flows
2. Room/screen loading stabilization for:
   - family chat room
   - community realtime chat room
   - moderated group post discussion flow
3. Route-entry sanity checks from their real parent surfaces

This batch does **not** include:
- OCR
- AI chat
- structured post-card redesign
- visual polish unrelated to the bug
- feature expansion

---

# User-Observed Problem Statements

You must treat these as the starting truth to investigate:

1. Back navigation is unstable
   - instead of returning to the previous screen, it can jump to Home or unrelated chat/room surfaces

2. Some rooms are not loading
   - room entry may open a shell that does not load content correctly
   - loading may stall, blank, or half-resolve

Do not dismiss these as â€œcannot reproduceâ€ without a real reproduction attempt from the active app flows.

---

# Required Investigation Areas

You must inspect and harden, at minimum:

- `MainScreen`
- `Navigation`
- `NavigationKeys`
- any route objects or state restoration logic used by chat/community/family flows
- family chat room entry and back behavior
- community chat room entry and back behavior
- moderated group-post flow entry and back behavior

Look specifically for:
- incorrect `popBackStack()` usage
- default-tab resets that override true back behavior
- state restoration sending users to the wrong screen
- shared callbacks that route different flows through the wrong destination
- stale route arguments causing the wrong room/group to reopen

---

# Runtime-Facing Flows To Verify

## Flow A - Family chat room
Path:
- `Gia Ä‘Ã¬nh -> TrÃ² chuyá»‡n -> chá»n gia Ä‘Ã¬nh -> vÃ o phÃ²ng`

Must verify:
- room loads correctly
- composer/history area loads correctly
- back returns to the family room list
- another back returns to the Family tab, not Home

## Flow B - Community realtime chat room
Path:
- `Cá»™ng Ä‘á»“ng -> Há»™i nhÃ³m -> VÃ o chat`

Must verify:
- room loads correctly
- no blank shell / broken loader
- back returns to the correct group list / community surface
- not to an unrelated room or Home

## Flow C - Moderated group post flow
Path:
- `Cá»™ng Ä‘á»“ng -> Há»™i nhÃ³m -> Tháº£o luáº­n`

Must verify:
- discussion/post screen loads correctly
- tabs/content load correctly
- back returns to the correct community/group source
- not to Home or another room

---

# Technical Rules

## 1. Do not â€œfixâ€ this by brute-force routing everything to Home
That would hide the bug, not solve it.

## 2. Preserve real hierarchy
Back must follow the actual user path taken.

## 3. Prefer targeted fixes over broad refactors
If a specific route or restoration pattern is wrong, fix that directly.

## 4. If multiple flows are affected by one shared root cause, document it clearly
This is acceptable and desirable.

---

# Acceptance Criteria

This batch is successful only if:

1. Entering a room/screen from its real parent loads content reliably
2. Back from family chat room returns to the family room list, then Family context
3. Back from community chat room returns to the correct community source
4. Back from moderated group-post flow returns to the correct community source
5. No tested flow jumps unexpectedly to Home or another unrelated room
6. Frontend build succeeds after the fixes

---

# Required Report Format

Return in this exact format:

## 1. Batch Status
Choose one:
- `Code-level complete`
- `Partially complete`
- `Blocked`

## 2. Reproduced Problems
List exactly which navigation/loading problems you reproduced

## 3. Root Cause Analysis
Explain the actual cause(s)

## 4. Files Changed
List every touched file

## 5. Fix Summary
Describe exactly what changed in navigation/back-stack/loading behavior

## 6. Flow-by-Flow Outcome
Report separately for:
- Family chat room
- Community chat room
- Moderated group-post flow

## 7. Remaining Gaps
Be honest and specific

## 8. Build Result
Include frontend build result

---

# Hard Rules

1. Do not broaden into UI redesign work
2. Do not mark complete unless you actually fix the wrong back behavior
3. Do not mark complete unless the tested rooms/screens load reliably
4. Do not â€œsolveâ€ this by forcing navigation to Home
5. Do not hand-wave reproduction; use the real flows above

---

# Final Expectation

After this batch:

**navigation should feel boring**

in the best possible way:
- enter the right room
- see the right content
- press back
- return to the right place

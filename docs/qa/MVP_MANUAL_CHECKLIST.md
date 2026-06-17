# CareNest MVP Manual Checklist

Status values:
- `PASS`
- `FAIL`
- `BLOCKED`
- `NOT TESTED`

## 1. Current code-level baseline

| Area | Check | Status | Evidence |
| --- | --- | --- | --- |
| Backend | `./mvnw test` | PASS | Local agent verification on current `develop` worktree |
| Android | `./gradlew app:assembleDebug` | PASS | Local agent verification on current `develop` worktree |
| Runtime | PostgreSQL + Redis + Android device/emulator | NOT TESTED | Requires manual runtime environment |

Notes:
- AI chatbot and OCR are phase-final only and must not be counted as MVP runtime PASS.
- Local-only files such as `frontend/local.properties` are intentionally excluded from commits.

## 2. Patient checklist

| Flow | Expected result | Status | Notes |
| --- | --- | --- | --- |
| Register / login / refresh | User can sign up, log in, relaunch app, and keep session | NOT TESTED | |
| Profile | `/auth/me` and profile screen load real user data | NOT TESTED | |
| Family create / join | User can create family, join by code, and see active family | NOT TESTED | |
| Family invite | User receives invite notification and can accept/reject | NOT TESTED | |
| Health profile gating | No screen opens with sentinel profile ID; invalid route redirects safely | NOT TESTED | |
| Medication list | Empty/loading/error states render cleanly | NOT TESTED | |
| Add medicine | Save valid medicine, reject invalid date/input | NOT TESTED | |
| Medicine schedule | Create schedule and see reminder rows | NOT TESTED | |
| Vaccination tracker | Create schedule, administer dose, refresh state correctly | NOT TESTED | |
| Growth record | Add record and see history/chart without blank state bugs | NOT TESTED | |
| Doctor directory | Open doctor list and doctor profile | NOT TESTED | |
| Booking online | Create online booking and see it in booking center | NOT TESTED | |
| Booking offline | Create offline booking and see confirmed schedule/status updates | NOT TESTED | |
| Consultation room | Only valid online bookings can open consultation | NOT TESTED | |
| Family chat | Send/receive family messages with correct family isolation | NOT TESTED | |
| Community | Join/leave group, create/edit/delete own post, comment, report | NOT TESTED | |
| Notifications | List, unread count, mark read, mark all read, open correct target | NOT TESTED | |

## 3. Doctor checklist

| Flow | Expected result | Status | Notes |
| --- | --- | --- | --- |
| Doctor verification | Non-approved doctor is gated; approved doctor can enter doctor flows | NOT TESTED | |
| Doctor workspace | Pending requests load with stable empty/loading/error states | NOT TESTED | |
| Approve / reject booking | UI refreshes and patient receives correct notification | NOT TESTED | |
| Confirm schedule | Offline/online confirmations update booking and appointment state | NOT TESTED | |
| Consultation room | Doctor can open only allowed thread and send message | NOT TESTED | |
| Restrict / unrestrict | Messaging state changes correctly and thread remains isolated | NOT TESTED | |
| Community governance | Host/mod/admin permissions behave correctly | NOT TESTED | |

## 4. Admin checklist

| Flow | Expected result | Status | Notes |
| --- | --- | --- | --- |
| Admin login | Admin is routed to admin area, non-admin is denied | NOT TESTED | |
| User management | Toggle active status and admin role via UI without DB access | NOT TESTED | |
| Last-admin guard | Cannot demote or disable the last active admin | NOT TESTED | |
| Group request moderation | Approve/reject doctor group requests from UI | NOT TESTED | |
| Content moderation | Review reports and resolve/delete content from UI | NOT TESTED | |
| System notifications | User receives admin status/role notifications without broken navigation | NOT TESTED | |

## 5. Security regression checklist

| Scenario | Expected result | Status | Notes |
| --- | --- | --- | --- |
| No token | `401` | NOT TESTED | |
| Wrong role | `403` or guarded navigation | NOT TESTED | |
| Cross-family profile access | `403` or `404`, no leaked data | NOT TESTED | |
| Cross-booking / cross-thread guessed ID | `403` or `404`, no leaked data | NOT TESTED | |
| Cross-family chat subscription | Must be denied | NOT TESTED | |

## 6. Known non-MVP or deferred items

| Area | Status | Notes |
| --- | --- | --- |
| AI chatbot | Deferred | Must stay disabled or explicitly marked non-MVP until real provider + structured safety flow are ready |
| OCR medication import | Deferred | Must stay disabled or explicitly marked non-MVP until real image pipeline + confirm-before-save flow are ready |

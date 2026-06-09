# Phase 3: Digital Clinic Ecosystem — Architecture & Product Plan

> **Status:** Draft for Review  
> **Author:** CareNest Engineering  
> **Created:** 2026-06-09  
> **Target Branch:** `develop`

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Chat Room Typology](#2-chat-room-typology)
3. [Request-Approve Workflow (Appointment State Machine)](#3-request-approve-workflow)
4. [Governance & RBAC Architecture](#4-governance--rbac-architecture)
5. [Sprint Execution Plan](#5-sprint-execution-plan)

---

## 1. Executive Summary

### The Universal Touchpoint Principle

Phase 3 transforms CareNest from a community-oriented health platform into a **Digital Clinic Ecosystem**. The core design principle is the **Universal Touchpoint**: every Doctor avatar rendered anywhere in the application — whether in the Social Feed, post comments, article bylines, or Specialty Public Chat rooms — is a clickable entry point into a unified experience.

### End-to-End User Journey

```
Doctor Avatar (anywhere in app)
       │
       ▼
Doctor Profile ("Phòng Khám Số")
  ┌─── Credentials, specialty, ratings, availability
  │    Public Q&A feed (DOCTOR_CLINIC room)
  │
  ▼
Booking Request
  ┌─── Patient selects: ONLINE_CHAT or OFFLINE_CLINIC
  │    Submits request with symptoms/notes
  │
  ▼
Doctor Workspace (Triage)
  ┌─── Doctor reviews pending requests
  │    Approves or Rejects with reason
  │
  ▼
Active Appointment
  ┌─── For ONLINE_CHAT: system provisions a PRIVATE_CONSULTATION room
  │    For OFFLINE_CLINIC: system displays location, date, time
  │
  ▼
Consultation & Completion
  ┌─── Consultation occurs (chat or in-person)
  │    Doctor or system marks appointment COMPLETED
  │    Private chat room is sealed (read-only archive)
  └─── Patient may leave a rating/review
```

This loop ensures that **discovery, trust-building, booking, and consultation** are connected through a single, seamless flow. No dead-end profiles. No disconnected booking forms. Every touchpoint leads to action.

### Design Goals

| Goal | Description |
|------|-------------|
| **Discoverability** | Doctors are visible wherever content is created. Every avatar is a funnel entry. |
| **Trust** | Doctor Profile aggregates credentials, eKYC status, specialty, and community activity. |
| **Controlled Access** | Doctors own their availability. Patients request; doctors approve. No forced assignments. |
| **Privacy** | Private consultations are strictly 1-on-1 with no admin read access. |
| **Auditability** | All administrative and automated actions are logged to a dedicated audit trail. |

---

## 2. Chat Room Typology

CareNest Phase 3 introduces a strict taxonomy of three distinct chat room types. This taxonomy exists to prevent architectural spaghetti — each room type has a clear domain anchor, access model, and purpose boundary.

### 2.1 SPECIALTY_PUBLIC

| Property | Value |
|----------|-------|
| **Anchor Entity** | `specialty_id` (e.g., Pediatrics, Nutrition, Dermatology) |
| **Purpose** | Open community discussion space organized by medical specialty |
| **Who Can Enter** | Any authenticated user (USER, DOCTOR, ADMIN) |
| **Who Can Post** | All members; doctors receive a verified badge on their messages |
| **Moderation** | Community-level moderation (HOST/MODERATOR roles via Group Role) |
| **Lifecycle** | Created by system or by approved doctor request; persists indefinitely |
| **What It Is NOT For** | Private medical advice, 1-on-1 consultation, appointment scheduling |

**Primary Key Relationship:**  
`chat_groups.id` → `chat_groups.specialty_id` (nullable FK to specialties table).  
A `SPECIALTY_PUBLIC` room has `type = 'SPECIALTY_PUBLIC'` and `specialty_id IS NOT NULL`.

### 2.2 DOCTOR_CLINIC

| Property | Value |
|----------|-------|
| **Anchor Entity** | `doctor_user_id` (the owning doctor's user ID) |
| **Purpose** | Semi-public Q&A room attached to a specific doctor's profile ("Phòng Khám Số") |
| **Who Can Enter** | Any authenticated user may read and post questions |
| **Who Can Post** | All members; the owning doctor's replies are visually distinguished |
| **Moderation** | The owning doctor is automatically `HOST`; may appoint `MODERATOR`s |
| **Lifecycle** | Created when a doctor's group creation request is approved; frozen if eKYC expires or doctor is suspended |
| **What It Is NOT For** | Private consultation, sharing protected health information, replacing appointments |

**Primary Key Relationship:**  
`chat_groups.id` → `chat_groups.doctor_user_id` (nullable FK to users table).  
A `DOCTOR_CLINIC` room has `type = 'DOCTOR_CLINIC'` and `doctor_user_id IS NOT NULL`.  
**Constraint:** One `DOCTOR_CLINIC` per doctor.

### 2.3 PRIVATE_CONSULTATION

| Property | Value |
|----------|-------|
| **Anchor Entity** | `appointment_id` (the active appointment that generated this room) |
| **Purpose** | Strictly private 1-on-1 consultation channel between one patient and one doctor |
| **Who Can Enter** | Exactly two participants: the patient and the doctor from the linked appointment |
| **Who Can Post** | Both participants only |
| **Moderation** | None. Admin cannot read content. System can only detect room existence for audit metadata. |
| **Lifecycle** | Provisioned automatically when an `ONLINE_CHAT` appointment transitions to `ACTIVE`. Sealed (read-only) when appointment transitions to `COMPLETED`. |
| **What It Is NOT For** | Group discussion, community Q&A, follow-up without new appointment |

**Primary Key Relationship:**  
`chat_groups.id` → `chat_groups.appointment_id` (nullable FK to appointments table).  
A `PRIVATE_CONSULTATION` room has `type = 'PRIVATE_CONSULTATION'` and `appointment_id IS NOT NULL`.  
**Constraint:** One room per appointment. No re-use.

### 2.4 Type Discrimination Summary

| Field | SPECIALTY_PUBLIC | DOCTOR_CLINIC | PRIVATE_CONSULTATION |
|-------|-----------------|---------------|----------------------|
| `type` | `SPECIALTY_PUBLIC` | `DOCTOR_CLINIC` | `PRIVATE_CONSULTATION` |
| `specialty_id` | ✅ Required | ❌ NULL | ❌ NULL |
| `doctor_user_id` | ❌ NULL | ✅ Required | ❌ NULL |
| `appointment_id` | ❌ NULL | ❌ NULL | ✅ Required |
| `is_private` | `false` | `false` | `true` |
| Max participants | Unlimited | Unlimited | 2 |
| Admin readable | Yes (via report) | Yes (via report) | **No** |

---

## 3. Request-Approve Workflow

### 3.1 Appointment State Machine

The appointment lifecycle follows a strict state machine. No state can be skipped; transitions are guarded by role and business rules.

```
                    ┌──────────────┐
                    │   PENDING    │
                    │  (created by │
                    │   patient)   │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │ Doctor      │            │ Doctor
              │ approves    │            │ rejects
              ▼             │            ▼
     ┌────────────┐         │   ┌──────────────┐
     │  APPROVED  │         │   │   REJECTED   │
     │ (scheduled │         │   │ (with reason)│
     │  for date) │         │   └──────────────┘
     └─────┬──────┘         │         (terminal)
           │                │
           │ Start time     │ Patient cancels
           │ reached        │ before approval
           ▼                ▼
     ┌────────────┐   ┌──────────────┐
     │   ACTIVE   │   │  CANCELLED   │
     │(consult in │   │              │
     │  progress) │   └──────────────┘
     └─────┬──────┘         (terminal)
           │
           │ Doctor or
           │ system closes
           ▼
     ┌────────────┐
     │ COMPLETED  │
     │(chat sealed│
     │ read-only) │
     └────────────┘
         (terminal)
```

### 3.2 State Transition Rules

| From | To | Trigger | Actor | Side Effects |
|------|----|---------|-------|--------------|
| — | `PENDING` | Patient submits booking request | Patient (USER) | Notification sent to doctor |
| `PENDING` | `APPROVED` | Doctor accepts the request | Doctor (DOCTOR) | Notification sent to patient; for `ONLINE_CHAT`: appointment is scheduled |
| `PENDING` | `REJECTED` | Doctor declines with reason | Doctor (DOCTOR) | Notification sent to patient with rejection reason |
| `PENDING` | `CANCELLED` | Patient withdraws request | Patient (USER) | No further action |
| `APPROVED` | `ACTIVE` | Scheduled start time is reached (system) or doctor manually activates | System / Doctor | For `ONLINE_CHAT`: `PRIVATE_CONSULTATION` room is provisioned; both parties gain access |
| `APPROVED` | `CANCELLED` | Patient cancels before activation | Patient (USER) | Notification sent to doctor |
| `ACTIVE` | `COMPLETED` | Doctor closes consultation or system auto-closes after timeout | Doctor / System | `PRIVATE_CONSULTATION` room is sealed (read-only); patient may leave review |

### 3.3 Request Creation Rules

- Only authenticated users with role `USER` may create a booking request.
- Request payload must include:
  - `doctor_id` — target doctor
  - `appointment_type` — `ONLINE_CHAT` or `OFFLINE_CLINIC`
  - `preferred_date` — requested date/time
  - `symptoms_note` — free-text description (optional but recommended)
- A patient may not have more than **3 concurrent PENDING requests** to the same doctor.
- A patient may not create a request to a doctor whose eKYC is expired or whose account is suspended.

### 3.4 Private Consultation Room Provisioning

A `PRIVATE_CONSULTATION` chat room is created **if and only if** all of the following conditions are met:

1. The appointment has `appointment_type = ONLINE_CHAT`.
2. The appointment has transitioned to `ACTIVE` state.
3. No existing `PRIVATE_CONSULTATION` room is linked to this `appointment_id`.

Upon provisioning:
- A new `chat_groups` record is created with `type = 'PRIVATE_CONSULTATION'` and `appointment_id` set.
- Exactly two `user_group_memberships` records are created: one for the patient, one for the doctor.
- The room is immediately available for messaging.

Upon completion:
- The room's `is_frozen` flag is set to `true`.
- No further messages may be sent.
- Historical messages remain readable by the two participants only.

---

## 4. Governance & RBAC Architecture

### 4.1 The Two-Question Rule

CareNest's permission model is built on two orthogonal axes:

| Question | Axis | Values | Scope |
|----------|------|--------|-------|
| *"Who is this person in the system?"* | **System Role** | `USER`, `DOCTOR`, `ADMIN` | Global — set at account level, governs API access and feature visibility |
| *"What can this person do in this specific group?"* | **Group Role** | `HOST`, `MODERATOR`, `MEMBER` | Per-group — set at membership level, governs in-group permissions |

**These two axes are independent.** A `USER` can be a `HOST` of a community group. A `DOCTOR` is a `MEMBER` in groups they join. An `ADMIN` may not even be a member of a group and operates through a separate Admin Monitor interface.

#### System Role Capabilities

| System Role | Capabilities |
|-------------|-------------|
| `USER` | Browse communities, join groups, post content, create booking requests, participate in consultations |
| `DOCTOR` | All USER capabilities + create group requests (SPECIALTY_PUBLIC, DOCTOR_CLINIC), manage own Doctor Profile, approve/reject booking requests, conduct consultations |
| `ADMIN` | System-wide oversight via Admin Monitor, role override, group freeze/dissolve, report adjudication. **Cannot** read PRIVATE_CONSULTATION content. |

#### Group Role Capabilities

| Group Role | Capabilities |
|------------|-------------|
| `HOST` | Full group management: approve/reject posts, mute members, suspend posting privileges, appoint/remove moderators, edit group metadata |
| `MODERATOR` | Post moderation: approve/reject posts, mute members, suspend posting privileges. Cannot modify group settings or appoint other moderators. |
| `MEMBER` | Read posts, create posts (subject to moderation), like, comment, participate in group chat |

### 4.2 Group Creation Workflow

Group creation follows a strict request-approve process with explicit entity separation.

#### Entity Separation Rule

| Table | Purpose | Contains |
|-------|---------|----------|
| `group_creation_requests` | Pending/rejected group proposals | All requests regardless of outcome |
| `chat_groups` | Active, approved groups only | Only groups that have been approved and are operational |

> **Critical:** These two concepts must **never** be collapsed into a single table with status flags. The `chat_groups` table must contain only approved, operational groups. Denied or pending requests live exclusively in `group_creation_requests`.

#### Creation Flow

1. A `DOCTOR` with **active, non-expired eKYC** submits a group creation request.
2. The request specifies the target type: `SPECIALTY_PUBLIC` or `DOCTOR_CLINIC`.
3. Request is stored in `group_creation_requests` with status `PENDING`.
4. An `ADMIN` reviews and either:
   - **Approves:** A new `chat_groups` record is created; the requesting doctor is assigned as `HOST`. The request status updates to `APPROVED`.
   - **Rejects:** The request status updates to `REJECTED` with a reason. No `chat_groups` record is created.

#### Validation Rules

- Only `DOCTOR` accounts may submit group creation requests.
- The doctor's eKYC must be `VERIFIED` and not expired at the time of submission.
- A doctor may have at most **one** `DOCTOR_CLINIC` group.
- Duplicate `SPECIALTY_PUBLIC` requests for the same specialty are rejected automatically.

### 4.3 Admin Monitor Boundaries

The Admin Monitor is the system-wide oversight interface available to `ADMIN`-role users. It operates under strict boundaries to balance safety with privacy.

#### Admin Capabilities

| Capability | Scope | Description |
|-----------|-------|-------------|
| **Group Moderation** | Public/semi-public groups | Freeze groups, dissolve groups, override group roles, review flagged content |
| **Chat Audit** | Public/semi-public messages | Review messages flagged by user reports or automated safety triggers |
| **Role Override Tool** | Any user | Temporarily or permanently modify a user's System Role or Group Role with mandatory reason logging |

#### Privacy Boundary

> **`PRIVATE_CONSULTATION` content is not readable by Admin.**

Admin can observe:
- That a `PRIVATE_CONSULTATION` room exists (metadata).
- Its linked `appointment_id`, creation timestamp, participant IDs, and frozen status.
- Aggregate statistics (message count, duration).

Admin **cannot** observe:
- Message content within the room.
- Attachments or media shared within the room.

Public and semi-public content (`SPECIALTY_PUBLIC`, `DOCTOR_CLINIC`) is exposed to Admin **only** through:
- User-submitted reports (`report_tickets`).
- Automated safety triggers (spam detection, prohibited content flags).

Admin does not have a blanket "read all messages" capability.

### 4.4 Audit Log Requirements

All administrative and automated governance actions must be recorded in a dedicated `audit_logs` table.

#### Schema

| Column | Type | Description |
|--------|------|-------------|
| `id` | `BIGSERIAL PK` | Auto-incrementing identifier |
| `actor_user_id` | `BIGINT FK → users.id` | The user who performed the action (NULL for system-automated actions) |
| `target_entity_type` | `VARCHAR` | Entity type affected: `USER`, `CHAT_GROUP`, `APPOINTMENT`, `GROUP_CREATION_REQUEST` |
| `target_entity_id` | `BIGINT` | ID of the affected entity |
| `action_type` | `VARCHAR` | Action performed: `FREEZE_GROUP`, `DISSOLVE_GROUP`, `MUTE_MEMBER`, `SUSPEND_POSTING`, `RESTRICT_MESSAGING`, `ROLE_OVERRIDE`, `EKYC_EXPIRY_FREEZE`, `ACCOUNT_SUSPENSION_CASCADE` |
| `reason` | `TEXT` | Mandatory human-readable justification |
| `metadata_json` | `JSONB` | Structured context (previous state, new state, policy reference) |
| `created_at` | `TIMESTAMP` | Immutable creation timestamp |

#### Logging Requirements

- **All** override, freeze, mute, suspend, and dissolve actions performed by Admin must emit an audit log entry.
- **All** automated policy actions (eKYC expiry cascade, account suspension cascade) must also emit audit log entries with `actor_user_id = NULL` and `reason` referencing the triggering policy.
- Audit logs are **append-only**. No update or delete operations are permitted on this table.

### 4.5 Data Retention & Evidence Preservation

CareNest follows a **no hard-delete** policy for entities involved in governance actions. This ensures evidence preservation for dispute resolution, legal compliance, and audit trail integrity.

#### Freeze & Soft-Delete Strategy

| Flag | Type | Purpose |
|------|------|---------|
| `is_frozen` | `BOOLEAN DEFAULT false` | Group/room is locked — no new messages, posts, or membership changes. Content remains readable. |
| `deleted_at` | `TIMESTAMP NULL` | Soft-delete marker. Entity is excluded from queries but preserved in storage. |

#### Rules

- Violating groups are **frozen first**, then optionally soft-deleted after review.
- Frozen groups display a banner: "This group has been suspended by administration."
- Soft-deleted entities are excluded from all user-facing queries but remain accessible to Admin for audit purposes.
- No `DELETE FROM` statements are permitted on `chat_groups`, `group_posts`, `chat_messages`, or `user_group_memberships` for governance actions.

### 4.6 Doctor Lifecycle Impact

When a doctor's status changes, the system must execute automated cascading actions to maintain platform integrity.

#### eKYC Expiry

When a doctor's eKYC verification expires:

| Action | Target | Details |
|--------|--------|---------|
| Freeze `DOCTOR_CLINIC` | Owned clinic group | Set `is_frozen = true`; display expiry banner |
| Block new appointments | Doctor's booking API | Return 403 for new booking requests |
| Preserve existing appointments | Active appointments | Allow completion of in-progress consultations |
| Emit audit log | `audit_logs` | `action_type = 'EKYC_EXPIRY_FREEZE'`, `reason = 'Doctor eKYC verification expired'` |

#### Account Suspension

When a doctor's account is suspended by Admin:

| Action | Target | Details |
|--------|--------|---------|
| Freeze `DOCTOR_CLINIC` | Owned clinic group | Set `is_frozen = true`; display suspension banner |
| Cancel pending appointments | Pending requests | Transition all `PENDING` appointments to `CANCELLED` with system reason |
| Remove HOST/MODERATOR roles | Other groups | Downgrade to `MEMBER` in all groups where doctor holds elevated role |
| Block all posting | All groups | Prevent content creation across all group memberships |
| Emit audit log | `audit_logs` | `action_type = 'ACCOUNT_SUSPENSION_CASCADE'`, `metadata_json` containing list of affected entities |

### 4.7 Terminology Standards

All user-facing and internal governance actions must use production-grade terminology:

| Action | Term | Description |
|--------|------|-------------|
| Prevent a member from sending messages | **Restrict Messaging** | Temporary or permanent messaging block within a group |
| Prevent a member from creating posts | **Suspend Posting** | Temporary or permanent post creation block within a group |
| Prevent a member from all group interaction | **Mute Member** | Full interaction block: no posts, comments, likes, or messages |
| Lock a group | **Freeze Group** | No new content; existing content preserved read-only |
| Remove a group | **Dissolve Group** | Soft-delete with evidence preservation |

---

## 5. Sprint Execution Plan

### Sprint 1: Universal Profile UI & Touchpoints

| Property | Value |
|----------|-------|
| **Objective** | Make every doctor avatar in the app clickable and route to a unified Doctor Profile screen |
| **Duration** | 2 weeks |

#### Key Deliverables

- **Doctor Profile Screen** (`DoctorProfileScreen.kt`): Display doctor's full name, specialty, hospital, eKYC badge, bio, ratings summary, and a "Book Appointment" CTA button.
- **Avatar Touchpoint Component** (`DoctorAvatarTouchpoint.kt`): Reusable composable that wraps any doctor avatar with click-to-navigate behavior. Must be integrated into:
  - Social Feed post author rows
  - Comment author rows
  - Article bylines
  - Specialty Public Chat member lists
- **Backend: Doctor Public Profile API** (`GET /api/v1/doctors/{id}/profile`): Returns public profile data, aggregated rating, and clinic group reference.
- **Navigation Integration**: Register the Doctor Profile route in the app's navigation graph with `doctorId` parameter.

#### Dependencies

- Existing user/doctor data model must include specialty and hospital fields.
- eKYC verification status must be queryable.

#### Acceptance Focus

- Tapping any doctor avatar anywhere in the app navigates to the correct Doctor Profile.
- Profile loads within 2 seconds on a standard connection.
- Non-doctor avatars do not trigger navigation.

---

### Sprint 2: Appointment Engine & Database Schema

| Property | Value |
|----------|-------|
| **Objective** | Implement the appointment data model, booking API, and state machine transitions |
| **Duration** | 2 weeks |

#### Key Deliverables

- **Database Schema**:
  - `appointments` table with columns: `id`, `patient_id`, `doctor_id`, `appointment_type` (ENUM: ONLINE_CHAT, OFFLINE_CLINIC), `status` (ENUM: PENDING, APPROVED, REJECTED, CANCELLED, ACTIVE, COMPLETED), `preferred_date`, `symptoms_note`, `rejection_reason`, `created_at`, `updated_at`.
  - `group_creation_requests` table (for Phase 3 group workflow).
  - `audit_logs` table.
  - Add `type`, `specialty_id`, `doctor_user_id`, `appointment_id`, `is_frozen`, `deleted_at` columns to `chat_groups`.
- **Backend: Appointment API**:
  - `POST /api/v1/appointments` — Create booking request (patient).
  - `GET /api/v1/appointments/my` — List patient's appointments.
  - `GET /api/v1/doctors/me/appointments` — List doctor's incoming requests.
  - `POST /api/v1/appointments/{id}/approve` — Approve request (doctor).
  - `POST /api/v1/appointments/{id}/reject` — Reject request (doctor).
  - `POST /api/v1/appointments/{id}/cancel` — Cancel request (patient).
  - `POST /api/v1/appointments/{id}/complete` — Complete consultation (doctor).
- **State Machine Service** (`AppointmentStateMachine.java`): Enforce all transition rules from Section 3.2. Reject invalid transitions with clear error messages.

#### Dependencies

- Sprint 1 must be complete (Doctor Profile provides the booking entry point).
- Database migration tooling must support the new schema additions.

#### Acceptance Focus

- All state transitions follow the documented state machine exactly.
- Invalid transitions return 400 with descriptive messages.
- Concurrent PENDING request limit (3 per patient-doctor pair) is enforced.
- Audit logs are emitted for all administrative transitions.

---

### Sprint 3: Doctor Workspace (Request Management)

| Property | Value |
|----------|-------|
| **Objective** | Build the doctor-facing interface for managing incoming appointment requests |
| **Duration** | 2 weeks |

#### Key Deliverables

- **Doctor Workspace Screen** (`DoctorWorkspaceScreen.kt`): Tabbed interface showing:
  - **Pending** tab: Incoming requests awaiting triage.
  - **Scheduled** tab: Approved appointments with dates.
  - **Active** tab: Currently in-progress consultations.
  - **History** tab: Completed and rejected/cancelled appointments.
- **Request Detail & Action Sheet**: Doctor can view patient's symptoms note, preferred date, and take action (Approve with date confirmation, or Reject with mandatory reason).
- **Push Notifications**:
  - Patient receives notification when request is approved/rejected.
  - Doctor receives notification when a new request arrives.
- **Backend: Doctor Workspace API**: Filtered appointment queries by status, sorted by urgency (PENDING first, then by preferred_date).

#### Dependencies

- Sprint 2 must be complete (Appointment Engine provides the data layer).
- Push notification infrastructure must be operational.

#### Acceptance Focus

- Doctor can triage requests efficiently with minimal taps.
- Rejection requires a reason — empty rejections are blocked by validation.
- Notification delivery is reliable and timely (< 30 seconds).
- Workspace handles 50+ concurrent pending requests without performance degradation.

---

### Sprint 4: Private Consultation Room Initialization

| Property | Value |
|----------|-------|
| **Objective** | Implement automatic provisioning of PRIVATE_CONSULTATION rooms when ONLINE_CHAT appointments become ACTIVE |
| **Duration** | 2 weeks |

#### Key Deliverables

- **Room Provisioning Service** (`ConsultationRoomService.java`): Triggered by the `APPROVED → ACTIVE` state transition for `ONLINE_CHAT` appointments. Creates `chat_groups` record with `type = 'PRIVATE_CONSULTATION'` and exactly two memberships.
- **Room Sealing Logic**: Triggered by the `ACTIVE → COMPLETED` transition. Sets `is_frozen = true` on the room. Prevents new messages but preserves history.
- **Consultation Chat UI** (`ConsultationChatScreen.kt`): Specialized chat interface for private consultations with:
  - Patient and doctor identifiers clearly displayed.
  - "End Consultation" button (doctor only).
  - Read-only mode indicator when room is sealed.
  - No forward/share/screenshot capabilities (UI-level deterrent).
- **Privacy Enforcement Middleware**: API-level guard ensuring only the two participants can access the room's messages endpoint. Admin requests return 403.
- **Patient Consultation Entry Point**: From the patient's appointment list, an "Enter Consultation" button appears when the appointment is `ACTIVE`.

#### Dependencies

- Sprint 3 must be complete (Doctor Workspace provides the activation trigger).
- WebSocket/real-time messaging infrastructure must support private rooms.
- Chat message encryption strategy must be defined (at minimum: TLS in transit, encrypted at rest).

#### Acceptance Focus

- Room is provisioned within 3 seconds of appointment activation.
- Only the two participants can see the room and its messages.
- Admin API calls to read room content return 403 Forbidden.
- Sealed rooms display a clear "Consultation ended" indicator and block message input.
- Room cannot be re-opened after sealing; a new appointment is required.

---

## Appendix: Entity Relationship Overview

```
┌──────────────┐       ┌───────────────────────┐       ┌──────────────────┐
│    users     │       │    chat_groups         │       │  appointments    │
│              │       │                       │       │                  │
│  id          │◄──────│  doctor_user_id (FK)  │       │  id              │
│  email       │       │  specialty_id (FK)    │       │  patient_id (FK) │──► users
│  role        │       │  appointment_id (FK)  │──────►│  doctor_id  (FK) │──► users
│  ...         │       │  type (ENUM)          │       │  status (ENUM)   │
└──────┬───────┘       │  is_frozen            │       │  type (ENUM)     │
       │               │  deleted_at           │       │  ...             │
       │               └───────────────────────┘       └──────────────────┘
       │
       │        ┌──────────────────────────┐      ┌─────────────────┐
       │        │ group_creation_requests  │      │   audit_logs    │
       │        │                          │      │                 │
       └───────►│  requester_id (FK)       │      │  actor_user_id  │
                │  target_type (ENUM)      │      │  target_entity  │
                │  status (ENUM)           │      │  action_type    │
                │  ...                     │      │  reason          │
                └──────────────────────────┘      │  metadata_json  │
                                                  │  created_at     │
                                                  └─────────────────┘
```

---

*This document serves as the authoritative architectural reference for Phase 3 of CareNest. All implementation must conform to the workflows, boundaries, and governance rules defined herein.*

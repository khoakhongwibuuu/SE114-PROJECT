# CareNest QA Test Accounts and Seeded Scenarios

This document describes the deterministic local QA seed dataset and how to use it for runtime verification.

## Reset and reseed workflow

1. Make sure Docker container `carenest-postgres` is running.
2. From `CareNest/backend`, run:

```cmd
.\reset_db.bat
```

3. Start the Spring Boot backend with a `dev` or `qa` profile.
4. Hibernate recreates the tables and `DatabaseSeeder` loads the QA dataset automatically.

## Test accounts

| Role | Email | Password | Primary use |
| --- | --- | --- | --- |
| Admin | `admin@gmail.com` | `Password123!` | System-level admin verification |
| Doctor (Pediatrics) | `bacsinhikhoa@gmail.com` | `Bacsinhikhoa` | Online consultation, pediatric flows |
| Doctor (General) | `bacsidakhoa@gmail.com` | `Bacsidakhoa` | General consultation and completed thread history |
| Patient A | `kiet@gmail.com` | `Kiet13012006` | Rich-data runtime account, family switching |
| Patient B | `doletuankiet06@gmail.com` | `Kiet13012006` | Sparse-data account, pending/restricted flows |
| QA Moderator | `qa.moderator@gmail.com` | `QaModerator123!` | Community moderation and host role |

## Family scenarios

### Family A
- Owner: `kiet@gmail.com`
- Rich-data family
- Contains child profile `Be Na`
- Used to test:
  - medication schedules
  - vaccination tracker
  - manual appointment ledger
  - dashboard tasks

### Family B
- Owner: `doletuankiet06@gmail.com`
- Sparse-data family
- `kiet@gmail.com` is also a member
- Used to test:
  - family switching
  - low-data / empty-state surfaces

## Community scenarios

Seeded groups:
- `Me va Be CareNest`
- `Chia se kinh nghiem Nhi khoa`
- `Suc khoe Gia dinh`

Seeded post states:
- one approved post with like + doctor comment
- one pending-approval post
- one rejected post with explicit rejection reason

QA moderator:
- `qa.moderator@gmail.com`
- host of all seeded groups

## Booking and consultation scenarios

Seeded booking states:
- `PENDING`
- `APPROVED` (ONLINE_CHAT)
- `APPROVED` (OFFLINE_CLINIC)
- `ACTIVE`
- `COMPLETED`
- `RESTRICTED`
- `REJECTED`
- `CANCELLED`

Consultation threads:
- active thread between Patient A and Pediatrics doctor
- completed thread between Patient A and General doctor
- restricted thread between Patient B and Pediatrics doctor

## Appointment ledger scenarios

- one independent manual appointment for `Be Na`
- one completed appointment synced from a completed booking

## Suggested runtime QA mapping

### Account: `kiet@gmail.com`
Use for:
- family switching between Family A and Family B
- dashboard task verification
- patient booking center
- active and completed consultation entry

### Account: `doletuankiet06@gmail.com`
Use for:
- sparse family state
- restricted consultation history
- pending booking visibility

### Account: `bacsinhikhoa@gmail.com`
Use for:
- doctor workspace triage
- active consultation counterpart verification

### Account: `qa.moderator@gmail.com`
Use for:
- community moderation queue
- host-level group post checks

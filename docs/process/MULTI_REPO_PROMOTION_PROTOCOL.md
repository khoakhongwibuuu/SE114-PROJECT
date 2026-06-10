# Multi-Repo Promotion Protocol

## Purpose

This protocol governs how code is promoted from the secondary working repository (`CareNest_KL`) into the primary repository (`SE114-PROJECT`).

The two repositories are intentionally independent. They do not share remotes, branches, or direct Git synchronization. Promotion happens through controlled human review and explicit code transfer.

## Repository Roles

### Primary Repository

- Name: `SE114-PROJECT`
- Purpose: official integration repository
- Protected branch of record: `develop`
- Release branch: `main`
- All final acceptance decisions happen here

### Secondary Repository

- Name: `CareNest_KL`
- Purpose: parallel implementation workspace
- Trusted for development
- Not treated as the source of truth for integration

## Non-Negotiable Rules

1. The two repositories must remain Git-independent.
2. No remote linking, no cherry-picking across remotes, no fetch-based sync between repos.
3. Only one batch is promoted at a time.
4. Promotion is based on real file diffs and real build results, not summary text alone.
5. The primary repository may reject any batch that is out of scope, noisy, or risky.
6. No coordination artifacts are promoted unless explicitly requested:
   - `.md` subagent notes
   - scratch scripts
   - temp export files
   - screenshots
   - runtime logs

## Promotion Unit

Every transfer from the secondary repo to the primary repo must be a single clearly bounded batch:

- one feature
- one fix pass
- one QA hardening pass
- one refactor with a narrow surface

Do not combine unrelated work into the same promotion batch.

## Required Transfer Package

Every promotion request from the secondary repo must contain all of the following:

1. Batch title
2. Goal
3. Exact file list changed
4. Exact user flows changed
5. Build result
6. Runtime QA result
7. Known gaps
8. Explicit scope declaration:
   - `No hidden scope changes: YES` or `NO`

If any of these are missing, the batch is not ready for promotion.

## Allowed Transfer Formats

Promotion into the primary repo may use one of these formats:

### Option A: Patch-Based Promotion

Use when the diff is clean and limited.

Required:

- full patch or exact diff
- final changed file list
- build evidence

### Option B: File-by-File Promotion

Use when the change is complex, the branches have drifted, or the patch is noisy.

Required:

- full contents of each changed file
- file list
- explanation of intended behavior

## Review Standard in Primary Repo

Before code is accepted into the primary repo, it must be checked for:

1. Scope correctness
2. Integration safety against current `develop`
3. Navigation/backstack regressions
4. State/data contract drift
5. Build success in the primary repo
6. User-facing wording honesty
7. Absence of junk artifacts

## Hard Rejection Conditions

A batch must be rejected or returned for cleanup if any of these are true:

- includes unrelated files
- includes temporary logs, screenshots, dump files, or scratch scripts
- includes subagent coordination markdown not requested for product docs
- rewrites architecture outside the assigned batch
- relies on unverified report claims
- compiles only in the secondary repo but breaks the primary repo
- changes routing or data contracts without naming that impact

## Promotion Flow

1. Implement in `CareNest_KL`
2. Build in `CareNest_KL`
3. Run runtime QA for the affected flow
4. Produce the required batch report
5. Send patch or file set for review
6. Re-apply or integrate into `SE114-PROJECT`
7. Build again in `SE114-PROJECT`
8. Review real diff in `SE114-PROJECT`
9. Accept or reject
10. Merge only in `SE114-PROJECT/develop`

## Commit Discipline

Promotion commits in the primary repo must:

- be small
- be scoped
- have plain commit messages
- exclude coordination files unless explicitly requested

Examples:

- `fix(booking): restore doctor profile booking flow`
- `feat(community): add moderated group post comments`
- `chore(pr): remove non-product artifacts from review scope`

## Runtime Evidence Rule

If a batch claims a flow works at runtime, that claim must be backed by one of:

- human test steps and outcomes
- device screenshots tied to named checkpoints
- log evidence tied to a reproduced scenario

Claims without evidence are treated as unverified.

## Default Decision Rule

When in doubt:

- slower promotion
- narrower scope
- stricter review

The goal is not to move code fastest. The goal is to move correct code safely into the primary repository.

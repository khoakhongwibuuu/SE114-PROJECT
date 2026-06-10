# Primary Repo Acceptance Checklist

Use this checklist before accepting any promoted batch from `CareNest_KL` into `SE114-PROJECT`.

## A. Scope Check

- [ ] Batch goal is clear and narrow
- [ ] Changed file list is complete
- [ ] No unrelated feature drift is present
- [ ] No `.md` coordination notes are included unless explicitly requested
- [ ] No logs, screenshots, dumps, or scratch scripts are included

## B. Diff Check

- [ ] Real diff has been reviewed in the primary repo
- [ ] No dangerous overwrite of newer primary-repo logic
- [ ] No silent routing changes
- [ ] No hidden DTO or API contract changes
- [ ] No duplicated abstractions introduced without reason

## C. Build Check

- [ ] Backend build passes in the primary repo
- [ ] Frontend build passes in the primary repo
- [ ] Any required generated artifacts are valid

## D. Flow Check

- [ ] Claimed user flow exists in code
- [ ] Claimed navigation path is reachable
- [ ] Backstack behavior is still sane
- [ ] Error handling remains honest
- [ ] Empty states remain honest

## E. Data / Security Check

- [ ] Access control still holds
- [ ] No obvious data leakage was introduced
- [ ] Sensitive fields are not overexposed
- [ ] Duplicate guards / state guards still behave correctly

## F. Runtime Claim Check

- [ ] Runtime claims are backed by checkpoints
- [ ] Untested items are explicitly marked
- [ ] Blockers are stated honestly

## G. Merge Readiness

Accept only if all are true:

- [ ] Scope is clean
- [ ] Builds pass
- [ ] No blocking review findings remain
- [ ] No stray artifacts remain in the PR
- [ ] The batch is safe to merge into `develop`

## Decision Output

Use one of these exact outcomes:

- `ACCEPT - ready to merge into develop`
- `REVISE - fix findings before merge`
- `REJECT - do not promote this batch`

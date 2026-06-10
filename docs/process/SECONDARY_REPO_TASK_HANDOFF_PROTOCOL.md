# Secondary Repo Task Handoff Protocol

Use this protocol for every completed task in the secondary repository.

## Rule 1 - Commit Immediately

When one task is finished, do not keep working silently on extra follow-up changes.

You must:

1. finish the scoped task
2. build and test as required by the task
3. commit immediately
4. push immediately to the secondary repository
5. report back in the required format

## Rule 2 - One Task, One Commit Group

A completed task must be pushed in a clean and reviewable state.

Allowed:

- 1 commit for a small task
- 2-3 commits for a larger task if they are logically separated

Not allowed:

- mixing unrelated fixes
- bundling multiple tasks into one handoff
- continuing into a new task before the previous one is reported

## Rule 3 - Push Before Report

Do not report a task as complete until the code is already pushed to the secondary repo.

Your report must include:

- branch name
- latest commit SHA
- push result

## Rule 4 - Mandatory Handoff Report

Use this exact structure after every pushed task:

```md
1. Task Status
PASS / PARTIAL / BLOCKED / NOT READY

2. Task Title
<short task name>

3. Branch
<branch name>

4. Latest Commit
SHA: <commit sha>
Message: <commit message>

5. Push Result
<success / failed>

6. Files Changed
- <file 1>
- <file 2>

7. Exact Flow Changed
- <flow 1>
- <flow 2>

8. Build Result
- Backend: <command + result>
- Frontend: <command + result>

9. Runtime Result
- <checkpoint result lines>

10. Known Gaps
- <gap 1>
- <gap 2>

11. Review Request
Ready for gate review in primary repo.
```

## Rule 5 - No Fake Completion

Do not say `PASS` unless:

- code is committed
- code is pushed
- required verification was actually performed

If runtime was not verified, say so directly.

## Rule 6 - No Hidden Follow-Up Work

After pushing a completed task:

- stop
- report
- wait for review or next task

Do not quietly continue editing the same branch after reporting completion.

## Rule 7 - Commit Message Standard

Use clean, scoped commit messages:

- `feat(clinic): add patient booking center consultation entry`
- `fix(booking): restore doctor profile health profile binding`
- `chore(review): remove non-product artifacts from secondary branch`

Avoid vague messages like:

- `update code`
- `fix bug`
- `done task`

## Rule 8 - Reviewer Is the Gate

Pushing to the secondary repo does not mean the task is accepted.

Acceptance happens only after gate review in the primary workflow.

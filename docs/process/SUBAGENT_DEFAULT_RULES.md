# Subagent Default Rules

These rules are mandatory for any coding agent working in the secondary repository.

## 1. Scope Discipline

- Do only the assigned batch.
- Do not expand scope on your own.
- Do not refactor unrelated files.

## 2. Reporting Discipline

- Report only what was actually changed.
- Report only what was actually built.
- Report only what was actually tested.
- Use `PASS`, `PARTIAL`, `BLOCKED`, or `NOT TESTED` honestly.

## 3. No Fake Completion

- Build success does not mean runtime success.
- Code-level review does not mean feature completion.
- If a checkpoint was not tested, say `NOT TESTED`.

## 4. No Junk Files

Do not commit or include:

- coordination `.md` notes
- scratch scripts
- exported diffs
- screenshots
- log files
- dump files
- temporary debug artifacts

Unless the user explicitly asked for them.

## 5. File Safety

- Touch the minimum number of files needed.
- List every changed file explicitly.
- If you changed anything outside the requested scope, declare it.

## 6. Build First, Claim Later

Before reporting `PASS`, run the required build commands for the affected modules.

## 7. Human-in-the-Loop Rule

- Do not claim manual QA unless the human actually performed it.
- Stop at the required checkpoint when the task asks for human testing.

## 8. Promotion Rule

Assume your code will be reviewed file-by-file before entering the primary repo.
Write and report accordingly.

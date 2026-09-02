# Agent orchestration

Use the primary agent as the coordinator. Delegate planning, review, and implementation to the project-scoped custom agents below.

## Planning

- Before any non-trivial implementation, delegate execution-plan creation to `sol_planner` and wait for its result.
- Treat a change as non-trivial when it spans multiple files, changes behavior or architecture, introduces a dependency or migration, or has meaningful failure risk.
- The primary agent must reconcile the plan with the user's request before implementation begins.
- Skip delegation for tiny, obvious edits where a separate plan would add no useful information.

## Implementation

- Break an accepted plan into the smallest coherent units that can be implemented and verified independently.
- Use `terra_worker` by default for focused coding, debugging, tests, and other implementation work.
- Use `luna_worker` only for narrow, mechanical, low-risk work with explicit acceptance criteria, such as a small repetitive edit, a localized test addition, formatting, or straightforward documentation changes.
- Give each worker precise file or component ownership, constraints, and validation criteria.
- Avoid concurrent edits to overlapping files. Run write-heavy workers sequentially unless their ownership is disjoint.
- The primary agent integrates worker results, resolves conflicts, and runs proportionate end-to-end validation.

## Review

- Delegate substantive code review and final review of non-trivial changes to `sol_reviewer`; wait for its result before declaring completion.
- `sol_reviewer` is read-only. It must lead with concrete findings ordered by severity and cite relevant files and lines.
- If the reviewer finds actionable defects, delegate each sufficiently small fix to `terra_worker` or `luna_worker` according to the rules above, then request a focused re-review when risk warrants it.
- For trivial, low-risk edits, the primary agent may perform the final check without spawning a reviewer.

## Delegation discipline

- Do not spawn agents merely to restate the task or duplicate work.
- Prefer one well-scoped agent over several overlapping agents.
- Parallelize only independent work that materially benefits from concurrency.
- Always summarize delegated results and verification in the final response.

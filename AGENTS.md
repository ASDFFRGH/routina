# Agent orchestration

Use the primary agent as the coordinator. Optimize for the smallest number of agent calls that can complete the task safely and correctly.

The default workflow is:

`primary -> worker -> primary validation`

Planning and review agents are optional escalation paths, not mandatory steps for ordinary implementation.

## Core principles

- Prefer fewer, broader, well-scoped delegations over many small delegations.
- Do not delegate work that the primary agent can complete reliably with little effort.
- Avoid having multiple agents reread the same repository context unless the expected quality benefit is meaningful.
- Reuse existing plans, findings, and repository knowledge instead of asking another agent to rediscover them.
- Keep delegated prompts concise and include only the files, constraints, and context needed for that unit of work.
- Do not spawn agents merely to restate, summarize, or confirm another agent's work.
- Avoid concurrent write-heavy agents unless file ownership is clearly disjoint.
- The primary agent owns final integration and validation.

## Task classification

Classify implementation work into one of three levels.

### Level 1: routine

Examples:

- localized bug fixes
- small feature changes
- straightforward refactors
- test additions
- documentation updates
- formatting or mechanical edits
- changes affecting only a few closely related files

Default workflow:

`primary -> terra_worker or luna_worker -> primary validation`

Do not invoke `sol_planner` or `sol_reviewer` by default.

### Level 2: substantial

Examples:

- a feature spanning several related components
- behavior changes with moderate regression risk
- non-trivial refactors
- changes requiring coordinated updates across multiple modules
- work where implementation direction is not obvious

The primary agent should first determine whether it can create a sufficiently reliable plan itself.

Use `sol_planner` only when at least one of the following is true:

- architectural choices materially affect the implementation
- there are several plausible approaches with meaningful tradeoffs
- the change touches unfamiliar or complex subsystems
- failure would be costly to diagnose or undo
- the user explicitly requests a detailed implementation plan

Typical workflow:

`primary -> optional sol_planner -> terra_worker -> primary validation`

Use `sol_reviewer` only when the completed change still has meaningful residual risk.

### Level 3: high risk

Examples:

- architecture changes
- database or schema migrations
- security-sensitive changes
- authentication or authorization changes
- payment or financial logic
- destructive data operations
- concurrency or distributed-system behavior
- public API compatibility changes
- large cross-cutting refactors
- changes with difficult rollback or significant production impact

Default workflow:

`primary -> sol_planner -> terra_worker -> primary validation -> sol_reviewer -> primary`

For high-risk changes, wait for the planner before implementation and wait for the reviewer before declaring completion.

## Planning

Do not delegate planning solely because a task touches multiple files.

Use `sol_planner` when planning complexity or failure risk justifies the extra model call.

When invoking `sol_planner`:

- ask for an executable plan, not a restatement of the request
- provide only relevant repository context
- require identification of affected components, implementation order, validation, and major risks
- ask it to avoid unnecessary scope expansion

The primary agent must reconcile the plan with the user's request before implementation.

If a usable plan already exists in the conversation or repository, reuse it rather than creating another planning call unless circumstances changed materially.

## Implementation

Use `terra_worker` as the default implementation agent for work requiring coding judgment.

Use `luna_worker` for narrow, mechanical, low-risk work with explicit acceptance criteria, including:

- repetitive edits
- localized test additions
- formatting
- simple documentation changes
- renames
- straightforward configuration changes

### Delegation granularity

Do not automatically split work into the smallest possible units.

Prefer the largest coherent unit that:

- has clear ownership
- can be implemented safely by one worker
- can be validated independently
- does not create an unnecessarily large context window

A worker may own multiple related files when they form one logical change.

Split work only when:

- components are genuinely independent
- different expertise is required
- the combined task would be too large or ambiguous
- parallelism provides a meaningful benefit
- separate validation boundaries reduce risk

Avoid repeated worker calls for adjacent edits that could reasonably be completed in one pass.

### Worker prompts

Every worker prompt should include:

- the exact goal
- relevant file or component ownership
- important constraints
- acceptance criteria
- required validation commands when known

Do not send broad repository dumps or unrelated prior discussion.

Ask workers to inspect additional files only when needed.

## Review

The primary agent performs the default final review for routine and moderate-risk work.

Use `sol_reviewer` only when a second high-capability review is likely to catch defects worth the additional cost.

Typical triggers include:

- security-sensitive logic
- migrations
- public API changes
- complex state transitions
- concurrency
- substantial architectural changes
- large diffs with non-obvious interactions
- implementation where tests cannot provide sufficient confidence

`sol_reviewer` is read-only.

It must:

- lead with concrete findings
- order findings by severity
- cite relevant files and lines
- distinguish correctness issues from optional improvements
- avoid suggesting unrelated refactors

Do not request a second review merely to confirm that the first review passed.

If the reviewer finds actionable defects:

- let the primary agent fix trivial issues directly when appropriate
- delegate focused fixes to `terra_worker` or `luna_worker` when implementation work is meaningful
- request re-review only when the fix affects the original high-risk area or introduces new meaningful risk

## Validation

The primary agent owns end-to-end validation.

Use proportionate validation:

- run targeted tests first
- run broader tests only when the affected surface justifies them
- avoid repeatedly running expensive full suites after every small edit
- reuse recent successful validation when no relevant code changed afterward

Before declaring completion, the primary agent should confirm:

- requested behavior is implemented
- relevant tests or checks pass
- no obvious unintended changes remain
- delegated work has been integrated correctly

## Context and token efficiency

Minimize repeated context transmission between agents.

- Summarize prior findings instead of forwarding entire transcripts.
- Provide file paths and focused excerpts instead of large repository snapshots.
- Do not ask multiple agents to independently explore the same code unless comparison is explicitly valuable.
- Reuse the primary agent's repository investigation when possible.
- Avoid copying long command outputs into delegated prompts unless they are directly relevant.
- Prefer one implementation pass plus targeted fixes over repeated speculative passes.

When a worker returns enough information for the primary agent to continue, do not spawn another agent just to interpret that result.

## Cost-aware escalation

Use the least expensive agent capable of completing the task reliably.

Preferred order:

1. primary agent directly, for trivial coordination or edits
2. `luna_worker`, for mechanical low-risk implementation
3. `terra_worker`, for normal implementation and debugging
4. `sol_planner`, only when planning complexity warrants it
5. `sol_reviewer`, only when independent high-quality review materially reduces risk

Do not use `sol_planner` and `sol_reviewer` together for ordinary changes.

For a typical feature or bug fix, aim for no more than one worker delegation unless additional calls are justified by concrete findings.

## Parallelism

Parallelize only when work is independent and the expected time saving is meaningful.

Safe examples:

- one worker modifies backend files while another modifies unrelated documentation
- separate workers investigate independent failures without editing overlapping files

Avoid parallel write-heavy agents when:

- they may touch the same files
- one change depends on another
- integration cost may exceed the benefit

## Completion reporting

In the final response, briefly summarize:

- what was changed
- which agents were used, if any
- important validation performed
- any remaining risks or limitations

Do not provide a verbose agent-by-agent transcript unless the user requests it.

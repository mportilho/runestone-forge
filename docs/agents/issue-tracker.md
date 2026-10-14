# Issue tracker: GitHub

Issues, specs, and PRDs for this repo live as GitHub issues in `mportilho/runestone-forge`. Use the `gh` CLI for all operations.

## Conventions

- **Create an issue**: `gh issue create --title "..." --body "..."`. Use a heredoc for multi-line bodies.
- **Read an issue**: `gh issue view <number> --comments`, filtering comments by `jq` and also fetching labels.
- **List issues**: `gh issue list --state open --json number,title,body,labels,comments` with appropriate `--label` and `--state` filters.
- **Comment on an issue**: `gh issue comment <number> --body "..."`.
- **Apply or remove labels**: `gh issue edit <number> --add-label "..."` or `--remove-label "..."`.
- **Close an issue**: `gh issue close <number> --comment "..."`.

Infer the repository from `git remote -v`; `gh` does this automatically when run inside the clone. Pass `--repo mportilho/runestone-forge` when running elsewhere.

## Pull requests as a triage surface

**PRs as a request surface: no.**

Triage labels and state transitions apply to GitHub Issues only. Collaborators can still review PRs normally.

GitHub shares one number space across issues and pull requests. Resolve an ambiguous number with `gh pr view <number>` and fall back to `gh issue view <number>`.

## Skill operations

- When a skill says to publish to the issue tracker, create a GitHub issue.
- When a skill says to fetch a ticket, use `gh issue view <number> --comments` and include its labels.

## Wayfinding operations

Used by `/wayfinder`. The **map** is a single issue with **child** issues as tickets.

- **Map**: a single issue labelled `wayfinder:map`, holding the Notes / Decisions-so-far / Fog body. `gh issue create --repo mportilho/runestone-forge --label wayfinder:map`.
- **Child ticket**: an issue linked to the map as a GitHub sub-issue (`gh api` on the sub-issues endpoint). Where sub-issues are not enabled, add the child to a task list in the map body and put `Part of #<map>` at the top of the child body. Labels: `wayfinder:<type>` (`research`/`prototype`/`grilling`/`task`). Once claimed, the ticket is assigned to the driving dev.
- **Blocking**: GitHub's native issue dependencies are the canonical, UI-visible representation. Add an edge with `gh api --method POST repos/mportilho/runestone-forge/issues/<child>/dependencies/blocked_by -F issue_id=<blocker-db-id>`, where `<blocker-db-id>` is the blocker's numeric database id (`gh api repos/mportilho/runestone-forge/issues/<n> --jq .id`, not the `#number` or `node_id`). GitHub reports `issue_dependencies_summary.blocked_by` for open blockers only. Where dependencies are not available, fall back to a `Blocked by: #<n>, #<n>` line at the top of the child body. A ticket is unblocked when every blocker is closed.
- **Frontier query**: list the map's open children, scoped to the map's sub-issues or task list, drop any with an open blocker or an assignee; first in map order wins.
- **Claim**: `gh issue edit <n> --repo mportilho/runestone-forge --add-assignee @me`.
- **Resolve**: `gh issue comment <n> --repo mportilho/runestone-forge --body "<answer>"`, then `gh issue close <n> --repo mportilho/runestone-forge`, then append a context pointer to the map's Decisions-so-far.

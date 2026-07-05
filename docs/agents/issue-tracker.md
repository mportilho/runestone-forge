# Issue tracker: GitHub

Issues and PRDs for this repo live as GitHub issues in `mportilho/runestone-forge`. Use the `gh` CLI for all operations.

## Conventions

- **Create an issue**: `gh issue create --repo mportilho/runestone-forge --title "..." --body "..."`. Use a heredoc for multi-line bodies.
- **Read an issue**: `gh issue view <number> --repo mportilho/runestone-forge --comments`, filtering comments by `jq` and also fetching labels.
- **List issues**: `gh issue list --repo mportilho/runestone-forge --state open --json number,title,body,labels,comments --jq '[.[] | {number, title, body, labels: [.labels[].name], comments: [.comments[].body]}]'` with appropriate `--label` and `--state` filters.
- **Comment on an issue**: `gh issue comment <number> --repo mportilho/runestone-forge --body "..."`
- **Apply / remove labels**: `gh issue edit <number> --repo mportilho/runestone-forge --add-label "..."` / `--remove-label "..."`
- **Close**: `gh issue close <number> --repo mportilho/runestone-forge --comment "..."`

Infer the repo from `git remote -v` when running inside this clone; pass `--repo mportilho/runestone-forge` when running elsewhere.

## Pull requests as a triage surface

**PRs as a request surface: no.**

Do not pull external PRs into the `/triage` issue queue. Collaborators can still review PRs normally, but triage labels and state transitions apply to GitHub Issues only.

If this is changed to `yes`, PRs run through the same labels and states as issues, using the `gh pr` equivalents:

- **Read a PR**: `gh pr view <number> --repo mportilho/runestone-forge --comments` and `gh pr diff <number> --repo mportilho/runestone-forge` for the diff.
- **List external PRs for triage**: `gh pr list --repo mportilho/runestone-forge --state open --json number,title,body,labels,author,authorAssociation,comments` then keep only `authorAssociation` of `CONTRIBUTOR`, `FIRST_TIME_CONTRIBUTOR`, or `NONE`; drop `OWNER`, `MEMBER`, and `COLLABORATOR`.
- **Comment / label / close**: `gh pr comment`, `gh pr edit --add-label` / `--remove-label`, `gh pr close`.

GitHub shares one number space across issues and PRs, so a bare `#42` may be either. Resolve with `gh pr view 42 --repo mportilho/runestone-forge` and fall back to `gh issue view 42 --repo mportilho/runestone-forge`.

## When a skill says "publish to the issue tracker"

Create a GitHub issue in `mportilho/runestone-forge`.

## When a skill says "fetch the relevant ticket"

Run `gh issue view <number> --repo mportilho/runestone-forge --comments`.

## Wayfinding operations

Used by `/wayfinder`. The **map** is a single issue with **child** issues as tickets.

- **Map**: a single issue labelled `wayfinder:map`, holding the Notes / Decisions-so-far / Fog body. `gh issue create --repo mportilho/runestone-forge --label wayfinder:map`.
- **Child ticket**: an issue linked to the map as a GitHub sub-issue (`gh api` on the sub-issues endpoint). Where sub-issues are not enabled, add the child to a task list in the map body and put `Part of #<map>` at the top of the child body. Labels: `wayfinder:<type>` (`research`/`prototype`/`grilling`/`task`). Once claimed, the ticket is assigned to the driving dev.
- **Blocking**: GitHub's native issue dependencies are the canonical, UI-visible representation. Add an edge with `gh api --method POST repos/mportilho/runestone-forge/issues/<child>/dependencies/blocked_by -F issue_id=<blocker-db-id>`, where `<blocker-db-id>` is the blocker's numeric database id (`gh api repos/mportilho/runestone-forge/issues/<n> --jq .id`, not the `#number` or `node_id`). GitHub reports `issue_dependencies_summary.blocked_by` for open blockers only. Where dependencies are not available, fall back to a `Blocked by: #<n>, #<n>` line at the top of the child body. A ticket is unblocked when every blocker is closed.
- **Frontier query**: list the map's open children, scoped to the map's sub-issues or task list, drop any with an open blocker or an assignee; first in map order wins.
- **Claim**: `gh issue edit <n> --repo mportilho/runestone-forge --add-assignee @me`.
- **Resolve**: `gh issue comment <n> --repo mportilho/runestone-forge --body "<answer>"`, then `gh issue close <n> --repo mportilho/runestone-forge`, then append a context pointer to the map's Decisions-so-far.

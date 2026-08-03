---
name: agents-bridge
description: Load and apply this repository AGENTS.md + WWG truth chain for Grok. Use at session start, before implementation, or when the user asks to load project rules.
user-invocable: true
disable-model-invocation: false
metadata:
  short-description: "Load AGENTS.md + WWG truth chain"
---

# Agents Bridge — Spring Boot API Template

## Steps

1. Resolve repo root (directory containing `AGENTS.md` and `.wwg/`).
2. Read in order:
   1. `AGENTS.md` — **including** Mandatory Per-Prompt Rule and embedded Meta-Prompt Template v2 (sections 0–17)
   2. `agent-meta-prompt-template-v2.md` only if you need the standalone fill-in form (must match AGENTS.md embed)
   3. `.wwg/RESOLVER.md`
   4. `.wwg/wiki/project-truth.md`
   5. `.wwg/wiki/terminology.md`
   6. `.wwg/workspace/current-task.md`
   7. Additional WWG surfaces required by the task (architecture, skills, governance)
3. For every meaningful user prompt: instantiate **Section 0 Task Contract** before plan/edit (see AGENTS.md Mandatory Per-Prompt Rule).
4. Confirm to the user: root path, execution mode selected, and which files were loaded.
5. Do not invent product domain beyond the template unless requested.

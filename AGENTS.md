# AGENTS.md

本 repo 是「本地單進程互動小說引擎」。長期規則：

- 只做 Kotlin/JVM CLI、SQLite、JSON 檔案協議。
- 不建立 web server，不開 port。
- 不使用 FastAPI、Express、Ktor server、Spring server、React、Next.js、Docker、Android UI 或 Rust。
- 不接 OpenAI API；AI 回合結果由人工或外部工具產生 JSON 檔案。
- 所有狀態變更必須走 `AiTurnResultValidator` 和 `StateReducer`。
- 多回合 runtime 的協議、驗收與限制見 `docs/MVP2_RELIABLE_MULTI_TURN_SPEC.md`。
- 角色記憶、知識邊界、context budget 與情緒因果規則見 `docs/MVP3_MEMORY_KNOWLEDGE_SPEC.md` 與 `docs/MEMORY_AND_KNOWLEDGE.md`。
- AI 只能提出 `memoryCandidates` / `knowledgeUpdates`；正式 memory、knowledge、recall log 與 emotion 變化只可由 `resolve-turn` 在 validator 通過後寫入。

常用檢查：

```powershell
./gradlew.bat clean test
```

## MVP-4 Scene Thread Runtime

- Use only additive migration `004_scene_thread_runtime.sql`; do not edit `001`, `002`, or `003`.
- Keep scene, thread, world-event, time and transition writes behind `resolve-turn` validation and the SQLite transaction.
- Do not let `story_threads.director_goal` become a character goal, character knowledge, action intent, visible action, or hidden motive.
- Scene `READY_TO_CLOSE` means a transition is allowed, not forced.
- Characters may only act from their own goals, emotions, memories, knowledge, current location, reachable items and available actions.
- AI/Codex may draft JSON result files, but must not write SQLite directly or bypass `contextHash`, `stateVersion`, `TransitionValidator`, or `NarrativeContinuityValidator`.

## MVP-5 Setting Pack Runtime

- Use only additive migration `005_setting_pack_runtime.sql`; do not edit `001`, `002`, `003`, or `004`.
- Setting packs must flow through parse, validate, diff, impact preview and transaction apply.
- Codex may generate setting pack JSON, but must not directly modify formal SQLite data with SQL, `sqlite3`, or ad hoc scripts.
- Do not let `author_only` secrets enter character initial knowledge or character-facing turn context.
- Do not silently patch existing canon while a session is active; preview must report blockers and apply must reject.
- Turn context may include relevant setting excerpts only, never the full setting pack or full dependency graph.
- See `docs/MVP5_SETTING_PACK_DEPENDENCY_SPEC.md` and `docs/SETTING_PACKS_AND_DEPENDENCIES.md`.

## Pilot-1 Stabilization

- Do not move to MVP-6 while Pilot-1 is FAIL.
- Keep final `ContextPacket` JSON at or below `24576` actual UTF-8 bytes; do not raise the limit.
- Setting-pack opening characters must provide `initialRuntimeTemplate`; `start-session` initializes missing opening runtime states from it inside the session transaction.
- Use `audit-context` before drafting a manual AI result.
- Use `pilot-preflight` before handing a fresh Pilot session to the author.
- Use `export-session-evidence` for read-only Pilot evidence.
- Use `explain-context-budget` when `prepare-turn` is close to or over the 24576-byte ceiling; it is read-only and reports section bytes, duplicate fragments, mandatory bytes and trimming simulation.
- Context budget fixes must use deterministic trimming, compact mandatory views and dynamic allocation; never raise `contextBudget.maxBytes` above 24576.
- Do not reuse old Pilot sessions as formal rerun evidence.
- Codex must not directly write SQLite or bypass Validator, `contextHash`, `stateVersion`, or transactions.
- See `docs/PILOT1_STABILIZATION_SPEC.md` and `docs/PILOT1_STABILIZATION.md`.

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

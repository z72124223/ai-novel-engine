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
- Final `ContextPacket` output must pass the top-level allowlist and must not include diagnostics such as largest paths, duplicate groups, removed/retained items, or budget-forensics traces. Those belong in `explain-context-budget` or `audit-context` sidecar reports.
- Do not output legacy aliases beside canonical fields; `retrievedMemories` is the runtime memory surface and the old `characterMemories` alias is omitted from finalized turn context.
- `contextHash` and `contextBudget.actualBytes` must be computed after normalization and after empty/default fields, legacy aliases, and diagnostics have been removed from the final payload.
- A turn context that fits bytes but drops a required semantic floor is not acceptable. Explicit memory cues must be satisfied by real `retrievedMemories`, relevant beliefs by `knowledgeClaims`, transition questions by location/continuity constraints, and referenced items/techniques by visible requirements/costs/limitations.
- If the semantic floor cannot fit, fail with `CONTEXT_QUALITY_UNSATISFIABLE` before creating a pending context. Do not use recent facts/events as fake recalled memories, do not load unrelated settings to fill a quota, and do not raise the `24576` byte hard max.
- If a turn explicitly uses a technique, item, or weapon, final context must include a Tier 1 `activeCapabilityContracts` card. Names/descriptions alone do not authorize use; `resolve-turn` must validate `capabilityUseChecks` and cost state deltas.
- Do not reuse old Pilot sessions as formal rerun evidence.
- Codex must not directly write SQLite or bypass Validator, `contextHash`, `stateVersion`, or transactions.
- See `docs/PILOT1_STABILIZATION_SPEC.md`, `docs/PILOT1_STABILIZATION.md`, and `docs/CONTEXT_RELEVANCE_FLOOR.md`.

## MVP-8 試用層

- MVP-8 = **試用層**（不是 Android）。規格：`docs/MVP8_PROVISIONAL_LAYER_SPEC.md`（中文正文）。
- 世界四層：鎖定層、留白層、試用層、定稿層。試用不得覆寫鎖定層。
- AI 可提案試用人物/傳聞等；僅在 `resolve-turn` 驗證通過後寫入，禁止直接改資料庫。
- 升格雙向：作者可升格；AI 可提交升格建議（須滿資格四選三）。正式寫入仍須作者確認。
- 升格須走：試用清單 → 設定包補丁草稿 → 驗證 → 預覽 → 正式匯入。
- 擋工具人：無慾望、無代價、無盲點、只送情報的試用角色。
- 作者限定秘密不得進試用提案或角色認知。
- 塵行預設**讀者模式**；升格可無限期延後。留白區見 `35_provisional_layer_charter_v1.json`。
- 資料庫只新增 `009_provisional_layer.sql`，不改 001–008。

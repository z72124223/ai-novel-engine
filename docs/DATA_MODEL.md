# Data Model

SQLite schema 位於 `app/src/main/resources/migrations/001_init.sql`。

## Core Tables

- `projects`：作品/劇本專案，保存 active scene 與 current turn。
- `characters`：角色 profile。
- `character_states`：角色可變狀態，例如位置、情緒、健康、inventory、flags。
- `character_memories`：角色記憶。
- `world_rules`：世界規則與優先級。
- `factions`：勢力。
- `locations`：地點。
- `items`：物品。
- `techniques`：角色技巧或可用行動方式。
- `scenes`：當前或歷史場景。
- `scene_contracts`：場景約束，包含允許揭露的 secrets 與禁止揭露片語。
- `facts`：已知事實。
- `secrets`：未揭露或已揭露秘密。
- `events`：已發生事件。
- `turn_logs`：每回合 context、result、narrative 與是否有狀態變化。
- `state_deltas`：逐項記錄 result 內的 delta operation。
- `dependency_edges`：素材之間的依賴關係。
- `task_queue`：後續待辦、劇情任務或人工處理事項。
- `story_sessions`：MVP-2 多回合 session，保存 current scene、next turn number、state version 與 status。

## JSON Columns

v1 使用 JSON 欄位保存彈性資料，例如 inventory、flags、operation payload。這些欄位仍由 Kotlin model 控制讀寫形狀。

## MVP-2 Runtime Columns

`002_multi_turn_runtime.sql` 不修改 `001_init.sql`，而是新增 session 與 runtime 欄位：

- `turn_logs.session_id/context_id/result_id/base_state_version/context_hash/turn_summary/validation_status`
- `scenes.progress_json/completed_requirements_json`

`result_id` 唯一；同一 `session_id + turn_number` 只能有一個正式 `applied` 回合。

## MVP-3 Memory And Knowledge

`003_memory_knowledge_runtime.sql` 只做 additive schema 變更：

- 擴充 `character_memories`：owner、memory type、details、source、subject、tags、trigger tags、emotional signature、confidence、reliability、recall metadata、decay、status、canon flag、supersedes relation。
- 新增 `character_knowledge`：角色對命題的 `KNOWN`、`BELIEVED`、`SUSPECTED`、`DOUBTED`、`MISREMEMBERED`、`UNKNOWN` 狀態與 confidence。
- 新增 `memory_recall_logs`：successful resolve 後記錄每回合哪些記憶被選入 context、分數與 reasons。

World facts 是 canonical truth；character knowledge 是角色認知；character memory 是角色經驗或聽聞，可能偏誤或不完整。世界資料庫知道的 fact 不會自動成為角色 knowledge。

## MVP-4 Scene Thread Runtime

`004_scene_thread_runtime.sql` is additive and does not modify migrations `001`, `002`, or `003`.

New tables:

- `story_threads`
- `session_thread_states`
- `scene_requirements`
- `session_scene_requirement_states`
- `scene_instances`
- `location_connections`
- `scene_transitions`
- `pending_scene_transitions`
- `scheduled_world_events`

`story_sessions` gains:

- `current_scene_instance_id`
- `world_time`
- `elapsed_minutes`

`scenes` remain definitions; `scene_instances` store per-session visits. Re-entering a scene creates a new instance and increments `visit_number`.

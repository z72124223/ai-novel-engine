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

## JSON Columns

v1 使用 JSON 欄位保存彈性資料，例如 inventory、flags、operation payload。這些欄位仍由 Kotlin model 控制讀寫形狀。

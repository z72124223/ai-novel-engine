# Current Route Gate V6 — GPT 規劃／Codex 實作

Status: `ACTIVE_SINGLE_CURRENT_ENTRY_GATE`

Supersedes:

- `docs/CURRENT_ROUTE_GATE_V5.md`
- `docs/GPT_WORK_CODEX_TRIPARTITE_COLLABORATION_V1.md`
- `.work/work_controller.md`
- 所有要求獨立 WORK 執行 DIALOGUE-01 的文件

舊文件保留為歷史，不刪除。

## 1. 現在唯一工作

```text
DIALOGUE-01 V2 — 華語真人對白定稿
```

## 2. 執行模式

```text
GPT 已完成規劃
→ Codex 一次完成實作
```

沒有獨立 WORK 階段。

## 3. 必讀順序

1. `PROJECT_STATE.md`
2. `.codex/dialogue01_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V6.md`
4. `docs/GPT_CODEX_COLLABORATION_V2.md`
5. `docs/work-packages/DIALOGUE-01_HUMAN_CHINESE_DIALOGUE_FINALIZATION_V2.md`
6. `docs/handoffs/DIALOGUE01_CODEX_ONLY_HANDOFF.md`
7. `docs/handoffs/DIALOGUE01_MERGE_PLAN_V2.md`
8. SCREEN-01 選定劇本、對白、Dialogue Bible、Shot、Pilot、SRT
9. 需要的程式與測試

## 4. 分支

來源：

```text
codex/screen01-video-first-pilot
513c9d9ac1b1e168ac7c1217ea3f6ba581181719
```

實作：

```text
codex/dialogue01-human-voice-finalization
```

## 5. Codex 不需詢問的事

- 某句保留、縮短、改寫或改成動作；
- 哪個子評審負責哪項檢查；
- 對白 ID、檔名、JSON 結構；
- 字幕時間；
- Shot 引用；
- 測試與 Hash；
- 普通 merge；
- 非 Canon Pilot 內的口語取捨。

## 6. 必須停止的事

- 正式 Canon；
- 永久命運或戀愛；
- 正式美術；
- 付費 Provider／API Key；
- 無法避免的破壞性 Git；
- 必須弱化 Transaction、Knowledge、Hash 或批准；
- 必須 force push；
- 修改核心情節才能解決對白問題；
- 同一核心阻塞經一次 Medium、一次 High 仍失敗。

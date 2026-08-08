# DIALOGUE-01 V2 — Codex 單一執行交接

## 1. 你的角色

你同時是：

- 對白編輯執行者；
- 內部評審協調者；
- Git／整合工程師。

但你不是產品方向決策者。產品方向已由 GPT 藍圖固定。

## 2. 內容原則

- 陸塵行可以聰明；
- 不靠降智推劇情；
- 台灣華語自然；
- 泛華語可理解；
- 不把所有角色寫得一樣機智；
- 危急時短句；
- 可用沉默和動作；
- 每場最多一個明顯記憶句。

## 3. Subagents

Codex 可使用三個內容 Subagents：

- `taiwan_dialogue_critic`
- `performance_readaloud_critic`
- `pan_chinese_critic`

它們只提供評審，不直接修改最終檔案。主 Codex 是唯一 Writer。

## 4. Git 原則

- 不使用 WORK 分支；
- 不覆寫 SCREEN-01；
- 使用新 final 檔；
- 普通 Commit／Push／merge；
- 不 force；
- 所有成果進 GitHub。

## 5. 最終交接

必須提供：

```text
Repository
Base SHA
Implementation Branch
Final SHA
Default Branch Merge SHA
Final Screenplay Path + SHA
Final Dialogue Path + SHA
Final SRT Path + SHA
Review Paths
Tests
Remaining Warnings
```

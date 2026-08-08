# GPT／CODEX 雙方協作憲章 V2

Status: `ACTIVE_MANDATORY_COLLABORATION_CHARTER`

## 1. 模式

DIALOGUE-01 採用：

```text
GPT 規劃
→ Codex 實作
```

取消獨立 WORK 角色。舊的 GPT／WORK／CODEX 三方文件若存在，只保留為歷史證據，不再控制目前工作。

## 2. GPT 負責什麼

GPT 已完成：

- 確認影片／動畫是主要終點；
- 確認 GitHub 遠端是團隊最高權威；
- 定義主角不靠降智推劇情；
- 定義繁體中文、自然台灣華語與泛華語可讀性；
- 定義「像 AI 對話」的問題；
- 定義允許修改與禁止修改；
- 定義產物、評審、測試、Git 與合併順序；
- 定義停止條件。

本憲章進 Git 後，GPT 的 DIALOGUE-01 規劃階段完成。GPT 不逐句遙控 Codex，也不要求 Codex 每一小步回來詢問。

## 3. Codex 負責什麼

Codex 一次負責：

- Git 同步與安全分支；
- 逐句對白稽核；
- AI 模擬朗讀；
- 對白改寫；
- 三個獨立內容子評審；
- 最多一次修訂；
- 影視劇本、對白軌、Shot、SRT 同步；
- Hash、引用、JSON、測試；
- Git Commit、Push、合併；
- 最終報告。

Codex 可以使用內部 Subagents，但它們都屬於 Codex 工作階段，不是另一個獨立 WORK 團隊。

## 4. GitHub 權威

GitHub 遠端是方向、版本、對白、劇本、評審、決策、合併與交接的最高權威。

本機、SQLite、終端、`out/**`、臨時檔案都只是執行環境。重要成果沒有 Commit／Push 就不算完成。

## 5. 共同禁止

- 不覆寫 SCREEN-01 歷史產物；
- 不改故事主要情節；
- 不讓陸塵行為了戲劇故意變笨；
- 不升格 Canon；
- 不生成圖片／影片；
- 不呼叫 Provider；
- 不寫下一章；
- 不新增互動遊戲；
- 不 force push。

## 6. 成功定義

1. 對白比 SCREEN-01 更像真人；
2. 角色聲音可以區分；
3. 台灣華語自然，其他華語地區也能理解；
4. 劇本、對白、Shot、字幕一致；
5. 所有成果都在 GitHub；
6. 使用者不需逐步管理 Codex。

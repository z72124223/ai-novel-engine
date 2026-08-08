# DIALOGUE-01 V2 合併計畫

## 1. SCREEN-01

來源：

```text
origin/codex/screen01-video-first-pilot
513c9d9ac1b1e168ac7c1217ea3f6ba581181719
```

安全納入預設分支：

- 可 fast-forward 就 fast-forward；
- 否則正常 merge；
- 不 rebase 已 Push 歷史；
- 不 force。

## 2. 藍圖

本 V2 藍圖已寫入預設分支。

## 3. 實作分支

從最新預設分支建立：

```text
codex/dialogue01-human-voice-finalization
```

不建立、也不使用 WORK 分支。

若舊的：

```text
work/dialogue01-human-voice-pass
```

已存在，保留歷史但忽略，不刪除、不合併。

## 4. Codex 完成後

1. Push implementation branch；
2. fetch；
3. 確認預設分支沒有未知衝突；
4. 正常 merge implementation branch；
5. 更新 `PROJECT_STATE.md`；
6. 普通 Push；
7. 保留實作分支供審查。

## 5. 禁止

- force push；
- squash 掉主要實作歷史；
- 覆寫 SCREEN-01；
- 合併本機未 Push 成果；
- 合併後自動開始圖片或影片。

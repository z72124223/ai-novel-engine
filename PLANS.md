# Plan

## Goal

修復既有非阻塞技術債：`:app:installDist` 產物必須能在 Windows script 中啟動 CLI，並能透過 distribution 讀到 packaged resources（封裝資源）中的 SQLite migrations（資料庫遷移檔）。

## Global Strategy

只調整 Gradle packaging（打包 / 發佈）設定，讓 application distribution（應用程式發佈產物）包含主程式 JAR（Java 封裝檔）與既有 resources。先修最小必要點，再用 clean test、installDist、distribution CLI 實跑驗證。

## Non-Goals

不新增功能。不開 MVP-7。不修改 SQLite schema。不修改 Validator、StateReducer、stateVersion、contextHash 邊界。不新增 server、port、API、Android、Rust、Docker、embedding/vector DB。不處理或 commit `data/`、`out/` ignored 產物。不 push、不 merge、不修改 `mvp-6` tag。

## Scope

預期 production 設定只限 `app/build.gradle.kts`。本檔 `PLANS.md` 僅記錄本次受限修復軌跡。

## Confirmed Facts

- 目前分支已切到 `fix/install-dist-packaging`。
- 初始工作樹在切分支前為 clean。
- `mvp-6` tag 指向 commit `be527031335efe8e2b0facae0145e1bbc084f8c6`。
- 原始環境缺少 `JAVA_HOME` 與 PATH 中的 `java`，會先阻止 Gradle wrapper 執行。
- 使用臨時 JDK 後，`:app:installDist` task 成功。
- installDist 產物的 Windows script classpath 包含 `lib/app.jar`。
- installDist 產物的 `lib` 目錄目前沒有 `app.jar`。
- 實跑 installDist 產物 `bin/app.bat help` 失敗：`ClassNotFoundException: com.localnovel.engine.MainKt`。
- `app/build.gradle.kts` 已套用 `application` plugin 並設定 `mainClass` 為 `com.localnovel.engine.MainKt`。
- `app/build.gradle.kts` 目前停用所有 `Jar` task。
- migrations 位於 `app/src/main/resources/migrations/`，repository 透過 classloader resource path `migrations/<file>` 載入。
- `docs/ROADMAP.md` 已記錄 `installDist` 技術債：app jar task 被停用導致 scripts 不能跑。

## Assumptions

- 重新啟用 app module 的 JAR task 足以讓 distribution script 找到 `MainKt` 與 resources。
- 既有 `ProcessResources` 的 `doNotTrackState` 設定已足以處理 OneDrive reparse file 風險。

## Open Questions

- `clean test --no-daemon` 是否會在重新啟用 JAR task 後仍保持 220 tests passing。
- distribution CLI 的 `init-db` 是否能從 packaged JAR 讀到全部 migrations。

## Implementation Steps

- [x] Step 1: 完成環境與根因診斷。
- [x] Step 2: 最小修改 Gradle packaging 設定，恢復 application distribution 需要的 app JAR。
- [x] Step 3: 驗證 tests、installDist、distribution CLI。
- [x] Step 4: 檢查 diff 與 git status，全部通過後 commit。

## Verification

- Step 2: 檢查 `git diff`，確認只碰 packaging 設定與本 plan。
- Step 3: 執行 `git diff --check`、`gradlew.bat clean test --no-daemon`、`gradlew.bat :app:installDist`、distribution CLI `help` 與臨時 db `init-db`。
- Step 4: 執行 `git status`、`git diff --stat`，再 commit。

## Risks

- 本機無全域 JDK；本輪驗證需使用暫存 JDK 與 per-command `JAVA_HOME`。
- 若 OneDrive reparse file 問題重現，可能需要保留 Jar task 但調整其 tracking 或輸入行為。

## Drift Log

- 2026-06-19: 初始 `installDist` 指令先被缺 JDK 擋住；以暫存 JDK 解除環境阻塞後，確認真正問題是 distribution script 指向不存在的 `lib/app.jar`。
- 2026-06-19: 已移除 `Jar` task 的 `enabled = false`，保留 OneDrive `doNotTrackState` 設定；目前未發現範圍漂移。
- 2026-06-19: 驗證通過：`git diff --check`、`clean test --no-daemon`、`:app:installDist`、distribution `help`、distribution `init-db`。
- 2026-06-19: commit completed on `fix/install-dist-packaging`; no push, merge, tag mutation, or MVP-7 work performed.

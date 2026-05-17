# SportLife 开发者规范

这份规范用于约束 SportLife 每一次迭代。目标是：只改用户要求的内容，保护已有数据，版本产物位置统一，GitHub 上的源码、更新日志和 Release 保持一致。

## 1. 开工前必须确认

每次修改前先做这些事：

- 查看当前工作区状态：`git status -sb`。
- 确认当前分支。默认使用 `main`，不要擅自新建分支、PR 或切换分支。
- 查看当前版本号：`app/build.gradle.kts` 中的 `versionCode` 和 `versionName`。
- 查看已有 APK：`dist/` 目录中的最高版本号。
- 查看最近更新：`CHANGELOG.md` 顶部条目。
- 先读相关代码，再动手。不要凭记忆改 UI、数据模型或发布流程。

如果工作区已有未提交改动，必须把它们当作用户已有工作处理，不要回退、覆盖或清理。

## 2. 改动范围规范

每次迭代只处理用户本次提出的问题：

- 不要顺手重构无关代码。
- 不要擅自改 UI 风格、颜色、布局基调或新增未要求的功能。
- 不要为了修一个问题引入大范围架构变化。
- 不要改动无关页面、无关数据表、无关文档。
- 不要删除用户文件，例如根目录下的 `icon.png`，除非用户明确要求。

如果发现另一个问题，先记录或说明，不要混进本次版本。

## 3. 代码实现规范

项目技术栈保持一致：

- Kotlin + Jetpack Compose。
- MVVM。
- Repository 负责数据访问。
- Room 负责本地持久化。
- UI 层保持声明式，状态由 ViewModel 管理。
- 复用已有组件、主题色、页面结构和命名方式。

UI 修改时要遵守：

- App 当前主视觉是深色/纯黑背景，不要擅自改成渐变、浅色或其他风格。
- 文案优先中文。
- 不要硬加设计稿里不存在、项目里没有规划的功能。
- 表单类输入要尽量适配手机操作，例如快捷选择、步进按钮、明确错误提示。
- 保存、删除、恢复等操作要有明确反馈。

数据修改时要遵守：

- 不能使用破坏性迁移。
- 不能让用户覆盖安装新版本后丢失打卡记录、健身计划、自定义分化、自定义标语等数据。
- 如果修改 Room 表结构，必须提升数据库版本、编写 migration、导出 schema，并验证旧版本可迁移。
- 默认数据 Seeder 不能覆盖用户已经编辑过的数据。
- 涉及备份/恢复时，要同步考虑 `DataBackupRepository` 和训练计划快照。

## 4. 版本号规范

只要是影响 App 行为、UI、数据、资源或用户可下载 APK 的修改，都必须更新版本：

- `versionCode` 每次递增 1。
- `versionName` 按已有顺序递增，例如 `2.5` 后为 `2.6`。
- 版本号修改位置：`app/build.gradle.kts`。

纯文档或开发规范修改一般不生成 APK，也不改 App 版本号；但仍然必须写入 `CHANGELOG.md`。

## 5. CHANGELOG 规范

每次修改都必须更新 `CHANGELOG.md`。

格式如下：

```markdown
## 2.6 - 2026-05-17

### 新增

- ...

### 优化

- ...

### 修复

- ...
```

要求：

- 最新版本永远写在最顶部。
- 日期使用当天日期，格式 `yyyy-MM-dd`。
- 文案必须说明用户能感知到的变化。
- 如果是修 bug，要写清楚修复了什么问题。
- 如果涉及数据迁移，要写明是否保护旧数据。
- GitHub 上的 `CHANGELOG.md` 必须和本地一致，也就是推送源码时必须包含 `CHANGELOG.md`。

## 6. 构建与 APK 产物规范

每次 App 版本更新后必须构建：

```powershell
.\gradlew.bat assembleDebug --console=plain
```

构建成功后，把 APK 复制到 `dist/`：

```powershell
Copy-Item app\build\outputs\apk\debug\app-debug.apk dist\SportLife-vX.Y.apk -Force
```

要求：

- 用户可下载版本统一放在 `dist/`。
- 文件命名统一为 `SportLife-v版本号.apk`，例如 `SportLife-v2.6.apk`。
- 不要把最终交付路径说成 `app/build/outputs/...`。
- `dist/` 是本地发布产物目录，仓库 `.gitignore` 已忽略 APK，不要把 APK 当源码提交。
- 如果已经生成错位置或错命名的 APK，要重新复制正确文件到 `dist/`。

## 7. 验证规范

每次修改至少完成：

- `.\gradlew.bat assembleDebug --console=plain` 构建成功。
- 检查 `git diff --stat`，确认没有混入无关文件。
- 检查 `git status -sb`，确认变更范围清楚。
- 检查 `dist/SportLife-vX.Y.apk` 是否存在。

如果改了界面或交互，尽量做真机/模拟器验证：

- 有 `adb` 时安装并截图验证。
- 没有 `adb` 或设备不可用时，最终说明“未做设备截图验证”的原因。

如果改了数据库：

- 验证旧数据不会被清空。
- 验证覆盖安装后的打卡记录、健身计划、自定义数据仍在。
- 验证备份/恢复相关逻辑。

## 8. GitHub 同步规范

当用户要求上传 GitHub 或发布版本时，必须做完整流程：

1. 确认当前分支是 `main`。
2. 不要擅自新建分支，不要开 PR，除非用户明确要求。
3. 提交源码改动，包含：
   - 代码改动。
   - `CHANGELOG.md`。
   - 必要的 README 或规范文档。
   - 不包含 `dist/` APK。
4. 推送到 GitHub 主分支。
5. GitHub 仓库里的 `CHANGELOG.md` 必须更新到和本地一致。
6. 创建或更新 GitHub Release：
   - tag 使用版本号，例如 `v2.6`。
   - Release 标题使用 `SportLife v2.6`。
   - Release notes 使用 `CHANGELOG.md` 中对应版本条目。
   - 上传 `dist/SportLife-v2.6.apk` 作为 Release 附件。
7. 如果 README 中有最新下载链接，要同步更新到最新 Release。

如果 GitHub CLI 不可用，需要换用可用的 GitHub connector、git push 或 API 方式完成；不能只说“本地已经好了”。

## 9. 最终回复规范

每次完成后，最终回复必须简洁说明：

- 改了什么。
- 关键文件路径。
- 版本号。
- 构建是否成功。
- APK 在 `dist/` 的准确路径。
- 如果上传 GitHub，则说明 main 分支、commit、release/tag、APK 附件是否完成。
- 如果某项验证没做，要说明原因。

不要把 `app/build/outputs/apk/debug/app-debug.apk` 当作交付文件告诉用户；它只是中间构建产物。

## 10. 发布前检查清单

App 版本发布前逐项确认：

- [ ] 改动只覆盖用户要求的范围。
- [ ] 没有擅自改 UI 风格或新增无关功能。
- [ ] `versionCode` 已递增。
- [ ] `versionName` 已顺延。
- [ ] `CHANGELOG.md` 顶部已有对应版本条目。
- [ ] 构建命令通过。
- [ ] APK 已复制到 `dist/SportLife-vX.Y.apk`。
- [ ] 数据迁移和备份逻辑已检查。
- [ ] `git diff --stat` 无异常。
- [ ] GitHub main 分支已推送。
- [ ] GitHub `CHANGELOG.md` 已同步。
- [ ] GitHub Release 已创建或更新。
- [ ] Release APK 附件已上传。

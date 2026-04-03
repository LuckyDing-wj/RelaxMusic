# RelaxMusic

RelaxMusic 是一个纯本地的 Android 音乐播放器项目，面向“本地曲库管理 + 播放体验”场景，当前基于 Jetpack Compose、Room 与 Media3 构建。项目已经从最初的单页原型演进为包含首页、曲库、列表、播放页、设置页的多页面应用，并具备本地扫描、播放控制、歌词展示、歌单管理、播放历史、主题切换和基础备份恢复能力。

## 当前能力

### 曲库导入与扫描

- 通过 Android SAF 目录选择器授权本地音乐目录
- 支持保存多个音乐目录，并可单独移除目录
- 递归扫描所选目录及全部子目录中的音频文件
- 当前支持的音频格式：`mp3`、`flac`、`wav`、`m4a`、`ogg`
- 提取标题、艺术家、专辑、时长等基础 metadata
- 使用 Room 持久化本地曲库，并在重新扫描时保留收藏与最近播放信息

### 浏览与管理

- 首页展示最近播放、快捷入口、曲库状态与目录管理
- 支持全部歌曲列表与搜索
- 支持按专辑、艺术家聚合浏览
- 支持收藏歌曲
- 支持播放历史查看
- 支持创建、重命名、删除歌单，以及向歌单添加或移除歌曲

### 播放体验

- 使用 Media3 ExoPlayer 播放本地音频
- 提供独立播放页，支持播放 / 暂停、上一首、下一首、拖动进度
- 支持顺序播放、单曲循环、列表循环、随机播放
- 支持睡眠定时停止播放
- 支持读取音频内嵌封面并在播放页展示
- 支持读取外部 `.lrc` 歌词或音频内嵌歌词，并在播放页滚动高亮
- 已接入前台播放服务、MediaSession 与通知栏基础控制

### 设置与数据

- 支持浅色 / 深色 / 跟随系统主题切换
- 支持导出和导入应用备份
- 当前备份内容包含已授权目录配置与歌单数据

## 技术栈

- Kotlin
- Jetpack Compose + Material 3
- AndroidX Navigation Compose
- Room
- Media3 ExoPlayer + MediaSession
- SAF + DocumentFile
- Coroutines + Flow
- Robolectric + JUnit4 + Truth

## 项目结构

```text
app/src/main/java/com/relaxmusic/app
├── data
│   ├── db          # Room 数据库、DAO、Entity
│   ├── local       # 目录扫描、metadata、歌词、封面、备份等本地能力
│   ├── player      # 播放控制封装
│   └── repository  # 数据仓库实现
├── domain          # 领域模型与仓库接口
├── service         # 前台播放服务与通知
├── ui
│   ├── components  # 通用 Compose 组件
│   ├── navigation  # 导航定义
│   ├── screens     # 首页、列表、播放页、设置页等界面
│   └── theme       # 主题、配色、字体
└── utils           # 工具类
```

## 环境要求

- Android Studio 较新稳定版
- JDK 17
- Android SDK 35
- 最低支持 Android 8.0（`minSdk = 26`）

## 本地运行

### 使用 Android Studio

1. 用 Android Studio 打开项目根目录
2. 确认 Gradle JDK 为 17
3. 等待 Gradle Sync 完成
4. 运行 `app` 到真机或模拟器

### 关于 Gradle Wrapper

仓库当前仅包含 `gradle/wrapper/gradle-wrapper.properties`，没有提交 `gradlew` / `gradlew.bat` 脚本。日常开发建议直接使用 Android Studio 同步、运行和打包；如果你需要纯命令行构建，可自行在本地补齐 wrapper 脚本，或使用已安装的 Gradle。

## 建议验证项

- 首次授权一个或多个本地音乐目录
- 扫描包含子目录的曲库，确认歌曲数正确
- 搜索歌曲并进入播放
- 检查专辑、艺术家、收藏、历史、歌单页面是否可用
- 验证播放页进度拖动、播放模式切换、睡眠定时
- 验证歌词与封面显示
- 切到后台后检查通知栏与后台播放控制
- 在设置页切换主题，并测试备份导出 / 导入

## 测试

项目当前包含数据层、导航、播放页状态、主题与设置页相关单元测试，测试目录位于 `app/src/test/java`。

由于仓库当前未提交 `gradlew` / `gradlew.bat`，如果需要在命令行执行测试，请先准备本地 Gradle 环境；在 Android Studio 中也可以直接运行对应测试。

## 备份说明

- 导出文件名为 `relaxmusic_backup.json`
- 默认导出目录为应用外部文件目录下的 `backup/`
- 当前导出内容主要包含：已保存的音乐目录配置、歌单及歌单歌曲关联
- 目前不包含完整歌曲文件本体，导入后仍需要设备可访问原始音频目录

## Release APK 签名配置

### 推荐 keystore 命名

推荐使用：

- 文件名：`relaxmusic-release.jks`
- Alias：`relaxmusic-release`

推荐存放位置：

- 项目外固定目录，或
- 项目内 `keystore/relaxmusic-release.jks`（确保已加入忽略规则）

### `local.properties` 配置

在项目根目录的 `local.properties` 中添加：

```properties
RELAXMUSIC_RELEASE_STORE_FILE=D\:\Code\IdeaProjects\RelaxMusic\keystore\relaxmusic-release.jks
RELAXMUSIC_RELEASE_STORE_PASSWORD=你的keystore密码
RELAXMUSIC_RELEASE_KEY_ALIAS=relaxmusic-release
RELAXMUSIC_RELEASE_KEY_PASSWORD=你的key密码
```

也支持通过同名环境变量传入。

### 生成 Release APK

配置完成后，可通过 Android Studio 执行 release 构建；如果本地已补齐 Gradle wrapper 或安装了可用 Gradle，也可以执行：

```bash
gradle assembleRelease
```

生成后的 APK 一般位于：`app/build/outputs/apk/release/app-release.apk`

## 当前边界与后续可完善点

- 当前定位仍是本地播放器，不涉及在线音源、账号系统或云同步
- 备份能力目前偏轻量，主要覆盖设置与歌单，不覆盖完整播放状态迁移
- 通知栏样式、锁屏媒体信息与后台恢复策略仍可继续增强
- 若后续增加共享 schema、同步能力或更复杂媒体库能力，README 也应同步升级

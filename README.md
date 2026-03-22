# RelaxMusic

一个纯本地的 Android 音乐播放器原型，当前已经具备以下 V1 能力：

- 通过 Android 系统目录选择器授权本地音乐目录
- 递归扫描所选目录及全部子目录中的音频文件
- 提取标题、艺术家、专辑、时长等基础 metadata
- 使用 Room 保存本地曲库
- 支持搜索、列表展示、当前播放高亮
- 使用 Media3 ExoPlayer 播放本地音频
- 支持迷你播放器、播放详情页、切歌、拖动进度、播放模式切换
- 支持睡眠定时停止播放
- 已接入前台播放服务、MediaSession 和通知栏基础控制

## 技术栈

- Kotlin
- Jetpack Compose
- Room
- Media3 ExoPlayer
- SAF + DocumentFile
- Coroutines + Flow

## 打开方式

1. 用 Android Studio 打开当前目录
2. 确认 Gradle JDK 使用 17 或 Embedded JDK
3. 等待 Gradle Sync 完成
4. 运行到真机上测试

如果 Android Studio 提示缺少 Gradle Wrapper，可直接让 IDE 按当前项目配置同步；当前仓库已补充 `gradle/wrapper/gradle-wrapper.properties`，但如果本地还没有 wrapper 脚本，建议用 Android Studio 的 Gradle 同步和运行能力先启动项目。

## 当前建议验证项

- 选择本地音乐目录
- 扫描子目录音频文件
- 搜索歌曲
- 点击歌曲播放
- 迷你播放器与播放页同步
- 切到后台后继续播放
- 通知栏播放控制
- 睡眠定时停止播放

## 当前已知仍可继续增强的点

- 通知栏样式和锁屏媒体元数据可以继续完善
- 扫描进度还不是逐文件实时显示
- 设置页目录展示还可以更友好
- 后台恢复策略和播放历史尚未接入

## 首次运行建议

- 真机优先，尤其是目录授权和本地音频播放
- Android 13+ 如通知未出现，检查通知权限是否已允许
- 如果前台服务相关报错，先确认系统没有拦截通知与后台活动

## Release APK 签名配置

### 推荐 keystore 命名

推荐使用：

- 文件名：`relaxmusic-release.jks`
- Alias：`relaxmusic-release`

推荐存放位置：

- 项目外固定目录，或
- 项目内 `keystore/relaxmusic-release.jks`（已在 `.gitignore` 中忽略）

### local.properties 配置

在项目根目录的 `local.properties` 中添加：

```properties
RELAXMUSIC_RELEASE_STORE_FILE=D\:\Code\IdeaProjects\RelaxMusic\keystore\relaxmusic-release.jks
RELAXMUSIC_RELEASE_STORE_PASSWORD=你的keystore密码
RELAXMUSIC_RELEASE_KEY_ALIAS=relaxmusic-release
RELAXMUSIC_RELEASE_KEY_PASSWORD=你的key密码
```

也支持通过环境变量传入同名配置。

### 生成 release APK

配置完成后，可以使用 Android Studio 的 release 构建，或使用命令行：

```bash
./gradlew assembleRelease
```

Windows：

```bat
gradlew.bat assembleRelease
```

生成后的 APK 一般位于：

- `app/build/outputs/apk/release/app-release.apk`

### 注意事项

- 不要提交 `.jks` 文件到 git
- 不要泄露 keystore 密码和 alias 密码
- 后续版本升级必须继续使用同一个 keystore

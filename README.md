# Android 资源恢复助手

一个用于查找和恢复已删除照片和视频的 Android 应用。

## 技术栈

- **Kotlin** - 主要开发语言
- **Jetpack Compose** - 现代化 UI 框架
- **Material Design 3** - UI 设计规范
- **Coil** - 图片加载库
- **ExoPlayer** - 视频播放
- **Accompanist Permissions** - 权限管理

## 核心功能

### 1. 文件扫描系统
- 扫描设备存储中的 `.thumbnails` 缓存文件夹
- 扫描临时文件和系统缓存
- 支持扫描外部存储和 SD 卡
- 使用后台线程避免 UI 卡顿

### 2. 分类过滤功能
- 顶部三个 Tab：**全部** | **仅图片** | **仅视频**
- 支持的图片格式：jpg, jpeg, png, gif, webp
- 支持的视频格式：mp4, avi, mov, mkv, 3gp

### 3. 主界面显示
- 网格布局显示找到的文件（3 列）
- 显示缩略图
- 显示文件名、大小、日期
- 长按多选模式

### 4. 权限管理
- 请求存储访问权限（READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE）
- Android 13+ 适配（READ_MEDIA_IMAGES, READ_MEDIA_VIDEO）
- 权限说明对话框

### 5. 基础恢复功能
- 点击文件预览（图片/视频）
- "恢复"按钮将文件复制到 `DCIM/Recovered` 文件夹
- 显示恢复进度
- 恢复成功提示

## 项目结构

```
app/
├── src/main/
│   ├── java/com/example/androidresourcerecover/
│   │   ├── MainActivity.kt                # 主活动
│   │   ├── data/
│   │   │   ├── RecoveredFile.kt           # 数据模型
│   │   │   └── FileRepository.kt          # 数据管理
│   │   ├── scanner/
│   │   │   └── FileScanner.kt             # 文件扫描逻辑
│   │   ├── ui/
│   │   │   ├── MainScreen.kt              # 主屏幕
│   │   │   ├── FileGrid.kt                # 文件网格
│   │   │   ├── FilePreviewDialog.kt       # 预览对话框
│   │   │   ├── Components.kt              # UI 组件
│   │   │   └── theme/                     # 主题配置
│   │   └── viewmodel/
│   │       └── MainViewModel.kt           # 状态管理
│   ├── AndroidManifest.xml
│   └── res/
│       └── values/
│           ├── strings.xml
│           ├── colors.xml
│           └── themes.xml
└── build.gradle.kts
```

## 系统要求

- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 34 (Android 14)
- **compileSdk**: 34

## 构建和运行

1. 克隆项目
```bash
git clone <repository-url>
cd android_resource_recover
```

2. 使用 Android Studio 打开项目

3. 同步 Gradle 依赖

4. 连接 Android 设备或启动模拟器

5. 点击 Run 按钮

## 权限说明

应用需要以下权限：

- **READ_EXTERNAL_STORAGE** (Android 12 及以下) - 读取设备存储
- **WRITE_EXTERNAL_STORAGE** (Android 12 及以下) - 写入恢复的文件
- **READ_MEDIA_IMAGES** (Android 13+) - 读取图片
- **READ_MEDIA_VIDEO** (Android 13+) - 读取视频

## 开发者说明

### 关键组件

**FileScanner**: 负责扫描设备存储，查找可恢复的媒体文件。扫描位置包括：
- `.thumbnails` 文件夹
- `DCIM`, `Pictures`, `Movies` 目录
- Android 数据和媒体缓存目录

**FileRepository**: 处理文件恢复操作，将文件复制到恢复目录。

**MainViewModel**: 管理应用状态，包括：
- 扫描状态和进度
- 文件列表和过滤
- 选择状态
- 恢复操作

## License

MIT License
# 优雅音乐播放器

一个基于Maven构建的安卓音乐播放器应用，具有现代化UI设计和丰富的音乐播放功能。

## 项目特点

- 优雅美观的用户界面设计
- 支持本地音乐文件播放
- 多种播放模式（顺序播放、单曲循环、列表循环、随机播放）
- 专辑封面显示
- 收藏喜爱的歌曲
- 最近播放、最近添加和收藏列表
- 迷你播放器和全屏播放详情页

## 构建指南

### 环境要求

- JDK 8或更高版本
- Maven 3.6.0或更高版本
- Android SDK (API 31)

### 构建步骤

#### 1. 确认Maven安装

确保Maven已正确安装并配置环境变量。可以通过以下命令验证：

```
mvn --version
```

#### 2. 解决依赖问题

当前项目构建可能会遇到依赖下载问题。可以尝试以下解决方案：

##### 方案1：使用国内Maven镜像

在`~/.m2/settings.xml`文件中添加阿里云Maven镜像配置：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven Repository</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

##### 方案2：强制更新依赖

使用以下命令强制更新依赖：

```
mvn clean install -U
```

#### 3. 构建项目

在项目根目录执行以下命令构建项目：

```
mvn clean install
```

成功构建后，APK文件将生成在`target/`目录下。

## 项目结构

```
├── pom.xml                                 # Maven项目配置文件
├── src/
│   └── main/
│       ├── AndroidManifest.xml             # 应用清单文件
│       ├── java/com/musicplayer/elegant/   # Java源代码
│       │   ├── MusicPlayerApplication.java # 应用程序类
│       │   ├── adapter/                    # 适配器类
│       │   │   └── SongAdapter.java        # 歌曲列表适配器
│       │   ├── data/                       # 数据层
│       │   │   └── MusicRepository.java    # 音乐数据仓库
│       │   ├── model/                      # 数据模型
│       │   │   └── Song.java               # 歌曲实体类
│       │   ├── service/                    # 服务
│       │   │   └── MusicPlaybackService.java # 音乐播放服务
│       │   ├── ui/                         # 用户界面
│       │   │   ├── MainActivity.java       # 主活动
│       │   │   ├── PlayDetailActivity.java # 播放详情活动
│       │   │   └── fragments/              # 片段
│       │   │       └── HomeFragment.java   # 首页片段
│       │   └── viewmodel/                  # 视图模型
│       │       └── MusicPlayerViewModel.java # 音乐播放器视图模型
│       └── res/                            # 资源文件
│           ├── drawable/                   # 图形资源
│           │   └── default_album_art.xml   # 默认专辑封面
│           ├── layout/                     # 布局文件
│           │   ├── activity_main.xml       # 主活动布局
│           │   ├── activity_play_detail.xml # 播放详情布局
│           │   ├── fragment_home.xml       # 首页片段布局
│           │   └── item_song_card.xml      # 歌曲卡片布局
│           ├── menu/                       # 菜单资源
│           │   └── bottom_navigation_menu.xml # 底部导航菜单
│           └── values/                     # 值资源
│               └── strings.xml             # 字符串资源
```

## 功能说明

### 主界面

主界面包含底部导航栏，可以在首页、歌曲、专辑和播放列表之间切换。底部还有一个迷你播放器，显示当前播放的歌曲信息，点击可以打开播放详情页。

### 首页

首页显示最近播放、收藏和最近添加的歌曲，以卡片形式展示，点击可以直接播放。顶部有搜索栏，可以搜索歌曲、艺术家或专辑。

### 播放详情页

播放详情页显示当前播放歌曲的专辑封面、标题、艺术家等信息，以及播放进度条和控制按钮（播放/暂停、上一首、下一首、播放模式、收藏）。

### 音乐播放服务

应用使用后台服务播放音乐，即使退出应用也能继续播放。支持多种播放模式，包括顺序播放、单曲循环、列表循环和随机播放。

## 技术实现

- 使用Maven构建Android项目
- 采用MVVM架构模式，使用ViewModel和LiveData实现UI和数据的分离
- 使用ExoPlayer实现音频播放功能
- 使用Room数据库存储歌曲信息和播放列表
- 使用Glide加载专辑封面图片
- 使用Material Design组件实现现代化UI

## 使用方法

1. 克隆项目到本地
2. 使用Maven构建项目：`mvn clean install`
3. 将生成的APK安装到Android设备上
4. 启动应用，授予存储权限以访问本地音乐文件
5. 开始享受音乐！

## 常见问题

### 依赖下载失败

如果遇到依赖下载失败的问题，可能是网络问题或仓库配置问题。尝试使用上述的镜像配置或强制更新依赖。

### Android SDK路径问题

确保在系统环境变量中正确设置了`ANDROID_HOME`环境变量，指向Android SDK的安装路径。

### 构建插件版本问题

当前项目使用的是`com.simpligility.maven.plugins:android-maven-plugin:4.2.0`，如果构建失败，可能需要检查此插件是否兼容当前的Maven和JDK版本。

## 权限说明

应用需要以下权限：

- `READ_EXTERNAL_STORAGE`：读取设备上的音乐文件
- `WRITE_EXTERNAL_STORAGE`：保存播放列表等数据
- `INTERNET`：获取专辑封面等在线资源
- `FOREGROUND_SERVICE`：后台播放音乐

## 参考资源

本项目参考了GitHub上的多个开源音乐播放器项目，借鉴了其设计理念和功能实现方式，同时结合Material Design设计规范，打造了这款优雅美观的音乐播放器应用。
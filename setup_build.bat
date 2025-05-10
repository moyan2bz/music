@echo off
echo ===== 优雅音乐播放器构建助手 =====
echo.

REM 检查ANDROID_HOME环境变量
echo 正在检查ANDROID_HOME环境变量...
echo.

if "%ANDROID_HOME%"=="" (
    echo [警告] ANDROID_HOME环境变量未设置！
    echo 请按照以下步骤设置ANDROID_HOME环境变量：
    echo.
    echo 1. 右键点击"此电脑"，选择"属性"
    echo 2. 点击"高级系统设置"
    echo 3. 点击"环境变量"
    echo 4. 在"系统变量"部分，点击"新建"
    echo 5. 变量名输入：ANDROID_HOME
    echo 6. 变量值输入Android SDK路径（例如：C:\Users\用户名\AppData\Local\Android\Sdk）
    echo 7. 点击"确定"保存设置
    echo.
    echo 设置完成后，请重新运行此脚本
    echo.
    pause
    exit /b 1
) else (
    echo [成功] ANDROID_HOME已设置为: %ANDROID_HOME%
    echo.
)

REM 检查Maven
echo 正在检查Maven安装...
call mvn --version > nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [错误] Maven未正确安装或未添加到PATH中！
    echo 请确保已安装Maven并添加到系统PATH中
    echo.
    pause
    exit /b 1
) else (
    echo [成功] Maven已正确安装
    echo.
)

REM 检查JDK
echo 正在检查JDK安装...
java -version > nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [错误] JDK未正确安装或未添加到PATH中！
    echo 请确保已安装JDK 8或更高版本并设置JAVA_HOME环境变量
    echo.
    pause
    exit /b 1
) else (
    echo [成功] JDK已正确安装
    echo.
)

REM 提示用户选择操作
echo 请选择要执行的操作：
echo 1. 构建项目（包含测试）
echo 2. 构建项目（跳过测试）
echo 3. 退出
echo.

set /p choice=请输入选项（1-3）: 

if "%choice%"=="1" (
    echo.
    echo 正在构建项目（包含测试）...
    call mvn clean package
) else if "%choice%"=="2" (
    echo.
    echo 正在构建项目（跳过测试）...
    call mvn clean package -DskipTests
) else if "%choice%"=="3" (
    echo 退出构建助手
    exit /b 0
) else (
    echo 无效的选项，请重新运行脚本
    exit /b 1
)

REM 检查构建结果
if %ERRORLEVEL% neq 0 (
    echo.
    echo [错误] 构建失败！请检查上述错误信息
) else (
    echo.
    echo [成功] 构建完成！APK文件位于target目录
    echo APK路径: %CD%\target\elegant-music-player.apk
)

echo.
pause
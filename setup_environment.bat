@echo off
chcp 65001 >nul
echo ========================================
echo 秒送餐外卖系统 - 环境配置工具
echo ========================================
echo.

REM 检查Java环境
echo [1/5] 检查Java环境...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Java已安装
    java -version
) else (
    echo ❌ Java未安装或未配置环境变量
    echo 请安装JDK 17或更高版本
    echo 下载地址: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)
echo.

REM 检查Android SDK
echo [2/5] 检查Android SDK...
if exist "%ANDROID_HOME%" (
    echo ✅ ANDROID_HOME已配置: %ANDROID_HOME%
) else (
    echo ⚠️ ANDROID_HOME未配置
    echo 尝试查找Android SDK...
    
    REM 尝试常见路径
    if exist "D:\sdk" (
        echo 找到SDK: D:\sdk
        set ANDROID_HOME=D:\sdk
    ) else if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk" (
        echo 找到SDK: C:\Users\%USERNAME%\AppData\Local\Android\Sdk
        set ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
    ) else (
        echo ❌ 未找到Android SDK
        echo 请安装Android Studio或配置ANDROID_HOME环境变量
    )
)
echo.

REM 检查local.properties
echo [3/5] 配置Android项目...
cd android-user
if exist "local.properties" (
    echo ✅ local.properties已存在
) else (
    echo 创建local.properties...
    if defined ANDROID_HOME (
        echo sdk.dir=%ANDROID_HOME:\=\\% > local.properties
        echo ✅ 已创建local.properties
    ) else (
        echo ❌ 无法创建local.properties，请手动配置
    )
)
cd ..
echo.

REM 获取本机IP地址
echo [4/5] 获取本机IP地址...
echo 正在获取IP地址...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4" ^| findstr /v "127.0.0.1"') do (
    set IP=%%a
    goto :found_ip
)
:found_ip
set IP=%IP: =%
if defined IP (
    echo ✅ 本机IP地址: %IP%
    echo.
    echo 请根据测试方式选择配置:
    echo.
    echo [A] 使用Android模拟器测试
    echo     API地址: http://10.0.2.2:8080/api/
    echo.
    echo [B] 使用真实设备测试（同一WiFi）
    echo     API地址: http://%IP%:8080/api/
    echo.
    echo [C] 使用真实设备测试（手机热点）
    echo     请先连接手机热点，然后重新运行此脚本
    echo.
    
    set /p choice="请选择测试方式 (A/B/C): "
    
    if /i "%choice%"=="A" (
        echo 配置为模拟器模式...
        powershell -Command "(Get-Content android-user\app\src\main\res\values\strings.xml) -replace 'http://[^:]+:8080/api/', 'http://10.0.2.2:8080/api/' | Set-Content android-user\app\src\main\res\values\strings.xml"
        echo ✅ 已配置为模拟器模式
    ) else if /i "%choice%"=="B" (
        echo 配置为真机模式...
        powershell -Command "(Get-Content android-user\app\src\main\res\values\strings.xml) -replace 'http://[^:]+:8080/api/', 'http://%IP%:8080/api/' | Set-Content android-user\app\src\main\res\values\strings.xml"
        echo ✅ 已配置为真机模式
    ) else if /i "%choice%"=="C" (
        echo 请先连接手机热点，然后重新运行此脚本
    ) else (
        echo 无效选择，跳过配置
    )
) else (
    echo ❌ 无法获取IP地址
)
echo.

REM 检查后端服务
echo [5/5] 检查后端服务...
netstat -ano | findstr :8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 后端服务正在运行 (端口8080)
) else (
    echo ⚠️ 后端服务未运行
    echo 请启动后端服务:
    echo   cd backend
    echo   mvn spring-boot:run
)
echo.

echo ========================================
echo 配置完成！
echo ========================================
echo.
echo 下一步操作:
echo 1. 启动后端服务 (如果未启动)
echo 2. 在Android Studio中构建项目
echo 3. 启动模拟器或连接真机
echo 4. 运行应用进行测试
echo.
echo 详细说明请查看: README_测试指南.md
echo.
pause

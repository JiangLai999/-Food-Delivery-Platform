@echo off
chcp 65001 >nul
echo ========================================
echo API地址配置切换工具
echo ========================================
echo.

echo 当前配置:
findstr "api_base_url" android-user\app\src\main\res\values\strings.xml
echo.

echo 请选择测试模式:
echo.
echo [1] Android模拟器模式
echo     API地址: http://10.0.2.2:8080/api/
echo     说明: 模拟器专用地址，指向宿主机
echo.
echo [2] 真机测试模式（输入IP地址）
echo     说明: 用于真实设备测试
echo.
echo [3] 自动检测本机IP
echo     说明: 自动获取当前IP并配置
echo.
echo [4] 保持当前配置
echo.

set /p choice="请选择 (1/2/3/4): "

if "%choice%"=="1" (
    echo 配置为模拟器模式...
    powershell -Command "(Get-Content android-user\app\src\main\res\values\strings.xml) -replace '<string name="api_base_url"[^>]*>.*?</string>', '<string name="api_base_url" translatable="false">http://10.0.2.2:8080/api/</string>' | Set-Content android-user\app\src\main\res\values\strings.xml"
    echo ✅ 已配置为模拟器模式
) else if "%choice%"=="2" (
    set /p ip="请输入服务器IP地址: "
    if defined ip (
        echo 配置为真机模式: %ip%
        powershell -Command "(Get-Content android-user\app\src\main\res\values\strings.xml) -replace '<string name="api_base_url"[^>]*>.*?</string>', '<string name="api_base_url" translatable="false">http://%ip%:8080/api/</string>' | Set-Content android-user\app\src\main\res\values\strings.xml"
        echo ✅ 已配置为真机模式
    ) else (
        echo ❌ IP地址为空
    )
) else if "%choice%"=="3" (
    echo 正在获取本机IP地址...
    for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4" ^| findstr /v "127.0.0.1"') do (
        set IP=%%a
        goto :found_ip
    )
    :found_ip
    set IP=%IP: =%
    if defined IP (
        echo 本机IP地址: %IP%
        echo 配置为真机模式: %IP%
        powershell -Command "(Get-Content android-user\app\src\main\res\values\strings.xml) -replace '<string name="api_base_url"[^>]*>.*?</string>', '<string name="api_base_url" translatable="false">http://%IP%:8080/api/</string>' | Set-Content android-user\app\src\main\res\values\strings.xml"
        echo ✅ 已配置为真机模式
    ) else (
        echo ❌ 无法获取IP地址
    )
) else if "%choice%"=="4" (
    echo 保持当前配置
) else (
    echo 无效选择
)

echo.
echo 更新后的配置:
findstr "api_base_url" android-user\app\src\main\res\values\strings.xml
echo.

echo 注意: 修改配置后需要重新构建APK
echo   cd android-user
echo   gradlew assembleDebug
echo.
pause

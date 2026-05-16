@echo off
chcp 65001 >nul
echo ========================================
echo Android模拟器问题诊断工具
echo ========================================
echo.

echo [1/6] 检查系统信息
echo ----------------------------------------
systeminfo | findstr /C:"OS Name" /C:"System Type"
echo.

echo [2/6] 检查虚拟化技术
echo ----------------------------------------
echo 正在检查虚拟化技术...
powershell -Command "Get-ComputerInfo | Select-Object HyperVisorPresent, HyperVRequirement"
echo.

echo [3/6] 检查HAXM安装
echo ----------------------------------------
if exist "C:\Program Files\Intel\HAXM" (
    echo ✅ HAXM已安装: C:\Program Files\Intel\HAXM
) else (
    echo ❌ HAXM未安装
    echo 建议安装HAXM:
    echo   1. Android Studio → Tools → SDK Manager → SDK Tools
    echo   2. 勾选 "Intel x86 Emulator Accelerator (HAXM installer)"
    echo   3. 点击 Apply
)
echo.

echo [4/6] 检查Android SDK
echo ----------------------------------------
if defined ANDROID_HOME (
    echo ✅ ANDROID_HOME: %ANDROID_HOME%
) else (
    echo ⚠️ ANDROID_HOME未设置
)

if exist "%ANDROID_HOME%\emulator\emulator.exe" (
    echo ✅ 模拟器工具存在
) else if exist "D:\sdk\emulator\emulator.exe" (
    echo ✅ 模拟器工具存在 (D:\sdk)
) else (
    echo ❌ 模拟器工具不存在
)
echo.

echo [5/6] 检查可用AVD
echo ----------------------------------------
echo 正在检查可用模拟器...
set EMULATOR_PATH=
if exist "%ANDROID_HOME%\emulator\emulator.exe" (
    set EMULATOR_PATH=%ANDROID_HOME%\emulator\emulator.exe
) else if exist "D:\sdk\emulator\emulator.exe" (
    set EMULATOR_PATH=D:\sdk\emulator\emulator.exe
)

if defined EMULATOR_PATH (
    "%EMULATOR_PATH%" -list-avds
) else (
    echo ❌ 无法检查AVD，模拟器工具不存在
)
echo.

echo [6/6] 检查模拟器进程
echo ----------------------------------------
tasklist | findstr /I "emulator.exe"
if %errorlevel% equ 0 (
    echo ✅ 模拟器进程正在运行
) else (
    echo ⚠️ 模拟器进程未运行
)
echo.

echo ========================================
echo 诊断结果和建议
echo ========================================
echo.

echo 常见问题解决方案:
echo.

echo 问题1: 模拟器启动失败
echo 解决方案:
echo   1. 检查BIOS中虚拟化技术是否启用
echo   2. 安装HAXM (Intel硬件加速执行管理器)
echo   3. 降低模拟器内存配置 (2GB以下)
echo   4. 尝试不同的系统镜像 (x86而不是x86_64)
echo.

echo 问题2: 模拟器运行缓慢
echo 解决方案:
echo   1. 确保HAXM已正确安装
echo   2. 分配更多内存给模拟器
echo   3. 使用SSD硬盘
echo   4. 关闭其他占用资源的程序
echo.

echo 问题3: 网络连接问题
echo 解决方案:
echo   1. 模拟器使用10.0.2.2访问宿主机
echo   2. 确保后端服务已启动
echo   3. 检查防火墙设置
echo.

echo 问题4: 应用闪退
echo 解决方案:
echo   1. 检查日志: adb logcat
echo   2. 清除应用数据
echo   3. 重新安装应用
echo.

echo 如需更多帮助，请查看: README_测试指南.md
echo.
pause

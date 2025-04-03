@echo off
echo 正在启动前端项目...

:: 检查是否安装了 Node.js
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未安装 Node.js，请先安装 Node.js
    pause
    exit /b 1
)

:: 检查是否安装了 npm
where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未安装 npm，请先安装 npm
    pause
    exit /b 1
)

:: 检查 node_modules 目录是否存在
if not exist "node_modules" (
    echo 正在安装依赖...
    call npm install
    if %errorlevel% neq 0 (
        echo 安装依赖失败
        pause
        exit /b 1
    )
)

:: 启动开发服务器
echo 正在启动开发服务器...
call npm run dev

:: 如果发生错误，暂停显示错误信息
if %errorlevel% neq 0 (
    echo 启动失败，请检查错误信息
    pause
) 
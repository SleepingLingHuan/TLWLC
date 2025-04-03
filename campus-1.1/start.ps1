# 设置工作目录为脚本所在目录
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

# 检查是否安装了 Node.js
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Write-Host "错误: 未安装 Node.js，请先安装 Node.js"
    exit 1
}

# 检查是否安装了 npm
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Host "错误: 未安装 npm，请先安装 npm"
    exit 1
}

# 检查 node_modules 目录是否存在
if (-not (Test-Path "node_modules")) {
    Write-Host "正在安装依赖..."
    npm install
}

# 启动开发服务器
Write-Host "正在启动开发服务器..."
npm run dev

# 如果发生错误，暂停显示错误信息
if ($LASTEXITCODE -ne 0) {
    Write-Host "启动失败，请检查错误信息"
    pause
} 
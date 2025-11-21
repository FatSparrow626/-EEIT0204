param(
    [int]$maxDepth = 2
)

$root = Get-Location
$outFile = "tree.txt"

# 要忽略的資料夾名稱（可自行增加）
$ignoreList = @(".git", "node_modules", ".metadata", ".vscode", ".gitattributes", ".gitattributes", ".gitignore")

# 清空輸出檔案
"" | Out-File $outFile

function Show-Tree {
    param(
        [string]$path,
        [int]$level
    )

    # 列出資料夾內容
    Get-ChildItem $path -Force | ForEach-Object {

        # 若在忽略列表 → 跳過
        if ($ignoreList -contains $_.Name) {
            return
        }

        $indent = " " * ($level * 4)
        "$indent|-- $($_.Name)" | Out-File $outFile -Append
        
        # 遞迴（只處理資料夾 且 未達 maxDepth）
        if ($_.PSIsContainer -and $level -lt ($maxDepth - 1)) {
            Show-Tree -path $_.FullName -level ($level + 1)
        }
    }
}

# 標記根位置
"Root: $root" | Out-File $outFile -Append

# 開始遞迴
Show-Tree -path $root -level 0

Write-Host "已生成 tree.txt（最大層數：$maxDepth，已忽略ignoreList所列檔案）"

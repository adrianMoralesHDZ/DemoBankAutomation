# Parseador de dump XML de UiAutomator
param(
    [Parameter(Mandatory=$true)]
    [string]$DumpPath
)

[xml]$xml = Get-Content -LiteralPath $DumpPath -Raw

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " ANÁLISIS DE DUMP: $DumpPath" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 1) Resumen de la jerarquía
$root = $xml.hierarchy
Write-Host "Rotación: $($root.rotation)"
Write-Host "Tamaño de pantalla inferido del nodo raiz: $($root.node.bounds)"
Write-Host ""

# 2) Todos los nodos con contenido visible
$allNodes = $xml.GetElementsByTagName('node')
Write-Host "Total de nodos en la jerarquía: $($allNodes.Count)" -ForegroundColor Green
Write-Host ""

# 3) Solo los nodos que tienen texto, content-desc o resource-id con valor
Write-Host "=== NODOS CON TEXTO O DESCRIPCION VISIBLE ===" -ForegroundColor Yellow
Write-Host ("{0,-50} {1,-30} {2,-45} {3}" -f "TEXTO","CLASE","CONTENT-DESC","BOUNDS")
Write-Host ("=" * 140)

$visibleNodes = $allNodes | Where-Object {
    ($_.text -ne $null -and $_.text -ne "") -or
    ($_.content_desc -ne $null -and $_.content_desc -ne "") -or
    ($_.resource_id -ne $null -and $_.resource_id -ne "")
}

foreach ($n in $visibleNodes) {
    $txt = if ($n.text) { $n.text } else { "(vacio)" }
    $cls = if ($n.class) { $n.class } else { "?" }
    $desc = if ($n.content_desc) { $n.content_desc } else { "(vacio)" }
    $bnd = $n.bounds
    Write-Host ("{0,-50} {1,-30} {2,-45} {3}" -f $txt, $cls, $desc, $bnd)
}

Write-Host ""
Write-Host "=== NODOS EDITABLES (EditTexts) ===" -ForegroundColor Yellow
$edits = $allNodes | Where-Object { $_.class -eq 'android.widget.EditText' }
foreach ($n in $edits) {
    Write-Host "Texto: '$($n.text)'"
    Write-Host "Hint: '$($n.hint)'"
    Write-Host "ResourceId: '$($n.resource_id)'"
    Write-Host "ContentDesc: '$($n.content_desc)'"
    Write-Host "Password: $($n.password)"
    Write-Host "Bounds: $($n.bounds)"
    Write-Host "---"
}

Write-Host ""
Write-Host "=== NODOS CON TEXTO QUE PARECEN POSIBLES ERRORES ===" -ForegroundColor Yellow
$candidates = $allNodes | Where-Object {
    $_.text -and (
        $_.text -match '(?i)error|inv|vacio|vacío|requer|ingres|oblig|por fav|neces|completa|debe|introduc|selecciona|proporc'
    )
}
if ($candidates.Count -gt 0) {
    foreach ($n in $candidates) {
        Write-Host "TEXTO='$($n.text)' CLASE='$($n.class)' DESC='$($n.content_desc)' ID='$($n.resource_id)' BOUNDS='$($n.bounds)'"
    }
} else {
    Write-Host "(No se encontraron nodos con palabras clave de error)" -ForegroundColor Red
}

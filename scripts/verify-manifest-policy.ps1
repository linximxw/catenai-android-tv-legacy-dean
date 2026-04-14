[CmdletBinding()]
param(
    [string]$RepoRoot
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($RepoRoot)) {
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    $RepoRoot = (Resolve-Path (Join-Path $scriptDir "..")).Path
}

function Assert-Contains {
    param(
        [string]$Path,
        [string]$Pattern,
        [string]$Message
    )

    if (-not (Test-Path $Path)) {
        throw "Missing file: $Path"
    }

    $content = Get-Content -Raw -Encoding UTF8 $Path
    if ($content -notmatch $Pattern) {
        throw $Message
    }
}

function Assert-NotContains {
    param(
        [string]$Path,
        [string]$Pattern,
        [string]$Message
    )

    if (-not (Test-Path $Path)) {
        throw "Missing file: $Path"
    }

    $content = Get-Content -Raw -Encoding UTF8 $Path
    if ($content -match $Pattern) {
        throw $Message
    }
}

$mainManifest = Join-Path $RepoRoot "app/src/main/AndroidManifest.xml"
$releaseManifest = Join-Path $RepoRoot "app/src/release/AndroidManifest.xml"

Assert-NotContains `
    -Path $mainManifest `
    -Pattern 'android:sharedUserId\s*=' `
    -Message "Debug installs are blocked because main manifest still declares sharedUserId."

Assert-Contains `
    -Path $releaseManifest `
    -Pattern 'android:sharedUserId\s*=\s*"android\.uid\.system"' `
    -Message "Release manifest must keep android.uid.system for future system-app packaging."

Write-Output "Manifest policy check passed."

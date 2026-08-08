param(
    [Parameter(Mandatory = $true)]
    [string]$EpisodeDir,
    [string]$CoverImage = "",
    [string]$OutputVideo = "",
    [switch]$UseLegacySapi,
    [switch]$Publish
)

$ErrorActionPreference = "Stop"

if (-not $UseLegacySapi) {
    $repoRoot = Split-Path $PSScriptRoot -Parent
    $renderer = Join-Path $PSScriptRoot "render-audio-novel-preview.mjs"
    $nodeArgs = @($renderer, "--episode-dir", (Resolve-Path $EpisodeDir).Path)
    if (-not [string]::IsNullOrWhiteSpace($CoverImage)) {
        $nodeArgs += @("--cover", $CoverImage)
    }
    if (-not [string]::IsNullOrWhiteSpace($OutputVideo)) {
        $nodeArgs += @("--output", $OutputVideo)
    }
    if ($Publish) {
        $nodeArgs += @("--publish")
    }
    & node @nodeArgs
    if ($LASTEXITCODE -ne 0) {
        throw "render-audio-novel-preview.mjs failed with exit code $LASTEXITCODE"
    }
    if ($Publish) {
        $episodeDirPath = (Resolve-Path $EpisodeDir).Path
        $exportPath = Join-Path $episodeDirPath "episode_export.json"
        if (Test-Path $exportPath) {
            $export = Get-Content $exportPath -Raw -Encoding UTF8 | ConvertFrom-Json
            $sessionId = $export.sessionId
            if (-not [string]::IsNullOrWhiteSpace($sessionId)) {
                $gradlew = Join-Path $repoRoot "gradlew.bat"
                $relativeEpisodeDir = ($EpisodeDir -replace "\\", "/").TrimStart("./")
                Push-Location $repoRoot
                try {
                    & $gradlew run --args="audit-audio-novel-publish --db data/story.db --session $sessionId --episode-dir $relativeEpisodeDir"
                } finally {
                    Pop-Location
                }
                if ($LASTEXITCODE -ne 0) {
                    throw "audit-audio-novel-publish failed with exit code $LASTEXITCODE"
                }
            }
        }
    }
    return
}

Add-Type -AssemblyName System.Speech

function Invoke-External {
    param(
        [string]$Command,
        [string[]]$Arguments
    )
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        & $Command @Arguments 2>&1 | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "$Command failed with exit code $LASTEXITCODE"
        }
    } finally {
        $ErrorActionPreference = $prev
    }
}

$episodeDirPath = Resolve-Path $EpisodeDir
$timelinePath = Join-Path $episodeDirPath "voice_timeline.json"
if (-not (Test-Path $timelinePath)) {
    throw "Missing voice_timeline.json in $episodeDirPath"
}

$timeline = Get-Content $timelinePath -Raw -Encoding UTF8 | ConvertFrom-Json
$exportPath = Join-Path $episodeDirPath "episode_export.json"
if (Test-Path $exportPath) {
    $export = Get-Content $exportPath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ([string]::IsNullOrWhiteSpace($CoverImage) -and $export.coverImagePath) {
        $CoverImage = $export.coverImagePath
    }
}

$repoRoot = Split-Path $PSScriptRoot -Parent
if ([string]::IsNullOrWhiteSpace($CoverImage)) {
    $CoverImage = "world/chenxing/assets/cover/chenxing_cover_v1.png"
}
$coverPath = if ([System.IO.Path]::IsPathRooted($CoverImage)) { $CoverImage } else { Join-Path $repoRoot $CoverImage }
if (-not (Test-Path $coverPath)) {
    throw "Cover image not found: $coverPath"
}

$audioDir = Join-Path $episodeDirPath "audio"
New-Item -ItemType Directory -Force -Path $audioDir | Out-Null
if ([string]::IsNullOrWhiteSpace($OutputVideo)) {
    $OutputVideo = Join-Path $episodeDirPath "episode_preview.mp4"
}

function New-SapiWave {
    param(
        [string]$Text,
        [string]$VoiceName,
        [int]$Rate = 0,
        [string]$OutPath
    )
    $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
    try {
        $synth.SelectVoice($VoiceName)
        $synth.Rate = $Rate
        $synth.SetOutputToWaveFile($OutPath)
        $synth.Speak($Text)
    } finally {
        $synth.SetOutputToNull()
        $synth.Dispose()
        [GC]::Collect()
        [GC]::WaitForPendingFinalizers()
    }
}

$narratorVoice = "Microsoft Hanhan Desktop"
$dialogueVoice = "Microsoft Hanhan Desktop"
$concatLines = New-Object System.Collections.Generic.List[string]
$gapSeconds = 0.35
$gapPath = Join-Path $audioDir "_gap.wav"
Invoke-External -Command "ffmpeg" -Arguments @("-y", "-f", "lavfi", "-i", "anullsrc=r=22050:cl=mono", "-t", "$gapSeconds", "-q:a", "9", "-acodec", "pcm_s16le", $gapPath)

foreach ($cue in $timeline.audioCues) {
    $wavName = "$($cue.cueId).wav"
    $wavPath = Join-Path $audioDir $wavName
    $voice = if ($cue.characterId -eq "char-lu-chenxing") { $dialogueVoice } else { $narratorVoice }
    $rate = if ($cue.characterId -eq "char-lu-chenxing") { -2 } else { 0 }
    if (-not (Test-Path $wavPath)) {
        Write-Host "tts $($cue.cueId) [$voice] $($cue.text.Substring(0, [Math]::Min(24, $cue.text.Length)))..."
        New-SapiWave -Text $cue.text -VoiceName $voice -Rate $rate -OutPath $wavPath
    } else {
        Write-Host "reuse $($cue.cueId)"
    }
    $concatLines.Add("file '$wavName'")
    $concatLines.Add("file '_gap.wav'")
}

$concatListPath = Join-Path $audioDir "concat.txt"
# Drop trailing gap entry
if ($concatLines.Count -gt 0) { $concatLines.RemoveAt($concatLines.Count - 1) }
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllLines($concatListPath, $concatLines, $utf8NoBom)

$combinedWav = Join-Path $audioDir "combined.wav"
Push-Location $audioDir
try {
    Invoke-External -Command "ffmpeg" -Arguments @("-y", "-f", "concat", "-safe", "0", "-i", "concat.txt", "-c:a", "pcm_s16le", "-ar", "22050", "-ac", "1", "combined.wav")
} finally {
    Pop-Location
}

Invoke-External -Command "ffmpeg" -Arguments @(
    "-y",
    "-loop", "1", "-i", $coverPath,
    "-i", $combinedWav,
    "-vf", "scale=trunc(iw/2)*2:trunc(ih/2)*2",
    "-c:v", "libx264", "-tune", "stillimage", "-pix_fmt", "yuv420p",
    "-c:a", "aac", "-b:a", "192k",
    "-shortest",
    "-movflags", "+faststart",
    $OutputVideo
)

Write-Host "wrote=$OutputVideo"
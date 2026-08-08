$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$pipeline = "samples/chenxing/pipeline"
$chainOut = "$pipeline/chain_outputs"
$story = "samples/chenxing/chenxing_story_input_pack_sample.json"
$chain = "$pipeline/chenxing_story_video_chain_report_sample.json"
$render = "$chainOut/local_render_package_draft.json"
$cards = "$chainOut/local_asset_prompt_cards_draft.json"
$foundation = "samples/local_story_input/chenxing_09_foundation_card_pack_contract_sample.json"
$foundationValidation = "samples/chenxing/chenxing_09_foundation_card_pack_validation_report_sample.json"

function Invoke-Cli([string[]]$CliArgs) {
    $argString = $CliArgs -join " "
    & .\gradlew.bat :app:run --no-daemon --args="$argString"
    if ($LASTEXITCODE -ne 0) { throw "CLI failed: $argString" }
}

Invoke-Cli @(
    "validate-09-foundation-card-pack",
    "--input", $foundation,
    "--validation", "chenxing-09-foundation-card-pack-validation-v0",
    "--out", $foundationValidation
)

Invoke-Cli @(
    "draft-local-story-video-preview-handoff",
    "--chain-report", $chain,
    "--render-package", $render,
    "--asset-prompt-cards", $cards,
    "--story-input", $story,
    "--handoff", "chenxing-story-video-preview-handoff-v0-001",
    "--out", "$pipeline/chenxing_story_video_preview_handoff_draft_sample.json"
)

$handoff = "$pipeline/chenxing_story_video_preview_handoff_draft_sample.json"
Invoke-Cli @(
    "review-local-story-video-preview-handoff",
    "--handoff", $handoff,
    "--review", "chenxing-story-video-preview-handoff-review-v0-001",
    "--out", "$pipeline/chenxing_story_video_preview_handoff_review_report_sample.json"
)

$handoffReview = "$pipeline/chenxing_story_video_preview_handoff_review_report_sample.json"
Invoke-Cli @(
    "dry-run-local-renderer-adapter",
    "--handoff", $handoff,
    "--review", $handoffReview,
    "--dry-run-id", "chenxing-local-renderer-adapter-dry-run-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_adapter_dry_run_report_sample.json"
)

$dryRun = "$pipeline/chenxing_local_renderer_adapter_dry_run_report_sample.json"
Invoke-Cli @(
    "plan-local-renderer-adapter-implementation",
    "--dry-run-report", $dryRun,
    "--plan", "chenxing-local-renderer-adapter-implementation-plan-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_adapter_implementation_plan_sample.json"
)

$implPlan = "$pipeline/chenxing_local_renderer_adapter_implementation_plan_sample.json"
Invoke-Cli @(
    "review-local-renderer-adapter-implementation-plan",
    "--plan", $implPlan,
    "--review", "chenxing-local-renderer-adapter-implementation-plan-review-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_adapter_implementation_plan_review_report_sample.json"
)

$implPlanReview = "$pipeline/chenxing_local_renderer_adapter_implementation_plan_review_report_sample.json"
Invoke-Cli @(
    "draft-local-renderer-adapter-implementation",
    "--handoff", $handoff,
    "--plan", $implPlan,
    "--plan-review", $implPlanReview,
    "--draft", "chenxing-local-renderer-adapter-implementation-draft-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_adapter_implementation_draft_sample.json"
)

$implDraft = "$pipeline/chenxing_local_renderer_adapter_implementation_draft_sample.json"
Invoke-Cli @(
    "review-local-renderer-adapter-implementation-draft",
    "--draft", $implDraft,
    "--review", "chenxing-local-renderer-adapter-implementation-draft-review-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_adapter_implementation_draft_review_report_sample.json"
)

$implDraftReview = "$pipeline/chenxing_local_renderer_adapter_implementation_draft_review_report_sample.json"
Invoke-Cli @(
    "plan-local-renderer-manifest-draft",
    "--implementation-draft", $implDraft,
    "--draft-review", $implDraftReview,
    "--plan", "chenxing-local-renderer-manifest-draft-plan-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_manifest_draft_plan_sample.json"
)

$manifestPlan = "$pipeline/chenxing_local_renderer_manifest_draft_plan_sample.json"
Invoke-Cli @(
    "review-local-renderer-manifest-draft-plan",
    "--plan", $manifestPlan,
    "--review", "chenxing-local-renderer-manifest-draft-plan-review-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_manifest_draft_plan_review_report_sample.json"
)

$manifestPlanReview = "$pipeline/chenxing_local_renderer_manifest_draft_plan_review_report_sample.json"
Invoke-Cli @(
    "draft-local-renderer-manifest-json",
    "--plan", $manifestPlan,
    "--plan-review", $manifestPlanReview,
    "--draft", "chenxing-local-renderer-manifest-draft-json-draft-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_manifest_draft_json_draft_sample.json"
)

$manifestJsonDraft = "$pipeline/chenxing_local_renderer_manifest_draft_json_draft_sample.json"
Invoke-Cli @(
    "review-local-renderer-manifest-draft-json",
    "--draft", $manifestJsonDraft,
    "--review", "chenxing-local-renderer-manifest-draft-json-review-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_manifest_draft_json_review_report_sample.json"
)

$manifestJsonReview = "$pipeline/chenxing_local_renderer_manifest_draft_json_review_report_sample.json"
Invoke-Cli @(
    "dry-run-local-renderer-manifest-draft-json-adapter",
    "--draft", $manifestJsonDraft,
    "--review", $manifestJsonReview,
    "--dry-run-id", "chenxing-local-renderer-manifest-draft-json-adapter-dry-run-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_manifest_draft_json_adapter_dry_run_report_sample.json"
)

$manifestAdapterDryRun = "$pipeline/chenxing_local_renderer_manifest_draft_json_adapter_dry_run_report_sample.json"
Invoke-Cli @(
    "plan-local-renderer-manifest-adapter-contract",
    "--dry-run-report", $manifestAdapterDryRun,
    "--plan", "chenxing-local-renderer-manifest-adapter-contract-plan-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_manifest_adapter_contract_plan_sample.json"
)

$contractPlan = "$pipeline/chenxing_local_renderer_manifest_adapter_contract_plan_sample.json"
Invoke-Cli @(
    "review-local-renderer-manifest-adapter-contract-plan",
    "--plan", $contractPlan,
    "--review", "chenxing-local-renderer-manifest-adapter-contract-plan-review-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_manifest_adapter_contract_plan_review_report_sample.json"
)

$contractPlanReview = "$pipeline/chenxing_local_renderer_manifest_adapter_contract_plan_review_report_sample.json"
Invoke-Cli @(
    "draft-local-renderer-manifest-adapter-contract",
    "--plan", $contractPlan,
    "--plan-review", $contractPlanReview,
    "--draft", "chenxing-local-renderer-manifest-adapter-contract-draft-v0-001",
    "--out", "$pipeline/chenxing_local_renderer_manifest_adapter_contract_draft_sample.json"
)

Invoke-Cli @(
    "review-09-foundation-card-pack",
    "--pack", $foundation,
    "--validation", $foundationValidation,
    "--review", "chenxing-09-foundation-card-pack-review-v0-001",
    "--out", "$pipeline/chenxing_09_foundation_card_pack_review_report_sample.json"
)

Write-Host "Chenxing pipeline samples generated under $pipeline"
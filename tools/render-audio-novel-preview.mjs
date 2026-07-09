#!/usr/bin/env node
/**
 * Render audio-novel episode preview with Edge TTS per oral segment,
 * variable breathing gaps, and burned-in Traditional Chinese subtitles.
 */
import { readFileSync, writeFileSync, mkdirSync, existsSync, copyFileSync } from "node:fs";
import { join, resolve, dirname, basename } from "node:path";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";
import { Communicate, SubMaker } from "edge-tts-universal";

const __dirname = dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = resolve(__dirname, "..");

const VOICE_BY_CHARACTER = {
  narrator: "zh-TW-YunJheNeural",
};
const FEMALE_CHARACTER_IDS = new Set([
  // Reserve female neural voices for explicit female speaking roles only.
]);
const DEFAULT_VOICE = "zh-TW-YunJheNeural";
const FEMALE_VOICE = "zh-TW-HsiaoChenNeural";
const SENTENCE_GAP_SECONDS = 0.42;
const DIALOGUE_GAP_SECONDS = 0.72;
const TURN_GAP_SECONDS = 1.0;
const SECTION_TITLE_SECONDS = 1.2;
const SUBTITLE_WORDS_PER_CUE = 5;
const SUBTITLE_MAX_CHARS = 12;
const SUBTITLE_MIN_DURATION_MS = 700;
const SUBTITLE_MAX_DURATION_MS = 2100;
const SUBTITLE_FONT_SIZE_PUBLISH = 22;
const SUBTITLE_FONT_SIZE_DEV = 24;

function parseArgs(argv) {
  const args = {
    episodeDir: "",
    coverImage: "",
    outputVideo: "",
    rate: "-4%",
    publish: false,
  };
  for (let i = 2; i < argv.length; i++) {
    const token = argv[i];
    if (token === "--episode-dir") args.episodeDir = argv[++i];
    else if (token === "--cover") args.coverImage = argv[++i];
    else if (token === "--output") args.outputVideo = argv[++i];
    else if (token === "--rate") args.rate = argv[++i];
    else if (token === "--publish") args.publish = true;
    else if (token === "--help" || token === "-h") {
      console.log(`Usage: node tools/render-audio-novel-preview.mjs --episode-dir <dir> [--cover <path>] [--output <mp4>]`);
      process.exit(0);
    }
  }
  if (!args.episodeDir) {
    console.error("Missing --episode-dir");
    process.exit(1);
  }
  args.episodeDir = resolve(args.episodeDir);
  if (!args.outputVideo) {
    args.outputVideo = join(args.episodeDir, "episode_preview.mp4");
  }
  return args;
}

function run(cmd, cmdArgs, options = {}) {
  const result = spawnSync(cmd, cmdArgs, {
    encoding: "utf8",
    stdio: ["ignore", "pipe", "pipe"],
    ...options,
  });
  if (result.status !== 0) {
    const detail = (result.stderr || result.stdout || "").trim();
    throw new Error(`${cmd} ${cmdArgs.join(" ")} failed (${result.status}): ${detail}`);
  }
  return result.stdout ?? "";
}

function loadVoiceTimeline(episodeDir) {
  const timelinePath = join(episodeDir, "voice_timeline.json");
  if (!existsSync(timelinePath)) {
    throw new Error(`Missing voice_timeline.json in ${episodeDir}`);
  }
  return JSON.parse(readFileSync(timelinePath, "utf8"));
}

function loadSectionTitles(episodeDir) {
  const scriptPath = join(episodeDir, "episode_script.md");
  if (!existsSync(scriptPath)) return new Map();
  const raw = readFileSync(scriptPath, "utf8");
  const titles = new Map();
  for (const block of raw.split(/^##\s+/m).slice(1)) {
    const header = block.trim().split(/\r?\n/)[0] ?? "";
    const panelMatch = header.match(/·\s*([^\s（]+)（[^）]+）/);
    if (panelMatch) {
      titles.set(panelMatch[1].trim(), header.trim());
    }
  }
  return titles;
}

function voiceForCue(cue) {
  if (VOICE_BY_CHARACTER[cue.characterId]) return VOICE_BY_CHARACTER[cue.characterId];
  if (FEMALE_CHARACTER_IDS.has(cue.characterId)) return FEMALE_VOICE;
  return DEFAULT_VOICE;
}

function rateForCue(cue, baseRate) {
  if (cue.characterId !== "narrator") return "-6%";
  return baseRate;
}

function subtitleTextForCue(cue, publish) {
  const text = (cue.text ?? "").trim();
  if (!publish) return text;
  const quoteMatch = text.match(/「([^」]+)」/);
  if (quoteMatch && cue.characterId !== "narrator") {
    return quoteMatch[1].trim();
  }
  return text.replace(/shot-\d+[-\w]*/g, "").replace(/\s+/g, "").trim();
}

function hardSplitSegment(segment, maxChars) {
  const parts = [];
  let i = 0;
  while (i < segment.length) {
    let end = Math.min(i + maxChars, segment.length);
    if (end < segment.length) {
      const nextChar = segment[end];
      if (/[。！？；，、]/.test(nextChar)) end += 1;
    }
    parts.push(segment.slice(i, end));
    i = end;
  }
  return parts;
}

function mergeSubtitleParts(parts) {
  const merged = [];
  for (const part of parts) {
    const piece = part.trim();
    if (!piece) continue;
    if (/^(?:[。！？；，、]|……+)$/.test(piece) && merged.length > 0) {
      merged[merged.length - 1] += piece;
      continue;
    }
    merged.push(piece);
  }
  return merged;
}

function splitSubtitleText(text, maxChars = SUBTITLE_MAX_CHARS) {
  const normalized = text.replace(/\s+/g, "").trim();
  if (!normalized) return [];
  if (normalized.length <= maxChars) return [normalized];

  const segments = normalized
    .split(/(?<=[。！？；])|(?<=[，、])|(?<=——)|(?<=……)/)
    .map((s) => s.trim())
    .filter(Boolean);
  const source = segments.length > 0 ? segments : [normalized];
  const parts = [];
  let buffer = "";

  const flush = () => {
    if (buffer) {
      parts.push(buffer);
      buffer = "";
    }
  };

  for (const segment of source) {
    if (segment.length <= maxChars) {
      if (buffer && buffer.length + segment.length > maxChars) flush();
      buffer += segment;
      if (buffer.length >= maxChars) flush();
      continue;
    }
    flush();
    parts.push(...hardSplitSegment(segment, maxChars));
  }
  flush();
  return mergeSubtitleParts(parts);
}

function forceSplitByDuration(text, totalDurationMs, maxChars = SUBTITLE_MAX_CHARS) {
  const normalized = text.replace(/\s+/g, "").trim();
  if (!normalized) return [];
  const targetChunks = Math.min(
    Math.ceil(totalDurationMs / SUBTITLE_MAX_DURATION_MS),
    Math.max(2, Math.ceil(normalized.length / Math.max(6, Math.floor(maxChars / 2)))),
  );
  if (targetChunks <= 1) return [normalized];
  const size = Math.ceil(normalized.length / targetChunks);
  const parts = [];
  for (let i = 0; i < normalized.length; i += size) {
    parts.push(normalized.slice(i, i + size));
  }
  return mergeSubtitleParts(parts);
}

function resolveSubtitleChunks(text, totalDurationMs, maxChars = SUBTITLE_MAX_CHARS) {
  const normalized = text.replace(/\s+/g, "").trim();
  let chunks = splitSubtitleText(normalized, maxChars);
  if (chunks.length === 1 && totalDurationMs > SUBTITLE_MAX_DURATION_MS) {
    const commaSplit = mergeSubtitleParts(
      normalized
        .split(/(?<=[，、])|(?<=——)|(?<=……)/)
        .map((s) => s.trim())
        .filter(Boolean),
    );
    if (commaSplit.length > 1) {
      chunks = commaSplit.flatMap((part) => splitSubtitleText(part, maxChars));
    } else if (normalized.length > 6) {
      chunks = forceSplitByDuration(normalized, totalDurationMs, maxChars);
    }
  }
  return chunks;
}

function ensureEnoughChunks(chunks, normalized, totalDurationMs, maxChars = SUBTITLE_MAX_CHARS) {
  const minChunks = Math.max(1, Math.ceil(totalDurationMs / SUBTITLE_MAX_DURATION_MS));
  let resolved = chunks.length > 0 ? chunks : [normalized];
  let guard = 0;
  while (resolved.length < minChunks && guard < 6) {
    resolved = forceSplitByDuration(
      normalized,
      totalDurationMs,
      Math.max(4, Math.ceil(normalized.length / minChunks)),
    );
    guard += 1;
    if (resolved.length >= minChunks) break;
  }
  return resolved;
}

function buildTimedSubtitleBlocks(text, startMs, totalDurationMs, options = {}) {
  const maxChars = options.maxChars ?? SUBTITLE_MAX_CHARS;
  const minDurationMs = options.minDurationMs ?? SUBTITLE_MIN_DURATION_MS;
  const maxDurationMs = options.maxDurationMs ?? SUBTITLE_MAX_DURATION_MS;
  const normalized = text.replace(/\s+/g, "").trim();
  if (!normalized) return [];

  let chunks = ensureEnoughChunks(
    resolveSubtitleChunks(normalized, totalDurationMs, maxChars),
    normalized,
    totalDurationMs,
    maxChars,
  );
  if (chunks.length === 0) return [];

  const safeTotal = Math.max(totalDurationMs, minDurationMs * chunks.length);
  const weights = chunks.map((chunk) => Math.max(chunk.length, 1));
  const totalWeight = weights.reduce((sum, weight) => sum + weight, 0);
  let cursor = startMs;
  const blocks = [];

  for (let i = 0; i < chunks.length; i++) {
    const remainingChunks = chunks.length - i;
    const remainingMs = startMs + safeTotal - cursor;
    const weighted = Math.round((weights[i] / totalWeight) * safeTotal);
    const minReserve = minDurationMs * (remainingChunks - 1);
    let duration = Math.min(
      maxDurationMs,
      Math.max(minDurationMs, weighted),
      Math.max(minDurationMs, remainingMs - minReserve),
    );
    if (i === chunks.length - 1) {
      duration = Math.max(minDurationMs, Math.min(maxDurationMs, startMs + safeTotal - cursor));
    }
    blocks.push({
      startMs: cursor,
      endMs: cursor + duration,
      text: chunks[i],
    });
    cursor += duration;
  }
  return blocks;
}

function appendSubtitleBlocks(cues, index, blocks) {
  let next = index;
  for (const block of blocks) {
    cues.push(
      `${next}\n${msToSrtTime(block.startMs)} --> ${msToSrtTime(block.endMs)}\n${block.text}`,
    );
    next += 1;
  }
  return next;
}

function parseSrtBlocks(srt) {
  if (!srt.trim()) return [];
  const blocks = [];
  for (const block of srt.trim().split(/\r?\n\r?\n/)) {
    const lines = block.split(/\r?\n/);
    if (lines.length < 3) continue;
    const timing = lines[1];
    const parts = timing.split("-->");
    if (parts.length !== 2) continue;
    blocks.push({
      startMs: srtTimeToMs(parts[0]),
      endMs: srtTimeToMs(parts[1]),
      text: lines.slice(2).join("\n").trim(),
    });
  }
  return blocks;
}

function refineTimedSubtitleBlocks(blocks, offsetMs) {
  const refined = [];
  for (const block of blocks) {
    const startMs = block.startMs + offsetMs;
    const durationMs = Math.max(0, block.endMs - block.startMs);
    const needsSplit =
      durationMs > SUBTITLE_MAX_DURATION_MS || block.text.replace(/\s+/g, "").length > SUBTITLE_MAX_CHARS;
    if (!needsSplit) {
      refined.push({ startMs, endMs: startMs + durationMs, text: block.text });
      continue;
    }
    refined.push(
      ...buildTimedSubtitleBlocks(block.text, startMs, durationMs, {
        minDurationMs: Math.min(SUBTITLE_MIN_DURATION_MS, Math.floor(durationMs / 2)),
      }),
    );
  }
  return refined;
}

function gapSecondsBetween(current, next, currentPanel, nextPanel) {
  if (!next) return 0;
  // Turn gaps are inserted once at panel boundaries; do not add a second gap here.
  if (currentPanel !== nextPanel) return 0;
  const currentDialogue = current.characterId !== "narrator";
  const nextDialogue = next.characterId !== "narrator";
  if (currentDialogue || nextDialogue) return DIALOGUE_GAP_SECONDS;
  return SENTENCE_GAP_SECONDS;
}

async function synthesizeCue(text, voice, rate, outMp3Path) {
  const communicate = new Communicate(text, { voice, rate });
  const audioChunks = [];
  const subMaker = new SubMaker();

  for await (const chunk of communicate.stream()) {
    if (chunk.type === "audio" && chunk.data) {
      audioChunks.push(chunk.data);
    } else if (chunk.type === "WordBoundary") {
      subMaker.feed(chunk);
    }
  }

  if (audioChunks.length === 0) {
    throw new Error(`Edge TTS returned no audio for ${basename(outMp3Path)}`);
  }

  subMaker.mergeCues(SUBTITLE_WORDS_PER_CUE);
  writeFileSync(outMp3Path, Buffer.concat(audioChunks));
  return subMaker.getSrt();
}

function probeDurationSeconds(mediaPath) {
  const out = run("ffprobe", [
    "-v", "error",
    "-show_entries", "format=duration",
    "-of", "default=noprint_wrappers=1:nokey=1",
    mediaPath,
  ]);
  const seconds = Number.parseFloat(out.trim());
  if (!Number.isFinite(seconds)) {
    throw new Error(`Could not read duration for ${mediaPath}`);
  }
  return seconds;
}

function ensureGapFile(audioDir, seconds, cache) {
  const key = seconds.toFixed(2);
  if (cache.has(key)) return cache.get(key);
  const fileName = `_gap_${key.replace(".", "p")}.mp3`;
  const gapPath = join(audioDir, fileName);
  if (!existsSync(gapPath)) {
    run("ffmpeg", [
      "-y", "-f", "lavfi", "-i", "anullsrc=r=24000:cl=mono",
      "-t", key,
      "-c:a", "libmp3lame", "-q:a", "5",
      gapPath,
    ]);
  }
  cache.set(key, fileName);
  return fileName;
}

function srtTimeToMs(time) {
  const m = time.trim().match(/(\d+):(\d+):(\d+),(\d+)/);
  if (!m) return 0;
  return (
    Number(m[1]) * 3_600_000 +
    Number(m[2]) * 60_000 +
    Number(m[3]) * 1_000 +
    Number(m[4])
  );
}

function msToSrtTime(ms) {
  const clamped = Math.max(0, Math.round(ms));
  const hours = Math.floor(clamped / 3_600_000);
  const minutes = Math.floor((clamped % 3_600_000) / 60_000);
  const seconds = Math.floor((clamped % 60_000) / 1_000);
  const millis = clamped % 1_000;
  return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")},${String(millis).padStart(3, "0")}`;
}

function shiftSrt(srt, offsetMs) {
  if (!srt.trim()) return "";
  const blocks = srt.trim().split(/\r?\n\r?\n/);
  const shifted = [];
  let index = 1;
  for (const block of blocks) {
    const lines = block.split(/\r?\n/);
    if (lines.length < 2) continue;
    const timing = lines[1];
    const parts = timing.split("-->");
    if (parts.length !== 2) continue;
    const start = srtTimeToMs(parts[0]) + offsetMs;
    const end = srtTimeToMs(parts[1]) + offsetMs;
    const text = lines.slice(2).join("\n");
    shifted.push(`${index}\n${msToSrtTime(start)} --> ${msToSrtTime(end)}\n${text}`);
    index += 1;
  }
  return shifted.join("\n\n") + (shifted.length ? "\n" : "");
}

function appendTitleCue(cues, index, title, startMs, durationMs) {
  cues.push(
    `${index}\n${msToSrtTime(startMs)} --> ${msToSrtTime(startMs + durationMs)}\n${title}`,
  );
  return index + 1;
}

async function main() {
  const args = parseArgs(process.argv);
  const audioDir = join(args.episodeDir, "audio_edge");
  mkdirSync(audioDir, { recursive: true });

  const coverPath = resolveCoverImage(args.episodeDir, args.coverImage);
  if (!existsSync(coverPath)) {
    throw new Error(`Cover image not found: ${coverPath}`);
  }

  const timeline = loadVoiceTimeline(args.episodeDir);
  const cues = timeline.audioCues ?? [];
  if (cues.length === 0) {
    throw new Error("voice_timeline.json has no audioCues");
  }
  const sectionTitles = args.publish ? new Map() : loadSectionTitles(args.episodeDir);
  const gapCache = new Map();

  console.log(`cues=${cues.length} publish=${args.publish}`);

  const concatLines = [];
  const mergedSrtBlocks = [];
  let offsetMs = 0;
  let srtIndex = 1;
  let lastPanelId = "";

  for (let i = 0; i < cues.length; i++) {
    const cue = cues[i];
    const nextCue = cues[i + 1];
    const panelId = cue.panelId ?? `turn-${i}`;
    const sectionTitle = sectionTitles.get(panelId);

    if (panelId !== lastPanelId) {
      if (lastPanelId) {
        const gapName = ensureGapFile(audioDir, TURN_GAP_SECONDS, gapCache);
        concatLines.push(`file '${gapName}'`);
        offsetMs += Math.round(TURN_GAP_SECONDS * 1000);
      }
      if (sectionTitle && !args.publish) {
        const gapName = ensureGapFile(audioDir, SECTION_TITLE_SECONDS, gapCache);
        concatLines.push(`file '${gapName}'`);
        const titleDurationMs = Math.round(SECTION_TITLE_SECONDS * 1000);
        srtIndex = appendTitleCue(mergedSrtBlocks, srtIndex, `【${sectionTitle}】`, offsetMs, titleDurationMs);
        offsetMs += titleDurationMs;
      }
      lastPanelId = panelId;
    }

    const slug = cue.cueId || `cue-${String(i).padStart(3, "0")}`;
    const mp3Path = join(audioDir, `${slug}.mp3`);
    const voice = voiceForCue(cue);
    const rate = rateForCue(cue, args.rate);
    console.log(`tts ${slug} [${voice}] ${cue.text.slice(0, 28)}...`);
    const srt = await synthesizeCue(cue.text, voice, rate, mp3Path);
    const duration = probeDurationSeconds(mp3Path);
    concatLines.push(`file '${basename(mp3Path)}'`);

    const displaySubtitle = subtitleTextForCue(cue, args.publish);
    const durationMs = Math.round(duration * 1000);
    if (args.publish) {
      const blocks = buildTimedSubtitleBlocks(displaySubtitle, offsetMs, durationMs);
      srtIndex = appendSubtitleBlocks(mergedSrtBlocks, srtIndex, blocks);
    } else {
      const shiftedBlocks = refineTimedSubtitleBlocks(parseSrtBlocks(srt), offsetMs);
      if (shiftedBlocks.length > 0) {
        srtIndex = appendSubtitleBlocks(mergedSrtBlocks, srtIndex, shiftedBlocks);
      } else if (displaySubtitle) {
        const blocks = buildTimedSubtitleBlocks(displaySubtitle, offsetMs, durationMs);
        srtIndex = appendSubtitleBlocks(mergedSrtBlocks, srtIndex, blocks);
      }
    }
    offsetMs += Math.round(duration * 1000);

    const gapSeconds = gapSecondsBetween(cue, nextCue, panelId, nextCue?.panelId ?? panelId);
    if (gapSeconds > 0) {
      const gapName = ensureGapFile(audioDir, gapSeconds, gapCache);
      concatLines.push(`file '${gapName}'`);
      offsetMs += Math.round(gapSeconds * 1000);
    }
  }

  const concatListPath = join(audioDir, "concat.txt");
  writeFileSync(concatListPath, concatLines.join("\n") + "\n", "utf8");

  run("ffmpeg", ["-y", "-f", "concat", "-safe", "0", "-i", "concat.txt", "-c:a", "copy", "combined.mp3"], {
    cwd: audioDir,
  });

  const episodeSrtPath = join(args.episodeDir, "episode_subtitles.srt");
  const renderSrtPath = join(audioDir, "subtitles.srt");
  const mergedSrt = mergedSrtBlocks.join("\n\n") + (mergedSrtBlocks.length ? "\n" : "");
  writeFileSync(episodeSrtPath, mergedSrt, "utf8");
  writeFileSync(renderSrtPath, mergedSrt, "utf8");

  const renderCoverPath = join(audioDir, "cover.png");
  copyFileSync(coverPath, renderCoverPath);

  const subtitleStyle = [
    "FontName=Microsoft JhengHei",
    "FontSize=" + (args.publish ? String(SUBTITLE_FONT_SIZE_PUBLISH) : String(SUBTITLE_FONT_SIZE_DEV)),
    "PrimaryColour=&H00FFFFFF",
    "OutlineColour=&H00000000",
    "BorderStyle=1",
    "Outline=2",
    "Shadow=0",
    "MarginV=56",
    "MarginL=36",
    "MarginR=36",
    "Alignment=2",
    "WrapStyle=2",
  ]
    .join(",")
    .replace(/,/g, "\\,");
  const vf = [
    "scale=trunc(iw/2)*2:trunc(ih/2)*2",
    `subtitles=subtitles.srt:force_style='${subtitleStyle}'`,
  ].join(",");
  const outputVideo = resolve(args.outputVideo);

  run(
    "ffmpeg",
    [
      "-y",
      "-loop", "1", "-i", "cover.png",
      "-i", "combined.mp3",
      "-vf", vf,
      "-c:v", "libx264", "-tune", "stillimage", "-pix_fmt", "yuv420p",
      "-c:a", "aac", "-b:a", "192k",
      "-shortest",
      "-movflags", "+faststart",
      outputVideo,
    ],
    { cwd: audioDir },
  );

  console.log(`wrote=${outputVideo}`);
  console.log(`subtitles=${episodeSrtPath}`);
}

function resolveCoverImage(episodeDir, coverArg) {
  if (coverArg) {
    return resolve(coverArg.startsWith("/") || /^[A-Za-z]:/.test(coverArg) ? coverArg : join(REPO_ROOT, coverArg));
  }
  const exportPath = join(episodeDir, "episode_export.json");
  if (existsSync(exportPath)) {
    const exp = JSON.parse(readFileSync(exportPath, "utf8"));
    if (exp.coverImagePath) {
      return resolve(join(REPO_ROOT, exp.coverImagePath));
    }
  }
  return join(REPO_ROOT, "world/chenxing/assets/cover/chenxing_cover_v1.png");
}

main().catch((err) => {
  console.error(err?.stack || String(err));
  process.exit(1);
});
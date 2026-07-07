# A Group Branch Delivery Failure Fallback Rule V0

Status: `A_GROUP_BRANCH_DELIVERY_FAILURE_FALLBACK_RULE_V0_DRAFT_FOR_REVIEW`

Date: 2026-07-05

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This docs/control rule set prevents A-group branch delivery failures from
turning into silent waiting loops. If A04 or another bounded planning branch
fails to deliver synthesis, A01 must classify the failure, perform at most one
compact retry, then either synthesize from available evidence, reassign one
bounded A-group branch, or issue a precise hold.

Selected process-rule outcome:
`A_GROUP_BRANCH_DELIVERY_FAILURE_FALLBACK_RULE_PATCH_ALIGNED`

## A_GROUP_BRANCH_DELIVERY_FAILURE

`A_GROUP_BRANCH_DELIVERY_FAILURE` is the route state when A01 requested an
A-group branch output and the expected planning artifact did not arrive in a
usable form.

This state is not a human stop condition by default. It is a process
reliability gap that A01 must handle without direct B02/B03 dispatch and
without waiting forever.

## Detection Signals

A01 must classify `A_GROUP_BRANCH_DELIVERY_FAILURE` when any of these signals
appears for an expected A-group branch output:

- `systemError`;
- no agent result;
- userMessage-only turn;
- repeated empty completion;
- stale branch with no synthesis output.

The detection record must name the requested owner, expected artifact,
available A02/A03 evidence, missing A04 or branch output, and whether the gap
affects final-product chain impact or process-risk reduction toward generated
video / 有聲漫畫.

## Retry Policy

One `compact retry` is allowed.

The compact retry must:

- restate the exact requested artifact;
- include only the minimum upstream A02/A03 evidence needed;
- preserve the original docs/control boundary;
- set a clear expected owner and response shape;
- state that the retry must not dispatch B02/B03 directly.

No second retry is allowed unless A01 records new evidence that changes the
failure state. Repeating the same request after a failed compact retry is
treated as a stale waiting loop.

## Fallback Policy

The `fallback policy` after compact retry failure is exactly one of:

1. A01 synthesizes from available A02/A03 evidence when the available reviewed
   evidence is enough to form one bounded docs/control package.
2. A01 reassigns one bounded A-group branch with a narrower request, one owner,
   and one expected output.
3. A01 issues `HOLD_FOR_A_GROUP_PLANNING` with exact owner, blocker, action,
   and the missing evidence needed to continue.

The hold record must not be a vague wait. It must name:

- owner;
- blocker;
- required action;
- expected output;
- freshness limit;
- final-product chain impact or process-risk reduction.

## Freshness Rule

Failed branch output must not block B01 forever if A01 has enough reviewed
A02/A03 evidence to form a bounded docs/control package. A01 may proceed from
available evidence only when:

- the package remains docs/control only;
- the unavailable branch output would not change the safety boundary;
- the package can state assumptions and residual risk;
- B01 remains the only B02/B03 dispatch authority;
- B03 review remains available when B01 dispatches review.

If these conditions are not met, A01 must use `HOLD_FOR_A_GROUP_PLANNING` or
one bounded branch reassignment.

## Authority Separation

- A01 owns branch-failure classification and package formation.
- B01 owns B-group dispatch.
- A02/A03/A04 and future A branches may propose, critique, and synthesize, but
  must not dispatch directly to B02 or B03.
- B02 reports completion to B01 only.
- B03 reviews only when B01 dispatches review.

## Product-Distance Requirement

Every fallback must state either final-product chain impact or process-risk
reduction toward generated video / 有聲漫畫.

Allowed final-product chain references include:

- character assets;
- scene;
- storyboard/camera;
- prompt/asset card;
- voice/subtitle/SFX;
- renderer/package QA;
- process reliability required to keep those chains moving.

If no product-distance or process-risk reduction can be named, A01 must not
approve the fallback as another status-only slice.

## Regression Guards

- branch-failure guard: `systemError`, no agent result, userMessage-only turn,
  repeated empty completion, or stale branch with no synthesis output must be
  classified as `A_GROUP_BRANCH_DELIVERY_FAILURE`.
- retry guard: only one compact retry is allowed without new evidence.
- fallback guard: after retry failure, A01 must synthesize, reassign one
  bounded branch, or issue `HOLD_FOR_A_GROUP_PLANNING` with exact
  owner/blocker/action.
- freshness guard: missing A04 output cannot block B01 forever when enough
  reviewed A02/A03 evidence exists for a safe docs/control package.
- authority guard: A-group windows must not dispatch directly to B02/B03.
- product-distance guard: every fallback must name generated video / 有聲漫畫
  impact or process-risk reduction.

## Boundary Summary

- docs/control only
- no runtime/schema/provider/API/media work
- no SQLite
- no renderer/ffmpeg/TTS
- no web/server
- no sample JSON
- no query instances
- no evidence bundle
- no source-basis selection
- no approval, canon, registry, release, final approval, or production lock
- no 04/05/06/07/08 restart
- no Phase 20/21 restart
- no creation/archive of windows unless separately authorized
- no direct B02/B03 dispatch by A-group windows
- no staging, commit, push, archive/window creation

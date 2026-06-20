# Internet Dice Prefetch And Verification Roadmap

## Summary

VASSAL currently sends each Internet Dice Button roll directly to the selected dice server. For RANDOM.ORG, this means each visible roll can become one API request even when the requested dice are ordinary repeated rolls such as ASL-style `2d6`.

This roadmap captures a future feature track for RANDOM.ORG result prefetching and optional verification metadata. The goals are to reduce request count, preserve hidden results until consumed, and leave room for cryptographic audit support without forcing verification noise into casual play.

## Current Behavior

- `InternetDiceButton` delegates rolls to `DieManager`.
- `DieManager` selects a `DieServer` based on module/button preferences.
- `RandomOrgDiceServer` calls RANDOM.ORG `generateSignedIntegers` for exactly the dice in the current `RollSet`.
- The code currently extracts only the returned integer data. It does not preserve the RANDOM.ORG `random` payload or `signature`.

## Provider Capabilities

The cache design should be provider-aware rather than assuming every Internet
dice service supports the same roll shapes.

- RANDOM.ORG is not limited to six-sided dice in the API path VASSAL uses.
  `RandomOrgDiceServer` calls `generateSignedIntegers` with `min = 1` and
  `max = dieSides`, so `d8`, `d10`, `d20`, and other ordinary die sizes can be
  represented as integer ranges.
- qrandom.io's dice endpoint is d6-only, but `QRandomDiceServer` uses the
  generic number-array endpoint with configurable `min` and `max` values. That
  allows qrandom.io to support ordinary non-d6 dice, subject to provider limits
  and verification behavior.
- Future providers should advertise their supported die ranges, batch limits,
  verification support, and quota behavior explicitly.

## Design Goals

- Reduce RANDOM.ORG request count for repeated rolls.
- Keep prefetched results hidden until consumed by a visible roll.
- Make prefetching configurable and bounded.
- Keep normal chat output clean for casual users.
- Preserve a path for optional verification metadata later.
- Avoid changing local dice button behavior.

## Prefetch Configuration

Add bounded RANDOM.ORG prefetch settings:

- Name: `RANDOM.ORG prefetch pool size`
- Default: `50`
- Minimum: `0`
- Maximum: `500`
- `0` means disabled; fetch exactly the dice requested by the current roll.
- Name: `RANDOM.ORG maximum cached rolls`
- Default: `500`
- Minimum: `0`
- Maximum: `5000`

The pool size controls the target size for each active roll-shape pool. The
maximum cached rolls setting caps the total hidden results across all active
pools so modules with many different dice buttons do not multiply the default
pool size into excessive API usage.

Rationale:

- RANDOM.ORG free developer accounts are request-limited and bit-limited.
- Prefetching saves requests, especially for repeated `2d6` play.
- Prefetching still consumes random bits when the batch is fetched, so the setting must remain visible and configurable.

## Prefetch Model

Use lazy, roll-shape-keyed hidden pools for RANDOM.ORG results.

A roll shape is the normalized request shape, not the physical button:

- selected service
- die count
- die sides/range
- modifier or other deterministic post-processing, if relevant

Examples:

- `1d6`
- `2d6`
- `2d8`
- `3d6+2`

Multiple buttons with the same shape should share a pool. Buttons with
different shapes should not.

Recommended first implementation:

- Create a pool only when that roll shape is actually used.
- Do not eagerly fill every possible die shape in a module.
- When a roll asks for a shape:
  - consume one hidden result from the matching pool if available
  - otherwise fetch a new batch of `max(prefetchPoolSize, 1)` hidden rolls for
    that exact shape
  - then consume one visible roll result
- A visible `2d6` roll should consume one cached `2d6` result, not two values
  from a generic `d6` pool.
- Refill only when a pool is empty in the first version.
- Later, refill lazily in the background when a pool drops below a low-water
  mark, such as 20% of its configured target size.
- Enforce the global maximum cached rolls across all pools before fetching a
  new batch.
- Clear pools when:
  - selected dice server changes
  - API key changes
  - prefetch pool size changes
  - maximum cached rolls changes
  - module/game session ends

The first version should refill synchronously only on demand. Background refill
can be added later if the UI experience warrants it, but it must respect account
request limits and the global cache cap.

## Verification Metadata

RANDOM.ORG signed responses can prove that returned random data came from RANDOM.ORG and was not modified. VASSAL does not currently retain enough information for after-the-fact verification.

Make verification recording optional and separate from prefetching:

- Setting: `Record RANDOM.ORG verification details`
- Default: `false`
- Normal chat/log output should remain concise when disabled.

When enabled, retain enough metadata to verify each visible roll:

- the exact RANDOM.ORG `random` payload for the batch
- the `signature`
- the die range for the batch
- the batch id
- the consumed offsets for each visible roll

For example, a visible `2d6` roll might reference:

- batch `12`
- range `1..6`
- offsets `17-18`

The full payload should not be dumped into ordinary chat by default. Prefer a compact chat/log reference plus a future "copy verification data" or "show verification details" action.

## Phased Implementation

### Phase 1: RANDOM.ORG Batch Result Object

- Replace raw `int[]` parsing with a small value object that can carry:
  - `int[] data`
  - optional raw `random` JSON fragment
  - optional `signature`
- Keep existing behavior unchanged by applying only `data`.
- Add parser tests for response data and signature capture.

### Phase 2: Prefetch Pool

- Add a `RandomOrgRollPool` or equivalent helper.
- Cache hidden results by normalized roll shape.
- Add the prefetch size preference to `DieManager`.
- Add a global maximum cached rolls preference.
- Clear pools when relevant configuration changes.
- Keep `0` as exact-fetch mode.
- Fill pools lazily only after a shape is used.

### Phase 3: Verification Recording

- Add optional verification metadata capture.
- Associate visible rolls with batch id and offsets.
- Decide where verification records should live so they can survive logfile/save workflows when enabled.
- Add UI/reporting only after the storage model is settled.

### Phase 4: UX Polish

- Consider showing pool status in debug/developer output, not ordinary gameplay chat.
- Consider background refill after a successful roll.
- Document quota implications and verification behavior in the reference manual.

## Test Plan

Prefetch tests:

- default prefetch size is `50`
- configured prefetch size clamps to `0..500`
- default maximum cached rolls is `500`
- maximum cached rolls clamps to `0..5000`
- `0` uses exact-fetch behavior
- a `2d6` roll with pool size `50` fetches 50 hidden `2d6` roll
  results and consumes one
- repeated `2d6` rolls reuse the same hidden pool without another request
- two different `2d6` buttons share the same hidden pool
- `1d6`, `2d6`, and `2d8` use separate pools
- RANDOM.ORG pools can fetch non-d6 shapes such as `2d8`
- qrandom.io pools can fetch non-d6 shapes such as `2d8`
- unused roll shapes do not prefetch
- global maximum cached rolls limits total cached results across all pools
- API key/server/pool-size changes clear cached values

Verification tests:

- RANDOM.ORG signed response parser captures data and signature
- consumed roll records include batch id and offsets
- verification metadata is omitted when recording is disabled
- verification metadata is retained when recording is enabled

Regression tests:

- current internet dice report formatting remains unchanged
- RANDOM.ORG missing API key error remains clear
- qrandom.io behavior is unaffected

## Open Questions

- Should verification metadata be stored in the game log, save file, or a separate sidecar structure?
- Should a module author or an individual player control the prefetch size?
- What should the default global cached-roll cap be for free RANDOM.ORG developer accounts?
- Should verification recording be a global player preference, a module setting, or per-button?
- How should VASSAL expose verification data without cluttering normal chat?
- Should background refill be added after the first synchronous implementation?
- Should provider capability metadata be hard-coded per built-in service first,
  or exposed as part of a more general provider/plugin model later?

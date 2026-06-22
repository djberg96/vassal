# Future Feature Roadmap

This roadmap tracks feature ideas and follow-on improvements which are larger
than local warning/TODO cleanup. Items here are not implementation commitments;
they are parking places for designs that need product decisions, compatibility
review, or broader testing before work begins.

## Java 25+ Architecture Modernization

### Virtual-Thread Background Work

- Current state:
  - Swing UI work must continue to happen on the Event Dispatch Thread, but
    blocking work can be moved off heavyweight platform threads.
  - An application-level virtual-thread background task service is in place.
  - Rules Assistant calls, internet dice requests, message-board work, dynamic
    chat connects, server status refreshes, update checks, and tile cache
    maintenance now use virtual-thread-backed background work.
  - `BackgroundTasks` exposes callback APIs whose names make EDT delivery
    explicit at call sites.
  - Focused tests cover callback ordering, error delivery, and cancellation
    behavior for the shared background task service.
- Remaining improvements:
  - Evaluate P2P connection attempts and message handling for virtual-thread
    cleanup after more multiplayer testing.
  - Consider module scanning and heavier image rendering work once profiling
    shows where the UI still blocks.
  - Continue standardizing cancellation and result delivery back to Swing
    components.
- Suggested next steps:
  - Keep using `submitWithCallbacksOnEdt()` when migrating older blocking code
    so Swing thread boundaries stay visible.

### Modern HTTP And External Services

- Current state:
  - A shared `HttpClient` service is in place for new/modernized HTTP callers.
  - Internet dice, Rules Assistant providers, the chat HTTP wrapper, BeanShell
    remote posting, and bug-report upload use the shared layer.
  - Timeout handling and HTTP error-message formatting are centralized for
    those paths.
- Remaining improvements:
  - Audit remaining `HttpURLConnection` usages and classify them as either
    true external-service calls or special-purpose URL/JAR/file loading.
  - Add retries only where product behavior calls for them; avoid hidden retry
    loops for user-triggered actions unless the UI explains what is happening.
  - Continue improving JSON request/response handling where providers need
    richer structured data.
- Suggested next steps:
  - Add more mockable tests for HTTP status handling, timeouts, and malformed
    responses.
  - Prefer the shared layer for new external-service work.

### Language Cleanup

- Current state:
  - The codebase has many older Java idioms from the Java 8-11 era.
  - Initial modernization passes converted small data carriers and type checks
    in movement reporting, scripting, and chat code.
- Proposed improvements:
  - Continue using records for small immutable data carriers such as AI history
    entries, dice results, image metadata, and endpoint descriptors.
  - Use pattern matching for `instanceof` and switch expressions where they
    reduce noisy casts and make enum handling exhaustive.
  - Consider sealed interfaces only for closed internal hierarchies, not
    extension/plugin APIs.
- Suggested implementation path:
  - Apply these patterns opportunistically during normal cleanup.
  - Avoid broad mechanical rewrites unless they reduce real complexity.

### Observability And Performance

- Current state:
  - Heavy modules can stress image rendering, SVG rendering, tile caches, and
    memory usage, but profiling is mostly manual.
  - JFR events now cover Rules Assistant request/index work, SVG rendering
    through JSVG/Batik, and command-line tile slicing/reconstruction.
- Proposed improvements:
  - Add Java Flight Recorder events around module loading, broader image
    rendering/cache paths, and P2P sync.
  - Evaluate Java 25+ runtime/GC behavior for large modules and document
    useful launch flags if any are consistently helpful.
- Suggested implementation path:
  - Continue adding low-overhead JFR events behind stable utility methods.
  - Use profiling runs on known heavy modules before changing defaults.

### Deferred Newer Java Features

- Structured concurrency, Vector API, Foreign Function and Memory API, and
  preview language features may be useful later, but should remain experiments
  until they are final and do not require preview/incubator flags for normal
  builds.

## Game Piece Image Typography

### Rich Outlined Text Styles

- Current state:
  - Game Piece Image text items define a font family plus per-layout size,
    bold, italic, and outline settings.
  - Outline color and thickness are configurable on the text item layout.
  - Legacy labels without a layout-level outline color still use each
    `TextItemInstance` outline color, preserving older generated images.
  - The default one-pixel thickness preserves the previous corner-offset
    outline behavior for old modules.
  - `TextBoxItem` renders through `JTextPane` and does not use the outline
    drawing path.
  - Game Piece Image definitions can now define font families separately from
    per-item size, bold, italic, outline, thickness, and outline color.
- Proposed improvements:
  - Consider outline modes, such as corner-only, full eight-neighbor offset,
    or vector shape stroke.
  - Decide whether `TextBoxItem` should support outlined text or remain plain
    Swing/HTML text.
- Compatibility notes:
  - `TextItem.encode()` and `TextItem.decode()` persist the layout font
    settings. New fields must decode old text-item strings safely with
    sensible defaults.
  - Existing modules may already rely on per-text-item outline color, so that
    color should remain per generated image unless there is a strong reason to
    centralize it later.
- Suggested implementation path:
  - Add characterization tests for current `TextItem` encode/decode
    compatibility.
  - Keep font-family definitions separate from per-layout typography controls.
  - Update `TextItem.drawLabel()` if outline modes or vector-stroked rendering
    are added.
  - Add rendering tests for outlined text using small generated images.
  - Manually verify the Game Piece Image editor with existing modules.

## Game Piece Image Assets

### SVG-First Generated Pieces

- Current state:
  - Game Piece Image generated pieces are written as SVG, making generated
    assets portable and editable outside VASSAL.
  - Generated unit-size symbols are SVG vectors.
  - The piece image selector can include generated images, refresh archive
    listings after image changes, and filter images by bucket.
- Remaining improvements:
  - Decide whether built-in symbol sets should remain code-drawn or move to
    external SVG files which can be added, replaced, and inspected more easily.
  - Consider additional symbol sets only after identifying clear licensing and
    module-author demand.
  - Add more rendering/round-trip tests for generated SVG pieces.

### SVG Renderer Stack

- Current state:
  - SVG rendering uses JSVG by default for normal SVGs.
  - Complex filter-heavy SVGs fall back to Batik when JSVG output is known to
    be inadequate.
  - Rotated SVG pieces preserve vector rendering instead of degrading to a
    blurry raster path.
- Remaining improvements:
  - Use JFR recordings from heavy modules to decide whether additional fallback
    rules or renderer options are needed.
  - Keep Batik available for compatibility unless JSVG can handle the same
    real modules with equal quality and lower memory use.

## Rules Assistant

### Module Rules And Charts

- Current state:
  - The Rules Assistant can read PDF help files, HTML chart text, and image
    chart metadata.
  - Manus tasks are reused per module/provider/key/model so the assistant can
    stay warm between sessions.
  - Session history persists while the module is open, and answers can be
    copied or saved to module notes when available.
- Remaining improvements:
  - Add true image-chart understanding through OCR or provider-specific vision
    upload if the text/metadata approach is not enough for chart-heavy modules.
  - Improve provider abstractions for file upload, task reuse, and capabilities
    such as vision or citation support.
  - Consider indexing rules/charts in a background task before the first
    question, with clear progress and cancellation.

## Fork Planning

### Dannik Working Name

- Current state:
  - Fork planning and LGPL considerations are documented in `FORK_PLAN.md`.
  - `Dannik` is the working fork name, with `Chenshu` and `Samant` as backups.
- Remaining improvements:
  - Decide final branding before broad package/application renaming.
  - Prepare a differences-from-VASSAL document once the fork identity is firm.

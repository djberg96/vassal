# Future Feature Roadmap

This roadmap tracks feature ideas and follow-on improvements which are larger
than local warning/TODO cleanup. Items here are not implementation commitments;
they are parking places for designs that need product decisions, compatibility
review, or broader testing before work begins.

## Java 26 Architecture Modernization

### Virtual-Thread Background Work

- Current state:
  - VASSAL now targets Java 26, but much of the app still reflects older
    Java threading patterns.
  - Swing UI work must continue to happen on the Event Dispatch Thread, but
    blocking work can be moved off heavyweight platform threads.
- Proposed improvements:
  - Add a small application-level background task service backed by virtual
    threads.
  - Use it for Rules Assistant calls, internet dice requests, P2P connection
    attempts, module scanning, PDF/rules indexing, and I/O-heavy image work.
  - Standardize cancellation and result delivery back to Swing components.
- Suggested implementation path:
  - Start with newer isolated features, such as Rules Assistant and internet
    dice, before touching older cross-cutting systems.
  - Keep EDT boundaries explicit in helper APIs.
  - Add focused tests for cancellation, error reporting, and callback ordering.

### Modern HTTP And External Services

- Current state:
  - Newer code still uses some `HttpURLConnection` plumbing.
  - Java's modern `HttpClient` is available and has continued improving since
    Java 11.
- Proposed improvements:
  - Replace ad hoc HTTP callers with a shared, testable HTTP client layer.
  - Apply it first to Rules Assistant providers and internet dice services.
  - Centralize timeout, error-message, retry, and JSON request handling.
- Suggested implementation path:
  - Introduce the shared layer without changing user-visible behavior.
  - Add mockable tests for HTTP status handling, timeouts, and malformed
    responses.

### Language Cleanup

- Current state:
  - The codebase has many older Java idioms from the Java 8-11 era.
- Proposed improvements:
  - Use records for small immutable data carriers such as AI history entries,
    dice results, image metadata, and endpoint descriptors.
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
- Proposed improvements:
  - Add Java Flight Recorder events around module loading, image rendering,
    SVG renderer fallback, tile generation, Rules Assistant requests, and P2P
    sync.
  - Evaluate Java 26 runtime/GC behavior for large modules and document useful
    launch flags if any are consistently helpful.
- Suggested implementation path:
  - Add low-overhead JFR events behind stable utility methods.
  - Use profiling runs on known heavy modules before changing defaults.

### Deferred Java 26 Features

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

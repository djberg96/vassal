# Future Feature Roadmap

This roadmap tracks feature ideas and follow-on improvements which are larger
than local warning/TODO cleanup. Items here are not implementation commitments;
they are parking places for designs that need product decisions, compatibility
review, or broader testing before work begins.

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

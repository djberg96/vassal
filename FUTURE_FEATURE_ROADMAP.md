# Future Feature Roadmap

This roadmap tracks feature ideas and follow-on improvements which are larger
than local warning/TODO cleanup. Items here are not implementation commitments;
they are parking places for designs that need product decisions, compatibility
review, or broader testing before work begins.

## Game Piece Image Typography

### Rich Outlined Text Styles

- Current state:
  - `OutlineFont` now exposes the existing outline flag through the Font Style
    editor.
  - The Font Style sample preview renders the same simple outline effect used
    by `TextItem`.
  - Actual `TextItem` outline color still comes from each
    `TextItemInstance`, not from the shared `FontStyle`.
  - Outline thickness is fixed at the current one-pixel offset behavior.
  - `TextBoxItem` renders through `JTextPane` and does not use the outline
    drawing path.
- Proposed improvements:
  - Add font-style-level outline color.
  - Add configurable outline thickness.
  - Consider outline modes, such as corner-only, full eight-neighbor offset,
    or vector shape stroke.
  - Decide whether `TextBoxItem` should support outlined text or remain plain
    Swing/HTML text.
- Compatibility notes:
  - `FontConfigurer.encode()` and `FontConfigurer.decode()` already persist
    the boolean outline flag. Adding color or thickness would extend the
    serialized font-style format.
  - New fields must decode old font strings safely with sensible defaults.
  - Existing modules may already rely on per-text-item outline color, so a
    font-style-level color should define how it interacts with the existing
    `TextItemInstance` setting.
- Suggested implementation path:
  - Add characterization tests for current `FontConfigurer` encode/decode
    compatibility.
  - Extend `OutlineFont` or replace it with a small immutable text-style value
    object if the configuration grows beyond font plus one flag.
  - Update the Font Style editor and preview together.
  - Update `TextItem.drawLabel()` to use configurable thickness/mode.
  - Add rendering tests for outlined text using small generated images.
  - Manually verify the Game Piece Image editor with existing modules.

# Fork Plan

This document collects the first-pass decisions and release checklist for a
renamed fork of VASSAL. It is a planning note, not legal advice.

## License Review

### Summary

The visible project files do not show a blocker to forking, renaming,
modifying, or redistributing the project, provided the fork preserves the
existing license notices and satisfies the source-distribution obligations.

The main project license is the GNU Lesser General Public License version 2.1,
as shown by `LICENSE` and the root `pom.xml`.

### Practical Obligations

- Keep the LGPL 2.1 license text in the repository and in source/binary
  distributions.
- Preserve existing copyright and license headers.
- Keep the fork source available under LGPL-compatible terms when distributing
  modified binaries.
- Make it clear that the fork is modified and is not the upstream VASSAL
  project.
- Keep notices for bundled third-party code:
  - BeanShell-derived code includes Apache 2.0 and SPL/LGPL notices depending
    on the file.
  - Repackaged ASM code under `vassal-app/src/main/java/bsh/org/objectweb/asm`
    includes a BSD-style license notice.
  - NetBeans wizard code under `org/netbeans/...` carries CDDL header notices.
  - Chess clock artwork is attributed under CC BY in the existing image license
    files.
- Before a public binary release, run the Maven license aggregation workflow
  and include generated dependency notices/licenses with the release artifacts.

### Trademark And Branding Notes

The LGPL covers copyright licensing. It does not grant trademark rights in the
VASSAL name, icon, website identity, or project branding.

Before publishing a renamed fork:

- Replace the product name, application title, package metadata, release
  artifact names, and user-facing branding.
- Replace or substantially redesign the VASSAL icon and installer artwork.
- Keep a factual attribution such as "Forked from VASSAL" where useful, but do
  not imply endorsement by the VASSAL project.
- Avoid names or logos that are visually or phonetically close to VASSAL.

### Release Blockers To Resolve

- Add a consolidated `THIRD_PARTY_NOTICES.md` or equivalent release notice file.
- Ensure CDDL license text is included for the NetBeans wizard sources, because
  those file headers require including the CDDL license with covered code.
- Verify generated binary distributions include dependency licenses.
- Replace VASSAL branding in installers, app bundles, desktop metadata, icons,
  website links, and documentation.

## Name Candidates

Recommended working name: **Dannik**.

Rationale: It is short, direct, easy to pronounce, and has a vassal-adjacent
meaning without being visually or phonetically close to VASSAL. It also has a
personal hook for the fork owner while still reading like a standalone product
name.

Preferred backups:

- **Chenshu**: Distinctive and pronounceable, with a subject/vassal meaning.
- **Samant**: Strong, compact, and historically connected to feudatory/vassal
  language.

Other candidates:

- **Boardwright**: Suggests a craft tool for making and playing board-game
  modules; clearer as a product category but less connected to the VASSAL
  lineage.
- **Tablewright**: Similar craft-tool feel, slightly broader tabletop emphasis.
- **Hexwright**: Strong wargame signal, but too hex-specific for card and board
  games.
- **Counterworks**: Good wargame/counter flavor, but less clear as an engine.
- **Boardforge**: Clear maker language, but likely more crowded.
- **OpenBoard Engine**: Descriptive, but generic and less brandable.

Name selection still needs a proper availability pass: repository name,
package identifiers, domain/social handles if needed, and trademark search.

## Current Fork Differences

The fork has diverged from upstream in several broad areas.

### Modern Java And BeanShell Work

- Java 25+ build/test path is the working baseline.
- BeanShell support has been modernized around current parser generation and
  warning cleanup.
- Parser regeneration and diagnostic suppression are documented so generated
  parser warnings are treated as generated-code noise, not hand-edit targets.
- Additional BeanShell regression tests cover parser and language behavior.

### Editor Warning And TODO Cleanup

- Large batches of unused imports, raw types, resource leaks, nullability
  warnings, stale TODO/FIXME/XXX comments, and generated-code warning noise have
  been triaged or cleaned up.
- Several old FIXME notes were converted into clearer comments or documented
  roadmap items.
- IDE lifecycle mappings were added for Maven/VS Code/Eclipse m2e warnings.

### Peer-To-Peer Networking

- P2P server selection now allows setting the intended P2P network as current.
- P2P local address discovery handles Unix VM environments more robustly.
- Successful P2P connections show a confirmation dialog and close the
  connection list after acknowledgement.
- P2P synchronization now:
  - prompts before overwriting an in-progress game,
  - defers sync until the connection dialog is dismissed,
  - reports completion after restore applies,
  - offers side selection after sync when no side has been claimed.

### Internet Dice

- Internet dice support was re-enabled and modernized around current services.
- Random.org signed API credentials moved from module objects to global user
  preferences so module authors do not accidentally publish private API keys.
- Nonfunctional email-result settings were removed.
- The Internet Dice preferences UI now has cleaner labels, key visibility
  toggling, verification, and layout polish.
- Future verification/prefetch ideas are tracked in `INTERNET_DICE_ROADMAP.md`.

### Game Piece Image Definitions

- Generated game-piece images are now visible to ordinary image selectors.
- Image selectors support buckets, so images can be grouped by archive-relative
  paths instead of living only in one flat namespace.
- Game Piece Image Definitions can write generated images into a bucket.
- Image lists refresh when archive contents change, so newly generated/imported
  images appear without restarting the editor.

### Game Piece Image Typography

- Font definitions were simplified toward reusable font families.
- Label items now own size, bold, italic, outline, outline thickness, and
  outline color at the layout level.
- Legacy labels without layout-level outline color still use per-image
  `TextItemInstance` outline colors for compatibility.
- Outline rendering is covered by tests, including the layout preview path.

### UI And Workflow Polish

- The Module Manager minimizes when editing a module.
- Shared dialogs receive application icons more consistently.
- Message-board send cancellation interrupts the worker.
- Preferences mnemonics and disabled sound preference labels are cleaner.
- Local build/run instructions and `tools/run-vassal.sh` document the current
  source workflow.

### Image, Tile, And Archive Robustness

- Image listings refresh after archive changes.
- Failed image size probes are not cached.
- Tile cache worker thread policy is centralized.
- Tile image reconstitution uses parallel reads where appropriate.
- Several image/archive error paths now log or present clearer failures.

## Rename Checklist

- Choose final product name.
- Replace visible strings:
  - README title and badges
  - application window titles
  - app bundle names
  - installer names
  - desktop metadata
  - release artifact names
- Replace package metadata:
  - Maven group/artifact IDs if desired
  - Flatpak/AppStream IDs
  - Windows Launch4j metadata
  - macOS app bundle metadata
- Replace branding assets:
  - SVG/PNG/ICO/ICNS icons
  - installer art
  - screenshots and documentation images
- Decide whether Java packages remain `VASSAL.*` initially or are migrated
  later. A Java package rename is possible but high-risk and should be a
  separate refactor.
- Add fork attribution and third-party notices.
- Update release documentation to explain compatibility with existing VASSAL
  modules and saves.

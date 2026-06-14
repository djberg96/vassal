# VS Code Task-Tag Warning Roadmap

Source: `/Users/dberger/vscode_vassal_warnings3.json`

This is a triage roadmap for VS Code task-tag diagnostics, not an implementation
commit list. The goal is to sort TODO, FIXME, and comment-line XXX markers into
practical work buckets so cleanup can proceed in batches with focused tests.

## Summary

| Metric | Count |
| --- | ---: |
| Total diagnostics in export | 376 |
| Task-tag diagnostics triaged here | 320 |
| Non-task/generated/compiler-option diagnostics deferred | 56 |

| Bucket | Diagnostic instances | Intent |
| --- | ---: | --- |
| No Longer Relevant | 22 | Stale comments, obsolete version notes, placeholders, or tag noise which should usually be removed after confirming context. |
| Minor Refactoring | 132 | Local behavior-preserving fixes, especially error-message cleanup and simple API cleanup. |
| Medium Refactoring | 90 | Small subsystem work requiring focused tests or manual UI checks. |
| Major Refactoring | 76 | Cross-cutting architecture, compatibility-sensitive behavior, or broad subsystem redesign. |

The counts above are grouped triage counts. Each group below names representative
locations rather than every matching diagnostic.

## No Longer Relevant

### Stale Version And Historical Notes

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/map/ImageSaver.java:54`
  - `vassal-app/src/main/java/VASSAL/build/module/GameState.java:127`
  - `vassal-app/src/main/java/VASSAL/launch/AbstractLaunchAction.java` entries mentioning future removal after dependent releases
- Rationale: These comments refer to past version milestones or old compatibility windows. Since this work targets 3.8 and Java 26, several are now decisions to remove or rewrite, not open design questions.
- Recommended next action: Confirm current callers, remove obsolete comments, and either remove the legacy code or move the remaining compatibility concern into a named test.
- Suggested test scope: Targeted compile plus launch/load smoke tests for any removed launcher or save/load compatibility code.

### Placeholder Text And Non-Actionable XXX Matches

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/Chatter.java:521`
  - `vassal-app/src/main/java/VASSAL/build/module/gamepieceimage/ItemInstance.java:26`
  - message-only `XXX` diagnostics where the match is code, data, documentation, or a naming convention rather than a comment-line task marker
- Rationale: Only `XXX` markers on comment lines should be treated as actionable cleanup. Other `XXX` occurrences can be legitimate placeholder text, documentation examples, UI sentinel values, or naming conventions.
- Recommended next action: Ignore non-comment `XXX` matches. For comment-line `XXX` markers, either convert them into clear TODO/FIXME wording or resolve/remove the stale comment.
- Suggested test scope: No Maven tests for comment-only edits; run `git diff --check`.

### Comments Superseded By Recent Warning Cleanup

- Representatives:
  - simple raw-type, unused-code, or old Java-version notes around files already modernized in the Java 26/BeanShell pass
- Rationale: Some tags were left behind after the implementation was already cleaned up. These should be removed only after checking the nearby code still supports the comment.
- Recommended next action: Audit nearby code and delete stale task tags when the claimed issue is gone.
- Suggested test scope: `mvn -T 8 -Pwarnings -pl vassal-app -am -DskipTests -Dcheckstyle.skip -Dpmd.skip -Dspotbugs.skip compile`

## Minor Refactoring

### Error Message Review Sweep

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/Builder.java:250`
  - `vassal-app/src/main/java/VASSAL/build/module/GameState.java:1519`
  - `vassal-app/src/main/java/VASSAL/chat/node/NodeClient.java:171`
  - `vassal-app/src/main/java/VASSAL/chat/node/SocketHandler.java:126`
  - `vassal-app/src/test/java/VASSAL/chat/CompressorTest.java`
- Rationale: The largest cluster is `FIXME: review error message` plus close variants. Most are local catch blocks where existing VASSAL dialog/logging helpers should be applied consistently.
- Recommended next action: Process by package, replacing vague catches with the appropriate existing helper: `ReadErrorDialog`, `WriteErrorDialog`, `ErrorDialog`, `WarningDialog`, or logger output. Avoid inventing new error UI until a repeated gap is proven.
- Suggested test scope: Targeted tests for changed package plus manual trigger where UI wording changes; full `verify` after each package batch.

### Simple API And Return-Value Cleanup

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/DieManager.java:137`
  - `vassal-app/src/main/java/VASSAL/build/module/DieManager.java:148`
  - `vassal-app/src/main/java/VASSAL/configure/Configurer.java:44`
  - `vassal-app/src/main/java/VASSAL/tools/lang/Reference.java`
- Rationale: These are small, local type-safety or API hygiene items, such as returning empty arrays instead of `null` or narrowing mutable holder types.
- Recommended next action: Change one API at a time, update direct callers, and add tests for null/empty behavior before removing compatibility assumptions.
- Suggested test scope: Targeted unit tests for the owning class and `mvn -T 8 -pl vassal-app -am test-compile`.

### Local Swing And UI Cleanup

- Representatives:
  - `vassal-app/src/main/java/VASSAL/configure/SavedGameUpdaterDialog.java`
  - `vassal-app/src/main/java/VASSAL/build/widget/PanelWidget.java:217`
  - `vassal-app/src/main/java/VASSAL/tools/swing/FlowLabel.java`
  - `vassal-app/src/main/java/VASSAL/tools/UsernameAndPasswordDialog.java`
- Rationale: These are mostly localized UI fixes: obsolete modality APIs, invalid input guardrails, small layout issues, and parent/icon handling.
- Recommended next action: Fix isolated dialogs in small batches and keep behavior unchanged except for the explicit UI defect.
- Suggested test scope: Existing dialog/configurer tests when present, screenshot/manual smoke checks for edited dialogs, then full `verify`.

### Low-Risk Test Debt

- Representatives:
  - `vassal-app/src/main/java/VASSAL/chat/peer2peer/UnitTest.java:15`
  - `vassal-app/src/main/java/VASSAL/chat/peer2peer/ClientTest.java`
  - `vassal-app/src/test/java/VASSAL/counters/EmbellishmentTest.java:34`
  - `vassal-app/src/test/java/VASSAL/tools/lang/MemoryUtilsTest.java`
- Rationale: These are test-organization or test-coverage comments, not production refactors.
- Recommended next action: Move runnable test-like classes under `src/test/java` or delete obsolete manual tests once equivalent JUnit coverage exists.
- Suggested test scope: New/converted tests plus full `mvn -T 8 -pl vassal-app -am test`.

## Medium Refactoring

### Module Loading, Wizard, And First-Run Flow

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/WizardSupport.java:679`
  - `vassal-app/src/main/java/VASSAL/build/module/WizardSupport.java:826`
  - `vassal-app/src/main/java/org/netbeans/api/wizard/displayer/WizardDisplayerImpl.java`
  - `vassal-app/src/main/java/org/netbeans/spi/wizard/GenericListener.java`
- Rationale: These issues involve stream ownership, default save/load behavior, wizard icon handling, and event-listener wiring. They are not huge individually, but they need lifecycle tests and manual flow checks.
- Recommended next action: Start with stream ownership in `WizardSupport`, then isolate wizard UI issues. Preserve save/load compatibility.
- Suggested test scope: Wizard support tests where practical, module creation/load manual test, and `verify`.

### Inventory And Reporting UX

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/Inventory.java:472`
  - `vassal-app/src/main/java/VASSAL/build/module/Inventory.java:473`
  - `vassal-app/src/main/java/VASSAL/build/module/Inventory.java:482`
  - `vassal-app/src/main/java/VASSAL/build/module/Inventory.java:1604`
- Rationale: Inventory export and display behavior crosses UI, file output, and module data. These are likely straightforward but need real module scenarios.
- Recommended next action: Add overwrite/existing-file tests first, then clean the display hack and reassess the safety comment near refresh/update behavior.
- Suggested test scope: Focused Inventory tests plus manual export from an existing module.

### Image Saving, Tiling, And Rendering Cache

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/map/ImageSaver.java:129`
  - `vassal-app/src/main/java/VASSAL/build/module/map/ImageSaver.java:215`
  - `vassal-app/src/main/java/VASSAL/build/module/map/boardPicker/Board.java:507`
  - `vassal-app/src/main/java/VASSAL/build/module/map/boardPicker/board/RegionGrid.java:938`
  - `vassal-app/src/main/java/VASSAL/build/module/map/boardPicker/board/mapgrid/ZoneHighlight.java:142`
- Rationale: This area needs better cancellation, progress estimates, ImageOp usage, and exception propagation. It touches rendering but can be tackled feature by feature.
- Recommended next action: First write characterization tests for successful tile/image generation and failure propagation. Then replace stale image-saving comments with concrete behavior.
- Suggested test scope: ImageSaver/imageop/tilecache tests and manual map screenshot/export on a large board.

### Metadata Parser And XML Utilities

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/metadata/AbstractMetaData.java:449`
  - XML parse error-message review entries in metadata/module loading code
- Rationale: Shared parser synchronization is an implementation smell but can be addressed locally with parser-per-call or a small pool if tests prove it safe.
- Recommended next action: Prefer parser-per-call unless profiling shows parser construction cost matters. Add concurrency coverage before changing synchronization.
- Suggested test scope: Metadata parse tests, concurrent metadata-load test, and module manager smoke test.

### Internationalization And Message Constants

- Representatives:
  - `vassal-app/src/main/java/VASSAL/i18n/LocaleConfigurer.java:42`
  - `vassal-app/src/main/java/VASSAL/i18n/Resources.java:47`
  - `vassal-app/src/main/java/VASSAL/i18n/Resources.java:227`
  - `vassal-app/src/main/java/VASSAL/configure/SoundConfigurer.java:52`
- Rationale: String/key cleanup is broad enough to deserve a batch plan, but most changes should be mechanical once the naming policy is chosen.
- Recommended next action: Define one i18n key policy, replace raw `VASSAL` and obvious string constants in batches, and avoid changing serialized module data.
- Suggested test scope: Existing i18n tests, compile, and manual launch in default locale.

### ADC2 Importer And Legacy Conversion

- Representatives:
  - `vassal-app/src/main/java/VASSAL/tools/imports/adc2/ADC2Module.java`
  - `vassal-app/src/main/java/VASSAL/tools/imports/adc2/MapBoard.java`
  - `vassal-app/src/main/java/VASSAL/tools/imports/adc2/SymbolSetFileFilter.java`
- Rationale: The ADC2 importer has many task tags in one subsystem. Changes are isolated from main play/edit flows but need import fixtures to avoid regressions.
- Recommended next action: Inventory existing ADC2 fixtures, add import characterization tests, then resolve comments by behavior.
- Suggested test scope: ADC2 import tests and manual import of at least one known module.

## Major Refactoring

### Chat Networking Lifecycle

- Representatives:
  - `vassal-app/src/main/java/VASSAL/chat/node/SocketHandler.java:162`
  - `vassal-app/src/main/java/VASSAL/chat/node/SocketHandler.java:163`
  - `vassal-app/src/main/java/VASSAL/chat/node/ServerNode.java:186`
  - `vassal-app/src/main/java/VASSAL/chat/messageboard/MessageBoardControls.java:226`
- Rationale: Thread stopping, stream closure, cancellation, and concurrent module maps are lifecycle and concurrency concerns. Piecemeal cleanup risks hidden hangs.
- Recommended next action: Design explicit ownership for sockets, queues, and worker threads before editing. Then add lifecycle tests using local sockets.
- Suggested test scope: Unit tests for close/cancel behavior, peer-to-peer smoke test, and manual connect/disconnect.

### Drag And Drop / Piece Movement

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/map/PieceMover.java:972`
  - `vassal-app/src/main/java/VASSAL/build/module/map/PieceMover.java:1774`
  - `vassal-app/src/main/java/VASSAL/build/module/map/SetupStack.java:1164`
- Rationale: Replacing AWT drag-and-drop with Swing DnD, clarifying merge behavior, and reducing duplication with setup stacks are deep interaction changes.
- Recommended next action: Do not start with a full DnD rewrite. First add regression tests around merge candidates, stack setup, and ordinary counter movement.
- Suggested test scope: PieceMover/SetupStack tests plus manual module movement test on map, stack, deck, and piece palette.

### Game State Serialization And Streaming

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/BasicLogger.java:461`
  - `vassal-app/src/main/java/VASSAL/build/module/GameState.java:1360`
  - `vassal-app/src/main/java/VASSAL/build/module/GameState.java:1388`
  - `vassal-app/src/main/java/VASSAL/build/module/GameState.java:1663`
- Rationale: Avoiding giant encoded strings means changing command encoding/decoding APIs toward stream-based behavior. That is worthwhile but compatibility-sensitive.
- Recommended next action: Draft stream-capable encode/decode APIs while preserving current string APIs as wrappers until tests cover save, load, log, and replay.
- Suggested test scope: Save/load/log/replay regression suite, large-module performance test, and manual save/log replay.

### GameState Model/View Separation

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/GameState.java:364`
  - `vassal-app/src/main/java/VASSAL/build/module/GameState.java:405`
- Rationale: These comments point to a larger separation between state management and UI actions. That affects module lifecycle and editor/player behavior.
- Recommended next action: Postpone until serialization and launch flows are stable. Start by extracting pure state operations with tests.
- Suggested test scope: GameState unit tests, module load/new-game/close-game manual tests.

### Map, Board, And Grid Geometry

- Representatives:
  - `vassal-app/src/main/java/VASSAL/build/module/map/boardPicker/board/HexGrid.java:501`
  - `vassal-app/src/main/java/VASSAL/build/module/map/boardPicker/board/HexGrid.java:901`
  - `vassal-app/src/main/java/VASSAL/build/module/map/boardPicker/board/mapgrid/SquareGridNumbering.java:225`
  - `vassal-app/src/main/java/VASSAL/build/module/map/Zoomer.java:834`
- Rationale: Grid snapping, vertex math, zoom edge cases, and board drawing order are high-risk because modules depend on historical behavior.
- Recommended next action: Build module-fixture tests before changing geometry. Treat visible behavior changes as 3.8 compatibility decisions.
- Suggested test scope: Geometry unit tests, fixture modules with known snap points, and manual map zoom/snap checks.

### Module Compatibility And Layering Semantics

- Representatives:
  - `vassal-app/src/main/java/VASSAL/counters/Stack.java:262`
  - `vassal-app/src/main/java/VASSAL/counters/Obscurable.java`
  - `vassal-app/src/main/java/VASSAL/counters/Embellishment0.java`
  - `vassal-app/src/main/java/VASSAL/counters/MovementMarkable.java`
- Rationale: Comments here concern old module data, visibility/obscured properties, and layer behavior. Since compatibility may be intentionally broken for 3.8, these need explicit migration decisions.
- Recommended next action: Group by trait, add serialization tests, then decide whether to preserve old module behavior or migrate on load.
- Suggested test scope: Counter serialization tests, old-module fixture load, and manual trait editing.

## Excluded Or Deferred Diagnostics

- `bsh/Parser.java` dead-code and unused-variable diagnostics are generated parser artifacts and are not part of this task-tag roadmap.
- VS Code compiler-option diagnostics such as unused/serial-analysis options being ignored are environment/configuration noise, not source task tags.
- Non-task warnings from the JSON should remain in the ordinary warning cleanup workflow, not this TODO/FIXME/comment-line-XXX roadmap.
- `XXX` occurrences outside comments should be excluded unless surrounding context proves they are intentional task markers.

## Recommended Cleanup Order

1. Remove no-longer-relevant task tags and placeholders.
2. Run the error-message review sweep package by package.
3. Handle minor API cleanup with targeted tests.
4. Pick one medium subsystem and add characterization tests before refactoring.
5. Defer major refactors until each has fixture coverage and a short design note.

## Validation For This Roadmap

- Every grouped entry above references at least one real file/line or a real subsystem from the VS Code export.
- The four bucket counts total 320 task-tag diagnostics.
- The 56 remaining diagnostics are intentionally excluded as non-task/generated/compiler-option diagnostics.
- No production code changes are implied by this file.

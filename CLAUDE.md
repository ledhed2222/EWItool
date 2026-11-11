# EWItool

JavaFX GUI patch editor/librarian for the Akai EWI4000s, talking to the instrument over MIDI sysex. Single Maven module, single package `com.github.ledhed2222.ewitool`.

## Commands

- `mvn javafx:run` - run the app
- `mvn verify` - unit tests (`*Test`, surefire) + integration tests (`*IT`, failsafe)
- `mvn test -Dtest=EWI4000sPatchTest` - single test class
- `scripts/build-macos.sh` - signed macOS app + dmg, used by the release workflow; runs locally (needs a Developer ID cert in the keychain)

## Layout

- `EWI4000sPatch` - one 206-byte sysex patch; `decodeBlob()`/`encodeBlob()` map the blob to fields. Byte offsets are load-bearing; the round-trip test guards them.
- `PatchSet` - `.syx` library files and legacy `.bnk`/`.sqs` import.
- `MidiHandler`/`MidiSender`/`MidiReceiver` - MIDI I/O; uses coremidi4j on macOS.
- `Ui*`, `*Tab` - JavaFX UI. `SharedData` holds app-wide state; `UserPrefs` wraps `java.util.prefs`.

## Testing

Tests must not start the JavaFX toolkit or touch MIDI devices, so they run headless in CI on all three OSes. Avoid code paths that show an `Alert` or call `Platform.runLater` (e.g. `ScratchPad`). Use `@TempDir` for filesystem tests.

## Versioning and release

- Version lives only in `pom.xml`; `UiMain.APP_VERSION` reads it from the jar manifest (`"dev"` under `javafx:run`).
- Java version lives in `.java-version` (read by all workflows) and `pom.xml`.
- Pushing a `v*` tag runs `.github/workflows/build-installers.yaml`, which jpackages macOS (signed + notarized), Windows, and Linux installers onto the GitHub release.
- Don't hardcode dependency versions in workflows or scripts; they read jars from `target/libs` so Dependabot bumps don't break packaging.

## Gotchas

- Patch numbering: users see 0-99, the device stores user #0 as internal 99 (see comment at top of `EWI4000sPatch`).
- `Debugger.VERBOSE` must be `false` in releases.
- `PatchSet.convertLegacy` assumes a full 100-patch bank.

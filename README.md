# EWItool
EWItool is an open source controller and patch editor/librarian for the popular Akai EWI4000s wind synthesizer. Akai's own patch editor for the EWI4000s no longer runs on macOS (and perhaps other OSes) and there is no way to edit the instrument's sounds other than through software.

EWItool includes a fully graphical sound (patch) editor and patch library management. For more information see the [wiki](https://github.com/ledhed2222/EWItool/wiki).

EWItool was originally developed by [Steve Merrony](https://github.com/SMerrony), who not only created the program here, but more importantly decoded the MIDI format to be able to do this development at all. It's now being maintained here.

Installers are published for macOS, Windows, and Linux.

The latest version may always be downloaded [here](https://github.com/ledhed2222/EWItool/releases).

## Development

Requires the JDK version in `.java-version` and Maven.

```
mvn javafx:run   # run the app
mvn verify       # unit (*Test) and integration (*IT) tests
```

## Releasing

1. Bump `<version>` in `pom.xml` (the app reads its version from the jar manifest) and merge to master.
2. Tag and push: `git tag v<version> && git push origin v<version>`.

The `Build Native Installers` workflow builds a signed/notarized macOS `.dmg`, a Windows `.exe`, and Linux `.deb`/`.rpm`, and attaches them to the GitHub release for the tag.

Dependabot opens weekly PRs for Maven dependencies and GitHub Actions; CI must pass before merging.

### Bumping Java version

Update `.java-version` and `maven.compiler.source`/`target` in `pom.xml`.

### Changing icon

Replace `src/main/resources/logo.png` and `src/main/resources/logo.icns`.

# EWItool
EWItool is an open source controller and patch editor/librarian for the popular Akai EWI4000s wind synthesizer. Akai's own patch editor for the EWI4000s no longer runs on macOS (and perhaps other OSes) and there is no way to edit the instrument's sounds other than through software.

EWItool includes a fully graphical sound (patch) editor and patch library management. For more information see the [wiki](https://github.com/ledhed2222/EWItool/wiki).

EWItool was originally developed by [Steve Merrony](https://github.com/SMerrony), who not only created the program here, but more importantly decoded the MIDI format to be able to do this development at all. It's now being maintained here.

This version has been tested on macOS Catalina, but *should* run on any reasonably modern OS. For now, I am just packaging and releasing macOS distributions, but you can feel free to compile the source on your OS and contact me to add the package to the release.

The latest version may always be downloaded [here](https://github.com/ledhed2222/EWItool/releases).

## Deployment

Create a release branch. Merge into it. Bump the version in the following places:

`pom.xml`
`package.json` both the `version` and `jdeploy.jar` fields

Merge into master

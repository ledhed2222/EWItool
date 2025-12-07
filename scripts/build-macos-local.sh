#!/bin/bash
set -e

# Get version from pom.xml
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Building version: $VERSION"

# Find Developer ID certificate
CERT_IDENTITY=$(security find-identity -v -p codesigning | grep "Developer ID Application" | head -1 | sed -n 's/.*"\(.*\)"/\1/p')
echo "Using certificate: $CERT_IDENTITY"

# Build JAR
echo "Building JAR..."
mvn clean package

# Create clean input directory
echo "Preparing jpackage input..."
rm -rf jpackage-input
mkdir -p jpackage-input
cp target/EWItool-$VERSION.jar jpackage-input/

# Remove old app bundle
rm -rf target/EWItool.app

# Create app bundle
echo "Creating app bundle..."

# Get JavaFX version and architecture
JAVAFX_VERSION=23.0.2
ARCH=$(uname -m)
if [ "$ARCH" = "arm64" ]; then
  JAVAFX_ARCH="mac-aarch64"
else
  JAVAFX_ARCH="mac"
fi

# Build module path from Maven repository
JAVAFX_BASE="$HOME/.m2/repository/org/openjfx"
MODULE_PATH="$JAVAFX_BASE/javafx-base/$JAVAFX_VERSION/javafx-base-$JAVAFX_VERSION-$JAVAFX_ARCH.jar"
MODULE_PATH="$MODULE_PATH:$JAVAFX_BASE/javafx-graphics/$JAVAFX_VERSION/javafx-graphics-$JAVAFX_VERSION-$JAVAFX_ARCH.jar"
MODULE_PATH="$MODULE_PATH:$JAVAFX_BASE/javafx-controls/$JAVAFX_VERSION/javafx-controls-$JAVAFX_VERSION-$JAVAFX_ARCH.jar"

jpackage \
  --type app-image \
  --name EWItool \
  --input jpackage-input \
  --main-jar EWItool-$VERSION.jar \
  --main-class com.github.ledhed2222.ewitool.Main \
  --dest target \
  --app-version $VERSION \
  --vendor "Ledhed2222" \
  --icon src/main/resources/logo.icns \
  --module-path "$MODULE_PATH" \
  --add-modules javafx.base,javafx.graphics,javafx.controls

# Sign nested binaries with entitlements
echo "Signing nested binaries..."
find target/EWItool.app/Contents -type f \( -name "*.dylib" -o -name "*.jnilib" -o -perm +111 \) | while read file; do
  echo "  Signing: $file"
  codesign --force --sign "$CERT_IDENTITY" --timestamp --options runtime --entitlements .github/macos/entitlements.plist "$file" 2>/dev/null || true
done

# Sign app bundle with entitlements
echo "Signing app bundle..."
codesign --force --sign "$CERT_IDENTITY" --timestamp --options runtime --entitlements .github/macos/entitlements.plist --deep target/EWItool.app

# Verify signature
echo "Verifying signature..."
codesign --verify --deep --strict --verbose=2 target/EWItool.app

# Create DMG
echo "Creating DMG..."
rm -f target/EWItool-$VERSION.dmg
hdiutil create -volname EWItool -srcfolder target/EWItool.app -ov -format UDZO target/EWItool-$VERSION.dmg

# Sign DMG
echo "Signing DMG..."
codesign --force --sign "$CERT_IDENTITY" --timestamp target/EWItool-$VERSION.dmg

# Verify DMG
codesign --verify --verbose=4 target/EWItool-$VERSION.dmg

echo ""
echo "✅ Build complete!"
echo "App bundle: target/EWItool.app"
echo "DMG: target/EWItool-$VERSION.dmg"
echo ""
echo "Test the app with: target/EWItool.app/Contents/MacOS/EWItool"

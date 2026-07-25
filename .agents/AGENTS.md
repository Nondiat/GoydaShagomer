# GoydaShagomer Rules & Workflows

## Android GitHub Actions Release APK Workflow
This project utilizes `.github/workflows/build-apk.yml` for automated Release APK builds and signing:
- **Runner**: `ubuntu-latest`
- **Permissions**: `contents: write`
- **Steps**:
  1. `actions/checkout@v4`
  2. `actions/setup-java@v4` (JDK 17, temurin)
  3. `android-actions/setup-android@v3`
  4. Automatic self-signed keystore generation or Secrets decoding
  5. `./gradlew assembleRelease --no-daemon`
  6. `zipalign` alignment and `apksigner` V2 + V3 scheme signing
  7. `actions/upload-artifact@v4` artifact packaging (`GoydaShagomer`)
  8. `softprops/action-gh-release@v2` tag release publishing

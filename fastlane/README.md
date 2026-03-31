fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android metadata

```sh
[bundle exec] fastlane android metadata
```

Upload metadata & screenshots to Play Store (no APK)

### android build

```sh
[bundle exec] fastlane android build
```

Build signed release AAB

### android internal

```sh
[bundle exec] fastlane android internal
```

Build & upload AAB to internal testing track

### android alpha

```sh
[bundle exec] fastlane android alpha
```

Promote internal → alpha

### android production

```sh
[bundle exec] fastlane android production
```

Promote alpha → production

### android release

```sh
[bundle exec] fastlane android release
```

Bump versionCode and versionName, then build internal

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).

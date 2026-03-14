# RemotePad – Mouse Jiggler & Bluetooth Trackpad

> Keep your PC awake and your Teams/Slack status **Active** — then use your phone as a wireless mouse.

---

## Features

### 😴 Mouse Jiggler
- Prevents PC sleep, screensaver, and lock screen automatically
- **Stops Microsoft Teams & Slack from switching you to "Away"**
- Configurable movement interval (1–120 seconds) and range
- Random movement pattern for natural, undetectable behavior
- Runs silently in the background even when you switch apps
- Persistent foreground service with status notification

### 🖱️ Bluetooth Trackpad
- Smooth cursor control with adjustable sensitivity
- Single tap → Left click
- Two-finger tap → Right click
- Recognized as a real HID mouse — no drivers needed on PC

### 📡 Bluetooth HID
- Pure Bluetooth connection — no Wi-Fi, no PC software, no cables
- Works with Windows, macOS, Linux, Android TV and more
- Remembers last connected device for instant reconnect

### 🔍 Device Management
- Scan and pair Bluetooth devices directly from the app
- Filter to show computers only (default on)
- Unpair devices with one tap
- Auto-connect to last used device on launch

---

## Screenshots

> *(Coming soon)*

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | Clean Architecture (Data / Domain / Presentation) |
| DI | Hilt |
| Async | Coroutines + StateFlow / SharedFlow |
| Persistence | DataStore Preferences |
| BT Protocol | Bluetooth HID Device API (`BluetoothHidDevice`) |
| CI/CD | Fastlane + Google Play Supply |

---

## Requirements

- Android **9.0 (Pie)** or higher — `minSdk 28`
- Bluetooth 4.0+ on your phone
- PC or device with Bluetooth support

---

## Build

### Debug
```bash
./gradlew assembleDebug
```

### Release (signed AAB)
1. Copy the keystore template and fill in your values:
   ```bash
   cp keystore.properties.example keystore.properties
   ```
2. Edit `keystore.properties`:
   ```properties
   storeFile=/path/to/your.jks
   storePassword=your_store_password
   keyAlias=your_key_alias
   keyPassword=your_key_password
   ```
3. Build:
   ```bash
   ./gradlew bundleRelease
   ```

---

## Fastlane

### Setup
Install dependencies:
```bash
bundle install
```

Set your Google Play API key in `~/.zshrc` (or `~/.bashrc`):
```bash
export SUPPLY_JSON_KEY="/path/to/service-account.json"
```

### Lanes

| Command | Description |
|---------|-------------|
| `fastlane build` | Build signed release AAB |
| `fastlane internal` | Build + upload to internal testing |
| `fastlane alpha` | Promote internal → alpha |
| `fastlane production` | Promote alpha → production |
| `fastlane metadata` | Upload store listing & screenshots only |
| `fastlane release version:1.1` | Bump version + build + upload internal |

> **Note:** First upload to Google Play must be done manually via the Play Console web UI.

---

## Project Structure

```
app/src/main/java/com/godofcodes/androidmouse/
├── data/
│   ├── bluetooth/       # BluetoothHidManager, HID descriptor & report builder
│   ├── local/           # DataStore (preferences, jiggler config)
│   └── repository/      # Repository implementations
├── domain/
│   ├── model/           # BtDevice, ConnectionState, JigglerConfig, MouseEvent
│   ├── repository/      # Repository interfaces
│   └── usecase/         # One use case per action
├── presentation/
│   ├── navigation/      # NavGraph, Screen sealed class
│   ├── theme/           # Material3 theme
│   └── ui/
│       ├── scan/        # Device scan & pair screen
│       ├── touchpad/    # Trackpad + jiggler toggle
│       ├── jiggler/     # Jiggler config screen
│       └── components/  # MouseButtonBar, TouchpadSurface
├── di/                  # Hilt modules
└── service/             # MouseForegroundService
```

---

## Store Listing

Play Store metadata lives in `fastlane/metadata/android/` and is available in **9 languages**:
`en-US`, `tr-TR`, `de-DE`, `fr-FR`, `es-ES`, `pt-BR`, `ru-RU`, `ja-JP`, `ko-KR`

---

## License

```
Copyright 2025 guvencan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

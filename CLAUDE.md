# RemotePad - Claude Code Guidelines

## Project Overview
Android application: **RemotePad** (package: `com.godofcodes.androidmouse`)
- Language: **Kotlin**
- UI: **Jetpack Compose**
- Architecture: **Clean Architecture**
- Play Store app name: **RemotePad – Mouse Jiggler & BT**

---

## Architecture: Clean Architecture

### Layer Structure
```
app/
└── src/main/java/com/godofcodes/androidmouse/
    ├── data/           # Data Layer
    │   ├── local/      # DataStore
    │   ├── bluetooth/  # BluetoothHidManager (HID device profile)
    │   ├── repository/ # Repository implementations
    │   └── mapper/     # Data <-> Domain mappers
    ├── domain/         # Domain Layer (pure Kotlin, no Android deps)
    │   ├── model/      # Domain models / entities
    │   ├── repository/ # Repository interfaces
    │   └── usecase/    # Use cases (one action per class)
    ├── presentation/   # Presentation Layer
    │   ├── ui/         # Composable screens & components
    │   ├── viewmodel/  # ViewModels (one per screen/feature)
    │   └── navigation/ # NavGraph, Routes
    └── di/             # Hilt dependency injection modules
```

### Dependency Rule
- **Domain** has zero Android/framework dependencies
- **Data** depends on Domain (implements interfaces)
- **Presentation** depends on Domain (calls use cases via ViewModel)
- **Never** let Domain or Data import from Presentation

---

## Kotlin Best Practices

- Use **data classes** for models, **sealed classes** for UI state & results
- Prefer **`val`** over `var`; use `var` only when mutation is required
- Use **coroutines + Flow** for async; no RxJava
- Use **`Result<T>`** or sealed `Resource<T>` for error handling in domain/data
- Extension functions for reusable utilities
- Null safety: prefer **safe calls (`?.`)** and **`let`/`run`** scopes over `!!`
- Use **`StateFlow`** / **`SharedFlow`** in ViewModels; never expose mutable state

---

## Jetpack Compose Best Practices

- **Stateless composables** wherever possible; hoist state up
- One composable = one responsibility (small, focused functions)
- Use **`remember`** and **`derivedStateOf`** to avoid recomposition
- Screen composables collect from ViewModel via **`collectAsStateWithLifecycle()`**
- Use **Material3** components; define theme in `ui/theme/`
- Previews with **`@Preview`** for all standalone composables
- Navigation: **single-activity** with Compose Navigation; typed routes via sealed class
- All user-visible strings must use `stringResource()` — no hardcoded strings in Compose

---

## Dependency Injection

- Use **Hilt** exclusively
- One `@Module` per layer (DataModule, DomainModule, PresentationModule)
- Inject interfaces, not implementations

---

## Testing

- Unit tests for **UseCases** and **ViewModels**
- Use **MockK** for mocking; **Turbine** for Flow testing
- UI tests with **Compose Testing** APIs
- Repository tests with **in-memory Room** database

---

## String Resources

- All user-visible strings must live in `app/src/main/res/values/strings.xml`
- Turkish translations in `app/src/main/res/values-tr/strings.xml`
- Format strings use Android positional args: `%1$s`, `%1$d`
- Never hardcode UI text in Kotlin/Compose source files

---

## Release Build Rules

### Signing
- Signing config reads from `keystore.properties` (git-ignored)
- Copy `keystore.properties.example` → `keystore.properties` and fill in values
- Keys: `storeFile`, `storePassword`, `keyAlias`, `keyPassword`
- Never commit `keystore.properties`, `*.jks`, or `*.keystore`

### ProGuard / R8
- Rules file: `app/proguard-rules.pro`
- Release build: `isMinifyEnabled = true`, `isShrinkResources = true`
- Debug build: `isMinifyEnabled = false`
- Keep domain models: `-keep class com.godofcodes.androidmouse.domain.model.** { *; }`
- Keep Hilt, DataStore, Coroutines, Compose classes per `proguard-rules.pro`
- Always test release builds after adding new libraries (new keep rules may be needed)

### Build types
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    signingConfig = signingConfigs.getByName("release")
    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
}
debug {
    isMinifyEnabled = false
}
```

---

## Fastlane

### Setup
- `fastlane/Appfile`: package name + JSON key path via env var
- Google Play API key: set `SUPPLY_JSON_KEY` in `~/.zshrc`:
  ```
  export SUPPLY_JSON_KEY="/path/to/service-account.json"
  ```
- Run `source ~/.zshrc` after editing

### Lanes
| Lane | Command | Description |
|------|---------|-------------|
| `build` | `fastlane build` | Build signed release AAB |
| `internal` | `fastlane internal` | Build + upload to internal testing |
| `alpha` | `fastlane alpha` | Promote internal → alpha |
| `production` | `fastlane production` | Promote alpha → production |
| `metadata` | `fastlane metadata` | Upload store listing & screenshots only |
| `release` | `fastlane release version:1.1` | Bump version + build + upload internal |

### Store Metadata
- Metadata lives in `fastlane/metadata/android/{locale}/`
- Locales: `en-US`, `tr-TR`, `de-DE`, `fr-FR`, `es-ES`, `pt-BR`, `ru-RU`, `ja-JP`, `ko-KR`
- Files per locale: `title.txt`, `short_description.txt`, `full_description.txt`, `changelogs/`
- **First upload to Play Console must be done manually** (web UI); subsequent uploads via `fastlane internal`

---

## App Store SEO Guidelines

### Google Play Store Optimization
- **App title** (max 30 chars): `RemotePad – Mouse Jiggler & BT` — primary keyword first
- **Short description** (max 80 chars): Jiggler-first hook + trackpad benefit + keywords
- **Full description** (max 4000 chars):
  - First 167 chars shown before "Read more" — lead with jiggler value prop
  - Jiggler section first, then trackpad, then BT HID details
  - Use keyword-rich paragraphs naturally (no keyword stuffing)
  - Include feature list with bullet points
  - Add call-to-action at the end
- **Keywords**: "mouse jiggler", "remote mouse", "bluetooth trackpad", "pc controller"
- **Screenshots**: First 2-3 screenshots must convey jiggler + trackpad value
- **Feature graphic**: 1024x500 px, brand-consistent, no text overlap with Play badge
- **App icon**: Distinct, recognizable at small sizes, no text
- **Category**: Tools
- **Localization**: 9 languages configured in fastlane metadata
- **Rating & Reviews**: Prompt for rating after positive user actions (not on first launch)

### strings.xml SEO Fields
```xml
<!-- Store listing metadata — keep in sync with Play Console -->
<string name="store_title">RemotePad – Mouse Jiggler &amp; BT</string>
<string name="store_short_description">…</string>
```

---

## Gradle Build Rules

> **MANDATORY: After every code change, a Gradle build MUST be triggered automatically via the PostToolUse hook.**

Build command: `./gradlew assembleDebug`

If build fails:
1. Read the full error output
2. Fix the root cause before proceeding
3. Do NOT skip or ignore build errors

---

## Git Rules

> **MANDATORY: Never commit or push without explicit user approval.**

- Always show the diff summary and ask "Commit & push?" before any git operation
- Commit message format: `type(scope): description` (Conventional Commits)
  - Types: `feat`, `fix`, `refactor`, `test`, `chore`, `docs`
- Never use `--no-verify` or force push unless the user explicitly requests it
- Never amend a published commit

---

## Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Max line length: **120 characters**
- Use **trailing commas** in multi-line expressions
- No unused imports or variables
- Organize imports: Android → Third-party → Project

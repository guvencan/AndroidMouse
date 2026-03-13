# AndroidMouse - Claude Code Guidelines

## Project Overview
Android application: **AndroidMouse**
- Language: **Kotlin**
- UI: **Jetpack Compose**
- Architecture: **Clean Architecture**

---

## Architecture: Clean Architecture

### Layer Structure
```
app/
└── src/main/java/com/androidmouse/
    ├── data/           # Data Layer
    │   ├── local/      # Room DB, DataStore
    │   ├── remote/     # Retrofit, API services
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

## App Store SEO Guidelines

### Google Play Store Optimization
- **App title** (max 30 chars): Include primary keyword near the start
- **Short description** (max 80 chars): Hook + primary benefit + 1-2 keywords
- **Full description** (max 4000 chars):
  - First 167 chars are shown before "Read more" — make them count
  - Use keyword-rich paragraphs naturally (no keyword stuffing)
  - Include feature list with bullet points
  - Add call-to-action at the end
- **Keywords**: Research via Google Play Console, competitor analysis
- **Screenshots**: First 2-3 screenshots must convey core value proposition
- **Feature graphic**: 1024x500 px, brand-consistent, no text overlap with Play badge
- **App icon**: Distinct, recognizable at small sizes, no text
- **Category**: Choose the most specific applicable category
- **Localization**: Translate store listing for top target markets (EN, TR at minimum)
- **Rating & Reviews**: Prompt for rating after positive user actions (not on first launch)
- **Update frequency**: Regular updates signal active maintenance to the algorithm

### strings.xml SEO Fields (to be added)
```xml
<!-- Store listing metadata — keep in sync with Play Console -->
<string name="store_title">AndroidMouse</string>
<string name="store_short_description">...</string>
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
- Use **trailing commas** in multi-line expressionsPAtter
- No unused imports or variables
- Organize imports: Android → Third-party → Project
# cURLer — "Paste curl" feature (full core flow)

## Context

cURLer is a fresh, single-Activity Jetpack Compose Android app (`com.hrithikvish.curler`) with no feature code yet — just the stock "Empty Activity" template. The goal is to build a pocket API client: paste a raw `curl` command, have it parsed/validated into an editable request, review it, send it, and see the response — matching a provided wireframe (`~/Downloads/curler-wireframes.html`) exactly in layout/structure.

Two things from the source materials needed reconciling before planning:
- The wireframe's visual language is a colorful Material-expressive palette (indigo/lime/coral/amber/teal). The provided `CurlerTheme.kt` is a deliberately **monochrome** theme (13-stop grayscale ramp + exactly two signal colors, with an explicit comment forbidding color-as-decoration). **Decision: keep the theme monochrome as authored.** All wireframe components are adapted to grayscale + `SignalError`/`SignalSuccess`, using outline/filled/dashed/weight treatments instead of hue to differentiate things like HTTP methods.
- Scope: build the **full core flow** (Paste/Build → Review → Send → Response) plus a minimal **Home** (history list) and navigation, per your direction. The wireframe's **Environments** screen is out of scope for this pass (distinct feature, its own effort later).

Confirmed build choices: **Hilt** for DI, **Room** for history persistence (survives restarts), bundle **JetBrains Mono only** (code/curl/JSON/URL text) and use system font (Roboto) for body/headings, **Environments omitted** entirely (including its Home entry point).

---

## 1. Gradle changes

`gradle/libs.versions.toml` — add:
```toml
[versions]
navigationCompose = "2.9.6"
lifecycleViewmodelCompose = "2.9.4"
okhttp = "5.1.0"
kotlinxSerializationJson = "1.9.0"
kotlinxCoroutines = "1.10.2"
ksp = "2.2.10-2.0.2"
hilt = "2.57"
hiltNavigationCompose = "1.2.0"
room = "2.7.2"

[libraries]
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

`app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

dependencies {
    // existing...
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

Root `build.gradle.kts` — add `alias(libs.plugins.ksp) apply false` and `alias(libs.plugins.hilt) apply false`.

`app/src/main/AndroidManifest.xml`:
- Add `<uses-permission android:name="android.permission.INTERNET" />`.
- Point `android:name` on `<application>` at the new `CurlerApplication` class.

---

## 2. Package structure

```
app/src/main/java/com/hrithikvish/curler/
├── CurlerApplication.kt                      [@HiltAndroidApp]
├── MainActivity.kt                           [@AndroidEntryPoint — hosts CurlerTheme { CurlerNavHost() }]
│
├── data/
│   ├── model/
│   │   ├── HttpMethod.kt
│   │   ├── ParsedCurlRequest.kt               [sealed: Success/Error]
│   │   ├── HttpRequestModel.kt
│   │   ├── HttpResponseModel.kt
│   │   └── HistoryEntry.kt                    [domain model, not the Room entity]
│   │
│   ├── curlparser/
│   │   ├── CurlTokenizer.kt
│   │   └── CurlParser.kt
│   │
│   ├── network/
│   │   ├── RequestExecutor.kt                 [interface]
│   │   └── OkHttpRequestExecutor.kt           [@Inject constructor(OkHttpClient)]
│   │
│   ├── json/
│   │   └── JsonPrettyPrinter.kt
│   │
│   ├── query/
│   │   └── QueryParamsParser.kt               [manual string parsing, no android.net.Uri — JVM testable]
│   │
│   └── history/
│       ├── HistoryEntity.kt                   [@Entity, headers/body stored as JSON via TypeConverter]
│       ├── HistoryDao.kt                      [@Dao — insert, delete, observeAll(): Flow<List<HistoryEntity>>]
│       ├── CurlerDatabase.kt                  [@Database]
│       ├── Converters.kt                      [@TypeConverter for List<Pair<String,String>> <-> JSON string]
│       └── HistoryRepository.kt               [wraps Dao, maps Entity <-> domain HistoryEntry]
│
├── di/
│   ├── NetworkModule.kt                       [@Provides OkHttpClient w/ timeouts]
│   ├── DatabaseModule.kt                      [@Provides CurlerDatabase, HistoryDao]
│   └── RepositoryModule.kt                    [@Binds RequestExecutor -> OkHttpRequestExecutor]
│
├── ui/
│   ├── theme/
│   │   ├── Color.kt                           [grayscale ramp + SignalError/SignalSuccess]
│   │   ├── Theme.kt                           [CurlerLightColorScheme/CurlerDarkColorScheme + CurlerTheme composable]
│   │   ├── Type.kt                            [CurlerTypography + standalone codeMono TextStyle, JetBrains Mono via res/font/]
│   │   └── Shape.kt                           [CurlerShapes + PillShape constant]
│   │
│   ├── navigation/
│   │   ├── CurlerDestinations.kt              [@Serializable routes: Home, requestFlow{NewRequest,Review,Response}, historyFlow{Review,Response}]
│   │   └── CurlerNavHost.kt
│   │
│   ├── components/
│   │   ├── MethodChip.kt                      [reusable monochrome method differentiation]
│   │   ├── SegmentedControl.kt
│   │   ├── KeyValueRow.kt
│   │   ├── HeaderEditRow.kt
│   │   ├── JsonText.kt                        [AnnotatedString, weight/tone "highlighting"]
│   │   ├── StatusCard.kt
│   │   └── EmptyState.kt
│   │
│   └── screens/
│       ├── home/{HomeScreen.kt, HomeViewModel.kt, HomeUiState.kt}
│       ├── newrequest/{NewRequestScreen.kt, PasteTab.kt, BuildTab.kt, RequestFlowViewModel.kt, RequestFlowUiState.kt}
│       ├── review/ReviewScreen.kt              [shared by both flows via a small ReviewCapable interface]
│       └── response/ResponseScreen.kt          [shared by both flows]
```

Test sources mirror `data/curlparser`, `data/json`, `data/query`, plus `ui/screens/newrequest/RequestFlowViewModelTest.kt` (using a fake `RequestExecutor`/`HistoryRepository`, `kotlinx-coroutines-test`).

---

## 3. Theme integration

- Fix package: `com.curler.ui.theme` → `com.hrithikvish.curler.ui.theme`.
- Adopt the provided file's composable name `CurlerTheme`; update `MainActivity.kt`'s call site (was `CURLerTheme { ... }`) and delete the stub `Greeting`/`GreetingPreview`.
- Split per the existing template convention: grayscale constants + `SignalError`/`SignalSuccess` → `Color.kt` (replacing the stock purple palette); `CurlerLightColorScheme`/`CurlerDarkColorScheme` + the `CurlerTheme` composable → `Theme.kt`; typography → `Type.kt`; new `Shape.kt`.
- **Type.kt**: build out `titleLarge`/`titleMedium`/`labelLarge`/`bodyLarge`/`bodyMedium`/`labelSmall` on `FontFamily.Default` (Roboto), plus a standalone `val codeMono = TextStyle(fontFamily = JetBrainsMono, ...)` applied directly (not via `MaterialTheme.typography`) wherever the wireframe uses JetBrains Mono: paste textarea, header/query values, JSON body, URL text, chip labels.
  - Bundle JetBrains Mono Regular + Medium as `.ttf` under `app/src/main/res/font/`, declare `val JetBrainsMono = FontFamily(Font(R.font.jetbrains_mono_regular, FontWeight.Normal), Font(R.font.jetbrains_mono_medium, FontWeight.Medium))`.
- **Shape.kt**: map wireframe radii to the closest M3 `Shapes()` slots (`small`=14dp/`--r-sm`, `medium`=20dp/`--r-md`, `large`=32dp/`--r-lg`) and define `val PillShape = RoundedCornerShape(percent = 50)` as a standalone constant applied directly via each pill component's `shape =` param (buttons, chips, segmented-control segments) — M3's global `Shapes()` has no pill slot.

---

## 4. cURL parser

```kotlin
enum class HttpMethod { GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS }

sealed class ParsedCurlRequest {
    data class Success(
        val method: HttpMethod,
        val rawMethod: String,
        val url: String,
        val headers: List<Pair<String, String>>,
        val body: String?,
        val bodyIsValidJson: Boolean,
    ) : ParsedCurlRequest()
    data class Error(val message: String) : ParsedCurlRequest()
}
```

Direct Kotlin port of the wireframe's JS `parseCurl`/`tokenize` (used as reference algorithm — the JS itself is a browser-only demo, not shipped):

- **Tokenizer**: regex `"([^"]*)"|'([^']*)'|(\S+)"`, using `MatchGroupCollection` nullable-group checks (`m.groups[1]?.value ?: m.groups[2]?.value ?: m.groups[3]?.value ?: ""`) since Kotlin regex groups are null (not empty string) when non-participating — a real Kotlin-vs-JS porting subtlety.
- **Parser rules**: collapse `\`+newline → space and trim; require `tokens[0] == "curl"`; walk tokens handling `-X`/`--request`, `-H`/`--header` (split on first `:`, error if missing), `-d`/`--data`/`--data-raw`/`--data-binary` (last wins), `-u`/`--user` (→ `Authorization: Basic <base64(user:pass)>` — **properly base64-encode this**, unlike the wireframe's JS placeholder text, since it's a real header sent over the wire), `--url`, and a bare non-flag token as URL if none set yet. Unrecognized flags are skipped (matches reference behavior; known quirk that an unrecognized flag's value could be misread as URL — accepted, documented, not over-engineered). No URL → error; URL not matching `^https?://` → error. Method defaults to POST if body present else GET. Body is JSON-parsed via `kotlinx.serialization.json.Json.parseToJsonElement` for `bodyIsValidJson` (failure is informational, not a fatal parse error — raw body still carried through).

**Test cases** (`CurlParserTest.kt`, write first — highest value, pure JVM): happy path POST w/ headers+body; method inference (GET vs POST); multiple `-H` accumulate in order; header missing colon → exact error message; missing `curl` prefix; no URL; URL missing scheme; `-u user:pass` → correctly base64-encoded header; single/double-quoted tokens with embedded spaces; multi-line with `\` continuations; invalid JSON body → `bodyIsValidJson=false`, raw body preserved; empty/whitespace input; `--data-raw`/`--data-binary` parity with `-d`; repeated `-d` (last wins); case-insensitive method.

---

## 5. HTTP execution

```kotlin
data class HttpRequestModel(val method: HttpMethod, val url: String, val headers: List<Pair<String,String>> = emptyList(), val body: String? = null)
data class HttpResponseModel(val statusCode: Int, val statusMessage: String, val headers: List<Pair<String,String>>, val body: String, val isBodyJson: Boolean, val durationMs: Long, val sizeBytes: Long)
interface RequestExecutor { suspend fun execute(request: HttpRequestModel): Result<HttpResponseModel> }
```

`OkHttpRequestExecutor` (Hilt-injected `OkHttpClient`, provided by `di/NetworkModule.kt` with `connectTimeout=15s`, `readTimeout=30s`, `writeTimeout=15s`): builds `okhttp3.Request` from the model (GET/HEAD/DELETE without body → no `RequestBody`; POST/PUT/PATCH → `body.toRequestBody(contentType)`, content type from the `Content-Type` header if present else `application/json; charset=utf-8`; empty-but-required body → `"".toRequestBody(null)`), executes via `withContext(Dispatchers.IO) { client.newCall(request).execute() }` inside try/catch for `IOException` → `Result.failure`. Success path measures elapsed ms, reads body string, computes size from `contentLength()` (fallback to byte-array size), and JSON-validity for display. Network failures render via a `SignalError`-tinted state on the Response screen — the one place beyond DELETE where the signal color is justified.

---

## 6. Navigation & shared state

Type-safe `androidx.navigation.compose` routes (`@Serializable` objects/data classes). Two nested graphs sharing screens:

- **`requestFlow`** (`NewRequest` start → `Review` → `Response`), driven by a single `@HiltViewModel RequestFlowViewModel` scoped to the graph's back-stack entry (`hiltViewModel(navController.getBackStackEntry("requestFlow"))`), holding both Paste-tab and Build-tab state plus the canonical `HttpRequestModel`/`HttpResponseModel` once built/sent. This is what makes tab-switching non-destructive: both tabs' state live in the same VM instance for the life of the graph segment.
- **`historyFlow`** (`Review` → `Response` only), entered directly from a Home history-row tap, driven by a lighter `HistoryReviewViewModel` that just loads a `HistoryEntry` by id — no tab state needed. `ReviewScreen`/`ResponseScreen` are written once and reused by both flows via a small shared interface (`ReviewCapable`: exposes `request: StateFlow<HttpRequestModel>` and `suspend fun send(): Result<HttpResponseModel>`), which is the "scalable" layering payoff for a single-module app.

Flow: Home `+` FAB → `requestFlow` (NewRequest). Paste's "Parse & validate" / Build's "Continue to review" both call `viewModel.validateAndBuildRequest()`, navigating to `Review` only on success (failure = inline error, no navigation). Review's send FAB → `viewModel.send()` (loading state on the FAB) → navigate to `Response` regardless of outcome (Response renders success or error state). Reaching Response writes a `HistoryEntry` via `HistoryRepository` (Room-backed) so Home populates from real usage. Back-stack is standard push/pop — no custom manipulation needed.

---

## 7. Screens

**Reusable `MethodChip`** (`ui/components/MethodChip.kt`) is the core monochrome method-differentiation piece, reused on Build tab's method selector, Review's url-card, History rows, Response's meta line:

| Method | Treatment |
|---|---|
| GET | Outline only, `Black30` border |
| POST | Solid filled, `Black90` bg / `White` text |
| PUT/PATCH | Dashed border (`PathEffect.dashPathEffect`) |
| DELETE | Filled `SignalError` bg / `White` text |

- **Home**: `Scaffold` + `TopAppBar("Home")` + search field (wired: client-side substring filter over the Room-backed history list) + `LazyColumn` of history rows (status dot: `SignalSuccess`/`SignalError` filled circle, `MethodChip`, url + relative time + status code, overflow menu with wired Delete) + empty state + `+` FAB → `requestFlow`. No Environments entry point.
- **New Request**: topbar + `SegmentedControl("Paste curl"/"Build manually")`. Paste tab: hint row, `Card` with multiline text field (`codeMono`), "Parse & validate" button, recent-chips row (sourced from `HistoryEntry.rawCurlText`, stored specifically to support this). Build tab: method-pill row (`MethodChip`, selectable), URL field, header edit-rows (key/value/delete, "+ Add header"), body segmented control (JSON wired; Form is visual-only stub for v1) + JSON field, "Continue to review" button. Build tab wrapped in `verticalScroll`.
- **Review**: topbar with "VALID" badge, `SegmentedControl(Headers/Body/Query)`, url-card (`MethodChip` + URL), per-tab content (`KeyValueRow` list / `JsonText` / query params via `QueryParamsParser`, deliberately manual string parsing rather than `android.net.Uri` so it's JVM-unit-testable), send FAB with loading state.
- **Response**: topbar with copy-to-clipboard action, `StatusCard` (pill label, big status code, duration/size/method+path meta), `JsonText` (AnnotatedString weight/tone highlighting) or raw text fallback, error-state variant on network failure.

---

## 8. Testing

Priority order: `CurlTokenizerTest`, `CurlParserTest` (the ~15 cases above — write before any UI), `JsonPrettyPrinterTest`, `QueryParamsParserTest`, `RequestFlowViewModelTest` (fake `RequestExecutor`/`HistoryRepository`, `kotlinx-coroutines-test`, asserts tab-switch state retention and no-navigation-on-parse-failure). `OkHttpRequestExecutorTest` via MockWebServer is a stretch/optional addition. Compose UI/Espresso tests are out of scope for this pass.

---

## 9. Build order

1. Gradle + manifest (Section 1) — verify `./gradlew assembleDebug` still builds.
2. Theme (Section 3) — Color/Theme/Type/Shape, JetBrains Mono bundling, `MainActivity` call-site fix.
3. Data models + parser + tests (Section 4) — get `CurlParserTest` fully green before UI work.
4. Room persistence (`HistoryEntity`/`Dao`/`Database`/`Converters`/`HistoryRepository`) + Hilt modules (`NetworkModule`, `DatabaseModule`, `RepositoryModule`) + `CurlerApplication`.
5. Network layer (Section 5).
6. Navigation skeleton (Section 6) with placeholder screen bodies — verify the full back-stack (Home→New Request→Review→Response, history-row→Review→Response, and back) before investing in real UI.
7. Reusable components (Section 7 intro) — build/preview in isolation.
8. Home screen, real UI + Room wiring.
9. New Request screen — `RequestFlowViewModel`, both tabs, manually verify tab-switch retains state.
10. Review screen.
11. Wire Send — Review FAB → executor → Response navigation, both outcomes.
12. Response screen — status card, JSON viewer, copy, error state.
13. Wire history writes on Response reached; wire history-row tap → `historyFlow`.
14. Polish: recent-chips, delete-from-history, search filter, dark-mode spot check, icon-button content descriptions.

Verify buildability after steps 2, 6, 9, 11, 13 at minimum — those are where nav/state-sharing/network integration risk concentrates.

### Critical files
- `app/src/main/java/com/hrithikvish/curler/data/curlparser/CurlParser.kt`
- `app/src/main/java/com/hrithikvish/curler/ui/navigation/CurlerNavHost.kt`
- `app/src/main/java/com/hrithikvish/curler/ui/screens/newrequest/RequestFlowViewModel.kt`
- `app/src/main/java/com/hrithikvish/curler/data/network/OkHttpRequestExecutor.kt`
- `app/src/main/java/com/hrithikvish/curler/data/history/HistoryRepository.kt`
- `app/src/main/java/com/hrithikvish/curler/ui/theme/Theme.kt`
- `gradle/libs.versions.toml`

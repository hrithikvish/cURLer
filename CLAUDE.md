# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

cURLer (`com.hrithikvish.curler`) is a single-Activity Jetpack Compose Android app: a pocket API client. Paste a raw `curl` command (or build a request manually), review the parsed request, send it, and view the response. History of past requests persists across restarts.

See `PLAN.md` for the original design doc — it documents the reasoning behind the monochrome theme, the two-nav-graph structure, and the full screen-by-screen spec. Treat it as background/rationale, not a live task list; the code has evolved slightly since (e.g. nav graphs use `navigation<CurlerRoute.X>` + `hiltViewModel(parentEntry)` rather than the string-route form originally sketched).

## Commands

Standard Gradle Android project — no custom build/lint scripts.

```bash
./gradlew assembleDebug              # build debug APK
./gradlew test                       # run all JVM unit tests (app/src/test)
./gradlew testDebugUnitTest --tests "com.hrithikvish.curler.data.curlparser.CurlParserTest"   # single test class
./gradlew testDebugUnitTest --tests "*.CurlParserTest.methodInference*"                        # single test method
./gradlew connectedAndroidTest       # instrumented tests (app/src/androidTest), needs a device/emulator
./gradlew lint                       # Android lint
```

There is no README and no separate lint/format config beyond standard AGP/Kotlin defaults.

## Architecture

### Layering

- `data/model` — plain domain models (`HttpMethod`, `HttpRequestModel`, `HttpResponseModel`, `ParsedCurlRequest`, `HistoryEntry`). No Android/framework dependencies; JVM-testable.
- `data/curlparser` — `CurlTokenizer` + `CurlParser`, a hand-rolled Kotlin port of a curl-command parser (originally prototyped in JS for a wireframe demo — see PLAN.md §4 for the full flag list and quoting/escaping rules). Pure string parsing, no Android dependency.
- `data/query` — `QueryParamsParser`: deliberately manual string parsing instead of `android.net.Uri`, specifically so it stays JVM-unit-testable outside an emulator.
- `data/json` — `JsonPrettyPrinter` for response body formatting.
- `data/network` — `RequestExecutor` interface + `OkHttpRequestExecutor` impl (OkHttp, `Dispatchers.IO`, timeouts from `di/NetworkModule`).
- `data/history` — Room persistence: `HistoryEntity`/`HistoryDao`/`CurlerDatabase`/`Converters` (headers/body stored as JSON via TypeConverter), wrapped by `HistoryRepository` which maps `HistoryEntity <-> HistoryEntry` domain model.
- `di` — Hilt modules: `NetworkModule` (OkHttpClient), `DatabaseModule` (Room), `RepositoryModule` (binds `RequestExecutor`).
- `ui/theme` — see Theme below.
- `ui/navigation` — `CurlerDestinations.kt` (type-safe `@Serializable` sealed routes) + `CurlerNavHost.kt`.
- `ui/components` — shared Compose pieces reused across screens (`MethodChip`, `SegmentedControl`, `KeyValueRow`, `HeaderEditRow`, `JsonText`, `StatusCard`, `EmptyState`).
- `ui/screens/{home,newrequest,review,response}` — one package per screen, each with its own `*ViewModel` + `*UiState`.

### Navigation & state sharing

Two nested Compose nav graphs share the `Review`/`Response` screens via a common interface, `ReviewCapable` (`request: StateFlow<HttpRequestModel>`, `suspend fun send(): Result<HttpResponseModel>`):

- **`RequestFlowGraph`** (`NewRequest` → `RequestReview` → `RequestResponse`): all three screens grab the *same* `RequestFlowViewModel` instance via `hiltViewModel(navController.getBackStackEntry<CurlerRoute.RequestFlowGraph>())`. This is what makes switching between the Paste/Build tabs on `NewRequest` non-destructive — both tabs' state lives in one VM for the life of the graph segment, and the built `HttpRequestModel`/`HttpResponseModel` ride along into Review/Response.
- **`HistoryFlowGraph(historyId)`** (`HistoryReview` → `HistoryResponse`): entered directly from a Home history-row tap, driven by `HistoryReviewViewModel`, which loads a `HistoryEntry` by id. When loading finishes it auto-navigates forward to `HistoryResponse` (see the `LaunchedEffect` guarded by `didAutoOpenSavedResponse` in `CurlerNavHost.kt`) so tapping history goes straight to the saved response.

Both `RequestFlowViewModel` and `HistoryReviewViewModel` implement `ReviewCapable`; `ReviewScreen`/`ResponseScreen` are written once and take a `ReviewCapable` (or plain state) rather than a concrete ViewModel type.

`RequestFlowViewModel.validateAndBuildRequest()` is the single entry point called by both the Paste tab's "Parse & validate" and the Build tab's "Continue to review" — it dispatches to `validateFromPaste`/`validateFromBuild` and only signals success (enabling navigation) if the resulting `HttpRequestModel` is valid. Reaching `RequestResponse`/`HistoryResponse` always writes a `HistoryEntry` (success or failure) via `HistoryRepository`, which is what backs the Home list.

### Theme — monochrome by design

`ui/theme` is deliberately grayscale: a 13-stop `Black02`…`Black100` + `White` ramp plus exactly two signal colors (`SignalError`, `SignalSuccess`). `CurlerTheme` forces `dynamicColor = false` by default — Material You dynamic color would inject the user's wallpaper hue into `primary`, which breaks this brief. Do not introduce new hues or re-enable dynamic color without discussing it; differentiate UI state (HTTP methods, statuses, etc.) via outline/filled/dashed/weight treatments instead of color, following `MethodChip.kt`'s pattern (GET = outline, POST = filled, PUT/PATCH = dashed border, DELETE = filled `SignalError`).

Two font families are used directly (not swapped in as the Material typography default): system font (Roboto) for body/headings via `CurlerTypography`, and a standalone `codeMono` `TextStyle` (JetBrains Mono, bundled under `res/font/`) applied explicitly wherever the UI shows curl/JSON/URL/code text.

### cURL parsing quirks worth knowing before touching `CurlParser.kt`

- `CurlTokenizer` is a stateful word-accumulator (shlex-style, not regex-based): quoted and unquoted fragments glue into a single token, and a quote can open mid-word. Handles single quotes (literal), double quotes (backslash escapes for `\`, `"`, `$`, `` ` ``), Bash's ANSI-C `$'...'` quoting with its own backslash escapes (`\n`, `\t`, `\r`) — needed because curl commands copied from mobile/network logs sometimes wrap `--data` in `$'...'` when the payload has literal spaces — and `\`-newline line continuations (collapsed to a space before tokenizing, in `CurlParser`).
- `-u`/`--user` is turned into a real `Authorization: Basic <base64>` header — must stay correctly base64-encoded since it's sent over the wire (not just a display placeholder).
- Unrecognized flags are silently skipped; a bare non-flag token is taken as the URL only if no URL is set yet. This is a known, accepted quirk (an unrecognized flag's value could be misread as the URL) — not something to "fix" without checking `PLAN.md` §4 first, since it mirrors intentional reference behavior.
- Once that first bare token starts the URL, the parser keeps greedily consuming subsequent bare (non-`-`-prefixed) tokens into it, space-joined, until the next flag. This is deliberate leniency for real-world pasted curl commands (e.g. copied from mobile network logs) that aren't properly shell-escaped and have literal unquoted spaces inside the URL — strict shell/curl semantics (and even `curlconverter`, the most widely used curl parser, which uses a tree-sitter Bash grammar) would otherwise truncate the URL at the first space.
- Method defaults: `POST` if a body is present, else `GET`, unless `-X`/`--request` is explicit.
- Missing URL, URL without `http(s)://` scheme, or a header missing `:` are hard parse errors (`ParsedCurlRequest.Error`); an invalid-JSON body is *not* a parse error — `bodyIsValidJson` is just set to `false` and the raw body is preserved.

## Testing conventions

JVM unit tests (`app/src/test`) mirror the `data/curlparser`, `data/json`, `data/query` packages, plus `ui/screens/newrequest/RequestFlowViewModelTest.kt`, which uses hand-written fakes (`FakeHistoryDao`, `FakeRequestExecutor`) and `kotlinx-coroutines-test` rather than a mocking library. Follow that pattern (fakes over mocks) for new ViewModel tests. `CurlParserTest`/`CurlTokenizerTest` are the highest-value tests in the repo — the parser has many edge cases (quoting, line continuations, flag aliases, last-write-wins on repeated `-d`); check them before changing parser behavior.

Compose UI/instrumented tests are minimal (`androidTest` currently just has the AGP-generated `ExampleInstrumentedTest`) — this is not a project convention to extend by default.

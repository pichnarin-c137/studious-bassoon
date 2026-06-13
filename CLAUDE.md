# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

GymApp is an Android gym-membership app for the Cambodia market (Kotlin, Jetpack Compo
MVI, Hilt). The product/architecture specs live in `docs/init/`. v1 runs fully offline on
in-memory mock data.

## Build & test

The build toolchain is bleeding-edge (AGP 9.2.1 / Gradle 9.4.1) with **built-in Kotlin**, and
on this machine the CLI must be steered to a real JDK 21 — Gradle's daemon-JVM auto-detect
otherwise picks a VSCode-bundled JRE that lacks `jlink`, which breaks AGP's `JdkImageTransform`.
Android Studio is unaffected (it bundles its own JDK). Canonical CLI invocation:

```bash
JAVA_HOME=/home/darksister/.jdk/jdk-21.0.8 ./gradlew \
  -Dorg.gradle.java.installations.auto-detect=false \
  -Dorg.gradle.java.installations.auto-download=false \
  -Dorg.gradle.java.installations.paths=/home/darksister/.jdk/jdk-21.0.8 \
  <tasks>
```
(The JDK path is local to this environment; any full JDK 21 with `jlink` works.)

- Build debug APK: `<gradlew…> :app:assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
- Unit tests: `<gradlew…> :app:testDebugUnitTest`
- Single test: `<gradlew…> :app:testDebugUnitTest --tests "com.gymapp.util.DateTimeUtilTest"`
  (append `.methodName` to run one case)

## Toolchain version lock (do not bump casually)

AGP 9.2.1 pins its built-in Kotlin compiler to **2.3.10**, which forces the rest of the matrix
in `gradle/libs.versions.toml`. Breaking this alignment fails the build:
- Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`) **must equal** the Kotlin
  version (2.3.10). KSP tracks it at **2.3.9** (closest published; the patch gap is fine).
- **Hilt ≥ 2.59** is mandatory — earlier versions throw "Android BaseExtension not found" on AGP 9.
- `gradle.properties` sets `android.disallowKotlinSourceSets=false` so KSP can register its
  generated sources under built-in Kotlin. Don't remove it.
- Do **not** add `org.jetbrains.kotlin.android` — built-in Kotlin replaces it. There is no
  `composeOptions` block (the compiler is Kotlin-versioned).
- `kotlinx-datetime` 0.6.2: `DayOfWeek.isoDayNumber` is unresolved here — use `.ordinal` (0 = Mon).

## Architecture

Single-Activity Compose app. `MainActivity` (an `AppCompatActivity`, required for the per-app
locale API) collects the persisted theme + `WindowSizeClass`, then **gates on
`SessionManager.signedIn`**: `LoginScreen` (owner-provisioned, mock `FakeGymApi.signIn`) until
signed in, else `GymAppRoot`. The shell nav is **Home · Progress · [+ Log] · Activity · Profile** —
a custom bottom bar with a raised lime center FAB (not Material `NavigationBar`); side rail on
Expanded. Check-in & Membership are not tabs but detail routes (Home membership block → membership →
check-in QR); the center [+ Log] is the detail-route **quick-log fast-path** (a real MVI screen).
**Activity** is the social feed (owner announcements + friends' workouts + kudos). `ComingSoon` is no
longer used by any screen (kept as a generic component).

**MVI, one pipeline per screen** (`ui/screens/<feature>/`): a `Screen` composable reads
`StateFlow<UiState<T>>` from a `@HiltViewModel`, emits `*Intent`s via `onIntent`, and the
ViewModel calls a `UseCase` → `Repository` → `GymApi`. `UiState<T>` is `Loading | Success | Error`
(`domain/state/`); per-screen payloads are in `domain/state/ScreenData.kt`; use cases fan out
with `async` for parallel fetches.

**Mock-data swap point**: `data/api/GymApi` is the real Retrofit contract, but `di/ApiModule`
binds it to `FakeGymApi` (in-memory `MockData` + simulated delay). Going live = replace that one
`@Binds` with a Retrofit-provided implementation; nothing above the API layer changes.

**Cross-cutting utilities** (`util/`):
- `DateTimeUtil` — all timestamps are stored as UTC epoch millis and rendered in
  `Asia/Phnom_Penh` (`dd MMM yyyy` / `h:mm a`). Use it; never format dates inline.
- `ThemeManager` / `LanguageManager` wrap a single DataStore (`di/DataStoreModule`). Theme flows
  into `ui/theme/GymTheme`; language is applied via `LocaleHelper` (`AppCompatDelegate`) and
  persisted+restored by the `AppLocalesMetadataHolderService` declared in the manifest.
- `WindowSize` (COMPACT/MEDIUM/EXPANDED) is provided through `LocalWindowSize`; screens adapt
  padding/columns off it. `ScreenContainer` applies the breakpoint padding and tablet max-width.

## Design language (telemetry / data-led)

Dark near-black (`#101012`) canvas + a single electric-lime (`#D6FB3D`) accent used **only** for the
next action (CTA), live/active status, the active tab, and positive progress — never decorative or
as pill fills. Flat: no gradients/shadows.
- **Accent ink vs fill**: anything thin or textual (chart lines/ticks, status dots, accent text &
  icons, the active tab) must use `MaterialTheme.colorScheme.accentInk` (`ui/theme/Theme.kt`) —
  lime on dark, olive `LimeInk` on light, because pure lime is ~1.1:1 against light surfaces.
  `colorScheme.primary` (pure lime) is only for *fills* with dark content on top (CTA button, FAB).
- **De-carded.** Screens use `Hairline` (1px rule) between sections, `OverlineLabel` (tiny tracked
  caps) captions, oversized monospaced numbers (`MonoNumbers` → `StatNumber`/`MetricBlock`), and
  `StatusDot` for status. Primitives live in `ui/components/Telemetry.kt`. `GymCard` still exists but
  is unused on the main screens; `MemberCard`/`StatCard`/`StatusChip` were deleted — don't
  reintroduce them.
- Stat rows: in a weighted `Row`, set `MetricBlock(horizontalAlignment = Start/CenterHorizontally/
  End)` so outer metrics hug the screen edges (plain left-aligned thirds read as misaligned).
- Charts are hand-drawn (`Canvas`/layout, no chart lib): `WeekBarChart`, `TrendLineChart`,
  `BusynessStrip` in `ui/components/Charts.kt`.
- Two font weights only (400/500); the only ALL-CAPS is `OverlineLabel`.
- Tracked-caps labels (`OverlineLabel`, `StatusDot`) drop their letter-spacing in the km locale —
  tracking breaks Khmer subscript-consonant shaping (see `isKhmerUi()` in `CommonComponents.kt`).

## Conventions

- App package is `com.gymapp` (note: Gradle `rootProject.name` is still `membership_app_v1`).
- **No hardcoded UI strings** — everything goes through `res/values/strings.xml` with a Khmer
  translation in `res/values-km/`. The app font is Noto Sans Khmer (covers Khmer + Latin).
- **Icons**: nav icons are vector drawables (`res/drawable/ic_nav_*.xml`) to keep the APK small
  (target < 20 MB); for in-screen accents use only the core `Icons.Filled`/`Icons.AutoMirrored`
  set — `material-icons-extended` is intentionally not a dependency. Glyphs absent from the core set
  (flame/barbell/trophy/eye/qr/people) are `res/drawable/ic_*.xml` vectors tinted via `Icon(tint=)`.
- Money is rendered Latin-digit USD (`money()` in `ui/screens/membership/Labels.kt`); enum→label
  mappings (plan/payment types) also live there.

## Stubbed for later passes

Login is mock (`FakeGymApi.signIn` accepts any non-blank ID + password).

The **Log** tab is the implemented quick-log fast-path (`ui/screens/log/`): pick a session-type chip
(Gym · Cardio · Bodyweight) + duration → `logSession` moves the **weekly** streak forward (stateful
`FakeGymApi`, returns `StreakState`). It deliberately captures **no** set/volume data — the richer
set-level logger (using `WorkoutLogEntry`) is a later progressive-disclosure pass.

The **streak is weekly and kind** (`StreakState` — not the old daily `VisitStats.currentStreak`, which
was removed): a member sets a `weeklyTarget` (3–6, default `MockData.WEEKLY_TARGET_DEFAULT` = 5, shared
with `weeklyActivity.sessionsTarget` so Home's week bars and the goal agree), and `weekStreak` counts
consecutive weeks the target was hit — **a rest day never breaks it**. Earned `freezesAvailable`
protect a missed week (the missed-week *consume* is a backend stub; the in-memory app has no week
rollover). `getStreakState`/`setWeeklyTarget` live on `CheckInRepository`/`ProgressRepository`; the
inline goal setter is on Progress, and the lime Home "This week" nudge ("N to your weekly goal" /
"Weekly goal met") is the rest-day reason-to-open. In the mock, `FakeGymApi` derives `sessionsThisWeek`
from the same Mon→Sun pattern as `weeklyActivity`, bumps `weekStreak` once when a log crosses the
target, and banks a freeze every 4th secured week (capped).

The **Progress** tab (`ui/screens/progress/`) is a **daily consistency dashboard**, not volume
analytics — it's built on the honest facts the quick-log captures (showing up + duration + type) plus
the kind streak, so it moves with real activity instead of fabricated volume. Layout (de-carded):
week-streak hero (weekly goal line + freeze badge + inline target setter) + per-day `ConsistencyStrip`
(lime tick per trained day, height ∝ duration; `ui/components/Charts.kt`) over the 7/30/90 window →
time-invested metrics (Sessions · Hours · Avg) → neutral `TypeMixBar` (Gym/Cardio/Bodyweight; off the
lime budget) → honest last-session row (type · duration · when, no `0 kg / 0 sets`) → a **Strength
stub** that lights up when set-level logging lands. The per-day strip reads seeded `MockData.trainingDays`
via `getTrainingDays` (**not** wired to `logSession`; that wire-in, plus real volume/PRs, is deferred —
seeded `volumeTrend`/`personalRecords` + repo methods are kept for it), but the week-streak hero reads
`getStreakState`, which `logSession` does move within a run. Home owns the week-streak metric, the
weekly-goal nudge & current-week `WeekBarChart`; Progress is the longitudinal view and doesn't repeat it.

The **Activity** tab (`ui/screens/activity/`) is slice 1 of the social feed: owner `Announcement`s
pinned above a friends' `ActivityFeedItem` feed with one-tap optimistic kudos. **No real-time
presence** — async, friends-scoped, post-hoc; `FeedActor` is slim (no phone/memberCode), the feed
exposes only type + duration + optional PR (no exact times/location). The model is built for the
unbuilt slices: **slice 2** = `logSession` also prepends an `isYou=true` feed item (Log → feed);
**slice 3** = set-level detail fills `FeedPr` → 🏆 milestone rows + self-initiated share cards.

Camera QR *scanning* (Check-in only generates the member's QR), real freeze/referral actions,
live KHQR payments, set-level workout/metric CRUD, and expiry push notifications are placeholders.

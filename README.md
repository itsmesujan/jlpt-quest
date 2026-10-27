# JLPT Quest — Android App

A gamified Japanese learning app for JLPT learners worldwide: a quest-map path through
N5→N1 (launch content: 3 N5 chapters), FSRS spaced repetition, timed boss/mock exams with
sectional diagnosis, and one Premium subscription. Native Kotlin + Jetpack Compose,
offline-first, built for a solo developer with AI help.

**Working title:** "JLPT Quest" — rename freely before launch (it appears in
`app_name`, the store listing draft, and class names only as the package; package name
changes are a find-replace).

## Architecture

```
app/src/main/java/com/itsmesujan/jlpt/
├── MainActivity.kt               ComponentActivity, edge-to-edge, Compose entry
├── JlptQuestApplication.kt       Manual DI (ProgressRepository); no framework
├── ui/
│   ├── AppNav.kt                 Bottom nav (Path · Practice · Profile) + all routes
│   ├── AppViewModel.kt           Single VM: profile/progress flows, TTS, billing, unlock rules
│   ├── theme/Theme.kt            Material 3 (dynamic colour, brand fallback, true dark)
│   ├── components/Common.kt      Kiko mascot, stars, stats strip
│   └── screens/
│       ├── OnboardingScreen.kt   Goal → daily goal → 12-item placement → result → soft paywall hook
│       ├── PathScreen.kt         Quest map, single "Today" CTA, guidebook notes, Five Worlds panel
│       ├── LessonScreen.kt       Session engine: MC / typed / listening (TTS) / reading, instant
│       │                         WHY feedback, missed items re-queued once, results (+XP, stars)
│       ├── ReviewScreen.kt       Practice hub: FSRS review session (4-button), weak spots, boss list
│       ├── MockScreen.kt         Timed boss/mock, no mid-test feedback, /180 results + sectional trap
│       ├── ProfileScreen.kt      Progress, mastery, achievements, subscription mgmt, licences, deletion
│       └── PaywallScreen.kt      Premium offers from Play, full disclosure text, restore, dismiss
├── content/Content.kt            Bundled original N5 content: 3 chapters × 6 nodes, placement set
├── domain/
│   ├── Models.kt                 Chapter / LessonNode / Exercise, JLPT levels
│   ├── FsrsScheduler.kt          FSRS-style D/S/R scheduler + rating inference from performance
│   └── MockScoring.kt            /180 approximation + official pass marks & sectional minimums
├── data/
│   ├── ProgressRepository.kt     Offline-first writes: XP/streak/stars, cards, mistakes, entitlement
│   └── local/                    Room DB: PlayerProfile, NodeProgress, ReviewCard, Mistake (+ DAOs)
├── billing/Billing.kt            Play Billing 9.x: connect/query/buy/acknowledge/restore,
│                                 ProductIds, entitlement + feature flags, PaywallMoment enum
└── tts/JapaneseTts.kt            On-device ja-JP TextToSpeech wrapper (fallback audio)
app/src/main/assets/              Reserved for content packs (audio/JSON) as content moves out of code
store-listing/                    Icon 512, feature graphic, checklist, listing draft, screenshot plan
.github/workflows/android.yml     Cloud build: unit tests + debug APK + release AAB
```

Key design rules (from the research blueprint — do not regress):
- **No hearts/energy.** Wrong answers never block learning; missed items re-queue and become FSRS weak spots.
- **Typed retrieval beats tapping** where possible; lesson ratings are *inferred* from correctness/latency, explicit 4-button ratings exist only in flashcard review.
- **Offline-first:** lessons, reviews and mocks run entirely from Room + bundled content.
- **Paywalls only** post-placement, post-mock, at free limits, Day-3/7 recaps, Settings — never mid-lesson, never after a failed exercise, no fake timers.
- Content is **original**. Never reproduce real JLPT questions or textbook/competitor material. (Licences for JMdict/KANJIDIC/KanjiVG/Tatoeba expansion are credited in Profile → Licences.)

## Open in Android Studio

1. Install Android Studio (latest stable) with SDK Platform 36 and Build-Tools.
2. Open this `android/` folder. Let Gradle sync (AGP 8.13, Kotlin 2.2.20, Gradle 8.14.3).
   - The wrapper jar isn't committed yet; Android Studio works without it. To enable
     `./gradlew` and match CI, run `gradle wrapper --gradle-version 8.14.3` once on any
     machine with Gradle installed and commit `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`.
3. Run the `app` configuration on an emulator or device (minSdk 26 / Android 8+).
4. Run unit tests: `gradle testDebugUnitTest` (FSRS + mock-scoring tests in `app/src/test`).

## applicationId — SET (do not change after first upload)

The applicationId and namespace are **`com.itsmesujan.jlpt`** (provided by the owner on
2026-10-27; package folders are `java/com/itsmesujan/jlpt/` and every Kotlin file's
`package` line matches). Play treats the applicationId as permanent once the first
bundle is uploaded, so any future change would have to happen *before* that upload,
as a coordinated find-replace of the ID, the namespace, the package declarations and
the folder paths. The debug build uses suffix `.debug` so debug and release can coexist
on one phone.

## Signing

```bash
keytool -genkeypair -v -keystore release-key.jks -alias jlptquest \
  -keyalg RSA -keysize 2048 -validity 10000
cp keystore.properties.example keystore.properties   # fill in values, NEVER commit it
```
Back the `.jks` up offline in **two** places — losing it complicates upload-key recovery.
CI alternative: set secrets `KEYSTORE_BASE64` (base64 of the .jks), `KEYSTORE_PASSWORD`,
`KEY_ALIAS`, `KEY_PASSWORD`; the workflow decodes and signs with them. Locally, env vars
`JLPT_KEYSTORE_FILE / JLPT_KEYSTORE_PASSWORD / JLPT_KEY_ALIAS / JLPT_KEY_PASSWORD` work too.
Enrol in **Play App Signing** at first upload (Google holds the final signing key; yours is the upload key).

## Play Console products (create with EXACTLY these IDs)

| Product ID | Type | Price (US) | Notes |
|---|---|---|---|
| `premium_monthly` | Subscription | $7.99/mo | Base plan, no trial |
| `premium_yearly` | Subscription | $47.99/yr | Add a **7-day free trial** offer in Console (trials live in Console, not code) |
| `premium_lifetime` | One-time (non-consumable) | $179 | |

Also in Console: accept country-pricing templates, then review JP/KR/ID/VN/IN/BR/MX manually;
add licence testers so you can test purchases for free; verify purchase → acknowledge (≤3 days,
the app does this) → restore on a second device. **Server verification is a TODO in code**
(`Billing.kt`): before production revenue, verify purchase tokens with the Play Developer API
(or RevenueCat) on a backend — client-only granting is for closed testing only.

## Publishing checklist

1. Build signed AAB (`gradle bundleRelease` or download from GitHub Actions artifacts).
2. Create the app in Play Console with the final applicationId; upload to a **closed track**.
3. Recruit **18–20 testers**; get **≥12 opted-in continuously for 14 days** (personal accounts
   created after 13 Nov 2023 must pass this gate before production access). Give testers real
   tasks; keep feedback — the production application asks about it.
4. Complete: store listing (see `store-listing/`), privacy policy URL on your domain,
   Data Safety form (v0.1 truthfully: no off-device data except Play Billing), content rating
   (IARC), target audience 13+/general, ads declaration = none, account-deletion fields
   (not applicable until accounts exist — data is local-only in v0.1).
5. Apply for production, respond to review, then staged rollout (10% → 50% → 100%) while
   watching Android Vitals and reviews.

## What's complete vs placeholder

**Complete & playable in code**
- Full lesson loop for Chapter 1 end-to-end (and Chapters 2–3 with the same six node types):
  Word Battle, Kanji Clash, Grammar Dojo, Echo Quest (TTS + text fallback), Story Trail, Chapter Boss.
- FSRS scheduling end-to-end: lesson results create/update Room review cards; Practice runs due reviews + weak spots.
- XP/levels/stars/streak (+ freeze logic), sequential unlocks, Five Worlds roadmap panel.
- Timed mocks with /180 estimate, sectional minimum trap, weakest-skill callout.
- Play Billing 9 integration: connect, query, buy, acknowledge, restore; entitlement gates premium nodes.
- Adaptive icon set (fg/bg/monochrome vectors) + 512 PNG + feature graphic; Kiko mascot placeholder vector.

**Placeholder / TODO (marked in code)**
- Server-side purchase verification; Firebase auth/sync/analytics/crash (event taxonomy listed in `JlptQuestApplication`).
- Placement *credit* (placement currently estimates and recommends; it does not auto-complete nodes).
- Per-exercise "Report an issue" backend queue; guidebook full formation tables (short notes ship).
- Pre-generated neural audio packs replacing device TTS; KanjiVG stroke tracing; shadowing/speaking; reader feed; leagues/friends; push notifications + widget (v1.5 per blueprint).
- N4–N1 content waves; Room migrations beyond v1 (destructive fallback is v0.x-only); legacy mipmap PNGs via Image Asset Studio.
- Gradle wrapper jar (see above); the project has **not been compiled on this machine** (no Android SDK here by design) — first compile happens in Android Studio / GitHub Actions; static checks performed are listed in the delivery report.

# Fintech Mobile Patterns (Android)

A production-oriented Android sample demonstrating fintech-specific mobile patterns — multi-currency payments, biometric-gated confirmation, offline-first transaction handling, typed error recovery, and retry with exponential backoff — built with Clean Architecture, MVI, Jetpack Compose, and Hilt. Companion to [clean-architecture-android](https://github.com/nerojust/clean-architecture-android), which demonstrates the same architectural shape on a simpler domain.

![CI](https://github.com/nerojust/fintech-mobile-patterns-android/actions/workflows/ci.yml/badge.svg)

## Sample app: "Vela"

A fictional multi-currency wallet. No employer code, branding, or real API shape appears anywhere — the backend is a fake, in-process implementation (`FakeVelaApiService`) with injectable simulated latency and failure rate, standing behind the same `VelaApiService` interface a real Retrofit-backed client would implement.

| Feature | Demonstrates |
|---|---|
| Send money (multi-currency, live conversion) | Multi-currency payment UX |
| Biometric confirmation before submit | Biometric-gated payment flow |
| Card management (masked PAN — last4/brand/expiry only) | No-raw-PAN-on-client pattern |
| Transaction history (Pending / Synced / Failed) | Offline-first state visibility |
| Failed payment recovery with typed failure reasons | Error recovery UX |
| Background resubmission with exponential backoff | Retry logic |

## Screenshots

A bottom navigation bar (Send / Cards / History) switches between the three feature screens.

| Send money | Cards (masked PAN) | Transaction history |
|---|---|---|
| ![Send money screen](docs/screenshots/send_money.png) | ![Cards screen](docs/screenshots/cards.png) | ![Transaction history screen, empty state](docs/screenshots/transaction_history.png) |

## Module graph

```mermaid
graph TD
    app --> feature_payment[feature:payment]
    app --> feature_cards[feature:cards]
    app --> feature_transactions[feature:transactions]
    app --> data
    app --> core_ui[core:ui]
    feature_payment --> domain
    feature_cards --> domain
    feature_transactions --> domain
    feature_payment --> core_security[core:security]
    data --> domain
    data --> core_network[core:network]
    data --> core_database[core:database]
    data --> core_common[core:common]
```

## Offline-first & retry flow

```mermaid
sequenceDiagram
    participant UI as SendMoneyScreen
    participant VM as SendMoneyViewModel
    participant Repo as TransactionRepositoryImpl
    participant DB as Room outbox
    participant API as FakeVelaApiService
    participant Worker as PaymentSyncWorker

    UI->>VM: SubmitClicked (after biometric)
    VM->>Repo: submitPayment(amount, currency, cardId)
    Repo->>DB: insert(status=PENDING)
    Repo->>API: submitTransaction() via RetryPolicy
    alt success
        Repo->>DB: update(status=SYNCED)
    else insufficient funds (non-retryable)
        Repo->>DB: update(status=FAILED_PERMANENT, reason=INSUFFICIENT_FUNDS)
    else transient network failure
        Repo->>DB: update(attempts+1, still PENDING)
        Repo->>Worker: scheduleSync() (exponential backoff)
    end
    Repo-->>VM: Transaction(post-attempt status) — not the optimistic pre-attempt row
    VM-->>UI: Success (Pending/Synced) or Failed (FailedPermanent)
    Note over Worker: Background sweep also triggered on reconnect (ConnectivityObserver)
    Note over Worker: After 5 transient failures a row becomes FAILED_PERMANENT/NETWORK_UNAVAILABLE
    Note over Worker: The next sync revives it to PENDING and retries (insufficient-funds rows are never revived)
```

## Why this structure differs from clean-architecture-android

- `:domain` stays pure-Kotlin JVM, zero Android dependencies — same rule as the companion repo.
- `:data` **is** an Android library module here (the companion repo's `:data` is pure JVM). This repo's `:data` wires Room (`:core:database`) for the offline outbox and a `WorkManager` `CoroutineWorker` for background sync — both are Android APIs. The domain/data boundary that matters (business logic has zero Android dependency) is still enforced at `:domain`.
- Hilt `@Module`/`@Binds`/`@Provides` wiring lives only in `:app`, exactly as in the companion repo — every other module uses plain `@Inject`/`@AssistedInject` constructors.
- A payment is written to the Room outbox as `PENDING` *before* any network call — this is what makes it offline-first: the write survives process death, so a payment the user believes was submitted cannot silently vanish if the app is killed.

## Setup

1. Clone the repo and open it in Android Studio (Ladybug or newer).
2. Ensure Android SDK platform 35 and build-tools 35.0.0 are installed.
3. Run `./gradlew :app:installDebug` with a device/emulator connected, or use the `app` run configuration in Android Studio.
4. The device/emulator needs at least one enrolled biometric (fingerprint or face) to complete the send-money flow — Android's biometric APIs require this.

## Testing

- All unit tests: `./gradlew test`
- Per module: `./gradlew :domain:test`, `./gradlew :data:test`, `./gradlew :feature:payment:testDebugUnitTest`, etc.
- Room DAO test (requires a connected device/emulator): `./gradlew :core:database:connectedAndroidTest`
- `EncryptedTokenStore` round-trip test (requires a connected device/emulator): `./gradlew :core:security:connectedAndroidTest`
- Compose UI tests (requires a connected device/emulator): `./gradlew :feature:payment:connectedDebugAndroidTest`
- Lint: `./gradlew ktlintCheck detekt`

## Known simplifications (extension points)

- No real payment processor/backend — `FakeVelaApiService` is in-process only. Swapping in a real Retrofit client means implementing `VelaApiService` against a real host and rebinding it in `di/NetworkModule.kt` — no change needed anywhere else.
- `CardRepository.removeCard` is a no-op — the fake backend has no delete endpoint yet.
- The cards list screen is currently display-only: `CardsViewModel`, `CardsIntent.AddCardClicked`/`RemoveCard`, `AddCardUseCase`, and `RemoveCardUseCase` are fully wired and tested (adding a card genuinely persists through the fake backend), but `CardsContent` doesn't yet expose any UI (a FAB, a swipe action) to trigger either intent.
- `core:security`'s `EncryptedTokenStore` (Keystore-backed via `EncryptedSharedPreferences`) is available for encrypted auth-token storage and verified by an instrumented save/get/clear round-trip test, but it is not yet wired into a login/session screen — this demo app has no auth flow to attach it to.
- No root/jailbreak detection, remote config, or crash reporting — out of proportion for a pattern-demonstration repo.
- No screenshot testing (Paparazzi/Roborazzi) — deferred, not needed to demonstrate the core testing pyramid.

## License

MIT — see [LICENSE](LICENSE).

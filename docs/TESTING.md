# Testing

Three test source sets ship with the skeleton:

| Source set           | Runs on                   | Use for                                     |
|----------------------|---------------------------|---------------------------------------------|
| `shared/commonTest`  | JVM, Android, iOS         | Domain + use case + repository unit tests   |
| `shared/androidTest` | Android device/emulator   | Android-specific integration                |
| `android/androidTest`| Android device/emulator   | Compose UI + Activity tests                 |

Write tests in `commonTest` whenever possible — they run on all targets and catch KMP-specific regressions for free.

## Layout

```
shared/src/commonTest/kotlin/id/startapp/
├── application/
│   └── usecases/auth/       …UseCaseTest.kt
├── core/
│   └── validator/           …ValidatorTest.kt
└── infrastructure/
    └── repository/          …RepositoryTest.kt
```

Test classes live alongside the package they test: `LoginUseCaseTest.kt` sits under `commonTest/.../application/usecases/auth/`.

## Run

```bash
./gradlew :shared:allTests                   # every target
./gradlew :shared:jvmTest                    # commonTest on JVM (fastest)
./gradlew :shared:testDebugUnitTest          # commonTest compiled for Android
./gradlew :shared:iosSimulatorArm64Test      # commonTest + iosTest on sim
./gradlew :android:testDebugUnitTest         # Android-only unit tests
./gradlew :android:connectedDebugAndroidTest # instrumented tests (device needed)
```

CI should run at minimum `./gradlew :shared:allTests :android:testDebugUnitTest`.

## Unit test recipe

Use `kotlin.test` for assertions (it's already a dep). No Mockito in KMP — hand-roll fakes or use a lightweight library like MockK on androidTest.

```kotlin
// shared/src/commonTest/kotlin/.../application/usecases/auth/LoginUseCaseTest.kt
class LoginUseCaseTest {

    @Test
    fun `login stores token on success`() = runTest {
        val tokenStorage = FakeTokenStorage()
        val api = FakeAuthApi(loginResult = Result.Success(AuthToken.valid()))
        val repo = AuthRepositoryImpl(authApiClient = api, tokenStorage = tokenStorage)

        val result = LoginUseCase(repo).invoke("a@b", "pw")

        assertTrue(result is Result.Success)
        assertEquals(AuthToken.valid(), tokenStorage.stored)
    }

    @Test
    fun `login rate-limits after threshold`() = runTest {
        val limiter = RateLimiter.forLogin()
        val repo = AuthRepositoryImpl(authApiClient = AlwaysFailing, tokenStorage = FakeTokenStorage())
        val useCase = LoginUseCase(repo, rateLimiter = limiter)

        repeat(5) { useCase.invoke("a@b", "bad") }
        val sixth = useCase.invoke("a@b", "bad")

        assertTrue(sixth is Result.Error)
        assertIs<NetworkError.RateLimited>((sixth as Result.Error).error)
    }
}
```

Conventions:

- **Backticked names** — `fun `does X when Y``. Reads like documentation.
- **Fakes over mocks** — implement the interface yourself. Fakes compose well and avoid framework ceremony. Put them under `commonTest/.../fakes/` and share.
- **`runTest`** for coroutines — use `kotlinx-coroutines-test`'s virtual scheduler, not `runBlocking`.
- **One behavior per test** — if you need two assertions for the same behavior, that's fine; but one test that verifies three unrelated things is three tests.

## Repository/integration tests

Use the in-memory SQLDelight driver in `commonTest`:

```kotlin
val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
    AppDatabase.Schema.create(it)
}
val db = AppDatabase(driver)
```

For the API client, either fake the `HttpClient` engine via `MockEngine` or fake at the `AuthApi`/`CatalogApi` interface level. Prefer the latter — testing Ktor serialization rarely pays off.

## Compose UI tests

Put UI tests under `android/src/androidTest/`. Use `createAndroidComposeRule()`:

```kotlin
class ProductListScreenTest {

    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun showsProducts() {
        composeRule.setContent {
            AppTheme {
                ProductListScreen(products = listOf(Product(id = "1", name = "Hat", priceCents = 100, categoryId = "c1")))
            }
        }
        composeRule.onNodeWithText("Hat").assertIsDisplayed()
    }
}
```

`TestTags` from `presentation/ui/accessibility/TestTags.kt` keeps selectors stable — prefer `onNodeWithTag(TestTags.PRODUCT_LIST)` over `onNodeWithText`.

## iOS tests

`commonTest` code compiles for iOS automatically. Run with:

```bash
./gradlew :shared:iosSimulatorArm64Test
```

Platform-specific iOS tests go in `shared/src/iosTest/kotlin/…` — there's no such directory in the skeleton yet, but add it when you need iOS-only assertions (e.g., Keychain behavior).

## Coverage

Kover (or Jacoco for Android) isn't wired yet. When you add it, target `shared/commonTest` + Android unit tests; skip Compose UI tests for coverage since they're flaky under headless CI.

## Continuous integration

A reasonable CI job:

```yaml
- uses: actions/setup-java@v4
  with: { distribution: temurin, java-version: 17 }
- run: ./gradlew --no-daemon detekt :shared:allTests :android:testDebugUnitTest :android:assembleDebug
```

Cache `~/.gradle/caches` and `~/.konan` (Kotlin/Native deps). First build is ~15 min uncached; cached builds run in 2–3 min.

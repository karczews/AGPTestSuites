# AGP Test Suites

Sample Android project used to exercise Compose Preview Screenshot Testing
with [AGP test suites](https://developer.android.com/studio/preview/compose-screenshot-testing-with-testsuites)
(experimental).

## Requirements

- JDK 17 or higher
- Android Gradle Plugin `9.5.0-alpha03` or higher (see `gradle/libs.versions.toml`)
- Screenshot engine `0.0.1-alpha16` or higher (see `screenshotEngine` in `gradle/libs.versions.toml`)

The integration is gated behind two experimental flags in `gradle.properties`:

```properties
android.experimental.enableScreenshotTest=true
android.experimental.testSuiteSupport=true
```

The suite itself is declared in `app/build.gradle.kts`:

```kotlin
testOptions {
    screenshotTests.create("screenshotTest") {
        engineVersion = libs.versions.screenshotEngine.get()
        targetVariants.add("debug")
        dependencies {
            implementation(libs.androidx.compose.ui.tooling)
            implementation(libs.screenshot.validation.api)
        }
    }
}
```

## Adding a screenshot test

Tests live in `app/src/screenshotTest/kotlin/`. Annotate a `@Composable` with
`@PreviewTest` plus a regular `@Preview`:

```kotlin
@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SpeedGaugeCruisingPreview() {
    SpeedGaugeTheme {
        SpeedGauge(speedKmh = 90f)
    }
}
```

Existing examples: `app/src/screenshotTest/kotlin/com/github/karczews/speedgauge/SpeedGaugeScreenshotTest.kt`.

## Running screenshot tests

Task names follow the pattern `{verb}{SuiteName}{Target}{Variant}TestSuite`.
With the `screenshotTest` suite targeting `debug`, the tasks are:

```sh
# Re-render previews and update the golden reference images:
./gradlew updateDebugScreenshotTestDefaultTestSuite

# Render fresh screenshots and compare them against the references:
./gradlew testDebugScreenshotTestDefaultTestSuite
```

Reference (golden) images are stored under
`app/src/screenshotTestDefaultDebug/reference/` — commit them together with
the test that produces them. On failure, open the HTML report at
`app/build/reports/tests/testDebugScreenshotTestDefaultTestSuite/index.html`,
which shows the diff, the expected image, and the actual rendering.

## Notes

- The `Restricted methods will be blocked ...` and `Unsafe` warnings during
  test runs come from LayoutLib loading its native renderer on a newer JDK.
  They are harmless and will be addressed upstream.
- `:app:tasks --all` currently fails on this AGP alpha while eagerly
  realizing the release variant's preview task (`enableUnitTest = false`).
  Targeted tasks (`assemble*`, `update*`, `test*`) are unaffected, so no
  workaround is kept in the build files.

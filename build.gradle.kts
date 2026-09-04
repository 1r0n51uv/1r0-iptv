// NOTE: this sandbox has no route to Google's Maven repo (dl.google.com),
// so ./gradlew here can configure/test ":domain" but NOT ":app" (needs the
// Android Gradle Plugin + androidx artifacts from google()). Build/run
// ":app" from a normal machine with the Android SDK (Android Studio, or
// ANDROID_HOME + local.properties set).
//
// Deliberately NOT declaring com.android.application/kotlin.android here
// (even with apply false): a root-level `plugins {}` entry is resolved
// whenever the root project configures, regardless of which task runs.
// ":app" declares its own Android/Compose plugins directly instead, so
// `./gradlew :domain:test --configure-on-demand` never touches them.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

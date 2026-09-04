// A root-level `plugins {}` entry is resolved whenever the root project
// configures, regardless of which task runs, so this needs a route to
// Google's Maven repo (dl.google.com) for AGP even to run `:domain:test`.
// All three Android/Kotlin-Android plugins :app needs are declared
// together here (apply false) rather than only in :app - Gradle puts
// kotlin.android's dynamic AGP hook in a different classloader scope from
// AGP itself if they're not resolved together, which fails :app's
// configuration with "NoClassDefFoundError: com/android/build/gradle/api/
// BaseVariant".
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

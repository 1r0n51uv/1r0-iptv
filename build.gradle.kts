// Android/Compose/Hilt plugin declarations move here once the ":app" module
// is scaffolded. Kept out for now: this sandbox has no route to Google's
// Maven repo (dl.google.com), so declaring com.android.application here
// (even with apply false) would break plugin resolution for every module,
// including the pure-Kotlin ":domain" module this environment can build.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

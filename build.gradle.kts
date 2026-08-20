import java.io.FileInputStream
import java.util.Properties

// Top-level build file: applies plugins with `apply false` so every
// submodule resolves the same plugin versions from the version catalog.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}

// ---------------------------------------------------------------------
// Shared versioning + signing, computed ONCE here instead of duplicated
// across every one of the ~19 standalone app modules in this ecosystem.
// Each app module's build.gradle.kts reads these via `rootProject.extra`.
// ---------------------------------------------------------------------

// versionCode must strictly increase for Android to accept a build as a
// valid in-place update. Deriving it from the commit count means it only
// ever goes up, automatically, for every app in the ecosystem at once.
val ecosystemVersionCode: Int = try {
    providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
    }.standardOutput.asText.get().trim().toInt()
} catch (e: Exception) {
    1
}
extra["ecosystemVersionCode"] = ecosystemVersionCode
extra["ecosystemVersionName"] = "1.0.$ecosystemVersionCode"

// Signing: read local credentials from keystore.properties (gitignored,
// see keystore/README.md). CI reconstructs the identical file from GitHub
// Secrets before this script runs, so every app — local or CI-built —
// carries the same signing certificate as every other build of itself.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}
extra["hasKeystore"] = keystorePropertiesFile.exists()
extra["keystoreStoreFile"] = keystoreProperties.getProperty("storeFile", "")
extra["keystoreStorePassword"] = keystoreProperties.getProperty("storePassword", "")
extra["keystoreKeyAlias"] = keystoreProperties.getProperty("keyAlias", "")
extra["keystoreKeyPassword"] = keystoreProperties.getProperty("keyPassword", "")

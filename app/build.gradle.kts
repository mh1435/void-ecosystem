import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// ---------------------------------------------------------------------
// Signing: read local credentials from keystore.properties (gitignored,
// see keystore/README.md). CI reconstructs the identical file from
// GitHub Secrets before this build script runs, so a locally-built APK
// and a CI-built APK carry the exact same signing certificate. That
// identity — not the code inside the APK — is what Android checks
// before accepting an install as an "update" instead of demanding an
// uninstall first.
// ---------------------------------------------------------------------
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

// versionCode must strictly increase for the OS to accept a build as a
// valid in-place update — a duplicate or lower versionCode is rejected
// even when the signature matches. Deriving it from the commit count
// means you never have to remember to bump it by hand, and it can never
// go backwards as long as history only grows.
// Uses providers.exec (not ProcessBuilder) so this external process is a
// tracked configuration-cache input instead of an opaque side effect —
// running a process directly at configuration time is disallowed once
// org.gradle.configuration-cache=true (see gradle.properties).
fun gitCommitCount(): Int = try {
    providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
    }.standardOutput.asText.get().trim().toInt()
} catch (e: Exception) {
    1
}

val ecosystemVersionCode = gitCommitCount()
val ecosystemVersionName = "1.0.$ecosystemVersionCode"

android {
    namespace = "com.voidecosystem.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        // Freeze this forever. Changing applicationId makes Android treat
        // a rebuild as a *different app* — a guaranteed "must uninstall
        // first", independent of signing or versioning.
        applicationId = "com.voidecosystem.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = ecosystemVersionCode
        versionName = ecosystemVersionName

        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Only apply the real signing config when keystore.properties
            // exists (local dev machine or CI with secrets decoded). A
            // clean checkout without it still builds — just unsigned —
            // instead of failing the whole project.
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))

    // Gateway
    implementation(project(":feature:dashboard"))

    // System & Developer Tools
    implementation(project(":feature:theming"))
    implementation(project(":feature:terminal"))
    implementation(project(":feature:sysmonitor"))

    // AI & Automation
    implementation(project(":feature:omniassistant"))
    implementation(project(":feature:automation"))
    implementation(project(":feature:routines"))

    // Media & Entertainment
    implementation(project(":feature:musicplayer"))
    implementation(project(":feature:gallery"))

    // Productivity & Life Tracking
    implementation(project(":feature:focushub"))
    implementation(project(":feature:todo"))
    implementation(project(":feature:journal"))
    implementation(project(":feature:finance"))
    implementation(project(":feature:pantry"))

    // Core OS Utilities
    implementation(project(":feature:calculator"))
    implementation(project(":feature:notes"))
    implementation(project(":feature:calendar"))
    implementation(project(":feature:filemanager"))

    // Communication & Connectivity
    implementation(project(":feature:dialer"))
    implementation(project(":feature:browser"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

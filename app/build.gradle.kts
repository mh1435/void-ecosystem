plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

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
        versionCode = rootProject.extra["ecosystemVersionCode"] as Int
        versionName = rootProject.extra["ecosystemVersionName"] as String

        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            if (rootProject.extra["hasKeystore"] as Boolean) {
                storeFile = rootProject.file(rootProject.extra["keystoreStoreFile"] as String)
                storePassword = rootProject.extra["keystoreStorePassword"] as String
                keyAlias = rootProject.extra["keystoreKeyAlias"] as String
                keyPassword = rootProject.extra["keystoreKeyPassword"] as String
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
            if (rootProject.extra["hasKeystore"] as Boolean) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        compose = true
        // Exposes BuildConfig.VERSION_CODE, which this app's own build uses
        // as the "latest known" ecosystem versionCode when deciding whether
        // an installed pillar app is stale (see ApkInstaller).
        buildConfig = true
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
    // The launcher app only needs the design system and the dashboard
    // screen itself — every pillar app is a separate installed APK, so
    // :app never depends on :feature:<pillar> modules. Tapping a tile
    // launches the corresponding app via PackageManager instead of an
    // in-process navigation route.
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":feature:dashboard"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

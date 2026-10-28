import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.bernaferrari.wifiweakspot.android"
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }

    defaultConfig {
        applicationId = "com.bernaferrari.wifiweakspot"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Upload key for Play: create keystore.properties (storeFile, storePassword, keyAlias, keyPassword),
    // kept out of git. Without it, local release builds use the debug key, which Play rejects.
    val keystore = rootProject.file("keystore.properties").takeIf { it.exists() }?.let { file ->
        Properties().apply { file.inputStream().use(::load) }
    }
    signingConfigs {
        if (keystore != null) create("upload") {
            storeFile = rootProject.file(keystore.getProperty("storeFile"))
            storePassword = keystore.getProperty("storePassword")
            keyAlias = keystore.getProperty("keyAlias")
            keyPassword = keystore.getProperty("keyPassword")
        }
    }

    sourceSets.create("screenshots") {
        kotlin.srcDir("../demo-fixtures/src/main/kotlin")
    }

    buildTypes {
        // Captures run the shipping configuration (R8, not debuggable): what people install, and fast enough that
        // a busy emulator never shows an "isn't responding" dialog during first composition.
        create("screenshots") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".screenshots"
            matchingFallbacks += "release"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("upload") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(compose.components.uiToolingPreview)
    "screenshotsImplementation"(libs.koin.android)
    "screenshotsImplementation"(libs.androidx.datastore.preferences.core)
    "screenshotsImplementation"(libs.kotlinx.datetime)
    debugImplementation(compose.uiTooling)
}

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

/**
 * The real WifiWeakspot app running in a browser. It compiles :composeApp's shared source
 * (every screen, view model, navigation, and the design system) unchanged; only the
 * platform services (Wi-Fi, permissions, storage, router) are simulated here.
 */
kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("wifiweakspot-demo")
        browser {
            commonWebpackConfig { outputFileName = "wifiweakspot-demo.js" }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("../composeApp/src/main/kotlin")
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(libs.compose.material3)
                implementation(libs.materialKolor.material3)
                implementation(libs.kotlinx.serialization.core)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
                implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.1.2")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0")
                implementation(libs.koin.core)
                implementation("io.insert-koin:koin-compose:${libs.versions.koin.get()}")
                implementation(libs.koin.compose.viewmodel)
                implementation("androidx.room:room-common:2.8.5")
                implementation("androidx.datastore:datastore-preferences-core:${libs.versions.datastore.get()}")
                implementation(libs.kotlinx.datetime)
            }
        }
    }
}

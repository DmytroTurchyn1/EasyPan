/*
 * Created  8/9/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
}
val keystorePropertiesFile: File? = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile?.exists() == true) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
} else {
    logger.warn("Keystorefile not found")
}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
android {
    namespace = "com.cook.easypan"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.cook.easypan"
        minSdk = 28
        targetSdk = 37
        versionCode = 21
        versionName = "v1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Works whether the properties value is quoted or not; missing file yields "".
        buildConfigField(
            "String",
            "CLIENT_ID",
            "\"${keystoreProperties.getProperty("clientId")?.trim('"') ?: ""}\""
        )
    }
    signingConfigs {
        if (keystorePropertiesFile?.exists() == true && keystoreProperties.getProperty("storeFile") != null) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
                ?: logger.warn("Release signingConfig not configured; skipping assignment. Configure keystore.properties to enable signed release builds.")
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    sourceSets {
        // The benchmark build type mimics release; reuse its variant sources
        // (e.g. AppCheckInstaller with the Play Integrity provider).
        getByName("benchmark") {
            java.srcDirs("src/release/java")
            kotlin.srcDirs("src/release/java")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
    tasks.register("printVersion") {
        doLast {
            println(android.defaultConfig.versionName)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    implementation(libs.bundles.firebase)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.coil)

    debugImplementation(libs.bundles.compose.debug)
    // App Check debug provider must never ship in release builds.
    debugImplementation(libs.firebase.appcheck.debug)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.bundles.android.test)
    testImplementation(libs.bundles.test)
}

import com.android.build.api.variant.FilterConfiguration.FilterType
import com.android.build.api.variant.impl.VariantOutputImpl
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystorePropertiesExist = keystorePropertiesFile.exists()
val keystoreProperties = Properties().apply {
    if (keystorePropertiesExist) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

// Only release-type tasks need real signing credentials — don't fail
// assembleDebug/test/lint (or CI, which doesn't have this file) just because
// keystore.properties is absent, but fail fast and clearly if someone
// actually tries to build/sign a release without it.
val isBuildingRelease = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
check(keystorePropertiesExist || !isBuildingRelease) {
    "keystore.properties not found at ${keystorePropertiesFile.path} — required to sign the release build. " +
        "Create it with storeFile, storePassword, keyAlias, and keyPassword properties."
}

// AGP 9.4.0-alpha08 bug (https://issuetracker.google.com/issues/402800800):
// splits.abi is supposed to be ignored when building an app bundle, but R8's
// resource shrinking still runs once per ABI split and collides in
// buildReleasePreBundle. Keep splits.abi off for bundle tasks until fixed upstream.
val isBuildingBundle = gradle.startParameter.taskNames.any { it.contains("bundle", ignoreCase = true) }

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.hrithikvish.curler"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        applicationId = "com.hrithikvish.curler"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    splits {
        abi {
            isEnable = !isBuildingBundle
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
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
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val outputImpl = output as VariantOutputImpl
            val abi = output.filters.find { it.filterType.toString() == FilterType.ABI.toString() }?.identifier
            val abiPart = if (abi != null) "_$abi" else ""

            outputImpl.outputFileName = "cURLer_${variant.name}${abiPart}.apk"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
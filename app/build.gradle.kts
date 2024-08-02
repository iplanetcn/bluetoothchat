plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.phenix.bluetoothchat"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.phenix.bluetoothchat"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        aidl = false
        buildConfig = false
        compose = false
        prefab = false
        renderScript = false
        resValues = false
        shaders = false
        viewBinding = true
    }
    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }

    lint {
        disable.addAll(arrayOf("NotifyDataSetChanged", "MissingPermission"))
        // enable += "RtlHardcoded" + "RtlCompat" + "RtlEnabled"
        // checkOnly += "NewApi" + "InlinedApi"
        // quiet = true
        // abortOnError = false
        // ignoreWarnings = true
        // checkDependencies = true
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.core.google.shortcuts)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.gson)
//    implementation(libs.compose.compiler)
//    implementation(libs.appcompat)
    implementation(libs.google.material)
    implementation(libs.constraintlayout)
    implementation(libs.appcompat)
    implementation(libs.activity)
//    implementation(libs.startup.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
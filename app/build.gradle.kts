import com.google.gson.Gson
import org.gradle.kotlin.dsl.libs

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {


    namespace = "com.example.yukti"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.yukti"
        minSdk = 26
        targetSdk = 35
        versionCode = 58
        versionName = "6.3.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation ("com.google.guava:guava:32.1.3-android")
    implementation (libs.glide)
    implementation(libs.coil.compose)
    implementation (libs.androidx.material.icons.extended)
    implementation (libs.material3)
    implementation (libs.gson)
    implementation (libs.firebase.bom.v3223)
    implementation (libs.google.firebase.auth)
    implementation (libs.google.firebase.messaging)
    implementation (libs.google.firebase.database)

    implementation (libs.hilt.android)
    implementation (libs.okhttp)
    implementation (libs.gson)



    implementation(libs.generativeai)
    implementation (libs.androidx.navigation.compose)
    implementation (libs.jetbrains.kotlinx.coroutines.play.services)
    implementation (libs.play.services.auth)
    implementation (libs.firebase.auth.ktx)
    implementation (libs.kotlinx.coroutines.play.services.v161)
    implementation(libs.firebase.bom)

    implementation (libs.material3)
    implementation (libs.ui)
    implementation (libs.androidx.foundation)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.inappmessaging.display)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation (libs.androidx.core.splashscreen)
    implementation (libs.material3)

}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id ("realm-android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.kttelematic"
    compileSdk = 34

    packagingOptions{
        exclude("META-INF/DEPENDENCIES")
    }

    defaultConfig {
        applicationId = "com.example.kttelematic"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            with(manifestPlaceholders) {
                put("appName", "KT Telematic")
                put("mapKey", "AIzaSyCQuxPE9vQMMqy-m9R_7ZWYbPwfTYhv1nU")
            }
        }

        debug {
            isDebuggable = true
            isMinifyEnabled = false
            versionNameSuffix = "-dev"
            applicationIdSuffix = ".dev"
            with(manifestPlaceholders) {
                put("appName", "KT Telematic")
                put("mapKey", "AIzaSyCQuxPE9vQMMqy-m9R_7ZWYbPwfTYhv1nU")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }


    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.code.gson:gson:2.10.1") // Gson implementation
    implementation("com.google.android.gms:play-services-maps:18.2.0") // Map Implementation
    implementation("com.google.android.gms:play-services-location:21.2.0") // Map Implementation
    implementation("com.google.android.libraries.places:places:3.4.0") // Map Implementation
    implementation("com.airbnb.android:lottie:5.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Kotlin components
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.20")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Navigation Component
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")

    implementation("com.android.volley:volley:1.2.1")// Volley - Server API Calls
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.squareup.okhttp:okhttp-urlconnection:2.2.0")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.22"

    // Este es el plugin necesario para que Firebase funcione
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {    namespace = "com.example.zappy_mobile"
    compileSdk = 35 // Se recomienda usar 34 o 35 por ahora, 36 puede ser inestable

    defaultConfig {
        applicationId = "com.example.zappy_mobile"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = org.gradle.api.JavaVersion.VERSION_11
        targetCompatibility = org.gradle.api.JavaVersion.VERSION_11
    }
    }


dependencies {
    // --- Firebase ---
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-crashlytics-ndk")
    implementation("com.google.firebase:firebase-messaging")

    // --- Android UI ---
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // --- GLIDE (Para reproducir el GIF) ---
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}


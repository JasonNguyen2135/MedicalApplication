plugins {
    id("com.android.application")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}
android {
    namespace = "com.example.umc"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.umc"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Firebase BOM

    // Firebase modules
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
// Gson Converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// OKHttp Logging (để xem log request/response)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("io.noties.markwon:core:4.6.2")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("io.noties.markwon:core:4.6.2")


}

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.0"
}

val apiKey: String? = project.findProperty("API_KEY") as String?

android {
    namespace = "com.example.fusion0"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fusion0"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_KEY",  "\"${apiKey}\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}


dependencies {
    testImplementation ("androidx.test:core:1.4.0")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.objenesis:objenesis:3.2")
    androidTestImplementation("org.mockito:mockito-android:5.5.0")
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("org.conscrypt:conscrypt-android:2.5.2")
    testImplementation(libs.junit.junit)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    androidTestImplementation("org.mockito:mockito-core:5.3.1")
    androidTestImplementation("org.mockito:mockito-android:5.3.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation ("com.google.zxing:core:3.4.1")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation(libs.places)

    //Google maps, location and places libraries
    implementation("com.google.android.libraries.places:places:4.0.0")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")


    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    implementation("me.dm7.barcodescanner:zxing:1.9.8")

    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.test.espresso:espresso-intents:3.5.1")

    // OpenAI related dependencies
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.code.gson:gson:2.11.0")

    implementation("com.github.yalantis:ucrop:2.2.6")
    implementation("com.github.ybq:Android-SpinKit:1.4.0")

    implementation("com.airbnb.android:lottie-compose:6.6.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("com.facebook.shimmer:shimmer:0.1.0@aar")

}
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.dkr.kumbarastore"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dkr.kumbarastore"
        minSdk = 21
        targetSdk = 34
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
            // Menggunakan packaging (bukan packagingOptions) untuk menangani file META-INF/DEPENDENCIES
            packaging {
                // Menggunakan resources.excludes.add() untuk file selain .so
                resources {
                    excludes.add("META-INF/DEPENDENCIES")
                    excludes.add("META-INF/INDEX.LIST")
                    excludes.add("META-INF/LICENSE.md")
                    excludes.add("META-INF/NOTICE.md")
                    // tambahkan excludes lainnya jika diperlukan
                }
                // Jika Anda memiliki file .so yang perlu dieksklusi, gunakan jniLibs.excludes.add()
                // jniLibs.excludes.add("nama_file.so")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {

    implementation ("com.sun.mail:android-mail:1.6.6")
    implementation ("com.sun.mail:android-activation:1.6.6")

    implementation ("androidx.core:core:1.13.1")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.firebase:firebase-auth:21.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("androidx.annotation:annotation:1.6.0")

    implementation ("com.google.firebase:firebase-storage:21.0.0")
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
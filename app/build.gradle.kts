plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

@Suppress("unstableapiusage")
android {
    namespace = "com.doxart.myvpn"
    compileSdk = 35

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    defaultConfig {
        applicationId = "com.doxart.myvpn"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            javaCompileOptions {
                annotationProcessorOptions {
                    arguments["room.schemaLocation"] =
                            "$projectDir/schemas"
                }
            }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation ("com.github.doxart:EasyPops:1.0.8")

    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-perf")
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.google.firebase:firebase-messaging:24.1.0")

    implementation("com.google.android.gms:play-services-auth:21.3.0")

    implementation ("io.supercharge:shimmerlayout:2.1.0")

    implementation ("com.unity3d.ads-mediation:mediation-sdk:8.6.1")
    implementation ("com.unity3d.ads-mediation:adquality-sdk:7.22.4")

    implementation ("com.google.android.gms:play-services-appset:16.1.0")
    implementation ("com.google.android.gms:play-services-ads-identifier:18.2.0")
    implementation ("com.google.android.gms:play-services-basement:18.5.0")

    implementation ("com.revenuecat.purchases:purchases:8.11.0")

    implementation("com.android.support:multidex:1.0.3")

    implementation ("org.osmdroid:osmdroid-android:6.1.20")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.3.0")

    implementation ("fr.bmartel:jspeedtest:1.32.1")
    implementation ("com.github.Gruzer:simple-gauge-android:0.3.1")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.android.volley:volley:1.2.1")

    implementation ("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    implementation(project(mapOf("path" to ":vpnLib")))

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation ("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
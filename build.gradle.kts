buildscript {
    repositories {
        google()
        mavenCentral()
        maven { setUrl( "https://jitpack.io") }
        maven (url = "https://maven.google.com")
        maven ( url = "https://jitpack.io" )
        maven (url = "https://repository-achartengine.forge.cloudbees.com/snapshot/")
        maven {url = uri("https://android-sdk.is.com")}
    }
}

plugins {
    id("com.android.application") version "8.8.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
}
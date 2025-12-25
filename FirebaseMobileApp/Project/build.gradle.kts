// Top-level build file

plugins {
    id("com.android.application") version "8.7.1" apply false
    id("com.android.library") version "8.7.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

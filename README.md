# Foreground

A Flutter plugin to run an Android foreground service

## Getting Started

This plugin implements a foreground service for Android

## Proguard Rules

For release versions of an app, a proguard rules file needs to be provided

In your android/app/build.gradle file add:

buildTypes {
    release {
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }

In the android/app folder, place the file proguard-rules.pro


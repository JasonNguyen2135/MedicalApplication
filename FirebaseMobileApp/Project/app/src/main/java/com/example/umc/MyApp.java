package com.example.umc;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        Log.d("FIREBASE_TEST", "Firebase initialized: " + FirebaseApp.getInstance());
    }
}

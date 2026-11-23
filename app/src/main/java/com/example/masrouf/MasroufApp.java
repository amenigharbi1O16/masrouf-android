package com.example.masrouf;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class MasroufApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Langue par défaut : français (valeurs par défaut = res/values)
        if (AppCompatDelegate.getApplicationLocales().isEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("fr"));
        }
    }
}

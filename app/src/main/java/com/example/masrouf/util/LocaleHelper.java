package com.example.masrouf.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

/**
 * RÔLE : Gestionnaire centralisé du multilinguisme (i18n) et de la direction RTL/LTR.
 * Prend en charge l'anglais, le français et l'arabe avec bascule instantanée.
 */
public class LocaleHelper {

    private static final String PREF_NAME = "masrouf_locale_pref";
    private static final String KEY_LANG = "selected_language";

    public static Context setLocale(Context context, String language) {
        persist(context, language);
        return updateResources(context, language);
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, "fr"); // Français par défaut
    }

    private static void persist(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANG, language).apply();
    }

    public static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            configuration.setLayoutDirection(locale);
            return context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            configuration.setLayoutDirection(locale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }
}

package com.example.masrouf.util;

import java.util.Locale;

/**
 * RÔLE : Utilitaire de conversion et formatage monétaire en millimes entiers (Dinar Tunisien).
 * COUCHE MVC : Utilitaire (Model / Controller helper).
 * RÈGLE ARCHITECTURALE :
 *   - En Tunisie, 1 DT = 1 000 millimes.
 *   - Toutes les valeurs financières sont stockées en base SQLite sous forme d'entiers longs (millimes)
 *     afin d'éliminer définitivement les erreurs d'arrondi inhérentes aux types flottants IEEE 754 (float/double).
 *   - L'affichage se fait toujours au format standard tunisien avec exactement 3 décimales (ex: 14.500 DT).
 */
public final class MillimesUtil {

    private MillimesUtil() {
        // Empêche l'instanciation
    }

    /**
     * Convertit un montant en Dinars Tunisiens (DT) vers sa valeur entière en millimes.
     * Exemple : 14.500 DT -> 14500 millimes.
     */
    public static long dtToMillimes(double dt) {
        return Math.round(dt * 1000.0);
    }

    /**
     * Convertit une valeur en millimes entiers vers un double en Dinars Tunisiens (DT).
     * Exemple : 14500 millimes -> 14.500 DT.
     */
    public static double millimesToDt(long millimes) {
        return millimes / 1000.0;
    }

    /**
     * Formate une valeur en millimes sous la forme "XX.XXX DT".
     * Exemple : 14500 millimes -> "14.500 DT".
     */
    public static String formatMillimes(long millimes) {
        return String.format(Locale.US, "%.3f DT", millimes / 1000.0);
    }

    /**
     * Formate une valeur en DT direct sous la forme "XX.XXX DT".
     */
    public static String formatDt(double dt) {
        return String.format(Locale.US, "%.3f DT", dt);
    }

    /**
     * Parse une saisie utilisateur textuelle en Dinars Tunisiens vers des millimes entiers.
     * Gère la virgule ou le point comme séparateur décimal.
     */
    public static long parseDtToMillimes(String text) {
        if (text == null) return 0L;
        String clean = text.trim().replace(",", ".");
        if (clean.isEmpty()) return 0L;
        try {
            double val = Double.parseDouble(clean);
            return dtToMillimes(val);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}

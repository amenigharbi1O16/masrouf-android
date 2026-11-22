package com.example.masrouf.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.masrouf.model.User;

/**
 * RÔLE : Gestionnaire de session persistante de l'utilisateur connecté.
 * COUCHE MVC : Contrôleur (Controller).
 * EXIGENCES DU PROF COUVERTES :
 *   - Matrice §4 ligne 38 (Utilisation des SharedPreferences pour les états légers, Ch.6).
 *   - Matrice §4 ligne 1 (Maintien du rôle ADMIN ou USER pour les gardes de sécurité).
 *
 * Contrairement à la base SQLite qui stocke des données relationnelles volumineuses,
 * SharedPreferences est utilisé ici pour mémoriser simplement l'état d'authentification
 * (ID, rôle, email, état de connexion) sous forme de fichier XML clé/valeur privé.
 */
public class SessionManager {

    // Nom du fichier de préférences XML privé (stocké dans /shared_prefs/masrouf_session.xml)
    private static final String PREF_NAME = "masrouf_session";

    // Clés de stockage
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_LANGUAGE = "app_language";
    private static final String KEY_ACTIVE_SPACE = "active_space";
    private static final String KEY_FAMILY_ID = "family_id";
    private static final String KEY_FAMILY_NAME = "family_name";

    public static final String SPACE_PERSONAL = "PERSONAL";
    public static final String SPACE_FAMILY = "FAMILY";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    /**
     * Constructeur initialisant l'accès aux SharedPreferences en mode privé.
     * MODE_PRIVATE garantit que seul le package com.example.masrouf peut lire ce fichier.
     *
     * @param context Contexte Android
     */
    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = preferences.edit();
    }

    /**
     * Sauvegarde la session complète d'un utilisateur après une authentification réussie.
     *
     * @param user L'utilisateur authentifié
     */
    public void saveSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getFullName());
        editor.putString(KEY_USER_ROLE, user.getRole());
        // apply() enregistre les modifications de manière asynchrone sans bloquer le thread principal UI
        editor.apply();
    }

    /**
     * Vérifie si un utilisateur est actuellement connecté.
     *
     * @return true si une session valide est enregistrée, false sinon.
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Vérifie si l'utilisateur actuellement connecté possède les droits Administrateur.
     *
     * @return true si le rôle stocké est "ADMIN", false sinon.
     */
    public boolean isAdmin() {
        String role = preferences.getString(KEY_USER_ROLE, User.ROLE_USER);
        return User.ROLE_ADMIN.equalsIgnoreCase(role) || User.ROLE_PARENT.equalsIgnoreCase(role);
    }

    public boolean isParent() {
        String role = preferences.getString(KEY_USER_ROLE, User.ROLE_USER);
        return User.ROLE_PARENT.equalsIgnoreCase(role) || User.ROLE_ADMIN.equalsIgnoreCase(role);
    }

    public boolean isChild() {
        String role = preferences.getString(KEY_USER_ROLE, User.ROLE_USER);
        return User.ROLE_CHILD.equalsIgnoreCase(role);
    }

    public String getActiveSpace() {
        return preferences.getString(KEY_ACTIVE_SPACE, SPACE_PERSONAL);
    }

    public void setActiveSpace(String space) {
        editor.putString(KEY_ACTIVE_SPACE, space);
        editor.apply();
    }

    public long getFamilyId() {
        return preferences.getLong(KEY_FAMILY_ID, 1);
    }

    public void setFamilyId(long familyId) {
        editor.putLong(KEY_FAMILY_ID, familyId);
        editor.apply();
    }

    public String getFamilyName() {
        return preferences.getString(KEY_FAMILY_NAME, "Famille Ben Ali");
    }

    public void setFamilyName(String familyName) {
        editor.putString(KEY_FAMILY_NAME, familyName);
        editor.apply();
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, User.ROLE_USER);
    }

    /**
     * Déconnecte l'utilisateur en effaçant toutes les informations de session.
     */
    public void clearSession() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_ROLE);
        editor.apply();
    }

    public void logout() {
        clearSession();
    }

    /**
     * Mémorise la préférence de langue choisie (ex: "en", "fr", "ar").
     *
     * @param languageCode Code ISO de la langue.
     */
    public void saveLanguage(String languageCode) {
        editor.putString(KEY_LANGUAGE, languageCode);
        editor.apply();
    }

    /**
     * Récupère la langue actuellement configurée par l'utilisateur (français par défaut).
     *
     * @return Le code de langue ("fr", "en", ou "ar").
     */
    public String getLanguage() {
        return preferences.getString(KEY_LANGUAGE, "fr");
    }

    private static final String KEY_ONBOARDING_PREFIX = "onboarding_completed_";
    private static final String KEY_SELECTED_MONTH = "selected_month_key";

    public boolean isOnboardingCompleted(long userId) {
        return preferences.getBoolean(KEY_ONBOARDING_PREFIX + userId, false);
    }

    public boolean isOnboardingCompleted() {
        return isOnboardingCompleted(getUserId());
    }

    public void setOnboardingCompleted(long userId, boolean completed) {
        editor.putBoolean(KEY_ONBOARDING_PREFIX + userId, completed);
        editor.apply();
    }

    public void setOnboardingCompleted(boolean completed) {
        setOnboardingCompleted(getUserId(), completed);
    }

    public void createLoginSession(long id, String email, String name, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, id);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public void setSelectedMonthKey(String monthKey) {
        editor.putString(KEY_SELECTED_MONTH, monthKey);
        editor.apply();
    }

    public String getSelectedMonthKey() {
        return preferences.getString(KEY_SELECTED_MONTH, "2026-11");
    }

    public void updateUserProfile(String fullName, String email) {
        editor.putString(KEY_USER_NAME, fullName);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
}

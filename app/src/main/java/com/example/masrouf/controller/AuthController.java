package com.example.masrouf.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.masrouf.MainActivity;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.User;
import com.example.masrouf.util.PasswordUtil;
import com.example.masrouf.view.admin.AdminDashboardActivity;

import java.util.regex.Pattern;

/**
 * RÔLE : Contrôleur gérant la logique métier d'authentification, la validation des contraintes et l'aiguillage des rôles.
 * COUCHE MVC : Contrôleur (Controller).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°1 (2 types d'utilisateurs : ADMIN et USER avec redirection par rôle).
 *   - Exigence n°4 (Intents explicites avec transmission de données via extras, Ch.5).
 *   - Exigence n°9 (Pattern MVC : sépare la logique de validation de l'interface graphique).
 *
 * CONTRAINTES DE SÉCURITÉ APPLIQUÉES :
 * 1. Email : doit être une adresse valide se terminant strictement par "@gmail.com" (ex: name@gmail.com).
 * 2. Mot de passe : longueur minimale de 8 caractères, contenant au moins une majuscule, un chiffre et un caractère spécial.
 */
public class AuthController {

    private static final String TAG = "AuthController";

    // Clés standardisées pour le passage de données dans les Intents explicites (Ch.5 du cours)
    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_USER_EMAIL = "extra_user_email";
    public static final String EXTRA_USER_NAME = "extra_user_name";
    public static final String EXTRA_USER_ROLE = "extra_user_role";

    // Expression régulière pour vérifier que l'e-mail est bien sous le domaine @gmail.com
    private static final Pattern GMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@gmail\\.com$", Pattern.CASE_INSENSITIVE);

    // Expression régulière pour les contraintes de complexité du mot de passe :
    // - Longueur >= 8
    // - Au moins une lettre majuscule (?=.*[A-Z])
    // - Au moins un chiffre (?=.*[0-9])
    // - Au moins un caractère spécial / symbole (?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~])
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{};':\"|,.<>/?~";

    // Énumération des résultats possibles de la tentative de connexion
    public enum AuthStatus {
        SUCCESS,
        EMPTY_FIELDS,
        INVALID_EMAIL_DOMAIN,
        INVALID_PASSWORD_POLICY,
        INVALID_CREDENTIALS,
        ACCOUNT_SUSPENDED
    }

    /**
     * Classe encapsulant le résultat de l'opération d'authentification.
     */
    public static class LoginResult {
        private final AuthStatus status;
        private final User user;

        public LoginResult(AuthStatus status, User user) {
            this.status = status;
            this.user = user;
        }

        public AuthStatus getStatus() {
            return status;
        }

        public User getUser() {
            return user;
        }

        public boolean isSuccess() {
            return status == AuthStatus.SUCCESS;
        }
    }

    private final UserDao userDao;
    private final SessionManager sessionManager;

    /**
     * Constructeur initialisant les dépendances du contrôleur.
     *
     * @param context Contexte Android
     */
    public AuthController(Context context) {
        this.userDao = new UserDao(context);
        this.sessionManager = new SessionManager(context);
    }

    /**
     * Valide qu'une adresse email respecte le format standard et se termine strictement par @gmail.com.
     *
     * @param email L'adresse email à tester
     * @return true si l'adresse est un email @gmail.com valide, false sinon.
     */
    public static boolean isValidGmail(String email) {
        if (email == null) return false;
        return GMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valide les contraintes de sécurité du mot de passe :
     * - Longueur d'au moins 8 caractères
     * - Au moins une lettre majuscule (A-Z)
     * - Au moins un chiffre (0-9)
     * - Au moins un caractère spécial (!@#$%^&*...)
     *
     * @param password Le mot de passe en clair
     * @return true si toutes les contraintes sont respectées, false sinon.
     */
    public static boolean isValidPasswordPolicy(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (SPECIAL_CHARACTERS.indexOf(c) >= 0 || !Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }

        return hasUppercase && hasDigit && hasSpecial;
    }

    /**
     * Tente de connecter un utilisateur avec son e-mail et son mot de passe en clair.
     * Applique toutes les règles de validation avant d'interroger la base SQLite.
     *
     * @param email         L'adresse e-mail saisie par l'utilisateur (doit être @gmail.com)
     * @param plainPassword Le mot de passe en clair saisi
     * @return Un objet LoginResult détaillant le statut du traitement
     */
    public LoginResult login(String email, String plainPassword) {
        // 1. Validation de présence des champs obligatoires
        if (email == null || email.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            Log.w(TAG, "Tentative de connexion avec des champs vides");
            return new LoginResult(AuthStatus.EMPTY_FIELDS, null);
        }

        String cleanedEmail = email.trim().toLowerCase();

        // 2. Validation de la contrainte Email : doit obligatoirement être un @gmail.com
        if (!isValidGmail(cleanedEmail)) {
            Log.w(TAG, "Format d'e-mail invalide (doit être un @gmail.com) : " + cleanedEmail);
            return new LoginResult(AuthStatus.INVALID_EMAIL_DOMAIN, null);
        }

        // 3. Validation de la politique de mot de passe (8 car., 1 majuscule, 1 chiffre, 1 car. spécial)
        if (!isValidPasswordPolicy(plainPassword)) {
            Log.w(TAG, "Le mot de passe ne respecte pas les critères de sécurité requis");
            return new LoginResult(AuthStatus.INVALID_PASSWORD_POLICY, null);
        }

        // 4. Hachage immédiat du mot de passe en SHA-256 (ne jamais manipuler de mot de passe en clair)
        String passwordHash = PasswordUtil.hashPassword(plainPassword);

        // 5. Appel du DAO pour interroger la base SQLite de manière paramétrée
        User user = userDao.authenticate(cleanedEmail, passwordHash);

        // 6. Si aucun utilisateur ne correspond à cette combinaison email/hash
        if (user == null) {
            Log.w(TAG, "Échec d'authentification : identifiants incorrects pour " + cleanedEmail);
            return new LoginResult(AuthStatus.INVALID_CREDENTIALS, null);
        }

        // 7. Vérification de l'état d'activation du compte (actif vs suspendu)
        if (!user.isActive()) {
            Log.w(TAG, "Compte suspendu pour l'utilisateur : " + cleanedEmail);
            return new LoginResult(AuthStatus.ACCOUNT_SUSPENDED, user);
        }

        // 8. Succès : enregistrement de la session dans les SharedPreferences
        sessionManager.saveSession(user);
        Log.d(TAG, "Connexion validée pour : " + user.getEmail() + " [Rôle : " + user.getRole() + "]");

        return new LoginResult(AuthStatus.SUCCESS, user);
    }

    /**
     * Redirige l'utilisateur vers son écran d'accueil dédié selon son rôle applicatif.
     * Démontre l'utilisation des Intents explicites avec passage d'extras (Ch.5 du cours).
     *
     * @param currentActivity L'activité actuelle à terminer
     * @param user            L'utilisateur authentifié
     */
    public void redirectUserByRole(Activity currentActivity, User user) {
        Intent destinationIntent;

        if (user.isTemporaryPassword()) {
            // Mot de passe temporaire : redirection obligatoire vers l'écran de changement de mot de passe (Figma 03)
            Log.d(TAG, "Mot de passe temporaire : redirection vers ChangePasswordActivity");
            destinationIntent = new Intent(currentActivity, com.example.masrouf.view.ChangePasswordActivity.class);
        } else if (user.isAdmin()) {
            // Aiguillage RÔLE ADMIN -> Écran d'administration (Vue construite en Java pur)
            Log.d(TAG, "Redirection vers le tableau de bord Administrateur");
            destinationIntent = new Intent(currentActivity, AdminDashboardActivity.class);
        } else {
            // Aiguillage RÔLE USER -> Écran d'accueil utilisateur standard (Vue XML)
            Log.d(TAG, "Redirection vers l'accueil utilisateur standard");
            destinationIntent = new Intent(currentActivity, MainActivity.class);
        }

        // Transmission des données essentielles via les extras de l'Intent (Ch.5)
        destinationIntent.putExtra(EXTRA_USER_ID, user.getId());
        destinationIntent.putExtra(EXTRA_USER_EMAIL, user.getEmail());
        destinationIntent.putExtra(EXTRA_USER_NAME, user.getFullName());
        destinationIntent.putExtra(EXTRA_USER_ROLE, user.getRole());

        // Démarrage de l'activité cible
        currentActivity.startActivity(destinationIntent);

        // Clôture de l'écran de connexion pour que l'utilisateur ne revienne pas dessus avec le bouton Retour
        currentActivity.finish();
    }
}

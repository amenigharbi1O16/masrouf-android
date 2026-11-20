package com.example.masrouf.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.AuthController;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.model.User;
import com.example.masrouf.view.admin.AdminDashboardActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

/**
 * RÔLE : Activité de présentation et de captation des saisies pour l'authentification.
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°2 (Interface déclarée via layout XML : res/layout/activity_login.xml, Ch.4).
 *   - Matrice §4 ligne 7 (Listener explicite : l'activité elle-même implémente View.OnClickListener
 *                         et centralise le traitement avec v.getId() - Conforme au Lab 2 méthode 3).
 *   - Matrice §4 ligne 17 (Support multilingue dynamique : bascule EN / FR / AR).
 *   - Matrice §4 ligne 26 (Affichage combiné Toast pour erreurs et Snackbar pour confirmation).
 *   - Matrice §4 ligne 34 (Pattern MVC : zéro SQL dans l'activité, délégation au contrôleur).
 *   - Matrice §4 ligne 38 (Contrôle de session active au démarrage via SharedPreferences).
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    // Références vers les composants graphiques déclarés dans le fichier XML
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnCreateAccount;
    private TextView tvLangEn;
    private TextView tvLangFr;
    private TextView tvLangAr;

    // Dépendances architecturales (Contrôleurs)
    private AuthController authController;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialisation des contrôleurs métier
        this.sessionManager = new SessionManager(this);
        this.authController = new AuthController(this);

        // 2. Vérification immédiate de la session : si l'utilisateur est déjà authentifié,
        // on évite de réafficher le formulaire et on le redirige directement
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Session active détectée pour : " + sessionManager.getUserEmail());
            redirectLoggedInUser();
            return;
        }

        // 3. Liaison de l'activité avec son fichier de présentation XML (Ch.4 du cours)
        setContentView(R.layout.activity_login);

        // 4. Récupération des objets graphiques via leurs identifiants uniques R.id (Ch.4)
        initViews();

        // 5. Enregistrement des écouteurs d'événements (Listeners)
        // EXIGENCE LAB 2 MÉTHODE 3 : L'activité passe 'this' comme écouteur explicite
        setupListeners();
    }

    /**
     * Retrouve chaque composant graphique dans l'arbre des vues issu du layout XML.
     */
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLangEn = findViewById(R.id.tvLangEn);
        tvLangFr = findViewById(R.id.tvLangFr);
        tvLangAr = findViewById(R.id.tvLangAr);
    }

    /**
     * Associe l'activité comme listener de clics sur les boutons et sélecteurs.
     */
    private void setupListeners() {
        // Enregistrement explicite : l'activité écoute les événements de clic
        btnLogin.setOnClickListener(this);
        btnCreateAccount.setOnClickListener(this);
        tvLangEn.setOnClickListener(this);
        tvLangFr.setOnClickListener(this);
        tvLangAr.setOnClickListener(this);
    }

    /**
     * Méthode de rappel de l'interface View.OnClickListener (Lab 2, Méthode 3).
     * Tous les clics de l'écran convergent ici et sont aiguillés selon l'ID de la vue cliquée.
     *
     * @param v La vue ayant déclenché l'événement de clic
     */
    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        // Aiguillage par identifiant numérique R.id
        if (viewId == R.id.btnLogin) {
            // L'utilisateur clique sur "Log in"
            handleLoginAction();
        } else if (viewId == R.id.btnCreateAccount) {
            // L'utilisateur souhaite créer un nouveau compte
            handleCreateAccountAction();
        } else if (viewId == R.id.tvLangEn) {
            // Bascule vers la langue anglaise
            changeLanguage("en");
        } else if (viewId == R.id.tvLangFr) {
            // Bascule vers la langue française
            changeLanguage("fr");
        } else if (viewId == R.id.tvLangAr) {
            // Bascule vers la langue arabe
            changeLanguage("ar");
        }
    }

    /**
     * Traite la soumission du formulaire de connexion.
     * Récupère les données de la vue et délègue l'authentification au contrôleur.
     */
    private void handleLoginAction() {
        // 1. Récupération du texte saisi dans les champs EditText
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 2. Appel du contrôleur métier (Aucun calcul de hash ni requête SQL dans l'activité)
        AuthController.LoginResult result = authController.login(email, password);

        // 3. Gestion de l'affichage selon la réponse du contrôleur
        switch (result.getStatus()) {
            case SUCCESS:
                // Connexion réussie : notification visuelle via Snackbar Material (Matrice §4 ligne 26)
                Snackbar.make(findViewById(R.id.scrollLogin), R.string.msg_login_success, Snackbar.LENGTH_SHORT).show();

                // Redirection immédiate par Intent explicite selon le rôle (ADMIN ou USER)
                authController.redirectUserByRole(this, result.getUser());
                break;

            case EMPTY_FIELDS:
                // Champs incomplets : alerte rapide via Toast classique (Matrice §4 ligne 26)
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                if (email.isEmpty()) etEmail.requestFocus();
                else etPassword.requestFocus();
                break;

            case INVALID_EMAIL_DOMAIN:
                // Contrainte email : doit obligatoirement être @gmail.com
                Toast.makeText(this, R.string.error_invalid_email_domain, Toast.LENGTH_LONG).show();
                etEmail.requestFocus();
                break;

            case INVALID_PASSWORD_POLICY:
                // Contrainte mot de passe : 8 caractères, 1 majuscule, 1 chiffre, 1 caractère spécial
                Toast.makeText(this, R.string.error_password_policy, Toast.LENGTH_LONG).show();
                etPassword.requestFocus();
                break;

            case INVALID_CREDENTIALS:
                // Erreur d'identifiants
                Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_LONG).show();
                etPassword.setText(""); // Effacement sécurisé du champ mot de passe
                etPassword.requestFocus();
                break;

            case ACCOUNT_SUSPENDED:
                // Compte désactivé par l'administrateur
                Toast.makeText(this, R.string.error_account_suspended, Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Ouvre l'écran d'inscription (Figma 02).
     */
    private void handleCreateAccountAction() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    /**
     * Redirige un utilisateur déjà connecté selon son rôle mémorisé dans les SharedPreferences.
     */
    private void redirectLoggedInUser() {
        Intent intent;
        if (sessionManager.isAdmin()) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }

    /**
     * Change dynamiquement la langue de l'application et rafraîchit l'activité courante.
     * Démontre la gestion des ressources qualifiées (values, values-fr, values-ar) du Ch.3.
     *
     * @param languageCode Code de langue ("en", "fr", "ar")
     */
    private void changeLanguage(String languageCode) {
        // 1. Mémorisation du choix dans les préférences
        sessionManager.saveLanguage(languageCode);

        // 2. Configuration de la nouvelle Locale pour l'application
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        // 3. Application de la configuration
        createConfigurationContext(config);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // 4. Recréation de l'activité pour recharger tous les textes avec les bonnes ressources XML
        recreate();
    }
}

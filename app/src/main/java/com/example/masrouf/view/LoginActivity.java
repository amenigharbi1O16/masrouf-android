package com.example.masrouf.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.AuthController;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.view.admin.AdminDashboardActivity;
import com.example.masrouf.view.child.ChildMainActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

/**
 * RÔLE : Activité de présentation et de captation des saisies pour l'authentification (Image 4).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°2 (Interface déclarée via layout XML : res/layout/activity_login.xml, Ch.4).
 *   - Matrice §4 ligne 7 (Listener explicite : l'activité elle-même implémente View.OnClickListener
 *                         et centralise le traitement avec v.getId() - Conforme au Lab 2 méthode 3).
 *   - Matrice §4 ligne 17 (Support multilingue dynamique : bascule instantanée FR / AR / EN).
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

    private TextView btnLangFr;
    private TextView btnLangAr;
    private TextView btnLangEn;

    // Dépendances architecturales (Contrôleurs)
    private AuthController authController;
    private SessionManager sessionManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

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

        // 3. Liaison de l'activité avec son fichier de présentation XML
        setContentView(R.layout.activity_login);

        // 4. Récupération des objets graphiques
        initViews();

        // 5. Mise à jour de l'état visuel des pilules de langue
        updateLanguagePillsState(sessionManager.getLanguage());

        // 6. Enregistrement des écouteurs d'événements
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnLangFr = findViewById(R.id.btnLangFr);
        btnLangAr = findViewById(R.id.btnLangAr);
        btnLangEn = findViewById(R.id.btnLangEn);
    }

    private void setupListeners() {
        if (btnLogin != null) btnLogin.setOnClickListener(this);
        if (btnCreateAccount != null) btnCreateAccount.setOnClickListener(this);

        if (btnLangFr != null) btnLangFr.setOnClickListener(this);
        if (btnLangAr != null) btnLangAr.setOnClickListener(this);
        if (btnLangEn != null) btnLangEn.setOnClickListener(this);
    }

    private void updateLanguagePillsState(String currentLang) {
        if (btnLangFr == null || btnLangAr == null || btnLangEn == null) return;

        int activeBg = R.drawable.bg_pill_active;
        int inactiveBg = R.drawable.bg_pill_inactive;
        int activeText = Color.WHITE;
        int inactiveText = ContextCompat.getColor(this, R.color.deep_forest);

        if ("ar".equalsIgnoreCase(currentLang)) {
            btnLangAr.setBackgroundResource(activeBg);
            btnLangAr.setTextColor(activeText);

            btnLangFr.setBackgroundResource(inactiveBg);
            btnLangFr.setTextColor(inactiveText);

            btnLangEn.setBackgroundResource(inactiveBg);
            btnLangEn.setTextColor(inactiveText);
        } else if ("en".equalsIgnoreCase(currentLang)) {
            btnLangEn.setBackgroundResource(activeBg);
            btnLangEn.setTextColor(activeText);

            btnLangFr.setBackgroundResource(inactiveBg);
            btnLangFr.setTextColor(inactiveText);

            btnLangAr.setBackgroundResource(inactiveBg);
            btnLangAr.setTextColor(inactiveText);
        } else {
            // Français par défaut
            btnLangFr.setBackgroundResource(activeBg);
            btnLangFr.setTextColor(activeText);

            btnLangAr.setBackgroundResource(inactiveBg);
            btnLangAr.setTextColor(inactiveText);

            btnLangEn.setBackgroundResource(inactiveBg);
            btnLangEn.setTextColor(inactiveText);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.btnLogin) {
            handleLoginAction();
        } else if (viewId == R.id.btnCreateAccount) {
            handleCreateAccountAction();
        } else if (viewId == R.id.btnLangFr) {
            changeLanguage("fr");
        } else if (viewId == R.id.btnLangAr) {
            changeLanguage("ar");
        } else if (viewId == R.id.btnLangEn) {
            changeLanguage("en");
        }
    }

    private void handleLoginAction() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        AuthController.LoginResult result = authController.login(email, password);

        switch (result.getStatus()) {
            case SUCCESS:
                Snackbar.make(findViewById(R.id.scrollLogin), R.string.msg_login_success, Snackbar.LENGTH_SHORT).show();
                authController.redirectUserByRole(this, result.getUser());
                break;

            case EMPTY_FIELDS:
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                if (email.isEmpty()) etEmail.requestFocus();
                else etPassword.requestFocus();
                break;

            case INVALID_EMAIL_DOMAIN:
                Toast.makeText(this, R.string.error_invalid_email_domain, Toast.LENGTH_LONG).show();
                etEmail.requestFocus();
                break;

            case INVALID_PASSWORD_POLICY:
                Toast.makeText(this, R.string.error_password_policy, Toast.LENGTH_LONG).show();
                etPassword.requestFocus();
                break;

            case INVALID_CREDENTIALS:
                // Affiche un toast d'erreur pour mot de passe ou email incorrect
                Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_LONG).show();
                // Efface le mot de passe erroné
                etPassword.setText("");
                // Redonne le focus au champ de saisie du mot de passe
                etPassword.requestFocus();
                break;

            case ACCOUNT_SUSPENDED:
                Toast.makeText(this, "Ce compte est suspendu par le parent. Accès refusé.", Toast.LENGTH_LONG).show();
                etPassword.setText("");
                break;
        }
    }

    private void handleCreateAccountAction() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void redirectLoggedInUser() {
        // Déclare l'Intent de destination
        Intent intent;
        // Vérifie si l'utilisateur connecté est un parent
        if (sessionManager.isParent()) {
            // Redirige le parent vers son espace unifié sur MainActivity
            intent = new Intent(this, MainActivity.class);
        } else if (sessionManager.isChild()) {
            // Redirige l'enfant vers son accueil dédié ChildMainActivity
            intent = new Intent(this, ChildMainActivity.class);
        } else if (!sessionManager.isOnboardingCompleted()) {
            // Redirige le nouvel utilisateur vers les 3 écrans d'onboarding
            intent = new Intent(this, OnboardingWelcomeActivity.class);
        } else {
            // Redirige l'utilisateur personnel vers l'accueil standard
            intent = new Intent(this, MainActivity.class);
        }
        // Lance l'activité cible
        startActivity(intent);
        // Ferme l'écran de connexion pour empêcher le retour arrière
        finish();
    }

    private void changeLanguage(String languageCode) {
        sessionManager.saveLanguage(languageCode);
        LocaleHelper.setLocale(this, languageCode);
        recreate();
    }
}

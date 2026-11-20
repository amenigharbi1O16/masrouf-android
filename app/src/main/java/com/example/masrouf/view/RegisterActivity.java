package com.example.masrouf.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.AuthController;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.User;
import com.example.masrouf.util.PasswordUtil;

/**
 * RÔLE : Activité de création de compte pour un nouvel utilisateur (Figma 02).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°2 (Layout XML : res/layout/activity_register.xml).
 *   - Matrice §4 ligne 6 (Listener explicite via CLASSE ANONYME : new View.OnClickListener(){...}, Ch.4 / Lab 2).
 *   - Matrice §4 ligne 32 (Insertion d'un nouvel utilisateur dans SQLite via ContentValues).
 *   - Validation stricte : email @gmail.com et mot de passe (8 car., 1 majuscule, 1 chiffre, 1 symbole).
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etRegFullName;
    private EditText etRegEmail;
    private EditText etRegPassword;
    private EditText etRegConfirmPassword;
    private Button btnDoRegister;
    private Button btnBackToLogin;

    private UserDao userDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.userDao = new UserDao(this);
        this.sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etRegFullName = findViewById(R.id.etRegFullName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnDoRegister = findViewById(R.id.btnDoRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    /**
     * Enregistre les gestionnaires d'événements.
     * EXIGENCE DU COURS : Utilisation d'une CLASSE ANONYME pour le listener (Matrice §4 ligne 6).
     */
    private void setupListeners() {
        // 1. Bouton "Create account" : Listener explicite via une CLASSE ANONYME
        btnDoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Déclenchement de la procédure d'inscription
                performRegistration();
            }
        });

        // 2. Bouton "Already registered? Log in" : Retour vers LoginActivity
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Ferme simplement l'écran d'inscription pour révéler LoginActivity
            }
        });
    }

    /**
     * Valide les données saisies et procède à l'enregistrement dans la base SQLite.
     */
    private void performRegistration() {
        String fullName = etRegFullName.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim().toLowerCase();
        String password = etRegPassword.getText().toString().trim();
        String confirmPassword = etRegConfirmPassword.getText().toString().trim();

        // 1. Vérification des champs vides
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Vérification de la contrainte @gmail.com
        if (!AuthController.isValidGmail(email)) {
            Toast.makeText(this, R.string.error_invalid_email_domain, Toast.LENGTH_LONG).show();
            etRegEmail.requestFocus();
            return;
        }

        // 3. Vérification de la politique de mot de passe (8 car., 1 majuscule, 1 chiffre, 1 symbole)
        if (!AuthController.isValidPasswordPolicy(password)) {
            Toast.makeText(this, R.string.error_password_policy, Toast.LENGTH_LONG).show();
            etRegPassword.requestFocus();
            return;
        }

        // 4. Vérification de la correspondance des deux mots de passe
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas.", Toast.LENGTH_SHORT).show();
            etRegConfirmPassword.requestFocus();
            return;
        }

        // 5. Vérification de l'unicité de l'adresse email
        if (userDao.findByEmail(email) != null) {
            Toast.makeText(this, "Cette adresse email est déjà utilisée.", Toast.LENGTH_LONG).show();
            etRegEmail.requestFocus();
            return;
        }

        // 6. Hachage sécurisé du mot de passe en SHA-256 (ne jamais stocker en clair)
        String passwordHash = PasswordUtil.hashPassword(password);

        // 7. Création de la nouvelle entité User
        User newUser = new User(email, passwordHash, fullName, User.ROLE_USER);

        // 8. Insertion dans SQLite via le DAO
        long newUserId = userDao.insertUser(newUser);

        if (newUserId != -1) {
            newUser.setId(newUserId);
            Log.d(TAG, "Inscription réussie pour : " + email + ", nouvel ID = " + newUserId);
            Toast.makeText(this, "Compte créé avec succès ! Bienvenue.", Toast.LENGTH_LONG).show();

            // Enregistrement de la session active
            sessionManager.saveSession(newUser);

            // Redirection vers l'écran d'accueil
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(AuthController.EXTRA_USER_ID, newUserId);
            intent.putExtra(AuthController.EXTRA_USER_EMAIL, email);
            intent.putExtra(AuthController.EXTRA_USER_NAME, fullName);
            intent.putExtra(AuthController.EXTRA_USER_ROLE, User.ROLE_USER);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Erreur technique lors de la création du compte.", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.example.masrouf.view;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.AuthController;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.DatabaseHelper;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.User;
import com.example.masrouf.util.PasswordUtil;
import com.example.masrouf.view.child.ChildMainActivity;

/**
 * RÔLE : Activité imposant le changement de mot de passe lors de la première connexion (Figma 03).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°2 (Layout XML : res/layout/activity_change_password.xml).
 *   - Matrice §4 ligne 32 (Mise à jour SQLite via ContentValues pour modifier le mot de passe).
 *   - Matrice §4 ligne 39 (Hachage SHA-256 du nouveau mot de passe).
 */
import com.example.masrouf.BaseActivity;

public class ChangePasswordActivity extends BaseActivity {

    private static final String TAG = "ChangePasswordActivity";

    private EditText etTempPassword;
    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private Button btnSaveNewPassword;

    private UserDao userDao;
    private SessionManager sessionManager;
    private long userId;
    private String userEmail;

    private boolean isForcedPasswordChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        this.userDao = new UserDao(this);
        this.sessionManager = new SessionManager(this);

        // Lecture des informations passées en extras
        userId = getIntent().getLongExtra(AuthController.EXTRA_USER_ID, sessionManager.getUserId());
        userEmail = getIntent().getStringExtra(AuthController.EXTRA_USER_EMAIL);
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = sessionManager.getUserEmail();
        }

        User user = userDao.getUserById(userId);
        if (user != null && user.isTemporaryPassword()) {
            isForcedPasswordChange = true;
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isForcedPasswordChange) {
                    Toast.makeText(ChangePasswordActivity.this, "Changement de mot de passe obligatoire. Impossible de revenir en arrière.", Toast.LENGTH_SHORT).show();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        etTempPassword = findViewById(R.id.etTempPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnSaveNewPassword = findViewById(R.id.btnSaveNewPassword);
    }

    private void setupListeners() {
        btnSaveNewPassword.setOnClickListener(v -> performPasswordChange());
    }

    private void performPasswordChange() {
        String tempPass = etTempPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmNewPass = etConfirmNewPassword.getText().toString().trim();

        // 1. Vérification des champs vides
        if (tempPass.isEmpty() || newPass.isEmpty() || confirmNewPass.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Vérification que le mot de passe temporaire saisi est correct
        String tempHash = PasswordUtil.hashPassword(tempPass);
        User currentUser = userDao.authenticate(userEmail, tempHash);
        if (currentUser == null) {
            Toast.makeText(this, "Le mot de passe temporaire est incorrect.", Toast.LENGTH_LONG).show();
            etTempPassword.requestFocus();
            return;
        }

        // 3. Vérification de la politique de mot de passe (au moins 8 caractères et au moins un chiffre)
        boolean hasDigit = newPass.matches(".*\\d.*");
        if (newPass.length() < 8 || !hasDigit) {
            Toast.makeText(this, "Le nouveau mot de passe doit contenir au moins 8 caractères et au moins 1 chiffre.", Toast.LENGTH_LONG).show();
            etNewPassword.requestFocus();
            return;
        }

        // 4. Vérification de correspondance
        if (!newPass.equals(confirmNewPass)) {
            Toast.makeText(this, "Les nouveaux mots de passe ne correspondent pas.", Toast.LENGTH_SHORT).show();
            etConfirmNewPassword.requestFocus();
            return;
        }

        // 5. Hachage du nouveau mot de passe
        String newHash = PasswordUtil.hashPassword(newPass);

        // 6. Mise à jour dans SQLite via DatabaseHelper
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, newHash);
        cv.put(DatabaseHelper.COLUMN_USER_IS_TEMP_PASSWORD, 0); // Désactivation de l'obligation de changement

        int rowsUpdated = db.update(
                DatabaseHelper.TABLE_USERS,
                cv,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }
        );

        if (rowsUpdated > 0) {
            Log.d(TAG, "Mot de passe mis à jour avec succès pour l'utilisateur ID = " + userId);
            Toast.makeText(this, "Mot de passe mis à jour avec succès !", Toast.LENGTH_SHORT).show();

            // Mise à jour de l'objet utilisateur en session
            currentUser.setPasswordHash(newHash);
            currentUser.setTemporaryPassword(false);
            sessionManager.saveSession(currentUser);

            // Redirection vers l'accueil utilisateur dédié selon son rôle
            Intent intent;
            if (currentUser.isChild()) {
                intent = new Intent(this, ChildMainActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de la mise à jour du mot de passe.", Toast.LENGTH_SHORT).show();
        }
    }
}

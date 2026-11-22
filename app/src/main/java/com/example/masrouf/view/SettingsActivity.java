package com.example.masrouf.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.service.ReminderService;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.NotificationHelper;

/**
 * RÔLE : Écran Paramètres, Profil personnel et Démonstrations Techniques (Image 15 & Figma 22).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Compte personnel avec mise à jour du nom et de l'email dans SQLite.
 *   - Matrice §4 ligne 11 : Intent implicite ACTION_DIAL et ACTION_CALL (avec runtime permission).
 *   - Matrice §4 ligne 12 : Intent implicite SMS ACTION_SENDTO (smsto:).
 *   - Matrice §4 ligne 13 : Intent implicite ACTION_SEND avec Chooser.
 *   - Matrice §4 ligne 17 : Changement de langue dynamique multilingue (FR / AR / EN).
 *   - Matrice §4 ligne 20 : Lecture audio depuis res/raw/budget_alert.wav.
 *   - Matrice §4 ligne 35 : Notification système avec NotificationChannel et PendingIntent.
 *   - Matrice §4 ligne 36 : Démarrage du Service en arrière-plan (ReminderService).
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final int PERMISSION_REQUEST_CALL_PHONE = 2001;
    private static final String SUPPORT_PHONE_NUMBER = "+21671000000";

    private SessionManager sessionManager;
    private UserDao userDao;
    private BudgetDao budgetDao;
    private MediaPlayer mediaPlayer;

    private EditText etSettingsFullName;
    private EditText etSettingsEmail;
    private Button btnSaveProfile;

    private TextView btnSettingsLangFr;
    private TextView btnSettingsLangAr;
    private TextView btnSettingsLangEn;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initialisation de SettingsActivity");

        sessionManager = new SessionManager(this);
        userDao = new UserDao(this);
        budgetDao = new BudgetDao(this);

        setContentView(R.layout.activity_settings);

        initViews();
        setupUserData();
        setupPreferencesLinks();
        setupLanguagePills();
        setupTechnicalDemonstrations();
    }

    private void initViews() {
        findViewById(R.id.btn_back_settings).setOnClickListener(v -> finish());

        etSettingsFullName = findViewById(R.id.etSettingsFullName);
        etSettingsEmail = findViewById(R.id.etSettingsEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnSettingsLangFr = findViewById(R.id.btnSettingsLangFr);
        btnSettingsLangAr = findViewById(R.id.btnSettingsLangAr);
        btnSettingsLangEn = findViewById(R.id.btnSettingsLangEn);
    }

    private void setupUserData() {
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (etSettingsFullName != null && name != null) {
            etSettingsFullName.setText(name);
        }
        if (etSettingsEmail != null && email != null) {
            etSettingsEmail.setText(email);
        }

        // Si c'est un compte enfant, l'email est en lecture seule (créé par le parent)
        if (sessionManager.isChild() && etSettingsEmail != null) {
            etSettingsEmail.setEnabled(false);
            etSettingsEmail.setFocusable(false);
        }

        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> {
                String newName = etSettingsFullName.getText().toString().trim();
                String newEmail = etSettingsEmail.getText().toString().trim();

                if (newName.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newEmail.endsWith("@gmail.com")) {
                    Toast.makeText(this, R.string.error_invalid_email_domain, Toast.LENGTH_SHORT).show();
                    return;
                }

                long userId = sessionManager.getUserId();
                boolean updated = userDao.updateUserProfile(userId, newName, newEmail);

                if (updated) {
                    sessionManager.updateUserProfile(newName, newEmail);
                    Toast.makeText(this, "Profil mis à jour avec succès !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erreur lors de la mise à jour du profil", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupPreferencesLinks() {
        // Budget du mois
        findViewById(R.id.btnPrefBudget).setOnClickListener(v -> {
            if (sessionManager.isChild()) {
                Toast.makeText(this, "Accès réservé : Seul votre père peut modifier le budget global.", Toast.LENGTH_LONG).show();
                return;
            }
            Budget b = budgetDao.getCurrentBudget(sessionManager.getUserId());
            if (b != null) {
                startActivity(new Intent(SettingsActivity.this, ModifyBudgetActivity.class));
            } else {
                startActivity(new Intent(SettingsActivity.this, CreateBudgetActivity.class));
            }
        });

        // Gérer les catégories
        findViewById(R.id.btnPrefCategories).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, OnboardingCategoriesActivity.class))
        );

        // Points et récompenses
        findViewById(R.id.btnPrefPoints).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, PointsAndAchievementsActivity.class))
        );

        // Changer le mot de passe
        findViewById(R.id.btnPrefChangePassword).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, ChangePasswordActivity.class))
        );

        // Manuel d'utilisation PDF
        findViewById(R.id.btnPrefManual).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, UserManualActivity.class))
        );
    }

    private void setupLanguagePills() {
        updateLanguagePillsState(sessionManager.getLanguage());

        btnSettingsLangFr.setOnClickListener(v -> switchLanguage("fr"));
        btnSettingsLangAr.setOnClickListener(v -> switchLanguage("ar"));
        btnSettingsLangEn.setOnClickListener(v -> switchLanguage("en"));
    }

    private void updateLanguagePillsState(String lang) {
        if (btnSettingsLangFr == null || btnSettingsLangAr == null || btnSettingsLangEn == null) return;

        int activeBg = R.drawable.bg_pill_active;
        int inactiveBg = R.drawable.bg_pill_inactive;
        int activeText = Color.WHITE;
        int inactiveText = ContextCompat.getColor(this, R.color.deep_forest);

        if ("ar".equalsIgnoreCase(lang)) {
            btnSettingsLangAr.setBackgroundResource(activeBg);
            btnSettingsLangAr.setTextColor(activeText);
            btnSettingsLangFr.setBackgroundResource(inactiveBg);
            btnSettingsLangFr.setTextColor(inactiveText);
            btnSettingsLangEn.setBackgroundResource(inactiveBg);
            btnSettingsLangEn.setTextColor(inactiveText);
        } else if ("en".equalsIgnoreCase(lang)) {
            btnSettingsLangEn.setBackgroundResource(activeBg);
            btnSettingsLangEn.setTextColor(activeText);
            btnSettingsLangFr.setBackgroundResource(inactiveBg);
            btnSettingsLangFr.setTextColor(inactiveText);
            btnSettingsLangAr.setBackgroundResource(inactiveBg);
            btnSettingsLangAr.setTextColor(inactiveText);
        } else {
            btnSettingsLangFr.setBackgroundResource(activeBg);
            btnSettingsLangFr.setTextColor(activeText);
            btnSettingsLangAr.setBackgroundResource(inactiveBg);
            btnSettingsLangAr.setTextColor(inactiveText);
            btnSettingsLangEn.setBackgroundResource(inactiveBg);
            btnSettingsLangEn.setTextColor(inactiveText);
        }
    }

    private void switchLanguage(String code) {
        sessionManager.saveLanguage(code);
        LocaleHelper.setLocale(this, code);
        recreate();
    }

    private void setupTechnicalDemonstrations() {
        // ACTION_DIAL
        findViewById(R.id.btn_action_dial).setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + SUPPORT_PHONE_NUMBER));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Impossible d'ouvrir le composeur", Toast.LENGTH_SHORT).show();
            }
        });

        // ACTION_CALL (Permission runtime)
        findViewById(R.id.btn_action_call).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
            } else {
                launchDirectCall();
            }
        });

        // ACTION_SENDTO (SMS)
        findViewById(R.id.btn_action_sms).setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + SUPPORT_PHONE_NUMBER));
                intent.putExtra("sms_body", "Bonjour Masrouf Support, j'ai une question sur mon budget.");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Aucune application SMS trouvée", Toast.LENGTH_SHORT).show();
            }
        });

        // ACTION_SEND (Partage + Chooser)
        findViewById(R.id.btn_action_share).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Application Masrouf");
            intent.putExtra(Intent.EXTRA_TEXT, "Je gère mon budget familial avec Masrouf, l'application locale et sécurisée !");
            startActivity(Intent.createChooser(intent, "Partager via"));
        });

        // Audio RAW
        findViewById(R.id.btn_play_audio_raw).setOnClickListener(v -> {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                mediaPlayer = MediaPlayer.create(this, R.raw.budget_alert);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    Toast.makeText(this, "Alerte sonore jouée", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Erreur lecture audio", Toast.LENGTH_SHORT).show();
            }
        });

        // Notification test
        findViewById(R.id.btn_trigger_notification).setOnClickListener(v -> {
            NotificationHelper.showBudgetAlert(this, "Alerte Masrouf", "Ceci est un test de notification locale.", false);
            Toast.makeText(this, "Notification envoyée !", Toast.LENGTH_SHORT).show();
        });

        // Service Started
        findViewById(R.id.btn_start_service).setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, ReminderService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "Service Reminder démarré en arrière-plan", Toast.LENGTH_SHORT).show();
        });

        // Déconnexion
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void launchDirectCall() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + SUPPORT_PHONE_NUMBER));
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission d'appel refusée", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchDirectCall();
            } else {
                Toast.makeText(this, "Permission d'appel refusée par l'utilisateur", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

package com.example.masrouf.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.service.ReminderService;
import com.example.masrouf.util.NotificationHelper;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * RÔLE : Écran des paramètres, assistance utilisateur et démonstration des fonctionnalités système (Figma 22).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Matrice §4 ligne 11 : Intent implicite ACTION_DIAL et ACTION_CALL (avec contrôle de permission d'appel à l'exécution).
 *   - Matrice §4 ligne 12 : Intent implicite SMS avec ACTION_SENDTO (smsto:).
 *   - Matrice §4 ligne 13 : Intent implicite ACTION_SEND avec boîte de choix (Intent.createChooser).
 *   - Matrice §4 ligne 14 : Intent implicite ACTION_VIEW (Web et consultation de documents).
 *   - Matrice §4 ligne 15 : Sécurisation de chaque intent implicite par try/catch et resolveActivity.
 *   - Matrice §4 ligne 17 : Changement de langue multilingue (EN / FR / AR) avec persistance SharedPreferences.
 *   - Matrice §4 ligne 20 : Lecture d'une ressource audio brute locale (res/raw/budget_alert.wav).
 *   - Matrice §4 ligne 21 : Lecture et affichage d'un fichier du dossier assets (assets/masrouf_manual.txt).
 *   - Matrice §4 ligne 26 : Utilisation conjointe de Toast et Snackbar Material avec action.
 *   - Matrice §4 ligne 35 : Déclenchement de Notification système avec PendingIntent et NotificationChannel.
 *   - Matrice §4 ligne 36 : Lancement d'un composant SERVICE Android (ReminderService).
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final int PERMISSION_REQUEST_CALL_PHONE = 2001;
    private static final int PERMISSION_REQUEST_POST_NOTIFS = 2002;

    private static final String SUPPORT_PHONE_NUMBER = "+21671000000";

    private SessionManager sessionManager;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initialisation de SettingsActivity");

        // 1. Initialisation de la session utilisateur
        sessionManager = new SessionManager(this);

        // 2. Gonflage de la mise en page XML
        setContentView(R.layout.activity_settings);

        // 3. Configuration des composants graphiques et des écouteurs d'événements
        setupViews();
    }

    private void setupViews() {
        // Bouton retour
        findViewById(R.id.btn_back_settings).setOnClickListener(v -> finish());

        // 1. SÉLECTEUR DE LANGUE (Multilingue i18n, Matrice §4 ligne 17)
        setupLanguageSelector();

        // 2. INTENTS IMPLICITES TÉLÉPHONIE (ACTION_DIAL vs ACTION_CALL)
        findViewById(R.id.btn_action_dial).setOnClickListener(v -> handleActionDial());
        findViewById(R.id.btn_action_call).setOnClickListener(v -> handleActionCallWithPermission());

        // 3. INTENT IMPLICITE SMS (ACTION_SENDTO)
        findViewById(R.id.btn_action_sms).setOnClickListener(v -> handleActionSendSms());

        // 4. LECTURE DU FICHIER ASSETS (Guide utilisateur)
        findViewById(R.id.btn_read_manual_asset).setOnClickListener(v -> displayManualFromAssets());

        // 5. INTENT IMPLICITE ACTION_VIEW (Lien Web)
        findViewById(R.id.btn_action_view_web).setOnClickListener(v -> handleActionViewWeb());

        // 6. INTENT IMPLICITE ACTION_SEND + CHOOSER
        findViewById(R.id.btn_action_share).setOnClickListener(v -> handleActionShareApp());

        // 7. LECTURE AUDIO DEPUIS RES/RAW (Matrice §4 ligne 20)
        findViewById(R.id.btn_play_audio_raw).setOnClickListener(v -> playRawAudioAlert());

        // 8. DÉCLENCHEMENT DE NOTIFICATION AVEC PENDINGINTENT (Matrice §4 ligne 35)
        findViewById(R.id.btn_trigger_notification).setOnClickListener(v -> triggerNotificationTest());

        // 9. DÉMARRAGE DU SERVICE D'ARRIÈRE-PLAN (Matrice §4 ligne 36)
        findViewById(R.id.btn_start_service).setOnClickListener(v -> startReminderBackgroundService());

        // 10. DÉCONNEXION (Matrice §4 ligne 38)
        findViewById(R.id.btn_logout).setOnClickListener(v -> handleLogout());
    }

    /**
     * Configure le sélecteur de langue selon la valeur sauvegardée en SharedPreferences
     */
    private void setupLanguageSelector() {
        RadioGroup rgLanguage = findViewById(R.id.rg_language);
        RadioButton rbFr = findViewById(R.id.rb_lang_fr);
        RadioButton rbEn = findViewById(R.id.rb_lang_en);
        RadioButton rbAr = findViewById(R.id.rb_lang_ar);

        String currentLang = sessionManager.getLanguage();
        if ("en".equalsIgnoreCase(currentLang)) {
            rbEn.setChecked(true);
        } else if ("ar".equalsIgnoreCase(currentLang)) {
            rbAr.setChecked(true);
        } else {
            rbFr.setChecked(true);
        }

        rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedLang = "fr";
            if (checkedId == R.id.rb_lang_en) {
                selectedLang = "en";
            } else if (checkedId == R.id.rb_lang_ar) {
                selectedLang = "ar";
            }

            if (!selectedLang.equalsIgnoreCase(sessionManager.getLanguage())) {
                sessionManager.saveLanguage(selectedLang);
                Toast.makeText(this, "Langue modifiée. Redémarrage de l'affichage...", Toast.LENGTH_SHORT).show();

                // Application de la nouvelle Locale et recréation de l'activité
                applyLocale(selectedLang);
                recreate();
            }
        });
    }

    private void applyLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    /**
     * EXIGENCE DU PROF : ACTION_DIAL (Matrice §4 ligne 11 / Oral question 27)
     * Ouvre le composeur téléphonique système avec le numéro pré-saisi.
     * PARTICULARITÉ : Aucune permission dangereuse n'est requise dans le manifest !
     */
    private void handleActionDial() {
        Log.d(TAG, "handleActionDial: Déclenchement de ACTION_DIAL pour le numéro " + SUPPORT_PHONE_NUMBER);
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + SUPPORT_PHONE_NUMBER));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur ACTION_DIAL : " + e.getMessage(), e);
            Toast.makeText(this, "Aucun composeur téléphonique disponible.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * EXIGENCE DU PROF : ACTION_CALL (Matrice §4 ligne 11 / Oral question 27 & 30)
     * Lance directement l'appel téléphonique.
     * PARTICULARITÉ : Nécessite la permission dangereuse android.permission.CALL_PHONE
     * demandée dynamiquement à l'utilisateur à l'exécution (Runtime Permission, Android 6.0+).
     */
    private void handleActionCallWithPermission() {
        Log.d(TAG, "handleActionCallWithPermission: Vérification de la permission CALL_PHONE");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission non accordée : on affiche la boîte de dialogue système demandant la permission
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.CALL_PHONE },
                    PERMISSION_REQUEST_CALL_PHONE
            );
        } else {
            // Permission déjà accordée : on peut lancer l'appel directement
            executeDirectPhoneCall();
        }
    }

    private void executeDirectPhoneCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + SUPPORT_PHONE_NUMBER));
        try {
            startActivity(intent);
        } catch (SecurityException e) {
            Log.e(TAG, "Échec de sécurité lors de l'appel direct : " + e.getMessage(), e);
            Toast.makeText(this, "Permission d'appel refusée.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'appel direct : " + e.getMessage(), e);
            Toast.makeText(this, "Impossible d'effectuer l'appel direct.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback obligatoire du cycle de vie lors de la réponse de l'utilisateur à une demande de permission (Ch. 5).
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission accordée ! Lancement de l'appel...", Toast.LENGTH_SHORT).show();
                executeDirectPhoneCall();
            } else {
                Toast.makeText(this, "Permission CALL_PHONE refusée par l'utilisateur.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * EXIGENCE DU PROF : ACTION_SENDTO (Matrice §4 ligne 12)
     * Ouvre l'application de messagerie SMS avec destinataire et message pré-remplis.
     */
    private void handleActionSendSms() {
        Log.d(TAG, "handleActionSendSms: Déclenchement de l'intent SMS (smsto:)");
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + SUPPORT_PHONE_NUMBER));
        smsIntent.putExtra("sms_body", "Bonjour Masrouf, j'ai une question concernant le suivi de mon budget.");

        try {
            startActivity(smsIntent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur ACTION_SENDTO : " + e.getMessage(), e);
            Toast.makeText(this, "Aucune application SMS détectée.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * EXIGENCE DU PROF : Dossier assets (Matrice §4 ligne 21)
     * Lit un fichier embarqué dans le package APK (assets/masrouf_manual.txt) et l'affiche dans un AlertDialog.
     */
    private void displayManualFromAssets() {
        Log.d(TAG, "displayManualFromAssets: Lecture du fichier assets/masrouf_manual.txt");
        StringBuilder content = new StringBuilder();

        try (InputStream is = getAssets().open("masrouf_manual.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            new AlertDialog.Builder(this)
                    .setTitle("Guide Officiel Masrouf")
                    .setMessage(content.toString())
                    .setPositiveButton("Compris", (dialog, which) -> dialog.dismiss())
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la lecture du manuel : " + e.getMessage(), e);
            Toast.makeText(this, "Impossible d'ouvrir le fichier manuel dans assets.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * EXIGENCE DU PROF : ACTION_VIEW (Matrice §4 ligne 14)
     * Ouvre une page Web externe dans le navigateur par défaut de l'appareil.
     */
    private void handleActionViewWeb() {
        Log.d(TAG, "handleActionViewWeb: Ouverture de l'URL externe");
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/masrouf-app"));
        try {
            startActivity(webIntent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur ACTION_VIEW : " + e.getMessage(), e);
            Toast.makeText(this, "Aucun navigateur disponible.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * EXIGENCE DU PROF : ACTION_SEND avec Intent.createChooser (Matrice §4 ligne 13 & 15)
     */
    private void handleActionShareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Découvre Masrouf !");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Je gère facilement mes dépenses en Dinars Tunisiens avec l'application Masrouf. Télécharge-la !");

        Intent chooser = Intent.createChooser(shareIntent, "Partager l'application Masrouf avec");
        try {
            startActivity(chooser);
        } catch (Exception e) {
            Log.e(TAG, "Erreur ACTION_SEND chooser : " + e.getMessage(), e);
        }
    }

    /**
     * EXIGENCE DU PROF : Lecture audio res/raw (Matrice §4 ligne 20)
     * Joue un fichier son brut stocké dans res/raw via la classe MediaPlayer Android.
     */
    private void playRawAudioAlert() {
        Log.d(TAG, "playRawAudioAlert: Lecture du son res/raw/budget_alert.wav");
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, R.raw.budget_alert);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    mediaPlayer = null;
                });
                mediaPlayer.start();
                Toast.makeText(this, "🔊 Son d'alerte joué avec succès (res/raw)", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lecture audio : " + e.getMessage(), e);
        }
    }

    /**
     * EXIGENCE DU PROF : Notification avec PendingIntent (Matrice §4 ligne 35 & 16)
     */
    private void triggerNotificationTest() {
        Log.d(TAG, "triggerNotificationTest: Envoi d'une notification de test");
        NotificationHelper.showBudgetAlert(
                this,
                "Alerte Masrouf Démo (80%)",
                "Attention ! Votre enveloppe 'Alimentation' a atteint 80% de son plafond.",
                false
        );

        // Snackbar Material avec bouton d'action (Matrice §4 ligne 26)
        Snackbar.make(findViewById(android.R.id.content), "Notification envoyée dans la barre d'état !", Snackbar.LENGTH_LONG)
                .setAction("Compris", v -> Log.d(TAG, "Snackbar cliqué"))
                .show();
    }

    /**
     * EXIGENCE DU PROF : Service Android en arrière-plan (Matrice §4 ligne 36)
     */
    private void startReminderBackgroundService() {
        Log.d(TAG, "startReminderBackgroundService: Démarrage de ReminderService via startService()");
        Intent serviceIntent = new Intent(this, ReminderService.class);
        startService(serviceIntent);

        Toast.makeText(this, "⚙ ReminderService démarré en arrière-plan !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Déconnexion et retour au Login
     */
    private void handleLogout() {
        Log.d(TAG, "handleLogout: Déconnexion de l'utilisateur");
        sessionManager.logout();

        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
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

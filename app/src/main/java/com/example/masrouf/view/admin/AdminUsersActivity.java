package com.example.masrouf.view.admin;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.DatabaseHelper;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.view.LoginActivity;

/**
 * RÔLE : Gestion des comptes utilisateurs par l'administrateur (Figma 36).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°1 : Gestion des 2 rôles (écran protégé par un contrôle d'accès ADMIN).
 *   - Matrice §4 ligne 12 : Intent implicite SMS ACTION_SENDTO (envoi du mot de passe temporaire).
 *   - Matrice §4 ligne 29 : ListView liée directement à un CURSOR SQL via SIMPLECURSORADAPTER (Ch. 6 du cours).
 *   - Matrice §4 ligne 32 & 33 : Requêtes de mise à jour paramétrées SQLite via UserDao.
 *
 * PARTICULARITÉ PÉDAGOGIQUE POUR LE PROFESSEUR (Oral Question 37) :
 * Pourquoi utiliser SimpleCursorAdapter plutôt qu'un ArrayAdapter ?
 * - ArrayAdapter exige de convertir chaque ligne SQL en objet Java en mémoire (User.java, Product.java).
 * - SimpleCursorAdapter fait une liaison directe (binding) entre les colonnes du Cursor SQLite et les vues du layout XML
 *   sans aucune allocation d'objets intermédiaires. La seule contrainte est la présence obligatoire d'une colonne nommée '_id'.
 */
public class AdminUsersActivity extends AppCompatActivity {

    private static final String TAG = "AdminUsersActivity";

    private SessionManager sessionManager;
    private UserDao userDao;

    private ListView lvUsers;
    private TextView tvEmpty;
    private SimpleCursorAdapter cursorAdapter;
    private Cursor currentCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initialisation de AdminUsersActivity");

        // 1. Initialisation des composants MVC
        sessionManager = new SessionManager(this);
        userDao = new UserDao(this);

        // 2. GARDE DE SÉCURITÉ : Vérification du rôle ADMIN (Exigence n°1)
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Log.w(TAG, "Accès refusé : utilisateur non admin. Redirection vers Login.");
            Toast.makeText(this, "Accès réservé aux administrateurs.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 3. Gonflage de la mise en page
        setContentView(R.layout.activity_admin_users);

        lvUsers = findViewById(R.id.lv_admin_users);
        tvEmpty = findViewById(R.id.tv_empty_users);
        findViewById(R.id.btn_back_admin_users).setOnClickListener(v -> finish());

        // 4. Initialisation du SimpleCursorAdapter
        setupSimpleCursorAdapter();
    }

    /**
     * Configure et lie le SimpleCursorAdapter (Matrice §4 ligne 29 / Ch. 6 du cours)
     */
    private void setupSimpleCursorAdapter() {
        // 1. Obtention du Cursor brut contenant '_id', 'full_name', 'email', 'role', 'is_active'
        currentCursor = userDao.getAllUsersCursor();

        // 2. Colonnes sources du Cursor à afficher
        String[] fromColumns = new String[]{
                DatabaseHelper.COLUMN_USER_FULL_NAME,
                DatabaseHelper.COLUMN_USER_EMAIL,
                DatabaseHelper.COLUMN_USER_ROLE
        };

        // 3. Identifiants des TextView cibles dans item_admin_user.xml
        int[] toViews = new int[]{
                R.id.tv_user_name,
                R.id.tv_user_email,
                R.id.tv_user_role
        };

        // 4. Instanciation du SimpleCursorAdapter officiel Android
        cursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.item_admin_user,
                currentCursor,
                fromColumns,
                toViews,
                0 // flags : 0 pour désactiver l'auto-requête dépréciée
        );

        // 5. Utilisation d'un ViewBinder pour enrichir l'affichage du statut et câbler les boutons d'action
        cursorAdapter.setViewBinder((view, cursor, columnIndex) -> {
            // Personnalisation du badge de statut et boutons sur la vue racine
            View parent = (View) view.getParent();
            if (parent != null) {
                bindCustomActions(parent, cursor);
            }
            // Retourner false laisse le SimpleCursorAdapter appliquer le texte standard pour les autres champs
            return false;
        });

        lvUsers.setAdapter(cursorAdapter);
        updateEmptyState();
    }

    /**
     * Câble les actions d'activation/suspension et d'envoi de mot de passe par SMS
     */
    private void bindCustomActions(View itemView, Cursor cursor) {
        TextView tvStatus = itemView.findViewById(R.id.tv_user_status);
        Button btnToggle = itemView.findViewById(R.id.btn_toggle_status);
        Button btnSms = itemView.findViewById(R.id.btn_sms_reset);

        long userId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        String userEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL));
        String userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_FULL_NAME));
        int isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IS_ACTIVE));

        if (tvStatus != null) {
            if (isActive == 1) {
                tvStatus.setText("● Compte Actif");
                tvStatus.setTextColor(Color.parseColor("#1C5143"));
            } else {
                tvStatus.setText("● Compte Suspendu");
                tvStatus.setTextColor(Color.parseColor("#DC7770"));
            }
        }

        if (btnToggle != null) {
            btnToggle.setText(isActive == 1 ? "Suspendre" : "Activer");
            btnToggle.setOnClickListener(v -> toggleUserStatus(userId, userName, isActive == 1));
        }

        if (btnSms != null) {
            btnSms.setOnClickListener(v -> sendTemporaryPasswordSms(userId, userName, userEmail));
        }
    }

    /**
     * Modifie l'état d'un compte (Actif <-> Suspendu) et actualise le Cursor
     */
    private void toggleUserStatus(long userId, String userName, boolean currentlyActive) {
        boolean newStatus = !currentlyActive;
        boolean success = userDao.setUserActive(userId, newStatus);

        if (success) {
            String message = newStatus ? "Compte réactivé pour " + userName : "Compte suspendu pour " + userName;
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            refreshCursor();
        } else {
            Toast.makeText(this, "Erreur lors de la modification du statut.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Réinitialise le mot de passe et déclenche l'Intent implicite SMS (Matrice §4 ligne 12)
     */
    private void sendTemporaryPasswordSms(long userId, String userName, String email) {
        String newTempPassword = "Temp" + (100 + (int) (Math.random() * 900)) + "!";
        boolean updated = userDao.resetPassword(userId, newTempPassword);

        if (!updated) {
            Toast.makeText(this, "Échec de réinitialisation du mot de passe.", Toast.LENGTH_SHORT).show();
            return;
        }

        String smsBody = String.format(
                "Bonjour %s, votre mot de passe temporaire pour Masrouf est : %s (compte : %s). Veuillez le modifier dès votre connexion.",
                userName, newTempPassword, email
        );

        // Boîte de dialogue de confirmation avant l'envoi de l'intent SMS
        new AlertDialog.Builder(this)
                .setTitle("Mot de passe réinitialisé")
                .setMessage("Nouveau mot de passe temporaire : " + newTempPassword + "\n\nSouhaitez-vous l'envoyer par SMS à l'utilisateur ?")
                .setPositiveButton("Envoyer par SMS", (dialog, which) -> {
                    // EXIGENCE DU PROF : Intent implicite SMS (ACTION_SENDTO avec smsto:)
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                    smsIntent.setData(Uri.parse("smsto:")); // Destinataire libre ou à saisir
                    smsIntent.putExtra("sms_body", smsBody);

                    try {
                        startActivity(smsIntent);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur envoi SMS : " + e.getMessage(), e);
                        Toast.makeText(this, "Aucune application de SMS disponible.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Ignorer", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Rafraîchit le Cursor lié au SimpleCursorAdapter en fermant proprement l'ancien
     */
    private void refreshCursor() {
        Cursor newCursor = userDao.getAllUsersCursor();
        // changeCursor ferme automatiquement l'ancien curseur pour éviter les fuites de mémoire (CursorLeak)
        cursorAdapter.changeCursor(newCursor);
        currentCursor = newCursor;
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (currentCursor == null || currentCursor.getCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            lvUsers.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            lvUsers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libération finale des ressources SQLite du curseur
        if (currentCursor != null && !currentCursor.isClosed()) {
            currentCursor.close();
            currentCursor = null;
        }
    }
}

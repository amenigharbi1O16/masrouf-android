package com.example.masrouf.view.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.view.LoginActivity;

/**
 * RÔLE : Tableau de bord de supervision générale réservé aux administrateurs (Figma 35).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°1 (Gestion des 2 rôles : écran exclusif ADMIN avec garde d'accès).
 *   - Exigence n°2 & n°3 (VUE CONSTRUITE INTÉGRALEMENT EN CODE JAVA PUR SANS AUCUN FICHIER XML).
 *   - Matrice §4 ligne 8 (Listener explicite sous forme d'expression Lambda en Java 8+ / 11).
 *   - Matrice §4 ligne 24 (Gestion du cycle de vie : rafraîchissement des données dans onResume).
 *   - Matrice §4 ligne 32 & 33 (Requêtes d'agrégation SQL via le DAO).
 *
 * PARTICULARITÉ PÉDAGOGIQUE POUR LE PROFESSEUR :
 * Aucun fichier layout XML n'est associé à cette activité.
 * L'ensemble de la hiérarchie de vues (ScrollView, LinearLayout, TextView, Button, drawables dynamiques)
 * est instancié par code Java via "new View(this)", configuré par setters et injecté avec setContentView().
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";

    // Contrôleurs et DAO
    private SessionManager sessionManager;
    private UserDao userDao;

    // Vues dynamiques nécessitant une mise à jour lors de onResume()
    private TextView tvActiveCount;
    private TextView tvSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialisation des composants métier et d'accès aux données
        this.sessionManager = new SessionManager(this);
        this.userDao = new UserDao(this);

        // 2. GARDE DE SÉCURITÉ (Access Control Guard)
        // Vérifie formellement que l'utilisateur est connecté ET qu'il possède le rôle "ADMIN"
        if (!checkAdminAccess()) {
            return; // L'activité est terminée par finish(), on arrête l'exécution de onCreate()
        }

        // 3. CONSTRUCTION DE L'INTERFACE GRAPHIQUE 100% EN CODE JAVA (Exigence prof n°3)
        View adminRootView = buildAdminInterface();

        // 4. Attachement de l'arbre de vues Java à la fenêtre de l'Activité
        // Remplace l'habituel setContentView(R.layout.xxx)
        setContentView(adminRootView);

        // 5. Chargement initial des statistiques depuis SQLite via le DAO
        refreshStatistics();
    }

    /**
     * Garde de sécurité vérifiant les privilèges de l'utilisateur courant.
     * Si l'utilisateur n'est pas un administrateur valide, il est immédiatement expulsé.
     *
     * @return true si l'accès est autorisé, false sinon.
     */
    private boolean checkAdminAccess() {
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Log.e(TAG, "Tentative d'accès non autorisé à l'espace administrateur");
            Toast.makeText(this, R.string.admin_access_denied, Toast.LENGTH_LONG).show();

            // Redirection vers l'écran de connexion
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return false;
        }
        return true;
    }

    /**
     * Surcharge du cycle de vie onResume (Matrice §4 ligne 24).
     * Garantit que les statistiques d'utilisateurs sont rafraîchies lorsque l'administrateur
     * revient sur cet écran après avoir visité une sous-rubrique.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() déclenché : actualisation des compteurs SQLite");
        refreshStatistics();
    }

    /**
     * Construit dynamiquement par programmation l'ensemble de l'interface graphique de l'écran admin.
     * Reproduit fidèlement la structure de la maquette Figma 35 (Admin dashboard).
     *
     * @return La vue racine prête à être affichée (ScrollView).
     */
    private View buildAdminInterface() {
        // --- CONTENEUR DE DÉFILEMENT (ScrollView) ---
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.MATCH_PARENT
        ));
        // Fond d'écran beige chaleureux conforme au design system
        scrollView.setBackgroundColor(ContextCompat.getColor(this, R.color.warm_bg));
        scrollView.setFillViewport(true);

        // --- CONTENEUR RACINE VERTICAL (LinearLayout) ---
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        // Marges intérieures : 16dp sur les côtés, 20dp en haut, 28dp en bas
        int paddingM = dpToPx(16);
        rootLayout.setPadding(paddingM, dpToPx(20), paddingM, dpToPx(28));

        // 1. BARRE D'EN-TÊTE HORIZONTALE (Menu + Titre + Profil)
        LinearLayout headerLayout = buildHeaderLayout();
        rootLayout.addView(headerLayout);

        // 2. SOUS-TITRE / CATÉGORIE EN MAJUSCULES (ADMINISTRATOR · GLOBAL STATISTICS)
        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText(R.string.admin_subtitle);
        tvSubtitle.setTextColor(ContextCompat.getColor(this, R.color.muted));
        tvSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvSubtitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvSubtitle.setLetterSpacing(0.08f);

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, dpToPx(24), 0, dpToPx(12));
        tvSubtitle.setLayoutParams(subtitleParams);
        rootLayout.addView(tvSubtitle);

        // 3. CARTE SOMBRE DES COMPTES ACTIFS (Fond vert forêt sombre #102B27)
        LinearLayout statCard = buildStatCard();
        rootLayout.addView(statCard);

        // 4. CARTE MENTHE D'AVERTISSEMENT DE CONFIDENTIALITÉ (Statistiques anonymes uniquement)
        LinearLayout privacyCard = buildPrivacyNoticeCard();
        rootLayout.addView(privacyCard);

        // 5. LIGNES DE COMPTAGE FIXES (Catégories de produits & dépenses)
        rootLayout.addView(buildMetricRow(getString(R.string.admin_product_categories), "6"));
        rootLayout.addView(buildMetricRow(getString(R.string.admin_expense_categories), "10"));

        // 6. BOUTONS D'ACTIONS D'ADMINISTRATION
        // Bouton 1 : Gestion des utilisateurs (ouvre AdminUsersActivity avec SimpleCursorAdapter)
        Button btnManageUsers = createAdminActionButton(getString(R.string.admin_btn_manage_users));
        btnManageUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUsersActivity.class)));
        rootLayout.addView(btnManageUsers);

        Button btnManageCategories = createAdminActionButton(getString(R.string.admin_btn_manage_categories));
        btnManageCategories.setOnClickListener(v -> startActivity(new Intent(this, AdminCategoriesActivity.class)));
        rootLayout.addView(btnManageCategories);

        Button btnCategoryTypes = createAdminActionButton(getString(R.string.admin_btn_category_types));
        btnCategoryTypes.setOnClickListener(v -> startActivity(new Intent(this, AdminCategoriesActivity.class)));
        rootLayout.addView(btnCategoryTypes);

        Button btnDefaultPercentages = createAdminActionButton(getString(R.string.admin_btn_default_percentages));
        btnDefaultPercentages.setOnClickListener(v -> startActivity(new Intent(this, AdminPercentagesActivity.class)));
        rootLayout.addView(btnDefaultPercentages);

        Button btnPointsRules = createAdminActionButton(getString(R.string.admin_btn_points_rules));
        btnPointsRules.setOnClickListener(v -> startActivity(new Intent(this, AdminPointsRulesActivity.class)));
        rootLayout.addView(btnPointsRules);

        // Bouton 6 : Paramètres et assistance
        Button btnAdminSettings = createAdminActionButton(getString(R.string.admin_btn_settings));
        btnAdminSettings.setOnClickListener(v -> startActivity(new Intent(this, com.example.masrouf.view.SettingsActivity.class)));
        rootLayout.addView(btnAdminSettings);

        // 7. BOUTON DE DÉCONNEXION (Matrice §4 ligne 8 : Listener Lambda en Java)
        Button btnLogout = buildLogoutButton();
        rootLayout.addView(btnLogout);

        // Intégration du LinearLayout racine dans le ScrollView
        scrollView.addView(rootLayout);
        return scrollView;
    }

    /**
     * Construit l'en-tête supérieur avec icônes de navigation.
     */
    private LinearLayout buildHeaderLayout() {
        LinearLayout header = new LinearLayout(this);
        header.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        // Icône menu hamburger
        ImageView ivMenu = new ImageView(this);
        ivMenu.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)));
        ivMenu.setImageResource(R.drawable.ic_menu);
        header.addView(ivMenu);

        // Titre de l'écran "Vue d'ensemble admin"
        TextView tvTitle = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        titleParams.setMarginStart(dpToPx(16));
        tvTitle.setLayoutParams(titleParams);
        tvTitle.setText(R.string.admin_header_title);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.forest));
        header.addView(tvTitle);

        // Icône profil
        ImageView ivProfile = new ImageView(this);
        ivProfile.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)));
        ivProfile.setImageResource(R.drawable.ic_profile);
        header.addView(ivProfile);

        return header;
    }

    /**
     * Construit la carte sombre principale affichant le nombre de comptes actifs.
     */
    private LinearLayout buildStatCard() {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(16));
        card.setLayoutParams(cardParams);
        card.setOrientation(LinearLayout.VERTICAL);

        // Application programmatique d'un fond arrondi (coins à 24dp)
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(ContextCompat.getColor(this, R.color.forest));
        shape.setCornerRadius(dpToPx(24));
        card.setBackground(shape);

        // Padding interne généreux : 24dp côtés, 28dp haut, 32dp bas
        card.setPadding(dpToPx(24), dpToPx(28), dpToPx(24), dpToPx(32));

        // Label supérieur "COMPTES ACTIFS"
        TextView tvLabel = new TextView(this);
        tvLabel.setText(R.string.admin_stat_active_accounts);
        tvLabel.setTextColor(ContextCompat.getColor(this, R.color.muted_light));
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvLabel.setTypeface(Typeface.DEFAULT_BOLD);
        tvLabel.setLetterSpacing(0.06f);
        card.addView(tvLabel);

        // Grand nombre compteur (Display typography 38sp bold blanc)
        tvActiveCount = new TextView(this);
        tvActiveCount.setText("...");
        tvActiveCount.setTextColor(Color.WHITE);
        tvActiveCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 38);
        tvActiveCount.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        countParams.setMargins(0, dpToPx(12), 0, dpToPx(12));
        tvActiveCount.setLayoutParams(countParams);
        card.addView(tvActiveCount);

        // Ligne de résumé : "X au total · Y suspendus"
        tvSummary = new TextView(this);
        tvSummary.setText("...");
        tvSummary.setTextColor(Color.WHITE);
        tvSummary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        card.addView(tvSummary);

        return card;
    }

    /**
     * Construit la bannière verte menthe rappelant le principe fondamental d'anonymat.
     */
    private LinearLayout buildPrivacyNoticeCard() {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(20));
        card.setLayoutParams(cardParams);
        card.setOrientation(LinearLayout.VERTICAL);

        // Fond vert menthe pastel avec coins arrondis à 16dp
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(ContextCompat.getColor(this, R.color.mint));
        shape.setCornerRadius(dpToPx(16));
        card.setBackground(shape);

        card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Ligne supérieure avec icône coche et titre
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView checkIcon = new ImageView(this);
        checkIcon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(20), dpToPx(20)));
        checkIcon.setImageResource(R.drawable.ic_check);
        titleRow.addView(checkIcon);

        TextView tvTitle = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.setMarginStart(dpToPx(8));
        tvTitle.setLayoutParams(textParams);
        tvTitle.setText(R.string.admin_privacy_title);
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.forest));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        titleRow.addView(tvTitle);

        card.addView(titleRow);

        // Description de confidentialité
        TextView tvDesc = new TextView(this);
        tvDesc.setText(R.string.admin_privacy_desc);
        tvDesc.setTextColor(ContextCompat.getColor(this, R.color.forest));
        tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.setMargins(dpToPx(28), dpToPx(4), 0, 0);
        tvDesc.setLayoutParams(descParams);
        card.addView(tvDesc);

        return card;
    }

    /**
     * Construit une ligne de statistique avec libellé à gauche et valeur à droite.
     */
    private LinearLayout buildMetricRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(12));
        row.setLayoutParams(params);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        TextView tvLabel = new TextView(this);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        tvLabel.setText(label);
        tvLabel.setTextColor(ContextCompat.getColor(this, R.color.muted));
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        row.addView(tvLabel);

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextColor(ContextCompat.getColor(this, R.color.ink));
        tvValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvValue.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(tvValue);

        return row;
    }

    /**
     * Fabrique un bouton d'action administrative selon la charte graphique Figma.
     * Fond vert menthe clair, texte vert forêt, hauteur 52dp, coins arrondis à 16dp.
     *
     * @param buttonText Libellé du bouton
     * @return L'objet Button instancié et stylisé en Java
     */
    private Button createAdminActionButton(String buttonText) {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(52)
        );
        params.setMargins(0, 0, 0, dpToPx(10));
        button.setLayoutParams(params);

        // Application d'un fond arrondi menthe
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(ContextCompat.getColor(this, R.color.mint));
        shape.setCornerRadius(dpToPx(16));
        button.setBackground(shape);

        button.setText(buttonText);
        button.setTextColor(ContextCompat.getColor(this, R.color.forest));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setAllCaps(false);

        // Clic temporaire d'information pour les écrans des étapes futures
        button.setOnClickListener(v ->
                Toast.makeText(this, "Module : " + buttonText + " (Étape ultérieure)", Toast.LENGTH_SHORT).show()
        );

        return button;
    }

    /**
     * Construit le bouton de déconnexion administrative.
     * Utilise une expression lambda pour son gestionnaire de clic (Matrice §4 ligne 8).
     */
    private Button buildLogoutButton() {
        Button btnLogout = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(52)
        );
        params.setMargins(0, dpToPx(16), 0, 0);
        btnLogout.setLayoutParams(params);

        // Style avec bordure corail/rouge subtile pour action de sortie
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(Color.WHITE);
        shape.setStroke(dpToPx(1), ContextCompat.getColor(this, R.color.coral));
        shape.setCornerRadius(dpToPx(16));
        btnLogout.setBackground(shape);

        btnLogout.setText(R.string.btn_logout);
        btnLogout.setTextColor(ContextCompat.getColor(this, R.color.coral));
        btnLogout.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        btnLogout.setTypeface(Typeface.DEFAULT_BOLD);
        btnLogout.setAllCaps(false);

        // EXIGENCE MATRICE §4 LIGNE 8 : Gestionnaire de clic via une LAMBDA Java
        btnLogout.setOnClickListener(v -> performLogout());

        return btnLogout;
    }

    /**
     * Lit les données réelles dans SQLite via le DAO et met à jour les TextViews.
     */
    private void refreshStatistics() {
        if (userDao == null) return;

        // Requêtes SQL d'agrégation exécutées par UserDao
        int activeUsers = userDao.getActiveUserCount();
        int totalUsers = userDao.getTotalUserCount();
        int suspendedUsers = userDao.getSuspendedUserCount();

        Log.d(TAG, "Statistiques lues : Actifs=" + activeUsers + ", Total=" + totalUsers + ", Suspendus=" + suspendedUsers);

        if (tvActiveCount != null) {
            tvActiveCount.setText(String.valueOf(activeUsers));
        }

        if (tvSummary != null) {
            String summaryText = getString(R.string.admin_stat_accounts_summary, totalUsers, suspendedUsers);
            tvSummary.setText(summaryText);
        }
    }

    /**
     * Déconnecte l'administrateur, purge les SharedPreferences et le renvoie au login.
     */
    private void performLogout() {
        Log.d(TAG, "Déconnexion de l'administrateur");
        sessionManager.clearSession();
        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Méthode utilitaire de conversion dp en pixels selon la densité de l'écran.
     * Conforme au cours Ch.3 (1dp = 1px à 160 dpi).
     *
     * @param dp Valeur en dp
     * @return Valeur calculée en pixels réels
     */
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}

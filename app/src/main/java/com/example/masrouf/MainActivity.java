package com.example.masrouf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.util.NotificationHelper;
import com.example.masrouf.view.AddExpenseActivity;
import com.example.masrouf.view.AddPurchaseActivity;
import com.example.masrouf.view.AnalyticsActivity;
import com.example.masrouf.view.CalendarActivity;
import com.example.masrouf.view.CreateBudgetActivity;
import com.example.masrouf.view.LoginActivity;
import com.example.masrouf.view.ModifyBudgetActivity;
import com.example.masrouf.view.PointsAndAchievementsActivity;
import com.example.masrouf.view.ProductsActivity;
import com.example.masrouf.view.SettingsActivity;
import com.example.masrouf.view.UserManualActivity;
import com.example.masrouf.view.admin.AdminDashboardActivity;

import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran d'accueil principal (Figma 07) et Tiroir de Navigation Latéral (Figma 08).
 * COUCHE MVC : Vue (View).
 * EXIGENCES COUVERTES :
 *   - DrawerLayout natif Android : Clic sur les 3 lignes ouvre le tiroir latéral et affiche ses composants.
 *   - AUCUNE DONNÉE EN DUR : Catégories, montants et transactions sont chargés dynamiquement depuis SQLite.
 *   - Notifications automatiques : Alertes déclenchées dès que le budget atteint 80% ou est dépassé.
 *   - Responsive Design : Interface s'adaptant à toutes les tailles de téléphones et tablettes (portrait/paysage).
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_POST_NOTIFS = 101;

    // Composants du Tiroir de navigation (DrawerLayout)
    private DrawerLayout drawerLayout;
    private ImageView ivMainMenu;
    private TextView tvDrawerUserName;
    private TextView tvDrawerUserEmail;
    private TextView tvDrawerUserRole;
    private TextView btnDrawerAdmin;

    // Vues principales du tableau de bord
    private TextView tvMainGreeting;
    private TextView tvMainPeriodSubtitle;
    private TextView tvMainAvailableAmount;
    private TextView tvMainTotalBudgetLabel;
    private ProgressBar pbBudgetUsage;
    private TextView tvMainSpentLabel;
    private TextView tvMainPercentageLabel;
    private TextView tvMainDateRangeLabel;

    private LinearLayout layoutStatusBanner;
    private ImageView ivStatusIcon;
    private TextView tvStatusTitle;
    private TextView tvStatusDesc;

    // Conteneurs 100% dynamiques (Zéro donnée en dur)
    private LinearLayout layoutCategoriesContainer;
    private LinearLayout layoutTransactionsContainer;
    private TextView tvEmptyTransactions;

    private Button btnAddPurchase;
    private Button btnAddExpense;

    // DAO et contrôleurs de données
    private SessionManager sessionManager;
    private BudgetDao budgetDao;
    private TransactionDao transactionDao;

    // Drapeau pour éviter d'envoyer la notification d'alerte en boucle
    private boolean hasNotifiedWarningThisSession = false;
    private boolean hasNotifiedExceededThisSession = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initialisation de MainActivity");

        // 1. Initialisation des composants d'accès aux données
        sessionManager = new SessionManager(this);
        budgetDao = new BudgetDao(this);
        transactionDao = new TransactionDao(this);

        // 2. Gonflage de la mise en page
        setContentView(R.layout.activity_main);

        // 3. Liaison des vues
        initViews();

        // 4. Configuration du Tiroir de Navigation latéral (Figma 08)
        setupNavigationDrawer();

        // 5. Configuration des écouteurs de clics
        setupListeners();

        // 6. Demande de permission de notification (Android 13+ Tiramisu, API 33+)
        checkNotificationPermission();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        ivMainMenu = findViewById(R.id.ivMainMenu);

        tvMainGreeting = findViewById(R.id.tvMainGreeting);
        tvMainPeriodSubtitle = findViewById(R.id.tvMainPeriodSubtitle);
        tvMainAvailableAmount = findViewById(R.id.tvMainAvailableAmount);
        tvMainTotalBudgetLabel = findViewById(R.id.tvMainTotalBudgetLabel);
        pbBudgetUsage = findViewById(R.id.pbBudgetUsage);
        tvMainSpentLabel = findViewById(R.id.tvMainSpentLabel);
        tvMainPercentageLabel = findViewById(R.id.tvMainPercentageLabel);
        tvMainDateRangeLabel = findViewById(R.id.tvMainDateRangeLabel);

        layoutStatusBanner = findViewById(R.id.layoutStatusBanner);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusDesc = findViewById(R.id.tvStatusDesc);

        layoutCategoriesContainer = findViewById(R.id.layoutCategoriesContainer);
        layoutTransactionsContainer = findViewById(R.id.layoutTransactionsContainer);
        tvEmptyTransactions = findViewById(R.id.tvEmptyTransactions);

        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnAddPurchase = findViewById(R.id.btnAddPurchase);

        // Vues internes au Tiroir latéral
        tvDrawerUserName = findViewById(R.id.tvDrawerUserName);
        tvDrawerUserEmail = findViewById(R.id.tvDrawerUserEmail);
        tvDrawerUserRole = findViewById(R.id.tvDrawerUserRole);
        btnDrawerAdmin = findViewById(R.id.btnDrawerAdmin);
    }

    /**
     * Configure le tiroir de navigation latéral (Figma 08 - Navigation Drawer).
     * Les 3 lignes en haut à gauche ouvrent ce menu.
     */
    private void setupNavigationDrawer() {
        // Affichage des informations réelles de l'utilisateur dans le drawer
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String role = sessionManager.getUserRole();

        if (tvDrawerUserName != null) tvDrawerUserName.setText(name != null && !name.isEmpty() ? name : "Utilisateur");
        if (tvDrawerUserEmail != null) tvDrawerUserEmail.setText(email != null && !email.isEmpty() ? email : "email@gmail.com");

        if (tvDrawerUserRole != null) {
            if (sessionManager.isAdmin()) {
                tvDrawerUserRole.setText(R.string.drawer_role_admin);
            } else {
                tvDrawerUserRole.setText(R.string.drawer_role_user);
            }
        }

        // Affichage du bouton de supervision uniquement si le compte est ADMIN
        if (btnDrawerAdmin != null) {
            btnDrawerAdmin.setVisibility(sessionManager.isAdmin() ? View.VISIBLE : View.GONE);
            btnDrawerAdmin.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
            });
        }

        // Câblage des éléments de navigation du drawer
        findViewById(R.id.btnDrawerHome).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        findViewById(R.id.btnDrawerProducts).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, ProductsActivity.class));
        });

        findViewById(R.id.btnDrawerCalendar).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, CalendarActivity.class));
        });

        findViewById(R.id.btnDrawerAnalytics).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, AnalyticsActivity.class));
        });

        findViewById(R.id.btnDrawerBudget).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Budget currentBudget = budgetDao.getCurrentBudget(sessionManager.getUserId());
            if (currentBudget != null) {
                startActivity(new Intent(MainActivity.this, ModifyBudgetActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, CreateBudgetActivity.class));
            }
        });

        findViewById(R.id.btnDrawerPoints).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, PointsAndAchievementsActivity.class));
        });

        findViewById(R.id.btnDrawerManual).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, UserManualActivity.class));
        });

        findViewById(R.id.btnDrawerSettings).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        findViewById(R.id.btnDrawerLogout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            sessionManager.clearSession();
            Toast.makeText(MainActivity.this, "Session terminée", Toast.LENGTH_SHORT).show();
            Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
        });
    }

    private void setupListeners() {
        // Clic sur les trois lignes en haut à gauche : ouverture immédiate du tiroir latéral
        ivMainMenu.setOnClickListener(v -> {
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        // Clic sur l'icône profil : ouvre les Paramètres
        findViewById(R.id.ivMainProfile).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class))
        );

        // Bouton primaire "+ Add purchase" (Figma 16)
        btnAddPurchase.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPurchaseActivity.class);
            startActivity(intent);
        });

        // Bouton secondaire "+ Quick expense" (Figma 17)
        if (btnAddExpense != null) {
            btnAddExpense.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * Listener implicite via attribut XML 'android:onClick="onQuickExpenseClicked"' (Matrice §4 ligne 4).
     */
    public void onQuickExpenseClicked(View v) {
        Log.d(TAG, "Listener implicite déclenché via android:onClick");
        Intent intent = new Intent(this, AddExpenseActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() exécuté : actualisation complète des données depuis SQLite");
        loadDashboardData();
    }

    /**
     * Lit les données réelles dans SQLite et peuple les conteneurs dynamiques (AUCUNE DONNÉE EN DUR).
     */
    private void loadDashboardData() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) userId = 1;

        // Salutation personnalisée dynamique
        String fullName = sessionManager.getUserName();
        String firstName = (fullName != null && !fullName.isEmpty()) ? fullName.split(" ")[0] : "Utilisateur";
        tvMainGreeting.setText(String.format(getString(R.string.home_greeting_format), firstName));

        // 1. Récupération du budget actif depuis SQLite
        Budget currentBudget = budgetDao.getCurrentBudget(userId);
        if (currentBudget != null) {
            double totalBudget = currentBudget.getTotalAmount();
            double totalSpent = budgetDao.getTotalSpent(currentBudget.getId());
            double available = totalBudget - totalSpent;
            double percentage = totalBudget > 0 ? (totalSpent / totalBudget) * 100.0 : 0.0;

            // Affichage des montants formatés
            tvMainAvailableAmount.setText(String.format(Locale.US, "%.3f DT", Math.max(0, available)));
            tvMainSpentLabel.setText(String.format(Locale.US, getString(R.string.home_spent_format), String.format(Locale.US, "%.3f DT", totalSpent)));
            tvMainPercentageLabel.setText(String.format(Locale.US, getString(R.string.home_used_pct_format), percentage));
            tvMainTotalBudgetLabel.setText(String.format(Locale.US, getString(R.string.home_budget_format), String.format(Locale.US, "%.3f DT", totalBudget)));
            pbBudgetUsage.setProgress((int) Math.min(100, Math.round(percentage)));

            tvMainPeriodSubtitle.setText(String.format("%s · %s – %s", currentBudget.getStatus(), currentBudget.getStartDate(), currentBudget.getEndDate()));
            tvMainDateRangeLabel.setText(currentBudget.getStartDate() + " – " + currentBudget.getEndDate());

            // 2. Gestion dynamique du bandeau de statut et des NOTIFICATIONS automatiques
            GradientDrawable bannerShape = new GradientDrawable();
            bannerShape.setShape(GradientDrawable.RECTANGLE);
            bannerShape.setCornerRadius(dpToPx(14));

            if (percentage >= 100.0) {
                // Seuil critique : Budget dépassé (> 100%)
                bannerShape.setColor(ContextCompat.getColor(this, R.color.coral_light));
                layoutStatusBanner.setBackground(bannerShape);
                tvStatusTitle.setText(R.string.status_exceeded_title);
                tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.coral));
                tvStatusDesc.setText(String.format(getString(R.string.status_exceeded_desc), String.format(Locale.US, "%.3f DT", Math.abs(available))));
                tvStatusDesc.setTextColor(ContextCompat.getColor(this, R.color.coral));
                pbBudgetUsage.setProgressTintList(ContextCompat.getColorStateList(this, R.color.coral));

                // Émission de notification d'alerte critique
                if (!hasNotifiedExceededThisSession) {
                    NotificationHelper.showBudgetAlert(
                            this,
                            getString(R.string.notif_budget_exceeded_title),
                            String.format(getString(R.string.notif_budget_exceeded_msg), String.format(Locale.US, "%.3f DT", Math.abs(available))),
                            true
                    );
                    hasNotifiedExceededThisSession = true;
                }
            } else if (percentage >= 80.0) {
                // Seuil d'avertissement : 80% atteint
                bannerShape.setColor(Color.parseColor("#FFF5E5"));
                layoutStatusBanner.setBackground(bannerShape);
                tvStatusTitle.setText(R.string.status_warning_title);
                tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.gold));
                tvStatusDesc.setText(String.format(getString(R.string.status_warning_desc), String.format(Locale.US, "%.3f DT", available)));
                tvStatusDesc.setTextColor(ContextCompat.getColor(this, R.color.ink));
                pbBudgetUsage.setProgressTintList(ContextCompat.getColorStateList(this, R.color.gold));

                // Émission de notification d'avertissement
                if (!hasNotifiedWarningThisSession) {
                    NotificationHelper.showBudgetAlert(
                            this,
                            getString(R.string.notif_budget_warning_title),
                            String.format(getString(R.string.notif_budget_warning_msg), percentage, String.format(Locale.US, "%.3f DT", available)),
                            false
                    );
                    hasNotifiedWarningThisSession = true;
                }
            } else {
                // Statut sain (< 80%)
                bannerShape.setColor(ContextCompat.getColor(this, R.color.mint));
                layoutStatusBanner.setBackground(bannerShape);
                tvStatusTitle.setText(R.string.status_healthy_title);
                tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.forest));
                tvStatusDesc.setText(String.format(getString(R.string.status_healthy_desc), String.format(Locale.US, "%.3f DT", available)));
                tvStatusDesc.setTextColor(ContextCompat.getColor(this, R.color.forest));
                pbBudgetUsage.setProgressTintList(ContextCompat.getColorStateList(this, R.color.forest));
            }

            // 3. PEUPLEMENT 100% DYNAMIQUE DES CATÉGORIES DEPUIS SQLITE
            populateCategoriesDynamically(currentBudget.getId());

        } else {
            // Aucun budget défini
            tvMainAvailableAmount.setText("0.000 DT");
            tvMainPeriodSubtitle.setText(R.string.home_no_budget);
            tvStatusTitle.setText(R.string.home_no_budget);
            tvStatusDesc.setText("Cliquez sur le menu pour créer votre premier budget mensuel.");
        }

        // 4. PEUPLEMENT 100% DYNAMIQUE DES DERNIÈRES TRANSACTIONS DEPUIS SQLITE
        populateRecentTransactionsDynamically(userId);
    }

    /**
     * Construit dynamiquement les cartes de catégories sans aucune donnée codée en dur
     */
    private void populateCategoriesDynamically(long budgetId) {
        layoutCategoriesContainer.removeAllViews();
        List<BudgetAllocation> allocations = budgetDao.getAllocationsForBudget(budgetId);

        if (allocations == null || allocations.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Aucune catégorie allouée.");
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.muted));
            layoutCategoriesContainer.addView(tvEmpty);
            return;
        }

        for (BudgetAllocation a : allocations) {
            LinearLayout card = new LinearLayout(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, dpToPx(8));
            card.setLayoutParams(cardParams);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
            card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_input_card));

            // Ligne Titre catégorie + Ratio dépenses/allocation
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvName = new TextView(this);
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            tvName.setText(a.getCategoryName());
            tvName.setTextColor(ContextCompat.getColor(this, R.color.forest));
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvName.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(tvName);

            TextView tvRatio = new TextView(this);
            tvRatio.setText(String.format(Locale.US, "%.3f / %.3f DT", a.getSpentAmount(), a.getAllocatedAmount()));
            tvRatio.setTextColor(ContextCompat.getColor(this, R.color.muted));
            tvRatio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            row.addView(tvRatio);

            card.addView(row);

            // Barre de progression horizontale par catégorie
            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(5)
            );
            pbParams.setMargins(0, dpToPx(8), 0, 0);
            pb.setLayoutParams(pbParams);
            pb.setMax(100);
            int pct = a.getAllocatedAmount() > 0 ? (int) Math.min(100, Math.round((a.getSpentAmount() / a.getAllocatedAmount()) * 100)) : 0;
            pb.setProgress(pct);

            if (pct >= 100) {
                pb.setProgressTintList(ContextCompat.getColorStateList(this, R.color.coral));
            } else if (pct >= 80) {
                pb.setProgressTintList(ContextCompat.getColorStateList(this, R.color.gold));
            } else {
                pb.setProgressTintList(ContextCompat.getColorStateList(this, R.color.forest));
            }
            card.addView(pb);

            layoutCategoriesContainer.addView(card);
        }
    }

    /**
     * Construit dynamiquement la liste des achats récents depuis SQLite sans aucune donnée en dur
     */
    private void populateRecentTransactionsDynamically(long userId) {
        layoutTransactionsContainer.removeAllViews();
        List<Transaction> transactions = transactionDao.getRecentTransactions(userId, 8);

        if (transactions == null || transactions.isEmpty()) {
            tvEmptyTransactions.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyTransactions.setVisibility(View.GONE);

        for (Transaction t : transactions) {
            LinearLayout item = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, dpToPx(8));
            item.setLayoutParams(params);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
            item.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_input_card));

            // Icône feuille
            ImageView ivIcon = new ImageView(this);
            ivIcon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(22), dpToPx(22)));
            ivIcon.setImageResource(R.drawable.ic_leaf);
            item.addView(ivIcon);

            // Colonne Titre + Magasin + Date
            LinearLayout textCol = new LinearLayout(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textParams.setMarginStart(dpToPx(12));
            textCol.setLayoutParams(textParams);
            textCol.setOrientation(LinearLayout.VERTICAL);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(t.getTitle());
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.forest));
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
            textCol.addView(tvTitle);

            TextView tvSub = new TextView(this);
            tvSub.setText((t.getStore() != null ? t.getStore() : "Magasin") + " · " + (t.getDate() != null ? t.getDate() : ""));
            tvSub.setTextColor(ContextCompat.getColor(this, R.color.muted));
            tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            textCol.addView(tvSub);

            item.addView(textCol);

            // Montant débité
            TextView tvAmount = new TextView(this);
            tvAmount.setText(String.format(Locale.US, "-%.3f DT", t.getAmount()));
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.forest));
            tvAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvAmount.setTypeface(Typeface.DEFAULT_BOLD);
            item.addView(tvAmount);

            layoutTransactionsContainer.addView(item);
        }
    }

    /**
     * Contrôle et demande la permission de notification pour Android 13+ (API 33+)
     */
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        PERMISSION_REQUEST_POST_NOTIFS
                );
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Si le tiroir latéral est ouvert, la touche retour le ferme au lieu de quitter l'activité
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}

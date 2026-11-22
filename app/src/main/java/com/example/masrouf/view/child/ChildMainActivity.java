package com.example.masrouf.view.child;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.ChildPermission;
import com.example.masrouf.model.MoneyRequest;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;

import com.example.masrouf.view.AddExpenseActivity;
import com.example.masrouf.view.AddPurchaseActivity;
import com.example.masrouf.view.AnalyticsActivity;
import com.example.masrouf.view.CalendarActivity;
import com.example.masrouf.view.LoginActivity;
import com.example.masrouf.view.PointsAndAchievementsActivity;
import com.example.masrouf.view.ProductsActivity;
import com.example.masrouf.view.SettingsActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran d'accueil principal dédié au Compte Enfant (ChildDashboard).
 * COUCHE MVC : Vue (View).
 * Exigences de l'Espace Enfant :
 *  - Affichage du solde disponible calculé : allocations reçues - achats - dépenses.
 *  - Badges "Reçu ce mois" et "Dépensé".
 *  - Modal de demande d'argent au parent avec motif.
 *  - Demandes en cours avec 3 états explicites (Pending, Approved, Rejected).
 *  - Actions rapides "Nouvel achat" et "Nouvelle dépense".
 *  - Isolation stricte des données (aucun accès aux budgets globaux ou aux comptes frères/sœurs).
 *  - Tiroir de navigation latéral 6 sections : Accueil, Produits, Calendrier, Analyses, Points, Réglages.
 */
public class ChildMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayoutChild;
    private ImageView ivChildMenu;
    private TextView tvChildGreeting;
    private ImageView ivChildProfile;

    private TextView tvChildBalanceAmount;
    private TextView tvChildReceivedBadge;
    private TextView tvChildMonthSpent;
    private Button btnChildRequestMoney;
    private Button btnChildAddPurchase;
    private Button btnChildAddExpense;

    private LinearLayout layoutChildRequestsList;
    private LinearLayout layoutChildExpensesList;

    // Éléments du Tiroir de Navigation Enfant (6 Sections)
    private TextView tvDrawerChildName;
    private LinearLayout btnNavChildHome;
    private LinearLayout btnNavChildProducts;
    private LinearLayout btnNavChildCalendar;
    private LinearLayout btnNavChildAnalytics;
    private LinearLayout btnNavChildPoints;
    private LinearLayout btnNavChildSettings;
    private TextView btnDrawerChildLangAr;
    private TextView btnDrawerChildLangFr;
    private TextView btnDrawerChildLangEn;
    private LinearLayout btnDrawerChildLogout;

    private SessionManager sessionManager;
    private FamilyDao familyDao;
    private UserDao userDao;

    private long childId = -1;
    private User currentChild;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_main);

        sessionManager = new SessionManager(this);
        familyDao = new FamilyDao(this);
        userDao = new UserDao(this);

        childId = sessionManager.getUserId();

        initViews();
        setupNavigationDrawer();
        setupListeners();
        loadChildData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildData();
    }

    private void initViews() {
        drawerLayoutChild = findViewById(R.id.drawerLayoutChild);
        ivChildMenu = findViewById(R.id.ivChildMenu);
        tvChildGreeting = findViewById(R.id.tvChildGreeting);
        ivChildProfile = findViewById(R.id.ivChildProfile);

        tvChildBalanceAmount = findViewById(R.id.tvChildBalanceAmount);
        tvChildReceivedBadge = findViewById(R.id.tvChildReceivedBadge);
        tvChildMonthSpent = findViewById(R.id.tvChildMonthSpent);
        btnChildRequestMoney = findViewById(R.id.btnChildRequestMoney);
        btnChildAddPurchase = findViewById(R.id.btnChildAddPurchase);
        btnChildAddExpense = findViewById(R.id.btnChildAddExpense);

        layoutChildRequestsList = findViewById(R.id.layoutChildRequestsList);
        layoutChildExpensesList = findViewById(R.id.layoutChildExpensesList);

        tvDrawerChildName = findViewById(R.id.tvDrawerChildName);
        btnNavChildHome = findViewById(R.id.btnNavChildHome);
        btnNavChildProducts = findViewById(R.id.btnNavChildProducts);
        btnNavChildCalendar = findViewById(R.id.btnNavChildCalendar);
        btnNavChildAnalytics = findViewById(R.id.btnNavChildAnalytics);
        btnNavChildPoints = findViewById(R.id.btnNavChildPoints);
        btnNavChildSettings = findViewById(R.id.btnNavChildSettings);

        btnDrawerChildLangAr = findViewById(R.id.btnDrawerChildLangAr);
        btnDrawerChildLangFr = findViewById(R.id.btnDrawerChildLangFr);
        btnDrawerChildLangEn = findViewById(R.id.btnDrawerChildLangEn);
        btnDrawerChildLogout = findViewById(R.id.btnDrawerChildLogout);

        if (sessionManager.getUserName() != null && !sessionManager.getUserName().isEmpty()) {
            tvChildGreeting.setText("Bonjour, " + sessionManager.getUserName());
            tvDrawerChildName.setText(sessionManager.getUserName());
        }
    }

    private void setupNavigationDrawer() {
        ivChildMenu.setOnClickListener(v -> {
            if (drawerLayoutChild.isDrawerOpen(GravityCompat.START)) {
                drawerLayoutChild.closeDrawer(GravityCompat.START);
            } else {
                drawerLayoutChild.openDrawer(GravityCompat.START);
            }
        });

        // 1. Accueil
        btnNavChildHome.setOnClickListener(v -> drawerLayoutChild.closeDrawer(GravityCompat.START));

        // 2. Produits
        if (btnNavChildProducts != null) {
            btnNavChildProducts.setOnClickListener(v -> {
                drawerLayoutChild.closeDrawer(GravityCompat.START);
                startActivity(new Intent(ChildMainActivity.this, ProductsActivity.class));
            });
        }

        // 3. Calendrier
        if (btnNavChildCalendar != null) {
            btnNavChildCalendar.setOnClickListener(v -> {
                drawerLayoutChild.closeDrawer(GravityCompat.START);
                startActivity(new Intent(ChildMainActivity.this, CalendarActivity.class));
            });
        }

        // 4. Analyses
        if (btnNavChildAnalytics != null) {
            btnNavChildAnalytics.setOnClickListener(v -> {
                drawerLayoutChild.closeDrawer(GravityCompat.START);
                startActivity(new Intent(ChildMainActivity.this, AnalyticsActivity.class));
            });
        }

        // 5. Points
        if (btnNavChildPoints != null) {
            btnNavChildPoints.setOnClickListener(v -> {
                drawerLayoutChild.closeDrawer(GravityCompat.START);
                startActivity(new Intent(ChildMainActivity.this, PointsAndAchievementsActivity.class));
            });
        }

        // 6. Réglages
        if (btnNavChildSettings != null) {
            btnNavChildSettings.setOnClickListener(v -> {
                drawerLayoutChild.closeDrawer(GravityCompat.START);
                startActivity(new Intent(ChildMainActivity.this, SettingsActivity.class));
            });
        }

        // Déconnexion
        btnDrawerChildLogout.setOnClickListener(v -> {
            drawerLayoutChild.closeDrawer(GravityCompat.START);
            sessionManager.clearSession();
            Intent intent = new Intent(ChildMainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        updateDrawerLanguage(sessionManager.getLanguage());
        if (btnDrawerChildLangAr != null) btnDrawerChildLangAr.setOnClickListener(v -> changeAppLanguage("ar"));
        if (btnDrawerChildLangFr != null) btnDrawerChildLangFr.setOnClickListener(v -> changeAppLanguage("fr"));
        if (btnDrawerChildLangEn != null) btnDrawerChildLangEn.setOnClickListener(v -> changeAppLanguage("en"));
    }

    private void updateDrawerLanguage(String lang) {
        if (btnDrawerChildLangAr == null) return;
        btnDrawerChildLangAr.setBackground(null);
        btnDrawerChildLangFr.setBackground(null);
        btnDrawerChildLangEn.setBackground(null);
        btnDrawerChildLangAr.setTextColor(Color.parseColor("#A0C8BC"));
        btnDrawerChildLangFr.setTextColor(Color.parseColor("#A0C8BC"));
        btnDrawerChildLangEn.setTextColor(Color.parseColor("#A0C8BC"));

        if ("ar".equalsIgnoreCase(lang)) {
            btnDrawerChildLangAr.setBackgroundResource(R.drawable.bg_pill_active);
            btnDrawerChildLangAr.setTextColor(Color.WHITE);
        } else if ("en".equalsIgnoreCase(lang)) {
            btnDrawerChildLangEn.setBackgroundResource(R.drawable.bg_pill_active);
            btnDrawerChildLangEn.setTextColor(Color.WHITE);
        } else {
            btnDrawerChildLangFr.setBackgroundResource(R.drawable.bg_pill_active);
            btnDrawerChildLangFr.setTextColor(Color.WHITE);
        }
    }

    private void changeAppLanguage(String code) {
        sessionManager.saveLanguage(code);
        LocaleHelper.setLocale(this, code);
        recreate();
    }

    private void setupListeners() {
        btnChildRequestMoney.setOnClickListener(v -> showRequestMoneyDialog());
        if (btnChildAddPurchase != null) {
            btnChildAddPurchase.setOnClickListener(v -> startActivity(new Intent(ChildMainActivity.this, AddPurchaseActivity.class)));
        }
        btnChildAddExpense.setOnClickListener(v -> startActivity(new Intent(ChildMainActivity.this, AddExpenseActivity.class)));
        ivChildProfile.setOnClickListener(v -> startActivity(new Intent(ChildMainActivity.this, SettingsActivity.class)));
    }

    private void loadChildData() {
        currentChild = userDao.getUserById(childId);
        if (currentChild == null) return;

        // Contrôle du statut actif (exigence n°1 & n°8 : un enfant suspendu est bloqué immédiatement)
        if (!currentChild.isActive()) {
            sessionManager.clearSession();
            Toast.makeText(this, "Compte suspendu par le parent. Accès refusé.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        tvChildGreeting.setText("Bonjour, " + currentChild.getFullName());
        tvDrawerChildName.setText(currentChild.getFullName());

        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());

        // Calculs réels : Solde = Allocations reçues - Dépenses totales
        long receivedMillimes = familyDao.getChildTotalAllowancesMillimes(childId, currentMonth);
        long monthlySpentMillimes = familyDao.getChildMonthlySpendingMillimes(childId, currentMonth);

        // Solde disponible dynamique
        long availableBalanceMillimes = currentChild.getBalanceMillimes();
        tvChildBalanceAmount.setText(MillimesUtil.formatMillimes(availableBalanceMillimes));

        if (tvChildReceivedBadge != null) {
            tvChildReceivedBadge.setText(MillimesUtil.formatMillimes(receivedMillimes));
        }
        tvChildMonthSpent.setText(MillimesUtil.formatMillimes(monthlySpentMillimes));

        renderMyRequests();
        renderMyExpenses();
    }

    private void renderMyRequests() {
        layoutChildRequestsList.removeAllViews();
        List<MoneyRequest> list = familyDao.getMoneyRequestsByChild(childId);

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucune demande d'argent effectuée.");
            empty.setTextColor(Color.parseColor("#6B7C74"));
            empty.setPadding(0, 16, 0, 16);
            layoutChildRequestsList.addView(empty);
            return;
        }

        for (MoneyRequest req : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_input_card);
            card.setPadding(32, 24, 32, 24);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 14;
            card.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvReason = new TextView(this);
            tvReason.setText(req.getReason());
            tvReason.setTextSize(15);
            tvReason.setTextColor(Color.parseColor("#102B27"));
            tvReason.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvReason.setLayoutParams(rLp);
            topRow.addView(tvReason);

            TextView tvAmt = new TextView(this);
            tvAmt.setText(MillimesUtil.formatMillimes(req.getAmountMillimes()));
            tvAmt.setTextSize(15);
            tvAmt.setTextColor(Color.parseColor("#102B27"));
            tvAmt.setTypeface(null, Typeface.BOLD);
            topRow.addView(tvAmt);

            card.addView(topRow);

            LinearLayout statusRow = new LinearLayout(this);
            statusRow.setOrientation(LinearLayout.HORIZONTAL);
            statusRow.setGravity(Gravity.CENTER_VERTICAL);
            statusRow.setPadding(0, 10, 0, 0);

            TextView tvDate = new TextView(this);
            tvDate.setText(req.getRequestDate());
            tvDate.setTextSize(12);
            tvDate.setTextColor(Color.parseColor("#6B7C74"));
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvDate.setLayoutParams(dLp);
            statusRow.addView(tvDate);

            // Badge de statut (3 états exacts avec couleurs et icônes spécifiées)
            TextView tvStatus = new TextView(this);
            tvStatus.setTextSize(12);
            tvStatus.setTypeface(null, Typeface.BOLD);
            tvStatus.setPadding(16, 6, 16, 6);

            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setCornerRadius(16);

            if (req.isApproved()) {
                // Approved: mint #D1FAE5, text #065F46
                statusBg.setColor(Color.parseColor("#D1FAE5"));
                tvStatus.setBackground(statusBg);
                tvStatus.setText("✓ Approuvée");
                tvStatus.setTextColor(Color.parseColor("#065F46"));
            } else if (req.isRejected()) {
                // Rejected: coral #FEE2E2, text #991B1B
                statusBg.setColor(Color.parseColor("#FEE2E2"));
                tvStatus.setBackground(statusBg);
                tvStatus.setText("✕ Refusée");
                tvStatus.setTextColor(Color.parseColor("#991B1B"));
            } else {
                // Pending: amber #FEF3C7, text #B45309
                statusBg.setColor(Color.parseColor("#FEF3C7"));
                tvStatus.setBackground(statusBg);
                tvStatus.setText("⏳ En attente du parent");
                tvStatus.setTextColor(Color.parseColor("#B45309"));
            }
            statusRow.addView(tvStatus);
            card.addView(statusRow);

            if (req.getDecisionNote() != null && !req.getDecisionNote().isEmpty()) {
                TextView tvNote = new TextView(this);
                tvNote.setText("Note du parent : " + req.getDecisionNote());
                tvNote.setTextSize(12);
                tvNote.setTextColor(req.isApproved() ? Color.parseColor("#065F46") : Color.parseColor("#991B1B"));
                tvNote.setPadding(0, 8, 0, 0);
                card.addView(tvNote);
            }

            layoutChildRequestsList.addView(card);
        }
    }

    private void renderMyExpenses() {
        layoutChildExpensesList.removeAllViews();
        List<Transaction> list = familyDao.getChildTransactions(childId);

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucune dépense enregistrée pour le moment.");
            empty.setTextColor(Color.parseColor("#6B7C74"));
            empty.setPadding(0, 16, 0, 16);
            layoutChildExpensesList.addView(empty);
            return;
        }

        int count = Math.min(list.size(), 5);
        for (int i = 0; i < count; i++) {
            Transaction t = list.get(i);
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setBackgroundResource(R.drawable.bg_input_card);
            item.setPadding(32, 24, 32, 24);
            item.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 14;
            item.setLayoutParams(lp);

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(64, 64));
            iv.setBackgroundResource(R.drawable.bg_circle_mint);
            iv.setImageResource(R.drawable.ic_receipt);
            iv.setPadding(14, 14, 14, 14);
            iv.setColorFilter(Color.parseColor("#102B27"));
            item.addView(iv);

            LinearLayout details = new LinearLayout(this);
            details.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            dLp.setMargins(20, 0, 0, 0);
            details.setLayoutParams(dLp);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(t.getTitle());
            tvTitle.setTextSize(14);
            tvTitle.setTextColor(Color.parseColor("#102B27"));
            tvTitle.setTypeface(null, Typeface.BOLD);
            details.addView(tvTitle);

            TextView tvSub = new TextView(this);
            tvSub.setText(t.getDate() + (t.getStore() != null && !t.getStore().isEmpty() ? " · " + t.getStore() : ""));
            tvSub.setTextSize(12);
            tvSub.setTextColor(Color.parseColor("#6B7C74"));
            details.addView(tvSub);

            item.addView(details);

            TextView tvPrice = new TextView(this);
            tvPrice.setText("-" + MillimesUtil.formatMillimes(t.getAmountMillimes()));
            tvPrice.setTextSize(15);
            tvPrice.setTextColor(Color.parseColor("#102B27"));
            tvPrice.setTypeface(null, Typeface.BOLD);
            item.addView(tvPrice);

            layoutChildExpensesList.addView(item);
        }
    }

    private void showRequestMoneyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Demander de l'argent à papa/maman");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        EditText etAmount = new EditText(this);
        etAmount.setHint("Montant en DT (ex: 15.000)");
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        EditText etReason = new EditText(this);
        etReason.setHint("Motif (ex: Livre de sciences, sortie...)");
        layout.addView(etReason);

        builder.setView(layout);
        builder.setPositiveButton("Envoyer la demande", (dialog, which) -> {
            String amtStr = etAmount.getText().toString().trim();
            String reason = etReason.getText().toString().trim();

            if (amtStr.isEmpty() || reason.isEmpty()) {
                Toast.makeText(this, "Veuillez renseigner le montant et le motif.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long millimes = MillimesUtil.dtToMillimes(Double.parseDouble(amtStr));
                long parentId = currentChild != null && currentChild.getParentId() > 0 ? currentChild.getParentId() : 1;
                long famId = currentChild != null && currentChild.getFamilyId() > 0 ? currentChild.getFamilyId() : 1;

                long reqId = familyDao.createMoneyRequest(famId, childId,
                        currentChild != null ? currentChild.getFullName() : "Enfant",
                        parentId, millimes, reason);

                if (reqId > 0) {
                    Toast.makeText(this, "Demande envoyée à votre parent !", Toast.LENGTH_SHORT).show();
                    loadChildData();
                } else {
                    Toast.makeText(this, "Erreur lors de l'envoi de la demande.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Montant invalide.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
}

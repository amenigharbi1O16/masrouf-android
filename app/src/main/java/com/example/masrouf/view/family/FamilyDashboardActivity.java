package com.example.masrouf.view.family;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.ExpenseDao;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;
import com.example.masrouf.view.AddExpenseActivity;
import com.example.masrouf.view.AddPurchaseActivity;
import com.example.masrouf.view.CreateBudgetActivity;
import com.example.masrouf.view.CustomCategoryModal;
import com.example.masrouf.view.NotificationsActivity;
import com.example.masrouf.ProductsActivity;
import com.example.masrouf.view.VoiceInputDialog;
import com.example.masrouf.view.admin.AdminDrawerHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Tableau de bord principal Parent - Espace Familial.
 * 
 * Fonctionnalités :
 * - Header avec bouton Hamburger 40x40, greeting personnalisé et cloche de notification.
 * - AdminDrawer latéral complet (7 rubriques, profil, 3 langues, déconnexion).
 * - Carte "Budget du Mois" avec statut réactif selon l'existence d'un budget.
 * - Si budget === 0 : Carte d'incitation, bouton "+ Ajouter une catégorie", pas d'alertes 80/100%.
 * - Si budget > 0 : Reste à vivre, jauge de progression, bannière de santé (Sain, 80%, Dépassé).
 * - Cartes des envelopes par catégorie avec pastille DÉPASSÉ DE X DT.
 * - Section "Mes Enfants" avec bouton "Donner" (allocation depuis l'enveloppe Enfant).
 * - Actions rapides et bouton micro flottant centré pour saisie vocale bilingue.
 */
import com.example.masrouf.BaseActivity;

public class FamilyDashboardActivity extends BaseActivity {

    private DrawerLayout drawerLayoutFamily;
    private ImageView ivFamilyMenu;
    private TextView tvFamilyGreeting;
    private TextView tvFamilyNameSub;
    private FrameLayout frameNotifFamily;

    private LinearLayout cardBudgetMonth;
    private TextView tvBudgetPeriod;
    private TextView tvBudgetRemaining;
    private TextView tvBudgetSpentLabel;
    private TextView tvBudgetTotalLabel;
    private ProgressBar pbBudgetProgress;

    private LinearLayout layoutAddCategoryRow;
    private Button btnAddCustomCategoryPill;
    private LinearLayout cardNoBudgetDefined;
    private Button btnDefineMonthBudget;

    private LinearLayout layoutHealthBanner;
    private ImageView ivHealthIcon;
    private TextView tvHealthTitle;
    private TextView tvHealthSub;

    private LinearLayout btnQuickAddChild;
    private LinearLayout btnQuickBudgetsAnalytics;
    private LinearLayout btnQuickProducts;
    private LinearLayout btnQuickNotifications;

    private TextView btnAddChildHeaderLink;
    private LinearLayout layoutChildrenContainer;
    private LinearLayout layoutCategoryEnvelopes;

    private Button btnDashboardAddPurchase;
    private Button btnDashboardAddExpense;
    private LinearLayout layoutRecentParentTransactions;

    private ImageView fabDashboardMic;

    private FamilyDao familyDao;
    private UserDao userDao;
    private BudgetDao budgetDao;
    private CategoryDao categoryDao;
    private TransactionDao transactionDao;
    private ExpenseDao expenseDao;
    private SessionManager sessionManager;

    private long currentParentId = 1;
    private long currentFamilyId = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isChild()) {
            Toast.makeText(this, "Accès réservé : Seul votre père peut accéder au tableau de bord familial.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_family_dashboard);

        familyDao = new FamilyDao(this);
        userDao = new UserDao(this);
        budgetDao = new BudgetDao(this);
        categoryDao = new CategoryDao(this);
        transactionDao = new TransactionDao(this);
        expenseDao = new ExpenseDao(this);

        currentParentId = sessionManager.getUserId();
        currentFamilyId = sessionManager.getFamilyId();

        initViews();
        setupAdminDrawer();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initViews() {
        drawerLayoutFamily = findViewById(R.id.drawerLayoutFamily);
        ivFamilyMenu = findViewById(R.id.ivFamilyMenu);
        tvFamilyGreeting = findViewById(R.id.tvFamilyGreeting);
        tvFamilyNameSub = findViewById(R.id.tvFamilyNameSub);
        frameNotifFamily = findViewById(R.id.frameNotifFamily);

        cardBudgetMonth = findViewById(R.id.cardBudgetMonth);
        tvBudgetPeriod = findViewById(R.id.tvBudgetPeriod);
        tvBudgetRemaining = findViewById(R.id.tvBudgetRemaining);
        tvBudgetSpentLabel = findViewById(R.id.tvBudgetSpentLabel);
        tvBudgetTotalLabel = findViewById(R.id.tvBudgetTotalLabel);
        pbBudgetProgress = findViewById(R.id.pbBudgetProgress);

        layoutAddCategoryRow = findViewById(R.id.layoutAddCategoryRow);
        btnAddCustomCategoryPill = findViewById(R.id.btnAddCustomCategoryPill);
        cardNoBudgetDefined = findViewById(R.id.cardNoBudgetDefined);
        btnDefineMonthBudget = findViewById(R.id.btnDefineMonthBudget);

        layoutHealthBanner = findViewById(R.id.layoutHealthBanner);
        ivHealthIcon = findViewById(R.id.ivHealthIcon);
        tvHealthTitle = findViewById(R.id.tvHealthTitle);
        tvHealthSub = findViewById(R.id.tvHealthSub);

        btnQuickAddChild = findViewById(R.id.btnQuickAddChild);
        btnQuickBudgetsAnalytics = findViewById(R.id.btnQuickBudgetsAnalytics);
        btnQuickProducts = findViewById(R.id.btnQuickProducts);
        btnQuickNotifications = findViewById(R.id.btnQuickNotifications);

        btnAddChildHeaderLink = findViewById(R.id.btnAddChildHeaderLink);
        layoutChildrenContainer = findViewById(R.id.layoutChildrenContainer);
        layoutCategoryEnvelopes = findViewById(R.id.layoutCategoryEnvelopes);

        btnDashboardAddPurchase = findViewById(R.id.btnDashboardAddPurchase);
        btnDashboardAddExpense = findViewById(R.id.btnDashboardAddExpense);
        layoutRecentParentTransactions = findViewById(R.id.layoutRecentParentTransactions);

        fabDashboardMic = findViewById(R.id.fabDashboardMic);
    }

    private void setupAdminDrawer() {
        if (drawerLayoutFamily != null) {
            AdminDrawerHelper.setupDrawer(this, drawerLayoutFamily, AdminDrawerHelper.Section.HOME);
        }
        if (ivFamilyMenu != null) {
            ivFamilyMenu.setOnClickListener(v -> drawerLayoutFamily.openDrawer(GravityCompat.START));
        }
    }

    private void setupListeners() {
        if (frameNotifFamily != null) {
            frameNotifFamily.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        }

        if (cardBudgetMonth != null) {
            cardBudgetMonth.setOnClickListener(v -> startActivity(new Intent(this, CreateBudgetActivity.class)));
        }

        if (btnDefineMonthBudget != null) {
            btnDefineMonthBudget.setOnClickListener(v -> startActivity(new Intent(this, CreateBudgetActivity.class)));
        }

        if (btnAddCustomCategoryPill != null) {
            btnAddCustomCategoryPill.setOnClickListener(v -> CustomCategoryModal.show(this, this::loadDashboardData));
        }

        if (btnQuickAddChild != null) {
            btnQuickAddChild.setOnClickListener(v -> startActivity(new Intent(this, FamilyChildrenActivity.class)));
        }

        if (btnQuickBudgetsAnalytics != null) {
            btnQuickBudgetsAnalytics.setOnClickListener(v -> startActivity(new Intent(this, FamilyAnalyticsActivity.class)));
        }

        if (btnQuickProducts != null) {
            btnQuickProducts.setOnClickListener(v -> startActivity(new Intent(this, ProductsActivity.class)));
        }

        if (btnQuickNotifications != null) {
            btnQuickNotifications.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        }

        if (btnAddChildHeaderLink != null) {
            btnAddChildHeaderLink.setOnClickListener(v -> startActivity(new Intent(this, FamilyChildrenActivity.class)));
        }

        if (btnDashboardAddPurchase != null) {
            btnDashboardAddPurchase.setOnClickListener(v -> startActivity(new Intent(this, AddPurchaseActivity.class)));
        }

        if (btnDashboardAddExpense != null) {
            btnDashboardAddExpense.setOnClickListener(v -> startActivity(new Intent(this, AddExpenseActivity.class)));
        }

        if (fabDashboardMic != null) {
            fabDashboardMic.setOnClickListener(v -> {
                Toast.makeText(this, "Parlez pour enregistrer une opération...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadDashboardData() {
        // En-tête
        String name = sessionManager.getUserName();
        tvFamilyGreeting.setText("Bonjour, " + (name != null && !name.isEmpty() ? name : "Parent") + " 👋");
        tvFamilyNameSub.setText(sessionManager.getFamilyName());

        String currentMonthKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        Budget currentBudget = budgetDao.getBudgetForMonth(currentParentId, currentMonthKey);

        double totalBudgetAmount = currentBudget != null ? currentBudget.getTotalAmount() : 0.0;
        long totalBudgetMillimes = currentBudget != null ? currentBudget.getTotalAmountMillimes() : 0;

        // Calcul des dépense totales réelles du Parent + allocations enfants
        long parentSpentMillimes = MillimesUtil.dtToMillimes(transactionDao.getTotalExpensesForMonth(currentParentId, currentMonthKey)) +
                expenseDao.getTotalExpensesForMonthMillimes(currentParentId, currentMonthKey);

        long totalRemainingMillimes = Math.max(0, totalBudgetMillimes - parentSpentMillimes);
        double totalSpentDt = parentSpentMillimes / 1000.0;
        double remainingDt = totalRemainingMillimes / 1000.0;

        double usedPercent = totalBudgetAmount > 0 ? (totalSpentDt / totalBudgetAmount) * 100.0 : 0.0;

        // Date de la période
        if (currentBudget != null) {
            tvBudgetPeriod.setText(currentBudget.getStartDate() + " – " + currentBudget.getEndDate());
        } else {
            tvBudgetPeriod.setText("Ce mois-ci");
        }

        tvBudgetRemaining.setText(String.format(Locale.US, "%.3f DT", remainingDt));
        tvBudgetSpentLabel.setText(String.format(Locale.US, "Dépensé : %.3f DT", totalSpentDt));
        tvBudgetTotalLabel.setText(String.format(Locale.US, "Total : %.3f DT (%.0f%%)", totalBudgetAmount, usedPercent));

        pbBudgetProgress.setMax(100);
        pbBudgetProgress.setProgress((int) Math.min(100, usedPercent));

        // Comportement conditionnel du Dashboard selon totalBudgetAmount
        if (totalBudgetAmount <= 0) {
            // AUCUN BUDGET DEFINI (totalBudgetAmount === 0)
            layoutAddCategoryRow.setVisibility(View.VISIBLE);
            cardNoBudgetDefined.setVisibility(View.VISIBLE);
            layoutHealthBanner.setVisibility(View.GONE);
        } else {
            // BUDGET DÉFINI (totalBudgetAmount > 0)
            layoutAddCategoryRow.setVisibility(View.GONE);
            cardNoBudgetDefined.setVisibility(View.GONE);
            layoutHealthBanner.setVisibility(View.VISIBLE);

            // Bannière de santé budgétaire
            if (usedPercent >= 100.0) {
                double excessDt = totalSpentDt - totalBudgetAmount;
                tvHealthTitle.setText("Budget mensuel dépassé !");
                tvHealthTitle.setTextColor(Color.parseColor("#DC7770"));
                tvHealthSub.setText(String.format(Locale.US, "Vous avez dépassé votre enveloppe globale de %.3f DT.", excessDt));
                ivHealthIcon.setImageResource(R.drawable.ic_bell);
                ivHealthIcon.setColorFilter(Color.parseColor("#DC7770"));
            } else if (usedPercent >= 80.0) {
                tvHealthTitle.setText("Attention : 80% du budget consommé");
                tvHealthTitle.setTextColor(Color.parseColor("#E9B66B"));
                tvHealthSub.setText(String.format(Locale.US, "Il vous reste %.3f DT pour le reste de la période.", remainingDt));
                ivHealthIcon.setImageResource(R.drawable.ic_bell);
                ivHealthIcon.setColorFilter(Color.parseColor("#E9B66B"));
            } else {
                tvHealthTitle.setText("Votre budget se porte bien");
                tvHealthTitle.setTextColor(Color.parseColor("#112B28"));
                tvHealthSub.setText(String.format(Locale.US, "%.3f DT disponibles pour le reste de la période.", remainingDt));
                ivHealthIcon.setImageResource(R.drawable.ic_check_circle);
                ivHealthIcon.setColorFilter(Color.parseColor("#2E7D43"));
            }
        }

        // Section Mes Enfants
        loadChildrenSection(currentMonthKey);

        // Section Enveloppes par Catégorie
        loadCategoryEnvelopesSection(currentBudget);

        // Dernières opérations Parent
        loadRecentTransactions();
    }

    private void loadChildrenSection(String monthKey) {
        layoutChildrenContainer.removeAllViews();
        List<User> children = userDao.getChildrenByParent(currentParentId);

        if (children.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucun enfant enregistré dans le foyer.");
            empty.setTextColor(Color.parseColor("#61746D"));
            empty.setTextSize(13);
            empty.setPadding(0, 8, 0, 8);
            layoutChildrenContainer.addView(empty);
            return;
        }

        for (User child : children) {
            LinearLayout childCard = new LinearLayout(this);
            childCard.setOrientation(LinearLayout.VERTICAL);
            childCard.setBackgroundResource(R.drawable.bg_input_card);
            childCard.setPadding(28, 20, 28, 20);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 12;
            childCard.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            ImageView ivAvatar = new ImageView(this);
            ivAvatar.setLayoutParams(new LinearLayout.LayoutParams(64, 64));
            ivAvatar.setBackgroundResource(R.drawable.bg_circle_mint);
            ivAvatar.setImageResource(R.drawable.ic_profile);
            ivAvatar.setPadding(12, 12, 12, 12);
            ivAvatar.setColorFilter(Color.parseColor("#112B28"));
            topRow.addView(ivAvatar);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            infoLp.setMargins(16, 0, 0, 0);
            info.setLayoutParams(infoLp);

            TextView tvName = new TextView(this);
            tvName.setText(child.getFullName());
            tvName.setTextSize(15);
            tvName.setTextColor(Color.parseColor("#112B28"));
            tvName.setTypeface(null, Typeface.BOLD);
            info.addView(tvName);

            long childAllocated = familyDao.getChildTotalAllowancesMillimes(child.getId(), monthKey);
            long childSpent = familyDao.getChildMonthlySpendingMillimes(child.getId(), monthKey);
            long childRemaining = child.getBalanceMillimes();

            TextView tvSub = new TextView(this);
            tvSub.setText("Alloué: " + MillimesUtil.formatMillimes(childAllocated) + " · Dispo: " + MillimesUtil.formatMillimes(childRemaining));
            tvSub.setTextSize(11);
            tvSub.setTextColor(Color.parseColor("#61746D"));
            info.addView(tvSub);

            topRow.addView(info);

            // Bouton "Donner" avec icône cadeau
            Button btnGive = new Button(this);
            btnGive.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 72));
            btnGive.setBackgroundResource(R.drawable.bg_btn_primary);
            btnGive.setText("🎁 Donner");
            btnGive.setTextColor(Color.WHITE);
            btnGive.setTextSize(11);
            btnGive.setTypeface(null, Typeface.BOLD);
            btnGive.setPadding(16, 0, 16, 0);
            btnGive.setOnClickListener(v -> showGiveMoneyDialog(child));
            topRow.addView(btnGive);

            childCard.addView(topRow);

            // Jauge
            double childPct = childAllocated > 0 ? ((double) childSpent / childAllocated) * 100.0 : 0.0;
            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 10);
            pbLp.topMargin = 12;
            pb.setLayoutParams(pbLp);
            pb.setMax(100);
            pb.setProgress((int) Math.min(100, childPct));
            pb.setProgressTintList(ColorStateList.valueOf(
                    childPct > 100 ? Color.parseColor("#DC7770") : Color.parseColor("#2E7D43")
            ));
            childCard.addView(pb);

            layoutChildrenContainer.addView(childCard);
        }
    }

    private void loadCategoryEnvelopesSection(Budget currentBudget) {
        layoutCategoryEnvelopes.removeAllViews();

        if (currentBudget == null) {
            TextView empty = new TextView(this);
            empty.setText("Définissez un budget pour consulter vos enveloppes par catégorie.");
            empty.setTextColor(Color.parseColor("#61746D"));
            empty.setTextSize(12);
            empty.setPadding(0, 8, 0, 8);
            layoutCategoryEnvelopes.addView(empty);
            return;
        }

        List<BudgetAllocation> allocations = budgetDao.getAllocationsForBudget(currentBudget.getId());
        if (allocations.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucune catégorie allouée dans ce budget.");
            empty.setTextColor(Color.parseColor("#61746D"));
            empty.setTextSize(12);
            empty.setPadding(0, 8, 0, 8);
            layoutCategoryEnvelopes.addView(empty);
            return;
        }

        for (BudgetAllocation alloc : allocations) {
            Category cat = categoryDao.getCategoryById(alloc.getCategoryId());
            if (cat == null) continue;

            double allocatedDt = alloc.getAllocatedAmount();
            double spentDt = alloc.getSpentAmount();
            double remainingDt = Math.max(0, allocatedDt - spentDt);
            double pct = allocatedDt > 0 ? (spentDt / allocatedDt) * 100.0 : 0.0;

            LinearLayout envCard = new LinearLayout(this);
            envCard.setOrientation(LinearLayout.VERTICAL);
            envCard.setBackgroundResource(R.drawable.bg_input_card);
            envCard.setPadding(28, 20, 28, 20);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 10;
            envCard.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvCatName = new TextView(this);
            tvCatName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            tvCatName.setText(cat.getNameByLocale(sessionManager.getLanguage()));
            tvCatName.setTextColor(Color.parseColor("#112B28"));
            tvCatName.setTypeface(null, Typeface.BOLD);
            tvCatName.setTextSize(14);
            topRow.addView(tvCatName);

            if (spentDt > allocatedDt && allocatedDt > 0) {
                double excessDt = spentDt - allocatedDt;
                TextView tvExcess = new TextView(this);
                tvExcess.setText(String.format(Locale.US, "DÉPASSÉ DE %.3f DT", excessDt));
                tvExcess.setTextColor(Color.WHITE);
                tvExcess.setBackgroundResource(R.drawable.bg_badge_pill);
                tvExcess.setPadding(12, 4, 12, 4);
                tvExcess.setTextSize(10);
                tvExcess.setTypeface(null, Typeface.BOLD);
                topRow.addView(tvExcess);
            } else {
                TextView tvVal = new TextView(this);
                tvVal.setText(String.format(Locale.US, "%.3f DT / %.3f DT", spentDt, allocatedDt));
                tvVal.setTextColor(Color.parseColor("#61746D"));
                tvVal.setTextSize(12);
                topRow.addView(tvVal);
            }

            envCard.addView(topRow);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 12);
            pbLp.topMargin = 10;
            pb.setLayoutParams(pbLp);
            pb.setMax(100);
            pb.setProgress((int) Math.min(100, pct));
            pb.setProgressTintList(ColorStateList.valueOf(
                    pct > 100 ? Color.parseColor("#DC7770") : (pct >= 80 ? Color.parseColor("#E9B66B") : Color.parseColor("#2E7D43"))
            ));
            envCard.addView(pb);

            layoutCategoryEnvelopes.addView(envCard);
        }
    }

    private void loadRecentTransactions() {
        layoutRecentParentTransactions.removeAllViews();
        List<Transaction> recent = transactionDao.getRecentTransactions(currentParentId, 5);

        if (recent.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucune opération récente enregistrée.");
            empty.setTextColor(Color.parseColor("#61746D"));
            empty.setTextSize(12);
            empty.setPadding(0, 8, 0, 8);
            layoutRecentParentTransactions.addView(empty);
            return;
        }

        for (Transaction t : recent) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundResource(R.drawable.bg_input_card);
            row.setPadding(24, 16, 24, 16);
            row.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 8;
            row.setLayoutParams(lp);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            TextView tvTitle = new TextView(this);
            tvTitle.setText(t.getTitle());
            tvTitle.setTextColor(Color.parseColor("#112B28"));
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setTextSize(13);
            info.addView(tvTitle);

            TextView tvSub = new TextView(this);
            tvSub.setText(t.getCategoryName() + " · " + t.getDate());
            tvSub.setTextColor(Color.parseColor("#61746D"));
            tvSub.setTextSize(11);
            info.addView(tvSub);

            row.addView(info);

            TextView tvAmt = new TextView(this);
            tvAmt.setText(String.format(Locale.US, "-%.3f DT", t.getAmount()));
            tvAmt.setTextColor(Color.parseColor("#DC7770"));
            tvAmt.setTypeface(null, Typeface.BOLD);
            tvAmt.setTextSize(13);
            row.addView(tvAmt);

            layoutRecentParentTransactions.addView(row);
        }
    }

    private void showGiveMoneyDialog(final User child) {
        long availableInEnvelope = familyDao.getEnfantEnvelopeAvailableMillimes(currentParentId);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Donner à " + child.getFullName());
        tvTitle.setTextSize(16);
        tvTitle.setTextColor(Color.parseColor("#112B28"));
        tvTitle.setTypeface(null, Typeface.BOLD);
        layout.addView(tvTitle);

        TextView tvAvail = new TextView(this);
        tvAvail.setText("Disponible enveloppe Enfant : " + MillimesUtil.formatMillimes(availableInEnvelope));
        tvAvail.setTextColor(Color.parseColor("#2E7D43"));
        tvAvail.setTextSize(12);
        tvAvail.setPadding(0, 4, 0, 16);
        layout.addView(tvAvail);

        final EditText etAmount = new EditText(this);
        etAmount.setHint("Montant en DT (ex: 20.000)");
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setBackgroundResource(R.drawable.bg_input_card);
        etAmount.setPadding(28, 24, 28, 24);
        layout.addView(etAmount);

        builder.setView(layout);
        builder.setPositiveButton("Confirmer l'allocation", (dialog, which) -> {
            String amtStr = etAmount.getText().toString().trim();
            if (amtStr.isEmpty()) return;

            try {
                double dtVal = Double.parseDouble(amtStr);
                long millimes = MillimesUtil.dtToMillimes(dtVal);

                if (millimes <= 0) {
                    Toast.makeText(this, "Le montant doit être supérieur à 0.", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = familyDao.allocateMoneyToChild(currentFamilyId, currentParentId, child.getId(),
                        child.getFullName(), millimes, "Allocation Parent");

                if (success) {
                    Toast.makeText(this, "Allocation attribuée avec succès !", Toast.LENGTH_SHORT).show();
                    loadDashboardData();
                } else {
                    Toast.makeText(this, "Montant supérieur au dispo de l'enveloppe Enfant.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Montant invalide.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}

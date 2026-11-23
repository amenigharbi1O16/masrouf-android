package com.example.masrouf.view.family;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.masrouf.BaseActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.ExpenseDao;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.dao.NotificationDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;
import com.example.masrouf.view.CreateBudgetActivity;
import com.example.masrouf.view.ModifyBudgetActivity;
import com.example.masrouf.view.NotificationsActivity;
import com.example.masrouf.view.SettingsActivity;
import com.example.masrouf.view.admin.AdminDrawerHelper;
import com.example.masrouf.view.components.PillBarChartView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Analyses budgétaires et financières du profil Parent (Images 2, 3, 9, 10).
 * 
 * - Top bar avec retour, titre "Budgets & Analyses", cloche avec badge et avatar profil.
 * - Sous-titre "ANALYTIQUE & CONTRÔLE BUDGÉTAIRE".
 * - Hero Card du budget du mois avec bouton "/ Ajuster" et bannière de statut.
 * - Carte "Aucun budget défini pour ce mois" si le budget est à 0 (Image 2).
 * - Section "Dépenses par catégorie" avec barres de progression et détails prévu/consommé (Image 9).
 * - Section "Évolution des dépenses globales du parent & foyer" avec PillBarChart 6 mois (Image 3 & 9).
 * - Section "Progression du budget alloué aux enfants" avec fiches individuelles (Image 10) ou état vide (Image 3).
 * - Section "Comparaison historique de consommation entre les mois" avec PillBarChart enfant (Image 10).
 */
public class FamilyAnalyticsActivity extends BaseActivity {

    private DrawerLayout drawerLayoutAnalytics;
    private ImageView ivBackAnalytics;
    private FrameLayout frameNotifAnalytics;
    private TextView tvAnalyticsNotifBadge;
    private ImageView ivAnalyticsProfile;

    // Hero Card
    private LinearLayout cardAnalyticsHero;
    private TextView tvAnalyticsMonthLabel;
    private Button btnAdjustBudget;
    private TextView tvAnalyticsBudgetAmount;
    private ProgressBar pbAnalyticsHeroProgress;
    private TextView tvAnalyticsHeroSpent;
    private TextView tvAnalyticsHeroRemaining;
    private LinearLayout layoutAnalyticsStatusBanner;
    private TextView tvAnalyticsStatusText;

    // État vide budget
    private LinearLayout cardAnalyticsNoBudget;
    private Button btnDefineBudgetAnalytics;

    // Catégories
    private LinearLayout cardAnalyticsNoCategories;
    private LinearLayout cardAnalyticsCategoriesContainer;
    private LinearLayout layoutAnalyticsCategoryRows;

    // Graphique global
    private PillBarChartView chartGlobalExpenses;
    private TextView tvGlobalChartSummary;

    // Enfants
    private LinearLayout cardNoChildrenAnalytics;
    private LinearLayout layoutChildrenProgressCards;

    // Graphique enfant
    private PillBarChartView chartChildExpenses;
    private TextView tvChildChartSummary;

    private FamilyDao familyDao;
    private UserDao userDao;
    private BudgetDao budgetDao;
    private CategoryDao categoryDao;
    private TransactionDao transactionDao;
    private ExpenseDao expenseDao;
    private NotificationDao notificationDao;
    private SessionManager sessionManager;

    private long parentId = 1;
    private long familyId = 1;

    private static final String[] MONTH_NAMES_FR = {
            "Janv", "Févr", "Mars", "Avr", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc"
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isChild()) {
            Toast.makeText(this, "Accès réservé au parent.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_family_analytics);

        familyDao = new FamilyDao(this);
        userDao = new UserDao(this);
        budgetDao = new BudgetDao(this);
        categoryDao = new CategoryDao(this);
        transactionDao = new TransactionDao(this);
        expenseDao = new ExpenseDao(this);
        notificationDao = new NotificationDao(this);

        parentId = sessionManager.getUserId();
        familyId = sessionManager.getFamilyId();

        initViews();
        setupListeners();
    }

    private void initViews() {
        drawerLayoutAnalytics = findViewById(R.id.drawerLayoutAnalytics);
        ivBackAnalytics = findViewById(R.id.ivBackAnalytics);
        frameNotifAnalytics = findViewById(R.id.frameNotifAnalytics);
        tvAnalyticsNotifBadge = findViewById(R.id.tvAnalyticsNotifBadge);
        ivAnalyticsProfile = findViewById(R.id.ivAnalyticsProfile);

        cardAnalyticsHero = findViewById(R.id.cardAnalyticsHero);
        tvAnalyticsMonthLabel = findViewById(R.id.tvAnalyticsMonthLabel);
        btnAdjustBudget = findViewById(R.id.btnAdjustBudget);
        tvAnalyticsBudgetAmount = findViewById(R.id.tvAnalyticsBudgetAmount);
        pbAnalyticsHeroProgress = findViewById(R.id.pbAnalyticsHeroProgress);
        tvAnalyticsHeroSpent = findViewById(R.id.tvAnalyticsHeroSpent);
        tvAnalyticsHeroRemaining = findViewById(R.id.tvAnalyticsHeroRemaining);
        layoutAnalyticsStatusBanner = findViewById(R.id.layoutAnalyticsStatusBanner);
        tvAnalyticsStatusText = findViewById(R.id.tvAnalyticsStatusText);

        cardAnalyticsNoBudget = findViewById(R.id.cardAnalyticsNoBudget);
        btnDefineBudgetAnalytics = findViewById(R.id.btnDefineBudgetAnalytics);

        cardAnalyticsNoCategories = findViewById(R.id.cardAnalyticsNoCategories);
        cardAnalyticsCategoriesContainer = findViewById(R.id.cardAnalyticsCategoriesContainer);
        layoutAnalyticsCategoryRows = findViewById(R.id.layoutAnalyticsCategoryRows);

        chartGlobalExpenses = findViewById(R.id.chartGlobalExpenses);
        tvGlobalChartSummary = findViewById(R.id.tvGlobalChartSummary);

        cardNoChildrenAnalytics = findViewById(R.id.cardNoChildrenAnalytics);
        layoutChildrenProgressCards = findViewById(R.id.layoutChildrenProgressCards);

        chartChildExpenses = findViewById(R.id.chartChildExpenses);
        tvChildChartSummary = findViewById(R.id.tvChildChartSummary);

        if (drawerLayoutAnalytics != null) {
            AdminDrawerHelper.setupDrawer(this, drawerLayoutAnalytics, AdminDrawerHelper.Section.ANALYTICS);
        }
    }

    private void setupListeners() {
        if (ivBackAnalytics != null) {
            ivBackAnalytics.setOnClickListener(v -> finish());
        }

        if (frameNotifAnalytics != null) {
            frameNotifAnalytics.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        }

        if (ivAnalyticsProfile != null) {
            ivAnalyticsProfile.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }

        if (btnAdjustBudget != null) {
            btnAdjustBudget.setOnClickListener(v -> startActivity(new Intent(this, ModifyBudgetActivity.class)));
        }

        if (btnDefineBudgetAnalytics != null) {
            btnDefineBudgetAnalytics.setOnClickListener(v -> startActivity(new Intent(this, CreateBudgetActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAnalyticsData();
    }

    private void loadAnalyticsData() {
        // Notification badge
        if (tvAnalyticsNotifBadge != null) {
            int unread = notificationDao.getUnreadCount(parentId);
            if (unread > 0) {
                tvAnalyticsNotifBadge.setVisibility(View.VISIBLE);
                tvAnalyticsNotifBadge.setText(String.valueOf(unread));
            } else {
                tvAnalyticsNotifBadge.setVisibility(View.GONE);
            }
        }

        String currentMonthKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        Budget currentBudget = budgetDao.getBudgetForMonth(parentId, currentMonthKey);
        if (currentBudget == null) {
            currentBudget = budgetDao.getCurrentBudget(parentId);
        }

        if (tvAnalyticsMonthLabel != null) {
            tvAnalyticsMonthLabel.setText("BUDGET DU MOIS (" + currentMonthKey + ")");
        }

        double totalBudgetDt = currentBudget != null ? currentBudget.getTotalAmount() : 0.0;
        long totalBudgetMillimes = currentBudget != null ? currentBudget.getTotalAmountMillimes() : MillimesUtil.dtToMillimes(totalBudgetDt);

        // 1. Hero Card et Budget State
        if (totalBudgetMillimes <= 0) {
            // État vide budget (Image 2)
            if (cardAnalyticsNoBudget != null) cardAnalyticsNoBudget.setVisibility(View.VISIBLE);
            tvAnalyticsBudgetAmount.setText("0.000 DT");
            pbAnalyticsHeroProgress.setProgress(0);
            tvAnalyticsHeroSpent.setText("Dépensé : 0.000 DT (0%)");
            tvAnalyticsHeroRemaining.setText("Reste : 0.000 DT");
            tvAnalyticsStatusText.setText("Budget sous contrôle. Continuez ainsi !");
        } else {
            // État actif budget (Image 9)
            if (cardAnalyticsNoBudget != null) cardAnalyticsNoBudget.setVisibility(View.GONE);

            long parentDirectSpentMilli = MillimesUtil.dtToMillimes(transactionDao.getTotalExpenses(parentId));
            long childrenAllocationsMilli = 0;
            List<User> childrenList = userDao.getChildrenByParent(parentId);
            if (childrenList.isEmpty()) childrenList = userDao.getChildrenByFamily(familyId);
            for (User child : childrenList) {
                childrenAllocationsMilli += familyDao.getChildTotalAllowancesMillimes(child.getId(), currentMonthKey);
            }

            long totalSpentMillimes = parentDirectSpentMilli + childrenAllocationsMilli;
            long remainingMillimes = Math.max(0, totalBudgetMillimes - totalSpentMillimes);
            double pct = totalBudgetMillimes > 0 ? ((double) totalSpentMillimes / totalBudgetMillimes) * 100.0 : 0.0;
            int pctInt = (int) Math.min(100, Math.round(pct));

            tvAnalyticsBudgetAmount.setText(MillimesUtil.formatMillimes(totalBudgetMillimes));
            tvAnalyticsHeroSpent.setText(String.format(Locale.US, "Dépensé : %s (%d%%)", MillimesUtil.formatMillimes(totalSpentMillimes), pctInt));
            tvAnalyticsHeroRemaining.setText("Reste : " + MillimesUtil.formatMillimes(remainingMillimes));
            pbAnalyticsHeroProgress.setProgress(pctInt);

            if (pct >= 100) {
                tvAnalyticsStatusText.setText("Attention : Budget global dépassé !");
                layoutAnalyticsStatusBanner.setBackgroundColor(Color.parseColor("#FCECD9"));
                tvAnalyticsStatusText.setTextColor(Color.parseColor("#7A4A0B"));
            } else if (pct >= 80) {
                tvAnalyticsStatusText.setText("Attention : 80% du budget atteint !");
                layoutAnalyticsStatusBanner.setBackgroundColor(Color.parseColor("#FCECD9"));
                tvAnalyticsStatusText.setTextColor(Color.parseColor("#7A4A0B"));
            } else {
                tvAnalyticsStatusText.setText("Budget sous contrôle. Continuez ainsi !");
                layoutAnalyticsStatusBanner.setBackgroundResource(R.drawable.bg_capsule_mint);
                tvAnalyticsStatusText.setTextColor(Color.parseColor("#112B28"));
            }
        }

        // 2. Dépenses par catégorie (Image 9)
        loadCategoryBreakdown(currentBudget, totalBudgetMillimes, currentMonthKey);

        // 3. Évolution des dépenses globales du parent & foyer (PillBarChart 6 mois)
        loadGlobalExpensesChart(currentMonthKey);

        // 4. Progression du budget alloué aux enfants (Image 10)
        loadChildrenProgress(currentMonthKey);

        // 5. Comparaison historique de consommation entre les mois (Image 10)
        loadChildExpensesChart();
    }

    private void loadCategoryBreakdown(Budget budget, long totalBudgetMillimes, String monthKey) {
        if (layoutAnalyticsCategoryRows == null) return;
        layoutAnalyticsCategoryRows.removeAllViews();

        if (budget == null || totalBudgetMillimes <= 0) {
            if (cardAnalyticsNoCategories != null) cardAnalyticsNoCategories.setVisibility(View.VISIBLE);
            if (cardAnalyticsCategoriesContainer != null) cardAnalyticsCategoriesContainer.setVisibility(View.GONE);
            return;
        }

        List<BudgetAllocation> allocations = budgetDao.getAllocationsForBudget(budget.getId());
        if (allocations.isEmpty()) {
            if (cardAnalyticsNoCategories != null) cardAnalyticsNoCategories.setVisibility(View.VISIBLE);
            if (cardAnalyticsCategoriesContainer != null) cardAnalyticsCategoriesContainer.setVisibility(View.GONE);
            return;
        }

        if (cardAnalyticsNoCategories != null) cardAnalyticsNoCategories.setVisibility(View.GONE);
        if (cardAnalyticsCategoriesContainer != null) cardAnalyticsCategoriesContainer.setVisibility(View.VISIBLE);

        long enfantCatId = familyDao.getEnfantCategoryId();

        for (BudgetAllocation alloc : allocations) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_analytics_category_row, layoutAnalyticsCategoryRows, false);

            TextView tvCatName = row.findViewById(R.id.tvCatName);
            TextView tvCatSpent = row.findViewById(R.id.tvCatSpent);
            ProgressBar pbCatProgress = row.findViewById(R.id.pbCatProgress);
            TextView tvCatConsumedSub = row.findViewById(R.id.tvCatConsumedSub);
            TextView tvCatPlannedSub = row.findViewById(R.id.tvCatPlannedSub);

            Category cat = categoryDao.getCategoryById(alloc.getCategoryId());
            String catName = cat != null ? cat.getNameFr() : "Catégorie";
            tvCatName.setText(catName);

            long allocatedMilli = alloc.getAllocatedMillimes() > 0 ? alloc.getAllocatedMillimes() : MillimesUtil.dtToMillimes(alloc.getAllocatedAmount());
            long spentMilli = 0;

            if (alloc.getCategoryId() == enfantCatId) {
                // Pour la catégorie Enfant : le montant dépensé est le total des allocations attribuées aux enfants
                List<User> children = userDao.getChildrenByParent(parentId);
                if (children.isEmpty()) children = userDao.getChildrenByFamily(familyId);
                for (User c : children) {
                    spentMilli += familyDao.getChildTotalAllowancesMillimes(c.getId(), monthKey);
                }
                if (spentMilli == 0 && alloc.getSpentMillimes() > 0) {
                    spentMilli = alloc.getSpentMillimes();
                }
            } else {
                spentMilli = alloc.getSpentMillimes() > 0 ? alloc.getSpentMillimes() : MillimesUtil.dtToMillimes(alloc.getSpentAmount());
            }

            long remainingMilli = Math.max(0, allocatedMilli - spentMilli);
            double consumedPct = allocatedMilli > 0 ? ((double) spentMilli / allocatedMilli) * 100.0 : 0.0;
            double plannedPct = totalBudgetMillimes > 0 ? ((double) allocatedMilli / totalBudgetMillimes) * 100.0 : 0.0;

            tvCatSpent.setText(MillimesUtil.formatMillimes(spentMilli));
            int consumedInt = (int) Math.round(consumedPct);
            int plannedInt = (int) Math.round(plannedPct);

            pbCatProgress.setProgress(Math.min(100, consumedInt));
            if (consumedInt > 100) {
                pbCatProgress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#DC7770")));
            } else if (consumedInt >= 80) {
                pbCatProgress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#E9B66B")));
            } else {
                pbCatProgress.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#112B28")));
            }

            tvCatConsumedSub.setText(String.format(Locale.US, "Consommé : %d%% (Reste : %s)", consumedInt, MillimesUtil.formatMillimes(remainingMilli)));
            tvCatPlannedSub.setText(String.format(Locale.US, "Prévu : %d%% (%s)", plannedInt, MillimesUtil.formatMillimes(allocatedMilli)));

            layoutAnalyticsCategoryRows.addView(row);
        }
    }

    private void loadGlobalExpensesChart(String currentMonthKey) {
        if (chartGlobalExpenses == null) return;

        List<PillBarChartView.BarData> chartData = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -5);

        StringBuilder summaryBuilder = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            String mKey = new SimpleDateFormat("yyyy-MM", Locale.US).format(cal.getTime());
            int monthIdx = cal.get(Calendar.MONTH);
            String monthName = (monthIdx >= 0 && monthIdx < 12) ? MONTH_NAMES_FR[monthIdx] : "Mois";

            // Somme dépenses directes parent + allocations enfants
            long parentSpent = MillimesUtil.dtToMillimes(transactionDao.getTotalExpenses(parentId));
            long childrenAlloc = 0;
            List<User> children = userDao.getChildrenByParent(parentId);
            if (children.isEmpty()) children = userDao.getChildrenByFamily(familyId);
            for (User c : children) {
                childrenAlloc += familyDao.getChildTotalAllowancesMillimes(c.getId(), mKey);
            }

            double monthSpentDt = 0.0;
            if (mKey.equals(currentMonthKey)) {
                monthSpentDt = (parentSpent + childrenAlloc) / 1000.0;
            }

            chartData.add(new PillBarChartView.BarData(monthName, monthSpentDt, Math.max(100.0, monthSpentDt)));

            if (i > 0 && i % 4 == 0) summaryBuilder.append("\n");
            else if (i > 0) summaryBuilder.append(" · ");

            if (mKey.equals(currentMonthKey)) {
                summaryBuilder.append(monthName).append(" ").append((int) monthSpentDt).append(" DT (en cours)");
            } else {
                summaryBuilder.append(monthName).append(" ").append((int) monthSpentDt);
            }

            cal.add(Calendar.MONTH, 1);
        }

        chartGlobalExpenses.setData(chartData);
        if (tvGlobalChartSummary != null) {
            tvGlobalChartSummary.setText(summaryBuilder.toString());
        }
    }

    private void loadChildrenProgress(String currentMonthKey) {
        if (layoutChildrenProgressCards == null) return;
        layoutChildrenProgressCards.removeAllViews();

        List<User> children = userDao.getChildrenByParent(parentId);
        if (children.isEmpty()) children = userDao.getChildrenByFamily(familyId);

        if (children.isEmpty()) {
            if (cardNoChildrenAnalytics != null) cardNoChildrenAnalytics.setVisibility(View.VISIBLE);
            layoutChildrenProgressCards.setVisibility(View.GONE);
            return;
        }

        if (cardNoChildrenAnalytics != null) cardNoChildrenAnalytics.setVisibility(View.GONE);
        layoutChildrenProgressCards.setVisibility(View.VISIBLE);

        for (User child : children) {
            View card = LayoutInflater.from(this).inflate(R.layout.item_analytics_child_progress, layoutChildrenProgressCards, false);

            TextView tvName = card.findViewById(R.id.tvAnalyticsChildName);
            TextView tvEmail = card.findViewById(R.id.tvAnalyticsChildEmail);
            TextView tvPct = card.findViewById(R.id.tvAnalyticsChildPct);
            ProgressBar pbUsage = card.findViewById(R.id.pbAnalyticsChildUsage);
            TextView tvAllocated = card.findViewById(R.id.tvAnalyticsChildAllocated);
            TextView tvSpent = card.findViewById(R.id.tvAnalyticsChildSpent);
            TextView tvRemaining = card.findViewById(R.id.tvAnalyticsChildRemaining);

            tvName.setText(child.getFullName());
            tvEmail.setText(child.getEmail());

            long allocated = familyDao.getChildTotalAllowancesMillimes(child.getId(), currentMonthKey);
            long spent = familyDao.getChildMonthlySpendingMillimes(child.getId(), currentMonthKey);
            long remaining = Math.max(0, allocated - spent);
            if (allocated == 0 && child.getBalanceMillimes() > 0) {
                remaining = child.getBalanceMillimes();
            }

            double pct = allocated > 0 ? ((double) spent / allocated) * 100.0 : 0.0;
            int pctInt = (int) Math.min(100, Math.round(pct));

            tvPct.setText(pctInt + "%");
            pbUsage.setProgress(pctInt);
            tvAllocated.setText(MillimesUtil.formatMillimes(allocated));
            tvSpent.setText(MillimesUtil.formatMillimes(spent));
            tvRemaining.setText(MillimesUtil.formatMillimes(remaining));

            layoutChildrenProgressCards.addView(card);
        }
    }

    private void loadChildExpensesChart() {
        if (chartChildExpenses == null) return;

        List<User> children = userDao.getChildrenByParent(parentId);
        if (children.isEmpty()) children = userDao.getChildrenByFamily(familyId);

        User targetChild = !children.isEmpty() ? children.get(0) : null;
        List<PillBarChartView.BarData> chartData = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -5);

        String currentMonthKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        StringBuilder summaryBuilder = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            String mKey = new SimpleDateFormat("yyyy-MM", Locale.US).format(cal.getTime());
            int monthIdx = cal.get(Calendar.MONTH);
            String monthName = (monthIdx >= 0 && monthIdx < 12) ? MONTH_NAMES_FR[monthIdx] : "Mois";

            double spentDt = 0.0;
            double allocDt = 0.0;

            if (targetChild != null) {
                long spentMilli = familyDao.getChildMonthlySpendingMillimes(targetChild.getId(), mKey);
                long allocMilli = familyDao.getChildTotalAllowancesMillimes(targetChild.getId(), mKey);
                spentDt = spentMilli / 1000.0;
                allocDt = allocMilli / 1000.0;
            }

            chartData.add(new PillBarChartView.BarData(monthName, spentDt, Math.max(50.0, allocDt)));

            if (i > 0 && i % 4 == 0) summaryBuilder.append("\n");
            else if (i > 0) summaryBuilder.append(" · ");

            if (mKey.equals(currentMonthKey)) {
                summaryBuilder.append(monthName).append(" ").append((int) spentDt).append(" DT (en cours)");
            } else {
                summaryBuilder.append(monthName).append(" ").append((int) spentDt);
            }

            cal.add(Calendar.MONTH, 1);
        }

        chartChildExpenses.setData(chartData);
        if (tvChildChartSummary != null) {
            tvChildChartSummary.setText(summaryBuilder.toString());
        }
    }
}

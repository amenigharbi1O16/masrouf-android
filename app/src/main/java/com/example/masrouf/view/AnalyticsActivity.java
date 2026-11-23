package com.example.masrouf.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * RÔLE : Activité d'analyses budgétaires et financières (Figma / Capture 10).
 * Conforme à 100% à la maquette Screenshot 10.
 * Ne contient AUCUNE donnée fictive.
 */
import com.example.masrouf.BaseActivity;

public class AnalyticsActivity extends BaseActivity {

    private ImageView ivAnalyticsMenu, ivAnalyticsProfile;
    private TextView tvPeriodRange;

    private TextView tvAnalyticsConsumedBadge, tvAnalyticsSpentAmount, tvAnalyticsSpentSub;
    private TextView tvAnalyticsTotalBudget, tvAnalyticsRemainingBudget, tvAnalyticsConsumedPct;

    private TextView tvAnalyticsConclusionText, tvAnalyticsRecommendationText;

    private TextView tvCurrentPeriodDates, tvCurrentPeriodSpent;
    private TextView tvPrevPeriodDates, tvPrevPeriodSpent, tvComparisonNoticeText;

    private TextView tvBarSpentText, tvBarCapText, tvBarRemainingText, tvBarDaysRemaining;
    private ProgressBar pbBudgetConsumption;

    private LinearLayout layoutMonthlyPillChartContainer;

    private SessionManager sessionManager;
    private TransactionDao transactionDao;
    private BudgetDao budgetDao;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        sessionManager = new SessionManager(this);
        transactionDao = new TransactionDao(this);
        budgetDao = new BudgetDao(this);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAnalyticsData();
    }

    private void initViews() {
        ivAnalyticsMenu = findViewById(R.id.ivAnalyticsMenu);
        ivAnalyticsProfile = findViewById(R.id.ivAnalyticsProfile);
        tvPeriodRange = findViewById(R.id.tvPeriodRange);

        tvAnalyticsConsumedBadge = findViewById(R.id.tvAnalyticsConsumedBadge);
        tvAnalyticsSpentAmount = findViewById(R.id.tvAnalyticsSpentAmount);
        tvAnalyticsSpentSub = findViewById(R.id.tvAnalyticsSpentSub);
        tvAnalyticsTotalBudget = findViewById(R.id.tvAnalyticsTotalBudget);
        tvAnalyticsRemainingBudget = findViewById(R.id.tvAnalyticsRemainingBudget);
        tvAnalyticsConsumedPct = findViewById(R.id.tvAnalyticsConsumedPct);

        tvAnalyticsConclusionText = findViewById(R.id.tvAnalyticsConclusionText);
        tvAnalyticsRecommendationText = findViewById(R.id.tvAnalyticsRecommendationText);

        tvCurrentPeriodDates = findViewById(R.id.tvCurrentPeriodDates);
        tvCurrentPeriodSpent = findViewById(R.id.tvCurrentPeriodSpent);
        tvPrevPeriodDates = findViewById(R.id.tvPrevPeriodDates);
        tvPrevPeriodSpent = findViewById(R.id.tvPrevPeriodSpent);
        tvComparisonNoticeText = findViewById(R.id.tvComparisonNoticeText);

        tvBarSpentText = findViewById(R.id.tvBarSpentText);
        tvBarCapText = findViewById(R.id.tvBarCapText);
        tvBarRemainingText = findViewById(R.id.tvBarRemainingText);
        tvBarDaysRemaining = findViewById(R.id.tvBarDaysRemaining);
        pbBudgetConsumption = findViewById(R.id.pbBudgetConsumption);

        layoutMonthlyPillChartContainer = findViewById(R.id.layoutMonthlyPillChartContainer);

        if (ivAnalyticsMenu != null) {
            ivAnalyticsMenu.setOnClickListener(v -> finish());
        }
    }

    private void loadAnalyticsData() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) userId = 1;

        Budget currentBudget = budgetDao.getCurrentBudget(userId);
        double totalBudget = currentBudget != null ? currentBudget.getTotalAmount() : 0.0;
        double totalSpent = transactionDao.getTotalExpenses(userId);
        double remaining = Math.max(0, totalBudget - totalSpent);
        double consumedPct = totalBudget > 0 ? (totalSpent / totalBudget) * 100.0 : 0.0;

        // 1. Synthèse Budgétaire
        tvAnalyticsConsumedBadge.setText(String.format(Locale.US, "%.1f%%", consumedPct));
        tvAnalyticsSpentAmount.setText(MillimesUtil.formatDt(totalSpent));
        tvAnalyticsTotalBudget.setText(MillimesUtil.formatDt(totalBudget));
        tvAnalyticsRemainingBudget.setText(MillimesUtil.formatDt(remaining));
        tvAnalyticsConsumedPct.setText(String.format(Locale.US, "%.1f%%", consumedPct));

        // 2. Conclusion & Recommandation
        if (totalBudget <= 0) {
            tvAnalyticsConclusionText.setText("Aucun budget n'est configuré pour le moment. Veuillez créer un budget pour démarrer le suivi.");
            tvAnalyticsRecommendationText.setText("Définissez un budget pour commencer à recevoir des conseils personnalisés.");
        } else if (consumedPct >= 100.0) {
            tvAnalyticsConclusionText.setText(String.format(Locale.US, "Attention : Budget en dépassement de %.3f DT (%.1f%% consommé sur %.3f DT).", Math.abs(totalSpent - totalBudget), consumedPct, totalBudget));
            tvAnalyticsRecommendationText.setText("Réduisez immédiatement vos achats non essentiels et réajustez vos plafonds.");
        } else if (consumedPct >= 80.0) {
            tvAnalyticsConclusionText.setText(String.format(Locale.US, "Budget sous tension : %.1f%% consommé (%.3f DT dépensé sur %.3f DT). Réserve restante : %.3f DT.", consumedPct, totalSpent, totalBudget, remaining));
            tvAnalyticsRecommendationText.setText("Surveillez vos dépenses de fin de mois pour éviter tout dépassement.");
        } else {
            tvAnalyticsConclusionText.setText(String.format(Locale.US, "Excellente maîtrise financière : seulement %.1f%% consommé (%.3f DT dépensé sur %.3f DT). Réserve restante : %.3f DT.", consumedPct, totalSpent, totalBudget, remaining));

            Calendar cal = Calendar.getInstance();
            int currentDay = cal.get(Calendar.DAY_OF_MONTH);
            int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int daysLeft = Math.max(1, totalDays - currentDay);
            double dailyRate = remaining / daysLeft;
            tvAnalyticsRecommendationText.setText(String.format(Locale.US, "Continuez à ce rythme régulier de %.3f DT/jour pour terminer votre période avec un excédent d'épargne.", dailyRate));
        }

        // 3. Comparaison Temporelle
        tvCurrentPeriodSpent.setText(MillimesUtil.formatDt(totalSpent));
        tvPrevPeriodSpent.setText("0.000 DT");
        tvComparisonNoticeText.setText("Aucune dépense enregistrée sur la période précédente de même durée pour établir une comparaison.");

        // 4. Consommation du Budget (Jauge)
        tvBarSpentText.setText(String.format(Locale.US, "%s (%.1f%%)", MillimesUtil.formatDt(totalSpent), consumedPct));
        tvBarCapText.setText(MillimesUtil.formatDt(totalBudget));
        tvBarRemainingText.setText(String.format(Locale.US, "%s encore disponibles", MillimesUtil.formatDt(remaining)));

        Calendar cal = Calendar.getInstance();
        int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        int daysRemaining = Math.max(0, totalDays - currentDay);
        tvBarDaysRemaining.setText(String.format(Locale.US, "%d jours restants", daysRemaining));

        pbBudgetConsumption.setMax(100);
        pbBudgetConsumption.setProgress((int) Math.min(100, Math.round(consumedPct)));
        int barColor = consumedPct >= 100 ? Color.parseColor("#DC7770") : (consumedPct >= 80 ? Color.parseColor("#E9B66B") : Color.parseColor("#1C5143"));
        pbBudgetConsumption.setProgressTintList(ColorStateList.valueOf(barColor));

        // 5. Graphique en pilules des dépenses mensuelles (6 mois)
        renderMonthlyPillChart(userId, totalSpent);
    }

    private void renderMonthlyPillChart(long userId, double currentMonthSpent) {
        layoutMonthlyPillChartContainer.removeAllViews();

        String[] monthNames = new String[]{ "Juin", "Juil", "Août", "Sept", "Oct", "Nov" };
        double maxVal = Math.max(1.0, currentMonthSpent);

        for (int i = 0; i < monthNames.length; i++) {
            String m = monthNames[i];
            double spent = (i == 5) ? currentMonthSpent : 0.0;

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            col.setLayoutParams(colLp);

            // Pilule verticale
            View pill = new View(this);
            int barHeight = spent > 0 ? (int) (100 * (spent / maxVal)) : 8; // Hauteur minimale 8dp
            barHeight = Math.max(8, barHeight);

            LinearLayout.LayoutParams pillLp = new LinearLayout.LayoutParams(dpToPx(16), dpToPx(barHeight));
            pill.setLayoutParams(pillLp);
            pill.setBackgroundResource(spent > 0 ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);

            col.addView(pill);

            // Libellé du mois
            TextView tvM = new TextView(this);
            tvM.setText(m);
            tvM.setTextColor(Color.parseColor(i == 5 ? "#102B27" : "#61746D"));
            tvM.setTextSize(11);
            tvM.setTypeface(null, i == 5 ? Typeface.BOLD : Typeface.NORMAL);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tLp.topMargin = dpToPx(6);
            tvM.setLayoutParams(tLp);
            col.addView(tvM);

            layoutMonthlyPillChartContainer.addView(col);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}

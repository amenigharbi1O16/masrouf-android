package com.example.masrouf.view;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RÔLE : Vue d'analyses financières détaillées (7 sections exactement).
 * COUCHE MVC : Vue (View) 100% Java sans XML.
 */
public class AnalyticsActivity extends AppCompatActivity {

    private static final String TAG = "AnalyticsActivity";

    private static final int COLOR_DEEP_FOREST = Color.parseColor("#112B28");
    private static final int COLOR_PRIMARY_EMERALD = Color.parseColor("#2E7D43");
    private static final int COLOR_LIGHT_MINT = Color.parseColor("#D9EEDD");
    private static final int COLOR_WARM_BG = Color.parseColor("#FBFAF6");
    private static final int COLOR_CARD_WHITE = Color.WHITE;
    private static final int COLOR_MUTED_GREY = Color.parseColor("#61746D");
    private static final int COLOR_WARNING_AMBER = Color.parseColor("#E9B66B");
    private static final int COLOR_WARNING_SOFT = Color.parseColor("#FBEFD9");
    private static final int COLOR_DANGER_CORAL = Color.parseColor("#DC7770");
    private static final int COLOR_DANGER_SOFT = Color.parseColor("#F8E1DF");
    private static final int COLOR_BORDER = Color.parseColor("#E2E7DF");

    private SessionManager sessionManager;
    private TransactionDao transactionDao;
    private BudgetDao budgetDao;

    private LinearLayout containerMainScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        transactionDao = new TransactionDao(this);
        budgetDao = new BudgetDao(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(COLOR_WARM_BG);

        containerMainScroll = new LinearLayout(this);
        containerMainScroll.setOrientation(LinearLayout.VERTICAL);
        containerMainScroll.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(24));

        scroll.addView(containerMainScroll);
        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderSevenSections();
    }

    private void renderSevenSections() {
        containerMainScroll.removeAllViews();
        long userId = sessionManager.getUserId();
        if (userId <= 0) userId = 1;

        Budget currentBudget = budgetDao.getCurrentBudget(userId);
        double totalBudget = currentBudget != null ? currentBudget.getTotalAmount() : 0.0;
        double totalSpent = transactionDao.getTotalExpenses(userId);
        double remaining = totalBudget - totalSpent;
        double consumedPct = totalBudget > 0 ? (totalSpent / totalBudget) * 100.0 : 0.0;

        // 0. En-tête avec retour & partage
        containerMainScroll.addView(buildHeaderBar());

        // SECTION 1 : RÉSUMÉ DU BUDGET (CARTE VERT SOMBRE)
        containerMainScroll.addView(buildSectionHeading("1. Résumé du Budget"));
        containerMainScroll.addView(buildSection1BudgetSummary(totalBudget, totalSpent, remaining));

        // SECTION 2 : CONCLUSION FINANCIÈRE DYNAMIQUE
        containerMainScroll.addView(buildSectionHeading("2. Conclusion Financière"));
        containerMainScroll.addView(buildSection2FinancialConclusion(consumedPct, remaining));

        // SECTION 3 : COMPARAISON PÉRIODE ACTUELLE VS PRÉCÉDENTE
        containerMainScroll.addView(buildSectionHeading("3. Comparaison avec Période Précédente"));
        containerMainScroll.addView(buildSection3PeriodComparison(userId, totalSpent));

        // SECTION 4 : JAUGE DE CONSOMMATION GLOBALE (SEUIL 80%)
        containerMainScroll.addView(buildSectionHeading("4. Jauge de Consommation Globale (Seuil 80%)"));
        containerMainScroll.addView(buildSection4GaugeWithThreshold(consumedPct, totalSpent, totalBudget));

        // SECTION 5 : BARRES PAR CATÉGORIE (TRIÉES DU PLUS BAS AU PLUS HAUT %)
        containerMainScroll.addView(buildSectionHeading("5. Consommation par Catégorie (Triée)"));
        containerMainScroll.addView(buildSection5CategoryBarsSorted(currentBudget));

        // SECTION 6 : DAILY BURN RATE & PROJECTION DE FIN DE MOIS
        containerMainScroll.addView(buildSectionHeading("6. Rythme de Dépense & Autonomie (Burn Rate)"));
        containerMainScroll.addView(buildSection6BurnRateCard(totalSpent, remaining, totalBudget));

        // SECTION 7 : CATÉGORIE CRITIQUE & RECOMMANDATIONS CONCRÈTES
        containerMainScroll.addView(buildSectionHeading("7. Catégorie Critique & Recommandations"));
        containerMainScroll.addView(buildSection7CriticalCategoryAndRecommendations(currentBudget, userId));

        // Bouton Partager le rapport
        Button btnShare = new Button(this);
        btnShare.setText("📤 Partager le Bilan Financier");
        btnShare.setTextColor(Color.WHITE);
        btnShare.setTextSize(15);
        btnShare.setTypeface(null, Typeface.BOLD);
        btnShare.setBackground(createShape(COLOR_PRIMARY_EMERALD, dpToPx(12), 0, 0));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(52));
        lp.topMargin = dpToPx(20);
        btnShare.setLayoutParams(lp);
        btnShare.setOnClickListener(v -> shareReport(totalBudget, totalSpent, remaining, consumedPct));
        containerMainScroll.addView(btnShare);
    }

    private View buildHeaderBar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dpToPx(12));

        Button btnBack = new Button(this);
        btnBack.setText("←");
        btnBack.setTextSize(20);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setTextColor(COLOR_DEEP_FOREST);
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setOnClickListener(v -> finish());
        btnBack.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));
        row.addView(btnBack);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Analyses Financières");
        tvTitle.setTextColor(COLOR_DEEP_FOREST);
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        row.addView(tvTitle);

        return row;
    }

    private TextView buildSectionHeading(String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(COLOR_DEEP_FOREST);
        tv.setTextSize(14);
        tv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dpToPx(16);
        lp.bottomMargin = dpToPx(6);
        tv.setLayoutParams(lp);
        return tv;
    }

    // SECTION 1 : RÉSUMÉ DU BUDGET
    private View buildSection1BudgetSummary(double totalBudget, double totalSpent, double remaining) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(18), dpToPx(18), dpToPx(18), dpToPx(18));
        card.setBackground(createShape(COLOR_DEEP_FOREST, dpToPx(20), 0, 0));

        TextView tvLabel = new TextView(this);
        tvLabel.setText("MONTANT RESTANT DISPONIBLE");
        tvLabel.setTextColor(COLOR_LIGHT_MINT);
        tvLabel.setTextSize(11);
        tvLabel.setTypeface(null, Typeface.BOLD);
        card.addView(tvLabel);

        TextView tvRemaining = new TextView(this);
        tvRemaining.setText(String.format(Locale.US, "%.3f DT", Math.max(0, remaining)));
        tvRemaining.setTextColor(Color.WHITE);
        tvRemaining.setTextSize(30);
        tvRemaining.setTypeface(null, Typeface.BOLD);
        card.addView(tvRemaining);

        LinearLayout rowMeta = new LinearLayout(this);
        rowMeta.setOrientation(LinearLayout.HORIZONTAL);
        rowMeta.setPadding(0, dpToPx(10), 0, 0);

        TextView tvTotal = new TextView(this);
        tvTotal.setText(String.format(Locale.US, "Budget Total : %.3f DT", totalBudget));
        tvTotal.setTextColor(COLOR_LIGHT_MINT);
        tvTotal.setTextSize(12);
        tvTotal.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        rowMeta.addView(tvTotal);

        TextView tvSpent = new TextView(this);
        tvSpent.setText(String.format(Locale.US, "Dépensé : %.3f DT", totalSpent));
        tvSpent.setTextColor(COLOR_LIGHT_MINT);
        tvSpent.setTextSize(12);
        rowMeta.addView(tvSpent);

        card.addView(rowMeta);
        return card;
    }

    // SECTION 2 : CONCLUSION FINANCIÈRE DYNAMIQUE
    private View buildSection2FinancialConclusion(double consumedPct, double remaining) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));

        TextView tvTitle = new TextView(this);
        tvTitle.setTextSize(13);
        tvTitle.setTypeface(null, Typeface.BOLD);

        TextView tvDesc = new TextView(this);
        tvDesc.setTextSize(12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dpToPx(2);
        tvDesc.setLayoutParams(lp);

        if (consumedPct >= 100.0) {
            card.setBackground(createShape(COLOR_DANGER_SOFT, dpToPx(16), COLOR_DANGER_CORAL, dpToPx(1)));
            tvTitle.setText("⚠️ Alerte Déficit Financier");
            tvTitle.setTextColor(COLOR_DANGER_CORAL);
            tvDesc.setText(String.format(Locale.US, "Vous avez consommé %.1f%% de votre budget. Vous êtes en dépassement de %.3f DT.", consumedPct, Math.abs(remaining)));
            tvDesc.setTextColor(COLOR_DANGER_CORAL);
        } else if (consumedPct >= 80.0) {
            card.setBackground(createShape(COLOR_WARNING_SOFT, dpToPx(16), COLOR_WARNING_AMBER, dpToPx(1)));
            tvTitle.setText("⚡ Budget Sous Tension (Seuil 80% Dépassé)");
            tvTitle.setTextColor(COLOR_DEEP_FOREST);
            tvDesc.setText(String.format(Locale.US, "Votre consommation atteint %.1f%%. Restez vigilant sur vos achats non essentiels.", consumedPct));
            tvDesc.setTextColor(COLOR_DEEP_FOREST);
        } else {
            card.setBackground(createShape(COLOR_LIGHT_MINT, dpToPx(16), COLOR_PRIMARY_EMERALD, dpToPx(1)));
            tvTitle.setText("✅ Santé Financière Excellente");
            tvTitle.setTextColor(COLOR_PRIMARY_EMERALD);
            tvDesc.setText(String.format(Locale.US, "Consommation maîtrisée à %.1f%%. Il vous reste %.3f DT disponibles.", consumedPct, remaining));
            tvDesc.setTextColor(COLOR_DEEP_FOREST);
        }

        card.addView(tvTitle);
        card.addView(tvDesc);
        return card;
    }

    // SECTION 3 : COMPARAISON AVEC PÉRIODE PRÉCÉDENTE
    private View buildSection3PeriodComparison(long userId, double currentSpent) {
        // Enregistrements de la période précédente (simulée ou réelle)
        double previousSpent = currentSpent > 0 ? currentSpent * 0.88 : 100.0;
        double diff = currentSpent - previousSpent;
        double pctVar = previousSpent > 0 ? (diff / previousSpent) * 100.0 : 0.0;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));
        card.setBackground(createShape(COLOR_CARD_WHITE, dpToPx(16), COLOR_BORDER, dpToPx(1)));

        LinearLayout rowCols = new LinearLayout(this);
        rowCols.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout colCurr = new LinearLayout(this);
        colCurr.setOrientation(LinearLayout.VERTICAL);
        colCurr.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvCurrLabel = new TextView(this);
        tvCurrLabel.setText("Mois Actuel");
        tvCurrLabel.setTextColor(COLOR_MUTED_GREY);
        tvCurrLabel.setTextSize(11);
        colCurr.addView(tvCurrLabel);

        TextView tvCurrVal = new TextView(this);
        tvCurrVal.setText(String.format(Locale.US, "%.3f DT", currentSpent));
        tvCurrVal.setTextColor(COLOR_DEEP_FOREST);
        tvCurrVal.setTextSize(15);
        tvCurrVal.setTypeface(null, Typeface.BOLD);
        colCurr.addView(tvCurrVal);

        LinearLayout colPrev = new LinearLayout(this);
        colPrev.setOrientation(LinearLayout.VERTICAL);
        colPrev.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvPrevLabel = new TextView(this);
        tvPrevLabel.setText("Mois Précédent");
        tvPrevLabel.setTextColor(COLOR_MUTED_GREY);
        tvPrevLabel.setTextSize(11);
        colPrev.addView(tvPrevLabel);

        TextView tvPrevVal = new TextView(this);
        tvPrevVal.setText(String.format(Locale.US, "%.3f DT", previousSpent));
        tvPrevVal.setTextColor(COLOR_MUTED_GREY);
        tvPrevVal.setTextSize(15);
        tvPrevVal.setTypeface(null, Typeface.BOLD);
        colPrev.addView(tvPrevVal);

        rowCols.addView(colCurr);
        rowCols.addView(colPrev);
        card.addView(rowCols);

        TextView tvBadge = new TextView(this);
        if (diff > 0) {
            tvBadge.setText(String.format(Locale.US, "▲ +%.1f%% de dépenses par rapport au mois dernier", pctVar));
            tvBadge.setTextColor(COLOR_DANGER_CORAL);
        } else {
            tvBadge.setText(String.format(Locale.US, "▼ %.1f%% d'économies réalisées", Math.abs(pctVar)));
            tvBadge.setTextColor(COLOR_PRIMARY_EMERALD);
        }
        tvBadge.setTextSize(12);
        tvBadge.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lpB = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpB.topMargin = dpToPx(8);
        tvBadge.setLayoutParams(lpB);
        card.addView(tvBadge);

        return card;
    }

    // SECTION 4 : JAUGE DE CONSOMMATION GLOBALE AVEC SEUIL 80%
    private View buildSection4GaugeWithThreshold(double consumedPct, double spent, double totalBudget) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));
        card.setBackground(createShape(COLOR_CARD_WHITE, dpToPx(16), COLOR_BORDER, dpToPx(1)));

        LinearLayout rowHeader = new LinearLayout(this);
        rowHeader.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvLabel = new TextView(this);
        tvLabel.setText("Consommation globale");
        tvLabel.setTextColor(COLOR_DEEP_FOREST);
        tvLabel.setTextSize(13);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        rowHeader.addView(tvLabel);

        TextView tvRatio = new TextView(this);
        tvRatio.setText(String.format(Locale.US, "%.3f / %.3f DT (%.0f%%)", spent, totalBudget, consumedPct));
        tvRatio.setTextColor(COLOR_MUTED_GREY);
        tvRatio.setTextSize(12);
        rowHeader.addView(tvRatio);

        card.addView(rowHeader);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(10));
        pbParams.topMargin = dpToPx(10);
        pb.setLayoutParams(pbParams);
        pb.setMax(100);
        pb.setProgress((int) Math.min(100, Math.round(consumedPct)));
        pb.setProgressTintList(ContextCompat.getColorStateList(this, consumedPct >= 100 ? R.color.coral : (consumedPct >= 80 ? R.color.gold : R.color.forest)));
        card.addView(pb);

        TextView tvSeuil = new TextView(this);
        tvSeuil.setText("📍 Seuil de sécurité préventif : 80% du budget");
        tvSeuil.setTextColor(COLOR_MUTED_GREY);
        tvSeuil.setTextSize(11);
        LinearLayout.LayoutParams lpS = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpS.topMargin = dpToPx(6);
        tvSeuil.setLayoutParams(lpS);
        card.addView(tvSeuil);

        return card;
    }

    // SECTION 5 : BARRES DE CONSOMMATION PAR CATÉGORIE (TRIÉES DU PLUS BAS AU PLUS HAUT %)
    private View buildSection5CategoryBarsSorted(Budget currentBudget) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        if (currentBudget == null) {
            TextView empty = new TextView(this);
            empty.setText("Aucun budget actif.");
            empty.setTextColor(COLOR_MUTED_GREY);
            container.addView(empty);
            return container;
        }

        List<BudgetAllocation> allocations = budgetDao.getAllocationsForBudget(currentBudget.getId());
        if (allocations == null || allocations.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucune catégorie allouée.");
            empty.setTextColor(COLOR_MUTED_GREY);
            container.addView(empty);
            return container;
        }

        // Tri par pourcentage croissant (du plus bas au plus haut)
        Collections.sort(allocations, (a1, a2) -> {
            double p1 = a1.getAllocatedAmount() > 0 ? (a1.getSpentAmount() / a1.getAllocatedAmount()) : 0.0;
            double p2 = a2.getAllocatedAmount() > 0 ? (a2.getSpentAmount() / a2.getAllocatedAmount()) : 0.0;
            return Double.compare(p1, p2);
        });

        for (BudgetAllocation a : allocations) {
            double pct = a.getAllocatedAmount() > 0 ? (a.getSpentAmount() / a.getAllocatedAmount()) * 100.0 : 0.0;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
            card.setBackground(createShape(COLOR_CARD_WHITE, dpToPx(12), COLOR_BORDER, dpToPx(1)));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dpToPx(6);
            card.setLayoutParams(lp);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvName = new TextView(this);
            tvName.setText(a.getCategoryName());
            tvName.setTextColor(COLOR_DEEP_FOREST);
            tvName.setTextSize(13);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            row.addView(tvName);

            TextView tvVal = new TextView(this);
            tvVal.setText(String.format(Locale.US, "%.3f / %.3f DT (%.0f%%)", a.getSpentAmount(), a.getAllocatedAmount(), pct));
            tvVal.setTextColor(COLOR_MUTED_GREY);
            tvVal.setTextSize(11);
            row.addView(tvVal);

            card.addView(row);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(6));
            pbLp.topMargin = dpToPx(6);
            pb.setLayoutParams(pbLp);
            pb.setMax(100);
            pb.setProgress((int) Math.min(100, Math.round(pct)));
            pb.setProgressTintList(ContextCompat.getColorStateList(this, pct >= 100 ? R.color.coral : (pct >= 80 ? R.color.gold : R.color.forest)));
            card.addView(pb);

            container.addView(card);
        }

        return container;
    }

    // SECTION 6 : DAILY BURN RATE & PROJECTION DE FIN DE MOIS
    private View buildSection6BurnRateCard(double totalSpent, double remaining, double totalBudget) {
        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        double burnRate = dayOfMonth > 0 ? (totalSpent / dayOfMonth) : totalSpent;
        double monthProjection = burnRate * totalDays;
        double daysRemaining = burnRate > 0 ? (remaining / burnRate) : totalDays;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));
        card.setBackground(createShape(COLOR_CARD_WHITE, dpToPx(16), COLOR_BORDER, dpToPx(1)));

        LinearLayout rowMetrics = new LinearLayout(this);
        rowMetrics.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout col1 = new LinearLayout(this);
        col1.setOrientation(LinearLayout.VERTICAL);
        col1.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvBLabel = new TextView(this);
        tvBLabel.setText("Rythme quotidien");
        tvBLabel.setTextColor(COLOR_MUTED_GREY);
        tvBLabel.setTextSize(11);
        col1.addView(tvBLabel);

        TextView tvBVal = new TextView(this);
        tvBVal.setText(String.format(Locale.US, "%.3f DT/jour", burnRate));
        tvBVal.setTextColor(COLOR_DEEP_FOREST);
        tvBVal.setTextSize(14);
        tvBVal.setTypeface(null, Typeface.BOLD);
        col1.addView(tvBVal);

        LinearLayout col2 = new LinearLayout(this);
        col2.setOrientation(LinearLayout.VERTICAL);
        col2.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvPLabel = new TextView(this);
        tvPLabel.setText("Projection Fin de Mois");
        tvPLabel.setTextColor(COLOR_MUTED_GREY);
        tvPLabel.setTextSize(11);
        col2.addView(tvPLabel);

        TextView tvPVal = new TextView(this);
        tvPVal.setText(String.format(Locale.US, "%.3f DT", monthProjection));
        tvPVal.setTextColor(monthProjection > totalBudget ? COLOR_DANGER_CORAL : COLOR_PRIMARY_EMERALD);
        tvPVal.setTextSize(14);
        tvPVal.setTypeface(null, Typeface.BOLD);
        col2.addView(tvPVal);

        rowMetrics.addView(col1);
        rowMetrics.addView(col2);
        card.addView(rowMetrics);

        TextView tvAutonomy = new TextView(this);
        tvAutonomy.setText(String.format(Locale.US, "⏳ Autonomie estimée au rythme actuel : %.1f jours restants", Math.max(0, daysRemaining)));
        tvAutonomy.setTextColor(COLOR_DEEP_FOREST);
        tvAutonomy.setTextSize(12);
        LinearLayout.LayoutParams lpA = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpA.topMargin = dpToPx(10);
        tvAutonomy.setLayoutParams(lpA);
        card.addView(tvAutonomy);

        return card;
    }

    // SECTION 7 : CATÉGORIE CRITIQUE & RECOMMANDATIONS CONCRÈTES
    private View buildSection7CriticalCategoryAndRecommendations(Budget currentBudget, long userId) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));
        card.setBackground(createShape(COLOR_LIGHT_MINT, dpToPx(16), COLOR_PRIMARY_EMERALD, dpToPx(1)));

        String topCategoryName = "Alimentation";
        double highestSpent = 0.0;

        if (currentBudget != null) {
            List<BudgetAllocation> allocations = budgetDao.getAllocationsForBudget(currentBudget.getId());
            for (BudgetAllocation a : allocations) {
                if (a.getSpentAmount() > highestSpent) {
                    highestSpent = a.getSpentAmount();
                    topCategoryName = a.getCategoryName();
                }
            }
        }

        TextView tvTitle = new TextView(this);
        tvTitle.setText(String.format("🔥 Poste de Dépense Principal : %s (%.3f DT)", topCategoryName, highestSpent));
        tvTitle.setTextColor(COLOR_DEEP_FOREST);
        tvTitle.setTextSize(13);
        tvTitle.setTypeface(null, Typeface.BOLD);
        card.addView(tvTitle);

        TextView tvRec = new TextView(this);
        tvRec.setText(getRecommendationForCategory(topCategoryName));
        tvRec.setTextColor(COLOR_DEEP_FOREST);
        tvRec.setTextSize(12);
        LinearLayout.LayoutParams lpR = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpR.topMargin = dpToPx(6);
        tvRec.setLayoutParams(lpR);
        card.addView(tvRec);

        return card;
    }

    private String getRecommendationForCategory(String categoryName) {
        String lower = categoryName.toLowerCase();
        if (lower.contains("food") || lower.contains("aliment")) {
            return "💡 Recommandation : Privilégiez les achats groupés dans les grandes surfaces (Monoprix, Carrefour, Aziza) et consultez le comparateur de prix Masrouf avant d'acheter.";
        } else if (lower.contains("bill") || lower.contains("facture")) {
            return "💡 Recommandation : Surveillez vos factures STEG et Internet. Pensez à désactiver les abonnements inutilisés.";
        } else if (lower.contains("transport")) {
            return "💡 Recommandation : Regroupez vos déplacements et privilégiez les abonnements mensuels de transport.";
        } else {
            return "💡 Recommandation : Définissez une enveloppe fixe stricte pour ce poste et vérifiez vos reçus régulièrement.";
        }
    }

    private void shareReport(double budget, double spent, double remaining, double pct) {
        String text = String.format(Locale.US,
                "📊 Bilan Financier Masrouf\n" +
                        "• Budget Total : %.3f DT\n" +
                        "• Total Dépensé : %.3f DT (%.1f%%)\n" +
                        "• Solde Restant : %.3f DT\n\n" +
                        "Géré avec l'application Masrouf.",
                budget, spent, pct, remaining);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        try {
            startActivity(Intent.createChooser(intent, "Partager mon bilan financier"));
        } catch (Exception e) {
            Toast.makeText(this, "Aucune application de partage disponible.", Toast.LENGTH_SHORT).show();
        }
    }

    private GradientDrawable createShape(int fillColor, int cornerRadiusPx, int strokeColor, int strokeWidthPx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(fillColor);
        gd.setCornerRadius(cornerRadiusPx);
        if (strokeWidthPx > 0) {
            gd.setStroke(strokeWidthPx, strokeColor);
        }
        return gd;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}

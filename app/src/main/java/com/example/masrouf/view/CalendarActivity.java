package com.example.masrouf.view;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.util.MillimesUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * RÔLE : Calendrier annuel et gestion des périodes budgétaires (Figma / Image 11).
 * EXIGENCES DU PROF COUVERTES :
 *   - VUE 100% EN CODE JAVA PUR SANS LAYOUT XML (Exigence n°3 & Matrice §4 ligne 3).
 *   - Gestion des périodes : Interdiction des mois passés, confirmation pour mois futurs (Exigence n°3).
 *   - Initialisation automatique des dates (1er au dernier jour du mois).
 *   - Synchronisation immédiate avec l'Accueil, Analyses et Produits via SessionManager.
 * COUCHE MVC : Vue (View).
 */
import com.example.masrouf.BaseActivity;

public class CalendarActivity extends BaseActivity {

    private static final String TAG = "CalendarActivity";

    private SessionManager sessionManager;
    private BudgetDao budgetDao;
    private TransactionDao transactionDao;

    private int selectedYear;
    private int selectedMonthIndex; // 1-12
    private String selectedMonthKey;

    private TextView tvYearDisplay;
    private LinearLayout gridContainer;
    private LinearLayout bottomDetailCard;
    private TextView tvBottomPeriodTitle;
    private TextView tvBottomDateRange;
    private TextView tvBottomBudgetInfo;
    private TextView tvBottomBadge;
    private Button btnBottomModify;

    private final String[] monthNames = new String[]{
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        budgetDao = new BudgetDao(this);
        transactionDao = new TransactionDao(this);

        Calendar cal = Calendar.getInstance();
        selectedYear = cal.get(Calendar.YEAR);
        selectedMonthIndex = cal.get(Calendar.MONTH) + 1; // 1 à 12
        selectedMonthKey = String.format(Locale.US, "%04d-%02d", selectedYear, selectedMonthIndex);

        // Si une période est déjà enregistrée dans la session, l'utiliser
        String savedKey = sessionManager.getSelectedMonthKey();
        if (savedKey != null && savedKey.contains("-")) {
            try {
                String[] parts = savedKey.split("-");
                selectedYear = Integer.parseInt(parts[0]);
                selectedMonthIndex = Integer.parseInt(parts[1]);
                selectedMonthKey = savedKey;
            } catch (Exception ignored) {}
        }

        View rootView = buildCalendarUI();
        setContentView(rootView);

        refreshCalendar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCalendar();
    }

    private View buildCalendarUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#FBF8F2"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(24));

        // 1. Barre supérieure : Menu, Titre, Bell, Profile
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(0, 0, 0, dpToPx(16));

        ImageView ivMenu = new ImageView(this);
        ivMenu.setImageResource(R.drawable.ic_arrow_back);
        ivMenu.setColorFilter(Color.parseColor("#102B27"));
        ivMenu.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)));
        ivMenu.setOnClickListener(v -> finish());
        topBar.addView(ivMenu);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Calendrier");
        tvTitle.setTextColor(Color.parseColor("#102B27"));
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams lpTitle = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpTitle.setMarginStart(dpToPx(12));
        tvTitle.setLayoutParams(lpTitle);
        topBar.addView(tvTitle);

        ImageView ivBell = new ImageView(this);
        ivBell.setImageResource(R.drawable.ic_bell);
        ivBell.setColorFilter(Color.parseColor("#102B27"));
        LinearLayout.LayoutParams lpBell = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
        lpBell.setMarginEnd(dpToPx(12));
        ivBell.setLayoutParams(lpBell);
        topBar.addView(ivBell);

        ImageView ivProfile = new ImageView(this);
        ivProfile.setImageResource(R.drawable.ic_profile);
        ivProfile.setColorFilter(Color.parseColor("#102B27"));
        ivProfile.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28)));
        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        topBar.addView(ivProfile);

        root.addView(topBar);

        // 2. Sous-titre : VOTRE ANNÉE FINANCIÈRE
        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("VOTRE ANNÉE FINANCIÈRE");
        tvSubtitle.setTextColor(Color.parseColor("#6B7C74"));
        tvSubtitle.setTextSize(11);
        tvSubtitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvSubtitle.setLetterSpacing(0.06f);
        root.addView(tvSubtitle);

        // 3. Sélecteur d'année : < 2026 >
        LinearLayout yearSelector = new LinearLayout(this);
        yearSelector.setOrientation(LinearLayout.HORIZONTAL);
        yearSelector.setGravity(Gravity.CENTER_VERTICAL);
        yearSelector.setPadding(0, dpToPx(12), 0, dpToPx(8));

        TextView btnPrevYear = new TextView(this);
        btnPrevYear.setText("‹");
        btnPrevYear.setTextSize(26);
        btnPrevYear.setTextColor(Color.parseColor("#102B27"));
        btnPrevYear.setPadding(dpToPx(8), 0, dpToPx(8), 0);
        btnPrevYear.setOnClickListener(v -> {
            selectedYear--;
            tvYearDisplay.setText(String.valueOf(selectedYear));
            refreshCalendar();
        });
        yearSelector.addView(btnPrevYear);

        tvYearDisplay = new TextView(this);
        tvYearDisplay.setText(String.valueOf(selectedYear));
        tvYearDisplay.setTextColor(Color.parseColor("#102B27"));
        tvYearDisplay.setTextSize(22);
        tvYearDisplay.setTypeface(Typeface.DEFAULT_BOLD);
        tvYearDisplay.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpYear = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvYearDisplay.setLayoutParams(lpYear);
        yearSelector.addView(tvYearDisplay);

        TextView btnNextYear = new TextView(this);
        btnNextYear.setText("›");
        btnNextYear.setTextSize(26);
        btnNextYear.setTextColor(Color.parseColor("#102B27"));
        btnNextYear.setPadding(dpToPx(8), 0, dpToPx(8), 0);
        btnNextYear.setOnClickListener(v -> {
            selectedYear++;
            tvYearDisplay.setText(String.valueOf(selectedYear));
            refreshCalendar();
        });
        yearSelector.addView(btnNextYear);

        root.addView(yearSelector);

        // 4. Instructions
        TextView tvInstruct = new TextView(this);
        tvInstruct.setText("Choisissez un mois pour explorer ses périodes de budget.");
        tvInstruct.setTextColor(Color.parseColor("#6B7C74"));
        tvInstruct.setTextSize(12);
        tvInstruct.setPadding(0, 0, 0, dpToPx(14));
        root.addView(tvInstruct);

        // 5. Grille des 12 mois (6 rangées de 2 colonnes)
        gridContainer = new LinearLayout(this);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(gridContainer);

        // 6. Section de détail en bas de page
        tvBottomPeriodTitle = new TextView(this);
        tvBottomPeriodTitle.setTextColor(Color.parseColor("#102B27"));
        tvBottomPeriodTitle.setTextSize(16);
        tvBottomPeriodTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvBottomPeriodTitle.setPadding(0, dpToPx(20), 0, dpToPx(10));
        root.addView(tvBottomPeriodTitle);

        bottomDetailCard = new LinearLayout(this);
        bottomDetailCard.setOrientation(LinearLayout.VERTICAL);
        bottomDetailCard.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dpToPx(14));
        cardBg.setStroke(dpToPx(1.5f), Color.parseColor("#1C5143"));
        bottomDetailCard.setBackground(cardBg);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout leftText = new LinearLayout(this);
        leftText.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lpLeft = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        leftText.setLayoutParams(lpLeft);

        tvBottomDateRange = new TextView(this);
        tvBottomDateRange.setTextColor(Color.parseColor("#102B27"));
        tvBottomDateRange.setTextSize(15);
        tvBottomDateRange.setTypeface(Typeface.DEFAULT_BOLD);
        leftText.addView(tvBottomDateRange);

        tvBottomBudgetInfo = new TextView(this);
        tvBottomBudgetInfo.setTextColor(Color.parseColor("#6B7C74"));
        tvBottomBudgetInfo.setTextSize(12);
        tvBottomBudgetInfo.setPadding(0, dpToPx(2), 0, 0);
        leftText.addView(tvBottomBudgetInfo);

        row1.addView(leftText);

        tvBottomBadge = new TextView(this);
        tvBottomBadge.setText("✓ Active");
        tvBottomBadge.setTextColor(Color.parseColor("#102B27"));
        tvBottomBadge.setTextSize(11);
        tvBottomBadge.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Color.parseColor("#D9F0E4"));
        badgeBg.setCornerRadius(dpToPx(10));
        tvBottomBadge.setBackground(badgeBg);
        tvBottomBadge.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));
        row1.addView(tvBottomBadge);

        bottomDetailCard.addView(row1);

        // Bouton modifier
        btnBottomModify = new Button(this);
        btnBottomModify.setText("modifier");
        btnBottomModify.setTextColor(Color.parseColor("#102B27"));
        btnBottomModify.setTextSize(13);
        btnBottomModify.setAllCaps(false);
        GradientDrawable modBg = new GradientDrawable();
        modBg.setColor(Color.parseColor("#EAEFEA"));
        modBg.setCornerRadius(dpToPx(8));
        btnBottomModify.setBackground(modBg);
        LinearLayout.LayoutParams lpMod = new LinearLayout.LayoutParams(dpToPx(90), dpToPx(36));
        lpMod.setMargins(0, dpToPx(12), 0, 0);
        btnBottomModify.setLayoutParams(lpMod);
        bottomDetailCard.addView(btnBottomModify);

        root.addView(bottomDetailCard);

        scroll.addView(root);
        return scroll;
    }

    private void refreshCalendar() {
        gridContainer.removeAllViews();
        long userId = sessionManager.getUserId();

        Calendar currentCal = Calendar.getInstance();
        int realCurrentYear = currentCal.get(Calendar.YEAR);
        int realCurrentMonth = currentCal.get(Calendar.MONTH) + 1;

        // Construction des 6 rangées de 2 cartes
        for (int row = 0; row < 6; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setPadding(0, 0, 0, dpToPx(10));

            for (int col = 0; col < 2; col++) {
                int monthIndex = (row * 2) + col + 1; // 1 à 12
                String monthName = monthNames[monthIndex - 1];
                String monthKey = String.format(Locale.US, "%04d-%02d", selectedYear, monthIndex);

                boolean isSelected = monthKey.equals(selectedMonthKey);
                boolean isPast = (selectedYear < realCurrentYear) || (selectedYear == realCurrentYear && monthIndex < realCurrentMonth);

                Budget budget = budgetDao.getBudgetForMonth(userId, monthKey);
                double spent = transactionDao.getTotalExpensesForMonth(userId, monthKey);

                View card = createMonthCard(monthName, monthKey, monthIndex, budget, spent, isSelected, isPast);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                if (col == 0) {
                    lp.setMarginEnd(dpToPx(10));
                }
                card.setLayoutParams(lp);
                rowLayout.addView(card);
            }
            gridContainer.addView(rowLayout);
        }

        // Mise à jour de la carte de détail inférieure
        updateBottomDetail(userId);
    }

    private View createMonthCard(String name, String monthKey, int monthIndex, Budget budget, double spent, boolean isSelected, boolean isPast) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(14));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextSize(14);
        tvName.setTypeface(Typeface.DEFAULT_BOLD);

        TextView tvAmount = new TextView(this);
        tvAmount.setTextSize(11);
        tvAmount.setPadding(0, dpToPx(2), 0, dpToPx(6));

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams lpPb = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(4));
        pb.setLayoutParams(lpPb);
        pb.setMax(100);

        if (isSelected) {
            bg.setColor(Color.parseColor("#102B27"));
            tvName.setTextColor(Color.WHITE);
            tvAmount.setTextColor(Color.parseColor("#D9F0E4"));
            tvAmount.setText(spent > 0 ? (int) spent + " DT" : "0 DT");
            pb.setProgress(budget != null && budget.getMontantTotal() > 0 ? (int) ((spent / budget.getMontantTotal()) * 100) : 10);
            pb.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#D9F0E4")));
        } else {
            bg.setColor(Color.WHITE);
            tvName.setTextColor(Color.parseColor("#102B27"));
            tvAmount.setTextColor(Color.parseColor("#6B7C74"));
            tvAmount.setText(spent > 0 ? (int) spent + " DT" : "0 DT");
            pb.setProgress(0);
            pb.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#EAEFEA")));
        }

        if (isPast) {
            card.setAlpha(0.6f);
        }

        card.setBackground(bg);
        card.addView(tvName);
        card.addView(tvAmount);
        card.addView(pb);

        card.setOnClickListener(v -> handleMonthClick(name, monthKey, monthIndex, isPast, budget));

        return card;
    }

    private void handleMonthClick(String name, String monthKey, int monthIndex, boolean isPast, Budget budget) {
        if (isPast) {
            Toast.makeText(this, "Impossible de gérer un mois passé. Seuls le mois courant et les mois futurs sont autorisés.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (budget != null) {
            // Mois avec budget existant -> Activer la période
            selectedMonthKey = monthKey;
            selectedMonthIndex = monthIndex;
            sessionManager.setSelectedMonthKey(monthKey);
            refreshCalendar();
        } else {
            // Mois futur sans budget -> Demander confirmation pour gérer cette période (Exigence n°3)
            new AlertDialog.Builder(this)
                    .setTitle("Gérer cette période ?")
                    .setMessage("Voulez-vous définir et allouer un budget pour " + name + " " + selectedYear + " ?")
                    .setPositiveButton("Confirmer", (dialog, which) -> {
                        selectedMonthKey = monthKey;
                        selectedMonthIndex = monthIndex;
                        sessionManager.setSelectedMonthKey(monthKey);

                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, selectedYear);
                        c.set(Calendar.MONTH, monthIndex - 1);
                        int maxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);

                        String startDate = String.format(Locale.US, "%04d-%02d-01", selectedYear, monthIndex);
                        String endDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, monthIndex, maxDay);

                        Intent intent = new Intent(this, CreateBudgetActivity.class);
                        intent.putExtra("EXTRA_START_DATE", startDate);
                        intent.putExtra("EXTRA_END_DATE", endDate);
                        intent.putExtra("EXTRA_MONTH_KEY", monthKey);
                        startActivity(intent);
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        }
    }

    private void updateBottomDetail(long userId) {
        String monthName = monthNames[selectedMonthIndex - 1];
        tvBottomPeriodTitle.setText(monthName + " " + selectedYear);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, selectedYear);
        c.set(Calendar.MONTH, selectedMonthIndex - 1);
        int maxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        String rangeStr = "01–" + maxDay + " " + monthName.substring(0, Math.min(3, monthName.length())).toUpperCase() + ". " + selectedYear;
        tvBottomDateRange.setText(rangeStr);

        Budget budget = budgetDao.getBudgetForMonth(userId, selectedMonthKey);
        double spent = transactionDao.getTotalExpensesForMonth(userId, selectedMonthKey);

        if (budget != null) {
            tvBottomBudgetInfo.setText((int) spent + " DT / " + (int) budget.getMontantTotal() + " DT");
            tvBottomBadge.setVisibility(View.VISIBLE);
            btnBottomModify.setText("modifier");
            btnBottomModify.setOnClickListener(v -> {
                Intent intent = new Intent(this, ModifyBudgetActivity.class);
                intent.putExtra("EXTRA_BUDGET_ID", budget.getId());
                startActivity(intent);
            });
        } else {
            tvBottomBudgetInfo.setText("Aucun budget alloué pour ce mois");
            tvBottomBadge.setVisibility(View.GONE);
            btnBottomModify.setText("Créer");
            btnBottomModify.setOnClickListener(v -> {
                String startDate = String.format(Locale.US, "%04d-%02d-01", selectedYear, selectedMonthIndex);
                String endDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonthIndex, maxDay);
                Intent intent = new Intent(this, CreateBudgetActivity.class);
                intent.putExtra("EXTRA_START_DATE", startDate);
                intent.putExtra("EXTRA_END_DATE", endDate);
                intent.putExtra("EXTRA_MONTH_KEY", selectedMonthKey);
                startActivity(intent);
            });
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}

package com.example.masrouf.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.GamificationDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.Achievement;
import com.example.masrouf.model.User;
import com.example.masrouf.model.UserPoints;

import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran de Gamification, Points, Niveaux, Streaks et Badges (Figma 21).
 * COUCHE MVC : Vue (View).
 */
public class PointsAndAchievementsActivity extends AppCompatActivity {

    private TextView tvGamificationTotalPoints;
    private TextView tvGamificationLevel;
    private TextView tvGamificationStreak;
    private TextView tvGamificationBonusNotice;
    private LinearLayout layoutBadgesContainer;
    private LinearLayout layoutLedgerContainer;

    private SessionManager sessionManager;
    private UserDao userDao;
    private GamificationDao gamificationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_and_achievements);

        sessionManager = new SessionManager(this);
        userDao = new UserDao(this);
        gamificationDao = new GamificationDao(this);

        initViews();
        loadGamificationData();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackPoints);
        ivBack.setOnClickListener(v -> finish());

        tvGamificationTotalPoints = findViewById(R.id.tvGamificationTotalPoints);
        tvGamificationLevel = findViewById(R.id.tvGamificationLevel);
        tvGamificationStreak = findViewById(R.id.tvGamificationStreak);
        tvGamificationBonusNotice = findViewById(R.id.tvGamificationBonusNotice);
        layoutBadgesContainer = findViewById(R.id.layoutBadgesContainer);
        layoutLedgerContainer = findViewById(R.id.layoutLedgerContainer);
    }

    public static String getRankTitle(int points) {
        if (points >= 2000) return "Sage Financier";
        if (points >= 1200) return "Stratège Familial";
        if (points >= 700) return "Maître du Budget";
        if (points >= 300) return "Gestionnaire Averti";
        return "Apprenti Économe";
    }

    private void loadGamificationData() {
        long userId = sessionManager.getUserId();
        User user = userDao.getUserById(userId);

        if (user != null) {
            int pts = user.getPoints();
            tvGamificationTotalPoints.setText(String.valueOf(pts));
            tvGamificationLevel.setText(getRankTitle(pts));
            tvGamificationStreak.setText(getString(R.string.gamification_streak_format, user.getStreakCount()));

            if (user.getStreakCount() >= 3) {
                tvGamificationBonusNotice.setVisibility(View.VISIBLE);
                tvGamificationBonusNotice.setText(R.string.gamification_streak_bonus);
            } else {
                tvGamificationBonusNotice.setVisibility(View.GONE);
            }
        }

        // 1. Charger les badges
        loadBadges(userId);

        // 2. Charger le journal des points
        loadLedger(userId);
    }

    private void loadBadges(long userId) {
        layoutBadgesContainer.removeAllViews();
        List<Achievement> achievements = gamificationDao.getAllAchievements(userId);

        String currentLang = sessionManager.getLanguage();

        for (Achievement ach : achievements) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(32, 24, 32, 24);

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.WHITE);
            gd.setCornerRadius(24);
            gd.setStroke(2, Color.parseColor("#E5E2DA"));
            card.setBackground(gd);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 16;
            card.setLayoutParams(lp);

            ImageView ivIcon = new ImageView(this);
            ivIcon.setLayoutParams(new LinearLayout.LayoutParams(64, 64));
            ivIcon.setImageResource(ach.isUnlocked() ? R.drawable.ic_check : R.drawable.ic_leaf);
            ivIcon.setColorFilter(ach.isUnlocked() ? Color.parseColor("#1C5143") : Color.parseColor("#A0ADA7"));
            card.addView(ivIcon);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            textCol.setPadding(24, 0, 16, 0);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(ach.getLocalizedTitle(currentLang));
            tvTitle.setTextColor(Color.parseColor("#111A16"));
            tvTitle.setTextSize(15);
            tvTitle.setTypeface(null, Typeface.BOLD);
            textCol.addView(tvTitle);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(ach.getLocalizedDesc(currentLang));
            tvDesc.setTextColor(Color.parseColor("#6B7C74"));
            tvDesc.setTextSize(12);
            textCol.addView(tvDesc);

            card.addView(textCol);

            TextView tvStatus = new TextView(this);
            if (ach.isUnlocked()) {
                tvStatus.setText(R.string.gamification_badge_unlocked);
                tvStatus.setTextColor(Color.parseColor("#1C5143"));
                tvStatus.setTypeface(null, Typeface.BOLD);
            } else {
                tvStatus.setText(getString(R.string.gamification_badge_locked, ach.getPointsThreshold()));
                tvStatus.setTextColor(Color.parseColor("#A0ADA7"));
            }
            tvStatus.setTextSize(11);
            card.addView(tvStatus);

            layoutBadgesContainer.addView(card);
        }
    }

    private void loadLedger(long userId) {
        layoutLedgerContainer.removeAllViews();
        List<UserPoints> ledger = gamificationDao.getUserPointsLedger(userId);

        for (UserPoints up : ledger) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(28, 20, 28, 20);

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.WHITE);
            gd.setCornerRadius(20);
            row.setBackground(gd);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 12;
            row.setLayoutParams(lp);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            TextView tvReason = new TextView(this);
            tvReason.setText(up.getReason());
            tvReason.setTextColor(Color.parseColor("#111A16"));
            tvReason.setTextSize(14);
            tvReason.setTypeface(null, Typeface.BOLD);
            textCol.addView(tvReason);

            TextView tvDate = new TextView(this);
            tvDate.setText(up.getEarnedAt());
            tvDate.setTextColor(Color.parseColor("#6B7C74"));
            tvDate.setTextSize(11);
            textCol.addView(tvDate);

            row.addView(textCol);

            TextView tvPoints = new TextView(this);
            tvPoints.setText("+" + up.getPoints() + " pts");
            tvPoints.setTextColor(Color.parseColor("#1C5143"));
            tvPoints.setTextSize(15);
            tvPoints.setTypeface(null, Typeface.BOLD);
            row.addView(tvPoints);

            layoutLedgerContainer.addView(row);
        }
    }

    /**
     * Modal de célébration lors du respect d'un budget mensuel (Exigence n°8).
     */
    public static void showCelebrationDialog(Activity activity, double savingsAmount, Runnable onContinue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(48, 48, 48, 48);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#112B28"));
        gd.setCornerRadius(32);
        card.setBackground(gd);

        ImageView trophy = new ImageView(activity);
        trophy.setLayoutParams(new LinearLayout.LayoutParams(144, 144));
        trophy.setImageResource(R.drawable.ic_trophy);
        trophy.setColorFilter(Color.parseColor("#E9B66B"));
        card.addView(trophy);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("🎉 Félicitations !");
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(22);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setPadding(0, 24, 0, 12);
        card.addView(tvTitle);

        TextView tvSub = new TextView(activity);
        tvSub.setText("Vous avez respecté votre budget mensuel et réalisé une économie de :");
        tvSub.setTextColor(Color.parseColor("#D9EEDD"));
        tvSub.setTextSize(14);
        tvSub.setGravity(Gravity.CENTER);
        card.addView(tvSub);

        TextView tvSavings = new TextView(activity);
        tvSavings.setText(String.format(Locale.US, "%.3f DT", savingsAmount));
        tvSavings.setTextColor(Color.parseColor("#D9EEDD"));
        tvSavings.setTextSize(28);
        tvSavings.setTypeface(null, Typeface.BOLD);
        tvSavings.setPadding(0, 16, 0, 16);
        card.addView(tvSavings);

        TextView tvBonus = new TextView(activity);
        tvBonus.setText("+ 100 Points attribués à votre profil !");
        tvBonus.setTextColor(Color.parseColor("#E9B66B"));
        tvBonus.setTextSize(14);
        tvBonus.setTypeface(null, Typeface.BOLD);
        card.addView(tvBonus);

        Button btnAction = new Button(activity);
        btnAction.setText("Continuer vers le mois suivant");
        btnAction.setTextColor(Color.WHITE);
        btnAction.setTypeface(null, Typeface.BOLD);

        GradientDrawable bgBtn = new GradientDrawable();
        bgBtn.setColor(Color.parseColor("#2E7D43"));
        bgBtn.setCornerRadius(20);
        btnAction.setBackground(bgBtn);

        LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 100);
        lpBtn.topMargin = 32;
        btnAction.setLayoutParams(lpBtn);

        builder.setView(card);
        AlertDialog dialog = builder.create();

        btnAction.setOnClickListener(v -> {
            dialog.dismiss();
            if (onContinue != null) onContinue.run();
        });

        dialog.show();
    }
}

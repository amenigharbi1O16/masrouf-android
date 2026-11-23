package com.example.masrouf.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.NotificationDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.AppNotification;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran d'affichage des notifications budgétaires et familiales pour tous les profils (Parent, User, Child).
 * COUCHE MVC : Vue (View).
 * 
 * Design conforme :
 * - Cartes de notifications blanches avec coins arrondis 16dp.
 * - Icônes et couleurs adaptées au type (Menthe, Orange warning, Corail dépassement, Bleu bienvenue).
 * - "Tout marquer comme lu" avec persistance SQLite et mise à jour des compteurs.
 * - État vide : "Vous êtes à jour, aucune notification pour le moment".
 */
import com.example.masrouf.BaseActivity;

public class NotificationsActivity extends BaseActivity {

    private ImageView ivNotifBack;
    private ImageView ivNotifProfile;
    private TextView tvNotifUnreadCount;
    private TextView btnMarkAllRead;
    private LinearLayout containerNotifications;

    private SessionManager sessionManager;
    private NotificationDao notificationDao;
    private UserDao userDao;
    private BudgetDao budgetDao;

    private long userId = -1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        sessionManager = new SessionManager(this);
        notificationDao = new NotificationDao(this);
        userDao = new UserDao(this);
        budgetDao = new BudgetDao(this);

        userId = sessionManager.getUserId();

        ivNotifBack = findViewById(R.id.ivNotifBack);
        ivNotifProfile = findViewById(R.id.ivNotifProfile);
        tvNotifUnreadCount = findViewById(R.id.tvNotifUnreadCount);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        containerNotifications = findViewById(R.id.containerNotifications);

        if (ivNotifBack != null) {
            ivNotifBack.setOnClickListener(v -> finish());
        }

        if (ivNotifProfile != null) {
            ivNotifProfile.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }

        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> {
                notificationDao.markAllAsReadForUser(userId);
                loadNotifications();
                Toast.makeText(this, "Toutes les notifications sont marquées comme lues", Toast.LENGTH_SHORT).show();
            });
        }

        ensureInitialNotifications();
        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    /**
     * Génère les notifications initiales réelles du compte si aucune notification n'existe encore.
     */
    private void ensureInitialNotifications() {
        if (userId <= 0) return;
        List<AppNotification> list = notificationDao.getNotificationsForUser(userId);
        if (list.isEmpty()) {
            User current = userDao.getUserById(userId);
            if (current == null) return;

            if (current.isChild()) {
                notificationDao.insertNotification(userId,
                        "Bienvenue sur votre compte Enfant !",
                        "Votre compte Masrouf Enfant a été créé par votre parent. Vous pouvez répartir votre budget entre vos catégories.",
                        AppNotification.TYPE_WELCOME,
                        AppNotification.ROUTE_BUDGET);

                long balanceMilli = current.getBalanceMillimes();
                if (balanceMilli > 0) {
                    notificationDao.insertNotification(userId,
                            "Budget initial attribué par votre Parent",
                            "Un budget de " + MillimesUtil.formatMillimes(balanceMilli) + " vous a été attribué par votre parent.",
                            AppNotification.TYPE_PARENT_BUDGET_ASSIGNED,
                            AppNotification.ROUTE_BUDGET);
                }
            } else {
                notificationDao.insertNotification(userId,
                        "Bienvenue sur Masrouf",
                        "Bienvenue dans votre espace famille. Commencez par sélectionner vos catégories de dépenses familiales.",
                        AppNotification.TYPE_WELCOME,
                        "ROUTE_CATEGORIES");

                Budget currentBudget = budgetDao.getCurrentBudget(userId);
                if (currentBudget != null && currentBudget.getTotalAmount() > 0) {
                    notificationDao.insertNotification(userId,
                            "Période budgétaire active",
                            "Période en cours : " + currentBudget.getStartDate() + " – " + currentBudget.getEndDate() + " (Budget : " + String.format(Locale.US, "%.3f DT", currentBudget.getTotalAmount()) + ")",
                            AppNotification.TYPE_BUDGET_INITIALIZED,
                            AppNotification.ROUTE_BUDGET);
                }
            }
        }
    }

    private void loadNotifications() {
        if (containerNotifications == null) return;
        containerNotifications.removeAllViews();

        List<AppNotification> list = notificationDao.getNotificationsForUser(userId);
        int unreadCount = notificationDao.getUnreadCount(userId);

        if (tvNotifUnreadCount != null) {
            tvNotifUnreadCount.setText(unreadCount + " non lue(s)");
        }

        if (list.isEmpty()) {
            renderEmptyState();
            return;
        }

        for (final AppNotification notif : list) {
            View card = renderNotificationCard(notif);
            containerNotifications.addView(card);
        }
    }

    private void renderEmptyState() {
        LinearLayout empty = new LinearLayout(this);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(40, 80, 40, 80);

        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new LinearLayout.LayoutParams(96, 96));
        iv.setImageResource(R.drawable.ic_bell);
        iv.setColorFilter(Color.parseColor("#A8D5A0"));
        empty.addView(iv);

        TextView tv = new TextView(this);
        tv.setText("Vous êtes à jour, aucune notification pour le moment.");
        tv.setTextColor(Color.parseColor("#61746D"));
        tv.setTextSize(14);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 16, 0, 0);
        empty.addView(tv);

        containerNotifications.addView(empty);
    }

    private View renderNotificationCard(final AppNotification notif) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 16);
        card.setLayoutParams(lp);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_input_card);
        card.setPadding(24, 24, 24, 24);

        // Header de carte (Icône + Titre + Badge Nouveau)
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView ivIcon = new ImageView(this);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(72, 72);
        ivIcon.setLayoutParams(iconLp);
        ivIcon.setPadding(14, 14, 14, 14);

        // Palette visuelle adaptée au type (Image 4 : icône feuille verte dans cercle menthe)
        if (AppNotification.TYPE_WELCOME.equals(notif.getType())) {
            ivIcon.setBackgroundResource(R.drawable.bg_circle_mint);
            ivIcon.setImageResource(R.drawable.ic_leaf);
            ivIcon.setColorFilter(Color.parseColor("#112B28"));
        } else if (AppNotification.TYPE_EXCEEDED_100.equals(notif.getType())) {
            ivIcon.setBackgroundResource(R.drawable.bg_circle_gray);
            ivIcon.setImageResource(R.drawable.ic_info);
            ivIcon.setColorFilter(Color.parseColor("#DC7770"));
        } else if (AppNotification.TYPE_WARNING_80.equals(notif.getType())) {
            ivIcon.setBackgroundResource(R.drawable.bg_circle_mint);
            ivIcon.setImageResource(R.drawable.ic_info);
            ivIcon.setColorFilter(Color.parseColor("#E9B66B"));
        } else if (AppNotification.TYPE_PARENT_BUDGET_ASSIGNED.equals(notif.getType())) {
            ivIcon.setBackgroundResource(R.drawable.bg_circle_mint);
            ivIcon.setImageResource(R.drawable.ic_wallet);
            ivIcon.setColorFilter(Color.parseColor("#112B28"));
        } else {
            ivIcon.setBackgroundResource(R.drawable.bg_circle_mint);
            ivIcon.setImageResource(R.drawable.ic_receipt);
            ivIcon.setColorFilter(Color.parseColor("#112B28"));
        }

        topRow.addView(ivIcon);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        tLp.setMargins(16, 0, 16, 0);
        textLayout.setLayoutParams(tLp);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(notif.getTitle());
        tvTitle.setTextColor(Color.parseColor("#112B28"));
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, Typeface.BOLD);
        textLayout.addView(tvTitle);

        topRow.addView(textLayout);

        if (!notif.isRead()) {
            TextView badge = new TextView(this);
            badge.setText("Nouveau");
            badge.setTextColor(Color.WHITE);
            badge.setTextSize(10);
            badge.setTypeface(null, Typeface.BOLD);
            badge.setBackgroundResource(R.drawable.bg_badge_new);
            badge.setPadding(20, 6, 20, 6);
            topRow.addView(badge);
        }

        card.addView(topRow);

        // Corps du message
        TextView tvMsg = new TextView(this);
        tvMsg.setText(notif.getMessage());
        tvMsg.setTextColor(Color.parseColor("#112B28"));
        tvMsg.setTextSize(13);
        tvMsg.setPadding(0, 14, 0, 0);
        card.addView(tvMsg);

        // Encadré Action recommandée (Image 4)
        if (AppNotification.TYPE_WELCOME.equals(notif.getType()) || "ROUTE_CATEGORIES".equals(notif.getActionRoute())) {
            LinearLayout boxRec = new LinearLayout(this);
            boxRec.setOrientation(LinearLayout.VERTICAL);
            boxRec.setBackgroundResource(R.drawable.bg_card_cream);
            boxRec.setPadding(20, 16, 20, 16);
            LinearLayout.LayoutParams lpBox = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpBox.topMargin = 16;
            boxRec.setLayoutParams(lpBox);

            TextView tvRecTitle = new TextView(this);
            tvRecTitle.setText("Action recommandée :");
            tvRecTitle.setTextColor(Color.parseColor("#112B28"));
            tvRecTitle.setTextSize(12);
            tvRecTitle.setTypeface(null, Typeface.BOLD);
            boxRec.addView(tvRecTitle);

            TextView tvRecDesc = new TextView(this);
            tvRecDesc.setText("Finalisez le choix de vos catégories familiales.");
            tvRecDesc.setTextColor(Color.parseColor("#112B28"));
            tvRecDesc.setTextSize(13);
            tvRecDesc.setPadding(0, 4, 0, 0);
            boxRec.addView(tvRecDesc);

            card.addView(boxRec);
        }

        // Timestamp aligné en bas à droite (Image 4: 23 nov., 10:20 AM)
        String timeStr = formatTime(notif.getTimestamp());
        TextView tvTime = new TextView(this);
        tvTime.setText(timeStr);
        tvTime.setTextColor(Color.parseColor("#61746D"));
        tvTime.setTextSize(11);
        tvTime.setGravity(Gravity.END);
        tvTime.setPadding(0, 12, 0, 0);
        card.addView(tvTime);

        // Clic sur la carte marque comme lue et redirige vers l'écran concerné
        card.setOnClickListener(v -> {
            if (!notif.isRead()) {
                notificationDao.markAsRead(notif.getId());
                loadNotifications();
            }

            if ("ROUTE_CATEGORIES".equals(notif.getActionRoute())) {
                startActivity(new Intent(NotificationsActivity.this, OnboardingCategoriesActivity.class));
            } else if (AppNotification.ROUTE_ANALYTICS.equals(notif.getActionRoute())) {
                startActivity(new Intent(NotificationsActivity.this, AnalyticsActivity.class));
            } else if (AppNotification.ROUTE_BUDGET.equals(notif.getActionRoute())) {
                startActivity(new Intent(NotificationsActivity.this, MainActivity.class));
            }
        });

        return card;
    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0) timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM., hh:mm a", Locale.FRENCH);
        return sdf.format(new Date(timestamp));
    }
}

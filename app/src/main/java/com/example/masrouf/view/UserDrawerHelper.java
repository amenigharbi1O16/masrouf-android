package com.example.masrouf.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.masrouf.AnalysesActivity;
import com.example.masrouf.LoginActivity;
import com.example.masrouf.MainActivity;
import com.example.masrouf.ProductsActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.NotificationDao;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.view.child.ChildMainActivity;

public class UserDrawerHelper {

    public enum Section {
        HOME, PRODUCTS, CALENDAR, ANALYTICS, POINTS, NOTIFICATIONS, SETTINGS
    }

    public static void setupDrawer(final Activity activity, final DrawerLayout drawerLayout,
            final Section activeSection) {
        if (activity == null || drawerLayout == null)
            return;

        View drawerContent = drawerLayout.findViewById(R.id.sideDrawer);
        if (drawerContent == null)
            drawerContent = drawerLayout.findViewById(R.id.containerAdminDrawer);
        if (drawerContent == null)
            return;

        final SessionManager sessionManager = new SessionManager(activity);
        final NotificationDao notificationDao = new NotificationDao(activity);

        int displayWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int targetWidth = (int) (displayWidth * 0.82);
        int minWidth = (int) (260 * activity.getResources().getDisplayMetrics().density);
        int maxWidth = (int) (340 * activity.getResources().getDisplayMetrics().density);
        int finalWidth = Math.max(minWidth, Math.min(targetWidth, maxWidth));

        drawerContent.getLayoutParams().width = finalWidth;
        drawerContent.requestLayout();

        drawerLayout.setScrimColor(Color.argb(76, 0, 0, 0));

        TextView tvUserName = drawerContent.findViewById(R.id.tvDrawerUserName);
        if (tvUserName == null)
            tvUserName = drawerContent.findViewById(R.id.tvDrawerParentName);
        if (tvUserName == null)
            tvUserName = drawerContent.findViewById(R.id.drawerName);

        TextView tvUserEmail = drawerContent.findViewById(R.id.tvDrawerUserEmail);
        if (tvUserEmail == null)
            tvUserEmail = drawerContent.findViewById(R.id.tvDrawerParentEmail);

        TextView tvRoleBadge = drawerContent.findViewById(R.id.tvDrawerUserRoleBadge);

        String currentUserName = sessionManager.getUserName();
        String currentUserEmail = sessionManager.getUserEmail();

        if (tvUserName != null) {
            tvUserName.setText(currentUserName != null && !currentUserName.isEmpty() ? currentUserName : "Utilisateur");
        }
        if (tvUserEmail != null) {
            tvUserEmail.setText(
                    currentUserEmail != null && !currentUserEmail.isEmpty() ? currentUserEmail : "user@masrouf.tn");
        }

        if (tvRoleBadge != null) {
            tvRoleBadge.setText(sessionManager.isChild() ? "COMPTE ENFANT" : "COMPTE PERSONNEL");
        }

        TextView tvNotifBadge = drawerContent.findViewById(R.id.navNotifBadge);
        long userId = sessionManager.getUserId();
        int unreadCount = notificationDao.getUnreadCount(userId);

        if (tvNotifBadge != null) {
            if (unreadCount > 0) {
                tvNotifBadge.setVisibility(View.VISIBLE);
                tvNotifBadge.setText(String.valueOf(unreadCount));
            } else {
                tvNotifBadge.setVisibility(View.GONE);
            }
        }

        // Section clicks
        bindSectionClick(drawerContent, R.id.btnDrawerHome, R.id.navHome, activity,
                sessionManager.isChild() ? ChildMainActivity.class : MainActivity.class,
                drawerLayout);
        bindSectionClick(drawerContent, R.id.btnDrawerProducts, R.id.navProducts, activity, ProductsActivity.class,
                drawerLayout);
        bindSectionClick(drawerContent, R.id.btnDrawerCalendar, R.id.navCalendar, activity, CalendarActivity.class,
                drawerLayout);
        bindSectionClick(drawerContent, R.id.btnDrawerAnalytics, R.id.navAnalyses, activity, AnalysesActivity.class,
                drawerLayout);
        bindSectionClick(drawerContent, R.id.navPoints, 0, activity,
                PointsAndAchievementsActivity.class, drawerLayout);
        bindSectionClick(drawerContent, R.id.navNotifs, 0, activity,
                NotificationsActivity.class, drawerLayout);
        bindSectionClick(drawerContent, R.id.btnDrawerSettings, R.id.navSettings, activity, SettingsActivity.class,
                drawerLayout);

        // Language pills
        TextView btnLangFr = drawerContent.findViewById(R.id.btnDrawerLangFr);
        if (btnLangFr == null)
            btnLangFr = drawerContent.findViewById(R.id.langFr);
        TextView btnLangAr = drawerContent.findViewById(R.id.btnDrawerLangAr);
        if (btnLangAr == null)
            btnLangAr = drawerContent.findViewById(R.id.langAr);
        TextView btnLangEn = drawerContent.findViewById(R.id.btnDrawerLangEn);
        if (btnLangEn == null)
            btnLangEn = drawerContent.findViewById(R.id.langEn);

        String currentLang = sessionManager.getLanguage();
        updateLanguagePillStyles(btnLangFr, btnLangAr, btnLangEn, currentLang);

        final TextView finalFr = btnLangFr;
        final TextView finalAr = btnLangAr;
        final TextView finalEn = btnLangEn;
        if (finalFr != null)
            finalFr.setOnClickListener(v -> switchLanguage(activity, "fr", sessionManager, drawerLayout));
        if (finalAr != null)
            finalAr.setOnClickListener(v -> switchLanguage(activity, "ar", sessionManager, drawerLayout));
        if (finalEn != null)
            finalEn.setOnClickListener(v -> switchLanguage(activity, "en", sessionManager, drawerLayout));

        // Logout
        View btnLogout = drawerContent.findViewById(R.id.btnDrawerLogout);
        if (btnLogout == null)
            btnLogout = drawerContent.findViewById(R.id.navLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                sessionManager.clearSession();
                Toast.makeText(activity, "Déconnexion réussie.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            });
        }
    }

    private static void bindSectionClick(final View parentView, int primaryId, int fallbackId,
            final Activity currentActivity, final Class<?> targetClass,
            final DrawerLayout drawerLayout) {
        View btn = parentView.findViewById(primaryId);
        if (btn == null && fallbackId != 0)
            btn = parentView.findViewById(fallbackId);
        if (btn == null)
            return;

        btn.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            if (!currentActivity.getClass().equals(targetClass)) {
                Intent intent = new Intent(currentActivity, targetClass);
                currentActivity.startActivity(intent);
            }
        });
    }

    private static void updateLanguagePillStyles(TextView btnFr, TextView btnAr, TextView btnEn, String currentLang) {
        if (btnFr == null || btnAr == null || btnEn == null)
            return;

        btnFr.setBackgroundResource(
                "fr".equalsIgnoreCase(currentLang) ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);
        btnFr.setTextColor("fr".equalsIgnoreCase(currentLang) ? Color.parseColor("#112B28") : Color.WHITE);

        btnAr.setBackgroundResource(
                "ar".equalsIgnoreCase(currentLang) ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);
        btnAr.setTextColor("ar".equalsIgnoreCase(currentLang) ? Color.parseColor("#112B28") : Color.WHITE);

        btnEn.setBackgroundResource(
                "en".equalsIgnoreCase(currentLang) ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);
        btnEn.setTextColor("en".equalsIgnoreCase(currentLang) ? Color.parseColor("#112B28") : Color.WHITE);
    }

    private static void switchLanguage(Activity activity, String newLang, SessionManager sessionManager,
            DrawerLayout drawerLayout) {
        if (sessionManager.getLanguage().equalsIgnoreCase(newLang))
            return;

        sessionManager.setLanguage(newLang);
        LocaleHelper.setLocale(activity, newLang);

        drawerLayout.closeDrawer(GravityCompat.START);
        activity.recreate();
    }
}

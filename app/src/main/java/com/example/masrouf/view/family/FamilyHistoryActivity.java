package com.example.masrouf.view.family;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.FamilyAllocation;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * RÔLE : Journal chronologique complet des mouvements financiers de la famille
 * (allocations parent-enfant et dépenses enregistrées).
 * COUCHE MVC : Vue (View).
 */
public class FamilyHistoryActivity extends AppCompatActivity {

    private ImageView ivBackHistory;
    private LinearLayout layoutHistoryList;

    private FamilyDao familyDao;
    private UserDao userDao;
    private SessionManager sessionManager;
    private long familyId = 1;

    // Entité légère pour trier ensemble allocations et dépenses
    private static class HistoryItem {
        String title;
        String subtitle;
        String date;
        long amountMillimes;
        boolean isAllocation; // true = allocation (vert), false = dépense (rouge)

        HistoryItem(String title, String subtitle, String date, long amountMillimes, boolean isAllocation) {
            this.title = title;
            this.subtitle = subtitle;
            this.date = date;
            this.amountMillimes = amountMillimes;
            this.isAllocation = isAllocation;
        }
    }

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
        setContentView(R.layout.activity_family_history);

        familyDao = new FamilyDao(this);
        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);
        familyId = sessionManager.getFamilyId();

        ivBackHistory = findViewById(R.id.ivBackHistory);
        layoutHistoryList = findViewById(R.id.layoutHistoryList);

        ivBackHistory.setOnClickListener(v -> finish());

        loadHistory();
    }

    private void loadHistory() {
        layoutHistoryList.removeAllViews();
        List<HistoryItem> items = new ArrayList<>();

        // 1. Récupérer les allocations
        List<FamilyAllocation> allocs = familyDao.getAllocations(familyId);
        for (FamilyAllocation a : allocs) {
            items.add(new HistoryItem(
                    "Allocation à " + a.getChildName(),
                    a.getDescription() != null ? a.getDescription() : "Argent de poche",
                    a.getDate(),
                    a.getAmountMillimes(),
                    true
            ));
        }

        // 2. Récupérer les dépenses des enfants
        List<User> children = userDao.getChildrenByFamily(familyId);
        for (User c : children) {
            List<Transaction> transactions = familyDao.getChildTransactions(c.getId());
            for (Transaction t : transactions) {
                items.add(new HistoryItem(
                        c.getFullName() + " · " + t.getTitle(),
                        (t.getCategoryName() != null ? t.getCategoryName() : "Dépense") + (t.getStore() != null && !t.getStore().isEmpty() ? " · " + t.getStore() : ""),
                        t.getDate(),
                        t.getAmountMillimes(),
                        false
                ));
            }
        }

        // Trier par date décroissante
        Collections.sort(items, (o1, o2) -> {
            if (o1.date == null) return 1;
            if (o2.date == null) return -1;
            return o2.date.compareTo(o1.date);
        });

        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucune opération dans l'historique familial.");
            empty.setTextColor(Color.parseColor("#6B7C74"));
            empty.setPadding(0, 24, 0, 24);
            layoutHistoryList.addView(empty);
            return;
        }

        for (HistoryItem item : items) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundResource(R.drawable.bg_input_card);
            card.setPadding(32, 24, 32, 24);
            card.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 14;
            card.setLayoutParams(lp);

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(64, 64));
            iv.setBackgroundResource(R.drawable.bg_circle_mint);
            iv.setImageResource(item.isAllocation ? R.drawable.ic_wallet : R.drawable.ic_receipt);
            iv.setPadding(14, 14, 14, 14);
            iv.setColorFilter(Color.parseColor("#102B27"));
            card.addView(iv);

            LinearLayout details = new LinearLayout(this);
            details.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            // Définit les marges du bloc de détails : 20px à gauche pour séparer de l'icône
            dLp.setMargins(20, 0, 0, 0);
            details.setLayoutParams(dLp);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(item.title);
            tvTitle.setTextSize(14);
            tvTitle.setTextColor(Color.parseColor("#102B27"));
            tvTitle.setTypeface(null, Typeface.BOLD);
            details.addView(tvTitle);

            TextView tvSub = new TextView(this);
            tvSub.setText(item.date + " · " + item.subtitle);
            tvSub.setTextSize(12);
            tvSub.setTextColor(Color.parseColor("#6B7C74"));
            details.addView(tvSub);

            card.addView(details);

            TextView tvAmount = new TextView(this);
            if (item.isAllocation) {
                tvAmount.setText("+" + MillimesUtil.formatMillimes(item.amountMillimes));
                tvAmount.setTextColor(Color.parseColor("#2D7D68"));
            } else {
                tvAmount.setText("-" + MillimesUtil.formatMillimes(item.amountMillimes));
                tvAmount.setTextColor(Color.parseColor("#DE7363"));
            }
            tvAmount.setTextSize(15);
            tvAmount.setTypeface(null, Typeface.BOLD);
            card.addView(tvAmount);

            layoutHistoryList.addView(card);
        }
    }
}

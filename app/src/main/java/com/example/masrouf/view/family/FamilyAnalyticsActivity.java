package com.example.masrouf.view.family;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RÔLE : Analyses et statistiques de dépenses des enfants au sein de la famille.
 * COUCHE MVC : Vue (View).
 */
public class FamilyAnalyticsActivity extends AppCompatActivity {

    private ImageView ivBackAnalytics;
    private LinearLayout layoutAnalyticsByChild;
    private LinearLayout layoutAnalyticsByCat;

    private FamilyDao familyDao;
    private UserDao userDao;
    private SessionManager sessionManager;
    private long familyId = 1;

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
        sessionManager = new SessionManager(this);
        familyId = sessionManager.getFamilyId();

        ivBackAnalytics = findViewById(R.id.ivBackAnalytics);
        layoutAnalyticsByChild = findViewById(R.id.layoutAnalyticsByChild);
        layoutAnalyticsByCat = findViewById(R.id.layoutAnalyticsByCat);

        ivBackAnalytics.setOnClickListener(v -> finish());

        loadAnalytics();
    }

    private void loadAnalytics() {
        layoutAnalyticsByChild.removeAllViews();
        layoutAnalyticsByCat.removeAllViews();

        List<User> children = userDao.getChildrenByFamily(familyId);
        if (children.isEmpty()) {
            children = userDao.getChildrenByParent(sessionManager.getUserId());
        }

        long totalFamilyChildSpent = 0;
        Map<Long, Long> spentByChild = new HashMap<>();
        Map<String, Long> spentByCat = new HashMap<>();

        for (User c : children) {
            long spent = familyDao.getChildMonthlySpendingMillimes(c.getId(), "2026-11");
            spentByChild.put(c.getId(), spent);
            totalFamilyChildSpent += spent;

            List<Transaction> transactions = familyDao.getChildTransactions(c.getId());
            for (Transaction t : transactions) {
                String cat = t.getCategoryName() != null ? t.getCategoryName() : "Autres";
                long cur = spentByCat.containsKey(cat) ? spentByCat.get(cat) : 0;
                spentByCat.put(cat, cur + t.getAmountMillimes());
            }
        }

        // 1. Barres par enfant
        for (User c : children) {
            long s = spentByChild.containsKey(c.getId()) ? spentByChild.get(c.getId()) : 0;
            int pct = totalFamilyChildSpent > 0 ? (int) ((s * 100) / totalFamilyChildSpent) : 0;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_input_card);
            card.setPadding(32, 24, 32, 24);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 14;
            card.setLayoutParams(lp);

            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvName = new TextView(this);
            tvName.setText(c.getFullName());
            tvName.setTextSize(15);
            tvName.setTextColor(Color.parseColor("#102B27"));
            tvName.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvName.setLayoutParams(nLp);
            top.addView(tvName);

            TextView tvAmt = new TextView(this);
            tvAmt.setText(MillimesUtil.formatMillimes(s) + " (" + pct + "%)");
            tvAmt.setTextSize(14);
            tvAmt.setTextColor(Color.parseColor("#102B27"));
            tvAmt.setTypeface(null, Typeface.BOLD);
            top.addView(tvAmt);

            card.addView(top);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100);
            pb.setProgress(pct);
            pb.setPadding(0, 12, 0, 0);
            card.addView(pb);

            layoutAnalyticsByChild.addView(card);
        }

        // 2. Barres par catégorie
        for (Map.Entry<String, Long> entry : spentByCat.entrySet()) {
            int pct = totalFamilyChildSpent > 0 ? (int) ((entry.getValue() * 100) / totalFamilyChildSpent) : 0;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_input_card);
            card.setPadding(32, 24, 32, 24);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 14;
            card.setLayoutParams(lp);

            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvCat = new TextView(this);
            tvCat.setText(entry.getKey());
            tvCat.setTextSize(15);
            tvCat.setTextColor(Color.parseColor("#102B27"));
            tvCat.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvCat.setLayoutParams(cLp);
            top.addView(tvCat);

            TextView tvAmt = new TextView(this);
            tvAmt.setText(MillimesUtil.formatMillimes(entry.getValue()) + " (" + pct + "%)");
            tvAmt.setTextSize(14);
            tvAmt.setTextColor(Color.parseColor("#102B27"));
            tvAmt.setTypeface(null, Typeface.BOLD);
            top.addView(tvAmt);

            card.addView(top);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100);
            pb.setProgress(pct);
            pb.setPadding(0, 12, 0, 0);
            card.addView(pb);

            layoutAnalyticsByCat.addView(card);
        }
    }
}

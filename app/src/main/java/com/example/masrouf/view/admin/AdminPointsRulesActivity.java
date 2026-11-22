package com.example.masrouf.view.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.GamificationDao;
import com.example.masrouf.model.PointsRule;

import java.util.List;

/**
 * RÔLE : Gestion administrative des règles de points et gamification (Figma 42).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°1 (Rôle ADMIN).
 *   - Exigence n°9 (MVC avec GamificationDao).
 *   - Paramétrage dynamique des points récompensés (ex: 100 points pour respect du budget).
 */
public class AdminPointsRulesActivity extends AppCompatActivity {

    private ListView lvAdminPointsRules;
    private SessionManager sessionManager;
    private GamificationDao gamificationDao;
    private List<PointsRule> rules;
    private PointsRuleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Accès réservé aux administrateurs.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_points_rules);

        sessionManager = new SessionManager(this);
        gamificationDao = new GamificationDao(this);

        initViews();
        loadRules();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackAdminPointsRules);
        ivBack.setOnClickListener(v -> finish());

        lvAdminPointsRules = findViewById(R.id.lvAdminPointsRules);
    }

    private void loadRules() {
        rules = gamificationDao.getAllPointsRules();
        adapter = new PointsRuleAdapter();
        lvAdminPointsRules.setAdapter(adapter);
    }

    private class PointsRuleAdapter extends BaseAdapter {
        @Override public int getCount() { return rules.size(); }
        @Override public Object getItem(int position) { return rules.get(position); }
        @Override public long getItemId(int position) { return rules.get(position).getId(); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout card = new LinearLayout(AdminPointsRulesActivity.this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(28, 20, 28, 20);

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.WHITE);
            gd.setCornerRadius(20);
            gd.setStroke(2, Color.parseColor("#E5E2DA"));
            card.setBackground(gd);

            PointsRule rule = rules.get(position);
            String lang = sessionManager.getLanguage();

            LinearLayout textCol = new LinearLayout(AdminPointsRulesActivity.this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            TextView tvName = new TextView(AdminPointsRulesActivity.this);
            tvName.setText(rule.getLocalizedName(lang));
            tvName.setTextColor(Color.parseColor("#111A16"));
            tvName.setTextSize(14);
            tvName.setTypeface(null, Typeface.BOLD);
            textCol.addView(tvName);

            TextView tvKey = new TextView(AdminPointsRulesActivity.this);
            tvKey.setText(rule.getRuleKey());
            tvKey.setTextColor(Color.parseColor("#6B7C74"));
            tvKey.setTextSize(11);
            textCol.addView(tvKey);

            card.addView(textCol);

            // Points Value Badge (clickable to edit)
            TextView tvPoints = new TextView(AdminPointsRulesActivity.this);
            tvPoints.setText(rule.getPointsValue() + " pts");
            tvPoints.setTextColor(Color.parseColor("#1C5143"));
            tvPoints.setTextSize(15);
            tvPoints.setTypeface(null, Typeface.BOLD);
            tvPoints.setPadding(16, 8, 16, 8);
            tvPoints.setBackgroundResource(R.drawable.bg_btn_secondary);
            tvPoints.setOnClickListener(v -> showEditPointsDialog(rule));
            card.addView(tvPoints);

            // Switch Active
            SwitchCompat swActive = new SwitchCompat(AdminPointsRulesActivity.this);
            swActive.setChecked(rule.isActive());
            swActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                rule.setActive(isChecked);
                gamificationDao.updatePointsRule(rule.getId(), rule.getPointsValue(), isChecked);
                Toast.makeText(AdminPointsRulesActivity.this, "Règle mise à jour", Toast.LENGTH_SHORT).show();
            });
            card.addView(swActive);

            return card;
        }
    }

    private void showEditPointsDialog(PointsRule rule) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier la valeur des points");

        EditText etPoints = new EditText(this);
        etPoints.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etPoints.setText(String.valueOf(rule.getPointsValue()));
        builder.setView(etPoints);

        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            try {
                int newVal = Integer.parseInt(etPoints.getText().toString().trim());
                rule.setPointsValue(newVal);
                gamificationDao.updatePointsRule(rule.getId(), newVal, rule.isActive());
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Points mis à jour", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
}

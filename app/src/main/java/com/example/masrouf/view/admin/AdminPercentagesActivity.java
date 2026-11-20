package com.example.masrouf.view.admin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.model.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RÔLE : Gestion administrative des pourcentages par défaut des catégories (Figma 41).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°1 (Rôle ADMIN).
 *   - Exigence n°9 (MVC avec CategoryDao).
 *   - Règle stricte : Le total des pourcentages par défaut doit obligatoirement être égal à 100%.
 */
public class AdminPercentagesActivity extends AppCompatActivity {

    private TextView tvPercentagesTotalLabel;
    private TextView tvPercentagesStatusMsg;
    private LinearLayout layoutPercentageStatusBanner;
    private LinearLayout layoutPercentagesContainer;
    private Button btnSavePercentages;

    private SessionManager sessionManager;
    private CategoryDao categoryDao;
    private List<Category> categories;
    private final Map<Long, EditText> inputs = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_percentages);

        sessionManager = new SessionManager(this);
        categoryDao = new CategoryDao(this);

        initViews();
        loadCategories();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackAdminPercentages);
        ivBack.setOnClickListener(v -> finish());

        tvPercentagesTotalLabel = findViewById(R.id.tvPercentagesTotalLabel);
        tvPercentagesStatusMsg = findViewById(R.id.tvPercentagesStatusMsg);
        layoutPercentageStatusBanner = findViewById(R.id.layoutPercentageStatusBanner);
        layoutPercentagesContainer = findViewById(R.id.layoutPercentagesContainer);
        btnSavePercentages = findViewById(R.id.btnSavePercentages);

        btnSavePercentages.setOnClickListener(v -> savePercentages());
    }

    private void loadCategories() {
        categories = categoryDao.getAllCategories();
        layoutPercentagesContainer.removeAllViews();
        inputs.clear();

        String lang = sessionManager.getLanguage();

        for (Category c : categories) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(24, 16, 24, 16);

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.WHITE);
            gd.setCornerRadius(16);
            gd.setStroke(2, Color.parseColor("#E5E2DA"));
            row.setBackground(gd);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 10;
            row.setLayoutParams(lp);

            TextView tvName = new TextView(this);
            tvName.setText(c.getLocalizedName(lang));
            tvName.setTextColor(Color.parseColor("#111A16"));
            tvName.setTextSize(14);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            row.addView(tvName);

            EditText etPct = new EditText(this);
            etPct.setLayoutParams(new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT));
            etPct.setGravity(Gravity.END);
            etPct.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etPct.setText(String.format(Locale.US, "%.1f", c.getDefaultPercentage()));
            etPct.setTextColor(Color.parseColor("#1C5143"));
            etPct.setTextSize(14);
            etPct.setTypeface(null, Typeface.BOLD);

            etPct.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateTotal(); }
                @Override public void afterTextChanged(Editable s) {}
            });

            row.addView(etPct);

            TextView tvUnit = new TextView(this);
            tvUnit.setText(" %");
            tvUnit.setTextColor(Color.parseColor("#6B7C74"));
            tvUnit.setTextSize(14);
            row.addView(tvUnit);

            layoutPercentagesContainer.addView(row);
            inputs.put(c.getId(), etPct);
        }

        validateTotal();
    }

    private void validateTotal() {
        double sum = 0.0;
        for (EditText et : inputs.values()) {
            try {
                sum += Double.parseDouble(et.getText().toString().trim());
            } catch (Exception ignored) {}
        }

        tvPercentagesTotalLabel.setText(getString(R.string.admin_percentages_total_format, sum));

        boolean isValid = Math.abs(sum - 100.0) < 0.5;

        if (isValid) {
            tvPercentagesStatusMsg.setText(R.string.admin_percentages_valid);
            tvPercentagesStatusMsg.setTextColor(Color.parseColor("#1C5143"));
            btnSavePercentages.setEnabled(true);
            btnSavePercentages.setAlpha(1.0f);
        } else {
            tvPercentagesStatusMsg.setText(R.string.admin_percentages_invalid);
            tvPercentagesStatusMsg.setTextColor(Color.parseColor("#DC7770"));
            btnSavePercentages.setEnabled(false);
            btnSavePercentages.setAlpha(0.5f);
        }
    }

    private void savePercentages() {
        for (Category c : categories) {
            EditText et = inputs.get(c.getId());
            if (et != null) {
                try {
                    double val = Double.parseDouble(et.getText().toString().trim());
                    c.setDefaultPercentage(val);
                    categoryDao.updateCategory(c);
                } catch (Exception ignored) {}
            }
        }

        Toast.makeText(this, "Pourcentages enregistrés", Toast.LENGTH_SHORT).show();
        finish();
    }
}

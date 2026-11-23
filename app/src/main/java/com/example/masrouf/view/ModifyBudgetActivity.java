package com.example.masrouf.view;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.model.Category;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RÔLE : Écran de Modification du Budget existant et répartition des allocations (Figma 11).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec BudgetDao).
 *   - Matrice §4 ligne 32 (CRUD complet : Modification d'un budget et de ses allocations).
 *   - Règle de validation métier : Somme des allocations <= Total budget (fonds non alloués autorisés).
 *   - Bouton Sauvegarder bloqué avec message explicite en cas de dépassement.
 */
import com.example.masrouf.BaseActivity;

public class ModifyBudgetActivity extends BaseActivity {

    private EditText etModifyTotalAmount;
    private TextView tvModifyStartDate;
    private TextView tvModifyEndDate;
    private TextView tvAllocatedSummary;
    private TextView tvUnallocatedSummary;
    private TextView tvAllocationError;
    private LinearLayout layoutModifyCategoriesContainer;
    private Button btnSaveModifiedBudget;

    private SessionManager sessionManager;
    private BudgetDao budgetDao;
    private CategoryDao categoryDao;

    private Budget currentBudget;
    private List<BudgetAllocation> allocations = new ArrayList<>();
    private final Map<Long, EditText> allocationInputs = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (sessionManager.isChild()) {
            Toast.makeText(this, "Accès réservé : Seul votre père peut modifier le budget global.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_modify_budget);
        budgetDao = new BudgetDao(this);
        categoryDao = new CategoryDao(this);

        initViews();
        loadExistingBudget();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackModifyBudget);
        ivBack.setOnClickListener(v -> finish());

        etModifyTotalAmount = findViewById(R.id.etModifyTotalAmount);
        tvModifyStartDate = findViewById(R.id.tvModifyStartDate);
        tvModifyEndDate = findViewById(R.id.tvModifyEndDate);
        tvAllocatedSummary = findViewById(R.id.tvAllocatedSummary);
        tvUnallocatedSummary = findViewById(R.id.tvUnallocatedSummary);
        tvAllocationError = findViewById(R.id.tvAllocationError);
        layoutModifyCategoriesContainer = findViewById(R.id.layoutModifyCategoriesContainer);
        btnSaveModifiedBudget = findViewById(R.id.btnSaveModifiedBudget);

        findViewById(R.id.layoutModifyStartDate).setOnClickListener(v -> showDatePicker(true));
        findViewById(R.id.layoutModifyEndDate).setOnClickListener(v -> showDatePicker(false));

        etModifyTotalAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateAllocations(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSaveModifiedBudget.setOnClickListener(v -> saveModifications());
    }

    private void loadExistingBudget() {
        long userId = sessionManager.getUserId();
        currentBudget = budgetDao.getCurrentBudget(userId);

        if (currentBudget == null) {
            Toast.makeText(this, "Aucun budget actif à modifier", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etModifyTotalAmount.setText(String.format(Locale.US, "%.3f", currentBudget.getTotalAmount()));
        tvModifyStartDate.setText(currentBudget.getStartDate());
        tvModifyEndDate.setText(currentBudget.getEndDate());

        allocations = budgetDao.getAllocationsForBudget(currentBudget.getId());
        List<Category> allCategories = categoryDao.getAllCategories();
        String lang = sessionManager.getLanguage();

        layoutModifyCategoriesContainer.removeAllViews();
        allocationInputs.clear();

        for (Category cat : allCategories) {
            double currentAlloc = 0.0;
            for (BudgetAllocation a : allocations) {
                if (a.getCategoryId() == cat.getId()) {
                    currentAlloc = a.getAllocatedAmount();
                    break;
                }
            }

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

            TextView tvCatName = new TextView(this);
            tvCatName.setText(cat.getLocalizedName(lang));
            tvCatName.setTextColor(Color.parseColor("#111A16"));
            tvCatName.setTextSize(14);
            tvCatName.setTypeface(null, Typeface.BOLD);
            tvCatName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            row.addView(tvCatName);

            EditText etAmount = new EditText(this);
            etAmount.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));
            etAmount.setGravity(Gravity.END);
            etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etAmount.setText(String.format(Locale.US, "%.1f", currentAlloc));
            etAmount.setTextColor(Color.parseColor("#1C5143"));
            etAmount.setTextSize(14);
            etAmount.setTypeface(null, Typeface.BOLD);

            etAmount.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateAllocations(); }
                @Override public void afterTextChanged(Editable s) {}
            });

            row.addView(etAmount);
            layoutModifyCategoriesContainer.addView(row);

            allocationInputs.put(cat.getId(), etAmount);
        }

        validateAllocations();
    }

    private void validateAllocations() {
        double totalBudget = 0.0;
        try {
            totalBudget = Double.parseDouble(etModifyTotalAmount.getText().toString().trim());
        } catch (Exception ignored) {}

        double totalAllocated = 0.0;
        for (EditText et : allocationInputs.values()) {
            try {
                totalAllocated += Double.parseDouble(et.getText().toString().trim());
            } catch (Exception ignored) {}
        }

        double unallocated = totalBudget - totalAllocated;

        tvAllocatedSummary.setText(getString(R.string.modify_budget_allocated_summary,
                MillimesUtil.formatDt(totalAllocated), MillimesUtil.formatDt(totalBudget)));

        if (unallocated >= 0) {
            tvUnallocatedSummary.setVisibility(View.VISIBLE);
            tvUnallocatedSummary.setText(getString(R.string.modify_budget_unallocated_summary, MillimesUtil.formatDt(unallocated)));
            tvAllocationError.setVisibility(View.GONE);
            btnSaveModifiedBudget.setEnabled(true);
            btnSaveModifiedBudget.setAlpha(1.0f);
        } else {
            tvUnallocatedSummary.setVisibility(View.GONE);
            tvAllocationError.setVisibility(View.VISIBLE);
            btnSaveModifiedBudget.setEnabled(false);
            btnSaveModifiedBudget.setAlpha(0.5f);
        }
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            if (isStart) {
                tvModifyStartDate.setText(formatted);
            } else {
                tvModifyEndDate.setText(formatted);
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void saveModifications() {
        double totalBudget = Double.parseDouble(etModifyTotalAmount.getText().toString().trim());
        currentBudget.setTotalAmount(totalBudget);
        currentBudget.setStartDate(tvModifyStartDate.getText().toString().trim());
        currentBudget.setEndDate(tvModifyEndDate.getText().toString().trim());

        budgetDao.updateBudget(currentBudget);

        for (Map.Entry<Long, EditText> entry : allocationInputs.entrySet()) {
            long catId = entry.getKey();
            double amount = 0.0;
            try {
                amount = Double.parseDouble(entry.getValue().getText().toString().trim());
            } catch (Exception ignored) {}

            boolean found = false;
            for (BudgetAllocation a : allocations) {
                if (a.getCategoryId() == catId) {
                    budgetDao.updateAllocationAmount(a.getId(), amount);
                    found = true;
                    break;
                }
            }
            if (!found) {
                BudgetAllocation newAlloc = new BudgetAllocation(currentBudget.getId(), catId, "", amount);
                budgetDao.saveAllocation(newAlloc);
            }
        }

        Toast.makeText(this, R.string.modify_budget_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}

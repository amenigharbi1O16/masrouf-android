package com.example.masrouf.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.model.Category;

import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Activité d'allocation du budget par catégorie (Figma 10).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°2 (Layout XML : res/layout/activity_allocate_budget.xml).
 *   - Matrice §4 ligne 32 (Insertion transactionnelle en base SQLite du Budget et de ses BudgetAllocations).
 *   - Calculs dynamiques de solde alloué / non alloué en Dinars Tunisiens (DT).
 */
public class AllocateBudgetActivity extends AppCompatActivity {

    private static final String TAG = "AllocateBudgetActivity";

    private TextView tvAllocPeriod;
    private TextView tvAllocTotalBudget;
    private TextView tvAllocatedSum;
    private TextView tvUnallocatedSum;
    private TextView tvNoticeUnallocated;

    private EditText etAllocFood;
    private EditText etAllocTransport;
    private EditText etAllocBills;
    private EditText etAllocEntertainment;
    private EditText etAllocSavings;
    private Button btnCreateMyBudget;
    private ImageView ivBackAllocate;

    private double totalBudget = 1200.0;
    private String startDate = "2026-11-01";
    private String endDate = "2026-11-30";

    private BudgetDao budgetDao;
    private CategoryDao categoryDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (sessionManager.isChild()) {
            Toast.makeText(this, "Accès réservé : Seul votre père peut modifier le budget global.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_allocate_budget);

        budgetDao = new BudgetDao(this);
        categoryDao = new CategoryDao(this);
        sessionManager = new SessionManager(this);

        // Lecture des extras transmis depuis CreateBudgetActivity (Ch.5 du cours)
        totalBudget = getIntent().getDoubleExtra(CreateBudgetActivity.EXTRA_BUDGET_TOTAL, 1200.0);
        startDate = getIntent().getStringExtra(CreateBudgetActivity.EXTRA_BUDGET_START);
        if (startDate == null) startDate = "2026-11-01";
        endDate = getIntent().getStringExtra(CreateBudgetActivity.EXTRA_BUDGET_END);
        if (endDate == null) endDate = "2026-11-30";

        initViews();
        setupDynamicCalculations();

        btnCreateMyBudget.setOnClickListener(v -> saveBudgetAndAllocations());
        ivBackAllocate.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvAllocPeriod = findViewById(R.id.tvAllocPeriod);
        tvAllocTotalBudget = findViewById(R.id.tvAllocTotalBudget);
        tvAllocatedSum = findViewById(R.id.tvAllocatedSum);
        tvUnallocatedSum = findViewById(R.id.tvUnallocatedSum);
        tvNoticeUnallocated = findViewById(R.id.tvNoticeUnallocated);

        etAllocFood = findViewById(R.id.etAllocFood);
        etAllocTransport = findViewById(R.id.etAllocTransport);
        etAllocBills = findViewById(R.id.etAllocBills);
        etAllocEntertainment = findViewById(R.id.etAllocEntertainment);
        etAllocSavings = findViewById(R.id.etAllocSavings);
        btnCreateMyBudget = findViewById(R.id.btnCreateMyBudget);
        ivBackAllocate = findViewById(R.id.ivBackAllocate);

        tvAllocTotalBudget.setText(String.format(Locale.US, "%.3f DT", totalBudget));
        tvAllocPeriod.setText(startDate + " – " + endDate);
    }

    /**
     * Attache des TextWatchers pour recalculer en temps réel les totaux alloués et non alloués.
     */
    private void setupDynamicCalculations() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSums();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etAllocFood.addTextChangedListener(watcher);
        etAllocTransport.addTextChangedListener(watcher);
        etAllocBills.addTextChangedListener(watcher);
        etAllocEntertainment.addTextChangedListener(watcher);
        etAllocSavings.addTextChangedListener(watcher);

        updateSums();
    }

    private void updateSums() {
        double food = parseVal(etAllocFood);
        double transport = parseVal(etAllocTransport);
        double bills = parseVal(etAllocBills);
        double entertainment = parseVal(etAllocEntertainment);
        double savings = parseVal(etAllocSavings);

        double allocated = food + transport + bills + entertainment + savings;
        double unallocated = totalBudget - allocated;

        tvAllocatedSum.setText(String.format(Locale.US, "%.3f DT", allocated));
        tvUnallocatedSum.setText(String.format(Locale.US, "%.3f DT", unallocated));
        tvNoticeUnallocated.setText(String.format(Locale.US, "%.3f DT stays unallocated", Math.max(0, unallocated)));
    }

    private double parseVal(EditText et) {
        try {
            String s = et.getText().toString().trim();
            if (s.isEmpty()) return 0.0;
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Enregistre le budget et ses enveloppes en base de données.
     */
    private void saveBudgetAndAllocations() {
        double food = parseVal(etAllocFood);
        double transport = parseVal(etAllocTransport);
        double bills = parseVal(etAllocBills);
        double entertainment = parseVal(etAllocEntertainment);
        double savings = parseVal(etAllocSavings);

        double allocated = food + transport + bills + entertainment + savings;
        if (allocated > totalBudget) {
            Toast.makeText(this, "Erreur : La somme allouée (" + allocated + " DT) dépasse le budget total (" + totalBudget + " DT).", Toast.LENGTH_LONG).show();
            return;
        }

        long userId = sessionManager.getUserId();
        if (userId <= 0) userId = 1;

        // 1. Sauvegarde du budget
        Budget budget = new Budget(userId, totalBudget, startDate, endDate);
        long budgetId = budgetDao.saveBudget(budget);

        if (budgetId != -1) {
            // 2. Sauvegarde des allocations pour les catégories
            List<Category> categories = categoryDao.getAllCategories();
            for (Category c : categories) {
                double amount = 0.0;
                if ("Food".equalsIgnoreCase(c.getNameEn())) amount = food;
                else if ("Transport".equalsIgnoreCase(c.getNameEn())) amount = transport;
                else if ("Bills".equalsIgnoreCase(c.getNameEn())) amount = bills;
                else if ("Entertainment".equalsIgnoreCase(c.getNameEn())) amount = entertainment;
                else if ("Savings".equalsIgnoreCase(c.getNameEn())) amount = savings;

                if (amount > 0) {
                    BudgetAllocation alloc = new BudgetAllocation(budgetId, c.getId(), c.getNameEn(), amount);
                    budgetDao.saveAllocation(alloc);
                }
            }

            Log.d(TAG, "Nouveau budget enregistré avec succès ID = " + budgetId);
            Toast.makeText(this, "Budget créé avec succès !", Toast.LENGTH_SHORT).show();

            // Retour à l'accueil
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de l'enregistrement du budget.", Toast.LENGTH_SHORT).show();
        }
    }
}

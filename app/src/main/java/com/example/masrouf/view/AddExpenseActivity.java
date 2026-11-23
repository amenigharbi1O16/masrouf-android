package com.example.masrouf.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.ExpenseDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.Expense;
import com.example.masrouf.util.MillimesUtil;
import com.example.masrouf.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran d'enregistrement d'une dépense fixe ou récurrente (Figma 17).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Architecture MVC avec ExpenseDao et BudgetDao).
 *   - Matrice §4 ligne 32 (CRUD complet des dépenses).
 *   - Distinction stricte entre achats de produits et charges fixes (factures, loyer, abonnements).
 *   - Déclenchement automatique des alertes budgétaires si seuil atteint.
 */
import com.example.masrouf.BaseActivity;

public class AddExpenseActivity extends BaseActivity {

    private EditText etExpenseTitle;
    private EditText etExpenseAmount;
    private Spinner spinnerExpenseCategory;
    private TextView tvExpenseDate;
    private SwitchCompat switchExpenseRecurring;
    private EditText etExpenseNote;
    private Button btnSaveExpense;

    private SessionManager sessionManager;
    private ExpenseDao expenseDao;
    private CategoryDao categoryDao;
    private BudgetDao budgetDao;

    private List<Category> categories = new ArrayList<>();
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        sessionManager = new SessionManager(this);
        expenseDao = new ExpenseDao(this);
        categoryDao = new CategoryDao(this);
        budgetDao = new BudgetDao(this);

        initViews();
        loadCategories();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackAddExpense);
        ivBack.setOnClickListener(v -> finish());

        etExpenseTitle = findViewById(R.id.etExpenseTitle);
        etExpenseAmount = findViewById(R.id.etExpenseAmount);
        spinnerExpenseCategory = findViewById(R.id.spinnerExpenseCategory);
        tvExpenseDate = findViewById(R.id.tvExpenseDate);
        switchExpenseRecurring = findViewById(R.id.switchExpenseRecurring);
        etExpenseNote = findViewById(R.id.etExpenseNote);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        tvExpenseDate.setText(selectedDate);

        findViewById(R.id.layoutExpenseDate).setOnClickListener(v -> showDatePicker());
        btnSaveExpense.setOnClickListener(v -> saveExpense());
    }

    private void loadCategories() {
        categories = categoryDao.getAllCategories();
        List<String> names = new ArrayList<>();
        String lang = sessionManager.getLanguage();

        for (Category c : categories) {
            names.add(c.getLocalizedName(lang));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerExpenseCategory.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvExpenseDate.setText(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void saveExpense() {
        String title = etExpenseTitle.getText().toString().trim();
        String amountStr = etExpenseAmount.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        long userId = sessionManager.getUserId();

        Category selectedCat = null;
        if (!categories.isEmpty()) {
            int pos = spinnerExpenseCategory.getSelectedItemPosition();
            selectedCat = categories.get(pos);
        }

        long catId = selectedCat != null ? selectedCat.getId() : 1;
        String catName = selectedCat != null ? selectedCat.getLocalizedName("en") : "General";

        Budget budget = budgetDao.getCurrentBudget(userId);
        long budgetId = budget != null ? budget.getId() : 0;

        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setBudgetId(budgetId);
        expense.setCategoryId(catId);
        expense.setCategoryName(catName);
        expense.setTitle(title);
        expense.setAmount(amount);
        expense.setAmountMillimes(MillimesUtil.dtToMillimes(amount));
        expense.setDate(selectedDate);
        expense.setNote(etExpenseNote.getText().toString().trim());
        expense.setRecurring(switchExpenseRecurring.isChecked());

        expenseDao.insertExpense(expense);

        // Mise à jour de l'allocation budgétaire
        if (budgetId > 0) {
            budgetDao.recordExpenseInAllocation(budgetId, catId, amount);

            // Vérifier alerte de budget
            Budget updatedBudget = budgetDao.getBudgetById(budgetId);
            double totalSpent = budgetDao.getTotalSpent(budgetId);
            if (updatedBudget != null && updatedBudget.getTotalAmount() > 0) {
                double pct = (totalSpent / updatedBudget.getTotalAmount()) * 100.0;
                if (pct >= 100.0) {
                    NotificationHelper.showBudgetExceededNotification(this, totalSpent - updatedBudget.getTotalAmount());
                } else if (pct >= 80.0) {
                    NotificationHelper.showBudgetWarningNotification(this, pct, updatedBudget.getTotalAmount() - totalSpent);
                }
            }
        }

        Toast.makeText(this, R.string.expense_saved_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}

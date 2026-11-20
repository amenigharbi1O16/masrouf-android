package com.example.masrouf.view;

import android.app.AlertDialog;
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
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.ExpenseDao;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.Expense;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran d'édition ou de suppression d'une dépense existante (Figma 25).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec ExpenseDao).
 *   - Matrice §4 ligne 32 (CRUD complet : Update et Delete).
 */
public class EditExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_EXPENSE_ID = "extra_expense_id";

    private EditText etExpenseTitle;
    private EditText etExpenseAmount;
    private Spinner spinnerExpenseCategory;
    private TextView tvExpenseDate;
    private SwitchCompat switchExpenseRecurring;
    private EditText etExpenseNote;
    private Button btnSaveExpense;
    private Button btnDeleteExpense;

    private SessionManager sessionManager;
    private ExpenseDao expenseDao;
    private CategoryDao categoryDao;

    private Expense currentExpense;
    private List<Category> categories = new ArrayList<>();
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        sessionManager = new SessionManager(this);
        expenseDao = new ExpenseDao(this);
        categoryDao = new CategoryDao(this);

        TextView tvTitle = findViewById(R.id.tvExpenseHeaderTitle);
        tvTitle.setText(R.string.edit_expense_title);

        long expenseId = getIntent().getLongExtra(EXTRA_EXPENSE_ID, -1);
        if (expenseId == -1) {
            finish();
            return;
        }

        currentExpense = expenseDao.getExpenseById(expenseId);
        if (currentExpense == null) {
            finish();
            return;
        }

        initViews();
        loadCategories();
        fillExpenseData();
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
        btnDeleteExpense = findViewById(R.id.btnDeleteExpense);
        btnDeleteExpense.setVisibility(View.VISIBLE);

        findViewById(R.id.layoutExpenseDate).setOnClickListener(v -> showDatePicker());
        btnSaveExpense.setOnClickListener(v -> updateExpense());
        btnDeleteExpense.setOnClickListener(v -> confirmDelete());
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

    private void fillExpenseData() {
        etExpenseTitle.setText(currentExpense.getTitle());
        etExpenseAmount.setText(String.format(Locale.US, "%.3f", currentExpense.getAmount()));
        selectedDate = currentExpense.getDate();
        tvExpenseDate.setText(selectedDate);
        switchExpenseRecurring.setChecked(currentExpense.isRecurring());
        etExpenseNote.setText(currentExpense.getNote());

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == currentExpense.getCategoryId()) {
                spinnerExpenseCategory.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvExpenseDate.setText(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void updateExpense() {
        String title = etExpenseTitle.getText().toString().trim();
        String amountStr = etExpenseAmount.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int pos = spinnerExpenseCategory.getSelectedItemPosition();
        Category cat = categories.get(pos);

        currentExpense.setTitle(title);
        currentExpense.setAmount(amount);
        currentExpense.setAmountMillimes(MillimesUtil.dtToMillimes(amount));
        currentExpense.setCategoryId(cat.getId());
        currentExpense.setCategoryName(cat.getLocalizedName("en"));
        currentExpense.setDate(selectedDate);
        currentExpense.setRecurring(switchExpenseRecurring.isChecked());
        currentExpense.setNote(etExpenseNote.getText().toString().trim());

        expenseDao.updateExpense(currentExpense);
        Toast.makeText(this, R.string.expense_saved_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    expenseDao.deleteExpense(currentExpense.getId());
                    Toast.makeText(this, R.string.expense_deleted_success, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}

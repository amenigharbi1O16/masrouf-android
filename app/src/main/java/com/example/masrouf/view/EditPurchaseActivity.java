package com.example.masrouf.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.util.MillimesUtil;

import java.util.Calendar;
import java.util.Locale;

/**
 * RÔLE : Écran d'édition ou suppression d'un achat enregistré (Figma 24).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec TransactionDao).
 *   - Matrice §4 ligne 32 (CRUD complet : Update et Delete).
 */
import com.example.masrouf.BaseActivity;

public class EditPurchaseActivity extends BaseActivity {

    public static final String EXTRA_TRANSACTION_ID = "extra_trans_id";

    private EditText etEditPurchaseTitle;
    private EditText etEditPurchaseAmount;
    private EditText etEditPurchaseStore;
    private TextView tvEditPurchaseDate;
    private EditText etEditPurchaseNote;
    private Button btnSaveEditPurchase;
    private Button btnDeleteEditPurchase;

    private TransactionDao transactionDao;
    private Transaction currentTransaction;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_purchase);

        long transId = getIntent().getLongExtra(EXTRA_TRANSACTION_ID, -1);
        if (transId == -1) {
            finish();
            return;
        }

        transactionDao = new TransactionDao(this);
        currentTransaction = transactionDao.getTransactionById(transId);

        if (currentTransaction == null) {
            finish();
            return;
        }

        initViews();
        fillData();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackEditPurchase);
        ivBack.setOnClickListener(v -> finish());

        etEditPurchaseTitle = findViewById(R.id.etEditPurchaseTitle);
        etEditPurchaseAmount = findViewById(R.id.etEditPurchaseAmount);
        etEditPurchaseStore = findViewById(R.id.etEditPurchaseStore);
        tvEditPurchaseDate = findViewById(R.id.tvEditPurchaseDate);
        etEditPurchaseNote = findViewById(R.id.etEditPurchaseNote);
        btnSaveEditPurchase = findViewById(R.id.btnSaveEditPurchase);
        btnDeleteEditPurchase = findViewById(R.id.btnDeleteEditPurchase);

        findViewById(R.id.layoutEditPurchaseDate).setOnClickListener(v -> showDatePicker());
        btnSaveEditPurchase.setOnClickListener(v -> updatePurchase());
        btnDeleteEditPurchase.setOnClickListener(v -> confirmDelete());
    }

    private void fillData() {
        etEditPurchaseTitle.setText(currentTransaction.getTitle());
        etEditPurchaseAmount.setText(String.format(Locale.US, "%.3f", currentTransaction.getAmount()));
        etEditPurchaseStore.setText(currentTransaction.getStore());
        selectedDate = currentTransaction.getDate();
        tvEditPurchaseDate.setText(selectedDate);
        etEditPurchaseNote.setText(currentTransaction.getNote());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            tvEditPurchaseDate.setText(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void updatePurchase() {
        String title = etEditPurchaseTitle.getText().toString().trim();
        String amountStr = etEditPurchaseAmount.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        currentTransaction.setTitle(title);
        currentTransaction.setAmount(amount);
        currentTransaction.setAmountMillimes(MillimesUtil.dtToMillimes(amount));
        currentTransaction.setStore(etEditPurchaseStore.getText().toString().trim());
        currentTransaction.setDate(selectedDate);
        currentTransaction.setNote(etEditPurchaseNote.getText().toString().trim());

        transactionDao.updateTransaction(currentTransaction);
        Toast.makeText(this, "Achat mis à jour", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    transactionDao.deleteTransaction(currentTransaction.getId());
                    Toast.makeText(this, "Achat supprimé", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}

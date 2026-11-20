package com.example.masrouf.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RÔLE : Activité de saisie d'un achat de produit ou d'une dépense simple (Figma 16).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°2 (Layout XML : res/layout/activity_add_purchase.xml).
 *   - Matrice §4 ligne 10 (STARTACTIVITYFORRESULT & ONACTIVITYRESULT : ouvre SelectCategoryActivity et récupère la catégorie sélectionnée, Ch.5 du cours).
 *   - Matrice §4 ligne 25 (BUNDLE & SAVEDINSTANCESTATE : sauvegarde de l'état des champs lors des rotations d'écran, Ch.4).
 *   - Matrice §4 ligne 27 (Utilisation de widgets SPINNER pour les unités et magasins).
 *   - Matrice §4 ligne 9 (Transmission des données calculées vers ComparePriceActivity via Intent extras).
 */
public class AddPurchaseActivity extends AppCompatActivity {

    private static final String TAG = "AddPurchaseActivity";

    // Code de requête pour identifier le retour de sélection de catégorie (Ch.5 du cours)
    public static final int REQUEST_CATEGORY_CODE = 101;

    // Clés de sauvegarde d'état dans le Bundle (Matrice §4 ligne 25)
    private static final String KEY_SAVED_PRODUCT = "saved_product_name";
    private static final String KEY_SAVED_PRICE = "saved_total_price";

    private EditText etPurchaseProductName;
    private LinearLayout layoutSelectCategoryBtn;
    private TextView tvSelectedCategoryName;
    private EditText etPurchaseTotalPrice;
    private EditText etPurchaseQuantity;
    private Spinner spnPurchaseUnit;
    private Spinner spnPurchaseStore;
    private TextView tvCalculatedUnitPrice;
    private Button btnComparePrice;
    private Button btnCancelPurchase;
    private ImageView ivBackAddPurchase;

    private long selectedCategoryId = 1; // 1 = Food par défaut
    private String selectedCategoryName = "Food";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase);

        initViews();
        setupSpinners();
        setupDynamicCalculations();
        setupListeners();

        // Récupération d'un éventuel produit pré-sélectionné transmis par ProductsActivity
        Intent intent = getIntent();
        if (intent.hasExtra("EXTRA_PRESELECTED_PRODUCT_NAME")) {
            etPurchaseProductName.setText(intent.getStringExtra("EXTRA_PRESELECTED_PRODUCT_NAME"));
            double price = intent.getDoubleExtra("EXTRA_PRESELECTED_PRODUCT_PRICE", 14.500);
            etPurchaseTotalPrice.setText(String.format(Locale.US, "%.3f", price));
        }

        boolean isSimpleExpense = intent.getBooleanExtra("EXTRA_IS_SIMPLE_EXPENSE", false);
        if (isSimpleExpense) {
            btnComparePrice.setText(R.string.btn_add_expense);
        }

        // EXIGENCE MATRICE §4 LIGNE 25 : Restauration de l'état sauvé lors d'une rotation d'écran
        if (savedInstanceState != null) {
            String savedProd = savedInstanceState.getString(KEY_SAVED_PRODUCT);
            if (savedProd != null) etPurchaseProductName.setText(savedProd);
            String savedPrice = savedInstanceState.getString(KEY_SAVED_PRICE);
            if (savedPrice != null) etPurchaseTotalPrice.setText(savedPrice);
        }
    }

    private void initViews() {
        etPurchaseProductName = findViewById(R.id.etPurchaseProductName);
        layoutSelectCategoryBtn = findViewById(R.id.layoutSelectCategoryBtn);
        tvSelectedCategoryName = findViewById(R.id.tvSelectedCategoryName);
        etPurchaseTotalPrice = findViewById(R.id.etPurchaseTotalPrice);
        etPurchaseQuantity = findViewById(R.id.etPurchaseQuantity);
        spnPurchaseUnit = findViewById(R.id.spnPurchaseUnit);
        spnPurchaseStore = findViewById(R.id.spnPurchaseStore);
        tvCalculatedUnitPrice = findViewById(R.id.tvCalculatedUnitPrice);
        btnComparePrice = findViewById(R.id.btnComparePrice);
        btnCancelPurchase = findViewById(R.id.btnCancelPurchase);
        ivBackAddPurchase = findViewById(R.id.ivBackAddPurchase);
    }

    /**
     * EXIGENCE MATRICE §4 LIGNE 27 : Configuration des Spinners pour les unités et magasins.
     */
    private void setupSpinners() {
        // Spinner des unités
        String[] units = {"L", "kg", "unité", "pack", "g", "bouteille"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units);
        spnPurchaseUnit.setAdapter(unitAdapter);

        // Spinner des magasins
        String[] stores = {"Monoprix", "Carrefour", "Magasin Général", "Boutique du coin", "Marché central"};
        ArrayAdapter<String> storeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stores);
        spnPurchaseStore.setAdapter(storeAdapter);
    }

    private void setupListeners() {
        // EXIGENCE MATRICE §4 LIGNE 10 : Lancement de l'activité de sélection avec attente de résultat
        layoutSelectCategoryBtn.setOnClickListener(v -> {
            Intent selectIntent = new Intent(AddPurchaseActivity.this, SelectCategoryActivity.class);
            startActivityForResult(selectIntent, REQUEST_CATEGORY_CODE);
        });

        btnComparePrice.setOnClickListener(v -> proceedToComparison());
        btnCancelPurchase.setOnClickListener(v -> finish());
        ivBackAddPurchase.setOnClickListener(v -> finish());
    }

    /**
     * EXIGENCE DU COURS (Ch.5 & Lab 3) : Réception du résultat renvoyé par SelectCategoryActivity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Vérification que le retour correspond bien à notre demande de sélection de catégorie
        if (requestCode == REQUEST_CATEGORY_CODE && resultCode == RESULT_OK && data != null) {
            selectedCategoryId = data.getLongExtra(SelectCategoryActivity.EXTRA_CATEGORY_ID, 1);
            selectedCategoryName = data.getStringExtra(SelectCategoryActivity.EXTRA_CATEGORY_NAME);

            // Mise à jour de l'affichage avec la catégorie choisie
            tvSelectedCategoryName.setText(selectedCategoryName);
            Log.d(TAG, "Catégorie sélectionnée avec succès : " + selectedCategoryName + " (ID: " + selectedCategoryId + ")");
        }
    }

    /**
     * EXIGENCE DU COURS (Ch.4) : Sauvegarde des données en cas de changement de configuration (rotation).
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SAVED_PRODUCT, etPurchaseProductName.getText().toString());
        outState.putString(KEY_SAVED_PRICE, etPurchaseTotalPrice.getText().toString());
        Log.d(TAG, "onSaveInstanceState() : sauvegarde du formulaire dans le Bundle");
    }

    private void setupDynamicCalculations() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateUnitPrice();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etPurchaseTotalPrice.addTextChangedListener(watcher);
        etPurchaseQuantity.addTextChangedListener(watcher);
        calculateUnitPrice();
    }

    private void calculateUnitPrice() {
        try {
            double price = Double.parseDouble(etPurchaseTotalPrice.getText().toString().trim());
            double qty = Double.parseDouble(etPurchaseQuantity.getText().toString().trim());
            String unit = spnPurchaseUnit.getSelectedItem() != null ? spnPurchaseUnit.getSelectedItem().toString() : "L";

            if (qty > 0) {
                double unitPrice = price / qty;
                tvCalculatedUnitPrice.setText(String.format(Locale.US, "%.3f DT / %s", unitPrice, unit));
            }
        } catch (Exception ignored) {
            tvCalculatedUnitPrice.setText("0.000 DT");
        }
    }

    private void proceedToComparison() {
        String prodName = etPurchaseProductName.getText().toString().trim();
        String priceStr = etPurchaseTotalPrice.getText().toString().trim();
        String qtyStr = etPurchaseQuantity.getText().toString().trim();

        if (prodName.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs de l'achat.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        double qty = Double.parseDouble(qtyStr);
        String unit = spnPurchaseUnit.getSelectedItem().toString();
        String store = spnPurchaseStore.getSelectedItem().toString();
        double unitPrice = qty > 0 ? (price / qty) : price;

        boolean isSimpleExpense = getIntent().getBooleanExtra("EXTRA_IS_SIMPLE_EXPENSE", false);
        if (isSimpleExpense) {
            // Enregistrement direct et émission de notification
            long userId = new SessionManager(this).getUserId();
            if (userId <= 0) userId = 1;
            String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date());

            Transaction t = new Transaction(
                    userId, selectedCategoryId, selectedCategoryName, 0,
                    prodName, price, unitPrice, qty, unit, store, currentDate, "Dépense rapide"
            );
            new TransactionDao(this).insertTransaction(t);

            // Mise à jour de l'allocation budgétaire et contrôle des seuils
            BudgetDao budgetDao = new BudgetDao(this);
            Budget currentBudget = budgetDao.getCurrentBudget(userId);
            if (currentBudget != null) {
                budgetDao.recordExpenseInAllocation(currentBudget.getId(), selectedCategoryId, price);
                double totalSpent = budgetDao.getTotalSpent(currentBudget.getId());
                double totalBudget = currentBudget.getTotalAmount();
                if (totalBudget > 0) {
                    double ratio = totalSpent / totalBudget;
                    if (ratio >= 1.0) {
                        NotificationHelper.showBudgetAlert(
                                this,
                                getString(R.string.notif_budget_exceeded_title),
                                String.format(Locale.US, getString(R.string.notif_budget_exceeded_msg), String.format(Locale.US, "%.3f DT", totalSpent - totalBudget)),
                                true
                        );
                    } else if (ratio >= 0.8) {
                        NotificationHelper.showBudgetAlert(
                                this,
                                getString(R.string.notif_budget_warning_title),
                                String.format(Locale.US, getString(R.string.notif_budget_warning_msg), (ratio * 100.0), String.format(Locale.US, "%.3f DT", totalBudget - totalSpent)),
                                false
                        );
                    }
                }
            }

            // Notification d'achat enregistré
            NotificationHelper.showPurchaseNotification(
                    this,
                    getString(R.string.notif_purchase_added_title),
                    String.format(Locale.US, getString(R.string.notif_purchase_added_msg), String.format(Locale.US, "%.3f DT", price), prodName)
            );

            Toast.makeText(this, "Dépense enregistrée avec succès !", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Transmission vers ComparePriceActivity (Figma 18)
        Intent intent = new Intent(this, ComparePriceActivity.class);
        intent.putExtra("EXTRA_PROD_NAME", prodName);
        intent.putExtra("EXTRA_CAT_ID", selectedCategoryId);
        intent.putExtra("EXTRA_CAT_NAME", selectedCategoryName);
        intent.putExtra("EXTRA_TOTAL_PRICE", price);
        intent.putExtra("EXTRA_QTY", qty);
        intent.putExtra("EXTRA_UNIT", unit);
        intent.putExtra("EXTRA_STORE", store);
        intent.putExtra("EXTRA_UNIT_PRICE", unitPrice);
        startActivity(intent);
        finish();
    }
}

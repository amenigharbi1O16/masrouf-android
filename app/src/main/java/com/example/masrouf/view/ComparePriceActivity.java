package com.example.masrouf.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.ProductDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.Product;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RÔLE : Activité de comparaison des prix unitaires avant validation d'un achat (Figma 18).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°2 (Layout XML : res/layout/activity_compare_price.xml).
 *   - Matrice §4 ligne 32 (Écriture simultanée dans transactions, products et budget_allocations via SQLite).
 *   - Matrice §4 ligne 9 (Lecture des extras transmis par l'Intent).
 */
public class ComparePriceActivity extends AppCompatActivity {

    private static final String TAG = "ComparePriceActivity";

    private TextView tvCompareProdName;
    private TextView tvCompareProdSub;
    private TextView tvPreviousUnitPrice;
    private TextView tvNewUnitPrice;
    private TextView tvPriceChangeStatus;
    private TextView tvPriceDifferenceValue;
    private TextView tvComparePurchaseTotal;
    private Button btnConfirmPurchase;
    private Button btnEditPurchase;
    private ImageView ivBackComparePrice;

    private String productName;
    private long categoryId;
    private String categoryName;
    private double totalPrice;
    private double quantity;
    private String unit;
    private String store;
    private double unitPrice;

    private TransactionDao transactionDao;
    private ProductDao productDao;
    private BudgetDao budgetDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_price);

        transactionDao = new TransactionDao(this);
        productDao = new ProductDao(this);
        budgetDao = new BudgetDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        readIntentExtras();
        displayComparison();

        btnConfirmPurchase.setOnClickListener(v -> savePurchaseAndFinish());
        btnEditPurchase.setOnClickListener(v -> finish());
        ivBackComparePrice.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvCompareProdName = findViewById(R.id.tvCompareProdName);
        tvCompareProdSub = findViewById(R.id.tvCompareProdSub);
        tvPreviousUnitPrice = findViewById(R.id.tvPreviousUnitPrice);
        tvNewUnitPrice = findViewById(R.id.tvNewUnitPrice);
        tvPriceChangeStatus = findViewById(R.id.tvPriceChangeStatus);
        tvPriceDifferenceValue = findViewById(R.id.tvPriceDifferenceValue);
        tvComparePurchaseTotal = findViewById(R.id.tvComparePurchaseTotal);
        btnConfirmPurchase = findViewById(R.id.btnConfirmPurchase);
        btnEditPurchase = findViewById(R.id.btnEditPurchase);
        ivBackComparePrice = findViewById(R.id.ivBackComparePrice);
    }

    private void readIntentExtras() {
        Intent intent = getIntent();
        productName = intent.getStringExtra("EXTRA_PROD_NAME");
        if (productName == null) productName = "Olive oil";
        categoryId = intent.getLongExtra("EXTRA_CAT_ID", 1);
        categoryName = intent.getStringExtra("EXTRA_CAT_NAME");
        if (categoryName == null) categoryName = "Food";
        totalPrice = intent.getDoubleExtra("EXTRA_TOTAL_PRICE", 14.500);
        quantity = intent.getDoubleExtra("EXTRA_QTY", 1.0);
        unit = intent.getStringExtra("EXTRA_UNIT");
        if (unit == null) unit = "L";
        store = intent.getStringExtra("EXTRA_STORE");
        if (store == null) store = "Monoprix";
        unitPrice = intent.getDoubleExtra("EXTRA_UNIT_PRICE", 14.500);
    }

    private void displayComparison() {
        tvCompareProdName.setText(productName);
        tvCompareProdSub.setText(categoryName + " · " + String.format(Locale.US, "%.0f %s", quantity, unit));
        tvNewUnitPrice.setText(String.format(Locale.US, "%.3f DT / %s", unitPrice, unit));
        tvComparePurchaseTotal.setText(String.format(Locale.US, "%.3f DT", totalPrice));

        // Comparaison avec le prix historique connu
        double previousPrice = 15.000; // Référence par défaut
        Product existing = productDao.getProductById(1);
        if (existing != null && existing.getLastUnitPrice() > 0) {
            previousPrice = existing.getLastUnitPrice();
        }

        tvPreviousUnitPrice.setText(String.format(Locale.US, "%.3f DT / %s", previousPrice, unit));

        double diff = unitPrice - previousPrice;
        double pct = (diff / previousPrice) * 100.0;

        View layoutBadge = findViewById(R.id.layoutPriceChangeBadge);

        if (diff < 0) {
            if (layoutBadge != null) layoutBadge.setBackgroundResource(R.drawable.bg_admin_notice_card);
            tvPriceChangeStatus.setText("↓ Price decrease (Baisse de prix)");
            tvPriceChangeStatus.setTextColor(ContextCompat.getColor(this, R.color.forest));
            tvPriceDifferenceValue.setText(String.format(Locale.US, "–%.3f DT / –%.1f%%", Math.abs(diff), Math.abs(pct)));
            tvPriceDifferenceValue.setTextColor(ContextCompat.getColor(this, R.color.forest));
        } else if (diff > 0) {
            if (pct > 10.0) {
                if (layoutBadge != null) layoutBadge.setBackgroundResource(R.drawable.bg_btn_danger);
                tvPriceChangeStatus.setText("⚠️ ALERTE INFLATION (> 10%)");
                tvPriceChangeStatus.setTextColor(Color.WHITE);
                tvPriceDifferenceValue.setText(String.format(Locale.US, "+%.3f DT / +%.1f%%", diff, pct));
                tvPriceDifferenceValue.setTextColor(Color.WHITE);
            } else {
                if (layoutBadge != null) layoutBadge.setBackgroundResource(R.drawable.bg_input_card);
                tvPriceChangeStatus.setText("↑ Price increase (Hausse de prix)");
                tvPriceChangeStatus.setTextColor(ContextCompat.getColor(this, R.color.coral));
                tvPriceDifferenceValue.setText(String.format(Locale.US, "+%.3f DT / +%.1f%%", diff, pct));
                tvPriceDifferenceValue.setTextColor(ContextCompat.getColor(this, R.color.coral));
            }
        } else {
            if (layoutBadge != null) layoutBadge.setBackgroundResource(R.drawable.bg_input_card);
            tvPriceChangeStatus.setText("= Identical price (Prix identique)");
            tvPriceChangeStatus.setTextColor(ContextCompat.getColor(this, R.color.muted));
            tvPriceDifferenceValue.setText("0.000 DT / 0.0%");
            tvPriceDifferenceValue.setTextColor(ContextCompat.getColor(this, R.color.muted));
        }
    }

    /**
     * Enregistre l'achat complet dans SQLite, met à jour le budget et envoie les notifications.
     */
    private void savePurchaseAndFinish() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) userId = 1;

        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date());

        // 1. Insertion de la transaction
        Transaction t = new Transaction(
                userId,
                categoryId,
                categoryName,
                1,
                productName + " · " + String.format(Locale.US, "%.0f %s", quantity, unit),
                totalPrice,
                unitPrice,
                quantity,
                unit,
                store,
                currentDate,
                "Achat enregistré"
        );
        transactionDao.insertTransaction(t);

        // 2. Mise à jour du produit
        productDao.updateProductPrice(1, unitPrice, store, currentDate);

        // 3. Notification immédiate d'achat enregistré
        NotificationHelper.showPurchaseNotification(
                this,
                getString(R.string.notif_purchase_added_title),
                String.format(Locale.US, getString(R.string.notif_purchase_added_msg), String.format(Locale.US, "%.3f DT", totalPrice), productName)
        );

        // 4. Mise à jour de la consommation budgétaire dans l'allocation et vérification des seuils
        Budget currentBudget = budgetDao.getCurrentBudget(userId);
        if (currentBudget != null) {
            budgetDao.recordExpenseInAllocation(currentBudget.getId(), categoryId, totalPrice);

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

        Log.d(TAG, "Achat enregistré avec succès !");
        Toast.makeText(this, "Achat enregistré avec succès !", Toast.LENGTH_SHORT).show();

        // Retour à l'accueil
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}

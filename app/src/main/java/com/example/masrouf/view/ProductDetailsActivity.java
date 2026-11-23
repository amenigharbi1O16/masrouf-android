package com.example.masrouf.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.AddProductActivity;
import com.example.masrouf.R;
import com.example.masrouf.dao.PriceHistoryDao;
import com.example.masrouf.dao.ProductDao;
import com.example.masrouf.model.PriceHistory;
import com.example.masrouf.model.Product;
import com.example.masrouf.util.MillimesUtil;
import com.example.masrouf.util.PriceEvolutionChartView;

import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran de détail d'un produit, variation de prix et historique des relevés (Figma 13).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec ProductDao et PriceHistoryDao).
 *   - Matrice §4 ligne 32 (Traçabilité complète des prix).
 *   - Calcul exact de variation (nouvel achat vs achat précédent).
 */
import com.example.masrouf.BaseActivity;

public class ProductDetailsActivity extends BaseActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private TextView tvDetailCategory;
    private TextView tvDetailProductName;
    private TextView tvDetailBrand;
    private TextView tvDetailCurrentPrice;
    private TextView tvDetailVariationBadge;
    private TextView tvDetailPrevPrice;
    private LinearLayout layoutPriceHistoryContainer;
    private Button btnRecordPurchaseThisProduct;
    private Button btnCompareWithOthers;
    private ImageView ivEditProductBtn;

    private ProductDao productDao;
    private PriceHistoryDao priceHistoryDao;
    private Product product;
    private long productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        productDao = new ProductDao(this);
        priceHistoryDao = new PriceHistoryDao(this);

        initViews();
        loadProduct();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProduct();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackProductDetails);
        ivBack.setOnClickListener(v -> finish());

        ivEditProductBtn = findViewById(R.id.ivEditProductBtn);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailProductName = findViewById(R.id.tvDetailProductName);
        tvDetailBrand = findViewById(R.id.tvDetailBrand);
        tvDetailCurrentPrice = findViewById(R.id.tvDetailCurrentPrice);
        tvDetailVariationBadge = findViewById(R.id.tvDetailVariationBadge);
        tvDetailPrevPrice = findViewById(R.id.tvDetailPrevPrice);
        layoutPriceHistoryContainer = findViewById(R.id.layoutPriceHistoryContainer);
        btnRecordPurchaseThisProduct = findViewById(R.id.btnRecordPurchaseThisProduct);
        btnCompareWithOthers = findViewById(R.id.btnCompareWithOthers);

        ivEditProductBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailsActivity.this, AddProductActivity.class);
            intent.putExtra("id", productId);
            startActivity(intent);
        });

        btnRecordPurchaseThisProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailsActivity.this, AddPurchaseActivity.class);
            intent.putExtra("EXTRA_PRESELECTED_PRODUCT_ID", product.getId());
            intent.putExtra("EXTRA_PRESELECTED_PRODUCT_NAME", product.getName());
            intent.putExtra("EXTRA_PRESELECTED_PRODUCT_UNIT", product.getUnit());
            intent.putExtra("EXTRA_PRESELECTED_PRODUCT_PRICE", product.getLastUnitPrice());
            intent.putExtra("EXTRA_PRESELECTED_PRODUCT_STORE", product.getStore());
            startActivity(intent);
        });

        btnCompareWithOthers.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailsActivity.this, ComparePriceActivity.class);
            startActivity(intent);
        });
    }

    private void loadProduct() {
        product = productDao.getProductById(productId);
        if (product == null) {
            finish();
            return;
        }

        tvDetailProductName.setText(product.getName());
        tvDetailCategory.setText((product.getCategoryName() != null ? product.getCategoryName().toUpperCase() : "GENERAL") + " · " + product.getUnit());
        tvDetailBrand.setText("Brand: " + (product.getBrand() != null ? product.getBrand() : "-") + " · Store: " + (product.getStore() != null ? product.getStore() : "-"));
        tvDetailCurrentPrice.setText(MillimesUtil.formatDt(product.getLastUnitPrice()) + " / " + product.getUnit());
        tvDetailPrevPrice.setText("Previous: " + MillimesUtil.formatDt(product.getPreviousUnitPrice()) + " / " + product.getUnit());

        double pctChange = product.getPricePercentageChange();
        if (product.hasPriceDecreased()) {
            tvDetailVariationBadge.setText(String.format(Locale.US, "-%.1f%% cheaper", Math.abs(pctChange)));
            tvDetailVariationBadge.setTextColor(Color.parseColor("#1C5143"));
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.parseColor("#D9F0E4"));
            gd.setCornerRadius(16);
            tvDetailVariationBadge.setBackground(gd);
        } else if (product.hasPriceIncreased()) {
            tvDetailVariationBadge.setText(String.format(Locale.US, "+%.1f%% increase", pctChange));
            tvDetailVariationBadge.setTextColor(Color.parseColor("#DC7770"));
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.parseColor("#FBECEB"));
            gd.setCornerRadius(16);
            tvDetailVariationBadge.setBackground(gd);
        } else {
            tvDetailVariationBadge.setText("Stable price");
            tvDetailVariationBadge.setTextColor(Color.parseColor("#6B7C74"));
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.parseColor("#E5E2DA"));
            gd.setCornerRadius(16);
            tvDetailVariationBadge.setBackground(gd);
        }

        loadPriceHistory();
    }

    private void loadPriceHistory() {
        layoutPriceHistoryContainer.removeAllViews();
        List<PriceHistory> history = priceHistoryDao.getHistoryForProduct(productId);

        PriceEvolutionChartView chartView = findViewById(R.id.chartPriceEvolution);
        if (chartView != null) {
            chartView.setPriceHistory(history);
        }

        for (PriceHistory ph : history) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(24, 16, 24, 16);

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.WHITE);
            gd.setCornerRadius(16);
            row.setBackground(gd);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 8;
            row.setLayoutParams(lp);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            TextView tvStore = new TextView(this);
            tvStore.setText(ph.getStore() != null ? ph.getStore() : "Magasin");
            tvStore.setTextColor(Color.parseColor("#111A16"));
            tvStore.setTextSize(14);
            tvStore.setTypeface(null, Typeface.BOLD);
            textCol.addView(tvStore);

            TextView tvDate = new TextView(this);
            tvDate.setText(ph.getDate());
            tvDate.setTextColor(Color.parseColor("#6B7C74"));
            tvDate.setTextSize(11);
            textCol.addView(tvDate);

            row.addView(textCol);

            TextView tvPrice = new TextView(this);
            tvPrice.setText(MillimesUtil.formatDt(ph.getPrice()) + " / " + product.getUnit());
            tvPrice.setTextColor(Color.parseColor("#102B27"));
            tvPrice.setTextSize(15);
            tvPrice.setTypeface(null, Typeface.BOLD);
            row.addView(tvPrice);

            layoutPriceHistoryContainer.addView(row);
        }
    }
}

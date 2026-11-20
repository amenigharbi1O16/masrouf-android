package com.example.masrouf.view;

import android.app.AlertDialog;
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

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.ProductDao;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.Product;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran d'édition ou suppression d'un produit (Figma 26).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec ProductDao).
 *   - Matrice §4 ligne 32 (CRUD complet : Update et Delete).
 */
public class EditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private EditText etProductName;
    private EditText etProductBrand;
    private Spinner spinnerProductCategory;
    private EditText etProductUnit;
    private EditText etProductPrice;
    private EditText etProductStore;
    private Button btnSaveProduct;
    private Button btnDeleteProduct;

    private SessionManager sessionManager;
    private ProductDao productDao;
    private CategoryDao categoryDao;
    private Product currentProduct;
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        long productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            finish();
            return;
        }

        sessionManager = new SessionManager(this);
        productDao = new ProductDao(this);
        categoryDao = new CategoryDao(this);
        currentProduct = productDao.getProductById(productId);

        if (currentProduct == null) {
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvProductFormTitle);
        tvTitle.setText(R.string.product_edit_title);

        initViews();
        loadCategories();
        fillData();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackAddProduct);
        ivBack.setOnClickListener(v -> finish());

        etProductName = findViewById(R.id.etProductName);
        etProductBrand = findViewById(R.id.etProductBrand);
        spinnerProductCategory = findViewById(R.id.spinnerProductCategory);
        etProductUnit = findViewById(R.id.etProductUnit);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductStore = findViewById(R.id.etProductStore);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);
        btnDeleteProduct.setVisibility(View.VISIBLE);

        btnSaveProduct.setOnClickListener(v -> updateProduct());
        btnDeleteProduct.setOnClickListener(v -> confirmDelete());
    }

    private void loadCategories() {
        categories = categoryDao.getAllCategories();
        List<String> names = new ArrayList<>();
        String lang = sessionManager.getLanguage();

        for (Category c : categories) {
            names.add(c.getLocalizedName(lang));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerProductCategory.setAdapter(adapter);
    }

    private void fillData() {
        etProductName.setText(currentProduct.getName());
        etProductBrand.setText(currentProduct.getBrand());
        etProductUnit.setText(currentProduct.getUnit());
        etProductPrice.setText(String.format(Locale.US, "%.3f", currentProduct.getLastUnitPrice()));
        etProductStore.setText(currentProduct.getStore());

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == currentProduct.getCategoryId()) {
                spinnerProductCategory.setSelection(i);
                break;
            }
        }
    }

    private void updateProduct() {
        String name = etProductName.getText().toString().trim();
        String brand = etProductBrand.getText().toString().trim();
        String unit = etProductUnit.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String store = etProductStore.getText().toString().trim();

        if (name.isEmpty() || unit.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int pos = spinnerProductCategory.getSelectedItemPosition();
        Category cat = categories.get(pos);

        currentProduct.setName(name);
        currentProduct.setBrand(brand);
        currentProduct.setUnit(unit);
        currentProduct.setCategoryId(cat.getId());
        currentProduct.setCategoryName(cat.getLocalizedName("en"));
        currentProduct.setLastUnitPrice(price);
        currentProduct.setLastUnitPriceMillimes(MillimesUtil.dtToMillimes(price));
        currentProduct.setStore(store);

        productDao.updateProduct(currentProduct);
        Toast.makeText(this, R.string.product_saved_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    productDao.deleteProduct(currentProduct.getId());
                    Toast.makeText(this, "Produit supprimé", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}

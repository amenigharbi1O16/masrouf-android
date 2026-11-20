package com.example.masrouf.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.ProductDao;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.Product;
import com.example.masrouf.util.MillimesUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran d'ajout d'un nouveau produit au catalogue de suivi (Figma 15).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec ProductDao).
 *   - Matrice §4 ligne 32 (CRUD complet : Ajout d'un produit avec unité et prix unitaire).
 */
public class AddProductActivity extends AppCompatActivity {

    private EditText etProductName;
    private EditText etProductBrand;
    private Spinner spinnerProductCategory;
    private EditText etProductUnit;
    private EditText etProductPrice;
    private EditText etProductStore;
    private Button btnSaveProduct;

    private SessionManager sessionManager;
    private ProductDao productDao;
    private CategoryDao categoryDao;
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        sessionManager = new SessionManager(this);
        productDao = new ProductDao(this);
        categoryDao = new CategoryDao(this);

        initViews();
        loadCategories();
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

        btnSaveProduct.setOnClickListener(v -> saveProduct());
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

    private void saveProduct() {
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

        String now = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date());

        Product product = new Product();
        product.setName(name);
        product.setBrand(brand);
        product.setCategoryId(cat.getId());
        product.setCategoryName(cat.getLocalizedName("en"));
        product.setUnit(unit);
        product.setLastUnitPrice(price);
        product.setLastUnitPriceMillimes(MillimesUtil.dtToMillimes(price));
        product.setPreviousUnitPrice(price);
        product.setPreviousUnitPriceMillimes(MillimesUtil.dtToMillimes(price));
        product.setStore(store);
        product.setLastPurchaseDate(now);

        productDao.insertProduct(product);
        Toast.makeText(this, R.string.product_saved_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}

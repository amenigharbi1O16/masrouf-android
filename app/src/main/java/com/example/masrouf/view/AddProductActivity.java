package com.example.masrouf.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.ProductDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.Product;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.util.MillimesUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran d'ajout d'un produit et d'enregistrement de son achat (Figma / Image 10).
 * EXIGENCES DU PROF COUVERTES :
 *   - Matrice §4 ligne 32 (CRUD complet : Ajout produit avec photo, prix, unité).
 *   - Support Appareil photo (Camera) et Galerie d'images avec gestion des permissions (Exigence n°4).
 *   - Synchronisation automatique avec la table des transactions et allocations budgétaires.
 * COUCHE MVC : Vue (View).
 */
public class AddProductActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_IMAGE_PICK = 1002;
    private static final int PERMISSION_CAMERA_REQUEST = 2001;

    private EditText etProductName;
    private EditText etProductBrand;
    private LinearLayout layoutAddPhoto;
    private TextView tvAddPhotoLabel;
    private ImageView ivProductPhotoPreview;
    private Spinner spinnerProductCategory;
    private EditText etProductPrice;
    private EditText etProductQuantity;
    private Spinner spinnerProductUnit;
    private EditText etProductStore;
    private EditText etProductDate;
    private Button btnSaveProduct;
    private Button btnCancelProduct;

    private SessionManager sessionManager;
    private ProductDao productDao;
    private CategoryDao categoryDao;
    private TransactionDao transactionDao;
    private BudgetDao budgetDao;

    private List<Category> categories = new ArrayList<>();
    private Uri selectedPhotoUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        sessionManager = new SessionManager(this);
        productDao = new ProductDao(this);
        categoryDao = new CategoryDao(this);
        transactionDao = new TransactionDao(this);
        budgetDao = new BudgetDao(this);

        initViews();
        loadCategories();
        setupUnits();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackAddProduct);
        ivBack.setOnClickListener(v -> finish());

        etProductName = findViewById(R.id.etProductName);
        etProductBrand = findViewById(R.id.etProductBrand);
        layoutAddPhoto = findViewById(R.id.layoutAddPhoto);
        tvAddPhotoLabel = findViewById(R.id.tvAddPhotoLabel);
        ivProductPhotoPreview = findViewById(R.id.ivProductPhotoPreview);
        spinnerProductCategory = findViewById(R.id.spinnerProductCategory);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductQuantity = findViewById(R.id.etProductQuantity);
        spinnerProductUnit = findViewById(R.id.spinnerProductUnit);
        etProductStore = findViewById(R.id.etProductStore);
        etProductDate = findViewById(R.id.etProductDate);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        btnCancelProduct = findViewById(R.id.btnCancelProduct);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etProductDate.setText(today);
        etProductQuantity.setText("1");

        layoutAddPhoto.setOnClickListener(v -> showPhotoOptionsDialog());
        btnSaveProduct.setOnClickListener(v -> saveProductAndPurchase());
        btnCancelProduct.setOnClickListener(v -> finish());
    }

    private void setupUnits() {
        String[] units = new String[]{ "L", "kg", "g", "pack", "pièce", "bouteille" };
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units);
        spinnerProductUnit.setAdapter(unitAdapter);
    }

    private void loadCategories() {
        long userId = sessionManager.getUserId();
        categories = categoryDao.getUserSelectedCategoriesByType(userId, Category.TYPE_PRODUCT);
        List<String> names = new ArrayList<>();
        String lang = sessionManager.getLanguage();

        for (Category c : categories) {
            names.add(c.getNameByLocale(lang));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerProductCategory.setAdapter(adapter);
    }

    /**
     * Affiche une boîte de dialogue Bottom Sheet proposant Appareil photo, Galerie ou Supprimer.
     */
    private void showPhotoOptionsDialog() {
        boolean showDelete = selectedPhotoUri != null;
        PhotoSourceModal.show(this, showDelete, action -> {
            if (action == PhotoSourceModal.PhotoAction.CAMERA) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA }, PERMISSION_CAMERA_REQUEST);
                } else {
                    launchCameraIntent();
                }
            } else if (action == PhotoSourceModal.PhotoAction.GALLERY) {
                launchGalleryIntent();
            } else if (action == PhotoSourceModal.PhotoAction.DELETE) {
                selectedPhotoUri = null;
                if (ivProductPhotoPreview != null) ivProductPhotoPreview.setVisibility(View.GONE);
                if (tvAddPhotoLabel != null) tvAddPhotoLabel.setText("Ajouter une photo du produit");
            }
        });
    }

    private void launchCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Application appareil photo indisponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGalleryIntent() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        startActivityForResult(pickIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCameraIntent();
            } else {
                Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        ivProductPhotoPreview.setImageBitmap(imageBitmap);
                        ivProductPhotoPreview.setVisibility(View.VISIBLE);
                        tvAddPhotoLabel.setText("Photo ajoutée ✓");
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                selectedPhotoUri = data.getData();
                if (selectedPhotoUri != null) {
                    ivProductPhotoPreview.setImageURI(selectedPhotoUri);
                    ivProductPhotoPreview.setVisibility(View.VISIBLE);
                    tvAddPhotoLabel.setText("Photo sélectionnée ✓");
                }
            }
        }
    }

    private void saveProductAndPurchase() {
        String name = etProductName.getText().toString().trim();
        String brand = etProductBrand.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String qtyStr = etProductQuantity.getText().toString().trim();
        String store = etProductStore.getText().toString().trim();
        String date = etProductDate.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Veuillez renseigner le nom et le prix", Toast.LENGTH_SHORT).show();
            return;
        }

        double unitPrice;
        double quantity = 1.0;
        try {
            unitPrice = Double.parseDouble(priceStr.replace(',', '.'));
            if (!qtyStr.isEmpty()) {
                quantity = Double.parseDouble(qtyStr.replace(',', '.'));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Prix ou quantité invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        String unit = spinnerProductUnit.getSelectedItem() != null ? spinnerProductUnit.getSelectedItem().toString() : "L";
        int catPos = spinnerProductCategory.getSelectedItemPosition();
        Category cat = (catPos >= 0 && catPos < categories.size()) ? categories.get(catPos) : null;
        long catId = cat != null ? cat.getId() : 1;
        String catName = cat != null ? cat.getNameByLocale(sessionManager.getLanguage()) : "Alimentation";

        if (store.isEmpty()) {
            store = "Monoprix";
        }

        long userId = sessionManager.getUserId();
        long priceMillimes = MillimesUtil.dtToMillimes(unitPrice);

        // 1. Enregistrement dans la table des produits (Table 5)
        Product p = new Product();
        p.setUserId(userId);
        p.setName(name);
        p.setBrand(brand);
        p.setCategoryId(catId);
        p.setUnit(unit);
        p.setLastUnitPrice(unitPrice);
        p.setLastUnitPriceMillimes(priceMillimes);
        p.setPreviousUnitPrice(unitPrice);
        p.setPreviousUnitPriceMillimes(priceMillimes);
        p.setStore(store);
        p.setLastPurchaseDate(date);
        if (selectedPhotoUri != null) {
            p.setPhotoUri(selectedPhotoUri.toString());
        }

        long prodId = productDao.insertProduct(p);

        // 2. Enregistrement simultané dans la table des transactions d'achats (Table 6)
        double totalPurchaseAmount = unitPrice * quantity;
        long totalMillimes = MillimesUtil.dtToMillimes(totalPurchaseAmount);

        String currentMonthKey = sessionManager.getSelectedMonthKey();
        Budget currentBudget = budgetDao.getBudgetForMonth(userId, currentMonthKey);
        long budgetId = currentBudget != null ? currentBudget.getId() : 0;

        Transaction t = new Transaction();
        t.setUserId(userId);
        t.setBudgetId(budgetId);
        t.setCategoryId(catId);
        t.setCategoryName(catName);
        t.setProductId(prodId);
        t.setTitle(name + " · " + (int) quantity + " " + unit);
        t.setAmount(totalPurchaseAmount);
        t.setAmountMillimes(totalMillimes);
        t.setUnitPrice(unitPrice);
        t.setUnitPriceMillimes(priceMillimes);
        t.setQuantity(quantity);
        t.setUnit(unit);
        t.setStore(store);
        t.setDate(date);
        t.setNote("Achat de " + name);
        if (selectedPhotoUri != null) {
            t.setPhotoUri(selectedPhotoUri.toString());
        }

        transactionDao.insertTransaction(t);

        Toast.makeText(this, "Produit et achat enregistrés avec succès !", Toast.LENGTH_SHORT).show();
        finish();
    }
}

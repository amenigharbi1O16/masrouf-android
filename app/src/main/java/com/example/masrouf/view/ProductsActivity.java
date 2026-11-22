package com.example.masrouf.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.ProductDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.Product;
import com.example.masrouf.util.LocaleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Activité de consultation du catalogue de produits et de gestion des périodes (Image 10).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Matrice §4 ligne 30 (ADAPTER PERSONNALISÉ : classe héritant de BaseAdapter avec implémentation du pattern ViewHolder et recyclage convertView, Ch.6 du cours).
 *   - Matrice §4 ligne 9 (Transmission des détails du produit cliqué à l'écran de détails via Intent extras).
 *   - Gestion dynamique de l'état sans budget/période (Image 10).
 *   - Tiroir latéral de navigation intégré (Image 10).
 */
public class ProductsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayoutProducts;
    private ImageView ivMenuProducts;
    private FrameLayout frameNotifProducts;
    private ImageView ivProfileProducts;

    private LinearLayout layoutNoBudgetWarning;
    private Button btnDefinePeriodTop;
    private EditText etSearchProduct;
    private LinearLayout layoutEmptyProductsCard;
    private Button btnDefinePeriodCard;
    private ListView lvProducts;
    private Button btnAddProductFromCatalogue;

    private TextView tvDrawerUserNameProd;

    private ProductDao productDao;
    private BudgetDao budgetDao;
    private SessionManager sessionManager;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    private ProductAdapter productAdapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        productDao = new ProductDao(this);
        budgetDao = new BudgetDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupNavigationDrawer();
        setupListeners();
    }

    private void initViews() {
        drawerLayoutProducts = findViewById(R.id.drawerLayoutProducts);
        ivMenuProducts = findViewById(R.id.ivMenuProducts);
        frameNotifProducts = findViewById(R.id.frameNotifProducts);
        ivProfileProducts = findViewById(R.id.ivProfileProducts);

        layoutNoBudgetWarning = findViewById(R.id.layoutNoBudgetWarning);
        btnDefinePeriodTop = findViewById(R.id.btnDefinePeriodTop);
        etSearchProduct = findViewById(R.id.etSearchProduct);
        layoutEmptyProductsCard = findViewById(R.id.layoutEmptyProductsCard);
        btnDefinePeriodCard = findViewById(R.id.btnDefinePeriodCard);
        lvProducts = findViewById(R.id.lvProducts);
        btnAddProductFromCatalogue = findViewById(R.id.btnAddProductFromCatalogue);

        tvDrawerUserNameProd = findViewById(R.id.tvDrawerUserNameProd);
    }

    private void setupNavigationDrawer() {
        String name = sessionManager.getUserName();
        if (tvDrawerUserNameProd != null) {
            tvDrawerUserNameProd.setText(name != null && !name.isEmpty() ? name : "am75");
        }

        findViewById(R.id.btnDrawerHomeProd).setOnClickListener(v -> {
            drawerLayoutProducts.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ProductsActivity.this, MainActivity.class));
            finish();
        });

        findViewById(R.id.btnDrawerProductsProd).setOnClickListener(v -> drawerLayoutProducts.closeDrawer(GravityCompat.START));

        findViewById(R.id.btnDrawerCalendarProd).setOnClickListener(v -> {
            drawerLayoutProducts.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ProductsActivity.this, CalendarActivity.class));
        });

        findViewById(R.id.btnDrawerAnalyticsProd).setOnClickListener(v -> {
            drawerLayoutProducts.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ProductsActivity.this, AnalyticsActivity.class));
        });

        findViewById(R.id.btnDrawerPointsProd).setOnClickListener(v -> {
            drawerLayoutProducts.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ProductsActivity.this, PointsAndAchievementsActivity.class));
        });

        findViewById(R.id.btnDrawerSettingsProd).setOnClickListener(v -> {
            drawerLayoutProducts.closeDrawer(GravityCompat.START);
            startActivity(new Intent(ProductsActivity.this, SettingsActivity.class));
        });

        findViewById(R.id.btnDrawerLogoutProd).setOnClickListener(v -> {
            drawerLayoutProducts.closeDrawer(GravityCompat.START);
            sessionManager.clearSession();
            Intent intent = new Intent(ProductsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupListeners() {
        ivMenuProducts.setOnClickListener(v -> {
            if (drawerLayoutProducts != null) {
                if (drawerLayoutProducts.isDrawerOpen(GravityCompat.START)) {
                    drawerLayoutProducts.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayoutProducts.openDrawer(GravityCompat.START);
                }
            }
        });

        if (frameNotifProducts != null) {
            frameNotifProducts.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Notifications");
                builder.setMessage("Vous avez 1 recommandation active : Sélectionnez une période pour suivre vos prix.");
                builder.setPositiveButton("OK", (d, w) -> d.dismiss());
                builder.show();
            });
        }

        ivProfileProducts.setOnClickListener(v -> startActivity(new Intent(ProductsActivity.this, SettingsActivity.class)));

        View.OnClickListener goToCalendar = v -> startActivity(new Intent(ProductsActivity.this, CalendarActivity.class));
        if (btnDefinePeriodTop != null) btnDefinePeriodTop.setOnClickListener(goToCalendar);
        if (btnDefinePeriodCard != null) btnDefinePeriodCard.setOnClickListener(goToCalendar);

        if (btnAddProductFromCatalogue != null) {
            btnAddProductFromCatalogue.setOnClickListener(v -> startActivity(new Intent(ProductsActivity.this, AddProductActivity.class)));
        }

        // Recherche en temps réel
        if (etSearchProduct != null) {
            etSearchProduct.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterProducts(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Clic sur un produit de la liste -> détails complets
        lvProducts.setOnItemClickListener((parent, view, position, id) -> {
            if (position < filteredProducts.size()) {
                Product selected = filteredProducts.get(position);
                Intent intent = new Intent(ProductsActivity.this, ProductDetailsActivity.class);
                intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_ID, selected.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) userId = 1;

        Budget currentBudget = budgetDao.getCurrentBudget(userId);
        allProducts = productDao.getProductsByUser(userId);
        if (allProducts == null) allProducts = new ArrayList<>();

        if (currentBudget == null) {
            // État sans budget (Image 10)
            layoutNoBudgetWarning.setVisibility(View.VISIBLE);
            btnDefinePeriodTop.setVisibility(View.VISIBLE);
            layoutEmptyProductsCard.setVisibility(View.VISIBLE);
            lvProducts.setVisibility(View.GONE);
            btnAddProductFromCatalogue.setVisibility(View.GONE);
        } else if (allProducts.isEmpty()) {
            layoutNoBudgetWarning.setVisibility(View.GONE);
            btnDefinePeriodTop.setVisibility(View.GONE);
            layoutEmptyProductsCard.setVisibility(View.VISIBLE);
            lvProducts.setVisibility(View.GONE);
            btnAddProductFromCatalogue.setVisibility(View.VISIBLE);
        } else {
            // État actif avec produits
            layoutNoBudgetWarning.setVisibility(View.GONE);
            btnDefinePeriodTop.setVisibility(View.GONE);
            layoutEmptyProductsCard.setVisibility(View.GONE);
            lvProducts.setVisibility(View.VISIBLE);
            btnAddProductFromCatalogue.setVisibility(View.VISIBLE);

            filterProducts(etSearchProduct != null ? etSearchProduct.getText().toString() : "");
        }
    }

    private void filterProducts(String query) {
        filteredProducts.clear();
        String q = query.toLowerCase().trim();

        for (Product p : allProducts) {
            if (q.isEmpty() || p.getName().toLowerCase().contains(q) || (p.getBrand() != null && p.getBrand().toLowerCase().contains(q))) {
                filteredProducts.add(p);
            }
        }

        if (productAdapter == null) {
            productAdapter = new ProductAdapter(this, filteredProducts);
            lvProducts.setAdapter(productAdapter);
        } else {
            productAdapter.notifyDataSetChanged();
        }
    }

    /**
     * ADAPTER PERSONNALISÉ (BaseAdapter + ViewHolder - Matrice §4 ligne 30, Ch.6)
     */
    public static class ProductAdapter extends BaseAdapter {

        private final Context context;
        private final List<Product> products;
        private final LayoutInflater inflater;

        public ProductAdapter(Context context, List<Product> products) {
            this.context = context;
            this.products = products;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return products != null ? products.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return products.get(position);
        }

        @Override
        public long getItemId(int position) {
            return products.get(position).getId();
        }

        static class ViewHolder {
            TextView tvName;
            TextView tvSub;
            TextView tvStore;
            TextView tvPrice;
            TextView tvUnit;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_product, parent, false);
                holder = new ViewHolder();
                holder.tvName = convertView.findViewById(R.id.tvProdItemName);
                holder.tvSub = convertView.findViewById(R.id.tvProdItemSub);
                holder.tvStore = convertView.findViewById(R.id.tvProdItemStore);
                holder.tvPrice = convertView.findViewById(R.id.tvProdItemPrice);
                holder.tvUnit = convertView.findViewById(R.id.tvProdItemUnit);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Product product = products.get(position);

            if (holder.tvName != null) holder.tvName.setText(product.getName());
            if (holder.tvSub != null) {
                String brand = product.getBrand();
                String category = product.getCategoryName();
                holder.tvSub.setText(String.format("%s · %s", brand != null ? brand : "Générique", category != null ? category : "Produit"));
            }
            if (holder.tvStore != null) holder.tvStore.setText(product.getStore() != null ? product.getStore() : "Magasin");
            if (holder.tvPrice != null) holder.tvPrice.setText(String.format(Locale.US, "%.3f DT", product.getPrice()));
            if (holder.tvUnit != null) holder.tvUnit.setText(String.format(Locale.US, "/ %s", product.getUnit() != null ? product.getUnit() : "unité"));

            return convertView;
        }
    }
}

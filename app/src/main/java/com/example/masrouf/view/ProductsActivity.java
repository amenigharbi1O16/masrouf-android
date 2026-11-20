package com.example.masrouf.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.dao.ProductDao;
import com.example.masrouf.model.Product;

import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Activité de consultation du catalogue de produits et de suivi de leurs prix unitaires (Figma 12).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°2 (Layout XML : res/layout/activity_products.xml).
 *   - Matrice §4 ligne 30 (ADAPTER PERSONNALISÉ : classe héritant de BaseAdapter avec implémentation du pattern ViewHolder et recyclage convertView, Ch.6 du cours).
 *   - Matrice §4 ligne 9 (Transmission des détails du produit cliqué à l'écran d'achat via Intent extras).
 */
public class ProductsActivity extends AppCompatActivity {

    private ListView lvProducts;
    private Button btnNewPurchaseFromProducts;
    private ImageView ivBackProducts;

    private ProductDao productDao;
    private List<Product> productList;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        productDao = new ProductDao(this);
        lvProducts = findViewById(R.id.lvProducts);
        btnNewPurchaseFromProducts = findViewById(R.id.btnNewPurchaseFromProducts);
        ivBackProducts = findViewById(R.id.ivBackProducts);

        // Chargement des données depuis SQLite
        loadProducts();

        btnNewPurchaseFromProducts.setOnClickListener(v -> {
            Intent intent = new Intent(ProductsActivity.this, AddPurchaseActivity.class);
            startActivity(intent);
        });

        View btnHistory = findViewById(R.id.btnViewPurchaseHistory);
        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> startActivity(new Intent(ProductsActivity.this, PurchaseHistoryActivity.class)));
        }

        View ivAddProduct = findViewById(R.id.ivAddProductFromToolbar);
        if (ivAddProduct != null) {
            ivAddProduct.setOnClickListener(v -> startActivity(new Intent(ProductsActivity.this, AddProductActivity.class)));
        }

        ivBackProducts.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        productList = productDao.getAllProducts();

        // EXIGENCE MATRICE §4 LIGNE 30 : Utilisation de notre Adapter personnalisé
        productAdapter = new ProductAdapter(this, productList);
        lvProducts.setAdapter(productAdapter);

        // Clic sur un produit de la liste -> Ouvre les détails complets du produit (Figma 13)
        lvProducts.setOnItemClickListener((parent, view, position, id) -> {
            Product selected = productList.get(position);
            Intent intent = new Intent(ProductsActivity.this, ProductDetailsActivity.class);
            intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_ID, selected.getId());
            startActivity(intent);
        });
    }

    /**
     * EXIGENCE DU COURS (Ch.6) : Adapter personnalisé héritant de BaseAdapter.
     * Implémente le pattern 'ViewHolder' pour éviter les appels répétitifs et coûteux à findViewById().
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

        /**
         * Patron ViewHolder recommandé par le cours pour mémoriser les références de vues.
         */
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

            // 1. Recyclage de vue (convertView) pour économiser la mémoire RAM (Ch.6 du cours)
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_product, parent, false);
                holder = new ViewHolder();
                holder.tvName = convertView.findViewById(R.id.tvProdItemName);
                holder.tvSub = convertView.findViewById(R.id.tvProdItemSub);
                holder.tvStore = convertView.findViewById(R.id.tvProdItemStore);
                holder.tvPrice = convertView.findViewById(R.id.tvProdItemPrice);
                holder.tvUnit = convertView.findViewById(R.id.tvProdItemUnit);
                // Mémorisation dans le tag de la vue
                convertView.setTag(holder);
            } else {
                // Récupération instantanée du holder existant sans refaire findViewById
                holder = (ViewHolder) convertView.getTag();
            }

            // 2. Remplissage des données du produit courant
            Product p = products.get(position);
            holder.tvName.setText(p.getName());
            holder.tvSub.setText((p.getBrand() != null ? p.getBrand() + " · " : "") + p.getCategoryName() + " · 1 " + p.getUnit());
            holder.tvStore.setText(p.getStore() + " · " + p.getLastPurchaseDate());
            holder.tvPrice.setText(String.format(Locale.US, "%.3f DT", p.getLastUnitPrice()));
            holder.tvUnit.setText("par " + p.getUnit());

            return convertView;
        }
    }
}

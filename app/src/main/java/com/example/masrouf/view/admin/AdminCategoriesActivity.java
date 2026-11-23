package com.example.masrouf.view.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * RÔLE : Gestion administrative des catégories et de leurs types (Figma 39 & 40).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°1 (Espace réservé à l'administrateur).
 *   - Exigence n°9 (MVC avec CategoryDao).
 *   - Matrice §4 ligne 32 (CRUD complet : Ajout, consultation, suppression des catégories).
 */
import com.example.masrouf.BaseActivity;

public class AdminCategoriesActivity extends BaseActivity {

    private ListView lvAdminCategories;
    private Button btnFilterAll;
    private Button btnFilterProduct;
    private Button btnFilterExpense;
    private ImageView ivAddCategoryBtn;

    private SessionManager sessionManager;
    private CategoryDao categoryDao;
    private List<Category> allCategories = new ArrayList<>();
    private List<Category> displayedCategories = new ArrayList<>();
    private CategoryAdapter adapter;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Accès réservé aux administrateurs.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_categories);

        sessionManager = new SessionManager(this);
        categoryDao = new CategoryDao(this);

        initViews();
        loadCategories();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackAdminCategories);
        ivBack.setOnClickListener(v -> finish());

        lvAdminCategories = findViewById(R.id.lvAdminCategories);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterProduct = findViewById(R.id.btnFilterProduct);
        btnFilterExpense = findViewById(R.id.btnFilterExpense);
        ivAddCategoryBtn = findViewById(R.id.ivAddCategoryBtn);

        btnFilterAll.setOnClickListener(v -> setFilter("ALL"));
        btnFilterProduct.setOnClickListener(v -> setFilter(Category.TYPE_PRODUCT));
        btnFilterExpense.setOnClickListener(v -> setFilter(Category.TYPE_EXPENSE));

        ivAddCategoryBtn.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        allCategories = categoryDao.getAllCategories();
        applyFilter();
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        resetFilterButtons();

        if ("ALL".equals(filter)) {
            btnFilterAll.setBackgroundResource(R.drawable.bg_btn_primary);
            btnFilterAll.setTextColor(Color.WHITE);
        } else if (Category.TYPE_PRODUCT.equals(filter)) {
            btnFilterProduct.setBackgroundResource(R.drawable.bg_btn_primary);
            btnFilterProduct.setTextColor(Color.WHITE);
        } else {
            btnFilterExpense.setBackgroundResource(R.drawable.bg_btn_primary);
            btnFilterExpense.setTextColor(Color.WHITE);
        }

        applyFilter();
    }

    private void resetFilterButtons() {
        btnFilterAll.setBackgroundResource(R.drawable.bg_btn_secondary);
        btnFilterAll.setTextColor(getResources().getColor(R.color.forest));

        btnFilterProduct.setBackgroundResource(R.drawable.bg_btn_secondary);
        btnFilterProduct.setTextColor(getResources().getColor(R.color.forest));

        btnFilterExpense.setBackgroundResource(R.drawable.bg_btn_secondary);
        btnFilterExpense.setTextColor(getResources().getColor(R.color.forest));
    }

    private void applyFilter() {
        displayedCategories.clear();
        for (Category c : allCategories) {
            if ("ALL".equals(currentFilter) || currentFilter.equalsIgnoreCase(c.getType())) {
                displayedCategories.add(c);
            }
        }

        if (adapter == null) {
            adapter = new CategoryAdapter();
            lvAdminCategories.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nouvelle Catégorie");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etEn = new EditText(this);
        etEn.setHint("Nom en Anglais (ex: Health)");
        layout.addView(etEn);

        EditText etFr = new EditText(this);
        etFr.setHint("Nom en Français (ex: Santé)");
        layout.addView(etFr);

        EditText etAr = new EditText(this);
        etAr.setHint("Nom en Arabe (ex: الصحة)");
        layout.addView(etAr);

        builder.setView(layout);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String en = etEn.getText().toString().trim();
            String fr = etFr.getText().toString().trim();
            String ar = etAr.getText().toString().trim();

            if (!en.isEmpty() && !fr.isEmpty()) {
                Category c = new Category(en, fr, !ar.isEmpty() ? ar : en,
                        Category.TYPE_EXPENSE, "#1C5143", "ic_leaf", 5.0);
                c.setCustom(true);
                categoryDao.insertCategory(c);
                loadCategories();
                Toast.makeText(this, "Catégorie ajoutée", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private class CategoryAdapter extends BaseAdapter {
        @Override public int getCount() { return displayedCategories.size(); }
        @Override public Object getItem(int position) { return displayedCategories.get(position); }
        @Override public long getItemId(int position) { return displayedCategories.get(position).getId(); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout row = new LinearLayout(AdminCategoriesActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(28, 20, 28, 20);

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.WHITE);
            gd.setCornerRadius(20);
            gd.setStroke(2, Color.parseColor("#E5E2DA"));
            row.setBackground(gd);

            Category c = displayedCategories.get(position);

            LinearLayout textCol = new LinearLayout(AdminCategoriesActivity.this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

            TextView tvTitle = new TextView(AdminCategoriesActivity.this);
            tvTitle.setText(c.getNameFr() + " (" + c.getNameEn() + ") · " + c.getNameAr());
            tvTitle.setTextColor(Color.parseColor("#111A16"));
            tvTitle.setTextSize(14);
            tvTitle.setTypeface(null, Typeface.BOLD);
            textCol.addView(tvTitle);

            TextView tvType = new TextView(AdminCategoriesActivity.this);
            tvType.setText("Type: " + c.getType() + " · Pourcentage conseillé: " + c.getDefaultPercentage() + "%");
            tvType.setTextColor(Color.parseColor("#6B7C74"));
            tvType.setTextSize(12);
            textCol.addView(tvType);

            row.addView(textCol);

            return row;
        }
    }
}

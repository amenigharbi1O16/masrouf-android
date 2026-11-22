package com.example.masrouf.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.model.Category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RÔLE : Écran d'onboarding 02 / 03 - Choix personnalisé des catégories (Figma / Image 04).
 * Permet de sélectionner les catégories de produits et de dépenses.
 * COUCHE MVC : Vue (View).
 */
public class OnboardingCategoriesActivity extends AppCompatActivity {

    private ImageView ivBack, ivBell, ivProfile;
    private TextView tvCountBadge;
    private LinearLayout containerProducts, containerExpenses;
    private Button btnVerifyCategories;

    private CategoryDao categoryDao;
    private SessionManager sessionManager;
    private final Set<Long> selectedCategoryIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_categories);

        categoryDao = new CategoryDao(this);
        sessionManager = new SessionManager(this);

        ivBack = findViewById(R.id.ivBack);
        ivBell = findViewById(R.id.ivBell);
        ivProfile = findViewById(R.id.ivProfile);
        tvCountBadge = findViewById(R.id.tvCountBadge);
        containerProducts = findViewById(R.id.containerProducts);
        containerExpenses = findViewById(R.id.containerExpenses);
        btnVerifyCategories = findViewById(R.id.btnVerifyCategories);

        ivBack.setOnClickListener(v -> finish());
        ivBell.setOnClickListener(v -> Toast.makeText(this, "Aucune notification pour le moment", Toast.LENGTH_SHORT).show());
        ivProfile.setOnClickListener(v -> Toast.makeText(this, "Configurez vos catégories d'abord", Toast.LENGTH_SHORT).show());

        loadCategories();

        btnVerifyCategories.setOnClickListener(v -> {
            if (selectedCategoryIds.isEmpty()) {
                Toast.makeText(this, "Veuillez sélectionner au moins une catégorie", Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<Long> idsList = new ArrayList<>(selectedCategoryIds);
            Intent intent = new Intent(OnboardingCategoriesActivity.this, OnboardingConfirmActivity.class);
            intent.putExtra("EXTRA_SELECTED_CATEGORIES", idsList);
            startActivity(intent);
        });
    }

    private void loadCategories() {
        String lang = sessionManager.getLanguage();
        List<Category> products = categoryDao.getCategoriesByType(Category.TYPE_PRODUCT);
        List<Category> expenses = categoryDao.getCategoriesByType(Category.TYPE_EXPENSE);

        populateContainer(containerProducts, products, lang);
        populateContainer(containerExpenses, expenses, lang);
        updateUI();
    }

    private void populateContainer(LinearLayout container, List<Category> categories, String lang) {
        container.removeAllViews();
        for (Category cat : categories) {
            View itemView = createCategoryRow(cat, lang);
            container.addView(itemView);
        }
    }

    private View createCategoryRow(Category cat, String lang) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dpToPx(10), 0, dpToPx(10));

        // Icône de catégorie
        ImageView icon = new ImageView(this);
        int iconRes = getIconResId(cat.getIconName());
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#102B27")));
        LinearLayout.LayoutParams lpIcon = new LinearLayout.LayoutParams(dpToPx(22), dpToPx(22));
        icon.setLayoutParams(lpIcon);
        row.addView(icon);

        // Nom de la catégorie
        TextView tvName = new TextView(this);
        tvName.setText(cat.getNameByLocale(lang));
        tvName.setTextColor(Color.parseColor("#102B27"));
        tvName.setTextSize(14);
        LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpText.setMarginStart(dpToPx(14));
        tvName.setLayoutParams(lpText);
        row.addView(tvName);

        // Bouton / État "Sélectionner"
        TextView tvAction = new TextView(this);
        tvAction.setTextSize(13);
        tvAction.setTypeface(null, android.graphics.Typeface.BOLD);
        updateActionState(tvAction, selectedCategoryIds.contains(cat.getId()));

        row.setOnClickListener(v -> {
            if (selectedCategoryIds.contains(cat.getId())) {
                selectedCategoryIds.remove(cat.getId());
                updateActionState(tvAction, false);
            } else {
                selectedCategoryIds.add(cat.getId());
                updateActionState(tvAction, true);
            }
            updateUI();
        });

        row.addView(tvAction);
        return row;
    }

    private void updateActionState(TextView tvAction, boolean isSelected) {
        if (isSelected) {
            tvAction.setText("✓ Sélectionné");
            tvAction.setTextColor(Color.parseColor("#102B27"));
        } else {
            tvAction.setText("Sélectionner");
            tvAction.setTextColor(Color.parseColor("#6B7C74"));
        }
    }

    private void updateUI() {
        int count = selectedCategoryIds.size();
        tvCountBadge.setText("[ ] Catégories sélectionnées : " + count);
        btnVerifyCategories.setText("Vérifier les " + count + " catégories");

        if (count > 0) {
            btnVerifyCategories.setEnabled(true);
            btnVerifyCategories.setAlpha(1.0f);
        } else {
            btnVerifyCategories.setEnabled(false);
            btnVerifyCategories.setAlpha(0.6f);
        }
    }

    private int getIconResId(String iconName) {
        if (iconName == null) return R.drawable.ic_leaf;
        switch (iconName) {
            case "ic_cutlery": return R.drawable.ic_cutlery;
            case "ic_tshirt": return R.drawable.ic_tshirt;
            case "ic_sparkle": return R.drawable.ic_sparkle;
            case "ic_home": return R.drawable.ic_home;
            case "ic_laptop": return R.drawable.ic_laptop;
            case "ic_firstaid": return R.drawable.ic_firstaid;
            case "ic_car": return R.drawable.ic_car;
            case "ic_receipt": return R.drawable.ic_receipt;
            case "ic_school": return R.drawable.ic_school;
            case "ic_gamepad": return R.drawable.ic_gamepad;
            case "ic_wallet": return R.drawable.ic_wallet;
            case "ic_heart": return R.drawable.ic_heart;
            case "ic_card": return R.drawable.ic_card;
            case "ic_file": return R.drawable.ic_file;
            default: return R.drawable.ic_leaf;
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}

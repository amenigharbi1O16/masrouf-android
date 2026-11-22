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

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * RÔLE : Écran d'onboarding 03 / 03 - Confirmation de la sélection (Figma / Image 05).
 * Résume les catégories choisies et enregistre la sélection finale dans SQLite.
 * COUCHE MVC : Vue (View).
 */
public class OnboardingConfirmActivity extends AppCompatActivity {

    private ImageView ivBack, ivBell, ivProfile;
    private TextView tvSelectionSubtitle;
    private LinearLayout containerSelectedCards;
    private Button btnGoToHome, btnModifySelection;

    private CategoryDao categoryDao;
    private SessionManager sessionManager;
    private ArrayList<Long> selectedCategoryIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_confirm);

        categoryDao = new CategoryDao(this);
        sessionManager = new SessionManager(this);

        ivBack = findViewById(R.id.ivBack);
        ivBell = findViewById(R.id.ivBell);
        ivProfile = findViewById(R.id.ivProfile);
        tvSelectionSubtitle = findViewById(R.id.tvSelectionSubtitle);
        containerSelectedCards = findViewById(R.id.containerSelectedCards);
        btnGoToHome = findViewById(R.id.btnGoToHome);
        btnModifySelection = findViewById(R.id.btnModifySelection);

        selectedCategoryIds = (ArrayList<Long>) getIntent().getSerializableExtra("EXTRA_SELECTED_CATEGORIES");
        if (selectedCategoryIds == null) {
            selectedCategoryIds = new ArrayList<>();
        }

        ivBack.setOnClickListener(v -> finish());
        btnModifySelection.setOnClickListener(v -> finish());

        ivBell.setOnClickListener(v -> Toast.makeText(this, "Aucune notification pour le moment", Toast.LENGTH_SHORT).show());
        ivProfile.setOnClickListener(v -> Toast.makeText(this, "Terminez d'abord votre inscription", Toast.LENGTH_SHORT).show());

        displaySelectedCategories();

        btnGoToHome.setOnClickListener(v -> {
            long userId = sessionManager.getUserId();
            if (userId != -1 && !selectedCategoryIds.isEmpty()) {
                categoryDao.saveUserSelectedCategories(userId, selectedCategoryIds);
                sessionManager.setOnboardingCompleted(userId, true);
            }

            Intent intent = new Intent(OnboardingConfirmActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void displaySelectedCategories() {
        String lang = sessionManager.getLanguage();
        int count = selectedCategoryIds.size();
        tvSelectionSubtitle.setText(count + " catégories sélectionnées. Aucun budget requis pour l'instant.");

        containerSelectedCards.removeAllViews();
        for (Long catId : selectedCategoryIds) {
            Category cat = categoryDao.getCategoryById(catId);
            if (cat != null) {
                View card = createSelectedCard(cat, lang);
                containerSelectedCards.addView(card);
            }
        }
    }

    private View createSelectedCard(Category cat, String lang) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_input_card);
        card.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dpToPx(10));
        card.setLayoutParams(lp);

        // Icône de catégorie dans carré menthe
        ImageView icon = new ImageView(this);
        int iconRes = getIconResId(cat.getIconName());
        icon.setImageResource(iconRes);
        icon.setBackgroundResource(R.drawable.bg_circle_mint);
        icon.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#102B27")));
        LinearLayout.LayoutParams lpIcon = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        icon.setLayoutParams(lpIcon);
        card.addView(icon);

        // Nom et Type
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpText.setMarginStart(dpToPx(14));
        textLayout.setLayoutParams(lpText);

        TextView tvName = new TextView(this);
        tvName.setText(cat.getNameByLocale(lang));
        tvName.setTextColor(Color.parseColor("#102B27"));
        tvName.setTextSize(14);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        textLayout.addView(tvName);

        TextView tvType = new TextView(this);
        String typeLabel = Category.TYPE_PRODUCT.equalsIgnoreCase(cat.getType()) ? "Articles & Produits" : "Dépenses & Services";
        tvType.setText(typeLabel);
        tvType.setTextColor(Color.parseColor("#6B7C74"));
        tvType.setTextSize(11);
        tvType.setPadding(0, dpToPx(2), 0, 0);
        textLayout.addView(tvType);

        card.addView(textLayout);

        // Checkmark circulaire vert sombre
        ImageView check = new ImageView(this);
        check.setImageResource(R.drawable.ic_check_circle);
        check.setImageTintList(ColorStateList.valueOf(Color.parseColor("#102B27")));
        LinearLayout.LayoutParams lpCheck = new LinearLayout.LayoutParams(dpToPx(22), dpToPx(22));
        check.setLayoutParams(lpCheck);
        card.addView(check);

        return card;
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

package com.example.masrouf.view;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.NotificationDao;
import com.example.masrouf.model.AppNotification;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.model.Category;
import com.example.masrouf.util.MillimesUtil;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Bottom Sheet Modal pour l'ajout exclusif d'une catégorie sur le Tableau de Bord (Exigence n°4).
 * COUCHE MVC : Vue (View).
 */
public class CustomCategoryModal {

    public interface OnCategorySavedListener {
        void onCategorySaved();
    }

    private static final String[] ICON_PALETTE = new String[]{
            "ic_cutlery", "ic_tshirt", "ic_sparkle", "ic_home",
            "ic_laptop", "ic_firstaid", "ic_car", "ic_receipt",
            "ic_school", "ic_gamepad", "ic_wallet", "ic_heart",
            "ic_card", "ic_file", "ic_leaf", "ic_bell"
    };

    public static void show(Activity activity, OnCategorySavedListener listener) {
        SessionManager sessionManager = new SessionManager(activity);
        CategoryDao categoryDao = new CategoryDao(activity);
        BudgetDao budgetDao = new BudgetDao(activity);

        long userId = sessionManager.getUserId();

        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.modal_custom_category, null);
        dialog.setContentView(dialogView);

        TextView tabProduct = dialogView.findViewById(R.id.tabCategoryProduct);
        TextView tabExpense = dialogView.findViewById(R.id.tabCategoryExpense);
        LinearLayout containerSuggestions = dialogView.findViewById(R.id.containerCategorySuggestions);
        EditText etCatName = dialogView.findViewById(R.id.etCustomCatName);
        LinearLayout containerIcons = dialogView.findViewById(R.id.containerIconPalette);
        TextView tvRemainingSummary = dialogView.findViewById(R.id.tvRemainingPercentSummary);
        EditText etPercent = dialogView.findViewById(R.id.etCustomCatPercent);
        TextView tvCalculatedDt = dialogView.findViewById(R.id.tvCalculatedDtAllocation);
        TextView tvError = dialogView.findViewById(R.id.tvCatAllocationError);
        Button btnSave = dialogView.findViewById(R.id.btnSaveCustomCategory);

        final String[] selectedType = {Category.TYPE_PRODUCT};
        final String[] selectedIcon = {"ic_cutlery"};

        Budget currentBudget = budgetDao.getCurrentBudget(userId);
        double totalBudgetAmount = currentBudget != null ? currentBudget.getTotalAmount() : 0.0;

        // Calcul des allocations existantes
        double existingAllocatedPercent = 0.0;
        if (currentBudget != null) {
            List<BudgetAllocation> allocations = budgetDao.getAllocationsForBudget(currentBudget.getId());
            for (BudgetAllocation a : allocations) {
                if (totalBudgetAmount > 0) {
                    existingAllocatedPercent += (a.getAllocatedAmount() / totalBudgetAmount) * 100.0;
                }
            }
        }

        final double maxAllowedPercent = Math.max(0, 100.0 - existingAllocatedPercent);

        tvRemainingSummary.setText(String.format(Locale.US, "Disponible : %.1f%%", maxAllowedPercent));

        // Onglets
        tabProduct.setOnClickListener(v -> {
            selectedType[0] = Category.TYPE_PRODUCT;
            tabProduct.setBackgroundResource(R.drawable.bg_pill_active);
            tabProduct.setTextColor(Color.WHITE);
            tabExpense.setBackgroundResource(R.drawable.bg_pill_inactive);
            tabExpense.setTextColor(ContextCompat.getColor(activity, R.color.deep_forest));
            loadSuggestions(activity, categoryDao, userId, selectedType[0], containerSuggestions, etCatName);
        });

        tabExpense.setOnClickListener(v -> {
            selectedType[0] = Category.TYPE_EXPENSE;
            tabExpense.setBackgroundResource(R.drawable.bg_pill_active);
            tabExpense.setTextColor(Color.WHITE);
            tabProduct.setBackgroundResource(R.drawable.bg_pill_inactive);
            tabProduct.setTextColor(ContextCompat.getColor(activity, R.color.deep_forest));
            loadSuggestions(activity, categoryDao, userId, selectedType[0], containerSuggestions, etCatName);
        });

        // Chargement initial des suggestions
        loadSuggestions(activity, categoryDao, userId, selectedType[0], containerSuggestions, etCatName);

        // Chargement de la palette de 16 icônes
        loadIconPalette(activity, containerIcons, selectedIcon);

        // Surveillance dynamique du pourcentage
        etPercent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                double percentInput = 0.0;
                try {
                    percentInput = Double.parseDouble(input);
                } catch (Exception ignored) {}

                double calculatedDt = (percentInput / 100.0) * totalBudgetAmount;
                tvCalculatedDt.setText(String.format(Locale.US, "= %.3f DT", calculatedDt));

                if (percentInput > maxAllowedPercent) {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText(String.format(Locale.US, "⚠️ Dépassement ! Allocation disponible : %.1f%%", maxAllowedPercent));
                    btnSave.setEnabled(false);
                    btnSave.setAlpha(0.5f);
                } else {
                    tvError.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    btnSave.setAlpha(1.0f);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        btnSave.setOnClickListener(v -> {
            String name = etCatName.getText().toString().trim();
            String percentStr = etPercent.getText().toString().trim();

            if (name.isEmpty() || percentStr.isEmpty()) {
                Toast.makeText(activity, "Veuillez saisir un nom et une allocation.", Toast.LENGTH_SHORT).show();
                return;
            }

            double percent = Double.parseDouble(percentStr);
            if (percent > maxAllowedPercent) {
                Toast.makeText(activity, "L'allocation ne peut pas dépasser 100%.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insertion en base SQLite
            long newCatId = categoryDao.insertUserCategory(userId, name, selectedType[0], "#102B27", selectedIcon[0]);

            if (newCatId > 0 && currentBudget != null && totalBudgetAmount > 0) {
                double allocatedDt = (percent / 100.0) * totalBudgetAmount;
                BudgetAllocation newAlloc = new BudgetAllocation(currentBudget.getId(), newCatId, name, allocatedDt);
                budgetDao.saveAllocation(newAlloc);
            }

            if (newCatId > 0) {
                NotificationDao notifDao = new NotificationDao(activity);
                notifDao.insertNotification(userId,
                        "Nouvelle catégorie ajoutée",
                        "La catégorie \"" + name + "\" a été ajoutée à vos catégories.",
                        AppNotification.TYPE_CATEGORY_ADDED,
                        AppNotification.ROUTE_BUDGET);
            }

            Toast.makeText(activity, "Catégorie ajoutée avec succès !", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            if (listener != null) {
                listener.onCategorySaved();
            }
        });

        dialog.show();
    }

    private static void loadSuggestions(Activity activity, CategoryDao categoryDao, long userId, String type, LinearLayout container, EditText etCatName) {
        container.removeAllViews();
        List<Category> officialAll = categoryDao.getCategoriesByType(type);
        List<Category> userSelected = categoryDao.getUserSelectedCategories(userId);

        for (Category c : officialAll) {
            boolean alreadyHas = false;
            for (Category u : userSelected) {
                if (u.getId() == c.getId() || u.getNameEn().equalsIgnoreCase(c.getNameEn())) {
                    alreadyHas = true;
                    break;
                }
            }
            if (!alreadyHas) {
                TextView pill = new TextView(activity);
                pill.setText("+ " + c.getNameFr());
                pill.setTextColor(ContextCompat.getColor(activity, R.color.deep_forest));
                pill.setBackgroundResource(R.drawable.bg_pill_inactive);
                pill.setPadding(dpToPx(activity, 12), dpToPx(activity, 6), dpToPx(activity, 12), dpToPx(activity, 6));

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(dpToPx(activity, 8));
                pill.setLayoutParams(lp);

                pill.setOnClickListener(v -> etCatName.setText(c.getNameFr()));
                container.addView(pill);
            }
        }
    }

    private static void loadIconPalette(Activity activity, LinearLayout container, String[] selectedIconHolder) {
        container.removeAllViews();

        for (String iconName : ICON_PALETTE) {
            ImageView iv = new ImageView(activity);
            int iconRes = getIconResId(iconName);
            iv.setImageResource(iconRes);
            iv.setPadding(dpToPx(activity, 8), dpToPx(activity, 8), dpToPx(activity, 8), dpToPx(activity, 8));

            boolean isSelected = iconName.equals(selectedIconHolder[0]);
            updateIconBackground(activity, iv, isSelected);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpToPx(activity, 40), dpToPx(activity, 40));
            lp.setMarginEnd(dpToPx(activity, 8));
            iv.setLayoutParams(lp);

            iv.setOnClickListener(v -> {
                selectedIconHolder[0] = iconName;
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof ImageView) {
                        updateIconBackground(activity, (ImageView) child, child == iv);
                    }
                }
            });

            container.addView(iv);
        }
    }

    private static void updateIconBackground(Activity activity, ImageView iv, boolean isSelected) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(dpToPx(activity, 10));
        if (isSelected) {
            gd.setColor(ContextCompat.getColor(activity, R.color.mint));
            gd.setStroke(dpToPx(activity, 2), ContextCompat.getColor(activity, R.color.deep_forest));
            iv.setColorFilter(ContextCompat.getColor(activity, R.color.deep_forest));
        } else {
            gd.setColor(Color.parseColor("#F5F3ED"));
            gd.setStroke(dpToPx(activity, 1), Color.parseColor("#E2E7DF"));
            iv.setColorFilter(Color.parseColor("#61746D"));
        }
        iv.setBackground(gd);
    }

    private static int getIconResId(String iconName) {
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
            case "ic_bell": return R.drawable.ic_bell;
            default: return R.drawable.ic_leaf;
        }
    }

    private static int dpToPx(Activity activity, int dp) {
        return (int) (dp * activity.getResources().getDisplayMetrics().density);
    }
}

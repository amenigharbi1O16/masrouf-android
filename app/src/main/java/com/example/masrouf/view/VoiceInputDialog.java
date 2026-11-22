package com.example.masrouf.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.ExpenseDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.Expense;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.util.MillimesUtil;
import com.example.masrouf.util.VoiceTransactionParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Boîte de dialogue de revue et confirmation après saisie vocale (Exigence n°5).
 * Ne sauvegarde JAMAIS une interprétation incertaine automatiquement :
 * Permet à l'utilisateur de réviser, corriger le montant, le titre et la catégorie avant validation.
 * COUCHE MVC : Vue (View).
 */
public class VoiceInputDialog {

    public interface OnTransactionSavedListener {
        void onTransactionSaved();
    }

    public static void showReviewDialog(Activity activity, VoiceTransactionParser.ParsedVoiceResult result, OnTransactionSavedListener listener) {
        Context context = activity;
        SessionManager sessionManager = new SessionManager(context);
        CategoryDao categoryDao = new CategoryDao(context);
        TransactionDao transactionDao = new TransactionDao(context);
        ExpenseDao expenseDao = new ExpenseDao(context);
        BudgetDao budgetDao = new BudgetDao(context);

        long userId = sessionManager.getUserId();
        String currentMonthKey = sessionManager.getSelectedMonthKey();
        Budget currentBudget = budgetDao.getBudgetForMonth(userId, currentMonthKey);
        long budgetId = currentBudget != null ? currentBudget.getId() : 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Revue de la saisie vocale");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_voice_review, null);
        builder.setView(dialogView);

        RadioGroup rgType = dialogView.findViewById(R.id.rgVoiceType);
        RadioButton rbPurchase = dialogView.findViewById(R.id.rbVoicePurchase);
        RadioButton rbExpense = dialogView.findViewById(R.id.rbVoiceExpense);
        EditText etTitle = dialogView.findViewById(R.id.etVoiceTitle);
        EditText etAmount = dialogView.findViewById(R.id.etVoiceAmount);
        Spinner spCategory = dialogView.findViewById(R.id.spVoiceCategory);
        EditText etDate = dialogView.findViewById(R.id.etVoiceDate);

        // Pré-remplissage avec l'analyse NLP
        if (result.isPurchase) {
            rbPurchase.setChecked(true);
        } else {
            rbExpense.setChecked(true);
        }
        etTitle.setText(result.title);
        etAmount.setText(String.format(Locale.US, "%.3f", result.amountDt));

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etDate.setText(today);

        // Chargement des catégories de l'utilisateur
        List<Category> categories = categoryDao.getUserSelectedCategories(userId);
        List<String> catNames = new ArrayList<>();
        int selectedIndex = 0;
        String lang = sessionManager.getLanguage();

        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            catNames.add(c.getNameByLocale(lang));
            if (c.getNameEn().toLowerCase().contains(result.categoryKeyword.toLowerCase()) ||
                c.getNameFr().toLowerCase().contains(result.categoryKeyword.toLowerCase())) {
                selectedIndex = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, catNames);
        spCategory.setAdapter(adapter);
        if (selectedIndex < catNames.size()) {
            spCategory.setSelection(selectedIndex);
        }

        AlertDialog dialog = builder.create();

        Button btnConfirm = dialogView.findViewById(R.id.btnVoiceConfirm);
        Button btnCancel = dialogView.findViewById(R.id.btnVoiceCancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(context, "Veuillez remplir le titre et le montant", Toast.LENGTH_SHORT).show();
                return;
            }

            double amountDt;
            try {
                amountDt = Double.parseDouble(amountStr.replace(',', '.'));
            } catch (Exception e) {
                Toast.makeText(context, "Montant invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            int catPos = spCategory.getSelectedItemPosition();
            Category selectedCat = (catPos >= 0 && catPos < categories.size()) ? categories.get(catPos) : null;
            long catId = selectedCat != null ? selectedCat.getId() : 1;
            String catName = selectedCat != null ? selectedCat.getNameByLocale(lang) : "Général";

            long amountMillimes = MillimesUtil.dtToMillimes(amountDt);

            if (rbPurchase.isChecked()) {
                // Enregistrement d'un ACHAT (Transaction)
                Transaction t = new Transaction();
                t.setUserId(userId);
                t.setBudgetId(budgetId);
                t.setCategoryId(catId);
                t.setCategoryName(catName);
                t.setTitle(title);
                t.setAmount(amountDt);
                t.setAmountMillimes(amountMillimes);
                t.setUnitPrice(amountDt);
                t.setUnitPriceMillimes(amountMillimes);
                t.setQuantity(1.0);
                t.setUnit("Unité");
                t.setStore("Magasin");
                t.setDate(date);
                t.setNote("Saisie vocale");

                transactionDao.insertTransaction(t);
                Toast.makeText(context, "Achat enregistré : " + title + " (" + MillimesUtil.formatMillimes(amountMillimes) + ")", Toast.LENGTH_SHORT).show();
            } else {
                // Enregistrement d'une DÉPENSE (Expense)
                Expense exp = new Expense();
                exp.setUserId(userId);
                exp.setBudgetId(budgetId);
                exp.setCategoryId(catId);
                exp.setTitle(title);
                exp.setAmount(amountDt);
                exp.setAmountMillimes(amountMillimes);
                exp.setDate(date);
                exp.setNote("Saisie vocale");
                exp.setRecurring(false);

                expenseDao.insertExpense(exp);
                Toast.makeText(context, "Dépense enregistrée : " + title + " (" + MillimesUtil.formatMillimes(amountMillimes) + ")", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
            if (listener != null) {
                listener.onTransactionSaved();
            }
        });

        dialog.show();
    }
}

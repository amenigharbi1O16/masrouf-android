package com.example.masrouf.model;

import com.example.masrouf.util.MillimesUtil;

/**
 * RÔLE : Entité représentant une dépense fixe ou récurrente (loyer, facture STEG, abonnement bus, etc.).
 * COUCHE MVC : Modèle (Model).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Architecture MVC avec modèle pur).
 *   - Matrice §4 ligne 32 (CRUD complet des dépenses).
 *   - Spécifications Masrouf : Distinction nette entre achats de produits avec unité et dépenses fixes.
 */
public class Expense {

    private long id;
    private long userId;
    private long budgetId;
    private long categoryId;
    private String categoryName;
    private String title;
    private double amount;             // Montant en DT
    private long amountMillimes;       // Montant en millimes (entier pour zéro erreur d'arrondi)
    private String date;               // Date format YYYY-MM-DD ou "12 Nov 2026"
    private String note;
    private boolean isRecurring;       // Dépense fixe récurrente mensuelle

    public Expense() {
    }

    public Expense(long id, long userId, long budgetId, long categoryId, String categoryName,
                   String title, double amount, String date, String note, boolean isRecurring) {
        this.id = id;
        this.userId = userId;
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.title = title;
        this.amount = amount;
        this.amountMillimes = MillimesUtil.dtToMillimes(amount);
        this.date = date;
        this.note = note;
        this.isRecurring = isRecurring;
    }

    public Expense(long userId, long budgetId, long categoryId, String categoryName,
                   String title, double amount, String date, String note, boolean isRecurring) {
        this.userId = userId;
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.title = title;
        this.amount = amount;
        this.amountMillimes = MillimesUtil.dtToMillimes(amount);
        this.date = date;
        this.note = note;
        this.isRecurring = isRecurring;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(long budgetId) {
        this.budgetId = budgetId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        this.amountMillimes = MillimesUtil.dtToMillimes(amount);
    }

    public long getAmountMillimes() {
        return amountMillimes;
    }

    public void setAmountMillimes(long amountMillimes) {
        this.amountMillimes = amountMillimes;
        this.amount = MillimesUtil.millimesToDt(amountMillimes);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getFormattedAmount() {
        return MillimesUtil.formatMillimes(amountMillimes);
    }
}

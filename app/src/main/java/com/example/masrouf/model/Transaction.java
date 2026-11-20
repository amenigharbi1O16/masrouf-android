package com.example.masrouf.model;

import com.example.masrouf.util.MillimesUtil;

/**
 * RÔLE : Entité représentant une transaction d'achat de produit (Figma 07, 14, 16, 24).
 * COUCHE MVC : Modèle (Model).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec entité de données).
 *   - Matrice §4 ligne 29 (Utilisable avec SimpleCursorAdapter pour l'affichage de l'historique avec colonne _id).
 *   - Matrice §4 ligne 32 (CRUD complet des achats).
 *   - Calculs exacts en millimes.
 */
public class Transaction {

    private long id;
    private long userId;
    private long budgetId;
    private long categoryId;
    private String categoryName;
    private long productId;                 // 0 si achat sans produit du catalogue
    private String title;                   // Ex: "Olive oil · 1 L"
    private double amount;                  // Montant total en DT
    private long amountMillimes;            // Montant total en millimes
    private double unitPrice;               // Prix unitaire en DT
    private long unitPriceMillimes;         // Prix unitaire en millimes
    private double quantity;                // Quantité achetée (ex: 1.5)
    private String unit;                    // L, kg, unité, pack...
    private String store;                   // Ex: "Monoprix"
    private String date;                    // Format "12 Nov 2026"
    private String note;
    private String photoUri;

    public Transaction() {
    }

    public Transaction(long id, long userId, long categoryId, String categoryName, long productId,
                       String title, double amount, double unitPrice, double quantity,
                       String unit, String store, String date, String note) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.productId = productId;
        this.title = title;
        this.amount = amount;
        this.amountMillimes = MillimesUtil.dtToMillimes(amount);
        this.unitPrice = unitPrice;
        this.unitPriceMillimes = MillimesUtil.dtToMillimes(unitPrice);
        this.quantity = quantity;
        this.unit = unit;
        this.store = store;
        this.date = date;
        this.note = note;
    }

    public Transaction(long userId, long categoryId, String categoryName, long productId,
                       String title, double amount, double unitPrice, double quantity,
                       String unit, String store, String date, String note) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.productId = productId;
        this.title = title;
        this.amount = amount;
        this.amountMillimes = MillimesUtil.dtToMillimes(amount);
        this.unitPrice = unitPrice;
        this.unitPriceMillimes = MillimesUtil.dtToMillimes(unitPrice);
        this.quantity = quantity;
        this.unit = unit;
        this.store = store;
        this.date = date;
        this.note = note;
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

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.unitPriceMillimes = MillimesUtil.dtToMillimes(unitPrice);
    }

    public long getUnitPriceMillimes() {
        return unitPriceMillimes;
    }

    public void setUnitPriceMillimes(long unitPriceMillimes) {
        this.unitPriceMillimes = unitPriceMillimes;
        this.unitPrice = MillimesUtil.millimesToDt(unitPriceMillimes);
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
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

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getFormattedAmount() {
        return MillimesUtil.formatMillimes(amountMillimes);
    }
}

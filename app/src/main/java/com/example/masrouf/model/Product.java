package com.example.masrouf.model;

import com.example.masrouf.util.MillimesUtil;

/**
 * RÔLE : Entité représentant un produit suivi avec son unité et son historique de prix (Figma 12 & 13).
 * COUCHE MVC : Modèle (Model).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec entité de données).
 *   - Matrice §4 ligne 30 (Affichage d'items riches dans un Adapter personnalisé BaseAdapter).
 *   - Calculs exacts de variation de prix (augmentation / baisse en DT et en pourcentage).
 */
public class Product {

    private long id;
    private long userId;
    private String name;                   // Ex: "Olive oil", "Délice milk", "Couscous Randa"
    private long categoryId;               // Référence vers la catégorie parente
    private String categoryName;           // Ex: "Food"
    private String brand;                  // Ex: "Châal", "Délice"
    private String unit;                   // Ex: "L", "kg", "unité", "pack"
    private double lastUnitPrice;          // Dernier prix unitaire constaté en DT (ex: 14.500 DT/L)
    private long lastUnitPriceMillimes;    // Dernier prix unitaire en millimes (ex: 14500)
    private double previousUnitPrice;      // Prix avant le dernier achat en DT (ex: 15.000 DT/L)
    private long previousUnitPriceMillimes;// Prix avant le dernier achat en millimes (ex: 15000)
    private String store;                  // Ex: "Monoprix", "Carrefour", "Magasin Général"
    private String lastPurchaseDate;       // Ex: "12 Nov 2026"
    private String photoUri;               // URI locale vers la photo du produit (optionnelle)

    public Product() {
    }

    public Product(long id, String name, long categoryId, String categoryName, String brand,
                   String unit, double lastUnitPrice, double previousUnitPrice, String store, String lastPurchaseDate) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.brand = brand;
        this.unit = unit;
        this.lastUnitPrice = lastUnitPrice;
        this.lastUnitPriceMillimes = MillimesUtil.dtToMillimes(lastUnitPrice);
        this.previousUnitPrice = previousUnitPrice;
        this.previousUnitPriceMillimes = MillimesUtil.dtToMillimes(previousUnitPrice);
        this.store = store;
        this.lastPurchaseDate = lastPurchaseDate;
    }

    public Product(String name, long categoryId, String categoryName, String brand,
                   String unit, double lastUnitPrice, double previousUnitPrice, String store, String lastPurchaseDate) {
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.brand = brand;
        this.unit = unit;
        this.lastUnitPrice = lastUnitPrice;
        this.lastUnitPriceMillimes = MillimesUtil.dtToMillimes(lastUnitPrice);
        this.previousUnitPrice = previousUnitPrice;
        this.previousUnitPriceMillimes = MillimesUtil.dtToMillimes(previousUnitPrice);
        this.store = store;
        this.lastPurchaseDate = lastPurchaseDate;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getLastUnitPrice() {
        return lastUnitPrice;
    }

    public double getPrice() {
        return lastUnitPrice;
    }

    public void setLastUnitPrice(double lastUnitPrice) {
        this.lastUnitPrice = lastUnitPrice;
        this.lastUnitPriceMillimes = MillimesUtil.dtToMillimes(lastUnitPrice);
    }

    public long getLastUnitPriceMillimes() {
        return lastUnitPriceMillimes;
    }

    public void setLastUnitPriceMillimes(long lastUnitPriceMillimes) {
        this.lastUnitPriceMillimes = lastUnitPriceMillimes;
        this.lastUnitPrice = MillimesUtil.millimesToDt(lastUnitPriceMillimes);
    }

    public double getPreviousUnitPrice() {
        return previousUnitPrice;
    }

    public void setPreviousUnitPrice(double previousUnitPrice) {
        this.previousUnitPrice = previousUnitPrice;
        this.previousUnitPriceMillimes = MillimesUtil.dtToMillimes(previousUnitPrice);
    }

    public long getPreviousUnitPriceMillimes() {
        return previousUnitPriceMillimes;
    }

    public void setPreviousUnitPriceMillimes(long previousUnitPriceMillimes) {
        this.previousUnitPriceMillimes = previousUnitPriceMillimes;
        this.previousUnitPrice = MillimesUtil.millimesToDt(previousUnitPriceMillimes);
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public String getLastDate() {
        return lastPurchaseDate;
    }

    public double getLastPrice() {
        return lastUnitPrice;
    }

    public double getPreviousPrice() {
        return previousUnitPrice;
    }

    public void setLastPurchaseDate(String lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public double getPriceDifference() {
        return lastUnitPrice - previousUnitPrice;
    }

    public long getPriceDifferenceMillimes() {
        return lastUnitPriceMillimes - previousUnitPriceMillimes;
    }

    public double getPricePercentageChange() {
        if (previousUnitPriceMillimes <= 0) return 0.0;
        return ((double) (lastUnitPriceMillimes - previousUnitPriceMillimes) / (double) previousUnitPriceMillimes) * 100.0;
    }

    public boolean hasPriceDecreased() {
        return lastUnitPriceMillimes < previousUnitPriceMillimes;
    }

    public boolean hasPriceIncreased() {
        return lastUnitPriceMillimes > previousUnitPriceMillimes;
    }

    public String getFormattedLastPrice() {
        return MillimesUtil.formatMillimes(lastUnitPriceMillimes);
    }

    public String getFormattedPreviousPrice() {
        return MillimesUtil.formatMillimes(previousUnitPriceMillimes);
    }
}

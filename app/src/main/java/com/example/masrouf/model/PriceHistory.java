package com.example.masrouf.model;

import com.example.masrouf.util.MillimesUtil;

/**
 * RÔLE : Entité représentant un point d'historique de prix pour un produit donné (Figma 18).
 * COUCHE MVC : Modèle (Model).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Architecture MVC).
 *   - Matrice §4 ligne 32 (Traçabilité des prix dans le temps).
 */
public class PriceHistory {

    private long id;
    private long productId;
    private double price;             // Prix en DT
    private long priceMillimes;       // Prix en millimes
    private String store;             // Magasin (ex: Monoprix, Carrefour)
    private String date;              // Date du relevé de prix (ex: "12 Nov 2026")

    public PriceHistory() {
    }

    public PriceHistory(long id, long productId, double price, String store, String date) {
        this.id = id;
        this.productId = productId;
        this.price = price;
        this.priceMillimes = MillimesUtil.dtToMillimes(price);
        this.store = store;
        this.date = date;
    }

    public PriceHistory(long productId, double price, String store, String date) {
        this.productId = productId;
        this.price = price;
        this.priceMillimes = MillimesUtil.dtToMillimes(price);
        this.store = store;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.priceMillimes = MillimesUtil.dtToMillimes(price);
    }

    public long getPriceMillimes() {
        return priceMillimes;
    }

    public void setPriceMillimes(long priceMillimes) {
        this.priceMillimes = priceMillimes;
        this.price = MillimesUtil.millimesToDt(priceMillimes);
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

    public String getFormattedPrice() {
        return MillimesUtil.formatMillimes(priceMillimes);
    }
}

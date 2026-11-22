package com.example.masrouf.model;

/**
 * RÔLE : Entité représentant une catégorie budgétaire (Alimentation, Transport, Logement...).
 * COUCHE MVC : Modèle (Model).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Architecture MVC avec entités Modèle pures).
 *   - Matrice §4 ligne 17 (Prise en charge trilingue des libellés de catégories EN/FR/AR).
 */
public class Category {

    // Types de catégories possibles (Figma 40)
    public static final String TYPE_EXPENSE = "EXPENSE"; // Dépense générale (facture, loyer, transport)
    public static final String TYPE_PRODUCT = "PRODUCT"; // Catégorie d'achats de produits (ex: supermarché)

    private long id;
    private String nameEn;
    private String nameFr;
    private String nameAr;
    private String type;
    private String colorHex;
    private String iconName;
    private double defaultPercentage; // Pourcentage indicatif conseillé (ex: 41.6% pour Food)
    private boolean isCustom;          // true si créée par l'utilisateur
    private long userId;               // 0 si catégorie système

    public Category() {
    }

    public Category(long id, String nameEn, String nameFr, String nameAr,
                    String type, String colorHex, String iconName, double defaultPercentage) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.nameAr = nameAr;
        this.type = type;
        this.colorHex = colorHex;
        this.iconName = iconName;
        this.defaultPercentage = defaultPercentage;
        this.isCustom = false;
        this.userId = 0;
    }

    public Category(long id, String nameEn, String nameFr, String nameAr,
                    String type, String colorHex, String iconName, double defaultPercentage,
                    boolean isCustom, long userId) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.nameAr = nameAr;
        this.type = type;
        this.colorHex = colorHex;
        this.iconName = iconName;
        this.defaultPercentage = defaultPercentage;
        this.isCustom = isCustom;
        this.userId = userId;
    }

    public Category(String nameEn, String nameFr, String nameAr,
                    String type, String colorHex, String iconName, double defaultPercentage) {
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.nameAr = nameAr;
        this.type = type;
        this.colorHex = colorHex;
        this.iconName = iconName;
        this.defaultPercentage = defaultPercentage;
        this.isCustom = false;
        this.userId = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameFr() {
        return nameFr;
    }

    public void setNameFr(String nameFr) {
        this.nameFr = nameFr;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public String getNameByLocale(String lang) {
        if ("ar".equalsIgnoreCase(lang) && nameAr != null && !nameAr.isEmpty()) {
            return nameAr;
        }
        if ("en".equalsIgnoreCase(lang) && nameEn != null && !nameEn.isEmpty()) {
            return nameEn;
        }
        return nameFr != null && !nameFr.isEmpty() ? nameFr : (nameEn != null ? nameEn : "");
    }

    public String getLocalizedName(String lang) {
        return getNameByLocale(lang);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public double getDefaultPercentage() {
        return defaultPercentage;
    }

    public void setDefaultPercentage(double defaultPercentage) {
        this.defaultPercentage = defaultPercentage;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}

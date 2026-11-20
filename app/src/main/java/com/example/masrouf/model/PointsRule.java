package com.example.masrouf.model;

/**
 * RÔLE : Entité représentant une règle d'attribution de points pour la gamification (Figma 42).
 * COUCHE MVC : Modèle (Model).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Architecture MVC).
 *   - Spécifications Gamification : Attribution paramétrable par l'administrateur.
 */
public class PointsRule {

    private long id;
    private String ruleKey;       // Ex: "PERIOD_BUDGET_RESPECTED", "STREAK_3_PERIODS"
    private String nameEn;
    private String nameFr;
    private String nameAr;
    private int pointsValue;      // Ex: 100 points, 50 points
    private boolean isActive;

    public PointsRule() {
    }

    public PointsRule(long id, String ruleKey, String nameEn, String nameFr, String nameAr, int pointsValue, boolean isActive) {
        this.id = id;
        this.ruleKey = ruleKey;
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.nameAr = nameAr;
        this.pointsValue = pointsValue;
        this.isActive = isActive;
    }

    public PointsRule(String ruleKey, String nameEn, String nameFr, String nameAr, int pointsValue, boolean isActive) {
        this.ruleKey = ruleKey;
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.nameAr = nameAr;
        this.pointsValue = pointsValue;
        this.isActive = isActive;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public void setRuleKey(String ruleKey) {
        this.ruleKey = ruleKey;
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

    public int getPointsValue() {
        return pointsValue;
    }

    public void setPointsValue(int pointsValue) {
        this.pointsValue = pointsValue;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getLocalizedName(String langCode) {
        if ("fr".equalsIgnoreCase(langCode)) {
            return nameFr != null && !nameFr.isEmpty() ? nameFr : nameEn;
        } else if ("ar".equalsIgnoreCase(langCode)) {
            return nameAr != null && !nameAr.isEmpty() ? nameAr : nameEn;
        }
        return nameEn;
    }
}

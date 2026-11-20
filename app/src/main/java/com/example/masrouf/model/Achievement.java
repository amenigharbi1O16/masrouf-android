package com.example.masrouf.model;

/**
 * RÔLE : Entité représentant un badge / accomplissement à débloquer (Figma 21).
 * COUCHE MVC : Modèle (Model).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Architecture MVC).
 *   - Spécifications Gamification : Déblocage de badges par seuils de points et streaks.
 */
public class Achievement {

    private long id;
    private String code;            // Ex: "FIRST_BUDGET", "STREAK_3", "EXPERT_SAVER"
    private String titleEn;
    private String titleFr;
    private String titleAr;
    private String descEn;
    private String descFr;
    private String descAr;
    private String icon;
    private int pointsThreshold;
    private boolean isUnlocked;
    private String unlockedAt;

    public Achievement() {
    }

    public Achievement(long id, String code, String titleEn, String titleFr, String titleAr,
                       String descEn, String descFr, String descAr, String icon,
                       int pointsThreshold, boolean isUnlocked, String unlockedAt) {
        this.id = id;
        this.code = code;
        this.titleEn = titleEn;
        this.titleFr = titleFr;
        this.titleAr = titleAr;
        this.descEn = descEn;
        this.descFr = descFr;
        this.descAr = descAr;
        this.icon = icon;
        this.pointsThreshold = pointsThreshold;
        this.isUnlocked = isUnlocked;
        this.unlockedAt = unlockedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getTitleFr() {
        return titleFr;
    }

    public void setTitleFr(String titleFr) {
        this.titleFr = titleFr;
    }

    public String getTitleAr() {
        return titleAr;
    }

    public void setTitleAr(String titleAr) {
        this.titleAr = titleAr;
    }

    public String getDescEn() {
        return descEn;
    }

    public void setDescEn(String descEn) {
        this.descEn = descEn;
    }

    public String getDescFr() {
        return descFr;
    }

    public void setDescFr(String descFr) {
        this.descFr = descFr;
    }

    public String getDescAr() {
        return descAr;
    }

    public void setDescAr(String descAr) {
        this.descAr = descAr;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getPointsThreshold() {
        return pointsThreshold;
    }

    public void setPointsThreshold(int pointsThreshold) {
        this.pointsThreshold = pointsThreshold;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public String getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(String unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    public String getLocalizedTitle(String langCode) {
        if ("fr".equalsIgnoreCase(langCode)) {
            return titleFr != null && !titleFr.isEmpty() ? titleFr : titleEn;
        } else if ("ar".equalsIgnoreCase(langCode)) {
            return titleAr != null && !titleAr.isEmpty() ? titleAr : titleEn;
        }
        return titleEn;
    }

    public String getLocalizedDesc(String langCode) {
        if ("fr".equalsIgnoreCase(langCode)) {
            return descFr != null && !descFr.isEmpty() ? descFr : descEn;
        } else if ("ar".equalsIgnoreCase(langCode)) {
            return descAr != null && !descAr.isEmpty() ? descAr : descEn;
        }
        return descEn;
    }
}

package com.example.masrouf.model;

/**
 * RÔLE : Entité représentant une ligne de gain de points dans le journal de l'utilisateur (Figma 21).
 * COUCHE MVC : Modèle (Model).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Architecture MVC).
 *   - Spécifications Gamification : Journalisation des points et protection anti-doublon par période.
 */
public class UserPoints {

    private long id;
    private long userId;
    private long ruleId;
    private int points;
    private String reason;
    private String periodKey;   // Clé unique pour empêcher les doublons (ex: "2026-11_BUDGET")
    private String earnedAt;

    public UserPoints() {
    }

    public UserPoints(long id, long userId, long ruleId, int points, String reason, String periodKey, String earnedAt) {
        this.id = id;
        this.userId = userId;
        this.ruleId = ruleId;
        this.points = points;
        this.reason = reason;
        this.periodKey = periodKey;
        this.earnedAt = earnedAt;
    }

    public UserPoints(long userId, long ruleId, int points, String reason, String periodKey, String earnedAt) {
        this.userId = userId;
        this.ruleId = ruleId;
        this.points = points;
        this.reason = reason;
        this.periodKey = periodKey;
        this.earnedAt = earnedAt;
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

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPeriodKey() {
        return periodKey;
    }

    public void setPeriodKey(String periodKey) {
        this.periodKey = periodKey;
    }

    public String getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(String earnedAt) {
        this.earnedAt = earnedAt;
    }
}

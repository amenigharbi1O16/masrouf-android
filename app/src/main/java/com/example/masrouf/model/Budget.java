package com.example.masrouf.model;

import com.example.masrouf.util.MillimesUtil;

/**
 * RÔLE : Entité représentant un budget périodique défini par l'utilisateur (Figma 07, 09, 11).
 * COUCHE MVC : Modèle (Model).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec entité de données budgétaires).
 *   - Matrice §4 ligne 35 (Gestion des statuts budgétaires : HEALTHY < 80%, WARNING 80-100%, EXCEEDED > 100%).
 *   - Précision financière exacte en millimes (1 DT = 1000 millimes).
 */
public class Budget {

    // Constantes de statut budgétaire (conforme aux maquettes Figma 07, 27, 28)
    public static final String STATUS_HEALTHY = "HEALTHY";   // Dépenses < 80% du budget
    public static final String STATUS_WARNING = "WARNING";   // Dépenses entre 80% et 100%
    public static final String STATUS_EXCEEDED = "EXCEEDED"; // Dépenses > 100% (dépassement)

    private long id;
    private long userId;
    private double totalAmount;             // Montant total en Dinars Tunisiens (DT)
    private long totalAmountMillimes;       // Montant total en millimes (entier exact)
    private String startDate;               // Format YYYY-MM-DD
    private String endDate;                 // Format YYYY-MM-DD
    private String monthKey;                // Clé de période ex: "2026-11"
    private String status;                  // HEALTHY, WARNING, EXCEEDED

    public Budget() {
        this.status = STATUS_HEALTHY;
    }

    public Budget(long id, long userId, double totalAmount, String startDate, String endDate, String status) {
        this.id = id;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.totalAmountMillimes = MillimesUtil.dtToMillimes(totalAmount);
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthKey = extractMonthKey(startDate);
        this.status = status;
    }

    public Budget(long userId, double totalAmount, String startDate, String endDate) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.totalAmountMillimes = MillimesUtil.dtToMillimes(totalAmount);
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthKey = extractMonthKey(startDate);
        this.status = STATUS_HEALTHY;
    }

    private String extractMonthKey(String dateStr) {
        if (dateStr != null && dateStr.length() >= 7) {
            return dateStr.substring(0, 7);
        }
        return "";
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

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getMontantTotal() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        this.totalAmountMillimes = MillimesUtil.dtToMillimes(totalAmount);
    }

    public long getTotalAmountMillimes() {
        return totalAmountMillimes;
    }

    public void setTotalAmountMillimes(long totalAmountMillimes) {
        this.totalAmountMillimes = totalAmountMillimes;
        this.totalAmount = MillimesUtil.millimesToDt(totalAmountMillimes);
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
        if (this.monthKey == null || this.monthKey.isEmpty()) {
            this.monthKey = extractMonthKey(startDate);
        }
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getMonthKey() {
        return monthKey;
    }

    public void setMonthKey(String monthKey) {
        this.monthKey = monthKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormattedTotal() {
        return MillimesUtil.formatMillimes(totalAmountMillimes);
    }
}

package com.example.masrouf.model;

import com.example.masrouf.util.MillimesUtil;

/**
 * RÔLE : Entité représentant l'enveloppe allouée à une catégorie au sein d'un budget (Figma 10 & 11).
 * COUCHE MVC : Modèle (Model).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (MVC avec entité de données).
 *   - Matrice §4 ligne 32 (Calculs de montants alloués vs consommés).
 *   - Précision en millimes pour éviter les erreurs d'arrondi.
 */
public class BudgetAllocation {

    private long id;
    private long budgetId;
    private long categoryId;
    private String categoryName;
    private double allocatedAmount;         // Montant alloué en DT (ex: 500 DT pour Food)
    private long allocatedMillimes;         // Montant alloué en millimes (ex: 500000)
    private double spentAmount;             // Montant consommé en DT (ex: 320 DT)
    private long spentMillimes;             // Montant consommé en millimes (ex: 320000)

    public BudgetAllocation() {
    }

    public BudgetAllocation(long id, long budgetId, long categoryId, String categoryName,
                            double allocatedAmount, double spentAmount) {
        this.id = id;
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.allocatedAmount = allocatedAmount;
        this.allocatedMillimes = MillimesUtil.dtToMillimes(allocatedAmount);
        this.spentAmount = spentAmount;
        this.spentMillimes = MillimesUtil.dtToMillimes(spentAmount);
    }

    public BudgetAllocation(long budgetId, long categoryId, String categoryName, double allocatedAmount) {
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.allocatedAmount = allocatedAmount;
        this.allocatedMillimes = MillimesUtil.dtToMillimes(allocatedAmount);
        this.spentAmount = 0.0;
        this.spentMillimes = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public double getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(double allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
        this.allocatedMillimes = MillimesUtil.dtToMillimes(allocatedAmount);
    }

    public long getAllocatedMillimes() {
        return allocatedMillimes;
    }

    public void setAllocatedMillimes(long allocatedMillimes) {
        this.allocatedMillimes = allocatedMillimes;
        this.allocatedAmount = MillimesUtil.millimesToDt(allocatedMillimes);
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
        this.spentMillimes = MillimesUtil.dtToMillimes(spentAmount);
    }

    public long getSpentMillimes() {
        return spentMillimes;
    }

    public void setSpentMillimes(long spentMillimes) {
        this.spentMillimes = spentMillimes;
        this.spentAmount = MillimesUtil.millimesToDt(spentMillimes);
    }

    public double getRemainingAmount() {
        return allocatedAmount - spentAmount;
    }

    public long getRemainingMillimes() {
        return allocatedMillimes - spentMillimes;
    }

    public double getUsagePercentage() {
        if (allocatedMillimes <= 0) return 0.0;
        return ((double) spentMillimes / (double) allocatedMillimes) * 100.0;
    }

    public String getFormattedAllocated() {
        return MillimesUtil.formatMillimes(allocatedMillimes);
    }

    public String getFormattedSpent() {
        return MillimesUtil.formatMillimes(spentMillimes);
    }

    public String getFormattedRemaining() {
        return MillimesUtil.formatMillimes(getRemainingMillimes());
    }
}

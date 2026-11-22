package com.example.masrouf.model;

/**
 * Modèle représentant les fonds globaux du compte familial.
 * Précision financière exacte en millimes (INTEGER).
 */
public class FamilyFunds {
    private long familyId;
    private long parentId;
    private String familyName;
    private long totalFundsMillimes;
    private long availableFundsMillimes;
    private long allocatedFundsMillimes;
    private String createdAt;

    public FamilyFunds() {
    }

    public FamilyFunds(long familyId, long parentId, String familyName,
                       long totalFundsMillimes, long availableFundsMillimes,
                       long allocatedFundsMillimes, String createdAt) {
        this.familyId = familyId;
        this.parentId = parentId;
        this.familyName = familyName;
        this.totalFundsMillimes = totalFundsMillimes;
        this.availableFundsMillimes = availableFundsMillimes;
        this.allocatedFundsMillimes = allocatedFundsMillimes;
        this.createdAt = createdAt;
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public long getTotalFundsMillimes() {
        return totalFundsMillimes;
    }

    public void setTotalFundsMillimes(long totalFundsMillimes) {
        this.totalFundsMillimes = totalFundsMillimes;
    }

    public long getAvailableFundsMillimes() {
        return availableFundsMillimes;
    }

    public void setAvailableFundsMillimes(long availableFundsMillimes) {
        this.availableFundsMillimes = availableFundsMillimes;
    }

    public long getAllocatedFundsMillimes() {
        return allocatedFundsMillimes;
    }

    public void setAllocatedFundsMillimes(long allocatedFundsMillimes) {
        this.allocatedFundsMillimes = allocatedFundsMillimes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public double getTotalFundsDt() {
        return totalFundsMillimes / 1000.0;
    }

    public double getAvailableFundsDt() {
        return availableFundsMillimes / 1000.0;
    }

    public double getAllocatedFundsDt() {
        return allocatedFundsMillimes / 1000.0;
    }

    // Calcule automatiquement la part propre du parent : Budget Global Total - Somme allouée aux enfants
    public long getParentBalanceMillimes() {
        return Math.max(0, totalFundsMillimes - allocatedFundsMillimes);
    }

    // Calcule automatiquement la part du parent en Dinars
    public double getParentBalanceDt() {
        return getParentBalanceMillimes() / 1000.0;
    }
}

package com.example.masrouf.model;

/**
 * Modèle de permissions et plafonds configurés par le parent pour chaque enfant.
 */
public class ChildPermission {
    private long childId;
    private boolean canViewFamilyBudget;
    private long requiresApprovalAboveMillimes;
    private long dailySpendingLimitMillimes;
    private boolean isAccountActive;

    public ChildPermission() {
        this.canViewFamilyBudget = false;
        this.requiresApprovalAboveMillimes = 20000; // 20 DT par défaut
        this.dailySpendingLimitMillimes = 50000;    // 50 DT par défaut
        this.isAccountActive = true;
    }

    public ChildPermission(long childId, boolean canViewFamilyBudget,
                           long requiresApprovalAboveMillimes,
                           long dailySpendingLimitMillimes, boolean isAccountActive) {
        this.childId = childId;
        this.canViewFamilyBudget = canViewFamilyBudget;
        this.requiresApprovalAboveMillimes = requiresApprovalAboveMillimes;
        this.dailySpendingLimitMillimes = dailySpendingLimitMillimes;
        this.isAccountActive = isAccountActive;
    }

    public long getChildId() {
        return childId;
    }

    public void setChildId(long childId) {
        this.childId = childId;
    }

    public boolean isCanViewFamilyBudget() {
        return canViewFamilyBudget;
    }

    public void setCanViewFamilyBudget(boolean canViewFamilyBudget) {
        this.canViewFamilyBudget = canViewFamilyBudget;
    }

    public long getRequiresApprovalAboveMillimes() {
        return requiresApprovalAboveMillimes;
    }

    public void setRequiresApprovalAboveMillimes(long requiresApprovalAboveMillimes) {
        this.requiresApprovalAboveMillimes = requiresApprovalAboveMillimes;
    }

    public long getDailySpendingLimitMillimes() {
        return dailySpendingLimitMillimes;
    }

    public void setDailySpendingLimitMillimes(long dailySpendingLimitMillimes) {
        this.dailySpendingLimitMillimes = dailySpendingLimitMillimes;
    }

    public boolean isAccountActive() {
        return isAccountActive;
    }

    public void setAccountActive(boolean accountActive) {
        isAccountActive = accountActive;
    }

    public double getDailyLimitDt() {
        return dailySpendingLimitMillimes / 1000.0;
    }

    public double getApprovalThresholdDt() {
        return requiresApprovalAboveMillimes / 1000.0;
    }
}

package com.example.masrouf.model;

/**
 * Modèle représentant une allocation d'argent donnée par le parent à un enfant.
 */
public class FamilyAllocation {
    private long id;
    private long familyId;
    private long parentId;
    private long childId;
    private String childName;
    private long amountMillimes;
    private String description;
    private String date;
    private String createdAt;

    public FamilyAllocation() {
    }

    public FamilyAllocation(long id, long familyId, long parentId, long childId,
                            String childName, long amountMillimes, String description,
                            String date, String createdAt) {
        this.id = id;
        this.familyId = familyId;
        this.parentId = parentId;
        this.childId = childId;
        this.childName = childName;
        this.amountMillimes = amountMillimes;
        this.description = description;
        this.date = date;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public long getChildId() {
        return childId;
    }

    public void setChildId(long childId) {
        this.childId = childId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public long getAmountMillimes() {
        return amountMillimes;
    }

    public void setAmountMillimes(long amountMillimes) {
        this.amountMillimes = amountMillimes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public double getAmountDt() {
        return amountMillimes / 1000.0;
    }
}

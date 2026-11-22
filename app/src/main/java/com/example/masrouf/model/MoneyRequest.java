package com.example.masrouf.model;

/**
 * Modèle représentant une demande d'argent formulée par un enfant à son parent.
 */
public class MoneyRequest {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private long id;
    private long familyId;
    private long childId;
    private String childName;
    private long parentId;
    private long amountMillimes;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private String decisionNote;
    private String requestDate;
    private String decisionDate;

    public MoneyRequest() {
        this.status = STATUS_PENDING;
    }

    public MoneyRequest(long id, long familyId, long childId, String childName,
                        long parentId, long amountMillimes, String reason,
                        String status, String decisionNote, String requestDate, String decisionDate) {
        this.id = id;
        this.familyId = familyId;
        this.childId = childId;
        this.childName = childName;
        this.parentId = parentId;
        this.amountMillimes = amountMillimes;
        this.reason = reason;
        this.status = status != null ? status : STATUS_PENDING;
        this.decisionNote = decisionNote;
        this.requestDate = requestDate;
        this.decisionDate = decisionDate;
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

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public long getAmountMillimes() {
        return amountMillimes;
    }

    public void setAmountMillimes(long amountMillimes) {
        this.amountMillimes = amountMillimes;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(String decisionDate) {
        this.decisionDate = decisionDate;
    }

    public double getAmountDt() {
        return amountMillimes / 1000.0;
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(status);
    }

    public boolean isApproved() {
        return STATUS_APPROVED.equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return STATUS_REJECTED.equalsIgnoreCase(status);
    }
}

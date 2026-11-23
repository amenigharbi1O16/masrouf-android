package com.example.masrouf.model;

/**
 * Entité représentant une notification utilisateur enregistrée dans SQLite.
 * Modèle (MVC).
 */
public class AppNotification {

    public static final String TYPE_WELCOME = "WELCOME";
    public static final String TYPE_BUDGET_INITIALIZED = "BUDGET_INITIALIZED";
    public static final String TYPE_BUDGET_ALLOCATED = "BUDGET_ALLOCATED";
    public static final String TYPE_PARENT_BUDGET_ASSIGNED = "PARENT_BUDGET_ASSIGNED";
    public static final String TYPE_PERIOD_STARTED = "PERIOD_STARTED";
    public static final String TYPE_CATEGORY_ADDED = "CATEGORY_ADDED";
    public static final String TYPE_ALLOCATION_CHANGED = "ALLOCATION_CHANGED";
    public static final String TYPE_WARNING_80 = "WARNING_80";
    public static final String TYPE_EXCEEDED_100 = "EXCEEDED_100";
    public static final String TYPE_CATEGORY_LIMIT = "CATEGORY_LIMIT";
    public static final String TYPE_SPENDING_SUMMARY = "SPENDING_SUMMARY";

    public static final String ROUTE_BUDGET = "BUDGET";
    public static final String ROUTE_ANALYTICS = "ANALYTICS";
    public static final String ROUTE_CATEGORIES = "CATEGORIES";

    private long id;
    private long userId;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private long timestamp;
    private String periodKey;
    private String actionRoute;

    public AppNotification() {
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.type = TYPE_WELCOME;
    }

    public AppNotification(long userId, String title, String message, String type, String actionRoute) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type != null ? type : TYPE_WELCOME;
        this.isRead = false;
        this.timestamp = System.currentTimeMillis();
        this.actionRoute = actionRoute;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPeriodKey() {
        return periodKey;
    }

    public void setPeriodKey(String periodKey) {
        this.periodKey = periodKey;
    }

    public String getActionRoute() {
        return actionRoute;
    }

    public void setActionRoute(String actionRoute) {
        this.actionRoute = actionRoute;
    }
}

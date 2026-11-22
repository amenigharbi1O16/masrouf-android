package com.example.masrouf.model;

/**
 * RÔLE : Entité représentant un utilisateur dans l'application Masrouf.
 * COUCHE MVC : Modèle (Model).
 * EXIGENCE DU PROF COUVERTE : Exigence n°1 (2 types d'utilisateurs : ADMIN et USER)
 *                             et Exigence n°9 (Architecture MVC avec Modèle pur).
 *
 * Cette classe est un POJO (Plain Old Java Object) sans aucune dépendance vers Android
 * ni vers le code SQL, conformément au principe de séparation des responsabilités.
 */
public class User {

    // Constantes définissant les rôles dans l'application
    public static final String ROLE_ADMIN = "ADMIN";     // Rétrocompatibilité : migré automatiquement vers PARENT
    public static final String ROLE_PARENT = "PARENT";   // Administrateur familial (Compte Familial)
    public static final String ROLE_CHILD = "CHILD";     // Compte Enfant supervisé
    public static final String ROLE_USER = "USER";       // Utilisateur individuel (Compte Personnel)

    // Constantes de niveaux de gamification (Figma 21)
    public static final String LEVEL_BEGINNER = "Beginner"; // 0 - 299 points
    public static final String LEVEL_SAVER = "Saver";       // 300 - 999 points
    public static final String LEVEL_EXPERT = "Expert";     // 1000+ points

    private long id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String role;
    private boolean isActive;
    private boolean isTemporaryPassword;
    private String createdAt;

    // Champs spécifiques au compte familial
    private long familyId;
    private long parentId;
    private long balanceMillimes; // Solde courant pour compte Enfant

    // Gamification
    private int points;
    private int streakCount;
    private String level;

    public User() {
        this.level = LEVEL_BEGINNER;
    }

    public User(long id, String email, String passwordHash, String fullName,
                String role, boolean isActive, boolean isTemporaryPassword, String createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.isTemporaryPassword = isTemporaryPassword;
        this.createdAt = createdAt;
        this.points = 0;
        this.streakCount = 0;
        this.level = LEVEL_BEGINNER;
    }

    public User(long id, String email, String passwordHash, String fullName,
                String role, boolean isActive, boolean isTemporaryPassword, String createdAt,
                int points, int streakCount, String level) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.isTemporaryPassword = isTemporaryPassword;
        this.createdAt = createdAt;
        this.points = points;
        this.streakCount = streakCount;
        this.level = level != null ? level : LEVEL_BEGINNER;
    }

    public User(String email, String passwordHash, String fullName, String role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.isActive = true;
        this.isTemporaryPassword = false;
        this.createdAt = String.valueOf(System.currentTimeMillis());
        this.points = 0;
        this.streakCount = 0;
        this.level = LEVEL_BEGINNER;
    }

    public User(String fullName, String email, String passwordHash, String salt, String role) {
        this(email, passwordHash, fullName, role);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isTemporaryPassword() {
        return isTemporaryPassword;
    }

    public void setTemporaryPassword(boolean temporaryPassword) {
        isTemporaryPassword = temporaryPassword;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
        updateCalculatedLevel();
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void updateCalculatedLevel() {
        if (points >= 1000) {
            this.level = LEVEL_EXPERT;
        } else if (points >= 300) {
            this.level = LEVEL_SAVER;
        } else {
            this.level = LEVEL_BEGINNER;
        }
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(this.role) || ROLE_PARENT.equalsIgnoreCase(this.role);
    }

    public boolean isParent() {
        return ROLE_PARENT.equalsIgnoreCase(this.role) || ROLE_ADMIN.equalsIgnoreCase(this.role);
    }

    public boolean isChild() {
        return ROLE_CHILD.equalsIgnoreCase(this.role);
    }

    public boolean isIndividualUser() {
        return ROLE_USER.equalsIgnoreCase(this.role);
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

    public long getBalanceMillimes() {
        return balanceMillimes;
    }

    public void setBalanceMillimes(long balanceMillimes) {
        this.balanceMillimes = balanceMillimes;
    }

    public double getBalance() {
        return balanceMillimes / 1000.0;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", points=" + points +
                ", level='" + level + '\'' +
                '}';
    }
}

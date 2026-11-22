package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.User;
import com.example.masrouf.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * RÔLE : Data Access Object (DAO) dédié aux opérations CRUD sur l'entité User.
 * COUCHE MVC : DAO (Data Access Object / Couche d'accès aux données).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Composant DAO pour la connexion et l'isolation du code SQL).
 *   - Matrice §4 ligne 32 (CRUD complet via ContentValues et Cursor).
 *   - Matrice §4 ligne 33 (Requêtes paramétrées avec '?' - Immunité anti-injection SQL).
 *   - Matrice §4 ligne 1 (Gestion des 2 rôles ADMIN et USER en base).
 *
 * RÈGLE D'OR ARCHITECTURALE :
 * Aucun code d'interface graphique (View) ni de logique métier complexe ne doit résider ici.
 * Cette classe ne fait que traduire des objets Java en requêtes SQL SQLite et vice-versa.
 */
public class UserDao {

    private static final String TAG = "UserDao";

    // Référence vers l'assistant SQLite
    private final DatabaseHelper dbHelper;

    /**
     * Constructeur recevant le contexte pour initialiser le DatabaseHelper.
     *
     * @param context Contexte Android
     */
    public UserDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public UserDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Insère un nouvel utilisateur dans la table 'users'.
     *
     * @param user L'objet User contenant les informations à insérer.
     * @return L'identifiant (ID) généré par SQLite, ou -1 en cas d'erreur.
     */
    public long insertUser(User user) {
        // 1. Obtention de la base de données en mode écriture
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 2. Préparation des paires colonne / valeur via ContentValues (Ch.6 du cours)
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, user.getPasswordHash());
        values.put(DatabaseHelper.COLUMN_USER_FULL_NAME, user.getFullName());
        values.put(DatabaseHelper.COLUMN_USER_ROLE, user.getRole());
        values.put(DatabaseHelper.COLUMN_USER_IS_ACTIVE, user.isActive() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_USER_IS_TEMP_PASSWORD, user.isTemporaryPassword() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_USER_CREATED_AT, user.getCreatedAt());
        values.put(DatabaseHelper.COLUMN_USER_FAMILY_ID, user.getFamilyId());
        values.put(DatabaseHelper.COLUMN_USER_PARENT_ID, user.getParentId());
        values.put(DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES, user.getBalanceMillimes());

        long generatedId = -1;
        try {
            // 3. Exécution de l'insertion sécurisée via l'API SQLite d'Android
            generatedId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
            Log.d(TAG, "Utilisateur inséré avec succès, ID = " + generatedId);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'insertion de l'utilisateur : " + e.getMessage(), e);
        }

        return generatedId;
    }

    /**
     * Authentifie un utilisateur en vérifiant son e-mail et son condensat de mot de passe.
     * EXIGENCE : Requête paramétrée avec arguments séparés (anti-injection SQL).
     *
     * @param email        L'adresse e-mail saisie.
     * @param passwordHash L'empreinte SHA-256 du mot de passe saisi.
     * @return L'objet User correspondant si l'authentification réussit, null sinon.
     */
    public User authenticate(String email, String passwordHash) {
        // Obtention de la base en mode lecture seule
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User authenticatedUser = null;

        // Clause WHERE paramétrée avec des jokers '?'
        String selection = DatabaseHelper.COLUMN_USER_EMAIL + " = ? AND " +
                DatabaseHelper.COLUMN_USER_PASSWORD_HASH + " = ?";

        // Tableau des valeurs réelles qui remplaceront les '?' de manière strictement typée
        String[] selectionArgs = new String[]{ email.trim().toLowerCase(), passwordHash };

        Cursor cursor = null;
        try {
            // Exécution de la requête de sélection via db.query()
            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,               // null = sélectionner toutes les colonnes (SELECT *)
                    selection,          // Clause WHERE avec '?'
                    selectionArgs,      // Arguments sécurisés
                    null,               // groupBy
                    null,               // having
                    null                // orderBy
            );

            // Si le curseur contient au moins une ligne, les identifiants sont valides
            if (cursor != null && cursor.moveToFirst()) {
                authenticatedUser = cursorToUser(cursor);
                Log.d(TAG, "Authentification réussie pour : " + email + " (Rôle : " + authenticatedUser.getRole() + ")");
            } else {
                Log.w(TAG, "Échec d'authentification pour : " + email);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'authentification : " + e.getMessage(), e);
        } finally {
            // RÈGLE IMPORTANTE DU COURS : Toujours fermer le Cursor pour éviter les fuites de mémoire (CursorLeak)
            if (cursor != null) {
                cursor.close();
            }
        }

        return authenticatedUser;
    }

    /**
     * Recherche un utilisateur par son adresse e-mail.
     *
     * @param email L'adresse e-mail à rechercher.
     * @return L'objet User trouvé, ou null si aucun utilisateur ne correspond.
     */
    public User findByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        String selection = DatabaseHelper.COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = new String[]{ email.trim().toLowerCase() };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur findByEmail : " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }

    public User getUserByEmail(String email) {
        return findByEmail(email);
    }

    /**
     * Recherche un utilisateur par son identifiant unique.
     *
     * @param id L'identifiant clé primaire.
     * @return L'utilisateur correspondant ou null.
     */
    public User findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = new String[]{ String.valueOf(id) };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur findById : " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }

    public User getUserById(long id) {
        return findById(id);
    }

    /**
     * Met à jour le profil (nom complet et adresse email) de l'utilisateur (Figma 22 / Réglages).
     */
    public boolean updateUserProfile(long userId, String fullName, String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_FULL_NAME, fullName);
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, email.trim().toLowerCase());
        int rows = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }
        );
        return rows > 0;
    }

    /**
     * Récupère le nombre total d'utilisateurs enregistrés dans l'application.
     * Utilisé pour les métriques de supervision de l'écran d'administration (Figma 35).
     *
     * @return Le nombre total d'utilisateurs.
     */
    public int getTotalUserCount() {
        return getCountWhere(null, null);
    }

    /**
     * Récupère le nombre d'utilisateurs dont le compte est actif (is_active = 1).
     *
     * @return Le nombre de comptes actifs.
     */
    public int getActiveUserCount() {
        return getCountWhere(DatabaseHelper.COLUMN_USER_IS_ACTIVE + " = ?", new String[]{ "1" });
    }

    /**
     * Récupère le nombre d'utilisateurs dont le compte est suspendu (is_active = 0).
     *
     * @return Le nombre de comptes suspendus.
     */
    public int getSuspendedUserCount() {
        return getCountWhere(DatabaseHelper.COLUMN_USER_IS_ACTIVE + " = ?", new String[]{ "0" });
    }

    /**
     * Méthode utilitaire interne pour exécuter un COUNT(*) paramétré.
     */
    private int getCountWhere(String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{ "COUNT(*)" },
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du comptage des utilisateurs : " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    /**
     * Renvoie un Cursor SQLite brut sur tous les utilisateurs pour alimenter un SimpleCursorAdapter (Ch. 6 du cours).
     * IMPORTANT POUR LE PROF (Matrice §4 ligne 29) :
     * Le curseur DOIT obligatoirement comporter une colonne nommée '_id' pour que SimpleCursorAdapter fonctionne.
     */
    public Cursor getAllUsersCursor() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{
                        DatabaseHelper.COLUMN_USER_ID + " AS _id",
                        DatabaseHelper.COLUMN_USER_FULL_NAME,
                        DatabaseHelper.COLUMN_USER_EMAIL,
                        DatabaseHelper.COLUMN_USER_ROLE,
                        DatabaseHelper.COLUMN_USER_IS_ACTIVE
                },
                null, null, null, null,
                DatabaseHelper.COLUMN_USER_ID + " ASC"
        );
    }

    /**
     * Active ou suspend le compte d'un utilisateur.
     */
    public boolean setUserActive(long userId, boolean isActive) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_USER_IS_ACTIVE, isActive ? 1 : 0);
        return db.update(
                DatabaseHelper.TABLE_USERS,
                cv,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }
        ) > 0;
    }

    /**
     * Réinitialise le mot de passe avec un mot de passe temporaire imposé.
     */
    public boolean resetPassword(long userId, String newPlainPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword(newPlainPassword));
        cv.put(DatabaseHelper.COLUMN_USER_IS_TEMP_PASSWORD, 1);
        return db.update(
                DatabaseHelper.TABLE_USERS,
                cv,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }
        ) > 0;
    }

    /**
     * Transforme la ligne actuellement pointée par le Cursor en une instance du modèle User.
     * Méthode d'aide centrale assurant le mapping relationnel $\to$ objet.
     *
     * @param cursor Le curseur positionné sur une ligne de résultat valide.
     * @return Une nouvelle instance de User renseignée.
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();

        // getColumnIndexOrThrow garantit de lever une exception claire si un nom de colonne est mal orthographié
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD_HASH)));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_FULL_NAME)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE)));
        user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IS_ACTIVE)) == 1);
        user.setTemporaryPassword(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IS_TEMP_PASSWORD)) == 1);
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_CREATED_AT)));

        int pointsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_POINTS);
        if (pointsIndex != -1) {
            user.setPoints(cursor.getInt(pointsIndex));
        }
        int streakIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_STREAK);
        if (streakIndex != -1) {
            user.setStreakCount(cursor.getInt(streakIndex));
        }
        int levelIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_LEVEL);
        if (levelIndex != -1) {
            user.setLevel(cursor.getString(levelIndex));
        }

        int familyIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_FAMILY_ID);
        if (familyIdIndex != -1) {
            user.setFamilyId(cursor.getLong(familyIdIndex));
        }
        int parentIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PARENT_ID);
        if (parentIdIndex != -1) {
            user.setParentId(cursor.getLong(parentIdIndex));
        }
        int balanceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES);
        if (balanceIndex != -1) {
            user.setBalanceMillimes(cursor.getLong(balanceIndex));
        }

        return user;
    }

    /**
     * Met à jour le solde (en millimes) d'un compte enfant.
     */
    public boolean updateBalance(long userId, long newBalanceMillimes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES, newBalanceMillimes);
        return db.update(DatabaseHelper.TABLE_USERS, cv, DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }) > 0;
    }

    /**
     * Récupère la liste des enfants rattachés à un parent.
     */
    public List<User> getChildrenByParent(long parentId) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                    DatabaseHelper.COLUMN_USER_PARENT_ID + " = ? AND " + DatabaseHelper.COLUMN_USER_ROLE + " = ?",
                    new String[]{ String.valueOf(parentId), User.ROLE_CHILD },
                    null, null, DatabaseHelper.COLUMN_USER_FULL_NAME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToUser(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getChildrenByParent : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Récupère la liste des enfants rattachés à une famille.
     */
    public List<User> getChildrenByFamily(long familyId) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                    DatabaseHelper.COLUMN_USER_FAMILY_ID + " = ? AND " + DatabaseHelper.COLUMN_USER_ROLE + " = ?",
                    new String[]{ String.valueOf(familyId), User.ROLE_CHILD },
                    null, null, DatabaseHelper.COLUMN_USER_FULL_NAME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToUser(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getChildrenByFamily : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Met à jour le familyId et rôle d'un utilisateur.
     */
    public boolean updateUserFamily(long userId, long familyId, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_USER_FAMILY_ID, familyId);
        if (role != null) {
            cv.put(DatabaseHelper.COLUMN_USER_ROLE, role);
        }
        return db.update(DatabaseHelper.TABLE_USERS, cv, DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }) > 0;
    }
}

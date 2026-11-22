package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * RÔLE : Data Access Object pour la gestion des catégories de budgets et dépenses (Figma 10, 39, 40, 41).
 * COUCHE MVC : DAO.
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Composant DAO pour la connexion à la base).
 *   - Matrice §4 ligne 32 (CRUD complet : Ajout, Modification, Suppression, Consultation).
 *   - Matrice §4 ligne 33 (Requêtes paramétrées anti-injection).
 */
public class CategoryDao {

    private static final String TAG = "CategoryDao";
    private final DatabaseHelper dbHelper;

    public CategoryDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public CategoryDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null, null, null, null, null, DatabaseHelper.COLUMN_CAT_NAME_EN + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToCategory(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getAllCategories : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public List<Category> getCategoriesByType(String type) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_CATEGORIES,
                    null,
                    DatabaseHelper.COLUMN_CAT_TYPE + " = ?",
                    new String[]{ type },
                    null, null,
                    DatabaseHelper.COLUMN_CAT_NAME_EN + " ASC"
            );
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToCategory(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getCategoriesByType : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public Category getCategoryById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Category cat = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_CATEGORIES,
                    null,
                    DatabaseHelper.COLUMN_CAT_ID + " = ?",
                    new String[]{ String.valueOf(id) },
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                cat = cursorToCategory(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getCategoryById : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return cat;
    }

    public long insertCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_CAT_NAME_EN, category.getNameEn());
        cv.put(DatabaseHelper.COLUMN_CAT_NAME_FR, category.getNameFr());
        cv.put(DatabaseHelper.COLUMN_CAT_NAME_AR, category.getNameAr());
        cv.put(DatabaseHelper.COLUMN_CAT_TYPE, category.getType());
        cv.put(DatabaseHelper.COLUMN_CAT_COLOR, category.getColorHex());
        cv.put(DatabaseHelper.COLUMN_CAT_ICON, category.getIconName());
        cv.put(DatabaseHelper.COLUMN_CAT_DEFAULT_PCT, category.getDefaultPercentage());
        cv.put(DatabaseHelper.COLUMN_CAT_IS_CUSTOM, category.isCustom() ? 1 : 0);
        cv.put(DatabaseHelper.COLUMN_CAT_USER_ID, category.getUserId());

        return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, cv);
    }

    public int updateCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_CAT_NAME_EN, category.getNameEn());
        cv.put(DatabaseHelper.COLUMN_CAT_NAME_FR, category.getNameFr());
        cv.put(DatabaseHelper.COLUMN_CAT_NAME_AR, category.getNameAr());
        cv.put(DatabaseHelper.COLUMN_CAT_TYPE, category.getType());
        cv.put(DatabaseHelper.COLUMN_CAT_COLOR, category.getColorHex());
        cv.put(DatabaseHelper.COLUMN_CAT_ICON, category.getIconName());
        cv.put(DatabaseHelper.COLUMN_CAT_DEFAULT_PCT, category.getDefaultPercentage());

        return db.update(DatabaseHelper.TABLE_CATEGORIES, cv, DatabaseHelper.COLUMN_CAT_ID + " = ?", new String[]{ String.valueOf(category.getId()) });
    }

    /**
     * Insère une catégorie personnalisée créée par l'utilisateur et l'associe dans user_categories.
     * Cette fonction enregistre la catégorie en SQLite et met à jour instantanément la liste sur l'écran.
     *
     * @param userId   Identifiant de l'utilisateur créateur
     * @param name     Nom de la catégorie (FR / EN / AR)
     * @param type     Type de la catégorie (Produit vs Dépense)
     * @param colorHex Code couleur hexadécimal
     * @param iconName Nom de l'icône associée
     * @return L'identifiant généré pour la nouvelle catégorie
     */
    public long insertUserCategory(long userId, String name, String type, String colorHex, String iconName) {
        // Ouvre la base de données en mode écriture
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Prépare le conteneur des valeurs à insérer dans la table categories
        ContentValues cv = new ContentValues();
        // Renseigne le nom en anglais avec la valeur saisie
        cv.put(DatabaseHelper.COLUMN_CAT_NAME_EN, name);
        // Renseigne le nom en français avec la valeur saisie
        cv.put(DatabaseHelper.COLUMN_CAT_NAME_FR, name);
        // Renseigne le nom en arabe avec la valeur saisie
        cv.put(DatabaseHelper.COLUMN_CAT_NAME_AR, name);
        // Spécifie le type de la catégorie (product ou expense)
        cv.put(DatabaseHelper.COLUMN_CAT_TYPE, type);
        // Définit la couleur hexadécimale (par défaut le vert forêt #102B27)
        cv.put(DatabaseHelper.COLUMN_CAT_COLOR, colorHex != null ? colorHex : "#102B27");
        // Définit l'icône de la catégorie (coutellerie pour produit, reçu pour dépense)
        cv.put(DatabaseHelper.COLUMN_CAT_ICON, iconName != null ? iconName : ("product".equals(type) ? "ic_cutlery" : "ic_receipt"));
        // Fixe le pourcentage par défaut à 0.0 pour les catégories personnalisées
        cv.put(DatabaseHelper.COLUMN_CAT_DEFAULT_PCT, 0.0);
        // Marque la catégorie comme personnalisée (is_custom = 1)
        cv.put(DatabaseHelper.COLUMN_CAT_IS_CUSTOM, 1);
        // Associe l'identifiant de l'utilisateur propriétaire
        cv.put(DatabaseHelper.COLUMN_CAT_USER_ID, userId);

        // Insère la nouvelle catégorie dans la table categories
        long catId = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, cv);

        // Si l'insertion a réussi et que l'utilisateur est valide
        if (catId > 0 && userId > 0) {
            // Prépare l'association dans la table user_categories
            ContentValues uc = new ContentValues();
            // Associe l'identifiant de l'utilisateur
            uc.put(DatabaseHelper.COLUMN_UC_USER_ID, userId);
            // Associe l'identifiant de la nouvelle catégorie
            uc.put(DatabaseHelper.COLUMN_UC_CAT_ID, catId);
            // Insère la liaison pour que l'utilisateur voie immédiatement sa catégorie
            db.insert(DatabaseHelper.TABLE_USER_CATEGORIES, null, uc);
        }

        // Renvoie l'identifiant généré
        return catId;
    }

    public int deleteCategory(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_CATEGORIES, DatabaseHelper.COLUMN_CAT_ID + " = ?", new String[]{ String.valueOf(id) });
    }

    /**
     * Sauvegarde la liste des catégories choisies par un utilisateur (Étape 2 & 3 de l'onboarding).
     */
    public boolean saveUserSelectedCategories(long userId, List<Long> categoryIds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DatabaseHelper.TABLE_USER_CATEGORIES, DatabaseHelper.COLUMN_UC_USER_ID + " = ?", new String[]{ String.valueOf(userId) });
            for (Long catId : categoryIds) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COLUMN_UC_USER_ID, userId);
                cv.put(DatabaseHelper.COLUMN_UC_CAT_ID, catId);
                cv.put(DatabaseHelper.COLUMN_UC_IS_SELECTED, 1);
                db.insert(DatabaseHelper.TABLE_USER_CATEGORIES, null, cv);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erreur saveUserSelectedCategories : " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Récupère les catégories sélectionnées par l'utilisateur. Si aucune n'a encore été sélectionnée,
     * retourne l'ensemble des catégories par défaut.
     */
    public List<Category> getUserSelectedCategories(long userId) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String sql = "SELECT c.* FROM " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                    "INNER JOIN " + DatabaseHelper.TABLE_USER_CATEGORIES + " uc " +
                    "ON c." + DatabaseHelper.COLUMN_CAT_ID + " = uc." + DatabaseHelper.COLUMN_UC_CAT_ID + " " +
                    "WHERE uc." + DatabaseHelper.COLUMN_UC_USER_ID + " = ? AND uc." + DatabaseHelper.COLUMN_UC_IS_SELECTED + " = 1 " +
                    "ORDER BY c." + DatabaseHelper.COLUMN_CAT_ID + " ASC";
            cursor = db.rawQuery(sql, new String[]{ String.valueOf(userId) });
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToCategory(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getUserSelectedCategories : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        if (list.isEmpty()) {
            return getAllCategories();
        }
        return list;
    }

    /**
     * Récupère les catégories sélectionnées par l'utilisateur filtrées par type (PRODUCT ou EXPENSE).
     */
    public List<Category> getUserSelectedCategoriesByType(long userId, String type) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String sql = "SELECT c.* FROM " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                    "INNER JOIN " + DatabaseHelper.TABLE_USER_CATEGORIES + " uc " +
                    "ON c." + DatabaseHelper.COLUMN_CAT_ID + " = uc." + DatabaseHelper.COLUMN_UC_CAT_ID + " " +
                    "WHERE uc." + DatabaseHelper.COLUMN_UC_USER_ID + " = ? AND uc." + DatabaseHelper.COLUMN_UC_IS_SELECTED + " = 1 " +
                    "AND c." + DatabaseHelper.COLUMN_CAT_TYPE + " = ? " +
                    "ORDER BY c." + DatabaseHelper.COLUMN_CAT_ID + " ASC";
            cursor = db.rawQuery(sql, new String[]{ String.valueOf(userId), type });
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToCategory(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getUserSelectedCategoriesByType : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        if (list.isEmpty()) {
            return getCategoriesByType(type);
        }
        return list;
    }

    /**
     * Vérifie si l'utilisateur a déjà validé une sélection de catégories.
     */
    public boolean hasUserSelectedCategories(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean has = false;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USER_CATEGORIES +
                    " WHERE " + DatabaseHelper.COLUMN_UC_USER_ID + " = ?", new String[]{ String.valueOf(userId) });
            if (cursor != null && cursor.moveToFirst()) {
                has = cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur hasUserSelectedCategories : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return has;
    }

    public double getTotalDefaultPercentage() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double sum = 0.0;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT SUM(" + DatabaseHelper.COLUMN_CAT_DEFAULT_PCT + ") FROM " + DatabaseHelper.TABLE_CATEGORIES, null);
            if (cursor != null && cursor.moveToFirst()) {
                sum = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getTotalDefaultPercentage : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return sum;
    }

    private Category cursorToCategory(Cursor cursor) {
        Category cat = new Category();
        cat.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAT_ID)));
        cat.setNameEn(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAT_NAME_EN)));
        cat.setNameFr(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAT_NAME_FR)));
        cat.setNameAr(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAT_NAME_AR)));
        cat.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAT_TYPE)));
        cat.setColorHex(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAT_COLOR)));
        cat.setIconName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAT_ICON)));
        cat.setDefaultPercentage(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CAT_DEFAULT_PCT)));

        int customIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CAT_IS_CUSTOM);
        if (customIndex != -1) {
            cat.setCustom(cursor.getInt(customIndex) == 1);
        }
        int userIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CAT_USER_ID);
        if (userIndex != -1) {
            cat.setUserId(cursor.getLong(userIndex));
        }

        return cat;
    }
}

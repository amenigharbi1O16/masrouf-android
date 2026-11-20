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

    public int deleteCategory(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_CATEGORIES, DatabaseHelper.COLUMN_CAT_ID + " = ?", new String[]{ String.valueOf(id) });
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

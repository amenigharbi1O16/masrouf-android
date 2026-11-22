package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.PriceHistory;
import com.example.masrouf.model.Product;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * RÔLE : Data Access Object pour la gestion des produits et de l'historique de prix (Figma 12, 13, 15, 18, 26).
 * COUCHE MVC : DAO.
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Composant DAO).
 *   - Matrice §4 ligne 30 (Alimentation de l'Adapter personnalisé BaseAdapter).
 *   - Matrice §4 ligne 32 (CRUD complet : Ajout, Modification, Suppression, Consultation).
 *   - Matrice §4 ligne 33 (Requêtes paramétrées anti-injection).
 */
public class ProductDao {

    private static final String TAG = "ProductDao";
    private final DatabaseHelper dbHelper;

    public ProductDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public ProductDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT p." + DatabaseHelper.COLUMN_PROD_ID + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_NAME + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_CAT_ID + ", " +
                "c." + DatabaseHelper.COLUMN_CAT_NAME_EN + " AS cat_name, " +
                "p." + DatabaseHelper.COLUMN_PROD_BRAND + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_UNIT + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_LAST_PRICE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_PREV_PRICE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_STORE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_LAST_DATE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_LAST_PRICE_MILLIMES + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_PREV_PRICE_MILLIMES + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_PHOTO_URI + " " +
                "FROM " + DatabaseHelper.TABLE_PRODUCTS + " p " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                "ON p." + DatabaseHelper.COLUMN_PROD_CAT_ID + " = c." + DatabaseHelper.COLUMN_CAT_ID + " " +
                "ORDER BY p." + DatabaseHelper.COLUMN_PROD_NAME + " ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToProduct(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getAllProducts : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public List<Product> getProductsByUser(long userId) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT p." + DatabaseHelper.COLUMN_PROD_ID + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_NAME + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_CAT_ID + ", " +
                "c." + DatabaseHelper.COLUMN_CAT_NAME_EN + " AS cat_name, " +
                "p." + DatabaseHelper.COLUMN_PROD_BRAND + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_UNIT + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_LAST_PRICE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_PREV_PRICE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_STORE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_LAST_DATE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_LAST_PRICE_MILLIMES + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_PREV_PRICE_MILLIMES + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_PHOTO_URI + " " +
                "FROM " + DatabaseHelper.TABLE_PRODUCTS + " p " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                "ON p." + DatabaseHelper.COLUMN_PROD_CAT_ID + " = c." + DatabaseHelper.COLUMN_CAT_ID + " " +
                "WHERE p." + DatabaseHelper.COLUMN_PROD_USER_ID + " = ? OR p." + DatabaseHelper.COLUMN_PROD_USER_ID + " = 0 " +
                "ORDER BY p." + DatabaseHelper.COLUMN_PROD_NAME + " ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{ String.valueOf(userId) });
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToProduct(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getProductsByUser : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public Product getProductById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Product prod = null;
        Cursor cursor = null;

        String sql = "SELECT p." + DatabaseHelper.COLUMN_PROD_ID + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_NAME + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_CAT_ID + ", " +
                "c." + DatabaseHelper.COLUMN_CAT_NAME_EN + " AS cat_name, " +
                "p." + DatabaseHelper.COLUMN_PROD_BRAND + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_UNIT + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_LAST_PRICE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_PREV_PRICE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_STORE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_LAST_DATE + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_LAST_PRICE_MILLIMES + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_PREV_PRICE_MILLIMES + ", " +
                "p." + DatabaseHelper.COLUMN_PROD_PHOTO_URI + " " +
                "FROM " + DatabaseHelper.TABLE_PRODUCTS + " p " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                "ON p." + DatabaseHelper.COLUMN_PROD_CAT_ID + " = c." + DatabaseHelper.COLUMN_CAT_ID + " " +
                "WHERE p." + DatabaseHelper.COLUMN_PROD_ID + " = ?";

        try {
            cursor = db.rawQuery(sql, new String[]{ String.valueOf(id) });
            if (cursor != null && cursor.moveToFirst()) {
                prod = cursorToProduct(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getProductById : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return prod;
    }

    public long insertProduct(Product p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_PROD_USER_ID, p.getUserId());
        cv.put(DatabaseHelper.COLUMN_PROD_NAME, p.getName());
        cv.put(DatabaseHelper.COLUMN_PROD_CAT_ID, p.getCategoryId());
        cv.put(DatabaseHelper.COLUMN_PROD_BRAND, p.getBrand());
        cv.put(DatabaseHelper.COLUMN_PROD_UNIT, p.getUnit());
        cv.put(DatabaseHelper.COLUMN_PROD_LAST_PRICE, p.getLastUnitPrice());
        cv.put(DatabaseHelper.COLUMN_PROD_LAST_PRICE_MILLIMES, p.getLastUnitPriceMillimes() > 0 ? p.getLastUnitPriceMillimes() : MillimesUtil.dtToMillimes(p.getLastUnitPrice()));
        cv.put(DatabaseHelper.COLUMN_PROD_PREV_PRICE, p.getPreviousUnitPrice());
        cv.put(DatabaseHelper.COLUMN_PROD_PREV_PRICE_MILLIMES, p.getPreviousUnitPriceMillimes() > 0 ? p.getPreviousUnitPriceMillimes() : MillimesUtil.dtToMillimes(p.getPreviousUnitPrice()));
        cv.put(DatabaseHelper.COLUMN_PROD_STORE, p.getStore());
        cv.put(DatabaseHelper.COLUMN_PROD_LAST_DATE, p.getLastPurchaseDate());
        cv.put(DatabaseHelper.COLUMN_PROD_PHOTO_URI, p.getPhotoUri() != null ? p.getPhotoUri() : "");

        long id = db.insert(DatabaseHelper.TABLE_PRODUCTS, null, cv);
        if (id != -1) {
            p.setId(id);
            // Journaliser dans price_history
            PriceHistoryDao phDao = new PriceHistoryDao(dbHelper);
            phDao.insertPriceHistory(new PriceHistory(id, p.getLastUnitPrice(), p.getStore(), p.getLastPurchaseDate()));
        }
        return id;
    }

    public void updateProductPrice(long productId, double unitPrice, String store, String date) {
        recordNewPurchasePrice(productId, unitPrice, store, date);
    }

    public int updateProduct(Product p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_PROD_NAME, p.getName());
        cv.put(DatabaseHelper.COLUMN_PROD_CAT_ID, p.getCategoryId());
        cv.put(DatabaseHelper.COLUMN_PROD_BRAND, p.getBrand());
        cv.put(DatabaseHelper.COLUMN_PROD_UNIT, p.getUnit());
        cv.put(DatabaseHelper.COLUMN_PROD_LAST_PRICE, p.getLastUnitPrice());
        cv.put(DatabaseHelper.COLUMN_PROD_LAST_PRICE_MILLIMES, p.getLastUnitPriceMillimes() > 0 ? p.getLastUnitPriceMillimes() : MillimesUtil.dtToMillimes(p.getLastUnitPrice()));
        cv.put(DatabaseHelper.COLUMN_PROD_PREV_PRICE, p.getPreviousUnitPrice());
        cv.put(DatabaseHelper.COLUMN_PROD_PREV_PRICE_MILLIMES, p.getPreviousUnitPriceMillimes() > 0 ? p.getPreviousUnitPriceMillimes() : MillimesUtil.dtToMillimes(p.getPreviousUnitPrice()));
        cv.put(DatabaseHelper.COLUMN_PROD_STORE, p.getStore());
        cv.put(DatabaseHelper.COLUMN_PROD_LAST_DATE, p.getLastPurchaseDate());
        cv.put(DatabaseHelper.COLUMN_PROD_PHOTO_URI, p.getPhotoUri() != null ? p.getPhotoUri() : "");

        return db.update(DatabaseHelper.TABLE_PRODUCTS, cv, DatabaseHelper.COLUMN_PROD_ID + " = ?", new String[]{ String.valueOf(p.getId()) });
    }

    public int deleteProduct(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_PRODUCTS, DatabaseHelper.COLUMN_PROD_ID + " = ?", new String[]{ String.valueOf(id) });
    }

    /**
     * Met à jour le prix constaté après un nouvel achat et bascule l'ancien prix dans previous_unit_price.
     */
    public void recordNewPurchasePrice(long productId, double newUnitPrice, String store, String date) {
        Product prod = getProductById(productId);
        if (prod != null) {
            double oldPrice = prod.getLastUnitPrice();
            long oldMillimes = prod.getLastUnitPriceMillimes();
            long newMillimes = MillimesUtil.dtToMillimes(newUnitPrice);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COLUMN_PROD_PREV_PRICE, oldPrice);
            cv.put(DatabaseHelper.COLUMN_PROD_PREV_PRICE_MILLIMES, oldMillimes);
            cv.put(DatabaseHelper.COLUMN_PROD_LAST_PRICE, newUnitPrice);
            cv.put(DatabaseHelper.COLUMN_PROD_LAST_PRICE_MILLIMES, newMillimes);
            cv.put(DatabaseHelper.COLUMN_PROD_STORE, store);
            cv.put(DatabaseHelper.COLUMN_PROD_LAST_DATE, date);

            db.update(DatabaseHelper.TABLE_PRODUCTS, cv, DatabaseHelper.COLUMN_PROD_ID + " = ?", new String[]{ String.valueOf(productId) });

            // Enregistrer dans price_history
            ContentValues ph = new ContentValues();
            ph.put(DatabaseHelper.COLUMN_PH_PROD_ID, productId);
            ph.put(DatabaseHelper.COLUMN_PH_PRICE, newUnitPrice);
            ph.put(DatabaseHelper.COLUMN_PH_PRICE_MILLIMES, newMillimes);
            ph.put(DatabaseHelper.COLUMN_PH_STORE, store);
            ph.put(DatabaseHelper.COLUMN_PH_DATE, date);
            db.insert(DatabaseHelper.TABLE_PRICE_HISTORY, null, ph);
        }
    }

    private Product cursorToProduct(Cursor cursor) {
        Product prod = new Product();
        prod.setId(cursor.getLong(0));
        prod.setName(cursor.getString(1));
        prod.setCategoryId(cursor.getLong(2));
        prod.setCategoryName(cursor.getString(3));
        prod.setBrand(cursor.getString(4));
        prod.setUnit(cursor.getString(5));
        prod.setLastUnitPrice(cursor.getDouble(6));
        prod.setPreviousUnitPrice(cursor.getDouble(7));
        prod.setStore(cursor.getString(8));
        prod.setLastPurchaseDate(cursor.getString(9));

        if (cursor.getColumnCount() > 10) {
            prod.setLastUnitPriceMillimes(cursor.getLong(10) > 0 ? cursor.getLong(10) : MillimesUtil.dtToMillimes(prod.getLastUnitPrice()));
            prod.setPreviousUnitPriceMillimes(cursor.getLong(11) > 0 ? cursor.getLong(11) : MillimesUtil.dtToMillimes(prod.getPreviousUnitPrice()));
            prod.setPhotoUri(cursor.getString(12));
        } else {
            prod.setLastUnitPriceMillimes(MillimesUtil.dtToMillimes(prod.getLastUnitPrice()));
            prod.setPreviousUnitPriceMillimes(MillimesUtil.dtToMillimes(prod.getPreviousUnitPrice()));
        }

        return prod;
    }
}

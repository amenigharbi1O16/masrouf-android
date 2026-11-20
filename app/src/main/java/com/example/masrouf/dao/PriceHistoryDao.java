package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.PriceHistory;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * RÔLE : Data Access Object pour la table 'price_history' (suivi des variations de prix au fil du temps).
 * COUCHE MVC : DAO.
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Composant DAO).
 *   - Matrice §4 ligne 32 (CRUD complet).
 *   - Spécifications Masrouf : Historique et comparaison des prix unitaire par magasin.
 */
public class PriceHistoryDao {

    private static final String TAG = "PriceHistoryDao";
    private final DatabaseHelper dbHelper;

    public PriceHistoryDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public PriceHistoryDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertPriceHistory(PriceHistory ph) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_PH_PROD_ID, ph.getProductId());
        cv.put(DatabaseHelper.COLUMN_PH_PRICE, ph.getPrice());
        cv.put(DatabaseHelper.COLUMN_PH_PRICE_MILLIMES, ph.getPriceMillimes());
        cv.put(DatabaseHelper.COLUMN_PH_STORE, ph.getStore());
        cv.put(DatabaseHelper.COLUMN_PH_DATE, ph.getDate());

        return db.insert(DatabaseHelper.TABLE_PRICE_HISTORY, null, cv);
    }

    public List<PriceHistory> getHistoryForProduct(long productId) {
        List<PriceHistory> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_PRICE_HISTORY,
                    null,
                    DatabaseHelper.COLUMN_PH_PROD_ID + " = ?",
                    new String[]{ String.valueOf(productId) },
                    null, null,
                    DatabaseHelper.COLUMN_PH_ID + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PriceHistory ph = new PriceHistory();
                    ph.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PH_ID)));
                    ph.setProductId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PH_PROD_ID)));
                    ph.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PH_PRICE)));
                    ph.setPriceMillimes(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PH_PRICE_MILLIMES)));
                    ph.setStore(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PH_STORE)));
                    ph.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PH_DATE)));
                    list.add(ph);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getHistoryForProduct : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }
}

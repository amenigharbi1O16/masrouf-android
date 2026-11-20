package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.Transaction;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RÔLE : Data Access Object pour la gestion des transactions d'achats (Figma 07, 14, 16, 24).
 * COUCHE MVC : DAO.
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Composant DAO).
 *   - Matrice §4 ligne 29 (Fournit un Cursor avec colonne '_id' pour alimenter un SimpleCursorAdapter, Ch.6).
 *   - Matrice §4 ligne 32 (CRUD complet : Insertion, Modification, Suppression, Consultation).
 *   - Matrice §4 ligne 33 (Requêtes paramétrées avec '?' anti-injection).
 */
public class TransactionDao {

    private static final String TAG = "TransactionDao";
    private final DatabaseHelper dbHelper;

    public TransactionDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public TransactionDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertTransaction(Transaction t) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TRANS_USER_ID, t.getUserId());
        cv.put(DatabaseHelper.COLUMN_TRANS_BUDGET_ID, t.getBudgetId());
        cv.put(DatabaseHelper.COLUMN_TRANS_CAT_ID, t.getCategoryId());
        cv.put(DatabaseHelper.COLUMN_TRANS_CAT_NAME, t.getCategoryName());
        cv.put(DatabaseHelper.COLUMN_TRANS_PROD_ID, t.getProductId());
        cv.put(DatabaseHelper.COLUMN_TRANS_TITLE, t.getTitle());
        cv.put(DatabaseHelper.COLUMN_TRANS_AMOUNT, t.getAmount());
        cv.put(DatabaseHelper.COLUMN_TRANS_AMOUNT_MILLIMES, t.getAmountMillimes() > 0 ? t.getAmountMillimes() : MillimesUtil.dtToMillimes(t.getAmount()));
        cv.put(DatabaseHelper.COLUMN_TRANS_UNIT_PRICE, t.getUnitPrice());
        cv.put(DatabaseHelper.COLUMN_TRANS_UNIT_PRICE_MILLIMES, t.getUnitPriceMillimes() > 0 ? t.getUnitPriceMillimes() : MillimesUtil.dtToMillimes(t.getUnitPrice()));
        cv.put(DatabaseHelper.COLUMN_TRANS_QUANTITY, t.getQuantity());
        cv.put(DatabaseHelper.COLUMN_TRANS_UNIT, t.getUnit());
        cv.put(DatabaseHelper.COLUMN_TRANS_STORE, t.getStore());
        cv.put(DatabaseHelper.COLUMN_TRANS_DATE, t.getDate());
        cv.put(DatabaseHelper.COLUMN_TRANS_NOTE, t.getNote());
        cv.put(DatabaseHelper.COLUMN_TRANS_PHOTO_URI, t.getPhotoUri() != null ? t.getPhotoUri() : "");

        return db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, cv);
    }

    public int updateTransaction(Transaction t) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TRANS_CAT_ID, t.getCategoryId());
        cv.put(DatabaseHelper.COLUMN_TRANS_CAT_NAME, t.getCategoryName());
        cv.put(DatabaseHelper.COLUMN_TRANS_PROD_ID, t.getProductId());
        cv.put(DatabaseHelper.COLUMN_TRANS_TITLE, t.getTitle());
        cv.put(DatabaseHelper.COLUMN_TRANS_AMOUNT, t.getAmount());
        cv.put(DatabaseHelper.COLUMN_TRANS_AMOUNT_MILLIMES, t.getAmountMillimes() > 0 ? t.getAmountMillimes() : MillimesUtil.dtToMillimes(t.getAmount()));
        cv.put(DatabaseHelper.COLUMN_TRANS_UNIT_PRICE, t.getUnitPrice());
        cv.put(DatabaseHelper.COLUMN_TRANS_UNIT_PRICE_MILLIMES, t.getUnitPriceMillimes() > 0 ? t.getUnitPriceMillimes() : MillimesUtil.dtToMillimes(t.getUnitPrice()));
        cv.put(DatabaseHelper.COLUMN_TRANS_QUANTITY, t.getQuantity());
        cv.put(DatabaseHelper.COLUMN_TRANS_UNIT, t.getUnit());
        cv.put(DatabaseHelper.COLUMN_TRANS_STORE, t.getStore());
        cv.put(DatabaseHelper.COLUMN_TRANS_DATE, t.getDate());
        cv.put(DatabaseHelper.COLUMN_TRANS_NOTE, t.getNote());
        cv.put(DatabaseHelper.COLUMN_TRANS_PHOTO_URI, t.getPhotoUri() != null ? t.getPhotoUri() : "");

        return db.update(DatabaseHelper.TABLE_TRANSACTIONS, cv, DatabaseHelper.COLUMN_TRANS_ID + " = ?", new String[]{ String.valueOf(t.getId()) });
    }

    public int deleteTransaction(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_TRANSACTIONS, DatabaseHelper.COLUMN_TRANS_ID + " = ?", new String[]{ String.valueOf(id) });
    }

    public Transaction getTransactionById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Transaction t = null;
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null, DatabaseHelper.COLUMN_TRANS_ID + " = ?", new String[]{ String.valueOf(id) }, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                t = cursorToTransaction(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getTransactionById : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return t;
    }

    public List<Transaction> getRecentTransactions(long userId, int limit) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_TRANSACTIONS,
                    null,
                    DatabaseHelper.COLUMN_TRANS_USER_ID + " = ?",
                    new String[]{ String.valueOf(userId) },
                    null, null,
                    DatabaseHelper.COLUMN_TRANS_ID + " DESC",
                    String.valueOf(limit)
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToTransaction(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getRecentTransactions : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public List<Transaction> getAllTransactions(long userId) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_TRANSACTIONS,
                    null,
                    DatabaseHelper.COLUMN_TRANS_USER_ID + " = ?",
                    new String[]{ String.valueOf(userId) },
                    null, null,
                    DatabaseHelper.COLUMN_TRANS_ID + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToTransaction(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getAllTransactions : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public List<Transaction> getTransactionsForProduct(long productId) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_TRANSACTIONS,
                    null,
                    DatabaseHelper.COLUMN_TRANS_PROD_ID + " = ?",
                    new String[]{ String.valueOf(productId) },
                    null, null,
                    DatabaseHelper.COLUMN_TRANS_ID + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToTransaction(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getTransactionsForProduct : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public double getTotalExpensesForMonth(long userId, String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double total = 0.0;
        Cursor cursor = null;

        String monthKeyword = getMonthKeyword(yearMonth);

        try {
            String query = "SELECT SUM(" + DatabaseHelper.COLUMN_TRANS_AMOUNT + ") FROM " +
                    DatabaseHelper.TABLE_TRANSACTIONS +
                    " WHERE " + DatabaseHelper.COLUMN_TRANS_USER_ID + " = ? AND (" +
                    DatabaseHelper.COLUMN_TRANS_DATE + " LIKE ? OR " +
                    DatabaseHelper.COLUMN_TRANS_DATE + " LIKE ?)";

            cursor = db.rawQuery(query, new String[]{
                    String.valueOf(userId),
                    "%" + yearMonth + "%",
                    "%" + monthKeyword + "%"
            });

            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getTotalExpensesForMonth : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return total;
    }

    public Cursor getTransactionsCursor(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(
                DatabaseHelper.TABLE_TRANSACTIONS,
                new String[]{
                        DatabaseHelper.COLUMN_TRANS_ID,
                        DatabaseHelper.COLUMN_TRANS_TITLE,
                        DatabaseHelper.COLUMN_TRANS_AMOUNT,
                        DatabaseHelper.COLUMN_TRANS_CAT_NAME,
                        DatabaseHelper.COLUMN_TRANS_DATE,
                        DatabaseHelper.COLUMN_TRANS_STORE
                },
                DatabaseHelper.COLUMN_TRANS_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) },
                null, null,
                DatabaseHelper.COLUMN_TRANS_ID + " DESC"
        );
    }

    public double getTotalExpenses(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double total = 0.0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT SUM(" + DatabaseHelper.COLUMN_TRANS_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " WHERE " + DatabaseHelper.COLUMN_TRANS_USER_ID + " = ?", new String[]{ String.valueOf(userId) });
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getTotalExpenses : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }

    public Map<String, Double> getCategoryExpenses(long userId) {
        Map<String, Double> map = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_TRANS_CAT_NAME + ", SUM(" + DatabaseHelper.COLUMN_TRANS_AMOUNT + ") FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " WHERE " + DatabaseHelper.COLUMN_TRANS_USER_ID + " = ? GROUP BY " + DatabaseHelper.COLUMN_TRANS_CAT_NAME, new String[]{ String.valueOf(userId) });
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    map.put(cursor.getString(0), cursor.getDouble(1));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getCategoryExpenses : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return map;
    }

    public List<Transaction> getTransactionsForMonth(long userId, String yearMonth) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String monthKeyword = getMonthKeyword(yearMonth);
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_TRANSACTIONS,
                    null,
                    DatabaseHelper.COLUMN_TRANS_USER_ID + " = ? AND (" +
                            DatabaseHelper.COLUMN_TRANS_DATE + " LIKE ? OR " +
                            DatabaseHelper.COLUMN_TRANS_DATE + " LIKE ?)",
                    new String[]{ String.valueOf(userId), "%" + yearMonth + "%", "%" + monthKeyword + "%" },
                    null, null,
                    DatabaseHelper.COLUMN_TRANS_ID + " DESC"
            );
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToTransaction(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getTransactionsForMonth : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public Map<String, Double> getExpensesByCategoryForMonth(long userId, String yearMonth) {
        Map<String, Double> map = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        String monthKeyword = getMonthKeyword(yearMonth);

        try {
            String query = "SELECT " + DatabaseHelper.COLUMN_TRANS_CAT_NAME + ", SUM(" + DatabaseHelper.COLUMN_TRANS_AMOUNT + ") " +
                    "FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " " +
                    "WHERE " + DatabaseHelper.COLUMN_TRANS_USER_ID + " = ? AND (" +
                    DatabaseHelper.COLUMN_TRANS_DATE + " LIKE ? OR " +
                    DatabaseHelper.COLUMN_TRANS_DATE + " LIKE ?) " +
                    "GROUP BY " + DatabaseHelper.COLUMN_TRANS_CAT_NAME;

            cursor = db.rawQuery(query, new String[]{
                    String.valueOf(userId),
                    "%" + yearMonth + "%",
                    "%" + monthKeyword + "%"
            });

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String catName = cursor.getString(0);
                    double sum = cursor.getDouble(1);
                    map.put(catName, sum);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getExpensesByCategoryForMonth : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return map;
    }

    private String getMonthKeyword(String yearMonth) {
        if (yearMonth == null || !yearMonth.contains("-")) return "";
        String[] parts = yearMonth.split("-");
        if (parts.length < 2) return "";
        switch (parts[1]) {
            case "01": return "Jan";
            case "02": return "Feb";
            case "03": return "Mar";
            case "04": return "Apr";
            case "05": return "May";
            case "06": return "Jun";
            case "07": return "Jul";
            case "08": return "Aug";
            case "09": return "Sep";
            case "10": return "Oct";
            case "11": return "Nov";
            case "12": return "Dec";
            default: return "";
        }
    }

    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction t = new Transaction();
        t.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_ID)));
        t.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_USER_ID)));
        t.setCategoryId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_CAT_ID)));
        t.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_CAT_NAME)));
        t.setProductId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_PROD_ID)));
        t.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_TITLE)));
        t.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_AMOUNT)));
        t.setUnitPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_UNIT_PRICE)));
        t.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_QUANTITY)));
        t.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_UNIT)));
        t.setStore(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_STORE)));
        t.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_DATE)));
        t.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_NOTE)));

        int millimesIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TRANS_AMOUNT_MILLIMES);
        if (millimesIndex != -1) {
            t.setAmountMillimes(cursor.getLong(millimesIndex));
        } else {
            t.setAmountMillimes(MillimesUtil.dtToMillimes(t.getAmount()));
        }

        int unitPriceMillimesIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TRANS_UNIT_PRICE_MILLIMES);
        if (unitPriceMillimesIndex != -1) {
            t.setUnitPriceMillimes(cursor.getLong(unitPriceMillimesIndex));
        } else {
            t.setUnitPriceMillimes(MillimesUtil.dtToMillimes(t.getUnitPrice()));
        }

        int photoIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TRANS_PHOTO_URI);
        if (photoIndex != -1) {
            t.setPhotoUri(cursor.getString(photoIndex));
        }

        int budgetIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TRANS_BUDGET_ID);
        if (budgetIndex != -1) {
            t.setBudgetId(cursor.getLong(budgetIndex));
        }

        return t;
    }
}

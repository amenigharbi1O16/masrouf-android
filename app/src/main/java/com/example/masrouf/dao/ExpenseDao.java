package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.Expense;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * RÔLE : Data Access Object pour la gestion des dépenses fixes et récurrentes (Table 'expenses').
 * COUCHE MVC : DAO.
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Composant DAO).
 *   - Matrice §4 ligne 32 (CRUD complet des dépenses).
 *   - Matrice §4 ligne 33 (Requêtes paramétrées anti-injection).
 */
public class ExpenseDao {

    private static final String TAG = "ExpenseDao";
    private final DatabaseHelper dbHelper;

    public ExpenseDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_EXPENSE_USER_ID, expense.getUserId());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_BUDGET_ID, expense.getBudgetId());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_CAT_ID, expense.getCategoryId());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_TITLE, expense.getTitle());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_AMOUNT_MILLIMES, expense.getAmountMillimes());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_DATE, expense.getDate());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_NOTE, expense.getNote());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_IS_RECURRING, expense.isRecurring() ? 1 : 0);

        return db.insert(DatabaseHelper.TABLE_EXPENSES, null, cv);
    }

    public List<Expense> getExpensesForUser(long userId) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT e." + DatabaseHelper.COLUMN_EXPENSE_ID + ", " +
                "e." + DatabaseHelper.COLUMN_EXPENSE_USER_ID + ", " +
                "e." + DatabaseHelper.COLUMN_EXPENSE_BUDGET_ID + ", " +
                "e." + DatabaseHelper.COLUMN_EXPENSE_CAT_ID + ", " +
                "c." + DatabaseHelper.COLUMN_CAT_NAME_EN + " AS cat_name, " +
                "e." + DatabaseHelper.COLUMN_EXPENSE_TITLE + ", " +
                "e." + DatabaseHelper.COLUMN_EXPENSE_AMOUNT + ", " +
                "e." + DatabaseHelper.COLUMN_EXPENSE_AMOUNT_MILLIMES + ", " +
                "e." + DatabaseHelper.COLUMN_EXPENSE_DATE + ", " +
                "e." + DatabaseHelper.COLUMN_EXPENSE_NOTE + ", " +
                "e." + DatabaseHelper.COLUMN_EXPENSE_IS_RECURRING + " " +
                "FROM " + DatabaseHelper.TABLE_EXPENSES + " e " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c ON e." + DatabaseHelper.COLUMN_EXPENSE_CAT_ID + " = c." + DatabaseHelper.COLUMN_CAT_ID + " " +
                "WHERE e." + DatabaseHelper.COLUMN_EXPENSE_USER_ID + " = ? " +
                "ORDER BY e." + DatabaseHelper.COLUMN_EXPENSE_ID + " DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{ String.valueOf(userId) });
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Expense e = new Expense();
                    e.setId(cursor.getLong(0));
                    e.setUserId(cursor.getLong(1));
                    e.setBudgetId(cursor.getLong(2));
                    e.setCategoryId(cursor.getLong(3));
                    e.setCategoryName(cursor.getString(4));
                    e.setTitle(cursor.getString(5));
                    e.setAmount(cursor.getDouble(6));
                    e.setAmountMillimes(cursor.getLong(7));
                    e.setDate(cursor.getString(8));
                    e.setNote(cursor.getString(9));
                    e.setRecurring(cursor.getInt(10) == 1);
                    list.add(e);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getExpensesForUser : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public Expense getExpenseById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Expense expense = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_EXPENSES,
                    null,
                    DatabaseHelper.COLUMN_EXPENSE_ID + " = ?",
                    new String[]{ String.valueOf(id) },
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                expense = new Expense();
                expense.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_ID)));
                expense.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_USER_ID)));
                expense.setBudgetId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_BUDGET_ID)));
                expense.setCategoryId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_CAT_ID)));
                expense.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_TITLE)));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_AMOUNT)));
                expense.setAmountMillimes(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_AMOUNT_MILLIMES)));
                expense.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_DATE)));
                expense.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_NOTE)));
                expense.setRecurring(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_IS_RECURRING)) == 1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getExpenseById : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return expense;
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_EXPENSE_CAT_ID, expense.getCategoryId());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_TITLE, expense.getTitle());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_AMOUNT_MILLIMES, expense.getAmountMillimes());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_DATE, expense.getDate());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_NOTE, expense.getNote());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_IS_RECURRING, expense.isRecurring() ? 1 : 0);

        return db.update(DatabaseHelper.TABLE_EXPENSES, cv,
                DatabaseHelper.COLUMN_EXPENSE_ID + " = ?",
                new String[]{ String.valueOf(expense.getId()) });
    }

    public int deleteExpense(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_EXPENSES,
                DatabaseHelper.COLUMN_EXPENSE_ID + " = ?",
                new String[]{ String.valueOf(id) });
    }

    public long getTotalExpensesForMonthMillimes(long userId, String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long total = 0;
        Cursor cursor = null;

        try {
            String query = "SELECT SUM(" + DatabaseHelper.COLUMN_EXPENSE_AMOUNT_MILLIMES + ") FROM " +
                    DatabaseHelper.TABLE_EXPENSES +
                    " WHERE " + DatabaseHelper.COLUMN_EXPENSE_USER_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_EXPENSE_DATE + " LIKE ?";

            cursor = db.rawQuery(query, new String[]{ String.valueOf(userId), "%" + yearMonth + "%" });
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getTotalExpensesForMonthMillimes : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return total;
    }
}

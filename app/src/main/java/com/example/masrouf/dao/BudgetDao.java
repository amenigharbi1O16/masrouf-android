package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.Budget;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.util.MillimesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * RÔLE : Data Access Object pour la gestion des budgets et allocations par catégorie.
 * COUCHE MVC : DAO.
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Composant DAO).
 *   - Matrice §4 ligne 32 (CRUD complet : Création, Consultation, Modification Figma 11).
 *   - Matrice §4 ligne 33 (Requêtes paramétrées avec '?' anti-injection SQL).
 *   - Précision exacte en integer millimes.
 */
public class BudgetDao {

    private static final String TAG = "BudgetDao";
    private final DatabaseHelper dbHelper;

    public BudgetDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public BudgetDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Récupère le budget actif le plus récent pour un utilisateur.
     */
    public Budget getCurrentBudget(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Budget budget = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_BUDGETS,
                    null,
                    DatabaseHelper.COLUMN_BUDGET_USER_ID + " = ?",
                    new String[]{ String.valueOf(userId) },
                    null, null,
                    DatabaseHelper.COLUMN_BUDGET_ID + " DESC",
                    "1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                budget = cursorToBudget(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getCurrentBudget : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return budget;
    }

    /**
     * Récupère le budget associé à un mois donné (ex: "2026-11").
     */
    public Budget getBudgetForMonth(long userId, String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Budget budget = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_BUDGETS,
                    null,
                    DatabaseHelper.COLUMN_BUDGET_USER_ID + " = ? AND (" +
                            DatabaseHelper.COLUMN_BUDGET_START + " LIKE ? OR " +
                            DatabaseHelper.COLUMN_BUDGET_END + " LIKE ? OR " +
                            DatabaseHelper.COLUMN_BUDGET_MONTH_KEY + " = ?)",
                    new String[]{ String.valueOf(userId), "%" + yearMonth + "%", "%" + yearMonth + "%", yearMonth },
                    null, null,
                    DatabaseHelper.COLUMN_BUDGET_ID + " DESC",
                    "1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                budget = cursorToBudget(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getBudgetForMonth : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        if (budget == null) {
            budget = getCurrentBudget(userId);
        }

        return budget;
    }

    public long saveBudget(Budget budget) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_BUDGET_USER_ID, budget.getUserId());
        cv.put(DatabaseHelper.COLUMN_BUDGET_TOTAL, budget.getTotalAmount());
        cv.put(DatabaseHelper.COLUMN_BUDGET_TOTAL_MILLIMES, budget.getTotalAmountMillimes() > 0 ? budget.getTotalAmountMillimes() : MillimesUtil.dtToMillimes(budget.getTotalAmount()));
        cv.put(DatabaseHelper.COLUMN_BUDGET_START, budget.getStartDate());
        cv.put(DatabaseHelper.COLUMN_BUDGET_END, budget.getEndDate());
        cv.put(DatabaseHelper.COLUMN_BUDGET_MONTH_KEY, budget.getMonthKey());
        cv.put(DatabaseHelper.COLUMN_BUDGET_STATUS, budget.getStatus());
        cv.put(DatabaseHelper.COLUMN_BUDGET_CREATED_AT, String.valueOf(System.currentTimeMillis()));

        return db.insert(DatabaseHelper.TABLE_BUDGETS, null, cv);
    }

    public int updateBudget(Budget budget) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_BUDGET_TOTAL, budget.getTotalAmount());
        cv.put(DatabaseHelper.COLUMN_BUDGET_TOTAL_MILLIMES, budget.getTotalAmountMillimes() > 0 ? budget.getTotalAmountMillimes() : MillimesUtil.dtToMillimes(budget.getTotalAmount()));
        cv.put(DatabaseHelper.COLUMN_BUDGET_START, budget.getStartDate());
        cv.put(DatabaseHelper.COLUMN_BUDGET_END, budget.getEndDate());
        cv.put(DatabaseHelper.COLUMN_BUDGET_MONTH_KEY, budget.getMonthKey());
        cv.put(DatabaseHelper.COLUMN_BUDGET_STATUS, budget.getStatus());

        return db.update(DatabaseHelper.TABLE_BUDGETS, cv, DatabaseHelper.COLUMN_BUDGET_ID + " = ?", new String[]{ String.valueOf(budget.getId()) });
    }

    public List<BudgetAllocation> getAllocationsForBudget(long budgetId) {
        List<BudgetAllocation> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a." + DatabaseHelper.COLUMN_ALLOC_ID + ", " +
                "a." + DatabaseHelper.COLUMN_ALLOC_BUDGET_ID + ", " +
                "a." + DatabaseHelper.COLUMN_ALLOC_CAT_ID + ", " +
                "c." + DatabaseHelper.COLUMN_CAT_NAME_EN + " AS cat_name, " +
                "a." + DatabaseHelper.COLUMN_ALLOC_AMOUNT + ", " +
                "a." + DatabaseHelper.COLUMN_ALLOC_MILLIMES + ", " +
                "a." + DatabaseHelper.COLUMN_ALLOC_SPENT + ", " +
                "a." + DatabaseHelper.COLUMN_ALLOC_SPENT_MILLIMES + " " +
                "FROM " + DatabaseHelper.TABLE_BUDGET_ALLOCATIONS + " a " +
                "INNER JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                "ON a." + DatabaseHelper.COLUMN_ALLOC_CAT_ID + " = c." + DatabaseHelper.COLUMN_CAT_ID + " " +
                "WHERE a." + DatabaseHelper.COLUMN_ALLOC_BUDGET_ID + " = ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{ String.valueOf(budgetId) });
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    BudgetAllocation alloc = new BudgetAllocation();
                    alloc.setId(cursor.getLong(0));
                    alloc.setBudgetId(cursor.getLong(1));
                    alloc.setCategoryId(cursor.getLong(2));
                    alloc.setCategoryName(cursor.getString(3));
                    alloc.setAllocatedAmount(cursor.getDouble(4));
                    alloc.setAllocatedMillimes(cursor.getLong(5) > 0 ? cursor.getLong(5) : MillimesUtil.dtToMillimes(cursor.getDouble(4)));
                    alloc.setSpentAmount(cursor.getDouble(6));
                    alloc.setSpentMillimes(cursor.getLong(7) > 0 ? cursor.getLong(7) : MillimesUtil.dtToMillimes(cursor.getDouble(6)));
                    list.add(alloc);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getAllocationsForBudget : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public long saveAllocation(BudgetAllocation alloc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_ALLOC_BUDGET_ID, alloc.getBudgetId());
        cv.put(DatabaseHelper.COLUMN_ALLOC_CAT_ID, alloc.getCategoryId());
        cv.put(DatabaseHelper.COLUMN_ALLOC_AMOUNT, alloc.getAllocatedAmount());
        cv.put(DatabaseHelper.COLUMN_ALLOC_MILLIMES, alloc.getAllocatedMillimes() > 0 ? alloc.getAllocatedMillimes() : MillimesUtil.dtToMillimes(alloc.getAllocatedAmount()));
        cv.put(DatabaseHelper.COLUMN_ALLOC_SPENT, alloc.getSpentAmount());
        cv.put(DatabaseHelper.COLUMN_ALLOC_SPENT_MILLIMES, alloc.getSpentMillimes() > 0 ? alloc.getSpentMillimes() : MillimesUtil.dtToMillimes(alloc.getSpentAmount()));

        return db.insert(DatabaseHelper.TABLE_BUDGET_ALLOCATIONS, null, cv);
    }

    public void updateAllocationAmount(long allocId, double newAmount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_ALLOC_AMOUNT, newAmount);
        cv.put(DatabaseHelper.COLUMN_ALLOC_MILLIMES, MillimesUtil.dtToMillimes(newAmount));
        db.update(DatabaseHelper.TABLE_BUDGET_ALLOCATIONS, cv, DatabaseHelper.COLUMN_ALLOC_ID + " = ?", new String[]{ String.valueOf(allocId) });
    }

    /**
     * Met à jour le montant dépensé dans une catégorie et réévalue le statut global du budget.
     */
    public void recordExpenseInAllocation(long budgetId, long categoryId, double additionalSpent) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long addMillimes = MillimesUtil.dtToMillimes(additionalSpent);

        db.execSQL("UPDATE " + DatabaseHelper.TABLE_BUDGET_ALLOCATIONS + " " +
                "SET " + DatabaseHelper.COLUMN_ALLOC_SPENT + " = " + DatabaseHelper.COLUMN_ALLOC_SPENT + " + ?, " +
                DatabaseHelper.COLUMN_ALLOC_SPENT_MILLIMES + " = " + DatabaseHelper.COLUMN_ALLOC_SPENT_MILLIMES + " + ? " +
                "WHERE " + DatabaseHelper.COLUMN_ALLOC_BUDGET_ID + " = ? AND " +
                DatabaseHelper.COLUMN_ALLOC_CAT_ID + " = ?",
                new Object[]{ additionalSpent, addMillimes, budgetId, categoryId });

        double totalSpent = getTotalSpent(budgetId);
        Budget b = getBudgetById(budgetId);
        if (b != null && b.getTotalAmount() > 0) {
            double ratio = totalSpent / b.getTotalAmount();
            String newStatus = Budget.STATUS_HEALTHY;
            if (ratio >= 1.0) {
                newStatus = Budget.STATUS_EXCEEDED;
            } else if (ratio >= 0.8) {
                newStatus = Budget.STATUS_WARNING;
            }

            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COLUMN_BUDGET_STATUS, newStatus);
            db.update(DatabaseHelper.TABLE_BUDGETS, cv, DatabaseHelper.COLUMN_BUDGET_ID + " = ?", new String[]{ String.valueOf(budgetId) });
        }
    }

    public double getTotalSpent(long budgetId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double total = 0.0;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "SELECT SUM(" + DatabaseHelper.COLUMN_ALLOC_SPENT + ") FROM " + DatabaseHelper.TABLE_BUDGET_ALLOCATIONS + " WHERE " + DatabaseHelper.COLUMN_ALLOC_BUDGET_ID + " = ?",
                    new String[]{ String.valueOf(budgetId) }
            );
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getTotalSpent : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return total;
    }

    public Budget getBudgetById(long budgetId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Budget budget = null;
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseHelper.TABLE_BUDGETS, null, DatabaseHelper.COLUMN_BUDGET_ID + " = ?", new String[]{ String.valueOf(budgetId) }, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                budget = cursorToBudget(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getBudgetById : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return budget;
    }

    private Budget cursorToBudget(Cursor cursor) {
        Budget budget = new Budget();
        budget.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_ID)));
        budget.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_USER_ID)));
        budget.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_TOTAL)));

        int millimesIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BUDGET_TOTAL_MILLIMES);
        if (millimesIndex != -1) {
            budget.setTotalAmountMillimes(cursor.getLong(millimesIndex));
        } else {
            budget.setTotalAmountMillimes(MillimesUtil.dtToMillimes(budget.getTotalAmount()));
        }

        budget.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_START)));
        budget.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_END)));

        int monthKeyIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BUDGET_MONTH_KEY);
        if (monthKeyIndex != -1) {
            budget.setMonthKey(cursor.getString(monthKeyIndex));
        }

        budget.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_STATUS)));
        return budget;
    }
}

package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.Budget;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.ChildPermission;
import com.example.masrouf.model.FamilyAllocation;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.model.User;
import com.example.masrouf.util.MillimesUtil;
import com.example.masrouf.util.PasswordUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Data Access Object (DAO) dédié à la gestion du profil Parent, des enveloppes par catégorie,
 * des allocations individuelles aux enfants et des permissions parentales.
 * 
 * Architecture financière centralisée :
 * - Le parent définit un budget global pour la période.
 * - Répartition du budget global par catégories (dont la catégorie "Enfant").
 * - Le bouton "Donner" attribue un solde à l'enfant prélevé directement sur l'enveloppe "Enfant".
 * - Opérations atomiques protégées par des transactions SQLite.
 */
public class FamilyDao {

    private static final String TAG = "FamilyDao";

    private final DatabaseHelper dbHelper;
    private final BudgetDao budgetDao;
    private final CategoryDao categoryDao;

    public FamilyDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.budgetDao = new BudgetDao(context);
        this.categoryDao = new CategoryDao(context);
    }

    public FamilyDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.budgetDao = new BudgetDao(dbHelper);
        this.categoryDao = new CategoryDao(dbHelper);
    }

    /**
     * Recherche l'ID de la catégorie "Enfant" ou "Enfants" ou "Children".
     */
    public long getEnfantCategoryId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                    new String[]{ DatabaseHelper.COLUMN_CAT_ID },
                    DatabaseHelper.COLUMN_CAT_NAME_FR + " LIKE ? OR " +
                    DatabaseHelper.COLUMN_CAT_NAME_EN + " LIKE ? OR " +
                    DatabaseHelper.COLUMN_CAT_NAME_AR + " LIKE ?",
                    new String[]{ "%Enfant%", "%Children%", "%الأطفال%" },
                    null, null, null, "1");
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getEnfantCategoryId: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 11; // ID par défaut de la catégorie Enfants si non trouvée
    }

    /**
     * Récupère l'enveloppe budgétaire "Enfant" pour le budget courant du parent.
     */
    public BudgetAllocation getEnfantCategoryAllocation(long parentId) {
        Budget currentBudget = budgetDao.getCurrentBudget(parentId);
        if (currentBudget == null) return null;

        long catId = getEnfantCategoryId();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_BUDGET_ALLOCATIONS, null,
                    DatabaseHelper.COLUMN_ALLOC_BUDGET_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_ALLOC_CAT_ID + " = ?",
                    new String[]{ String.valueOf(currentBudget.getId()), String.valueOf(catId) },
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                BudgetAllocation alloc = new BudgetAllocation();
                alloc.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLOC_ID)));
                alloc.setBudgetId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLOC_BUDGET_ID)));
                alloc.setCategoryId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLOC_CAT_ID)));
                alloc.setAllocatedAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLOC_AMOUNT)));
                alloc.setAllocatedMillimes(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLOC_MILLIMES)));
                alloc.setSpentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLOC_SPENT)));
                alloc.setSpentMillimes(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLOC_SPENT_MILLIMES)));
                return alloc;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getEnfantCategoryAllocation: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    /**
     * Calcule le montant disponible en millimes dans l'enveloppe "Enfant" du parent.
     */
    public long getEnfantEnvelopeAvailableMillimes(long parentId) {
        BudgetAllocation alloc = getEnfantCategoryAllocation(parentId);
        if (alloc == null) return 0;
        long allocated = alloc.getAllocatedMillimes() > 0 ? alloc.getAllocatedMillimes() : MillimesUtil.dtToMillimes(alloc.getAllocatedAmount());
        long spent = alloc.getSpentMillimes() > 0 ? alloc.getSpentMillimes() : MillimesUtil.dtToMillimes(alloc.getSpentAmount());
        return Math.max(0, allocated - spent);
    }

    /**
     * TRANSACTION ATOMIQUE : Allocation d'un budget individuel à un enfant depuis l'enveloppe "Enfant".
     * Déduit le montant disponible de la catégorie "Enfant", crédite le solde de l'enfant
     * et enregistre l'allocation dans SQLite.
     */
    public boolean allocateMoneyToChild(long familyId, long parentId, long childId,
                                        String childName, long amountMillimes, String description) {
        if (amountMillimes <= 0) return false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Vérifier le budget actif et l'enveloppe Enfant
            Budget currentBudget = budgetDao.getCurrentBudget(parentId);
            long catId = getEnfantCategoryId();

            Cursor allocCursor = db.query(DatabaseHelper.TABLE_BUDGET_ALLOCATIONS, null,
                    DatabaseHelper.COLUMN_ALLOC_BUDGET_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_ALLOC_CAT_ID + " = ?",
                    new String[]{ String.valueOf(currentBudget != null ? currentBudget.getId() : 0), String.valueOf(catId) },
                    null, null, null);

            long allocId = 0;
            long currentAllocated = 0;
            long currentSpent = 0;

            if (allocCursor != null && allocCursor.moveToFirst()) {
                allocId = allocCursor.getLong(allocCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLOC_ID));
                currentAllocated = allocCursor.getLong(allocCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLOC_MILLIMES));
                currentSpent = allocCursor.getLong(allocCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLOC_SPENT_MILLIMES));
                allocCursor.close();
            } else {
                if (allocCursor != null) allocCursor.close();
            }

            long availableInEnvelope = Math.max(0, currentAllocated - currentSpent);
            // Si aucune enveloppe spécifique Enfant n'est définie mais qu'un budget global existe,
            // ou si l'enveloppe disponible est suffisante :
            if (allocId > 0 && availableInEnvelope < amountMillimes) {
                Log.w(TAG, "Montant disponible insuffisant dans l'enveloppe Enfant: " + availableInEnvelope + " < " + amountMillimes);
                return false;
            }

            // 2. Mettre à jour l'enveloppe catégorie Enfant (si existante)
            if (allocId > 0) {
                long newSpentMillimes = currentSpent + amountMillimes;
                double newSpentDt = newSpentMillimes / 1000.0;
                ContentValues cvAllocUpdate = new ContentValues();
                cvAllocUpdate.put(DatabaseHelper.COLUMN_ALLOC_SPENT_MILLIMES, newSpentMillimes);
                cvAllocUpdate.put(DatabaseHelper.COLUMN_ALLOC_SPENT, newSpentDt);
                db.update(DatabaseHelper.TABLE_BUDGET_ALLOCATIONS, cvAllocUpdate,
                        DatabaseHelper.COLUMN_ALLOC_ID + " = ?",
                        new String[]{ String.valueOf(allocId) });
            }

            // 3. Créditer le solde individuel de l'enfant dans la table users
            Cursor childCursor = db.query(DatabaseHelper.TABLE_USERS,
                    new String[]{ DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES },
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{ String.valueOf(childId) },
                    null, null, null);
            long currentChildBalance = 0;
            if (childCursor != null && childCursor.moveToFirst()) {
                currentChildBalance = childCursor.getLong(0);
                childCursor.close();
            } else {
                if (childCursor != null) childCursor.close();
            }

            ContentValues cvChild = new ContentValues();
            cvChild.put(DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES, currentChildBalance + amountMillimes);
            db.update(DatabaseHelper.TABLE_USERS, cvChild,
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{ String.valueOf(childId) });

            // 4. Enregistrer dans l'historique family_allocations
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String currentMonthKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
            String now = String.valueOf(System.currentTimeMillis());
            ContentValues cvAlloc = new ContentValues();
            cvAlloc.put(DatabaseHelper.COLUMN_FA_FAMILY_ID, familyId > 0 ? familyId : 1);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_PARENT_ID, parentId);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_CHILD_ID, childId);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_CHILD_NAME, childName);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_AMOUNT_MILLIMES, amountMillimes);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_DESCRIPTION, description != null && !description.isEmpty() ? description : "Budget alloué");
            cvAlloc.put(DatabaseHelper.COLUMN_FA_DATE, today);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_CREATED_AT, now);
            db.insert(DatabaseHelper.TABLE_FAMILY_ALLOCATIONS, null, cvAlloc);

            // 5. Synchroniser le budget mensuel de l'enfant dans TABLE_BUDGETS
            long childTotalForMonth = getChildTotalAllowancesMillimes(childId, currentMonthKey);
            double childTotalDt = childTotalForMonth / 1000.0;
            Cursor childBudgetCursor = db.query(DatabaseHelper.TABLE_BUDGETS, null,
                    DatabaseHelper.COLUMN_BUDGET_USER_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_BUDGET_START + " LIKE ?",
                    new String[]{ String.valueOf(childId), currentMonthKey + "%" },
                    null, null, null);

            if (childBudgetCursor != null && childBudgetCursor.moveToFirst()) {
                long bId = childBudgetCursor.getLong(childBudgetCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET_ID));
                childBudgetCursor.close();
                ContentValues cvB = new ContentValues();
                cvB.put(DatabaseHelper.COLUMN_BUDGET_TOTAL, childTotalDt);
                cvB.put(DatabaseHelper.COLUMN_BUDGET_TOTAL_MILLIMES, childTotalForMonth);
                db.update(DatabaseHelper.TABLE_BUDGETS, cvB, DatabaseHelper.COLUMN_BUDGET_ID + " = ?", new String[]{ String.valueOf(bId) });
            } else {
                if (childBudgetCursor != null) childBudgetCursor.close();
                ContentValues cvB = new ContentValues();
                cvB.put(DatabaseHelper.COLUMN_BUDGET_USER_ID, childId);
                cvB.put(DatabaseHelper.COLUMN_BUDGET_TOTAL, childTotalDt);
                cvB.put(DatabaseHelper.COLUMN_BUDGET_TOTAL_MILLIMES, childTotalForMonth);
                cvB.put(DatabaseHelper.COLUMN_BUDGET_START, currentMonthKey + "-01");
                cvB.put(DatabaseHelper.COLUMN_BUDGET_END, currentMonthKey + "-30");
                cvB.put(DatabaseHelper.COLUMN_BUDGET_STATUS, "ACTIVE");
                cvB.put(DatabaseHelper.COLUMN_BUDGET_CREATED_AT, now);
                db.insert(DatabaseHelper.TABLE_BUDGETS, null, cvB);
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Allocation réussie: " + amountMillimes + " millimes à " + childName);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erreur transaction allocateMoneyToChild: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Récupère la liste des allocations vers des enfants.
     */
    public List<FamilyAllocation> getAllocations(long familyId) {
        List<FamilyAllocation> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_FAMILY_ALLOCATIONS, null,
                    DatabaseHelper.COLUMN_FA_FAMILY_ID + " = ?",
                    new String[]{ String.valueOf(familyId) },
                    null, null, DatabaseHelper.COLUMN_FA_ID + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToAllocation(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getAllocations: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Récupère les permissions d'un enfant.
     */
    public ChildPermission getChildPermissions(long childId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_CHILD_PERMISSIONS, null,
                    DatabaseHelper.COLUMN_CP_CHILD_ID + " = ?",
                    new String[]{ String.valueOf(childId) },
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToPermissions(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getChildPermissions: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        ChildPermission defaultPerm = new ChildPermission();
        defaultPerm.setChildId(childId);
        defaultPerm.setCanViewFamilyBudget(false);
        defaultPerm.setRequiresApprovalAboveMillimes(20000);
        defaultPerm.setDailySpendingLimitMillimes(50000);
        defaultPerm.setAccountActive(true);
        return defaultPerm;
    }

    /**
     * Met à jour les permissions et plafonds d'un enfant.
     */
    public boolean updateChildPermissions(ChildPermission perm) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_CP_CAN_VIEW_BUDGET, perm.isCanViewFamilyBudget() ? 1 : 0);
        cv.put(DatabaseHelper.COLUMN_CP_APPROVAL_ABOVE_MILLIMES, perm.getRequiresApprovalAboveMillimes());
        cv.put(DatabaseHelper.COLUMN_CP_DAILY_LIMIT_MILLIMES, perm.getDailySpendingLimitMillimes());
        cv.put(DatabaseHelper.COLUMN_CP_IS_ACTIVE, perm.isAccountActive() ? 1 : 0);

        int row = db.update(DatabaseHelper.TABLE_CHILD_PERMISSIONS, cv,
                DatabaseHelper.COLUMN_CP_CHILD_ID + " = ?",
                new String[]{ String.valueOf(perm.getChildId()) });
        if (row <= 0) {
            cv.put(DatabaseHelper.COLUMN_CP_CHILD_ID, perm.getChildId());
            return db.insert(DatabaseHelper.TABLE_CHILD_PERMISSIONS, null, cv) > 0;
        }
        return row > 0;
    }

    public boolean saveChildPermissions(ChildPermission perm) {
        return updateChildPermissions(perm);
    }

    /**
     * TRANSACTION ATOMIQUE : Enregistrement d'une dépense effectuée par un enfant.
     */
    public boolean recordChildExpense(long childId, String title, long amountMillimes,
                                      long catId, String catName, String store, String note) {
        if (amountMillimes <= 0) return false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                    new String[]{ DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES },
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{ String.valueOf(childId) },
                    null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) cursor.close();
                return false;
            }
            long balance = cursor.getLong(0);
            cursor.close();

            if (balance < amountMillimes) {
                Log.w(TAG, "Solde enfant insuffisant (" + balance + " < " + amountMillimes + ")");
                return false;
            }

            ContentValues cvUser = new ContentValues();
            cvUser.put(DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES, balance - amountMillimes);
            db.update(DatabaseHelper.TABLE_USERS, cvUser,
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{ String.valueOf(childId) });

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            double dtAmount = amountMillimes / 1000.0;
            ContentValues cvTrans = new ContentValues();
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_USER_ID, childId);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_BUDGET_ID, 0);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_CAT_ID, catId);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_CAT_NAME, catName != null ? catName : "Dépense");
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_PROD_ID, 0);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_TITLE, title);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_AMOUNT, dtAmount);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_AMOUNT_MILLIMES, amountMillimes);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_UNIT_PRICE, dtAmount);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_UNIT_PRICE_MILLIMES, amountMillimes);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_QUANTITY, 1.0);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_STORE, store != null ? store : "");
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_DATE, today);
            cvTrans.put(DatabaseHelper.COLUMN_TRANS_NOTE, note != null ? note : "");
            db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, cvTrans);

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erreur recordChildExpense: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Calcule le total dépensé par un enfant durant un mois donné.
     */
    public long getChildMonthlySpendingMillimes(long childId, String monthKey) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        long total = 0;
        try {
            String query = "SELECT SUM(" + DatabaseHelper.COLUMN_TRANS_AMOUNT_MILLIMES + ") FROM " +
                    DatabaseHelper.TABLE_TRANSACTIONS + " WHERE " +
                    DatabaseHelper.COLUMN_TRANS_USER_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_TRANS_DATE + " LIKE ?";
            cursor = db.rawQuery(query, new String[]{ String.valueOf(childId), monthKey + "%" });
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getChildMonthlySpendingMillimes: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }

    /**
     * Calcule le total des allocations reçues par un enfant pour un mois donné.
     */
    public long getChildTotalAllowancesMillimes(long childId, String monthKey) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        long total = 0;
        try {
            String query = "SELECT SUM(" + DatabaseHelper.COLUMN_FA_AMOUNT_MILLIMES + ") FROM " +
                    DatabaseHelper.TABLE_FAMILY_ALLOCATIONS + " WHERE " +
                    DatabaseHelper.COLUMN_FA_CHILD_ID + " = ?" +
                    (monthKey != null && !monthKey.isEmpty() ? " AND " + DatabaseHelper.COLUMN_FA_DATE + " LIKE ?" : "");
            String[] args = (monthKey != null && !monthKey.isEmpty()) ?
                    new String[]{ String.valueOf(childId), monthKey + "%" } :
                    new String[]{ String.valueOf(childId) };
            cursor = db.rawQuery(query, args);
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getChildTotalAllowancesMillimes: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }

    /**
     * Récupère les transactions d'un enfant.
     */
    public List<Transaction> getChildTransactions(long childId) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null,
                    DatabaseHelper.COLUMN_TRANS_USER_ID + " = ?",
                    new String[]{ String.valueOf(childId) },
                    null, null, DatabaseHelper.COLUMN_TRANS_ID + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToTransaction(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getChildTransactions: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Création d'un nouvel enfant avec identifiants et mot de passe temporaire à changer obligatoirement.
     */
    public long addChildWithCredentials(long parentId, long familyId, String fullName,
                                       String email, String plainPassword, long initialAllocationMillimes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String now = String.valueOf(System.currentTimeMillis());
            ContentValues cvUser = new ContentValues();
            cvUser.put(DatabaseHelper.COLUMN_USER_EMAIL, email.trim().toLowerCase());
            cvUser.put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword(plainPassword));
            cvUser.put(DatabaseHelper.COLUMN_USER_FULL_NAME, fullName.trim());
            cvUser.put(DatabaseHelper.COLUMN_USER_ROLE, User.ROLE_CHILD);
            cvUser.put(DatabaseHelper.COLUMN_USER_IS_ACTIVE, 1);
            cvUser.put(DatabaseHelper.COLUMN_USER_IS_TEMP_PASSWORD, 1); // mustChangePassword = 1
            cvUser.put(DatabaseHelper.COLUMN_USER_POINTS, 0);
            cvUser.put(DatabaseHelper.COLUMN_USER_STREAK, 0);
            cvUser.put(DatabaseHelper.COLUMN_USER_LEVEL, User.LEVEL_BEGINNER);
            cvUser.put(DatabaseHelper.COLUMN_USER_FAMILY_ID, familyId > 0 ? familyId : 1);
            cvUser.put(DatabaseHelper.COLUMN_USER_PARENT_ID, parentId);
            cvUser.put(DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES, initialAllocationMillimes > 0 ? initialAllocationMillimes : 0);
            cvUser.put(DatabaseHelper.COLUMN_USER_CREATED_AT, now);

            long childId = db.insert(DatabaseHelper.TABLE_USERS, null, cvUser);
            if (childId <= 0) return -1;

            ContentValues cvPerm = new ContentValues();
            cvPerm.put(DatabaseHelper.COLUMN_CP_CHILD_ID, childId);
            cvPerm.put(DatabaseHelper.COLUMN_CP_CAN_VIEW_BUDGET, 0);
            cvPerm.put(DatabaseHelper.COLUMN_CP_APPROVAL_ABOVE_MILLIMES, 20000);
            cvPerm.put(DatabaseHelper.COLUMN_CP_DAILY_LIMIT_MILLIMES, 50000);
            cvPerm.put(DatabaseHelper.COLUMN_CP_IS_ACTIVE, 1);
            db.insert(DatabaseHelper.TABLE_CHILD_PERMISSIONS, null, cvPerm);

            if (initialAllocationMillimes > 0) {
                allocateMoneyToChild(familyId, parentId, childId, fullName, initialAllocationMillimes, "Allocation initiale");
            }

            db.setTransactionSuccessful();
            return childId;
        } catch (Exception e) {
            Log.e(TAG, "Erreur addChildWithCredentials: " + e.getMessage(), e);
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    private FamilyAllocation cursorToAllocation(Cursor c) {
        FamilyAllocation a = new FamilyAllocation();
        a.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FA_ID)));
        a.setFamilyId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FA_FAMILY_ID)));
        a.setParentId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FA_PARENT_ID)));
        a.setChildId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FA_CHILD_ID)));
        a.setChildName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FA_CHILD_NAME)));
        a.setAmountMillimes(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FA_AMOUNT_MILLIMES)));
        a.setDescription(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FA_DESCRIPTION)));
        a.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FA_DATE)));
        a.setCreatedAt(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FA_CREATED_AT)));
        return a;
    }

    private ChildPermission cursorToPermissions(Cursor c) {
        ChildPermission p = new ChildPermission();
        p.setChildId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CP_CHILD_ID)));
        p.setCanViewFamilyBudget(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CP_CAN_VIEW_BUDGET)) == 1);
        p.setRequiresApprovalAboveMillimes(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CP_APPROVAL_ABOVE_MILLIMES)));
        p.setDailySpendingLimitMillimes(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CP_DAILY_LIMIT_MILLIMES)));
        p.setAccountActive(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CP_IS_ACTIVE)) == 1);
        return p;
    }

    private Transaction cursorToTransaction(Cursor c) {
        Transaction t = new Transaction();
        t.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_ID)));
        t.setUserId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_USER_ID)));
        t.setBudgetId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_BUDGET_ID)));
        t.setCategoryId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_CAT_ID)));
        t.setCategoryName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_CAT_NAME)));
        t.setProductId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_PROD_ID)));
        t.setTitle(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_TITLE)));
        t.setAmount(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_AMOUNT)));
        t.setAmountMillimes(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_AMOUNT_MILLIMES)));
        t.setStore(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_STORE)));
        t.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_DATE)));
        t.setNote(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_NOTE)));
        return t;
    }
}

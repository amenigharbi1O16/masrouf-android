package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.ChildPermission;
import com.example.masrouf.model.FamilyAllocation;
import com.example.masrouf.model.FamilyFunds;
import com.example.masrouf.model.MoneyRequest;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.model.User;
import com.example.masrouf.util.PasswordUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Data Access Object (DAO) dédié à la gestion du Compte Familial, des fonds partagés,
 * des allocations aux enfants, des demandes d'argent et des permissions parentales.
 * Toutes les opérations modifiant des fonds sont protégées par des transactions SQLite atomiques.
 */
public class FamilyDao {

    private static final String TAG = "FamilyDao";

    private final DatabaseHelper dbHelper;

    public FamilyDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public FamilyDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Récupère les fonds de la famille par son ID.
     */
    public FamilyFunds getFamilyFunds(long familyId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_FAMILY_FUNDS, null,
                    DatabaseHelper.COLUMN_FF_ID + " = ?",
                    new String[]{ String.valueOf(familyId) },
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToFamilyFunds(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getFamilyFunds: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    /**
     * Récupère les fonds de la famille rattachée à un parent donné.
     */
    public FamilyFunds getFamilyFundsByParent(long parentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_FAMILY_FUNDS, null,
                    DatabaseHelper.COLUMN_FF_PARENT_ID + " = ?",
                    new String[]{ String.valueOf(parentId) },
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToFamilyFunds(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getFamilyFundsByParent: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        // Si non trouvé par parent_id direct, tenter avec family_id = 1 par défaut
        return getFamilyFunds(1);
    }

    /**
     * S'assure que la trésorerie familiale existe pour le parent donné, sinon la crée avec les valeurs initiales.
     */
    public FamilyFunds ensureFamilyFundsCreated(long parentId, String familyName) {
        FamilyFunds existing = getFamilyFundsByParent(parentId);
        if (existing != null) return existing;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_FF_PARENT_ID, parentId);
        cv.put(DatabaseHelper.COLUMN_FF_FAMILY_NAME, familyName != null && !familyName.isEmpty() ? familyName : "Famille");
        cv.put(DatabaseHelper.COLUMN_FF_TOTAL_MILLIMES, 500000);
        cv.put(DatabaseHelper.COLUMN_FF_AVAILABLE_MILLIMES, 350000);
        cv.put(DatabaseHelper.COLUMN_FF_ALLOCATED_MILLIMES, 150000);
        cv.put(DatabaseHelper.COLUMN_FF_CREATED_AT, String.valueOf(System.currentTimeMillis()));

        long id = db.insert(DatabaseHelper.TABLE_FAMILY_FUNDS, null, cv);
        if (id > 0) {
            return getFamilyFunds(id);
        }
        return getFamilyFunds(1);
    }

    /**
     * Met à jour les fonds globaux d'une famille.
     */
    public boolean updateFamilyFunds(FamilyFunds funds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_FF_TOTAL_MILLIMES, funds.getTotalFundsMillimes());
        cv.put(DatabaseHelper.COLUMN_FF_AVAILABLE_MILLIMES, funds.getAvailableFundsMillimes());
        cv.put(DatabaseHelper.COLUMN_FF_ALLOCATED_MILLIMES, funds.getAllocatedFundsMillimes());
        cv.put(DatabaseHelper.COLUMN_FF_FAMILY_NAME, funds.getFamilyName());
        return db.update(DatabaseHelper.TABLE_FAMILY_FUNDS, cv,
                DatabaseHelper.COLUMN_FF_ID + " = ?",
                new String[]{ String.valueOf(funds.getFamilyId()) }) > 0;
    }

    /**
     * TRANSACTION ATOMIQUE : Allocation d'argent d'un parent vers un enfant.
     * Déduit le montant des fonds disponibles de la famille,
     * l'ajoute aux fonds alloués, crédite le solde de l'enfant et enregistre l'historique.
     */
    public boolean allocateMoneyToChild(long familyId, long parentId, long childId,
                                        String childName, long amountMillimes, String description) {
        if (amountMillimes <= 0) return false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Vérifier fonds disponibles
            FamilyFunds funds = getFamilyFunds(familyId);
            if (funds == null || funds.getAvailableFundsMillimes() < amountMillimes) {
                Log.w(TAG, "Fonds disponibles insuffisants pour allouer: " + amountMillimes);
                return false;
            }

            // 2. Mettre à jour les fonds de la famille
            funds.setAvailableFundsMillimes(funds.getAvailableFundsMillimes() - amountMillimes);
            funds.setAllocatedFundsMillimes(funds.getAllocatedFundsMillimes() + amountMillimes);
            ContentValues cvFunds = new ContentValues();
            cvFunds.put(DatabaseHelper.COLUMN_FF_AVAILABLE_MILLIMES, funds.getAvailableFundsMillimes());
            cvFunds.put(DatabaseHelper.COLUMN_FF_ALLOCATED_MILLIMES, funds.getAllocatedFundsMillimes());
            db.update(DatabaseHelper.TABLE_FAMILY_FUNDS, cvFunds,
                    DatabaseHelper.COLUMN_FF_ID + " = ?",
                    new String[]{ String.valueOf(familyId) });

            // 3. Créditer le solde de l'enfant
            Cursor childCursor = db.query(DatabaseHelper.TABLE_USERS,
                    new String[]{ DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES },
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{ String.valueOf(childId) },
                    null, null, null);
            long currentBalance = 0;
            if (childCursor != null && childCursor.moveToFirst()) {
                currentBalance = childCursor.getLong(0);
                childCursor.close();
            }

            ContentValues cvChild = new ContentValues();
            cvChild.put(DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES, currentBalance + amountMillimes);
            db.update(DatabaseHelper.TABLE_USERS, cvChild,
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{ String.valueOf(childId) });

            // 4. Enregistrer dans family_allocations
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String now = String.valueOf(System.currentTimeMillis());
            ContentValues cvAlloc = new ContentValues();
            cvAlloc.put(DatabaseHelper.COLUMN_FA_FAMILY_ID, familyId);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_PARENT_ID, parentId);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_CHILD_ID, childId);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_CHILD_NAME, childName);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_AMOUNT_MILLIMES, amountMillimes);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_DESCRIPTION, description != null ? description : "Argent alloué");
            cvAlloc.put(DatabaseHelper.COLUMN_FA_DATE, today);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_CREATED_AT, now);
            db.insert(DatabaseHelper.TABLE_FAMILY_ALLOCATIONS, null, cvAlloc);

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
     * Récupère la liste des allocations d'une famille.
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
     * Crée une demande d'argent d'un enfant pour son parent.
     */
    public long createMoneyRequest(long familyId, long childId, String childName,
                                   long parentId, long amountMillimes, String reason) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_MR_FAMILY_ID, familyId);
        cv.put(DatabaseHelper.COLUMN_MR_CHILD_ID, childId);
        cv.put(DatabaseHelper.COLUMN_MR_CHILD_NAME, childName);
        cv.put(DatabaseHelper.COLUMN_MR_PARENT_ID, parentId);
        cv.put(DatabaseHelper.COLUMN_MR_AMOUNT_MILLIMES, amountMillimes);
        cv.put(DatabaseHelper.COLUMN_MR_REASON, reason);
        cv.put(DatabaseHelper.COLUMN_MR_STATUS, MoneyRequest.STATUS_PENDING);
        cv.put(DatabaseHelper.COLUMN_MR_REQUEST_DATE, today);
        return db.insert(DatabaseHelper.TABLE_MONEY_REQUESTS, null, cv);
    }

    /**
     * Récupère les demandes en attente pour la famille.
     */
    public List<MoneyRequest> getPendingMoneyRequests(long familyId) {
        return getMoneyRequests(familyId, MoneyRequest.STATUS_PENDING);
    }

    /**
     * Récupère les demandes d'argent filtrées par statut.
     */
    public List<MoneyRequest> getMoneyRequests(long familyId, String statusFilter) {
        List<MoneyRequest> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            String selection = DatabaseHelper.COLUMN_MR_FAMILY_ID + " = ?";
            String[] args;
            if (statusFilter != null && !statusFilter.isEmpty()) {
                selection += " AND " + DatabaseHelper.COLUMN_MR_STATUS + " = ?";
                args = new String[]{ String.valueOf(familyId), statusFilter };
            } else {
                args = new String[]{ String.valueOf(familyId) };
            }

            cursor = db.query(DatabaseHelper.TABLE_MONEY_REQUESTS, null,
                    selection, args, null, null, DatabaseHelper.COLUMN_MR_ID + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToMoneyRequest(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getMoneyRequests: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Récupère les demandes d'argent faites par un enfant spécifique.
     */
    public List<MoneyRequest> getMoneyRequestsByChild(long childId) {
        List<MoneyRequest> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MONEY_REQUESTS, null,
                    DatabaseHelper.COLUMN_MR_CHILD_ID + " = ?",
                    new String[]{ String.valueOf(childId) },
                    null, null, DatabaseHelper.COLUMN_MR_ID + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToMoneyRequest(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getMoneyRequestsByChild: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * TRANSACTION ATOMIQUE : Approbation d'une demande d'argent.
     * Déduit le montant des fonds disponibles, crédite l'enfant,
     * met à jour le statut de la demande et crée une allocation.
     */
    public boolean approveMoneyRequest(long requestId, String decisionNote) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Lire la requête
            Cursor cursor = db.query(DatabaseHelper.TABLE_MONEY_REQUESTS, null,
                    DatabaseHelper.COLUMN_MR_ID + " = ?",
                    new String[]{ String.valueOf(requestId) },
                    null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) cursor.close();
                return false;
            }
            MoneyRequest req = cursorToMoneyRequest(cursor);
            cursor.close();

            if (!req.isPending()) {
                Log.w(TAG, "La demande n'est pas en attente, impossible d'approuver");
                return false;
            }

            // 2. Déduire des fonds disponibles et ajouter aux fonds alloués
            FamilyFunds funds = getFamilyFunds(req.getFamilyId());
            if (funds == null || funds.getAvailableFundsMillimes() < req.getAmountMillimes()) {
                Log.w(TAG, "Fonds insuffisants pour approuver la demande");
                return false;
            }
            funds.setAvailableFundsMillimes(funds.getAvailableFundsMillimes() - req.getAmountMillimes());
            funds.setAllocatedFundsMillimes(funds.getAllocatedFundsMillimes() + req.getAmountMillimes());
            ContentValues cvFunds = new ContentValues();
            cvFunds.put(DatabaseHelper.COLUMN_FF_AVAILABLE_MILLIMES, funds.getAvailableFundsMillimes());
            cvFunds.put(DatabaseHelper.COLUMN_FF_ALLOCATED_MILLIMES, funds.getAllocatedFundsMillimes());
            db.update(DatabaseHelper.TABLE_FAMILY_FUNDS, cvFunds,
                    DatabaseHelper.COLUMN_FF_ID + " = ?",
                    new String[]{ String.valueOf(req.getFamilyId()) });

            // 3. Créditer le compte enfant
            Cursor childCursor = db.query(DatabaseHelper.TABLE_USERS,
                    new String[]{ DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES },
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{ String.valueOf(req.getChildId()) },
                    null, null, null);
            long balance = 0;
            if (childCursor != null && childCursor.moveToFirst()) {
                balance = childCursor.getLong(0);
                childCursor.close();
            }

            ContentValues cvChild = new ContentValues();
            cvChild.put(DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES, balance + req.getAmountMillimes());
            db.update(DatabaseHelper.TABLE_USERS, cvChild,
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{ String.valueOf(req.getChildId()) });

            // 4. Mettre à jour la demande en APPROVED
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            ContentValues cvReq = new ContentValues();
            cvReq.put(DatabaseHelper.COLUMN_MR_STATUS, MoneyRequest.STATUS_APPROVED);
            cvReq.put(DatabaseHelper.COLUMN_MR_DECISION_NOTE, decisionNote != null ? decisionNote : "Approuvé par le parent");
            cvReq.put(DatabaseHelper.COLUMN_MR_DECISION_DATE, today);
            db.update(DatabaseHelper.TABLE_MONEY_REQUESTS, cvReq,
                    DatabaseHelper.COLUMN_MR_ID + " = ?",
                    new String[]{ String.valueOf(requestId) });

            // 5. Enregistrer l'allocation
            String now = String.valueOf(System.currentTimeMillis());
            ContentValues cvAlloc = new ContentValues();
            cvAlloc.put(DatabaseHelper.COLUMN_FA_FAMILY_ID, req.getFamilyId());
            cvAlloc.put(DatabaseHelper.COLUMN_FA_PARENT_ID, req.getParentId());
            cvAlloc.put(DatabaseHelper.COLUMN_FA_CHILD_ID, req.getChildId());
            cvAlloc.put(DatabaseHelper.COLUMN_FA_CHILD_NAME, req.getChildName());
            cvAlloc.put(DatabaseHelper.COLUMN_FA_AMOUNT_MILLIMES, req.getAmountMillimes());
            cvAlloc.put(DatabaseHelper.COLUMN_FA_DESCRIPTION, "Demande validée : " + req.getReason());
            cvAlloc.put(DatabaseHelper.COLUMN_FA_DATE, today);
            cvAlloc.put(DatabaseHelper.COLUMN_FA_CREATED_AT, now);
            db.insert(DatabaseHelper.TABLE_FAMILY_ALLOCATIONS, null, cvAlloc);

            db.setTransactionSuccessful();
            Log.d(TAG, "Demande " + requestId + " approuvée avec succès.");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erreur approveMoneyRequest: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Rejet d'une demande d'argent avec motif.
     */
    public boolean rejectMoneyRequest(long requestId, String decisionNote) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_MR_STATUS, MoneyRequest.STATUS_REJECTED);
        cv.put(DatabaseHelper.COLUMN_MR_DECISION_NOTE, decisionNote != null ? decisionNote : "Refusé par le parent");
        cv.put(DatabaseHelper.COLUMN_MR_DECISION_DATE, today);
        return db.update(DatabaseHelper.TABLE_MONEY_REQUESTS, cv,
                DatabaseHelper.COLUMN_MR_ID + " = ?",
                new String[]{ String.valueOf(requestId) }) > 0;
    }

    /**
     * Récupère les permissions parentales d'un enfant.
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
        // Valeurs par défaut si inexistant
        return new ChildPermission(childId, false, 20000, 50000, true);
    }

    /**
     * Enregistre ou met à jour les permissions parentales d'un enfant.
     */
    public boolean saveChildPermissions(ChildPermission p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_CP_CHILD_ID, p.getChildId());
        cv.put(DatabaseHelper.COLUMN_CP_CAN_VIEW_BUDGET, p.isCanViewFamilyBudget() ? 1 : 0);
        cv.put(DatabaseHelper.COLUMN_CP_APPROVAL_ABOVE_MILLIMES, p.getRequiresApprovalAboveMillimes());
        cv.put(DatabaseHelper.COLUMN_CP_DAILY_LIMIT_MILLIMES, p.getDailySpendingLimitMillimes());
        cv.put(DatabaseHelper.COLUMN_CP_IS_ACTIVE, p.isAccountActive() ? 1 : 0);
        long row = db.insertWithOnConflict(DatabaseHelper.TABLE_CHILD_PERMISSIONS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        return row > 0;
    }

    /**
     * TRANSACTION ATOMIQUE : Enregistrement d'une dépense effectuée par un enfant.
     * Vérifie son solde disponible, déduit le montant de son solde dans la table users
     * et enregistre la transaction.
     */
    public boolean recordChildExpense(long childId, String title, long amountMillimes,
                                      long catId, String catName, String store, String note) {
        if (amountMillimes <= 0) return false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Vérifier solde de l'enfant
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

            // 2. Déduire du solde
            ContentValues cvUser = new ContentValues();
            cvUser.put(DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES, balance - amountMillimes);
            db.update(DatabaseHelper.TABLE_USERS, cvUser,
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{ String.valueOf(childId) });

            // 3. Insérer la transaction
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
     * Calcule le total des allocations reçues par un enfant pour un mois donné (ou au total si monthKey est null).
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
     * Récupère la liste des transactions d'un enfant spécifique.
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
     * TRANSACTION ATOMIQUE : Création d'un nouvel enfant avec identifiants de connexion indépendants
     * et allocation initiale optionnelle.
     */
    public long addChildWithCredentials(long parentId, long familyId, String fullName,
                                       String email, String plainPassword, long initialAllocationMillimes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Créer le compte utilisateur enfant
            String now = String.valueOf(System.currentTimeMillis());
            ContentValues cvUser = new ContentValues();
            cvUser.put(DatabaseHelper.COLUMN_USER_EMAIL, email.trim().toLowerCase());
            cvUser.put(DatabaseHelper.COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword(plainPassword));
            cvUser.put(DatabaseHelper.COLUMN_USER_FULL_NAME, fullName.trim());
            cvUser.put(DatabaseHelper.COLUMN_USER_ROLE, User.ROLE_CHILD);
            cvUser.put(DatabaseHelper.COLUMN_USER_IS_ACTIVE, 1);
            cvUser.put(DatabaseHelper.COLUMN_USER_IS_TEMP_PASSWORD, 1); // Mot de passe temporaire à changer obligatoirement à la 1ère connexion
            cvUser.put(DatabaseHelper.COLUMN_USER_POINTS, 0);
            cvUser.put(DatabaseHelper.COLUMN_USER_STREAK, 0);
            cvUser.put(DatabaseHelper.COLUMN_USER_LEVEL, User.LEVEL_BEGINNER);
            cvUser.put(DatabaseHelper.COLUMN_USER_FAMILY_ID, familyId);
            cvUser.put(DatabaseHelper.COLUMN_USER_PARENT_ID, parentId);
            cvUser.put(DatabaseHelper.COLUMN_USER_BALANCE_MILLIMES, initialAllocationMillimes > 0 ? initialAllocationMillimes : 0);
            cvUser.put(DatabaseHelper.COLUMN_USER_CREATED_AT, now);

            long childId = db.insert(DatabaseHelper.TABLE_USERS, null, cvUser);
            if (childId <= 0) return -1;

            // 2. Créer permissions par défaut
            ContentValues cvPerm = new ContentValues();
            cvPerm.put(DatabaseHelper.COLUMN_CP_CHILD_ID, childId);
            cvPerm.put(DatabaseHelper.COLUMN_CP_CAN_VIEW_BUDGET, 0);
            cvPerm.put(DatabaseHelper.COLUMN_CP_APPROVAL_ABOVE_MILLIMES, 20000);
            cvPerm.put(DatabaseHelper.COLUMN_CP_DAILY_LIMIT_MILLIMES, 50000);
            cvPerm.put(DatabaseHelper.COLUMN_CP_IS_ACTIVE, 1);
            db.insert(DatabaseHelper.TABLE_CHILD_PERMISSIONS, null, cvPerm);

            // 3. Si allocation initiale, déduire des fonds disponibles
            if (initialAllocationMillimes > 0) {
                FamilyFunds funds = getFamilyFunds(familyId);
                if (funds != null && funds.getAvailableFundsMillimes() >= initialAllocationMillimes) {
                    funds.setAvailableFundsMillimes(funds.getAvailableFundsMillimes() - initialAllocationMillimes);
                    funds.setAllocatedFundsMillimes(funds.getAllocatedFundsMillimes() + initialAllocationMillimes);
                    ContentValues cvFunds = new ContentValues();
                    cvFunds.put(DatabaseHelper.COLUMN_FF_AVAILABLE_MILLIMES, funds.getAvailableFundsMillimes());
                    cvFunds.put(DatabaseHelper.COLUMN_FF_ALLOCATED_MILLIMES, funds.getAllocatedFundsMillimes());
                    db.update(DatabaseHelper.TABLE_FAMILY_FUNDS, cvFunds,
                            DatabaseHelper.COLUMN_FF_ID + " = ?",
                            new String[]{ String.valueOf(familyId) });

                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    ContentValues cvAlloc = new ContentValues();
                    cvAlloc.put(DatabaseHelper.COLUMN_FA_FAMILY_ID, familyId);
                    cvAlloc.put(DatabaseHelper.COLUMN_FA_PARENT_ID, parentId);
                    cvAlloc.put(DatabaseHelper.COLUMN_FA_CHILD_ID, childId);
                    cvAlloc.put(DatabaseHelper.COLUMN_FA_CHILD_NAME, fullName);
                    cvAlloc.put(DatabaseHelper.COLUMN_FA_AMOUNT_MILLIMES, initialAllocationMillimes);
                    cvAlloc.put(DatabaseHelper.COLUMN_FA_DESCRIPTION, "Allocation initiale");
                    cvAlloc.put(DatabaseHelper.COLUMN_FA_DATE, today);
                    cvAlloc.put(DatabaseHelper.COLUMN_FA_CREATED_AT, now);
                    db.insert(DatabaseHelper.TABLE_FAMILY_ALLOCATIONS, null, cvAlloc);
                }
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

    // ==========================================
    // MÉTHODES DE MAPPING RELATIONNEL -> OBJET
    // ==========================================

    private FamilyFunds cursorToFamilyFunds(Cursor c) {
        FamilyFunds f = new FamilyFunds();
        f.setFamilyId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FF_ID)));
        f.setParentId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FF_PARENT_ID)));
        f.setFamilyName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FF_FAMILY_NAME)));
        f.setTotalFundsMillimes(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FF_TOTAL_MILLIMES)));
        f.setAvailableFundsMillimes(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FF_AVAILABLE_MILLIMES)));
        f.setAllocatedFundsMillimes(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FF_ALLOCATED_MILLIMES)));
        f.setCreatedAt(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FF_CREATED_AT)));
        return f;
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

    private MoneyRequest cursorToMoneyRequest(Cursor c) {
        MoneyRequest r = new MoneyRequest();
        r.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_ID)));
        r.setFamilyId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_FAMILY_ID)));
        r.setChildId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_CHILD_ID)));
        r.setChildName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_CHILD_NAME)));
        r.setParentId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_PARENT_ID)));
        r.setAmountMillimes(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_AMOUNT_MILLIMES)));
        r.setReason(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_REASON)));
        r.setStatus(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_STATUS)));
        r.setDecisionNote(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_DECISION_NOTE)));
        r.setRequestDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_REQUEST_DATE)));
        r.setDecisionDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MR_DECISION_DATE)));
        return r;
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
        t.setUnitPrice(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_UNIT_PRICE)));
        t.setUnitPriceMillimes(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_UNIT_PRICE_MILLIMES)));
        t.setQuantity(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_QUANTITY)));
        t.setUnit(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_UNIT)));
        t.setStore(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_STORE)));
        t.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_DATE)));
        t.setNote(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANS_NOTE)));
        return t;
    }
}

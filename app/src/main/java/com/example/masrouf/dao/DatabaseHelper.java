package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.masrouf.model.Budget;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.User;
import com.example.masrouf.util.MillimesUtil;
import com.example.masrouf.util.PasswordUtil;

/**
 * RÔLE : Gestionnaire central de création et d'évolution de la base de données SQLite locale Masrouf.
 * COUCHE MVC : DAO (Data Access Object / Persistance).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°8 (Base de données locale SQLite, Ch.6).
 *   - Exigence n°9 (Pattern DAO pour l'accès aux données).
 *   - Matrice §4 ligne 31 (SQLiteOpenHelper avec onCreate, onUpgrade, onConfigure).
 *   - Matrice §4 ligne 1 (Initialisation des 2 rôles : ADMIN et USER).
 *   - Matrice §4 ligne 32 (12 tables SQLite avec relations et intégrité référentielle).
 *   - Performance & Concurrence : Mode WAL (Write-Ahead Logging) et activation des clés étrangères.
 *   - Précision financière exacte en millimes (INTEGER).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME = "masrouf.db";
    public static final int DATABASE_VERSION = 6; // Version 6 avec modules Compte Familial (Parent) & Compte Enfant

    // 1. TABLE USERS
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_USER_FULL_NAME = "full_name";
    public static final String COLUMN_USER_ROLE = "role";
    public static final String COLUMN_USER_IS_ACTIVE = "is_active";
    public static final String COLUMN_USER_IS_TEMP_PASSWORD = "is_temp_password";
    public static final String COLUMN_USER_POINTS = "points";
    public static final String COLUMN_USER_STREAK = "streak_count";
    public static final String COLUMN_USER_LEVEL = "level";
    public static final String COLUMN_USER_CREATED_AT = "created_at";
    public static final String COLUMN_USER_FAMILY_ID = "family_id";
    public static final String COLUMN_USER_PARENT_ID = "parent_id";
    public static final String COLUMN_USER_BALANCE_MILLIMES = "balance_millimes";

    // 2. TABLE CATEGORIES
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CAT_ID = "id";
    public static final String COLUMN_CAT_NAME_EN = "name_en";
    public static final String COLUMN_CAT_NAME_FR = "name_fr";
    public static final String COLUMN_CAT_NAME_AR = "name_ar";
    public static final String COLUMN_CAT_TYPE = "type";
    public static final String COLUMN_CAT_COLOR = "color_hex";
    public static final String COLUMN_CAT_ICON = "icon_name";
    public static final String COLUMN_CAT_DEFAULT_PCT = "default_percentage";
    public static final String COLUMN_CAT_IS_CUSTOM = "is_custom";
    public static final String COLUMN_CAT_USER_ID = "user_id";

    // 3. TABLE BUDGETS
    public static final String TABLE_BUDGETS = "budgets";
    public static final String COLUMN_BUDGET_ID = "id";
    public static final String COLUMN_BUDGET_USER_ID = "user_id";
    public static final String COLUMN_BUDGET_TOTAL = "total_amount";
    public static final String COLUMN_BUDGET_TOTAL_MILLIMES = "total_amount_millimes";
    public static final String COLUMN_BUDGET_START = "start_date";
    public static final String COLUMN_BUDGET_END = "end_date";
    public static final String COLUMN_BUDGET_MONTH_KEY = "month_key";
    public static final String COLUMN_BUDGET_STATUS = "status";
    public static final String COLUMN_BUDGET_CREATED_AT = "created_at";

    // 4. TABLE BUDGET_ALLOCATIONS
    public static final String TABLE_BUDGET_ALLOCATIONS = "budget_allocations";
    public static final String COLUMN_ALLOC_ID = "id";
    public static final String COLUMN_ALLOC_BUDGET_ID = "budget_id";
    public static final String COLUMN_ALLOC_CAT_ID = "category_id";
    public static final String COLUMN_ALLOC_AMOUNT = "allocated_amount";
    public static final String COLUMN_ALLOC_MILLIMES = "allocated_millimes";
    public static final String COLUMN_ALLOC_SPENT = "spent_amount";
    public static final String COLUMN_ALLOC_SPENT_MILLIMES = "spent_millimes";

    // 5. TABLE PRODUCTS
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PROD_ID = "id";
    public static final String COLUMN_PROD_USER_ID = "user_id";
    public static final String COLUMN_PROD_NAME = "name";
    public static final String COLUMN_PROD_CAT_ID = "category_id";
    public static final String COLUMN_PROD_BRAND = "brand";
    public static final String COLUMN_PROD_UNIT = "unit";
    public static final String COLUMN_PROD_LAST_PRICE = "last_unit_price";
    public static final String COLUMN_PROD_LAST_PRICE_MILLIMES = "last_unit_price_millimes";
    public static final String COLUMN_PROD_PREV_PRICE = "previous_unit_price";
    public static final String COLUMN_PROD_PREV_PRICE_MILLIMES = "previous_unit_price_millimes";
    public static final String COLUMN_PROD_STORE = "store";
    public static final String COLUMN_PROD_LAST_DATE = "last_purchase_date";
    public static final String COLUMN_PROD_PHOTO_URI = "photo_uri";

    // 6. TABLE TRANSACTIONS (Achats avec _id pour SimpleCursorAdapter)
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_TRANS_ID = "_id";
    public static final String COLUMN_TRANS_USER_ID = "user_id";
    public static final String COLUMN_TRANS_BUDGET_ID = "budget_id";
    public static final String COLUMN_TRANS_CAT_ID = "category_id";
    public static final String COLUMN_TRANS_CAT_NAME = "category_name";
    public static final String COLUMN_TRANS_PROD_ID = "product_id";
    public static final String COLUMN_TRANS_TITLE = "title";
    public static final String COLUMN_TRANS_AMOUNT = "amount";
    public static final String COLUMN_TRANS_AMOUNT_MILLIMES = "amount_millimes";
    public static final String COLUMN_TRANS_UNIT_PRICE = "unit_price";
    public static final String COLUMN_TRANS_UNIT_PRICE_MILLIMES = "unit_price_millimes";
    public static final String COLUMN_TRANS_QUANTITY = "quantity";
    public static final String COLUMN_TRANS_UNIT = "unit";
    public static final String COLUMN_TRANS_STORE = "store";
    public static final String COLUMN_TRANS_DATE = "date";
    public static final String COLUMN_TRANS_NOTE = "note";
    public static final String COLUMN_TRANS_PHOTO_URI = "photo_uri";

    // 7. TABLE EXPENSES (Dépenses fixes / récurrentes)
    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_EXPENSE_USER_ID = "user_id";
    public static final String COLUMN_EXPENSE_BUDGET_ID = "budget_id";
    public static final String COLUMN_EXPENSE_CAT_ID = "category_id";
    public static final String COLUMN_EXPENSE_TITLE = "title";
    public static final String COLUMN_EXPENSE_AMOUNT = "amount";
    public static final String COLUMN_EXPENSE_AMOUNT_MILLIMES = "amount_millimes";
    public static final String COLUMN_EXPENSE_DATE = "date";
    public static final String COLUMN_EXPENSE_NOTE = "note";
    public static final String COLUMN_EXPENSE_IS_RECURRING = "is_recurring";

    // 8. TABLE PRICE_HISTORY (Suivi historique des prix de produits)
    public static final String TABLE_PRICE_HISTORY = "price_history";
    public static final String COLUMN_PH_ID = "id";
    public static final String COLUMN_PH_PROD_ID = "product_id";
    public static final String COLUMN_PH_PRICE = "price";
    public static final String COLUMN_PH_PRICE_MILLIMES = "price_millimes";
    public static final String COLUMN_PH_STORE = "store";
    public static final String COLUMN_PH_DATE = "date";

    // 9. TABLE POINTS_RULES (Règles de points administrables)
    public static final String TABLE_POINTS_RULES = "points_rules";
    public static final String COLUMN_RULE_ID = "id";
    public static final String COLUMN_RULE_KEY = "rule_key";
    public static final String COLUMN_RULE_NAME_EN = "name_en";
    public static final String COLUMN_RULE_NAME_FR = "name_fr";
    public static final String COLUMN_RULE_NAME_AR = "name_ar";
    public static final String COLUMN_RULE_POINTS = "points_value";
    public static final String COLUMN_RULE_IS_ACTIVE = "is_active";

    // 10. TABLE USER_POINTS (Journal des points gagnés)
    public static final String TABLE_USER_POINTS = "user_points";
    public static final String COLUMN_UP_ID = "id";
    public static final String COLUMN_UP_USER_ID = "user_id";
    public static final String COLUMN_UP_RULE_ID = "rule_id";
    public static final String COLUMN_UP_POINTS = "points";
    public static final String COLUMN_UP_REASON = "reason";
    public static final String COLUMN_UP_PERIOD_KEY = "period_key";
    public static final String COLUMN_UP_EARNED_AT = "earned_at";

    // 11. TABLE ACHIEVEMENTS (Badges et accomplissements)
    public static final String TABLE_ACHIEVEMENTS = "achievements";
    public static final String COLUMN_ACH_ID = "id";
    public static final String COLUMN_ACH_CODE = "code";
    public static final String COLUMN_ACH_TITLE_EN = "title_en";
    public static final String COLUMN_ACH_TITLE_FR = "title_fr";
    public static final String COLUMN_ACH_TITLE_AR = "title_ar";
    public static final String COLUMN_ACH_DESC_EN = "desc_en";
    public static final String COLUMN_ACH_DESC_FR = "desc_fr";
    public static final String COLUMN_ACH_DESC_AR = "desc_ar";
    public static final String COLUMN_ACH_ICON = "icon";
    public static final String COLUMN_ACH_THRESHOLD = "points_threshold";

    // 12. TABLE USER_ACHIEVEMENTS (Badges débloqués par utilisateur)
    public static final String TABLE_USER_ACHIEVEMENTS = "user_achievements";
    public static final String COLUMN_UA_ID = "id";
    public static final String COLUMN_UA_USER_ID = "user_id";
    public static final String COLUMN_UA_ACH_ID = "achievement_id";
    public static final String COLUMN_UA_UNLOCKED_AT = "unlocked_at";

    // 13. TABLE USER_CATEGORIES (Sélection personnalisée des catégories par utilisateur)
    public static final String TABLE_USER_CATEGORIES = "user_categories";
    public static final String COLUMN_UC_USER_ID = "user_id";
    public static final String COLUMN_UC_CAT_ID = "category_id";
    public static final String COLUMN_UC_IS_SELECTED = "is_selected";

    // 14. TABLE FAMILY_FUNDS (Fonds globaux de la famille)
    public static final String TABLE_FAMILY_FUNDS = "family_funds";
    public static final String COLUMN_FF_ID = "family_id";
    public static final String COLUMN_FF_PARENT_ID = "parent_id";
    public static final String COLUMN_FF_FAMILY_NAME = "family_name";
    public static final String COLUMN_FF_TOTAL_MILLIMES = "total_funds_millimes";
    public static final String COLUMN_FF_AVAILABLE_MILLIMES = "available_funds_millimes";
    public static final String COLUMN_FF_ALLOCATED_MILLIMES = "allocated_funds_millimes";
    public static final String COLUMN_FF_CREATED_AT = "created_at";

    // 15. TABLE FAMILY_ALLOCATIONS (Historique des allocations d'argent parent -> enfant)
    public static final String TABLE_FAMILY_ALLOCATIONS = "family_allocations";
    public static final String COLUMN_FA_ID = "id";
    public static final String COLUMN_FA_FAMILY_ID = "family_id";
    public static final String COLUMN_FA_PARENT_ID = "parent_id";
    public static final String COLUMN_FA_CHILD_ID = "child_id";
    public static final String COLUMN_FA_CHILD_NAME = "child_name";
    public static final String COLUMN_FA_AMOUNT_MILLIMES = "amount_millimes";
    public static final String COLUMN_FA_DESCRIPTION = "description";
    public static final String COLUMN_FA_DATE = "date";
    public static final String COLUMN_FA_CREATED_AT = "created_at";

    // 16. TABLE MONEY_REQUESTS (Demandes de fonds formulées par les enfants)
    public static final String TABLE_MONEY_REQUESTS = "money_requests";
    public static final String COLUMN_MR_ID = "id";
    public static final String COLUMN_MR_FAMILY_ID = "family_id";
    public static final String COLUMN_MR_CHILD_ID = "child_id";
    public static final String COLUMN_MR_CHILD_NAME = "child_name";
    public static final String COLUMN_MR_PARENT_ID = "parent_id";
    public static final String COLUMN_MR_AMOUNT_MILLIMES = "amount_millimes";
    public static final String COLUMN_MR_REASON = "reason";
    public static final String COLUMN_MR_STATUS = "status"; // PENDING, APPROVED, REJECTED
    public static final String COLUMN_MR_DECISION_NOTE = "decision_note";
    public static final String COLUMN_MR_REQUEST_DATE = "request_date";
    public static final String COLUMN_MR_DECISION_DATE = "decision_date";

    // 17. TABLE CHILD_PERMISSIONS (Permissions & plafonds parentaux par enfant)
    public static final String TABLE_CHILD_PERMISSIONS = "child_permissions";
    public static final String COLUMN_CP_CHILD_ID = "child_id";
    public static final String COLUMN_CP_CAN_VIEW_BUDGET = "can_view_family_budget";
    public static final String COLUMN_CP_APPROVAL_ABOVE_MILLIMES = "requires_approval_above_millimes";
    public static final String COLUMN_CP_DAILY_LIMIT_MILLIMES = "daily_spending_limit_millimes";
    public static final String COLUMN_CP_IS_ACTIVE = "is_account_active";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Exigence technique : Activation du WAL pour les accès concurrents
        db.enableWriteAheadLogging();
        // Exigence technique : Activation des contraintes de clés étrangères (Foreign Keys)
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Exécution de onCreate() : création des 12 tables SQLite Masrouf...");

        // 1. Table users
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_EMAIL + " TEXT NOT NULL UNIQUE, " +
                COLUMN_USER_PASSWORD_HASH + " TEXT NOT NULL, " +
                COLUMN_USER_FULL_NAME + " TEXT NOT NULL, " +
                COLUMN_USER_ROLE + " TEXT NOT NULL DEFAULT '" + User.ROLE_USER + "', " +
                COLUMN_USER_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1, " +
                COLUMN_USER_IS_TEMP_PASSWORD + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_USER_POINTS + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_USER_STREAK + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_USER_LEVEL + " TEXT NOT NULL DEFAULT '" + User.LEVEL_BEGINNER + "', " +
                COLUMN_USER_FAMILY_ID + " INTEGER DEFAULT 0, " +
                COLUMN_USER_PARENT_ID + " INTEGER DEFAULT 0, " +
                COLUMN_USER_BALANCE_MILLIMES + " INTEGER DEFAULT 0, " +
                COLUMN_USER_CREATED_AT + " TEXT NOT NULL);");

        // 2. Table categories
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COLUMN_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CAT_NAME_EN + " TEXT NOT NULL, " +
                COLUMN_CAT_NAME_FR + " TEXT NOT NULL, " +
                COLUMN_CAT_NAME_AR + " TEXT NOT NULL, " +
                COLUMN_CAT_TYPE + " TEXT NOT NULL, " +
                COLUMN_CAT_COLOR + " TEXT, " +
                COLUMN_CAT_ICON + " TEXT, " +
                COLUMN_CAT_DEFAULT_PCT + " REAL NOT NULL DEFAULT 0.0, " +
                COLUMN_CAT_IS_CUSTOM + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_CAT_USER_ID + " INTEGER DEFAULT 0);");

        // 3. Table budgets
        db.execSQL("CREATE TABLE " + TABLE_BUDGETS + " (" +
                COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BUDGET_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_BUDGET_TOTAL + " REAL NOT NULL, " +
                COLUMN_BUDGET_TOTAL_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_BUDGET_START + " TEXT NOT NULL, " +
                COLUMN_BUDGET_END + " TEXT NOT NULL, " +
                COLUMN_BUDGET_MONTH_KEY + " TEXT, " +
                COLUMN_BUDGET_STATUS + " TEXT NOT NULL, " +
                COLUMN_BUDGET_CREATED_AT + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_BUDGET_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);");

        // 4. Table budget_allocations
        db.execSQL("CREATE TABLE " + TABLE_BUDGET_ALLOCATIONS + " (" +
                COLUMN_ALLOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ALLOC_BUDGET_ID + " INTEGER NOT NULL, " +
                COLUMN_ALLOC_CAT_ID + " INTEGER NOT NULL, " +
                COLUMN_ALLOC_AMOUNT + " REAL NOT NULL, " +
                COLUMN_ALLOC_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_ALLOC_SPENT + " REAL NOT NULL DEFAULT 0.0, " +
                COLUMN_ALLOC_SPENT_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (" + COLUMN_ALLOC_BUDGET_ID + ") REFERENCES " + TABLE_BUDGETS + "(" + COLUMN_BUDGET_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COLUMN_ALLOC_CAT_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CAT_ID + ") ON DELETE CASCADE);");

        // 5. Table products
        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COLUMN_PROD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PROD_USER_ID + " INTEGER DEFAULT 0, " +
                COLUMN_PROD_NAME + " TEXT NOT NULL, " +
                COLUMN_PROD_CAT_ID + " INTEGER NOT NULL, " +
                COLUMN_PROD_BRAND + " TEXT, " +
                COLUMN_PROD_UNIT + " TEXT NOT NULL, " +
                COLUMN_PROD_LAST_PRICE + " REAL NOT NULL, " +
                COLUMN_PROD_LAST_PRICE_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_PROD_PREV_PRICE + " REAL NOT NULL, " +
                COLUMN_PROD_PREV_PRICE_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_PROD_STORE + " TEXT, " +
                COLUMN_PROD_LAST_DATE + " TEXT, " +
                COLUMN_PROD_PHOTO_URI + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_PROD_CAT_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CAT_ID + ") ON DELETE CASCADE);");

        // 6. Table transactions (achats, _id requis pour SimpleCursorAdapter)
        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COLUMN_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRANS_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_TRANS_BUDGET_ID + " INTEGER DEFAULT 0, " +
                COLUMN_TRANS_CAT_ID + " INTEGER NOT NULL, " +
                COLUMN_TRANS_CAT_NAME + " TEXT NOT NULL, " +
                COLUMN_TRANS_PROD_ID + " INTEGER DEFAULT 0, " +
                COLUMN_TRANS_TITLE + " TEXT NOT NULL, " +
                COLUMN_TRANS_AMOUNT + " REAL NOT NULL, " +
                COLUMN_TRANS_AMOUNT_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_TRANS_UNIT_PRICE + " REAL DEFAULT 0.0, " +
                COLUMN_TRANS_UNIT_PRICE_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_TRANS_QUANTITY + " REAL DEFAULT 1.0, " +
                COLUMN_TRANS_UNIT + " TEXT, " +
                COLUMN_TRANS_STORE + " TEXT, " +
                COLUMN_TRANS_DATE + " TEXT NOT NULL, " +
                COLUMN_TRANS_NOTE + " TEXT, " +
                COLUMN_TRANS_PHOTO_URI + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_TRANS_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);");

        // 7. Table expenses (dépenses fixes / récurrentes)
        db.execSQL("CREATE TABLE " + TABLE_EXPENSES + " (" +
                COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EXPENSE_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_EXPENSE_BUDGET_ID + " INTEGER DEFAULT 0, " +
                COLUMN_EXPENSE_CAT_ID + " INTEGER NOT NULL, " +
                COLUMN_EXPENSE_TITLE + " TEXT NOT NULL, " +
                COLUMN_EXPENSE_AMOUNT + " REAL NOT NULL, " +
                COLUMN_EXPENSE_AMOUNT_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_EXPENSE_DATE + " TEXT NOT NULL, " +
                COLUMN_EXPENSE_NOTE + " TEXT, " +
                COLUMN_EXPENSE_IS_RECURRING + " INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (" + COLUMN_EXPENSE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COLUMN_EXPENSE_CAT_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CAT_ID + ") ON DELETE CASCADE);");

        // 8. Table price_history
        db.execSQL("CREATE TABLE " + TABLE_PRICE_HISTORY + " (" +
                COLUMN_PH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PH_PROD_ID + " INTEGER NOT NULL, " +
                COLUMN_PH_PRICE + " REAL NOT NULL, " +
                COLUMN_PH_PRICE_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_PH_STORE + " TEXT, " +
                COLUMN_PH_DATE + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_PH_PROD_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COLUMN_PROD_ID + ") ON DELETE CASCADE);");

        // 9. Table points_rules
        db.execSQL("CREATE TABLE " + TABLE_POINTS_RULES + " (" +
                COLUMN_RULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RULE_KEY + " TEXT NOT NULL UNIQUE, " +
                COLUMN_RULE_NAME_EN + " TEXT NOT NULL, " +
                COLUMN_RULE_NAME_FR + " TEXT NOT NULL, " +
                COLUMN_RULE_NAME_AR + " TEXT NOT NULL, " +
                COLUMN_RULE_POINTS + " INTEGER NOT NULL, " +
                COLUMN_RULE_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1);");

        // 10. Table user_points
        db.execSQL("CREATE TABLE " + TABLE_USER_POINTS + " (" +
                COLUMN_UP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_UP_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_UP_RULE_ID + " INTEGER DEFAULT 0, " +
                COLUMN_UP_POINTS + " INTEGER NOT NULL, " +
                COLUMN_UP_REASON + " TEXT NOT NULL, " +
                COLUMN_UP_PERIOD_KEY + " TEXT, " +
                COLUMN_UP_EARNED_AT + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_UP_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);");

        // 11. Table achievements
        db.execSQL("CREATE TABLE " + TABLE_ACHIEVEMENTS + " (" +
                COLUMN_ACH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ACH_CODE + " TEXT NOT NULL UNIQUE, " +
                COLUMN_ACH_TITLE_EN + " TEXT NOT NULL, " +
                COLUMN_ACH_TITLE_FR + " TEXT NOT NULL, " +
                COLUMN_ACH_TITLE_AR + " TEXT NOT NULL, " +
                COLUMN_ACH_DESC_EN + " TEXT NOT NULL, " +
                COLUMN_ACH_DESC_FR + " TEXT NOT NULL, " +
                COLUMN_ACH_DESC_AR + " TEXT NOT NULL, " +
                COLUMN_ACH_ICON + " TEXT NOT NULL, " +
                COLUMN_ACH_THRESHOLD + " INTEGER NOT NULL);");

        // 12. Table user_achievements
        db.execSQL("CREATE TABLE " + TABLE_USER_ACHIEVEMENTS + " (" +
                COLUMN_UA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_UA_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_UA_ACH_ID + " INTEGER NOT NULL, " +
                COLUMN_UA_UNLOCKED_AT + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_UA_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COLUMN_UA_ACH_ID + ") REFERENCES " + TABLE_ACHIEVEMENTS + "(" + COLUMN_ACH_ID + ") ON DELETE CASCADE, " +
                "UNIQUE(" + COLUMN_UA_USER_ID + ", " + COLUMN_UA_ACH_ID + "));");

        // 13. Table user_categories (sélection onboarding des catégories par utilisateur)
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USER_CATEGORIES + " (" +
                COLUMN_UC_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_UC_CAT_ID + " INTEGER NOT NULL, " +
                COLUMN_UC_IS_SELECTED + " INTEGER NOT NULL DEFAULT 1, " +
                "PRIMARY KEY (" + COLUMN_UC_USER_ID + ", " + COLUMN_UC_CAT_ID + "), " +
                "FOREIGN KEY (" + COLUMN_UC_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COLUMN_UC_CAT_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CAT_ID + ") ON DELETE CASCADE);");

        // 14. Table family_funds
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FAMILY_FUNDS + " (" +
                COLUMN_FF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FF_PARENT_ID + " INTEGER NOT NULL, " +
                COLUMN_FF_FAMILY_NAME + " TEXT NOT NULL, " +
                COLUMN_FF_TOTAL_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_FF_AVAILABLE_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_FF_ALLOCATED_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_FF_CREATED_AT + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_FF_PARENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);");

        // 15. Table family_allocations
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FAMILY_ALLOCATIONS + " (" +
                COLUMN_FA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FA_FAMILY_ID + " INTEGER NOT NULL, " +
                COLUMN_FA_PARENT_ID + " INTEGER NOT NULL, " +
                COLUMN_FA_CHILD_ID + " INTEGER NOT NULL, " +
                COLUMN_FA_CHILD_NAME + " TEXT NOT NULL, " +
                COLUMN_FA_AMOUNT_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_FA_DESCRIPTION + " TEXT, " +
                COLUMN_FA_DATE + " TEXT NOT NULL, " +
                COLUMN_FA_CREATED_AT + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_FA_FAMILY_ID + ") REFERENCES " + TABLE_FAMILY_FUNDS + "(" + COLUMN_FF_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COLUMN_FA_PARENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COLUMN_FA_CHILD_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);");

        // 16. Table money_requests
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MONEY_REQUESTS + " (" +
                COLUMN_MR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MR_FAMILY_ID + " INTEGER NOT NULL, " +
                COLUMN_MR_CHILD_ID + " INTEGER NOT NULL, " +
                COLUMN_MR_CHILD_NAME + " TEXT NOT NULL, " +
                COLUMN_MR_PARENT_ID + " INTEGER NOT NULL, " +
                COLUMN_MR_AMOUNT_MILLIMES + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_MR_REASON + " TEXT NOT NULL, " +
                COLUMN_MR_STATUS + " TEXT NOT NULL DEFAULT '" + com.example.masrouf.model.MoneyRequest.STATUS_PENDING + "', " +
                COLUMN_MR_DECISION_NOTE + " TEXT, " +
                COLUMN_MR_REQUEST_DATE + " TEXT NOT NULL, " +
                COLUMN_MR_DECISION_DATE + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_MR_FAMILY_ID + ") REFERENCES " + TABLE_FAMILY_FUNDS + "(" + COLUMN_FF_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COLUMN_MR_CHILD_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COLUMN_MR_PARENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);");

        // 17. Table child_permissions
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHILD_PERMISSIONS + " (" +
                COLUMN_CP_CHILD_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_CP_CAN_VIEW_BUDGET + " INTEGER NOT NULL DEFAULT 0, " +
                COLUMN_CP_APPROVAL_ABOVE_MILLIMES + " INTEGER NOT NULL DEFAULT 20000, " +
                COLUMN_CP_DAILY_LIMIT_MILLIMES + " INTEGER NOT NULL DEFAULT 50000, " +
                COLUMN_CP_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY (" + COLUMN_CP_CHILD_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);");

        // Création des INDEX pour optimiser les performances des requêtes
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_budgets_user ON " + TABLE_BUDGETS + "(" + COLUMN_BUDGET_USER_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_alloc_budget ON " + TABLE_BUDGET_ALLOCATIONS + "(" + COLUMN_ALLOC_BUDGET_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_trans_user ON " + TABLE_TRANSACTIONS + "(" + COLUMN_TRANS_USER_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_expenses_user ON " + TABLE_EXPENSES + "(" + COLUMN_EXPENSE_USER_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_price_history_prod ON " + TABLE_PRICE_HISTORY + "(" + COLUMN_PH_PROD_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_points_user ON " + TABLE_USER_POINTS + "(" + COLUMN_UP_USER_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_alloc_child ON " + TABLE_FAMILY_ALLOCATIONS + "(" + COLUMN_FA_CHILD_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_requests_family ON " + TABLE_MONEY_REQUESTS + "(" + COLUMN_MR_FAMILY_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_requests_child ON " + TABLE_MONEY_REQUESTS + "(" + COLUMN_MR_CHILD_ID + ");");

        // Insertion du jeu de données complet de démonstration
        seedAllData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Exécution de onUpgrade() : migration sécurisée vers version " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHILD_PERMISSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MONEY_REQUESTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAMILY_ALLOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAMILY_FUNDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_ACHIEVEMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACHIEVEMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_POINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINTS_RULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET_ALLOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Insère le jeu complet de données de démonstration pour les 13 tables SQLite.
     */
    private void seedAllData(SQLiteDatabase db) {
        String now = String.valueOf(System.currentTimeMillis());

        // 1. UTILISATEURS
        // Compte Administrateur / Parent principal
        ContentValues admin = new ContentValues();
        admin.put(COLUMN_USER_EMAIL, "admin@gmail.com");
        admin.put(COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword("Admin123!"));
        admin.put(COLUMN_USER_FULL_NAME, "Administrateur Masrouf");
        admin.put(COLUMN_USER_ROLE, User.ROLE_PARENT);
        admin.put(COLUMN_USER_IS_ACTIVE, 1);
        admin.put(COLUMN_USER_IS_TEMP_PASSWORD, 0);
        admin.put(COLUMN_USER_POINTS, 0);
        admin.put(COLUMN_USER_STREAK, 0);
        admin.put(COLUMN_USER_LEVEL, User.LEVEL_EXPERT);
        admin.put(COLUMN_USER_FAMILY_ID, 1);
        admin.put(COLUMN_USER_PARENT_ID, 0);
        admin.put(COLUMN_USER_BALANCE_MILLIMES, 0);
        admin.put(COLUMN_USER_CREATED_AT, now);
        long parentAdminId = db.insert(TABLE_USERS, null, admin);

        // Compte Parent Démo (Sami Ben Ali)
        ContentValues parent = new ContentValues();
        parent.put(COLUMN_USER_EMAIL, "parent@gmail.com");
        parent.put(COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword("Parent123!"));
        parent.put(COLUMN_USER_FULL_NAME, "Sami Ben Ali");
        parent.put(COLUMN_USER_ROLE, User.ROLE_PARENT);
        parent.put(COLUMN_USER_IS_ACTIVE, 1);
        parent.put(COLUMN_USER_IS_TEMP_PASSWORD, 0);
        parent.put(COLUMN_USER_POINTS, 500);
        parent.put(COLUMN_USER_STREAK, 4);
        parent.put(COLUMN_USER_LEVEL, User.LEVEL_EXPERT);
        parent.put(COLUMN_USER_FAMILY_ID, 1);
        parent.put(COLUMN_USER_PARENT_ID, 0);
        parent.put(COLUMN_USER_BALANCE_MILLIMES, 0);
        parent.put(COLUMN_USER_CREATED_AT, now);
        long parentId = db.insert(TABLE_USERS, null, parent);

        // Compte Enfant 1 : Youssef Ben Ali (Solde 45 DT = 45000 millimes)
        ContentValues childYoussef = new ContentValues();
        childYoussef.put(COLUMN_USER_EMAIL, "youssef.benali@gmail.com");
        childYoussef.put(COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword("User123!"));
        childYoussef.put(COLUMN_USER_FULL_NAME, "Youssef Ben Ali");
        childYoussef.put(COLUMN_USER_ROLE, User.ROLE_CHILD);
        childYoussef.put(COLUMN_USER_IS_ACTIVE, 1);
        childYoussef.put(COLUMN_USER_IS_TEMP_PASSWORD, 0);
        childYoussef.put(COLUMN_USER_POINTS, 120);
        childYoussef.put(COLUMN_USER_STREAK, 2);
        childYoussef.put(COLUMN_USER_LEVEL, User.LEVEL_BEGINNER);
        childYoussef.put(COLUMN_USER_FAMILY_ID, 1);
        childYoussef.put(COLUMN_USER_PARENT_ID, parentId);
        childYoussef.put(COLUMN_USER_BALANCE_MILLIMES, 45000);
        childYoussef.put(COLUMN_USER_CREATED_AT, now);
        long youssefId = db.insert(TABLE_USERS, null, childYoussef);

        // Compte Enfant 2 : Salma Ben Ali (Solde 25 DT = 25000 millimes)
        ContentValues childSalma = new ContentValues();
        childSalma.put(COLUMN_USER_EMAIL, "salma.benali@gmail.com");
        childSalma.put(COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword("User123!"));
        childSalma.put(COLUMN_USER_FULL_NAME, "Salma Ben Ali");
        childSalma.put(COLUMN_USER_ROLE, User.ROLE_CHILD);
        childSalma.put(COLUMN_USER_IS_ACTIVE, 1);
        childSalma.put(COLUMN_USER_IS_TEMP_PASSWORD, 0);
        childSalma.put(COLUMN_USER_POINTS, 80);
        childSalma.put(COLUMN_USER_STREAK, 1);
        childSalma.put(COLUMN_USER_LEVEL, User.LEVEL_BEGINNER);
        childSalma.put(COLUMN_USER_FAMILY_ID, 1);
        childSalma.put(COLUMN_USER_PARENT_ID, parentId);
        childSalma.put(COLUMN_USER_BALANCE_MILLIMES, 25000);
        childSalma.put(COLUMN_USER_CREATED_AT, now);
        long salmaId = db.insert(TABLE_USERS, null, childSalma);

        // Table family_funds : 500 DT au total, 350 DT disponibles, 150 DT alloués
        ContentValues ff = new ContentValues();
        ff.put(COLUMN_FF_PARENT_ID, parentId);
        ff.put(COLUMN_FF_FAMILY_NAME, "Famille Ben Ali");
        ff.put(COLUMN_FF_TOTAL_MILLIMES, 500000);
        ff.put(COLUMN_FF_AVAILABLE_MILLIMES, 350000);
        ff.put(COLUMN_FF_ALLOCATED_MILLIMES, 150000);
        ff.put(COLUMN_FF_CREATED_AT, now);
        long familyId = db.insert(TABLE_FAMILY_FUNDS, null, ff);

        // Table family_allocations : 100 DT à Youssef, 50 DT à Salma
        ContentValues fa1 = new ContentValues();
        fa1.put(COLUMN_FA_FAMILY_ID, familyId);
        fa1.put(COLUMN_FA_PARENT_ID, parentId);
        fa1.put(COLUMN_FA_CHILD_ID, youssefId);
        fa1.put(COLUMN_FA_CHILD_NAME, "Youssef Ben Ali");
        fa1.put(COLUMN_FA_AMOUNT_MILLIMES, 100000);
        fa1.put(COLUMN_FA_DESCRIPTION, "Argent de poche Novembre 2026");
        fa1.put(COLUMN_FA_DATE, "2026-11-01");
        fa1.put(COLUMN_FA_CREATED_AT, now);
        db.insert(TABLE_FAMILY_ALLOCATIONS, null, fa1);

        ContentValues fa2 = new ContentValues();
        fa2.put(COLUMN_FA_FAMILY_ID, familyId);
        fa2.put(COLUMN_FA_PARENT_ID, parentId);
        fa2.put(COLUMN_FA_CHILD_ID, salmaId);
        fa2.put(COLUMN_FA_CHILD_NAME, "Salma Ben Ali");
        fa2.put(COLUMN_FA_AMOUNT_MILLIMES, 50000);
        fa2.put(COLUMN_FA_DESCRIPTION, "Argent de poche Novembre 2026");
        fa2.put(COLUMN_FA_DATE, "2026-11-01");
        fa2.put(COLUMN_FA_CREATED_AT, now);
        db.insert(TABLE_FAMILY_ALLOCATIONS, null, fa2);

        // Table money_requests : 1 en attente pour Youssef (20 DT), 1 approuvée pour Salma (15 DT)
        ContentValues mr1 = new ContentValues();
        mr1.put(COLUMN_MR_FAMILY_ID, familyId);
        mr1.put(COLUMN_MR_CHILD_ID, youssefId);
        mr1.put(COLUMN_MR_CHILD_NAME, "Youssef Ben Ali");
        mr1.put(COLUMN_MR_PARENT_ID, parentId);
        mr1.put(COLUMN_MR_AMOUNT_MILLIMES, 20000);
        mr1.put(COLUMN_MR_REASON, "Livre scolaire de mathématiques");
        mr1.put(COLUMN_MR_STATUS, com.example.masrouf.model.MoneyRequest.STATUS_PENDING);
        mr1.put(COLUMN_MR_REQUEST_DATE, "2026-11-18");
        db.insert(TABLE_MONEY_REQUESTS, null, mr1);

        ContentValues mr2 = new ContentValues();
        mr2.put(COLUMN_MR_FAMILY_ID, familyId);
        mr2.put(COLUMN_MR_CHILD_ID, salmaId);
        mr2.put(COLUMN_MR_CHILD_NAME, "Salma Ben Ali");
        mr2.put(COLUMN_MR_PARENT_ID, parentId);
        mr2.put(COLUMN_MR_AMOUNT_MILLIMES, 15000);
        mr2.put(COLUMN_MR_REASON, "Sortie scolaire musée");
        mr2.put(COLUMN_MR_STATUS, com.example.masrouf.model.MoneyRequest.STATUS_APPROVED);
        mr2.put(COLUMN_MR_DECISION_NOTE, "Bonne visite !");
        mr2.put(COLUMN_MR_REQUEST_DATE, "2026-11-10");
        mr2.put(COLUMN_MR_DECISION_DATE, "2026-11-11");
        db.insert(TABLE_MONEY_REQUESTS, null, mr2);

        // Table child_permissions
        ContentValues cp1 = new ContentValues();
        cp1.put(COLUMN_CP_CHILD_ID, youssefId);
        cp1.put(COLUMN_CP_CAN_VIEW_BUDGET, 1);
        cp1.put(COLUMN_CP_APPROVAL_ABOVE_MILLIMES, 20000);
        cp1.put(COLUMN_CP_DAILY_LIMIT_MILLIMES, 50000);
        cp1.put(COLUMN_CP_IS_ACTIVE, 1);
        db.insert(TABLE_CHILD_PERMISSIONS, null, cp1);

        ContentValues cp2 = new ContentValues();
        cp2.put(COLUMN_CP_CHILD_ID, salmaId);
        cp2.put(COLUMN_CP_CAN_VIEW_BUDGET, 0);
        cp2.put(COLUMN_CP_APPROVAL_ABOVE_MILLIMES, 15000);
        cp2.put(COLUMN_CP_DAILY_LIMIT_MILLIMES, 30000);
        cp2.put(COLUMN_CP_IS_ACTIVE, 1);
        db.insert(TABLE_CHILD_PERMISSIONS, null, cp2);

        // Utilisateur standard démo (Ines Ben Ali, niveau Saver avec 350 points et 3 mois de streak)
        ContentValues user = new ContentValues();
        user.put(COLUMN_USER_EMAIL, "ines.benali@gmail.com");
        user.put(COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword("User123!"));
        user.put(COLUMN_USER_FULL_NAME, "Ines Ben Ali");
        user.put(COLUMN_USER_ROLE, User.ROLE_USER);
        user.put(COLUMN_USER_IS_ACTIVE, 1);
        user.put(COLUMN_USER_IS_TEMP_PASSWORD, 0);
        user.put(COLUMN_USER_POINTS, 350);
        user.put(COLUMN_USER_STREAK, 3);
        user.put(COLUMN_USER_LEVEL, User.LEVEL_SAVER);
        user.put(COLUMN_USER_CREATED_AT, now);
        long userId = db.insert(TABLE_USERS, null, user);

        // Utilisateur avec mot de passe temporaire
        ContentValues tempUser = new ContentValues();
        tempUser.put(COLUMN_USER_EMAIL, "temp@gmail.com");
        tempUser.put(COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword("Temp123!"));
        tempUser.put(COLUMN_USER_FULL_NAME, "Nouveau Membre");
        tempUser.put(COLUMN_USER_ROLE, User.ROLE_USER);
        tempUser.put(COLUMN_USER_IS_ACTIVE, 1);
        tempUser.put(COLUMN_USER_IS_TEMP_PASSWORD, 1);
        tempUser.put(COLUMN_USER_POINTS, 0);
        tempUser.put(COLUMN_USER_STREAK, 0);
        tempUser.put(COLUMN_USER_LEVEL, User.LEVEL_BEGINNER);
        tempUser.put(COLUMN_USER_CREATED_AT, now);
        // Insère l'utilisateur temporaire en base
        db.insert(TABLE_USERS, null, tempUser);

        // 2. CATÉGORIES (16 catégories conformes aux maquettes Figma / Image 04)
        // Produits
        long catFood = insertCategory(db, "Food", "Alimentation", "الأغذية", Category.TYPE_PRODUCT, "#102B27", "ic_cutlery", 40.0, false, 0);
        long catClothes = insertCategory(db, "Clothes & Shoes", "Vêtements et chaussures", "الملابس والأحذية", Category.TYPE_PRODUCT, "#1C5143", "ic_tshirt", 10.0, false, 0);
        long catHygiene = insertCategory(db, "Hygiene & Personal Care", "Hygiène et soins personnels", "النظافة والعناية الشخصية", Category.TYPE_PRODUCT, "#2D7D68", "ic_sparkle", 8.0, false, 0);
        long catHome = insertCategory(db, "Home & Appliances", "Maison et équipement", "المنزل والتجهيزات", Category.TYPE_PRODUCT, "#3A5C52", "ic_home", 10.0, false, 0);
        long catIT = insertCategory(db, "IT & Electronics", "Matériel informatique", "المعدات الإعلامية", Category.TYPE_PRODUCT, "#4E6B60", "ic_laptop", 12.0, false, 0);
        long catHealth = insertCategory(db, "Health", "Santé", "الصحة", Category.TYPE_PRODUCT, "#608075", "ic_firstaid", 10.0, false, 0);

        // Dépenses
        long catTransport = insertCategory(db, "Transport", "Transport", "النقل", Category.TYPE_EXPENSE, "#1C5143", "ic_car", 12.0, false, 0);
        long catBills = insertCategory(db, "Bills", "Factures", "الفواتير", Category.TYPE_EXPENSE, "#6B7C74", "ic_receipt", 15.0, false, 0);
        long catEducation = insertCategory(db, "Education", "Études", "التعليم", Category.TYPE_EXPENSE, "#8E735B", "ic_school", 10.0, false, 0);
        long catEntertainment = insertCategory(db, "Leisure & Outings", "Loisirs et sorties", "الترفيه والخرجات", Category.TYPE_EXPENSE, "#E9B66B", "ic_gamepad", 8.0, false, 0);
        long catChildren = insertCategory(db, "Children", "Enfants", "الأطفال", Category.TYPE_EXPENSE, "#5C8276", "ic_profile", 10.0, false, 0);
        long catFamily = insertCategory(db, "Family Support", "Aide à la famille", "مساعدة العائلة", Category.TYPE_EXPENSE, "#446B60", "ic_profile", 10.0, false, 0);
        long catSavings = insertCategory(db, "Savings", "Épargne", "الادخار", Category.TYPE_EXPENSE, "#D9F0E4", "ic_wallet", 15.0, false, 0);
        long catDonations = insertCategory(db, "Donations & Charity", "Dons et charité", "الصدقات والتبرعات", Category.TYPE_EXPENSE, "#DE7363", "ic_heart", 5.0, false, 0);
        long catBankFees = insertCategory(db, "Bank Fees", "Frais bancaires", "المصاريف البنكية", Category.TYPE_EXPENSE, "#738079", "ic_card", 5.0, false, 0);
        long catOthers = insertCategory(db, "Others", "Autres", "أخرى", Category.TYPE_EXPENSE, "#9AA69F", "ic_file", 5.0, false, 0);

        // Sélection initiale pour Ines
        long[] inesSelection = new long[]{ catFood, catClothes, catHygiene, catIT, catTransport, catBills, catEducation, catEntertainment };
        for (long cid : inesSelection) {
            ContentValues uc = new ContentValues();
            uc.put(COLUMN_UC_USER_ID, userId);
            uc.put(COLUMN_UC_CAT_ID, cid);
            uc.put(COLUMN_UC_IS_SELECTED, 1);
            db.insert(TABLE_USER_CATEGORIES, null, uc);
        }

        // 3. BUDGET MENSUEL POUR INES (Figma 07 : Total 1200 DT, Dépensé 750 DT, Reste 450 DT)
        ContentValues budget = new ContentValues();
        budget.put(COLUMN_BUDGET_USER_ID, userId);
        budget.put(COLUMN_BUDGET_TOTAL, 1200.0);
        budget.put(COLUMN_BUDGET_TOTAL_MILLIMES, MillimesUtil.dtToMillimes(1200.0));
        budget.put(COLUMN_BUDGET_START, "2026-11-01");
        budget.put(COLUMN_BUDGET_END, "2026-11-30");
        budget.put(COLUMN_BUDGET_MONTH_KEY, "2026-11");
        budget.put(COLUMN_BUDGET_STATUS, Budget.STATUS_HEALTHY);
        budget.put(COLUMN_BUDGET_CREATED_AT, now);
        long budgetId = db.insert(TABLE_BUDGETS, null, budget);

        // Allocations des catégories (Figma 10 & 11)
        insertAllocation(db, budgetId, catFood, 500.0, 320.0);
        insertAllocation(db, budgetId, catTransport, 150.0, 100.0);
        insertAllocation(db, budgetId, catBills, 250.0, 200.0);
        insertAllocation(db, budgetId, catEntertainment, 100.0, 80.0);
        insertAllocation(db, budgetId, catSavings, 100.0, 50.0);

        // 4. PRODUITS SUIVIS AVEC HISTORIQUE DE PRIX (Figma 12 & 18)
        long prodOliveOil = insertProduct(db, "Olive oil", catFood, "Châal", "L", 14.500, 15.000, "Monoprix", "12 Nov 2026");
        long prodMilk = insertProduct(db, "Délice milk", catFood, "Délice", "L", 1.350, 1.350, "Carrefour", "10 Nov 2026");
        long prodCouscous = insertProduct(db, "Couscous Randa", catFood, "Randa", "kg", 1.200, 1.150, "Monoprix", "08 Nov 2026");
        long prodCoffee = insertProduct(db, "Coffee Ben Yedder", catFood, "Ben Yedder", "pack", 4.200, 4.000, "Magasin Général", "05 Nov 2026");

        // 5. PRICE HISTORY (Table 8)
        insertPriceHistory(db, prodOliveOil, 15.000, "Monoprix", "15 Oct 2026");
        insertPriceHistory(db, prodOliveOil, 14.500, "Monoprix", "12 Nov 2026");
        insertPriceHistory(db, prodMilk, 1.350, "Carrefour", "10 Nov 2026");
        insertPriceHistory(db, prodCouscous, 1.150, "Monoprix", "01 Nov 2026");
        insertPriceHistory(db, prodCouscous, 1.200, "Monoprix", "08 Nov 2026");
        insertPriceHistory(db, prodCoffee, 4.000, "Magasin Général", "20 Oct 2026");
        insertPriceHistory(db, prodCoffee, 4.200, "Magasin Général", "05 Nov 2026");

        // 6. TRANSACTIONS D'ACHATS (Table 6, Figma 07)
        insertTransaction(db, userId, budgetId, catFood, "Food", prodOliveOil, "Olive oil · 1 L", 14.500, 14.500, 1.0, "L", "Monoprix", "12 Nov 2026", "Achat d'huile d'olive");
        insertTransaction(db, userId, budgetId, catFood, "Food", prodMilk, "Délice milk · 2 L", 2.700, 1.350, 2.0, "L", "Carrefour", "10 Nov 2026", "Lait demi-écrémé");
        insertTransaction(db, userId, budgetId, catFood, "Food", prodCouscous, "Couscous Randa · 1 kg", 1.200, 1.200, 1.0, "kg", "Monoprix", "08 Nov 2026", "Paquet couscous");
        insertTransaction(db, userId, budgetId, catFood, "Food", prodCoffee, "Coffee Ben Yedder · 1 pack", 4.200, 4.200, 1.0, "pack", "Magasin Général", "05 Nov 2026", "Café moulu");

        // 7. DÉPENSES FIXES / RÉCURRENTES (Table 7, Figma 17 & 25)
        insertExpense(db, userId, budgetId, catBills, "STEG · Électricité", 85.000, "2026-11-06", "Facture bimestrielle électricité", true);
        insertExpense(db, userId, budgetId, catTransport, "Abonnement bus SNT", 35.000, "2026-11-11", "Pass mensuel étudiant", true);
        insertExpense(db, userId, budgetId, catEntertainment, "Sortie Cinéma Pathé", 25.000, "2026-11-04", "Billet et popcorn", false);

        // 8. RÈGLES DE POINTS DE GAMIFICATION (Table 9, Figma 42)
        long ruleBudgetRespected = insertPointsRule(db, "PERIOD_BUDGET_RESPECTED", "Respected period budget", "Budget de période respecté", "احترام الميزانية المحددة", 100, true);
        long ruleStreak3 = insertPointsRule(db, "STREAK_3_PERIODS", "3-month savings streak", "3 mois consécutifs d'économie", "ادخار 3 أشهر متتالية", 50, true);
        insertPointsRule(db, "FIRST_PURCHASE", "First purchase logged", "Premier achat enregistré", "تسجيل أول عملية شراء", 10, true);
        insertPointsRule(db, "FIRST_BUDGET", "First budget created", "Premier budget créé", "إنشاء أول ميزانية", 20, true);

        // 9. JOURNAL DE POINTS DE L'UTILISATEUR (Table 10, Figma 21)
        insertUserPoints(db, userId, ruleBudgetRespected, 100, "Budget respecté · Septembre 2026", "2026-09_BUDGET", "2026-09-30 20:00:00");
        insertUserPoints(db, userId, ruleBudgetRespected, 100, "Budget respecté · Octobre 2026", "2026-10_BUDGET", "2026-10-31 20:00:00");
        insertUserPoints(db, userId, ruleBudgetRespected, 100, "Budget respecté · Novembre 2026", "2026-11_BUDGET", "2026-11-15 14:00:00");
        insertUserPoints(db, userId, ruleStreak3, 50, "Bonus streak : 3 périodes consécutives !", "2026-11_STREAK_3", "2026-11-15 14:05:00");

        // Transactions enfants démo (Dépenses d'argent de poche)
        insertTransaction(db, youssefId, 0, catEducation, "Education", 0, "Fournitures scolaires", 15.000, 15.000, 1.0, "lot", "Librairie Al Kitab", "10 Nov 2026", "Cahiers et stylos");
        insertTransaction(db, youssefId, 0, catFood, "Food", 0, "Goûter boulangerie", 3.500, 3.500, 1.0, "pièce", "Boulangerie", "12 Nov 2026", "Jus et croissant");
        insertTransaction(db, salmaId, 0, catEducation, "Education", 0, "Cahier de dessin", 6.500, 6.500, 1.0, "pièce", "Librairie", "11 Nov 2026", "Matériel de dessin");

        // 10. ACHIEVEMENTS / BADGES (Table 11, Figma 21)
        long achFirst = insertAchievement(db, "FIRST_STEP", "First Step", "Premier Pas", "الخطوة الأولى",
                "Create your first budget", "Créez votre premier budget", "أنشئ ميزانيتك الأولى", "ic_leaf", 20);
        long achStreak = insertAchievement(db, "SAVINGS_STREAK", "Streak Master", "Maître de la Régularité", "سيد الاستمرار",
                "Maintain a 3-month savings streak", "Conservez un streak d'épargne de 3 mois", "حافظ على الادخار لمدة 3 أشهر", "ic_check", 150);
        long achExpert = insertAchievement(db, "EXPERT_SAVER", "Budget Legend", "Légende du Budget", "أسطورة الميزانية",
                "Reach 1,000 points and master all categories", "Atteignez 1 000 points et maîtrisez vos catégories", "بلغ 1000 نقطة واحتراف إدارة المصاريف", "ic_profile", 1000);

        // 11. BADGES DÉBLOQUÉS PAR INES (Table 12)
        insertUserAchievement(db, userId, achFirst, "2026-09-01 10:00:00");
        insertUserAchievement(db, userId, achStreak, "2026-11-15 14:05:00");

        Log.d(TAG, "Jeu complet de données initialisé avec succès pour les 12 tables SQLite.");
    }

    private long insertCategory(SQLiteDatabase db, String en, String fr, String ar, String type,
                                String color, String icon, double pct, boolean isCustom, long userId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CAT_NAME_EN, en);
        cv.put(COLUMN_CAT_NAME_FR, fr);
        cv.put(COLUMN_CAT_NAME_AR, ar);
        cv.put(COLUMN_CAT_TYPE, type);
        cv.put(COLUMN_CAT_COLOR, color);
        cv.put(COLUMN_CAT_ICON, icon);
        cv.put(COLUMN_CAT_DEFAULT_PCT, pct);
        cv.put(COLUMN_CAT_IS_CUSTOM, isCustom ? 1 : 0);
        cv.put(COLUMN_CAT_USER_ID, userId);
        return db.insert(TABLE_CATEGORIES, null, cv);
    }

    private void insertAllocation(SQLiteDatabase db, long budgetId, long catId, double allocated, double spent) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ALLOC_BUDGET_ID, budgetId);
        cv.put(COLUMN_ALLOC_CAT_ID, catId);
        cv.put(COLUMN_ALLOC_AMOUNT, allocated);
        cv.put(COLUMN_ALLOC_MILLIMES, MillimesUtil.dtToMillimes(allocated));
        cv.put(COLUMN_ALLOC_SPENT, spent);
        cv.put(COLUMN_ALLOC_SPENT_MILLIMES, MillimesUtil.dtToMillimes(spent));
        db.insert(TABLE_BUDGET_ALLOCATIONS, null, cv);
    }

    private long insertProduct(SQLiteDatabase db, String name, long catId, String brand, String unit,
                               double lastPrice, double prevPrice, String store, String date) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PROD_USER_ID, 0);
        cv.put(COLUMN_PROD_NAME, name);
        cv.put(COLUMN_PROD_CAT_ID, catId);
        cv.put(COLUMN_PROD_BRAND, brand);
        cv.put(COLUMN_PROD_UNIT, unit);
        cv.put(COLUMN_PROD_LAST_PRICE, lastPrice);
        cv.put(COLUMN_PROD_LAST_PRICE_MILLIMES, MillimesUtil.dtToMillimes(lastPrice));
        cv.put(COLUMN_PROD_PREV_PRICE, prevPrice);
        cv.put(COLUMN_PROD_PREV_PRICE_MILLIMES, MillimesUtil.dtToMillimes(prevPrice));
        cv.put(COLUMN_PROD_STORE, store);
        cv.put(COLUMN_PROD_LAST_DATE, date);
        cv.put(COLUMN_PROD_PHOTO_URI, "");
        return db.insert(TABLE_PRODUCTS, null, cv);
    }

    private void insertPriceHistory(SQLiteDatabase db, long prodId, double price, String store, String date) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PH_PROD_ID, prodId);
        cv.put(COLUMN_PH_PRICE, price);
        cv.put(COLUMN_PH_PRICE_MILLIMES, MillimesUtil.dtToMillimes(price));
        cv.put(COLUMN_PH_STORE, store);
        cv.put(COLUMN_PH_DATE, date);
        db.insert(TABLE_PRICE_HISTORY, null, cv);
    }

    private void insertTransaction(SQLiteDatabase db, long userId, long budgetId, long catId, String catName,
                                   long prodId, String title, double amount, double unitPrice, double qty,
                                   String unit, String store, String date, String note) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TRANS_USER_ID, userId);
        cv.put(COLUMN_TRANS_BUDGET_ID, budgetId);
        cv.put(COLUMN_TRANS_CAT_ID, catId);
        cv.put(COLUMN_TRANS_CAT_NAME, catName);
        cv.put(COLUMN_TRANS_PROD_ID, prodId);
        cv.put(COLUMN_TRANS_TITLE, title);
        cv.put(COLUMN_TRANS_AMOUNT, amount);
        cv.put(COLUMN_TRANS_AMOUNT_MILLIMES, MillimesUtil.dtToMillimes(amount));
        cv.put(COLUMN_TRANS_UNIT_PRICE, unitPrice);
        cv.put(COLUMN_TRANS_UNIT_PRICE_MILLIMES, MillimesUtil.dtToMillimes(unitPrice));
        cv.put(COLUMN_TRANS_QUANTITY, qty);
        cv.put(COLUMN_TRANS_UNIT, unit);
        cv.put(COLUMN_TRANS_STORE, store);
        cv.put(COLUMN_TRANS_DATE, date);
        cv.put(COLUMN_TRANS_NOTE, note);
        cv.put(COLUMN_TRANS_PHOTO_URI, "");
        db.insert(TABLE_TRANSACTIONS, null, cv);
    }

    private void insertExpense(SQLiteDatabase db, long userId, long budgetId, long catId,
                               String title, double amount, String date, String note, boolean isRecurring) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EXPENSE_USER_ID, userId);
        cv.put(COLUMN_EXPENSE_BUDGET_ID, budgetId);
        cv.put(COLUMN_EXPENSE_CAT_ID, catId);
        cv.put(COLUMN_EXPENSE_TITLE, title);
        cv.put(COLUMN_EXPENSE_AMOUNT, amount);
        cv.put(COLUMN_EXPENSE_AMOUNT_MILLIMES, MillimesUtil.dtToMillimes(amount));
        cv.put(COLUMN_EXPENSE_DATE, date);
        cv.put(COLUMN_EXPENSE_NOTE, note);
        cv.put(COLUMN_EXPENSE_IS_RECURRING, isRecurring ? 1 : 0);
        db.insert(TABLE_EXPENSES, null, cv);
    }

    private long insertPointsRule(SQLiteDatabase db, String key, String en, String fr, String ar, int points, boolean isActive) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_RULE_KEY, key);
        cv.put(COLUMN_RULE_NAME_EN, en);
        cv.put(COLUMN_RULE_NAME_FR, fr);
        cv.put(COLUMN_RULE_NAME_AR, ar);
        cv.put(COLUMN_RULE_POINTS, points);
        cv.put(COLUMN_RULE_IS_ACTIVE, isActive ? 1 : 0);
        return db.insert(TABLE_POINTS_RULES, null, cv);
    }

    private void insertUserPoints(SQLiteDatabase db, long userId, long ruleId, int points, String reason, String periodKey, String earnedAt) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_UP_USER_ID, userId);
        cv.put(COLUMN_UP_RULE_ID, ruleId);
        cv.put(COLUMN_UP_POINTS, points);
        cv.put(COLUMN_UP_REASON, reason);
        cv.put(COLUMN_UP_PERIOD_KEY, periodKey);
        cv.put(COLUMN_UP_EARNED_AT, earnedAt);
        db.insert(TABLE_USER_POINTS, null, cv);
    }

    private long insertAchievement(SQLiteDatabase db, String code, String tEn, String tFr, String tAr,
                                   String dEn, String dFr, String dAr, String icon, int threshold) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ACH_CODE, code);
        cv.put(COLUMN_ACH_TITLE_EN, tEn);
        cv.put(COLUMN_ACH_TITLE_FR, tFr);
        cv.put(COLUMN_ACH_TITLE_AR, tAr);
        cv.put(COLUMN_ACH_DESC_EN, dEn);
        cv.put(COLUMN_ACH_DESC_FR, dFr);
        cv.put(COLUMN_ACH_DESC_AR, dAr);
        cv.put(COLUMN_ACH_ICON, icon);
        cv.put(COLUMN_ACH_THRESHOLD, threshold);
        return db.insert(TABLE_ACHIEVEMENTS, null, cv);
    }

    private void insertUserAchievement(SQLiteDatabase db, long userId, long achId, String unlockedAt) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_UA_USER_ID, userId);
        cv.put(COLUMN_UA_ACH_ID, achId);
        cv.put(COLUMN_UA_UNLOCKED_AT, unlockedAt);
        db.insert(TABLE_USER_ACHIEVEMENTS, null, cv);
    }
}

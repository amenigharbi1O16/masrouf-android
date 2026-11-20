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
    public static final int DATABASE_VERSION = 4; // Version 4 avec 12 tables, WAL, clés étrangères et millimes

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

        // Création des INDEX pour optimiser les performances des requêtes
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_budgets_user ON " + TABLE_BUDGETS + "(" + COLUMN_BUDGET_USER_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_alloc_budget ON " + TABLE_BUDGET_ALLOCATIONS + "(" + COLUMN_ALLOC_BUDGET_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_trans_user ON " + TABLE_TRANSACTIONS + "(" + COLUMN_TRANS_USER_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_expenses_user ON " + TABLE_EXPENSES + "(" + COLUMN_EXPENSE_USER_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_price_history_prod ON " + TABLE_PRICE_HISTORY + "(" + COLUMN_PH_PROD_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_points_user ON " + TABLE_USER_POINTS + "(" + COLUMN_UP_USER_ID + ");");

        // Insertion du jeu de données complet de démonstration
        seedAllData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Exécution de onUpgrade() : migration sécurisée vers version " + newVersion);
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
     * Insère le jeu complet de données de démonstration pour les 12 tables SQLite.
     */
    private void seedAllData(SQLiteDatabase db) {
        String now = String.valueOf(System.currentTimeMillis());

        // 1. UTILISATEURS
        // Compte Administrateur
        ContentValues admin = new ContentValues();
        admin.put(COLUMN_USER_EMAIL, "admin@gmail.com");
        admin.put(COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword("Admin123!"));
        admin.put(COLUMN_USER_FULL_NAME, "Administrateur Masrouf");
        admin.put(COLUMN_USER_ROLE, User.ROLE_ADMIN);
        admin.put(COLUMN_USER_IS_ACTIVE, 1);
        admin.put(COLUMN_USER_IS_TEMP_PASSWORD, 0);
        admin.put(COLUMN_USER_POINTS, 0);
        admin.put(COLUMN_USER_STREAK, 0);
        admin.put(COLUMN_USER_LEVEL, User.LEVEL_EXPERT);
        admin.put(COLUMN_USER_CREATED_AT, now);
        db.insert(TABLE_USERS, null, admin);

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
        db.insert(TABLE_USERS, null, tempUser);

        // Compte suspendu de test
        ContentValues suspended = new ContentValues();
        suspended.put(COLUMN_USER_EMAIL, "suspendu@gmail.com");
        suspended.put(COLUMN_USER_PASSWORD_HASH, PasswordUtil.hashPassword("Suspendu123!"));
        suspended.put(COLUMN_USER_FULL_NAME, "Utilisateur Suspendu");
        suspended.put(COLUMN_USER_ROLE, User.ROLE_USER);
        suspended.put(COLUMN_USER_IS_ACTIVE, 0);
        suspended.put(COLUMN_USER_IS_TEMP_PASSWORD, 0);
        suspended.put(COLUMN_USER_POINTS, 0);
        suspended.put(COLUMN_USER_STREAK, 0);
        suspended.put(COLUMN_USER_LEVEL, User.LEVEL_BEGINNER);
        suspended.put(COLUMN_USER_CREATED_AT, now);
        db.insert(TABLE_USERS, null, suspended);

        // 2. CATÉGORIES (Figma 10 & 40)
        long catFood = insertCategory(db, "Food", "Alimentation", "الأغذية", Category.TYPE_PRODUCT, "#102B27", "ic_leaf", 41.6, false, 0);
        long catTransport = insertCategory(db, "Transport", "Transport", "النقل", Category.TYPE_EXPENSE, "#1C5143", "ic_menu", 12.5, false, 0);
        long catBills = insertCategory(db, "Bills", "Factures", "الفواتير", Category.TYPE_EXPENSE, "#6B7C74", "ic_profile", 20.8, false, 0);
        long catEntertainment = insertCategory(db, "Entertainment", "Loisirs", "الترفيه", Category.TYPE_EXPENSE, "#E9B66B", "ic_profile", 8.3, false, 0);
        long catSavings = insertCategory(db, "Savings", "Épargne", "الادخار", Category.TYPE_EXPENSE, "#D9F0E4", "ic_check", 8.3, false, 0);

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

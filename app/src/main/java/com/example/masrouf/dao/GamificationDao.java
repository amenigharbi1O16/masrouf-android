package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.Achievement;
import com.example.masrouf.model.PointsRule;
import com.example.masrouf.model.User;
import com.example.masrouf.model.UserPoints;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Data Access Object pour la Gamification (points, streaks, récompenses anti-doublon et badges).
 * COUCHE MVC : DAO.
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°9 (Composant DAO).
 *   - Spécifications Gamification :
 *       - 100 points récompensés une seule fois par période de budget respecté (anti-duplicate ledger).
 *       - Bonus de streak (+50 points) pour 3 périodes consécutives d'économie.
 *       - Niveaux : Débutant (0-299), Économe (300-999), Expert (1 000+).
 */
public class GamificationDao {

    private static final String TAG = "GamificationDao";
    private final DatabaseHelper dbHelper;

    public GamificationDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Vérifie si l'utilisateur a déjà reçu la récompense budgétaire pour cette période.
     */
    public boolean hasReceivedRewardForPeriod(long userId, String periodKey) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USER_POINTS,
                    new String[]{ DatabaseHelper.COLUMN_UP_ID },
                    DatabaseHelper.COLUMN_UP_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_UP_PERIOD_KEY + " = ?",
                    new String[]{ String.valueOf(userId), periodKey },
                    null, null, null
            );
            exists = (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            Log.e(TAG, "Erreur hasReceivedRewardForPeriod : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return exists;
    }

    /**
     * Récompense un budget de période respecté sans doublon (100 points + bonus streak éventuel).
     * @return le nombre de points attribués (0 si déjà récompensé).
     */
    public int awardPeriodBudgetRespected(long userId, String periodKey) {
        if (hasReceivedRewardForPeriod(userId, periodKey)) {
            Log.d(TAG, "Récompense déjà attribuée pour la période : " + periodKey);
            return 0; // Anti-doublon strict
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int pointsAwarded = 100;
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());

        // 1. Enregistrement dans user_points
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_UP_USER_ID, userId);
        cv.put(DatabaseHelper.COLUMN_UP_RULE_ID, 1);
        cv.put(DatabaseHelper.COLUMN_UP_POINTS, 100);
        cv.put(DatabaseHelper.COLUMN_UP_REASON, "Budget respecté · " + periodKey);
        cv.put(DatabaseHelper.COLUMN_UP_PERIOD_KEY, periodKey);
        cv.put(DatabaseHelper.COLUMN_UP_EARNED_AT, now);
        db.insert(DatabaseHelper.TABLE_USER_POINTS, null, cv);

        // 2. Mise à jour de l'utilisateur (points + streak)
        UserDao userDao = new UserDao(dbHelper);
        User user = userDao.getUserById(userId);
        if (user != null) {
            int newStreak = user.getStreakCount() + 1;
            int totalPoints = user.getPoints() + 100;

            // Bonus de streak : 3 périodes consécutives = +50 points
            if (newStreak % 3 == 0) {
                pointsAwarded += 50;
                totalPoints += 50;

                ContentValues bonusCv = new ContentValues();
                bonusCv.put(DatabaseHelper.COLUMN_UP_USER_ID, userId);
                bonusCv.put(DatabaseHelper.COLUMN_UP_RULE_ID, 2);
                bonusCv.put(DatabaseHelper.COLUMN_UP_POINTS, 50);
                bonusCv.put(DatabaseHelper.COLUMN_UP_REASON, "Bonus Streak 3 mois !");
                bonusCv.put(DatabaseHelper.COLUMN_UP_PERIOD_KEY, periodKey + "_STREAK");
                bonusCv.put(DatabaseHelper.COLUMN_UP_EARNED_AT, now);
                db.insert(DatabaseHelper.TABLE_USER_POINTS, null, bonusCv);
            }

            user.setPoints(totalPoints);
            user.setStreakCount(newStreak);
            user.updateCalculatedLevel();

            ContentValues userUpdate = new ContentValues();
            userUpdate.put(DatabaseHelper.COLUMN_USER_POINTS, user.getPoints());
            userUpdate.put(DatabaseHelper.COLUMN_USER_STREAK, user.getStreakCount());
            userUpdate.put(DatabaseHelper.COLUMN_USER_LEVEL, user.getLevel());
            db.update(DatabaseHelper.TABLE_USERS, userUpdate, DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{ String.valueOf(userId) });

            // Vérifier et débloquer les badges
            checkAndUnlockAchievements(userId, user.getPoints(), newStreak);
        }

        return pointsAwarded;
    }

    /**
     * Débloque automatiquement les badges mérités selon les points et le streak.
     */
    public void checkAndUnlockAchievements(long userId, int points, int streak) {
        List<Achievement> achievements = getAllAchievements(userId);
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (Achievement a : achievements) {
            if (!a.isUnlocked()) {
                boolean shouldUnlock = false;
                if ("FIRST_STEP".equals(a.getCode()) && points >= a.getPointsThreshold()) {
                    shouldUnlock = true;
                } else if ("SAVINGS_STREAK".equals(a.getCode()) && streak >= 3) {
                    shouldUnlock = true;
                } else if ("EXPERT_SAVER".equals(a.getCode()) && points >= a.getPointsThreshold()) {
                    shouldUnlock = true;
                }

                if (shouldUnlock) {
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COLUMN_UA_USER_ID, userId);
                    cv.put(DatabaseHelper.COLUMN_UA_ACH_ID, a.getId());
                    cv.put(DatabaseHelper.COLUMN_UA_UNLOCKED_AT, now);
                    db.insertWithOnConflict(DatabaseHelper.TABLE_USER_ACHIEVEMENTS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
        }
    }

    public List<UserPoints> getUserPointsLedger(long userId) {
        List<UserPoints> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USER_POINTS,
                    null,
                    DatabaseHelper.COLUMN_UP_USER_ID + " = ?",
                    new String[]{ String.valueOf(userId) },
                    null, null,
                    DatabaseHelper.COLUMN_UP_ID + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    UserPoints up = new UserPoints();
                    up.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UP_ID)));
                    up.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UP_USER_ID)));
                    up.setRuleId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UP_RULE_ID)));
                    up.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UP_POINTS)));
                    up.setReason(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UP_REASON)));
                    up.setPeriodKey(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UP_PERIOD_KEY)));
                    up.setEarnedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UP_EARNED_AT)));
                    list.add(up);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getUserPointsLedger : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public List<Achievement> getAllAchievements(long userId) {
        List<Achievement> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT a." + DatabaseHelper.COLUMN_ACH_ID + ", " +
                "a." + DatabaseHelper.COLUMN_ACH_CODE + ", " +
                "a." + DatabaseHelper.COLUMN_ACH_TITLE_EN + ", " +
                "a." + DatabaseHelper.COLUMN_ACH_TITLE_FR + ", " +
                "a." + DatabaseHelper.COLUMN_ACH_TITLE_AR + ", " +
                "a." + DatabaseHelper.COLUMN_ACH_DESC_EN + ", " +
                "a." + DatabaseHelper.COLUMN_ACH_DESC_FR + ", " +
                "a." + DatabaseHelper.COLUMN_ACH_DESC_AR + ", " +
                "a." + DatabaseHelper.COLUMN_ACH_ICON + ", " +
                "a." + DatabaseHelper.COLUMN_ACH_THRESHOLD + ", " +
                "ua." + DatabaseHelper.COLUMN_UA_UNLOCKED_AT + " " +
                "FROM " + DatabaseHelper.TABLE_ACHIEVEMENTS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USER_ACHIEVEMENTS + " ua " +
                "ON a." + DatabaseHelper.COLUMN_ACH_ID + " = ua." + DatabaseHelper.COLUMN_UA_ACH_ID + " AND ua." + DatabaseHelper.COLUMN_UA_USER_ID + " = ? " +
                "ORDER BY a." + DatabaseHelper.COLUMN_ACH_THRESHOLD + " ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, new String[]{ String.valueOf(userId) });
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Achievement ach = new Achievement();
                    ach.setId(cursor.getLong(0));
                    ach.setCode(cursor.getString(1));
                    ach.setTitleEn(cursor.getString(2));
                    ach.setTitleFr(cursor.getString(3));
                    ach.setTitleAr(cursor.getString(4));
                    ach.setDescEn(cursor.getString(5));
                    ach.setDescFr(cursor.getString(6));
                    ach.setDescAr(cursor.getString(7));
                    ach.setIcon(cursor.getString(8));
                    ach.setPointsThreshold(cursor.getInt(9));
                    String unlockedAt = cursor.getString(10);
                    ach.setUnlocked(unlockedAt != null);
                    ach.setUnlockedAt(unlockedAt != null ? unlockedAt : "");
                    list.add(ach);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getAllAchievements : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public List<PointsRule> getAllPointsRules() {
        List<PointsRule> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseHelper.TABLE_POINTS_RULES, null, null, null, null, null, DatabaseHelper.COLUMN_RULE_ID + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PointsRule pr = new PointsRule();
                    pr.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RULE_ID)));
                    pr.setRuleKey(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RULE_KEY)));
                    pr.setNameEn(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RULE_NAME_EN)));
                    pr.setNameFr(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RULE_NAME_FR)));
                    pr.setNameAr(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RULE_NAME_AR)));
                    pr.setPointsValue(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RULE_POINTS)));
                    pr.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RULE_IS_ACTIVE)) == 1);
                    list.add(pr);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getAllPointsRules : " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    public int updatePointsRule(long id, int pointsValue, boolean isActive) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_RULE_POINTS, pointsValue);
        cv.put(DatabaseHelper.COLUMN_RULE_IS_ACTIVE, isActive ? 1 : 0);
        return db.update(DatabaseHelper.TABLE_POINTS_RULES, cv, DatabaseHelper.COLUMN_RULE_ID + " = ?", new String[]{ String.valueOf(id) });
    }
}

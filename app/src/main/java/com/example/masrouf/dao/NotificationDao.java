package com.example.masrouf.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.masrouf.model.AppNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) dédié aux notifications utilisateur dans SQLite.
 * Couche DAO (MVC).
 */
public class NotificationDao {

    private static final String TAG = "NotificationDao";
    private final DatabaseHelper dbHelper;

    public NotificationDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public NotificationDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Insère une notification avec prévention automatique des doublons.
     */
    public long insertNotification(long userId, String title, String message, String type, String actionRoute) {
        if (userId <= 0 || title == null || message == null) return -1;

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 1. Détection des doublons récents (même titre et type pour cet utilisateur dans la dernière minute)
        long currentTs = System.currentTimeMillis();
        long windowStart = currentTs - 60000; // 60 secondes

        Cursor checkCursor = null;
        try {
            checkCursor = db.query(DatabaseHelper.TABLE_NOTIFICATIONS,
                    new String[]{ DatabaseHelper.COLUMN_NOTIF_ID },
                    DatabaseHelper.COLUMN_NOTIF_USER_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_NOTIF_TITLE + " = ? AND " +
                    DatabaseHelper.COLUMN_NOTIF_TIMESTAMP + " >= ?",
                    new String[]{ String.valueOf(userId), title, String.valueOf(windowStart) },
                    null, null, null);

            if (checkCursor != null && checkCursor.moveToFirst()) {
                Log.d(TAG, "Notification en doublon ignorée pour userId=" + userId + ", title=" + title);
                return -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur check duplicate notification: " + e.getMessage());
        } finally {
            if (checkCursor != null) checkCursor.close();
        }

        // 2. Insertion
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NOTIF_USER_ID, userId);
        cv.put(DatabaseHelper.COLUMN_NOTIF_TITLE, title);
        cv.put(DatabaseHelper.COLUMN_NOTIF_MESSAGE, message);
        cv.put(DatabaseHelper.COLUMN_NOTIF_TYPE, type != null ? type : AppNotification.TYPE_WELCOME);
        cv.put(DatabaseHelper.COLUMN_NOTIF_IS_READ, 0);
        cv.put(DatabaseHelper.COLUMN_NOTIF_TIMESTAMP, currentTs);
        cv.put(DatabaseHelper.COLUMN_NOTIF_PERIOD_KEY, "Aujourd'hui");
        cv.put(DatabaseHelper.COLUMN_NOTIF_ACTION_ROUTE, actionRoute);

        long id = -1;
        try {
            id = db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, cv);
            Log.d(TAG, "Notification insérée (ID=" + id + ") pour userId=" + userId);
        } catch (Exception e) {
            Log.e(TAG, "Erreur insertNotification: " + e.getMessage(), e);
        }
        return id;
    }

    /**
     * Récupère toutes les notifications d'un utilisateur triées de la plus récente à la plus ancienne.
     */
    public List<AppNotification> getNotificationsForUser(long userId) {
        List<AppNotification> list = new ArrayList<>();
        if (userId <= 0) return list;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_NOTIFICATIONS, null,
                    DatabaseHelper.COLUMN_NOTIF_USER_ID + " = ?",
                    new String[]{ String.valueOf(userId) },
                    null, null,
                    DatabaseHelper.COLUMN_NOTIF_TIMESTAMP + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    AppNotification notif = cursorToNotification(cursor);
                    list.add(notif);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getNotificationsForUser: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Compte le nombre de notifications non lues pour un utilisateur.
     */
    public int getUnreadCount(long userId) {
        if (userId <= 0) return 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.query(DatabaseHelper.TABLE_NOTIFICATIONS,
                    new String[]{ "COUNT(*)" },
                    DatabaseHelper.COLUMN_NOTIF_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_NOTIF_IS_READ + " = 0",
                    new String[]{ String.valueOf(userId) },
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur getUnreadCount: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    /**
     * Marque une notification comme lue.
     */
    public boolean markAsRead(long notifId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NOTIF_IS_READ, 1);
        return db.update(DatabaseHelper.TABLE_NOTIFICATIONS, cv,
                DatabaseHelper.COLUMN_NOTIF_ID + " = ?",
                new String[]{ String.valueOf(notifId) }) > 0;
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues.
     */
    public boolean markAllAsReadForUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NOTIF_IS_READ, 1);
        return db.update(DatabaseHelper.TABLE_NOTIFICATIONS, cv,
                DatabaseHelper.COLUMN_NOTIF_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }) > 0;
    }

    /**
     * Transforme le Cursor en objet AppNotification.
     */
    private AppNotification cursorToNotification(Cursor cursor) {
        AppNotification notif = new AppNotification();
        notif.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_ID)));
        notif.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_USER_ID)));
        notif.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_TITLE)));
        notif.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_MESSAGE)));
        notif.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_TYPE)));
        notif.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_IS_READ)) == 1);
        notif.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_TIMESTAMP)));
        notif.setPeriodKey(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_PERIOD_KEY)));
        notif.setActionRoute(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIF_ACTION_ROUTE)));
        return notif;
    }
}

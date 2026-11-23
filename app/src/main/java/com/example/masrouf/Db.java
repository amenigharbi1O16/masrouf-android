package com.example.masrouf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.masrouf.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Db extends SQLiteOpenHelper {
    public static final String[] CATS = {"Alimentation", "Vêtements et chaussures", "Hygiène et soins personnels",
            "Maison et équipement", "Matériel informatique", "Santé et médicaments"};
    public static final String[] UNITS = {"Aucune", "kg", "g", "L", "mL", "Pièce"};

    public Db(Context c) { super(c, "masrouf.db", null, 11); }

    @Override public void onCreate(SQLiteDatabase d) {
        ensureColumns(d);
    }

    @Override public void onUpgrade(SQLiteDatabase d, int o, int n) {
        ensureColumns(d);
    }

    @Override public void onDowngrade(SQLiteDatabase d, int o, int n) {
        ensureColumns(d);
    }

    @Override public void onOpen(SQLiteDatabase d) {
        super.onOpen(d);
        ensureColumns(d);
    }

    private void ensureColumns(SQLiteDatabase d) {
        d.execSQL("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, password_hash TEXT, full_name TEXT, role TEXT, is_active INTEGER DEFAULT 1, is_temp_password INTEGER DEFAULT 0, created_at TEXT, family_id INTEGER DEFAULT 0, parent_id INTEGER DEFAULT 0, balance_millimes INTEGER DEFAULT 0)");
        d.execSQL("CREATE TABLE IF NOT EXISTS products(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, brand TEXT, category TEXT, photo TEXT)");
        d.execSQL("CREATE TABLE IF NOT EXISTS purchases(id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER, total REAL, qty REAL, unit TEXT, store TEXT, date TEXT)");
        try { d.execSQL("ALTER TABLE products ADD COLUMN category TEXT"); } catch (Exception ignored) { }
        try { d.execSQL("ALTER TABLE products ADD COLUMN photo TEXT"); } catch (Exception ignored) { }
    }

    // ---------- Produits ----------
    public long addProduct(String name, String brand, String cat, String photo) {
        ContentValues v = new ContentValues();
        v.put("name", name); v.put("brand", brand); v.put("category", cat); v.put("photo", photo);
        return getWritableDatabase().insert("products", null, v);
    }

    public void updateProduct(long id, String name, String brand, String cat, String photo) {
        ContentValues v = new ContentValues();
        v.put("name", name); v.put("brand", brand); v.put("category", cat);
        if (photo != null) v.put("photo", photo);
        getWritableDatabase().update("products", v, "id=?", new String[]{String.valueOf(id)});
    }

    private Product read(Cursor c) {
        Product p = new Product();
        p.id = c.getLong(0); p.name = c.getString(1); p.brand = c.getString(2); p.category = c.getString(3); p.photo = c.getString(4);
        return p;
    }

    private void fillLast(Product p) {
        List<Purchase> l = purchases(p.id);
        if (!l.isEmpty()) { Purchase last = l.get(l.size() - 1); p.lastUnitPrice = last.unitPrice(); p.lastQty = last.qty; }
    }

    public Product product(long id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id,name,brand,category,photo FROM products WHERE id=?", new String[]{String.valueOf(id)});
        try { if (!c.moveToFirst()) return null; Product p = read(c); fillLast(p); return p; } finally { c.close(); }
    }

    public List<Product> products(String q, String cat) {
        StringBuilder sql = new StringBuilder("SELECT id,name,brand,category,photo FROM products WHERE 1=1");
        List<String> args = new ArrayList<>();
        if (q != null && !q.trim().isEmpty()) { sql.append(" AND (name LIKE ? OR brand LIKE ?)"); args.add("%" + q.trim() + "%"); args.add("%" + q.trim() + "%"); }
        if (cat != null) { sql.append(" AND category=?"); args.add(cat); }
        sql.append(" ORDER BY name COLLATE NOCASE ASC");
        Cursor c = getReadableDatabase().rawQuery(sql.toString(), args.toArray(new String[0]));
        List<Product> out = new ArrayList<>();
        while (c.moveToNext()) out.add(read(c));
        c.close();
        for (Product p : out) fillLast(p);
        return out;
    }

    public int productCount() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM products", null);
        c.moveToFirst(); int n = c.getInt(0); c.close(); return n;
    }

    // ---------- Achats ----------
    private ContentValues pv(long pid, double total, double qty, String unit, String store, String date) {
        ContentValues v = new ContentValues();
        v.put("product_id", pid); v.put("total", total); v.put("qty", qty); v.put("unit", unit); v.put("store", store); v.put("date", date);
        return v;
    }

    public long addPurchase(long pid, double total, double qty, String unit, String store, String date) {
        return getWritableDatabase().insert("purchases", null, pv(pid, total, qty, unit, store, date));
    }

    public void updatePurchase(long id, long pid, double total, double qty, String unit, String store, String date) {
        getWritableDatabase().update("purchases", pv(pid, total, qty, unit, store, date), "id=?", new String[]{String.valueOf(id)});
    }

    public void deletePurchase(long id) { getWritableDatabase().delete("purchases", "id=?", new String[]{String.valueOf(id)}); }

    private Purchase readP(Cursor c) {
        Purchase p = new Purchase();
        p.id = c.getLong(0); p.productId = c.getLong(1); p.total = c.getDouble(2); p.qty = c.getDouble(3);
        p.unit = c.getString(4); p.store = c.getString(5); p.date = c.getString(6);
        return p;
    }

    public List<Purchase> purchases(long pid) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id,product_id,total,qty,unit,store,date FROM purchases WHERE product_id=? ORDER BY date ASC, id ASC",
                new String[]{String.valueOf(pid)});
        List<Purchase> l = new ArrayList<>();
        while (c.moveToNext()) l.add(readP(c));
        c.close(); return l;
    }

    // ---------- Statistiques ----------
    public double sum(LocalDate from, LocalDate to) {
        if (from == null || to == null) return 0;
        Cursor c = getReadableDatabase().rawQuery("SELECT COALESCE(SUM(total),0) FROM purchases WHERE date BETWEEN ? AND ?",
                new String[]{from.toString(), to.toString()});
        c.moveToFirst(); double v = c.getDouble(0); c.close(); return v;
    }

    public Map<String, Double> byCategory(LocalDate from, LocalDate to) {
        Map<String, Double> m = new LinkedHashMap<>();
        if (from == null || to == null) return m;
        Cursor c = getReadableDatabase().rawQuery("SELECT p.category, SUM(u.total) AS t FROM purchases u JOIN products p ON p.id=u.product_id " +
                "WHERE u.date BETWEEN ? AND ? GROUP BY p.category ORDER BY t ASC", new String[]{from.toString(), to.toString()});
        while (c.moveToNext()) m.put(c.getString(0), c.getDouble(1));
        c.close(); return m;
    }

    public void resetAll() {
        SQLiteDatabase d = getWritableDatabase();
        d.delete("purchases", null, null); d.delete("products", null, null);
    }
}

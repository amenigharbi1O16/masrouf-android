package com.example.masrouf;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;

public final class Settings {
    private static SharedPreferences sp(Context c) { return c.getSharedPreferences("budget", Context.MODE_PRIVATE); }

    public static LocalDate start(Context c) { String s = sp(c).getString("start", null); return s == null ? null : LocalDate.parse(s); }
    public static LocalDate end(Context c) { String s = sp(c).getString("end", null); return s == null ? null : LocalDate.parse(s); }
    public static double budget(Context c) { try { return Double.parseDouble(sp(c).getString("budget", "0")); } catch (Exception e) { return 0; } }
    public static boolean hasPeriod(Context c) { return start(c) != null && end(c) != null; }
    public static boolean hasBudget(Context c) { return budget(c) > 0; }
    public static boolean ready(Context c) { return hasPeriod(c) && hasBudget(c); }

    public static void setPeriod(Context c, LocalDate s, LocalDate e) {
        sp(c).edit().putString("start", s.toString()).putString("end", e.toString()).apply();
    }
    public static void setBudget(Context c, double b) { sp(c).edit().putString("budget", String.valueOf(b)).apply(); }
    public static void clear(Context c) { sp(c).edit().clear().apply(); }
}

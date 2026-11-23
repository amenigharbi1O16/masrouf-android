package com.example.masrouf;

import java.time.LocalDate;
import java.util.Locale;

public final class Fmt {
    public static final String[] MU = {"JANV.","FÉVR.","MARS","AVR.","MAI","JUIN","JUIL.","AOÛT","SEPT.","OCT.","NOV.","DÉC."};
    public static final String[] ML = {"janv.","févr.","mars","avr.","mai","juin","juil.","août","sept.","oct.","nov.","déc."};
    public static final String[] MS = {"Janv","Févr","Mars","Avr","Mai","Juin","Juil","Août","Sept","Oct","Nov","Déc"};

    public static String money(double v) { return String.format(Locale.US, "%,.3f DT", v); }
    public static String signed(double v) { return String.format(Locale.US, "%s%.3f DT", v >= 0 ? "+" : "-", Math.abs(v)); }
    public static String pct(double v) { return String.format(Locale.US, "%.1f%%", v); }
    public static String spct(double v) { return String.format(Locale.US, "%+.1f%%", v); }
    public static String qty(double q) { return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q); }
    public static String shortNum(double v) { return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.US, "%.1f", v); }

    public static String period(LocalDate s, LocalDate e) {
        String ds = String.format(Locale.US, "%02d", s.getDayOfMonth());
        String de = String.format(Locale.US, "%02d", e.getDayOfMonth());
        if (s.getMonth() == e.getMonth() && s.getYear() == e.getYear())
            return ds + "–" + de + " " + MU[e.getMonthValue() - 1] + " " + e.getYear();
        return ds + " " + MU[s.getMonthValue() - 1] + "–" + de + " " + MU[e.getMonthValue() - 1] + " " + e.getYear();
    }

    public static String day(LocalDate d) {
        return String.format(Locale.US, "%02d", d.getDayOfMonth()) + " " + ML[d.getMonthValue() - 1] + " " + d.getYear();
    }
}

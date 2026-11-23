package com.example.masrouf;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

public class Store {
    public static class Period {
        public String start, end;
        public double total;
        public Map<String, Double> alloc = new LinkedHashMap<>();
        public boolean announced, w80, over;
        public double pct(String k) { Double d = alloc.get(k); return d == null ? 0 : d; }
        public double amount(String k) { return total * pct(k) / 100.0; }
        public double allocated() { double s = 0; for (double d : alloc.values()) s += d; return s; }
    }

    public static class Tx {
        public String type, cat, date, label = "", store = "", unit = "";
        public double amount, qty = 1;
        public boolean purchase() { return "purchase".equals(type); }
    }

    public static class Notif { public String title, body, action, time; public boolean read; }

    private static Store I;
    private final SharedPreferences sp;
    public boolean onboarded;
    public String lang = "fr", name = "Utilisateur", email = "";
    public final List<String> cats = new ArrayList<>();
    public final List<Period> periods = new ArrayList<>();
    public final List<Tx> txs = new ArrayList<>();
    public final List<Notif> notifs = new ArrayList<>();

    public static Store get(Context c) { if (I == null) I = new Store(c.getApplicationContext()); return I; }

    private Store(Context c) {
        sp = c.getSharedPreferences("masrouf", Context.MODE_PRIVATE);
        try {
            JSONObject o = new JSONObject(sp.getString("d", "{}"));
            onboarded = o.optBoolean("onb");
            lang = o.optString("lang", "fr");
            name = o.optString("name", name);
            email = o.optString("email", "");
            JSONArray a = o.optJSONArray("cats");
            if (a != null) for (int i = 0; i < a.length(); i++) cats.add(a.getString(i));
            a = o.optJSONArray("periods");
            if (a != null) for (int i = 0; i < a.length(); i++) {
                JSONObject j = a.getJSONObject(i);
                Period p = new Period();
                p.start = j.getString("s"); p.end = j.getString("e"); p.total = j.getDouble("t");
                p.announced = j.optBoolean("an"); p.w80 = j.optBoolean("w"); p.over = j.optBoolean("o");
                JSONObject al = j.getJSONObject("a");
                Iterator<String> it = al.keys();
                while (it.hasNext()) { String k = it.next(); p.alloc.put(k, al.getDouble(k)); }
                periods.add(p);
            }
            a = o.optJSONArray("tx");
            if (a != null) for (int i = 0; i < a.length(); i++) {
                JSONObject j = a.getJSONObject(i);
                Tx t = new Tx();
                t.type = j.getString("ty"); t.cat = j.getString("c"); t.date = j.getString("d");
                t.label = j.optString("l"); t.store = j.optString("s"); t.unit = j.optString("u");
                t.amount = j.getDouble("a"); t.qty = j.optDouble("q", 1);
                txs.add(t);
            }
            a = o.optJSONArray("notifs");
            if (a != null) for (int i = 0; i < a.length(); i++) {
                JSONObject j = a.getJSONObject(i);
                Notif n = new Notif();
                n.title = j.optString("t"); n.body = j.optString("b"); n.action = j.optString("a");
                n.time = j.optString("tm"); n.read = j.optBoolean("r");
                notifs.add(n);
            }
        } catch (Exception ignored) { }
    }

    public void save() {
        try {
            JSONObject o = new JSONObject();
            o.put("onb", onboarded); o.put("lang", lang); o.put("name", name); o.put("email", email);
            o.put("cats", new JSONArray(cats));
            JSONArray ps = new JSONArray();
            for (Period p : periods) {
                JSONObject j = new JSONObject();
                j.put("s", p.start); j.put("e", p.end); j.put("t", p.total); j.put("a", new JSONObject(p.alloc));
                j.put("an", p.announced); j.put("w", p.w80); j.put("o", p.over);
                ps.put(j);
            }
            o.put("periods", ps);
            JSONArray ts = new JSONArray();
            for (Tx t : txs) {
                JSONObject j = new JSONObject();
                j.put("ty", t.type); j.put("c", t.cat); j.put("d", t.date); j.put("l", t.label);
                j.put("s", t.store); j.put("u", t.unit); j.put("a", t.amount); j.put("q", t.qty);
                ts.put(j);
            }
            o.put("tx", ts);
            JSONArray ns = new JSONArray();
            for (Notif n : notifs) {
                JSONObject j = new JSONObject();
                j.put("t", n.title); j.put("b", n.body); j.put("a", n.action); j.put("tm", n.time); j.put("r", n.read);
                ns.put(j);
            }
            o.put("notifs", ns);
            sp.edit().putString("d", o.toString()).apply();
        } catch (Exception ignored) { }
    }

    public Period periodFor(String date) {
        for (Period p : periods) if (date.compareTo(p.start) >= 0 && date.compareTo(p.end) <= 0) return p;
        return null;
    }
    public Period current() { return periodFor(Ui.today()); }

    public double spent(Period p) {
        double s = 0;
        for (Tx t : txs) if (t.date.compareTo(p.start) >= 0 && t.date.compareTo(p.end) <= 0) s += t.amount;
        return s;
    }
    public double spentCat(Period p, String cat) {
        double s = 0;
        for (Tx t : txs) if (t.cat.equals(cat) && t.date.compareTo(p.start) >= 0 && t.date.compareTo(p.end) <= 0) s += t.amount;
        return s;
    }

    public void normalize(Period p) {
        double sum = 0;
        for (String k : cats) sum += Cat.def(k);
        p.alloc.clear();
        for (String k : cats) p.alloc.put(k, sum == 0 ? 0 : Cat.def(k) * 100.0 / sum);
    }

    public String addTx(Tx t, double pctIfNew) {
        Period p = periodFor(t.date);
        if (p == null) return "Aucune période avec budget ne couvre cette date. Définissez d'abord un budget.";
        if (t.amount <= 0) return "Montant invalide.";
        if (!cats.contains(t.cat)) cats.add(t.cat);
        if (!p.alloc.containsKey(t.cat) && pctIfNew > 0 && p.allocated() + pctIfNew <= 100.0001) p.alloc.put(t.cat, pctIfNew);
        txs.add(t);
        double sp = spent(p);
        if (sp > p.total && !p.over) {
            p.over = true;
            addNotif("Budget dépassé", "Vos dépenses dépassent le budget de la période " + Ui.range(p.start, p.end) + ".", "Révisez vos catégories ou ajustez le budget.");
        } else if (sp >= 0.8 * p.total && !p.w80) {
            p.w80 = true;
            addNotif("80 % du budget consommé", "Il vous reste " + Ui.f3(p.total - sp) + " DT pour cette période.", "Ralentissez vos dépenses non essentielles.");
        }
        save();
        return null;
    }

    public void addNotif(String title, String body, String action) {
        Notif n = new Notif();
        n.title = title; n.body = body; n.action = action; n.read = false;
        n.time = new SimpleDateFormat("d MMM, hh:mm a", Locale.FRANCE).format(new Date());
        notifs.add(n);
        save();
    }
    public int unread() { int c = 0; for (Notif n : notifs) if (!n.read) c++; return c; }
    public void markAllRead() { for (Notif n : notifs) n.read = true; save(); }

    public List<Period> donePeriods() {
        List<Period> l = new ArrayList<>();
        for (Period p : periods) if (p.end.compareTo(Ui.today()) < 0) l.add(p);
        Collections.sort(l, (a, b) -> a.start.compareTo(b.start));
        return l;
    }

    public int[] points() {
        int pts = 0, streak = 0, best = 0;
        for (Period p : donePeriods()) {
            if (spent(p) <= p.total) {
                pts += 100; streak++; best = Math.max(best, streak);
                if (streak % 3 == 0) pts += 50;
            } else streak = 0;
        }
        return new int[]{pts, streak, best};
    }
}

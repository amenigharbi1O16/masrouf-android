package com.example.masrouf;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class Ui {
    private Ui() { }

    public static final int MW = ViewGroup.LayoutParams.MATCH_PARENT, WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    public static final int V = LinearLayout.VERTICAL, H = LinearLayout.HORIZONTAL;
    public static final String[] MONTHS = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
    public static final String[] MON = {"JANV.", "FÉVR.", "MARS", "AVR.", "MAI", "JUIN", "JUIL.", "AOÛT", "SEPT.", "OCT.", "NOV.", "DÉC."};

    public static int dp(Context c, float v) { return Math.round(v * c.getResources().getDisplayMetrics().density); }

    public static double num(EditText e) {
        try { return Double.parseDouble(e.getText().toString().trim().replace(',', '.')); } catch (Exception ex) { return Double.NaN; }
    }

    public static String today() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()); }
    public static String f3(double v) { return new DecimalFormat("#,##0.000", DecimalFormatSymbols.getInstance(Locale.US)).format(v); }
    public static String f0(double v) { return Math.abs(v) < 0.0005 ? "0" : f3(v); }
    public static int[] ymd(String s) { return new int[]{Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(5, 7)), Integer.parseInt(s.substring(8, 10))}; }
    public static int lastDay(int y, int m0) { Calendar c = Calendar.getInstance(); c.set(y, m0, 1); return c.getActualMaximum(Calendar.DAY_OF_MONTH); }
    public static String range(String s, String e) {
        int[] a = ymd(s), b = ymd(e);
        if (a[0] == b[0] && a[1] == b[1]) return String.format(Locale.US, "%02d–%02d %s %d", a[2], b[2], MON[a[1] - 1], a[0]);
        return String.format(Locale.US, "%02d %s–%02d %s %d", a[2], MON[a[1] - 1], b[2], MON[b[1] - 1], b[0]);
    }
    public static long days(String s, String e) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return Math.round((f.parse(e).getTime() - f.parse(s).getTime()) / 86400000.0) + 1;
        } catch (Exception x) { return 0; }
    }
    public static double num(String s) { try { return Double.parseDouble(s.trim().replace(',', '.')); } catch (Exception e) { return 0; } }

    public static TextView tv(Context c, String t, float sp, int color, boolean bold) {
        TextView v = new TextView(c);
        v.setText(t); v.setTextSize(sp); v.setTextColor(c.getColor(color));
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        return v;
    }
    public static LinearLayout box(Context c, int bg, int padDp, int orient) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(orient);
        if (bg != 0) l.setBackgroundResource(bg);
        int p = dp(c, padDp);
        l.setPadding(p, p, p, p);
        return l;
    }
    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(H); l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }
    public static void add(ViewGroup p, View v, int topDp) {
        ViewGroup.LayoutParams old = v.getLayoutParams();
        int h = old != null && old.height > 0 ? old.height : WC;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MW, h);
        lp.topMargin = dp(p.getContext(), topDp);
        p.addView(v, lp);
    }
    public static void weight(LinearLayout p, View v, float w, int startDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, WC, w);
        lp.setMarginStart(dp(p.getContext(), startDp));
        p.addView(v, lp);
    }
    public static LinearLayout lr(Context c, View l, View r) {
        LinearLayout row = row(c);
        row.addView(l, new LinearLayout.LayoutParams(0, WC, 1));
        row.addView(r, new LinearLayout.LayoutParams(WC, WC));
        return row;
    }
    public static View divider(Context c) {
        View v = new View(c);
        v.setBackgroundColor(c.getColor(R.color.line));
        return v;
    }
    public static void addDivider(ViewGroup p, int topDp) {
        View d = divider(p.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MW, dp(p.getContext(), 1));
        lp.topMargin = dp(p.getContext(), topDp); lp.bottomMargin = dp(p.getContext(), topDp);
        p.addView(d, lp);
    }

    public static AppCompatButton btn(Context c, String t, int bg, int txtColor) {
        AppCompatButton b = new AppCompatButton(c);
        b.setText(t); b.setAllCaps(false); b.setTextSize(15); b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(c.getColor(txtColor)); b.setBackgroundResource(bg); b.setStateListAnimator(null);
        b.setMinHeight(dp(c, 52)); b.setMinimumHeight(dp(c, 52));
        return b;
    }
    public static AppCompatButton primary(Context c, String t) { return btn(c, t, R.drawable.bg_btn_primary, R.color.white); }
    public static AppCompatButton mint(Context c, String t) { return btn(c, t, R.drawable.bg_btn_mint, R.color.green); }

    public static TextView icon(Context c, String emoji, int sizeDp) {
        TextView t = new TextView(c);
        t.setText(emoji); t.setGravity(Gravity.CENTER); t.setTextSize(sizeDp * 0.42f);
        t.setBackgroundResource(R.drawable.bg_icon);
        t.setLayoutParams(new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp)));
        return t;
    }
    public static TextView chip(Context c, String t, boolean on) {
        TextView v = tv(c, t, 12, on ? R.color.dark : R.color.ink, on);
        v.setBackgroundResource(on ? R.drawable.bg_chip_on : R.drawable.bg_chip);
        v.setPadding(dp(c, 12), dp(c, 8), dp(c, 12), dp(c, 8));
        return v;
    }
    public static LinearLayout flex(Context c) {
        LinearLayout f = new LinearLayout(c);
        f.setOrientation(H);
        return f;
    }
    public static void addChip(ViewGroup f, View chip) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WC, WC);
        lp.setMargins(0, 0, dp(f.getContext(), 8), dp(f.getContext(), 8));
        f.addView(chip, lp);
    }

    public static ProgressBar bar(Context c, boolean dark, int pct) {
        ProgressBar p = new ProgressBar(c, null, android.R.attr.progressBarStyleHorizontal);
        p.setIndeterminate(false);
        p.setProgressDrawable(c.getDrawable(dark ? R.drawable.progress_dark : R.drawable.progress_light));
        p.setMax(100); p.setProgress(Math.max(0, Math.min(100, pct)));
        p.setLayoutParams(new LinearLayout.LayoutParams(MW, dp(c, 6)));
        return p;
    }

    public static LinearLayout banner(Context c, int kind, String title, String body) {
        LinearLayout r = box(c, kind == 1 ? R.drawable.bg_warn : R.drawable.bg_info, 14, H);
        int tc = kind == 1 ? R.color.warn : R.color.green;
        r.addView(tv(c, kind == 1 ? "⚠" : kind == 2 ? "✔" : "ⓘ", 16, tc, true));
        LinearLayout col = new LinearLayout(c);
        col.setOrientation(V); col.setPadding(dp(c, 10), 0, 0, 0);
        TextView t = tv(c, title, 13, tc, true); t.setTag("t"); col.addView(t);
        if (body != null) { TextView b = tv(c, body, 11, tc, false); b.setTag("b"); col.addView(b); }
        r.addView(col, new LinearLayout.LayoutParams(0, WC, 1));
        return r;
    }
    public static LinearLayout empty(Context c, String emoji, String title, String body) {
        LinearLayout card = box(c, R.drawable.bg_card, 20, V);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView ic = icon(c, emoji, 52); ic.setBackgroundResource(R.drawable.bg_oval_circle);
        card.addView(ic);
        TextView t = tv(c, title, 15, R.color.ink, true); t.setGravity(Gravity.CENTER);
        add(card, t, 12);
        TextView b = tv(c, body, 12, R.color.muted, false); b.setGravity(Gravity.CENTER);
        add(card, b, 6);
        return card;
    }

    public static LinearLayout fieldBox(Context c) {
        LinearLayout box = box(c, R.drawable.bg_input, 0, V);
        box.setPadding(dp(c, 16), dp(c, 10), dp(c, 16), dp(c, 10));
        return box;
    }
    public static EditText field(Context c, LinearLayout parent, String label, String hint, int type, String text) {
        LinearLayout box = fieldBox(c);
        box.addView(tv(c, label, 11, R.color.muted, false));
        EditText e = new EditText(c);
        e.setBackground(null); e.setPadding(0, dp(c, 4), 0, 0); e.setSingleLine(true);
        e.setHint(hint); e.setHintTextColor(c.getColor(R.color.hint)); e.setTextColor(c.getColor(R.color.ink));
        e.setTextSize(16); e.setInputType(type); if (text != null) e.setText(text);
        box.addView(e, new LinearLayout.LayoutParams(MW, WC));
        add(parent, box, 12);
        return e;
    }
    public static final int NUM = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    public static final int TXT = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;

    public static EditText dateField(Context c, LinearLayout parent, String label, String init, Runnable onChange) {
        EditText e = field(c, parent, label, "AAAA-MM-JJ", InputType.TYPE_NULL, init);
        e.setFocusable(false); e.setClickable(true);
        e.setOnClickListener(v -> {
            Calendar k = Calendar.getInstance();
            try { int[] a = ymd(e.getText().toString()); k.set(a[0], a[1] - 1, a[2]); } catch (Exception ignored) { }
            new DatePickerDialog(c, (dp, y, m, d) -> {
                e.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d));
                if (onChange != null) onChange.run();
            }, k.get(Calendar.YEAR), k.get(Calendar.MONTH), k.get(Calendar.DAY_OF_MONTH)).show();
        });
        return e;
    }
    public static Spinner spinner(Context c, LinearLayout parent, String label, List<String> items) {
        LinearLayout box = fieldBox(c);
        box.addView(tv(c, label, 11, R.color.muted, false));
        Spinner s = new Spinner(c);
        s.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, items));
        s.setPadding(0, 0, 0, 0);
        box.addView(s, new LinearLayout.LayoutParams(MW, WC));
        add(parent, box, 12);
        return s;
    }

    public static LinearLayout modal(Context c, Dialog d, String title, String sub) {
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(V); root.setBackgroundColor(c.getColor(R.color.white));
        LinearLayout head = row(c);
        head.setPadding(dp(c, 16), dp(c, 14), dp(c, 12), dp(c, 10));
        LinearLayout tx = new LinearLayout(c);
        tx.setOrientation(V);
        tx.addView(tv(c, title, 18, R.color.ink, true));
        tx.addView(tv(c, sub, 11, R.color.muted, false));
        head.addView(tx, new LinearLayout.LayoutParams(0, WC, 1));
        ImageView x = new ImageView(c);
        x.setImageResource(R.drawable.ic_close); x.setPadding(dp(c, 8), dp(c, 8), dp(c, 8), dp(c, 8));
        x.setOnClickListener(v -> d.dismiss());
        head.addView(x, new LinearLayout.LayoutParams(dp(c, 40), dp(c, 40)));
        root.addView(head);
        root.addView(divider(c), new LinearLayout.LayoutParams(MW, dp(c, 1)));
        ScrollView sv = new ScrollView(c);
        sv.setFillViewport(true);
        LinearLayout content = box(c, 0, 14, V);
        sv.addView(content, new ViewGroup.LayoutParams(MW, WC));
        root.addView(sv, new LinearLayout.LayoutParams(MW, 0, 1));
        d.setContentView(root);
        return content;
    }
    public static void showFull(Dialog d) {
        d.show();
        if (d.getWindow() != null) {
            d.getWindow().setLayout(MW, MW);
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }
}

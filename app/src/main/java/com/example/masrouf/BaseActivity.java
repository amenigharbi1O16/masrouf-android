package com.example.masrouf;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.masrouf.controller.SessionManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {
    protected Db db;
    private static final String[] CODES = {"en", "fr", "ar"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Db(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        View root = findViewById(android.R.id.content);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBell();
        refresh();
    }

    protected void refresh() { }

    protected int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    protected String currentLang() {
        LocaleListCompat l = AppCompatDelegate.getApplicationLocales();
        if (!l.isEmpty() && l.get(0) != null) return l.get(0).getLanguage();
        return "fr";
    }

    protected void changeLanguage(String code) {
        if (code.equals(currentLang())) return;
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code));
    }

    protected void setupLangPill() {
        int[] ids = {R.id.tv_lang_en, R.id.tv_lang_fr, R.id.tv_lang_ar};
        String lang = currentLang();
        for (int i = 0; i < ids.length; i++) {
            TextView tv = findViewById(ids[i]);
            if (tv == null) return;
            boolean sel = CODES[i].equals(lang);
            tv.setBackgroundResource(sel ? R.drawable.bg_lang_pill_selected : 0);
            tv.setTextColor(getColor(sel ? R.color.dark : R.color.muted));
            tv.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
            final String code = CODES[i];
            tv.setOnClickListener(v -> changeLanguage(code));
        }
    }

    protected void setupLangChips() {
        int[] ids = {R.id.chip_en, R.id.chip_fr, R.id.chip_ar};
        String lang = currentLang();
        for (int i = 0; i < ids.length; i++) {
            TextView tv = findViewById(ids[i]);
            if (tv == null) return;
            boolean sel = CODES[i].equals(lang);
            tv.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
            tv.setTextColor(getColor(sel ? R.color.white : R.color.dark));
            final String code = CODES[i];
            tv.setOnClickListener(v -> changeLanguage(code));
        }
    }

    protected void setupLoginLink() {
        View link = findViewById(R.id.tv_go_login);
        if (link != null) {
            link.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }
    }

    // ---------- Header & Menu (SOURCE A) ----------
    protected void header(String title, boolean back) {
        TextView tvTitle = findViewById(R.id.headerTitle);
        if (tvTitle != null) tvTitle.setText(title);

        ImageView nav = findViewById(R.id.btnNav);
        if (nav != null) {
            nav.setImageResource(back ? R.drawable.ic_back : R.drawable.ic_menu);
            nav.setOnClickListener(v -> { if (back) finish(); else showMenu(v); });
        }

        View btnBell = findViewById(R.id.btnBell);
        if (btnBell != null) btnBell.setOnClickListener(v -> showNotifs());

        View btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile != null) btnProfile.setOnClickListener(v -> showProfile());
    }

    private void showMenu(View anchor) {
        PopupMenu pm = new PopupMenu(this, anchor);
        pm.getMenu().add(0, 1, 0, "Accueil");
        pm.getMenu().add(0, 2, 1, "Produits");
        pm.getMenu().add(0, 3, 2, "Analyses");
        pm.getMenu().add(0, 4, 3, "Déconnexion");
        pm.setOnMenuItemClickListener(i -> {
            int id = i.getItemId();
            if (id == 1 && !(this instanceof MainActivity)) {
                startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            } else if (id == 2 && !(this instanceof ProductsActivity)) {
                startActivity(new Intent(this, ProductsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            } else if (id == 3 && !(this instanceof AnalysesActivity)) {
                startActivity(new Intent(this, AnalysesActivity.class));
            } else if (id == 4) {
                new SessionManager(this).clearSession();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            return true;
        });
        pm.show();
    }

    private List<String> notifications() {
        List<String> n = new ArrayList<>();
        if (!Settings.hasPeriod(this)) n.add("Aucune période sélectionnée. Choisissez-en une pour suivre votre budget.");
        if (!Settings.hasBudget(this)) n.add("Aucun budget défini pour la période.");
        if (Settings.ready(this)) {
            double c = db.sum(Settings.start(this), Settings.end(this)), b = Settings.budget(this);
            if (b > 0 && c >= 0.8 * b) n.add(String.format(Locale.US, "%.0f%% de votre budget est déjà consommé.", c / b * 100));
        }
        if (db.productCount() == 0) n.add("Ajoutez votre premier produit pour suivre ses prix.");
        return n;
    }

    protected void refreshBell() {
        TextView badge = findViewById(R.id.bellBadge);
        if (badge == null) return;
        int n = notifications().size();
        badge.setVisibility(n > 0 ? View.VISIBLE : View.GONE);
        badge.setText(String.valueOf(n));
    }

    private void showNotifs() {
        List<String> l = notifications();
        new AlertDialog.Builder(this).setTitle("Notifications")
                .setMessage(l.isEmpty() ? "Rien à signaler." : "• " + String.join("\n\n• ", l))
                .setPositiveButton("OK", null).show();
    }

    private void showProfile() {
        String per = Settings.hasPeriod(this) ? Fmt.period(Settings.start(this), Settings.end(this)) : "non définie";
        String bud = Settings.hasBudget(this) ? Fmt.money(Settings.budget(this)) : "non défini";
        new AlertDialog.Builder(this).setTitle("Profil")
                .setMessage("Période : " + per + "\nBudget : " + bud)
                .setItems(new String[]{"Choisir la période", "Définir le budget", "Réinitialiser les données"}, (d, which) -> {
                    if (which == 0) pickPeriod(this::afterSetup);
                    else if (which == 1) editBudget(this::afterSetup);
                    else new AlertDialog.Builder(this).setMessage("Supprimer produits, achats, période et budget ?")
                            .setPositiveButton("Supprimer", (x, y) -> { db.resetAll(); Settings.clear(this); afterSetup(); })
                            .setNegativeButton("Annuler", null).show();
                }).setNegativeButton("Fermer", null).show();
    }

    private void afterSetup() { refreshBell(); refresh(); }

    protected void pickPeriod(Runnable done) {
        LocalDate base = Settings.start(this) != null ? Settings.start(this) : LocalDate.now().withDayOfMonth(1);
        DatePickerDialog d1 = new DatePickerDialog(this, (v1, y1, m1, dd1) -> {
            LocalDate s = LocalDate.of(y1, m1 + 1, dd1);
            LocalDate defEnd = s.withDayOfMonth(s.lengthOfMonth());
            DatePickerDialog d2 = new DatePickerDialog(this, (v2, y2, m2, dd2) -> {
                Settings.setPeriod(this, s, LocalDate.of(y2, m2 + 1, dd2));
                if (done != null) done.run();
            }, defEnd.getYear(), defEnd.getMonthValue() - 1, defEnd.getDayOfMonth());
            d2.setTitle("Fin de la période");
            d2.getDatePicker().setMinDate(s.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            d2.show();
        }, base.getYear(), base.getMonthValue() - 1, base.getDayOfMonth());
        d1.setTitle("Début de la période");
        d1.show();
    }

    protected void editBudget(Runnable done) {
        EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        et.setHint("Ex. 1456.000");
        if (Settings.hasBudget(this)) et.setText(String.valueOf(Settings.budget(this)));
        int p = Ui.dp(this, 20);
        et.setPadding(p, p, p, p);
        new AlertDialog.Builder(this).setTitle("Budget de la période (DT)").setView(et)
                .setPositiveButton("Enregistrer", (d, w) -> {
                    double v = Ui.num(et);
                    if (Double.isNaN(v) || v <= 0) { Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show(); return; }
                    Settings.setBudget(this, v);
                    if (done != null) done.run();
                }).setNegativeButton("Annuler", null).show();
    }

    protected void ensureSetup(Runnable ok) {
        if (!Settings.hasPeriod(this)) {
            Toast.makeText(this, "Choisissez d'abord la période.", Toast.LENGTH_SHORT).show();
            pickPeriod(() -> ensureSetup(ok));
        } else if (!Settings.hasBudget(this)) {
            Toast.makeText(this, "Définissez d'abord le budget.", Toast.LENGTH_SHORT).show();
            editBudget(() -> ensureSetup(ok));
        } else ok.run();
    }
}

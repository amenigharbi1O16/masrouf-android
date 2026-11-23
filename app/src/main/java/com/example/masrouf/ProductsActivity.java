package com.example.masrouf;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductsActivity extends BaseActivity {
    private String query = "", cat = null;
    private boolean asc = true;
    private LinearLayout list, chips;
    private TextView count, sort, empty, periodText, periodChange;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_products);
        header("Produits", false);
        list = findViewById(R.id.list); chips = findViewById(R.id.chips);
        count = findViewById(R.id.count); sort = findViewById(R.id.sort); empty = findViewById(R.id.empty);
        periodText = findViewById(R.id.periodText); periodChange = findViewById(R.id.periodChange);
        findViewById(R.id.periodBanner).setOnClickListener(v -> pickPeriod(this::refresh));
        ((EditText) findViewById(R.id.search)).addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { query = s.toString(); refresh(); }
            public void afterTextChanged(Editable s) { }
        });
        sort.setOnClickListener(v -> { asc = !asc; refresh(); });
        findViewById(R.id.btnAdd).setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));
    }

    @Override protected void refresh() {
        if (Settings.hasPeriod(this)) {
            periodText.setText("Période active : " + Fmt.period(Settings.start(this), Settings.end(this)));
            periodChange.setText("Changer ›");
        } else {
            periodText.setText("Aucune période active");
            periodChange.setText("Choisir ›");
        }
        buildChips();
        List<Product> ps = db.products(query, cat);
        if (!asc) Collections.reverse(ps);
        sort.setText(asc ? "Nom A–Z" : "Nom Z–A");
        count.setText(ps.size() + (ps.size() > 1 ? " Produits" : " Produit"));
        list.removeAllViews();
        empty.setVisibility(ps.isEmpty() ? View.VISIBLE : View.GONE);
        for (Product p : ps) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_product, list, false);
            ((TextView) row.findViewById(R.id.tvTitle)).setText(p.name);
            ((TextView) row.findViewById(R.id.tvSub)).setText(p.category + (p.lastUnitPrice >= 0 ? " · " + Fmt.money(p.lastUnitPrice) : ""));
            row.setOnClickListener(v -> startActivity(new Intent(this, ProductDetailActivity.class).putExtra("id", p.id)));
            list.addView(row);
        }
    }

    private void buildChips() {
        chips.removeAllViews();
        List<String> names = new ArrayList<>();
        names.add("Tous les produits");
        Collections.addAll(names, Db.CATS);
        for (int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            boolean sel = (i == 0 && cat == null) || n.equals(cat);
            if (i > 0) chips.addView(label(" · ", false));
            TextView t = label(n, sel);
            final String target = i == 0 ? null : n;
            t.setOnClickListener(v -> { cat = target; refresh(); });
            chips.addView(t);
        }
    }

    private TextView label(String s, boolean selected) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(14);
        t.setTextColor(getColor(selected ? R.color.ink : R.color.muted));
        t.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        t.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return t;
    }
}

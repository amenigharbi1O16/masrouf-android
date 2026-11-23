package com.example.masrouf;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends BaseActivity {
    private long id;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_product_detail);
        id = getIntent().getLongExtra("id", -1);
        findViewById(R.id.btnHistory).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class).putExtra("id", id)));
        findViewById(R.id.btnBuy).setOnClickListener(v -> startActivity(new Intent(this, PurchaseFormActivity.class).putExtra("productId", id)));
        findViewById(R.id.btnEdit).setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class).putExtra("id", id)));
    }

    @Override protected void refresh() {
        Product p = db.product(id);
        if (p == null) { finish(); return; }
        header(p.name, true);
        List<Purchase> ps = db.purchases(id);
        ((TextView) findViewById(R.id.tvName)).setText(p.name.toUpperCase(Locale.ROOT));
        ((TextView) findViewById(R.id.tvBrand)).setText(p.brand == null || p.brand.isEmpty() ? p.name : p.brand);
        ((TextView) findViewById(R.id.tvMeta)).setText(p.category + "  ·  " + Fmt.qty(p.lastQty));
        TextView title = findViewById(R.id.tvCmpTitle), val = findViewById(R.id.tvCmpValue);
        if (ps.isEmpty()) {
            ((TextView) findViewById(R.id.tvLastPrice)).setText("—");
            ((TextView) findViewById(R.id.tvLastDate)).setText("—");
            ((TextView) findViewById(R.id.tvStore)).setText("—");
            title.setText("Aucun achat enregistré"); val.setText("—");
            ((LineChartView) findViewById(R.id.chart)).setData(new ArrayList<>(), "", "");
            return;
        }
        Purchase last = ps.get(ps.size() - 1);
        ((TextView) findViewById(R.id.tvLastPrice)).setText(Fmt.money(last.unitPrice()));
        ((TextView) findViewById(R.id.tvLastDate)).setText(Fmt.day(LocalDate.parse(last.date)));
        ((TextView) findViewById(R.id.tvStore)).setText(last.store == null || last.store.isEmpty() ? "—" : last.store);
        if (ps.size() < 2) {
            title.setText("Premier achat enregistré"); val.setText("—");
        } else {
            double prev = ps.get(ps.size() - 2).unitPrice(), diff = last.unitPrice() - prev;
            double pc = prev == 0 ? 0 : diff / prev * 100;
            title.setText(Math.abs(diff) < 0.0005 ? "Prix identique au dernier achat"
                    : diff > 0 ? "Prix en hausse par rapport à l'achat précédent" : "Prix en baisse par rapport à l'achat précédent");
            String arrow = Math.abs(diff) < 0.0005 ? "—" : diff > 0 ? "▲" : "▼";
            val.setText(arrow + "   " + Fmt.signed(diff) + "  /  " + Fmt.spct(pc));
        }
        List<Double> vals = new ArrayList<>();
        for (Purchase x : ps) vals.add(x.unitPrice());
        LocalDate f = LocalDate.parse(ps.get(0).date), l = LocalDate.parse(last.date);
        ((LineChartView) findViewById(R.id.chart)).setData(vals, Fmt.MS[f.getMonthValue() - 1] + ".", Fmt.MS[l.getMonthValue() - 1] + ".");
    }
}

package com.example.masrouf;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends BaseActivity {
    private long id;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_history);
        id = getIntent().getLongExtra("id", -1);
        header("Voir l'historique des achats", true);
    }

    @Override protected void refresh() {
        Product p = db.product(id);
        if (p == null) { finish(); return; }
        List<Purchase> ps = db.purchases(id);
        ((TextView) findViewById(R.id.tvCaption)).setText(p.name.toUpperCase(Locale.ROOT) + " · " + p.brand + " · " + Fmt.qty(p.lastQty));
        double total = 0; for (Purchase x : ps) total += x.total;
        ((TextView) findViewById(R.id.tvCount)).setText(ps.size() + (ps.size() > 1 ? " achats" : " achat"));
        ((TextView) findViewById(R.id.tvTotal)).setText(Fmt.money(total));
        LinearLayout list = findViewById(R.id.list);
        list.removeAllViews();
        findViewById(R.id.empty).setVisibility(ps.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.chartCard).setVisibility(ps.isEmpty() ? View.GONE : View.VISIBLE);
        findViewById(R.id.btnEditLast).setVisibility(ps.isEmpty() ? View.GONE : View.VISIBLE);
        List<Double> vals = new ArrayList<>();
        for (int i = ps.size() - 1; i >= 0; i--) {
            Purchase x = ps.get(i);
            View row = LayoutInflater.from(this).inflate(R.layout.item_purchase, list, false);
            ((TextView) row.findViewById(R.id.tvDate)).setText(Fmt.day(LocalDate.parse(x.date)));
            ((TextView) row.findViewById(R.id.tvStore)).setText((x.store == null || x.store.isEmpty() ? "Magasin" : x.store) + " · " + Fmt.qty(x.qty));
            ((TextView) row.findViewById(R.id.tvPrice)).setText(Fmt.money(x.total));
            row.setOnClickListener(v -> openEdit(x.id));
            list.addView(row);
        }
        for (Purchase x : ps) vals.add(x.unitPrice());
        if (!ps.isEmpty()) {
            LocalDate f = LocalDate.parse(ps.get(0).date), l = LocalDate.parse(ps.get(ps.size() - 1).date);
            ((LineChartView) findViewById(R.id.chart)).setData(vals, Fmt.MS[f.getMonthValue() - 1] + ".", Fmt.MS[l.getMonthValue() - 1] + ".");
            long lastId = ps.get(ps.size() - 1).id;
            findViewById(R.id.btnEditLast).setOnClickListener(v -> openEdit(lastId));
        }
    }

    private void openEdit(long purchaseId) {
        startActivity(new Intent(this, PurchaseFormActivity.class).putExtra("productId", id).putExtra("purchaseId", purchaseId));
    }
}

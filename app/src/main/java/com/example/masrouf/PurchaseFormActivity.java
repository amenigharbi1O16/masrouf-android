package com.example.masrouf;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class PurchaseFormActivity extends BaseActivity {
    private long productId, purchaseId = -1;
    private EditText etTotal, etQty, etStore;
    private Spinner spUnit;
    private TextView tvDate, tvUnitPrice;
    private LocalDate date = LocalDate.now();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_purchase_form);
        productId = getIntent().getLongExtra("productId", -1);
        purchaseId = getIntent().getLongExtra("purchaseId", -1);
        boolean edit = purchaseId >= 0;
        header(edit ? "Modifier l'achat" : "Achat de produit", true);
        ((TextView) findViewById(R.id.caption)).setText(edit ? "LA MODIFICATION AJUSTE LE BUDGET DE LA DIFFÉRENCE UNIQUEMENT." : "ENREGISTRER L'ACHAT D'UN PRODUIT");
        Product p = db.product(productId);
        if (p == null) { finish(); return; }
        ((TextView) findViewById(R.id.tvProduct)).setText(p.name + " · " + p.brand);
        etTotal = findViewById(R.id.etTotal); etQty = findViewById(R.id.etQty); etStore = findViewById(R.id.etStore);
        spUnit = findViewById(R.id.spUnit); tvDate = findViewById(R.id.tvDate); tvUnitPrice = findViewById(R.id.tvUnitPrice);
        ArrayAdapter<String> a = new ArrayAdapter<>(this, R.layout.spinner_item, Db.UNITS);
        a.setDropDownViewResource(R.layout.spinner_dropdown);
        spUnit.setAdapter(a);

        if (edit) {
            for (Purchase x : db.purchases(productId)) if (x.id == purchaseId) {
                etTotal.setText(Fmt.shortNum(x.total)); etQty.setText(Fmt.qty(x.qty));
                etStore.setText(x.store); date = LocalDate.parse(x.date);
                spUnit.setSelection(Math.max(0, Arrays.asList(Db.UNITS).indexOf(x.unit)));
            }
            View del = findViewById(R.id.btnDelete);
            del.setVisibility(View.VISIBLE);
            del.setOnClickListener(v -> new AlertDialog.Builder(this).setMessage("Supprimer cet achat ? Le budget sera ajusté.")
                    .setPositiveButton("Supprimer", (d, w) -> { db.deletePurchase(purchaseId); finish(); })
                    .setNegativeButton("Annuler", null).show());
        }
        tvDate.setText(date.toString());
        tvDate.setOnClickListener(v -> new DatePickerDialog(this, (dp, y, m, d) -> {
            date = LocalDate.of(y, m + 1, d); tvDate.setText(date.toString());
        }, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth()).show());

        TextWatcher w = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { updateUnit(); }
            public void afterTextChanged(Editable s) { }
        };
        etTotal.addTextChangedListener(w); etQty.addTextChangedListener(w);
        updateUnit();
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnCompare).setOnClickListener(v -> compare());
    }

    private void updateUnit() {
        double t = Ui.num(etTotal), q = Ui.num(etQty);
        tvUnitPrice.setText(Double.isNaN(t) || Double.isNaN(q) || q <= 0 ? "—" : Fmt.money(t / q));
    }

    private void compare() {
        double t = Ui.num(etTotal), q = Ui.num(etQty);
        if (Double.isNaN(t) || t <= 0) { etTotal.setError("Prix invalide"); return; }
        if (Double.isNaN(q) || q <= 0) { etQty.setError("Quantité invalide"); return; }
        double unit = t / q;
        List<Purchase> all = db.purchases(productId);
        Purchase prev = null;
        if (purchaseId >= 0) { for (int i = 0; i < all.size(); i++) if (all.get(i).id == purchaseId && i > 0) prev = all.get(i - 1); }
        else if (!all.isEmpty()) prev = all.get(all.size() - 1);
        String msg = "Prix unitaire : " + Fmt.money(unit);
        if (prev == null) msg += "\n\nAucun achat précédent à comparer.";
        else {
            double diff = unit - prev.unitPrice(), pc = prev.unitPrice() == 0 ? 0 : diff / prev.unitPrice() * 100;
            msg += "\nAchat précédent : " + Fmt.money(prev.unitPrice()) + "\nDifférence : " + Fmt.signed(diff) + " / " + Fmt.spct(pc);
        }
        new AlertDialog.Builder(this).setTitle("Comparaison des prix").setMessage(msg)
                .setPositiveButton("Enregistrer", (d, w) -> save(t, q))
                .setNegativeButton("Modifier", null).show();
    }

    private void save(double t, double q) {
        String unit = (String) spUnit.getSelectedItem(), store = etStore.getText().toString().trim();
        if (purchaseId >= 0) {
            db.updatePurchase(purchaseId, productId, t, q, unit, store, date.toString());
            Toast.makeText(this, "Achat modifié", Toast.LENGTH_SHORT).show();
            finish();
        } else ensureSetup(() -> {
            db.addPurchase(productId, t, q, unit, store, date.toString());
            Toast.makeText(this, "Achat enregistré", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

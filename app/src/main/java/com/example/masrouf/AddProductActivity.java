package com.example.masrouf;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.time.LocalDate;
import java.util.Arrays;

public class AddProductActivity extends BaseActivity {
    private long editId = -1;
    private EditText etName, etBrand, etPrice, etQty, etStore;
    private Spinner spCat, spUnit;
    private TextView tvDate, photoText;
    private String photoUri;
    private LocalDate date = LocalDate.now();

    private final ActivityResultLauncher<String> picker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) { photoUri = uri.toString(); photoText.setText("Photo ajoutée ✓"); }
    });

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_product);
        editId = getIntent().getLongExtra("id", -1);
        header(editId >= 0 ? "Modifier le produit" : "Ajouter un produit", editId >= 0);
        etName = findViewById(R.id.etName); etBrand = findViewById(R.id.etBrand); etPrice = findViewById(R.id.etPrice);
        etQty = findViewById(R.id.etQty); etStore = findViewById(R.id.etStore);
        spCat = findViewById(R.id.spCat); spUnit = findViewById(R.id.spUnit);
        tvDate = findViewById(R.id.tvDate); photoText = findViewById(R.id.photoText);
        adapt(spCat, Db.CATS); adapt(spUnit, Db.UNITS);
        tvDate.setText(date.toString());
        tvDate.setOnClickListener(v -> new DatePickerDialog(this, (dp, y, m, d) -> {
            date = LocalDate.of(y, m + 1, d); tvDate.setText(date.toString());
        }, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth()).show());
        findViewById(R.id.photoRow).setOnClickListener(v -> picker.launch("image/*"));
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        if (editId >= 0) {
            Product p = db.product(editId);
            if (p != null) {
                etName.setText(p.name); etBrand.setText(p.brand);
                spCat.setSelection(Math.max(0, Arrays.asList(Db.CATS).indexOf(p.category)));
            }
            ((TextView) findViewById(R.id.caption)).setText("Informations du produit");
            ((TextView) findViewById(R.id.heading)).setText("Modifier ce produit.");
            findViewById(R.id.purchaseGroup).setVisibility(View.GONE);
            findViewById(R.id.catsNote).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.btnSave)).setText("Enregistrer les modifications");
        }
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void adapt(Spinner s, String[] items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        a.setDropDownViewResource(R.layout.spinner_dropdown);
        s.setAdapter(a);
    }

    private void save() {
        String name = etName.getText().toString().trim(), brand = etBrand.getText().toString().trim();
        String cat = (String) spCat.getSelectedItem();
        if (name.isEmpty()) { etName.setError("Nom requis"); return; }
        if (editId >= 0) {
            db.updateProduct(editId, name, brand, cat, photoUri);
            Toast.makeText(this, "Produit modifié", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        double price = Ui.num(etPrice), qty = Ui.num(etQty);
        if (Double.isNaN(price) || price <= 0) { etPrice.setError("Prix invalide"); return; }
        if (Double.isNaN(qty) || qty <= 0) qty = 1;
        final double q = qty;
        String unit = (String) spUnit.getSelectedItem(), store = etStore.getText().toString().trim();
        ensureSetup(() -> {
            long pid = db.addProduct(name, brand, cat, photoUri);
            db.addPurchase(pid, price, q, unit, store, date.toString());
            Toast.makeText(this, "Produit et achat enregistrés", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

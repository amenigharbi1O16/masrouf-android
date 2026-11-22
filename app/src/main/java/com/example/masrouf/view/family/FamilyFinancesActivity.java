package com.example.masrouf.view.family;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.model.FamilyAllocation;
import com.example.masrouf.model.FamilyFunds;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;

import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Gestion des finances familiales et suivi des allocations distribuées.
 * COUCHE MVC : Vue (View).
 */
public class FamilyFinancesActivity extends AppCompatActivity {

    private ImageView ivBackFinances;
    private Button btnAdjustFunds;
    private Button btnTopUpReserve;
    private TextView tvFinTotal;
    private TextView tvFinAvailable;
    private TextView tvFinAllocated;
    private LinearLayout layoutAllocationsHistory;

    private FamilyDao familyDao;
    private SessionManager sessionManager;
    private long familyId = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (sessionManager.isChild()) {
            Toast.makeText(this, "Accès réservé au parent.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_family_finances);

        familyDao = new FamilyDao(this);
        sessionManager = new SessionManager(this);
        familyId = sessionManager.getFamilyId();

        ivBackFinances = findViewById(R.id.ivBackFinances);
        btnAdjustFunds = findViewById(R.id.btnAdjustFunds);
        btnTopUpReserve = findViewById(R.id.btnTopUpReserve);
        tvFinTotal = findViewById(R.id.tvFinTotal);
        tvFinAvailable = findViewById(R.id.tvFinAvailable);
        tvFinAllocated = findViewById(R.id.tvFinAllocated);
        layoutAllocationsHistory = findViewById(R.id.layoutAllocationsHistory);

        ivBackFinances.setOnClickListener(v -> finish());
        btnAdjustFunds.setOnClickListener(v -> showAdjustFundsDialog());
        if (btnTopUpReserve != null) {
            btnTopUpReserve.setOnClickListener(v -> showTopUpReserveDialog());
        }

        loadFinances();
    }

    private void loadFinances() {
        FamilyFunds funds = familyDao.getFamilyFunds(familyId);
        if (funds != null) {
            tvFinTotal.setText(MillimesUtil.formatMillimes(funds.getTotalFundsMillimes()));
            tvFinAvailable.setText(MillimesUtil.formatMillimes(funds.getAvailableFundsMillimes()));
            tvFinAllocated.setText(MillimesUtil.formatMillimes(funds.getAllocatedFundsMillimes()));
        }

        layoutAllocationsHistory.removeAllViews();
        List<FamilyAllocation> list = familyDao.getAllocations(familyId);
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucune allocation distribuée pour le moment.");
            empty.setTextColor(Color.parseColor("#6B7C74"));
            empty.setPadding(0, 16, 0, 16);
            layoutAllocationsHistory.addView(empty);
            return;
        }

        for (FamilyAllocation a : list) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setBackgroundResource(R.drawable.bg_input_card);
            item.setPadding(32, 24, 32, 24);
            item.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 14;
            item.setLayoutParams(lp);

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(64, 64));
            iv.setBackgroundResource(R.drawable.bg_circle_mint);
            iv.setImageResource(R.drawable.ic_wallet);
            iv.setPadding(14, 14, 14, 14);
            iv.setColorFilter(Color.parseColor("#102B27"));
            item.addView(iv);

            LinearLayout details = new LinearLayout(this);
            details.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);
            // Définit les marges du bloc de détails : 20px à gauche pour séparer de l'icône
            dLp.setMargins(20, 0, 0, 0);
            details.setLayoutParams(dLp);

            TextView tvChild = new TextView(this);
            tvChild.setText(a.getChildName());
            tvChild.setTextSize(15);
            tvChild.setTextColor(Color.parseColor("#102B27"));
            tvChild.setTypeface(null, Typeface.BOLD);
            details.addView(tvChild);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(a.getDate() + " · " + (a.getDescription() != null ? a.getDescription() : "Argent de poche"));
            tvDesc.setTextSize(12);
            tvDesc.setTextColor(Color.parseColor("#6B7C74"));
            details.addView(tvDesc);

            item.addView(details);

            TextView tvAmt = new TextView(this);
            tvAmt.setText("+" + MillimesUtil.formatMillimes(a.getAmountMillimes()));
            tvAmt.setTextSize(15);
            tvAmt.setTextColor(Color.parseColor("#2D7D68"));
            tvAmt.setTypeface(null, Typeface.BOLD);
            item.addView(tvAmt);

            layoutAllocationsHistory.addView(item);
        }
    }

    private void showAdjustFundsDialog() {
        FamilyFunds funds = familyDao.getFamilyFunds(familyId);
        if (funds == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajuster le budget total familial");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        TextView lbl = new TextView(this);
        lbl.setText("Nouveau budget total en DT :");
        lbl.setTextColor(Color.parseColor("#102B27"));
        lbl.setTypeface(null, Typeface.BOLD);
        layout.addView(lbl);

        EditText etTotal = new EditText(this);
        etTotal.setText(String.format(Locale.US, "%.3f", funds.getTotalFundsDt()));
        etTotal.setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etTotal);

        builder.setView(layout);
        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String valStr = etTotal.getText().toString().trim();
            if (valStr.isEmpty())
                return;
            try {
                long newTotalMillimes = MillimesUtil.dtToMillimes(Double.parseDouble(valStr));
                if (newTotalMillimes < funds.getAllocatedFundsMillimes()) {
                    Toast.makeText(this, "Le total ne peut pas être inférieur aux fonds déjà alloués.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                long diff = newTotalMillimes - funds.getTotalFundsMillimes();
                funds.setTotalFundsMillimes(newTotalMillimes);
                funds.setAvailableFundsMillimes(funds.getAvailableFundsMillimes() + diff);
                familyDao.updateFamilyFunds(funds);
                Toast.makeText(this, "Budget mis à jour avec succès !", Toast.LENGTH_SHORT).show();
                loadFinances();
            } catch (Exception e) {
                Toast.makeText(this, "Montant invalide.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showTopUpReserveDialog() {
        FamilyFunds funds = familyDao.getFamilyFunds(familyId);
        if (funds == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alimenter la réserve familiale");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        TextView lbl = new TextView(this);
        lbl.setText("Montant à ajouter à la réserve (en DT) :");
        lbl.setTextColor(Color.parseColor("#102B27"));
        lbl.setTypeface(null, Typeface.BOLD);
        layout.addView(lbl);

        EditText etAmount = new EditText(this);
        etAmount.setHint("ex: 100.000");
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        builder.setView(layout);
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String valStr = etAmount.getText().toString().trim();
            if (valStr.isEmpty()) return;
            try {
                long topUpMillimes = MillimesUtil.dtToMillimes(Double.parseDouble(valStr));
                if (topUpMillimes <= 0) return;

                funds.setTotalFundsMillimes(funds.getTotalFundsMillimes() + topUpMillimes);
                funds.setAvailableFundsMillimes(funds.getAvailableFundsMillimes() + topUpMillimes);
                familyDao.updateFamilyFunds(funds);
                Toast.makeText(this, "Réserve alimentée de +" + valStr + " DT avec succès !", Toast.LENGTH_SHORT).show();
                loadFinances();
            } catch (Exception e) {
                Toast.makeText(this, "Montant invalide.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
}

package com.example.masrouf.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;

/**
 * RÔLE : Écran de définition des paramètres généraux d'un nouveau budget (Figma 09).
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Exigence n°2 (Layout XML : res/layout/activity_create_budget.xml).
 *   - Matrice §4 ligne 5 (Listener explicite via CLASSE NOMMÉE : private class BudgetActionClickListener implements View.OnClickListener, Ch.4 méthode 1).
 *   - Matrice §4 ligne 27 (Utilisation de RadioGroup / RadioButton pour le type de période).
 *   - Matrice §4 ligne 9 (Transmission des montants et dates à l'écran suivant via Intent extras).
 */
public class CreateBudgetActivity extends AppCompatActivity {

    private static final String TAG = "CreateBudgetActivity";

    public static final String EXTRA_BUDGET_TOTAL = "extra_budget_total";
    public static final String EXTRA_BUDGET_START = "extra_budget_start";
    public static final String EXTRA_BUDGET_END = "extra_budget_end";

    private EditText etStartDate;
    private EditText etEndDate;
    private EditText etTotalBudget;
    private RadioGroup rgPeriodType;
    private RadioButton rbMonthly;
    private RadioButton rbCustom;
    private Button btnAllocateCategory;
    private Button btnCancelBudget;
    private ImageView ivBackCreateBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_budget);

        initViews();

        // EXIGENCE MATRICE §4 LIGNE 5 : Instanciation et liaison d'une CLASSE NOMMÉE de listener
        BudgetActionClickListener namedListener = new BudgetActionClickListener();
        btnAllocateCategory.setOnClickListener(namedListener);
        btnCancelBudget.setOnClickListener(namedListener);
        ivBackCreateBudget.setOnClickListener(namedListener);

        // Gestion du changement de RadioGroup (Matrice §4 ligne 27)
        rgPeriodType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMonthly) {
                etStartDate.setText("2026-11-01");
                etEndDate.setText("2026-11-30");
            }
        });
    }

    private void initViews() {
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etTotalBudget = findViewById(R.id.etTotalBudget);
        rgPeriodType = findViewById(R.id.rgPeriodType);
        rbMonthly = findViewById(R.id.rbMonthly);
        rbCustom = findViewById(R.id.rbCustom);
        btnAllocateCategory = findViewById(R.id.btnAllocateCategory);
        btnCancelBudget = findViewById(R.id.btnCancelBudget);
        ivBackCreateBudget = findViewById(R.id.ivBackCreateBudget);
    }

    /**
     * EXIGENCE DU COURS : Listener explicite déclaré sous forme de CLASSE NOMMÉE (Ch.4 méthode 1).
     * Démontre la première méthode enseignée pour écouter les clics de boutons.
     */
    private class BudgetActionClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btnAllocateCategory) {
                handleAllocateAction();
            } else if (id == R.id.btnCancelBudget || id == R.id.ivBackCreateBudget) {
                finish();
            }
        }
    }

    private void handleAllocateAction() {
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String totalStr = etTotalBudget.getText().toString().trim();

        if (startDate.isEmpty() || endDate.isEmpty() || totalStr.isEmpty()) {
            Toast.makeText(this, "Veuillez renseigner toutes les informations du budget.", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalAmount;
        try {
            totalAmount = Double.parseDouble(totalStr);
            if (totalAmount <= 0) {
                Toast.makeText(this, "Le budget doit être supérieur à zéro.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Montant invalide.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigation vers l'écran d'allocation par catégorie (Figma 10) avec passage des extras
        Intent intent = new Intent(this, AllocateBudgetActivity.class);
        intent.putExtra(EXTRA_BUDGET_TOTAL, totalAmount);
        intent.putExtra(EXTRA_BUDGET_START, startDate);
        intent.putExtra(EXTRA_BUDGET_END, endDate);
        startActivity(intent);
        finish();
    }
}

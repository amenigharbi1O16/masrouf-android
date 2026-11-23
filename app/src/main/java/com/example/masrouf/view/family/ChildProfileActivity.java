package com.example.masrouf.view.family;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.AuthController;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.ChildPermission;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;

import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Profil détaillé d'un enfant et gestion de ses plafonds et permissions
 * parentales.
 * COUCHE MVC : Vue (View).
 */
import com.example.masrouf.BaseActivity;

public class ChildProfileActivity extends BaseActivity {

    private ImageView ivBackChildProfile;
    private TextView tvChildProfileTitle;
    private Button btnGiveMoneyProfile;

    private TextView tvChildHeaderName;
    private TextView tvChildHeaderEmail;
    private TextView tvChildBalanceVal;
    private TextView tvChildSpentMonthVal;

    private EditText etDailyLimit;
    private EditText etApprovalThreshold;
    private CheckBox cbCanViewBudget;
    private CheckBox cbIsActive;
    private Button btnSavePermissions;
    private Button btnResetChildPassword;

    private LinearLayout layoutChildHistory;

    private FamilyDao familyDao;
    private UserDao userDao;
    private SessionManager sessionManager;

    private long childId = -1;
    private String childName = "";
    private User currentChild;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (sessionManager.isChild()) {
            Toast.makeText(this, "Accès réservé : Seul votre père peut modifier les plafonds du compte enfant.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_child_profile);

        familyDao = new FamilyDao(this);
        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);

        childId = getIntent().getLongExtra("child_id", -1);
        childName = getIntent().getStringExtra("child_name");

        initViews();
        loadChildData();
    }

    private void initViews() {
        ivBackChildProfile = findViewById(R.id.ivBackChildProfile);
        tvChildProfileTitle = findViewById(R.id.tvChildProfileTitle);
        btnGiveMoneyProfile = findViewById(R.id.btnGiveMoneyProfile);

        tvChildHeaderName = findViewById(R.id.tvChildHeaderName);
        tvChildHeaderEmail = findViewById(R.id.tvChildHeaderEmail);
        tvChildBalanceVal = findViewById(R.id.tvChildBalanceVal);
        tvChildSpentMonthVal = findViewById(R.id.tvChildSpentMonthVal);

        etDailyLimit = findViewById(R.id.etDailyLimit);
        etApprovalThreshold = findViewById(R.id.etApprovalThreshold);
        cbCanViewBudget = findViewById(R.id.cbCanViewBudget);
        cbIsActive = findViewById(R.id.cbIsActive);
        btnSavePermissions = findViewById(R.id.btnSavePermissions);
        btnResetChildPassword = findViewById(R.id.btnResetChildPassword);

        layoutChildHistory = findViewById(R.id.layoutChildHistory);

        ivBackChildProfile.setOnClickListener(v -> finish());
        btnGiveMoneyProfile.setOnClickListener(v -> showGiveMoneyDialog());
        btnSavePermissions.setOnClickListener(v -> savePermissions());
        if (btnResetChildPassword != null) {
            btnResetChildPassword.setOnClickListener(v -> showResetPasswordDialog());
        }
    }

    private void loadChildData() {
        if (childId <= 0) {
            Toast.makeText(this, "Enfant non trouvé.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentChild = userDao.getUserById(childId);
        if (currentChild == null) {
            Toast.makeText(this, "Utilisateur introuvable.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvChildHeaderName.setText(currentChild.getFullName());
        tvChildProfileTitle.setText("Profil · " + currentChild.getFullName());
        tvChildHeaderEmail.setText(currentChild.getEmail());
        tvChildBalanceVal.setText(MillimesUtil.formatMillimes(currentChild.getBalanceMillimes()));

        long monthlySpent = familyDao.getChildMonthlySpendingMillimes(childId, "2026-11");
        tvChildSpentMonthVal.setText(MillimesUtil.formatMillimes(monthlySpent));

        // Charger permissions
        ChildPermission perm = familyDao.getChildPermissions(childId);
        if (perm != null) {
            etDailyLimit.setText(String.format(Locale.US, "%.3f", perm.getDailyLimitDt()));
            etApprovalThreshold.setText(String.format(Locale.US, "%.3f", perm.getApprovalThresholdDt()));
            cbCanViewBudget.setChecked(perm.isCanViewFamilyBudget());
            cbIsActive.setChecked(perm.isAccountActive());
        }

        showPrivacyNotice();
    }

    /**
     * Garantit le respect de la règle de confidentialité de l'espace enfant :
     * Affiche un message explicatif indiquant que le détail des transactions
     * personnelles de l'enfant est privé et non accessible au parent.
     */
    private void showPrivacyNotice() {
        if (layoutChildHistory == null) return;
        layoutChildHistory.removeAllViews();

        LinearLayout noticeCard = new LinearLayout(this);
        noticeCard.setOrientation(LinearLayout.VERTICAL);
        noticeCard.setBackgroundResource(R.drawable.bg_input_card);
        noticeCard.setPadding(32, 28, 32, 28);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noticeCard.setLayoutParams(lp);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView ivShield = new ImageView(this);
        ivShield.setLayoutParams(new LinearLayout.LayoutParams(56, 56));
        ivShield.setImageResource(R.drawable.ic_shield);
        ivShield.setColorFilter(Color.parseColor("#102B27"));
        topRow.addView(ivShield);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Espace personnel protégé");
        tvTitle.setTextSize(15);
        tvTitle.setTextColor(Color.parseColor("#102B27"));
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setPadding(20, 0, 0, 0);
        topRow.addView(tvTitle);

        noticeCard.addView(topRow);

        TextView tvDesc = new TextView(this);
        tvDesc.setText("Conformément aux règles de confidentialité de Masrouf, l'enfant gère son espace en toute autonomie. Vous pouvez suivre son solde global et ses dépenses mensuelles, mais le détail exact de chacun de ses achats reste privé.");
        tvDesc.setTextSize(13);
        tvDesc.setTextColor(Color.parseColor("#6B7C74"));
        tvDesc.setPadding(0, 16, 0, 0);
        noticeCard.addView(tvDesc);

        layoutChildHistory.addView(noticeCard);
    }

    private void savePermissions() {
        try {
            String dailyStr = etDailyLimit.getText().toString().trim();
            String appStr = etApprovalThreshold.getText().toString().trim();

            long dailyMillimes = dailyStr.isEmpty() ? 50000 : MillimesUtil.dtToMillimes(Double.parseDouble(dailyStr));
            long appMillimes = appStr.isEmpty() ? 20000 : MillimesUtil.dtToMillimes(Double.parseDouble(appStr));

            ChildPermission p = new ChildPermission(childId, cbCanViewBudget.isChecked(),
                    appMillimes, dailyMillimes, cbIsActive.isChecked());
            boolean ok = familyDao.saveChildPermissions(p);

            // Mettre à jour isActive dans users
            userDao.setUserActive(childId, cbIsActive.isChecked());

            if (ok) {
                Toast.makeText(this, "Paramètres et plafonds enregistrés avec succès !", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erreur lors de l'enregistrement.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Format de montant invalide.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showGiveMoneyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Allouer de l'argent à " + currentChild.getFullName());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        EditText etAmount = new EditText(this);
        etAmount.setHint("Montant en DT (ex: 15.000)");
        etAmount.setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        EditText etDesc = new EditText(this);
        etDesc.setHint("Motif (facultatif)");
        layout.addView(etDesc);

        builder.setView(layout);
        builder.setPositiveButton("Transférer", (dialog, which) -> {
            String amtStr = etAmount.getText().toString().trim();
            if (amtStr.isEmpty())
                return;
            try {
                long millimes = MillimesUtil.dtToMillimes(Double.parseDouble(amtStr));
                boolean ok = familyDao.allocateMoneyToChild(sessionManager.getFamilyId(), sessionManager.getUserId(),
                        childId, currentChild.getFullName(), millimes, etDesc.getText().toString().trim());
                if (ok) {
                    Toast.makeText(this, "Allocation effectuée avec succès !", Toast.LENGTH_SHORT).show();
                    loadChildData();
                } else {
                    Toast.makeText(this, "Fonds disponibles insuffisants.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Montant invalide.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showResetPasswordDialog() {
        if (currentChild == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Réinitialiser le mot de passe");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        TextView lbl = new TextView(this);
        lbl.setText("Nouveau mot de passe temporaire pour " + currentChild.getFullName() + " :");
        lbl.setTextColor(Color.parseColor("#102B27"));
        lbl.setTypeface(null, Typeface.BOLD);
        layout.addView(lbl);

        EditText etNewPwd = new EditText(this);
        etNewPwd.setHint("Min. 8 car., 1 majuscule, 1 chiffre, 1 symbole");
        etNewPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPwd);

        builder.setView(layout);
        builder.setPositiveButton("Réinitialiser", (dialog, which) -> {
            String newPwd = etNewPwd.getText().toString().trim();
            if (!AuthController.isValidPasswordPolicy(newPwd)) {
                Toast.makeText(this, "Le mot de passe doit contenir au moins 8 caractères, 1 majuscule, 1 chiffre et 1 symbole.", Toast.LENGTH_LONG).show();
                return;
            }

            boolean ok = userDao.resetPassword(childId, newPwd);
            if (ok) {
                Toast.makeText(this, "Mot de passe réinitialisé avec succès ! L'enfant devra le modifier à la première connexion.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Erreur lors de la réinitialisation.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
}

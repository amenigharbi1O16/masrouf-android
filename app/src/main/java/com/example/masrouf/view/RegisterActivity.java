package com.example.masrouf.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.PasswordUtil;
import com.example.masrouf.view.family.FamilyDashboardActivity;

/**
 * RÔLE : Activité de création de compte pour un nouvel utilisateur (Image 3).
 * COUCHE MVC : Vue (View).
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private ImageView ivBack;
    private ImageView ivBell;
    private ImageView ivProfile;

    private TextView btnLangFr;
    private TextView btnLangAr;
    private TextView btnLangEn;

    private TextView btnTypePersonal;
    private TextView btnTypeFamily;
    private ImageView ivTypeIcon;
    private TextView tvTypeExplanation;
    private String selectedAccountType = User.ROLE_USER;

    private EditText etRegFullName;
    private EditText etRegEmail;
    private EditText etRegPassword;
    private EditText etRegConfirmPassword;

    private TextView tvValLength;
    private TextView tvValDigit;
    private TextView tvValMatch;

    private Button btnDoRegister;
    private Button btnBackToLogin;

    private UserDao userDao;
    private SessionManager sessionManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.userDao = new UserDao(this);
        this.sessionManager = new SessionManager(this);

        String passedRole = getIntent().getStringExtra("account_type");
        if (passedRole != null && !passedRole.isEmpty()) {
            this.selectedAccountType = passedRole;
        }

        initViews();
        updateLanguagePillsState(sessionManager.getLanguage());
        setupListeners();
        setupLiveValidation();
        selectAccountType(selectedAccountType);
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivBell = findViewById(R.id.ivBell);
        ivProfile = findViewById(R.id.ivProfile);

        btnLangFr = findViewById(R.id.btnLangFr);
        btnLangAr = findViewById(R.id.btnLangAr);
        btnLangEn = findViewById(R.id.btnLangEn);

        btnTypePersonal = findViewById(R.id.btnTypePersonal);
        btnTypeFamily = findViewById(R.id.btnTypeFamily);
        ivTypeIcon = findViewById(R.id.ivTypeIcon);
        tvTypeExplanation = findViewById(R.id.tvTypeExplanation);

        etRegFullName = findViewById(R.id.etRegFullName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);

        tvValLength = findViewById(R.id.tvValLength);
        tvValDigit = findViewById(R.id.tvValDigit);
        tvValMatch = findViewById(R.id.tvValMatch);

        btnDoRegister = findViewById(R.id.btnDoRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    private void updateLanguagePillsState(String currentLang) {
        if (btnLangFr == null || btnLangAr == null || btnLangEn == null) return;

        int activeBg = R.drawable.bg_pill_active;
        int inactiveBg = R.drawable.bg_pill_inactive;
        int activeText = Color.WHITE;
        int inactiveText = ContextCompat.getColor(this, R.color.deep_forest);

        if ("ar".equalsIgnoreCase(currentLang)) {
            btnLangAr.setBackgroundResource(activeBg);
            btnLangAr.setTextColor(activeText);
            btnLangFr.setBackgroundResource(inactiveBg);
            btnLangFr.setTextColor(inactiveText);
            btnLangEn.setBackgroundResource(inactiveBg);
            btnLangEn.setTextColor(inactiveText);
        } else if ("en".equalsIgnoreCase(currentLang)) {
            btnLangEn.setBackgroundResource(activeBg);
            btnLangEn.setTextColor(activeText);
            btnLangFr.setBackgroundResource(inactiveBg);
            btnLangFr.setTextColor(inactiveText);
            btnLangAr.setBackgroundResource(inactiveBg);
            btnLangAr.setTextColor(inactiveText);
        } else {
            btnLangFr.setBackgroundResource(activeBg);
            btnLangFr.setTextColor(activeText);
            btnLangAr.setBackgroundResource(inactiveBg);
            btnLangAr.setTextColor(inactiveText);
            btnLangEn.setBackgroundResource(inactiveBg);
            btnLangEn.setTextColor(inactiveText);
        }
    }

    private void setupListeners() {
        if (ivBack != null) ivBack.setOnClickListener(v -> finish());
        if (ivBell != null) ivBell.setOnClickListener(v -> Toast.makeText(this, "Aucune notification", Toast.LENGTH_SHORT).show());

        if (btnLangFr != null) btnLangFr.setOnClickListener(v -> changeLanguage("fr"));
        if (btnLangAr != null) btnLangAr.setOnClickListener(v -> changeLanguage("ar"));
        if (btnLangEn != null) btnLangEn.setOnClickListener(v -> changeLanguage("en"));

        if (btnTypePersonal != null) btnTypePersonal.setOnClickListener(v -> selectAccountType(User.ROLE_USER));
        if (btnTypeFamily != null) btnTypeFamily.setOnClickListener(v -> selectAccountType(User.ROLE_PARENT));

        btnDoRegister.setOnClickListener(v -> performRegistration());
        btnBackToLogin.setOnClickListener(v -> {
            Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        });
    }

    private void setupLiveValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateForm(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        etRegFullName.addTextChangedListener(watcher);
        etRegEmail.addTextChangedListener(watcher);
        etRegPassword.addTextChangedListener(watcher);
        etRegConfirmPassword.addTextChangedListener(watcher);
    }

    private void validateForm() {
        String name = etRegFullName.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String pwd = etRegPassword.getText().toString().trim();
        String confirm = etRegConfirmPassword.getText().toString().trim();

        boolean hasMinLength = pwd.length() >= 8;
        boolean hasDigit = false;
        for (char c : pwd.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
                break;
            }
        }
        boolean passwordsMatch = !pwd.isEmpty() && pwd.equals(confirm);

        updateIndicator(tvValLength, hasMinLength, "✓ Minimum 8 caractères", "⚪ Minimum 8 caractères");
        updateIndicator(tvValDigit, hasDigit, "✓ Au moins 1 chiffre", "⚪ Au moins 1 chiffre");
        updateIndicator(tvValMatch, passwordsMatch, "✓ Les mots de passe correspondent", "⚪ Les mots de passe correspondent");

        boolean isValid = !name.isEmpty() && !email.isEmpty() && hasMinLength && hasDigit && passwordsMatch;

        btnDoRegister.setEnabled(isValid);
        btnDoRegister.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void updateIndicator(TextView tv, boolean isValid, String validText, String invalidText) {
        if (tv == null) return;
        if (isValid) {
            tv.setText(validText);
            tv.setTextColor(ContextCompat.getColor(this, R.color.primary_emerald));
        } else {
            tv.setText(invalidText);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }

    private void selectAccountType(String role) {
        this.selectedAccountType = role;
        if (User.ROLE_PARENT.equals(role)) {
            if (btnTypeFamily != null) {
                btnTypeFamily.setBackgroundResource(R.drawable.bg_pill_active);
                btnTypeFamily.setTextColor(Color.WHITE);
            }
            if (btnTypePersonal != null) {
                btnTypePersonal.setBackgroundResource(R.drawable.bg_pill_inactive);
                btnTypePersonal.setTextColor(ContextCompat.getColor(this, R.color.deep_forest));
            }
            if (tvTypeExplanation != null) {
                tvTypeExplanation.setText(R.string.reg_info_family);
            }
            if (ivTypeIcon != null) {
                ivTypeIcon.setImageResource(R.drawable.ic_profile);
            }
        } else {
            if (btnTypePersonal != null) {
                btnTypePersonal.setBackgroundResource(R.drawable.bg_pill_active);
                btnTypePersonal.setTextColor(Color.WHITE);
            }
            if (btnTypeFamily != null) {
                btnTypeFamily.setBackgroundResource(R.drawable.bg_pill_inactive);
                btnTypeFamily.setTextColor(ContextCompat.getColor(this, R.color.deep_forest));
            }
            if (tvTypeExplanation != null) {
                tvTypeExplanation.setText(R.string.reg_info_personal);
            }
            if (ivTypeIcon != null) {
                ivTypeIcon.setImageResource(R.drawable.ic_profile);
            }
        }
    }

    private void changeLanguage(String languageCode) {
        sessionManager.saveLanguage(languageCode);
        LocaleHelper.setLocale(this, languageCode);
        recreate();
    }

    private void performRegistration() {
        String fullName = etRegFullName.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim().toLowerCase();
        String password = etRegPassword.getText().toString().trim();

        if (userDao.getUserByEmail(email) != null) {
            Toast.makeText(this, R.string.error_email_already_exists, Toast.LENGTH_LONG).show();
            etRegEmail.requestFocus();
            return;
        }

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);

        User newUser = new User(fullName, email, passwordHash, salt, selectedAccountType);

        // Pour un Compte Personnel (ROLE_USER), family_id doit être 0 (NULL), pas 1!
        if (User.ROLE_PARENT.equals(selectedAccountType)) {
            newUser.setFamilyId(1);
        } else {
            newUser.setFamilyId(0);
        }

        long newUserId = userDao.insertUser(newUser);

        if (newUserId > 0) {
            Log.d(TAG, "Utilisateur créé avec ID: " + newUserId + " (Rôle: " + selectedAccountType + ")");
            Toast.makeText(this, R.string.msg_register_success, Toast.LENGTH_SHORT).show();

            sessionManager.createLoginSession(newUserId, email, fullName, selectedAccountType);

            if (User.ROLE_PARENT.equals(selectedAccountType)) {
                FamilyDao familyDao = new FamilyDao(this);
                familyDao.ensureFamilyFundsCreated(newUserId, "Famille " + fullName);
                sessionManager.setFamilyId(1);
                sessionManager.setFamilyName("Famille " + fullName);
                sessionManager.setActiveSpace(SessionManager.SPACE_FAMILY);

                Intent familyIntent = new Intent(RegisterActivity.this, FamilyDashboardActivity.class);
                familyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(familyIntent);
                finish();
            } else {
                sessionManager.setOnboardingCompleted(false);

                Intent onboardingIntent = new Intent(RegisterActivity.this, OnboardingWelcomeActivity.class);
                onboardingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(onboardingIntent);
                finish();
            }
        } else {
            Toast.makeText(this, R.string.error_generic_database, Toast.LENGTH_LONG).show();
        }
    }
}

package com.example.masrouf;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.User;
import com.example.masrouf.util.PasswordUtil;
import com.example.masrouf.view.admin.AdminDashboardActivity;
import com.example.masrouf.view.child.ChildMainActivity;
import com.example.masrouf.view.family.FamilyDashboardActivity;

public class LoginActivity extends BaseActivity {

    private EditText etEmail, etPassword;
    private TextView tvErrorBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        tvErrorBanner = findViewById(R.id.tv_login_error_banner);

        setupLangChips();

        // Listener explicite
        findViewById(R.id.btn_login).setOnClickListener(v -> login());
    }

    /** Listener implicite : android:onClick="onCreateAccountClick" dans activity_login.xml */
    public void onCreateAccountClick(View view) {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    private void login() {
        if (tvErrorBanner != null) {
            tvErrorBanner.setVisibility(View.GONE);
        }

        String email = etEmail.getText().toString().trim();
        String pwd = etPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.err_email));
            return;
        }
        if (pwd.isEmpty()) {
            etPassword.setError(getString(R.string.err_password_len));
            return;
        }

        UserDao userDao = new UserDao(this);
        User user = userDao.findByEmail(email);

        if (user == null) {
            showError();
            return;
        }

        String pwdHash = PasswordUtil.hashPassword(pwd);
        if (!pwdHash.equals(user.getPasswordHash())) {
            showError();
            return;
        }

        // Authentification réussie
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.saveSession(user);

        Toast.makeText(this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
        redirectToHome(user);
    }

    private void showError() {
        if (tvErrorBanner != null) {
            tvErrorBanner.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_LONG).show();
        }
    }

    private void redirectToHome(User user) {
        String role = user.getRole();
        long userId = user.getId();
        com.example.masrouf.dao.CategoryDao categoryDao = new com.example.masrouf.dao.CategoryDao(this);
        com.example.masrouf.dao.BudgetDao budgetDao = new com.example.masrouf.dao.BudgetDao(this);

        Intent intent;
        if (User.ROLE_ADMIN.equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if (User.ROLE_PARENT.equalsIgnoreCase(role)) {
            intent = new Intent(this, FamilyDashboardActivity.class);
        } else if (User.ROLE_CHILD.equalsIgnoreCase(role)) {
            if (!categoryDao.hasUserSelectedCategories(userId)) {
                intent = new Intent(this, com.example.masrouf.view.OnboardingCategoriesActivity.class);
            } else {
                intent = new Intent(this, ChildMainActivity.class);
            }
        } else {
            if (!categoryDao.hasUserSelectedCategories(userId)) {
                intent = new Intent(this, com.example.masrouf.view.OnboardingCategoriesActivity.class);
            } else {
                com.example.masrouf.model.Budget budget = budgetDao.getCurrentBudget(userId);
                if (budget == null || budget.getTotalAmount() <= 0) {
                    intent = new Intent(this, com.example.masrouf.view.CreateBudgetActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
            }
        }
        startActivity(intent);
        finish();
    }
}

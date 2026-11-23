package com.example.masrouf;

import android.content.Intent;
import android.os.Bundle;

import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.model.User;
import com.example.masrouf.view.admin.AdminDashboardActivity;
import com.example.masrouf.view.child.ChildMainActivity;
import com.example.masrouf.view.family.FamilyDashboardActivity;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            redirectToHome(sessionManager.getUserRole());
            return;
        }

        setContentView(R.layout.activity_welcome);

        setupLangPill();
        setupLoginLink();

        // Listener explicite
        findViewById(R.id.btn_start).setOnClickListener(v ->
                startActivity(new Intent(this, OnboardingActivity.class)));
    }

    private void redirectToHome(String role) {
        Intent intent;
        if (User.ROLE_ADMIN.equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if (User.ROLE_PARENT.equalsIgnoreCase(role)) {
            intent = new Intent(this, FamilyDashboardActivity.class);
        } else if (User.ROLE_CHILD.equalsIgnoreCase(role)) {
            intent = new Intent(this, ChildMainActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}

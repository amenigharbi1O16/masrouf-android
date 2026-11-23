package com.example.masrouf;

import android.content.Intent;
import android.os.Bundle;

public class AccountTypeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);

        setupLangPill();
        setupLoginLink();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.card_personal).setOnClickListener(v -> openSignUp(SignUpActivity.TYPE_PERSONAL));
        findViewById(R.id.card_family).setOnClickListener(v -> openSignUp(SignUpActivity.TYPE_FAMILY));
    }

    private void openSignUp(String type) {
        Intent i = new Intent(this, SignUpActivity.class);
        i.putExtra(SignUpActivity.EXTRA_TYPE, type);
        startActivity(i);
    }
}

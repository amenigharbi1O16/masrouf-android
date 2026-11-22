package com.example.masrouf.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.util.LocaleHelper;

/**
 * RÔLE : Écran 01 / 03 du parcours de configuration des catégories (Prenez le contrôle de votre budget).
 * Conforme à 100% à la capture d'écran n°4.
 * COUCHE MVC : Vue (View).
 */
public class OnboardingStep1Activity extends AppCompatActivity {

    private ImageView ivStep1Back;
    private Button btnStep1SelectCategories;
    private SessionManager sessionManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_step1);

        sessionManager = new SessionManager(this);

        ivStep1Back = findViewById(R.id.ivStep1Back);
        btnStep1SelectCategories = findViewById(R.id.btnStep1SelectCategories);

        ivStep1Back.setOnClickListener(v -> finish());
        btnStep1SelectCategories.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingStep1Activity.this, OnboardingCategoriesActivity.class);
            startActivity(intent);
        });
    }
}

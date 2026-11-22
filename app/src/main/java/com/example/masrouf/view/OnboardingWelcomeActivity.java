package com.example.masrouf.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.view.child.ChildMainActivity;
import com.example.masrouf.view.family.FamilyDashboardActivity;

/**
 * RÔLE : Parcours d'accueil et de découverte en 3 phases (Welcome Flow / Slides / Choix du type de compte).
 * COUCHE MVC : Vue (View).
 * Conforme à 100% aux captures d'écran de l'application Masrouf (/welcome).
 */
public class OnboardingWelcomeActivity extends AppCompatActivity {

    private static final int PHASE_SPLASH = 1;
    private static final int PHASE_SLIDES = 2;
    private static final int PHASE_CHOICE = 3;

    private int currentPhase = PHASE_SPLASH;
    private int currentSlideIndex = 0; // 0, 1, 2

    private ImageView ivWelcomeBack;
    private TextView tvWelcomeSkip;

    private TextView btnWelcomeLangEn;
    private TextView btnWelcomeLangFr;
    private TextView btnWelcomeLangAr;

    private LinearLayout layoutPhase1;
    private LinearLayout layoutPhase2;
    private ScrollView layoutPhase3;

    // Éléments du carrousel Phase 2
    private ImageView ivSlideIcon;
    private TextView tvSlideTitle;
    private TextView tvSlideDesc;
    private TextView tvSlideCheck1;
    private TextView tvSlideCheck2;
    private View dot1, dot2, dot3;

    // Cartes Phase 3
    private LinearLayout cardPersonalChoice;
    private LinearLayout cardFamilyChoice;
    private String selectedRole = User.ROLE_USER;

    private Button btnWelcomeMainAction;
    private TextView btnWelcomeGoToLogin;

    private SessionManager sessionManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            Intent intent;
            if (sessionManager.isParent()) {
                intent = new Intent(this, FamilyDashboardActivity.class);
            } else if (sessionManager.isChild()) {
                intent = new Intent(this, ChildMainActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_onboarding_welcome);

        initViews();
        updateLanguagePillsState(sessionManager.getLanguage());
        setupListeners();
        updatePhaseUI();
    }

    private void initViews() {
        ivWelcomeBack = findViewById(R.id.ivWelcomeBack);
        tvWelcomeSkip = findViewById(R.id.tvWelcomeSkip);

        btnWelcomeLangEn = findViewById(R.id.btnWelcomeLangEn);
        btnWelcomeLangFr = findViewById(R.id.btnWelcomeLangFr);
        btnWelcomeLangAr = findViewById(R.id.btnWelcomeLangAr);

        layoutPhase1 = findViewById(R.id.layoutPhase1);
        layoutPhase2 = findViewById(R.id.layoutPhase2);
        layoutPhase3 = findViewById(R.id.layoutPhase3);

        ivSlideIcon = findViewById(R.id.ivSlideIcon);
        tvSlideTitle = findViewById(R.id.tvSlideTitle);
        tvSlideDesc = findViewById(R.id.tvSlideDesc);
        tvSlideCheck1 = findViewById(R.id.tvSlideCheck1);
        tvSlideCheck2 = findViewById(R.id.tvSlideCheck2);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        cardPersonalChoice = findViewById(R.id.cardPersonalChoice);
        cardFamilyChoice = findViewById(R.id.cardFamilyChoice);

        btnWelcomeMainAction = findViewById(R.id.btnWelcomeMainAction);
        btnWelcomeGoToLogin = findViewById(R.id.btnWelcomeGoToLogin);
    }

    private void updateLanguagePillsState(String lang) {
        if (btnWelcomeLangEn == null || btnWelcomeLangFr == null || btnWelcomeLangAr == null) return;

        int activeBg = R.drawable.bg_pill_active;
        int inactiveBg = R.drawable.bg_pill_inactive;
        int activeText = Color.WHITE;
        int inactiveText = ContextCompat.getColor(this, R.color.deep_forest);

        if ("ar".equalsIgnoreCase(lang)) {
            btnWelcomeLangAr.setBackgroundResource(activeBg);
            btnWelcomeLangAr.setTextColor(activeText);
            btnWelcomeLangFr.setBackgroundResource(inactiveBg);
            btnWelcomeLangFr.setTextColor(inactiveText);
            btnWelcomeLangEn.setBackgroundResource(inactiveBg);
            btnWelcomeLangEn.setTextColor(inactiveText);
        } else if ("en".equalsIgnoreCase(lang)) {
            btnWelcomeLangEn.setBackgroundResource(activeBg);
            btnWelcomeLangEn.setTextColor(activeText);
            btnWelcomeLangFr.setBackgroundResource(inactiveBg);
            btnWelcomeLangFr.setTextColor(inactiveText);
            btnWelcomeLangAr.setBackgroundResource(inactiveBg);
            btnWelcomeLangAr.setTextColor(inactiveText);
        } else {
            btnWelcomeLangFr.setBackgroundResource(activeBg);
            btnWelcomeLangFr.setTextColor(activeText);
            btnWelcomeLangAr.setBackgroundResource(inactiveBg);
            btnWelcomeLangAr.setTextColor(inactiveText);
            btnWelcomeLangEn.setBackgroundResource(inactiveBg);
            btnWelcomeLangEn.setTextColor(inactiveText);
        }
    }

    private void changeLanguage(String code) {
        sessionManager.saveLanguage(code);
        LocaleHelper.setLocale(this, code);
        recreate();
    }

    private void setupListeners() {
        btnWelcomeLangEn.setOnClickListener(v -> changeLanguage("en"));
        btnWelcomeLangFr.setOnClickListener(v -> changeLanguage("fr"));
        btnWelcomeLangAr.setOnClickListener(v -> changeLanguage("ar"));

        ivWelcomeBack.setOnClickListener(v -> handleBackAction());
        tvWelcomeSkip.setOnClickListener(v -> {
            currentPhase = PHASE_CHOICE;
            updatePhaseUI();
        });

        btnWelcomeMainAction.setOnClickListener(v -> handleMainAction());

        btnWelcomeGoToLogin.setOnClickListener(v -> {
            Intent loginIntent = new Intent(OnboardingWelcomeActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        });

        cardPersonalChoice.setOnClickListener(v -> {
            selectedRole = User.ROLE_USER;
            highlightChoiceCard();
            navigateToRegister(selectedRole);
        });

        cardFamilyChoice.setOnClickListener(v -> {
            selectedRole = User.ROLE_PARENT;
            highlightChoiceCard();
            navigateToRegister(selectedRole);
        });
    }

    private void highlightChoiceCard() {
        if (User.ROLE_PARENT.equals(selectedRole)) {
            cardFamilyChoice.setBackgroundResource(R.drawable.bg_pill_active);
            cardPersonalChoice.setBackgroundResource(R.drawable.bg_input_card);
        } else {
            cardPersonalChoice.setBackgroundResource(R.drawable.bg_pill_active);
            cardFamilyChoice.setBackgroundResource(R.drawable.bg_input_card);
        }
    }

    private void handleBackAction() {
        if (currentPhase == PHASE_CHOICE) {
            currentPhase = PHASE_SLIDES;
            currentSlideIndex = 2;
            updatePhaseUI();
        } else if (currentPhase == PHASE_SLIDES) {
            if (currentSlideIndex > 0) {
                currentSlideIndex--;
                updateSlideUI();
            } else {
                currentPhase = PHASE_SPLASH;
                updatePhaseUI();
            }
        } else {
            finish();
        }
    }

    private void handleMainAction() {
        if (currentPhase == PHASE_SPLASH) {
            currentPhase = PHASE_SLIDES;
            currentSlideIndex = 0;
            updatePhaseUI();
        } else if (currentPhase == PHASE_SLIDES) {
            if (currentSlideIndex < 2) {
                currentSlideIndex++;
                updateSlideUI();
            } else {
                currentPhase = PHASE_CHOICE;
                updatePhaseUI();
            }
        } else {
            navigateToRegister(selectedRole);
        }
    }

    private void navigateToRegister(String role) {
        Intent intent = new Intent(OnboardingWelcomeActivity.this, RegisterActivity.class);
        intent.putExtra("account_type", role);
        startActivity(intent);
    }

    private void updatePhaseUI() {
        if (currentPhase == PHASE_SPLASH) {
            ivWelcomeBack.setVisibility(View.INVISIBLE);
            tvWelcomeSkip.setVisibility(View.INVISIBLE);

            layoutPhase1.setVisibility(View.VISIBLE);
            layoutPhase2.setVisibility(View.GONE);
            layoutPhase3.setVisibility(View.GONE);

            btnWelcomeMainAction.setText(R.string.welcome_btn_start);
        } else if (currentPhase == PHASE_SLIDES) {
            ivWelcomeBack.setVisibility(View.VISIBLE);
            tvWelcomeSkip.setVisibility(View.VISIBLE);

            layoutPhase1.setVisibility(View.GONE);
            layoutPhase2.setVisibility(View.VISIBLE);
            layoutPhase3.setVisibility(View.GONE);

            updateSlideUI();
        } else {
            ivWelcomeBack.setVisibility(View.VISIBLE);
            tvWelcomeSkip.setVisibility(View.INVISIBLE);

            layoutPhase1.setVisibility(View.GONE);
            layoutPhase2.setVisibility(View.GONE);
            layoutPhase3.setVisibility(View.VISIBLE);

            btnWelcomeMainAction.setText(R.string.welcome_btn_choose_account);
            highlightChoiceCard();
        }
    }

    private void updateSlideUI() {
        int activeDotWidth = (int) (24 * getResources().getDisplayMetrics().density);
        int inactiveDotWidth = (int) (8 * getResources().getDisplayMetrics().density);

        if (currentSlideIndex == 0) {
            ivSlideIcon.setImageResource(R.drawable.ic_wallet);
            tvSlideTitle.setText(R.string.welcome_slide1_title);
            tvSlideDesc.setText(R.string.welcome_slide1_desc);
            tvSlideCheck1.setText(R.string.welcome_slide1_check1);
            tvSlideCheck2.setText(R.string.welcome_slide1_check2);

            setDotState(dot1, true, activeDotWidth);
            setDotState(dot2, false, inactiveDotWidth);
            setDotState(dot3, false, inactiveDotWidth);

            btnWelcomeMainAction.setText(R.string.welcome_btn_continue);
        } else if (currentSlideIndex == 1) {
            ivSlideIcon.setImageResource(R.drawable.ic_analytics);
            tvSlideTitle.setText(R.string.welcome_slide2_title);
            tvSlideDesc.setText(R.string.welcome_slide2_desc);
            tvSlideCheck1.setText(R.string.welcome_slide2_check1);
            tvSlideCheck2.setText(R.string.welcome_slide2_check2);

            setDotState(dot1, false, inactiveDotWidth);
            setDotState(dot2, true, activeDotWidth);
            setDotState(dot3, false, inactiveDotWidth);

            btnWelcomeMainAction.setText(R.string.welcome_btn_continue);
        } else {
            ivSlideIcon.setImageResource(R.drawable.ic_profile);
            tvSlideTitle.setText(R.string.welcome_slide3_title);
            tvSlideDesc.setText(R.string.welcome_slide3_desc);
            tvSlideCheck1.setText(R.string.welcome_slide3_check1);
            tvSlideCheck2.setText(R.string.welcome_slide3_check2);

            setDotState(dot1, false, inactiveDotWidth);
            setDotState(dot2, false, inactiveDotWidth);
            setDotState(dot3, true, activeDotWidth);

            btnWelcomeMainAction.setText(R.string.welcome_btn_choose_account);
        }
    }

    private void setDotState(View dot, boolean isActive, int width) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
        params.width = width;
        dot.setLayoutParams(params);
        dot.setBackgroundResource(isActive ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);
    }

    @Override
    public void onBackPressed() {
        handleBackAction();
    }
}

package com.example.masrouf;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import android.content.res.ColorStateList;

public class OnboardingActivity extends BaseActivity {

    private static final int[] TITLES = {R.string.ob1_title, R.string.ob2_title, R.string.ob3_title};
    private static final int[] DESCS = {R.string.ob1_desc, R.string.ob2_desc, R.string.ob3_desc};
    private static final int[] B1 = {R.string.ob1_b1, R.string.ob2_b1, R.string.ob3_b1};
    private static final int[] B2 = {R.string.ob1_b2, R.string.ob2_b2, R.string.ob3_b2};
    private static final int[] ICONS = {R.drawable.ic_wallet, R.drawable.ic_pie_chart, R.drawable.ic_group};

    // Couleur de la ligne en haut de la carte / du point actif
    private static final int[] ACCENT = {0xFF122B29, 0xFFDCEEDF, 0xFF1E88E5};
    // Couleur de l'icône et des coches
    private static final int[] MAIN = {0xFF122B29, 0xFF2E7D43, 0xFF1E88E5};
    // Fond du cercle de l'icône
    private static final int[] CIRCLE = {0xFFE8F5EC, 0xFFE8F5EC, 0xFFE3F0FC};

    private int step = 0;

    private View card, iconCircle;
    private ImageView ivIcon, ivCheck1, ivCheck2;
    private TextView tvTitle, tvDesc, tvB1, tvB2, btnContinue;
    private LinearLayout dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        card = findViewById(R.id.card_onboarding);
        iconCircle = findViewById(R.id.icon_circle);
        ivIcon = findViewById(R.id.iv_icon);
        ivCheck1 = findViewById(R.id.iv_check1);
        ivCheck2 = findViewById(R.id.iv_check2);
        tvTitle = findViewById(R.id.tv_title);
        tvDesc = findViewById(R.id.tv_desc);
        tvB1 = findViewById(R.id.tv_b1);
        tvB2 = findViewById(R.id.tv_b2);
        btnContinue = findViewById(R.id.btn_continue);
        dots = findViewById(R.id.dots);

        setupLangPill();
        setupLoginLink();

        findViewById(R.id.btn_back).setOnClickListener(v -> goBack());
        findViewById(R.id.tv_skip).setOnClickListener(v -> openAccountType());
        btnContinue.setOnClickListener(v -> {
            if (step < 2) {
                step++;
                render();
            } else {
                openAccountType();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBack();
            }
        });

        render();
    }

    private void goBack() {
        if (step > 0) {
            step--;
            render();
        } else {
            finish();
        }
    }

    private void openAccountType() {
        startActivity(new Intent(this, AccountTypeActivity.class));
    }

    private void render() {
        tvTitle.setText(TITLES[step]);
        tvDesc.setText(DESCS[step]);
        tvB1.setText(B1[step]);
        tvB2.setText(B2[step]);
        ivIcon.setImageResource(ICONS[step]);
        btnContinue.setText(step < 2 ? R.string.continue_btn : R.string.choose_type_btn);

        // Ligne colorée en haut de la carte
        LayerDrawable layers = (LayerDrawable) card.getBackground().mutate();
        GradientDrawable accent = (GradientDrawable) layers.findDrawableByLayerId(R.id.card_accent);
        accent.setColor(ACCENT[step]);

        // Cercle + icône + coches
        ((GradientDrawable) iconCircle.getBackground().mutate()).setColor(CIRCLE[step]);
        ColorStateList main = ColorStateList.valueOf(MAIN[step]);
        ivIcon.setImageTintList(main);
        ivCheck1.setImageTintList(main);
        ivCheck2.setImageTintList(main);

        // Indicateur de pages
        for (int i = 0; i < dots.getChildCount(); i++) {
            View d = dots.getChildAt(i);
            boolean active = i == step;
            GradientDrawable g = new GradientDrawable();
            g.setCornerRadius(dp(5));
            g.setColor(active ? ACCENT[step] : 0xFFCBD5E1);
            d.setBackground(g);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) d.getLayoutParams();
            lp.width = dp(active ? 30 : 10);
            d.setLayoutParams(lp);
        }
    }
}

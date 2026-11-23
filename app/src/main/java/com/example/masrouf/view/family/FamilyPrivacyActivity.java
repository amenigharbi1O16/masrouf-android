package com.example.masrouf.view.family;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.BaseActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.util.LocaleHelper;

/**
 * RÔLE : Gestion de la confidentialité, des règles de sécurité et du nom de famille.
 * COUCHE MVC : Vue (View).
 */
public class FamilyPrivacyActivity extends BaseActivity {

    private ImageView ivBackPrivacy;
    private EditText etFamilyNameEdit;
    private Button btnSaveFamilyName;

    private SessionManager sessionManager;

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
        setContentView(R.layout.activity_family_privacy);

        sessionManager = new SessionManager(this);

        ivBackPrivacy = findViewById(R.id.ivBackPrivacy);
        etFamilyNameEdit = findViewById(R.id.etFamilyNameEdit);
        btnSaveFamilyName = findViewById(R.id.btnSaveFamilyName);

        if (ivBackPrivacy != null) {
            ivBackPrivacy.setOnClickListener(v -> finish());
        }

        if (etFamilyNameEdit != null) {
            etFamilyNameEdit.setText(sessionManager.getFamilyName());
        }

        if (btnSaveFamilyName != null) {
            btnSaveFamilyName.setOnClickListener(v -> {
                String newName = etFamilyNameEdit.getText().toString().trim();
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Le nom ne peut pas être vide.", Toast.LENGTH_SHORT).show();
                    return;
                }
                sessionManager.setFamilyName(newName);
                Toast.makeText(this, "Nom de famille mis à jour !", Toast.LENGTH_SHORT).show();
            });
        }
    }
}

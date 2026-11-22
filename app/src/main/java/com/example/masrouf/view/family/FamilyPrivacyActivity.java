package com.example.masrouf.view.family;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.model.FamilyFunds;
import com.example.masrouf.util.LocaleHelper;

/**
 * RÔLE : Gestion de la confidentialité, des règles de sécurité et du nom de famille.
 * COUCHE MVC : Vue (View).
 */
public class FamilyPrivacyActivity extends AppCompatActivity {

    private ImageView ivBackPrivacy;
    private EditText etFamilyNameEdit;
    private Button btnSaveFamilyName;

    private FamilyDao familyDao;
    private SessionManager sessionManager;
    private long familyId = 1;

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

        familyDao = new FamilyDao(this);
        sessionManager = new SessionManager(this);
        familyId = sessionManager.getFamilyId();

        ivBackPrivacy = findViewById(R.id.ivBackPrivacy);
        etFamilyNameEdit = findViewById(R.id.etFamilyNameEdit);
        btnSaveFamilyName = findViewById(R.id.btnSaveFamilyName);

        ivBackPrivacy.setOnClickListener(v -> finish());

        FamilyFunds funds = familyDao.getFamilyFunds(familyId);
        if (funds != null) {
            etFamilyNameEdit.setText(funds.getFamilyName());
        }

        btnSaveFamilyName.setOnClickListener(v -> {
            String newName = etFamilyNameEdit.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Le nom ne peut pas être vide.", Toast.LENGTH_SHORT).show();
                return;
            }
            FamilyFunds f = familyDao.getFamilyFunds(familyId);
            if (f != null) {
                f.setFamilyName(newName);
                familyDao.updateFamilyFunds(f);
                sessionManager.setFamilyName(newName);
                Toast.makeText(this, "Nom de famille mis à jour !", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

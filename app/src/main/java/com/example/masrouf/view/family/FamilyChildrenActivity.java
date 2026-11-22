package com.example.masrouf.view.family;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;

import java.util.List;

/**
 * RÔLE : Gestion des comptes enfants (consultation, ajout avec identifiants,
 * modification).
 * COUCHE MVC : Vue (View).
 */
public class FamilyChildrenActivity extends AppCompatActivity {

    private ImageView ivBackChildren;
    private Button btnAddNewChild;
    private LinearLayout layoutChildrenList;

    private FamilyDao familyDao;
    private UserDao userDao;
    private SessionManager sessionManager;
    private long familyId = 1;
    private long parentId = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (sessionManager.isChild()) {
            Toast.makeText(this, "Accès réservé : Seul votre père peut accéder à la gestion des enfants.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_family_children);

        familyDao = new FamilyDao(this);
        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);

        familyId = sessionManager.getFamilyId();
        parentId = sessionManager.getUserId();

        ivBackChildren = findViewById(R.id.ivBackChildren);
        btnAddNewChild = findViewById(R.id.btnAddNewChild);
        layoutChildrenList = findViewById(R.id.layoutChildrenList);

        ivBackChildren.setOnClickListener(v -> finish());
        btnAddNewChild.setOnClickListener(v -> showAddChildDialog());

        loadChildren();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildren();
    }

    private void loadChildren() {
        layoutChildrenList.removeAllViews();
        List<User> children = userDao.getChildrenByFamily(familyId);
        if (children.isEmpty()) {
            children = userDao.getChildrenByParent(parentId);
        }

        if (children.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucun enfant rattaché à cette famille.");
            empty.setTextColor(Color.parseColor("#6B7C74"));
            empty.setPadding(0, 24, 0, 24);
            layoutChildrenList.addView(empty);
            return;
        }

        for (User child : children) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_input_card);
            card.setPadding(36, 32, 36, 32);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 20;
            card.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(72, 72));
            iv.setBackgroundResource(R.drawable.bg_circle_mint);
            iv.setImageResource(R.drawable.ic_profile);
            iv.setPadding(14, 14, 14, 14);
            iv.setColorFilter(Color.parseColor("#102B27"));
            topRow.addView(iv);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);
            iLp.setMargins(20, 0, 0, 0);
            info.setLayoutParams(iLp);

            TextView tvName = new TextView(this);
            tvName.setText(child.getFullName());
            tvName.setTextSize(16);
            tvName.setTextColor(Color.parseColor("#102B27"));
            tvName.setTypeface(null, Typeface.BOLD);
            info.addView(tvName);

            TextView tvEmail = new TextView(this);
            tvEmail.setText(child.getEmail());
            tvEmail.setTextSize(12);
            tvEmail.setTextColor(Color.parseColor("#6B7C74"));
            info.addView(tvEmail);

            topRow.addView(info);

            TextView tvBal = new TextView(this);
            tvBal.setText(MillimesUtil.formatMillimes(child.getBalanceMillimes()));
            tvBal.setTextSize(16);
            tvBal.setTextColor(Color.parseColor("#102B27"));
            tvBal.setTypeface(null, Typeface.BOLD);
            topRow.addView(tvBal);

            card.addView(topRow);

            // Bouton vers profil et permissions
            Button btnDetails = new Button(this);
            btnDetails.setText("Gérer le profil & les plafonds ›");
            btnDetails.setTextSize(13);
            btnDetails.setBackgroundResource(R.drawable.bg_btn_secondary);
            btnDetails.setTextColor(Color.parseColor("#102B27"));
            LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 84);
            bLp.topMargin = 20;
            btnDetails.setLayoutParams(bLp);
            btnDetails.setOnClickListener(v -> {
                Intent intent = new Intent(FamilyChildrenActivity.this, ChildProfileActivity.class);
                intent.putExtra("child_id", child.getId());
                intent.putExtra("child_name", child.getFullName());
                startActivity(intent);
            });
            card.addView(btnDetails);

            layoutChildrenList.addView(card);
        }
    }

    private void showAddChildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter un compte enfant");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        EditText etName = new EditText(this);
        etName.setHint("Nom complet de l'enfant (ex: Adam)");
        layout.addView(etName);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Adresse e-mail (@gmail.com)");
        layout.addView(etEmail);

        EditText etPassword = new EditText(this);
        etPassword.setHint("Mot de passe (8 car., maj, chiffre, symbole)");
        etPassword.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPassword);

        EditText etInitAlloc = new EditText(this);
        etInitAlloc.setHint("Allocation initiale en DT (ex: 30.000)");
        etInitAlloc.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etInitAlloc);

        builder.setView(layout);
        builder.setPositiveButton("Créer le compte", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim().toLowerCase();
            String pwd = etPassword.getText().toString().trim();
            String allocStr = etInitAlloc.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Veuillez renseigner tous les champs obligatoires.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!AuthController.isValidGmail(email)) {
                Toast.makeText(this, "L'e-mail doit se terminer par @gmail.com", Toast.LENGTH_LONG).show();
                return;
            }

            if (!AuthController.isValidPasswordPolicy(pwd)) {
                Toast.makeText(this, "Le mot de passe doit contenir 8 caractères, 1 majuscule, 1 chiffre et 1 symbole.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (userDao.getUserByEmail(email) != null) {
                Toast.makeText(this, "Cet e-mail est déjà utilisé.", Toast.LENGTH_SHORT).show();
                return;
            }

            long allocMillimes = 0;
            if (!allocStr.isEmpty()) {
                try {
                    allocMillimes = MillimesUtil.dtToMillimes(Double.parseDouble(allocStr));
                } catch (Exception ignored) {
                }
            }

            long newId = familyDao.addChildWithCredentials(parentId, familyId, name, email, pwd, allocMillimes);
            if (newId > 0) {
                Toast.makeText(this, "Compte enfant créé avec succès !", Toast.LENGTH_SHORT).show();
                loadChildren();
            } else {
                Toast.makeText(this, "Erreur lors de la création du compte.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
}

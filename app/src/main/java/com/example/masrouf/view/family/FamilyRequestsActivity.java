package com.example.masrouf.view.family;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.model.MoneyRequest;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;
import com.example.masrouf.util.NotificationHelper;

import java.util.List;

/**
 * RÔLE : Gestion des demandes d'argent des enfants avec onglets et validation
 * atomique.
 * COUCHE MVC : Vue (View).
 */
public class FamilyRequestsActivity extends AppCompatActivity {

    private ImageView ivBackRequests;
    private TextView tabPending;
    private TextView tabApproved;
    private TextView tabRejected;
    private LinearLayout layoutRequestsList;

    private FamilyDao familyDao;
    private SessionManager sessionManager;
    private long familyId = 1;

    private String currentStatus = MoneyRequest.STATUS_PENDING;

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
        setContentView(R.layout.activity_family_requests);

        familyDao = new FamilyDao(this);
        sessionManager = new SessionManager(this);
        familyId = sessionManager.getFamilyId();

        ivBackRequests = findViewById(R.id.ivBackRequests);
        tabPending = findViewById(R.id.tabPending);
        tabApproved = findViewById(R.id.tabApproved);
        tabRejected = findViewById(R.id.tabRejected);
        layoutRequestsList = findViewById(R.id.layoutRequestsList);

        ivBackRequests.setOnClickListener(v -> finish());

        tabPending.setOnClickListener(v -> switchTab(MoneyRequest.STATUS_PENDING));
        tabApproved.setOnClickListener(v -> switchTab(MoneyRequest.STATUS_APPROVED));
        tabRejected.setOnClickListener(v -> switchTab(MoneyRequest.STATUS_REJECTED));

        loadRequests();
    }

    private void switchTab(String status) {
        this.currentStatus = status;

        tabPending.setBackground(null);
        tabPending.setTextColor(Color.parseColor("#102B27"));
        tabApproved.setBackground(null);
        tabApproved.setTextColor(Color.parseColor("#102B27"));
        tabRejected.setBackground(null);
        tabRejected.setTextColor(Color.parseColor("#102B27"));

        if (MoneyRequest.STATUS_PENDING.equals(status)) {
            tabPending.setBackgroundResource(R.drawable.bg_pill_active);
            tabPending.setTextColor(Color.WHITE);
        } else if (MoneyRequest.STATUS_APPROVED.equals(status)) {
            tabApproved.setBackgroundResource(R.drawable.bg_pill_active);
            tabApproved.setTextColor(Color.WHITE);
        } else {
            tabRejected.setBackgroundResource(R.drawable.bg_pill_active);
            tabRejected.setTextColor(Color.WHITE);
        }

        loadRequests();
    }

    private void loadRequests() {
        layoutRequestsList.removeAllViews();
        List<MoneyRequest> list = familyDao.getMoneyRequests(familyId, currentStatus);

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Aucune demande trouvée pour cette section.");
            empty.setTextColor(Color.parseColor("#6B7C74"));
            empty.setPadding(0, 32, 0, 32);
            layoutRequestsList.addView(empty);
            return;
        }

        for (MoneyRequest req : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_input_card);
            card.setPadding(36, 28, 36, 28);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 20;
            card.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvName = new TextView(this);
            tvName.setText(req.getChildName());
            tvName.setTextSize(16);
            tvName.setTextColor(Color.parseColor("#102B27"));
            tvName.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);
            tvName.setLayoutParams(nameLp);
            topRow.addView(tvName);

            TextView tvAmount = new TextView(this);
            tvAmount.setText(MillimesUtil.formatMillimes(req.getAmountMillimes()));
            tvAmount.setTextSize(17);
            tvAmount.setTextColor(Color.parseColor("#102B27"));
            tvAmount.setTypeface(null, Typeface.BOLD);
            topRow.addView(tvAmount);

            card.addView(topRow);

            TextView tvReason = new TextView(this);
            tvReason.setText("Motif : " + req.getReason() + " (" + req.getRequestDate() + ")");
            tvReason.setTextSize(13);
            tvReason.setTextColor(Color.parseColor("#6B7C74"));
            tvReason.setPadding(0, 8, 0, 12);
            card.addView(tvReason);

            if (req.getDecisionNote() != null && !req.getDecisionNote().isEmpty()) {
                TextView tvNote = new TextView(this);
                tvNote.setText("Note du parent : " + req.getDecisionNote());
                tvNote.setTextSize(12);
                tvNote.setTextColor(req.isApproved() ? Color.parseColor("#2D7D68") : Color.parseColor("#DE7363"));
                tvNote.setPadding(0, 0, 0, 12);
                card.addView(tvNote);
            }

            // Boutons d'action uniquement si en attente
            if (req.isPending()) {
                LinearLayout btnRow = new LinearLayout(this);
                btnRow.setOrientation(LinearLayout.HORIZONTAL);

                Button btnApprove = new Button(this);
                btnApprove.setText("✓ Approuver");
                btnApprove.setTextSize(12);
                btnApprove.setBackgroundResource(R.drawable.bg_btn_primary);
                btnApprove.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams bAppLp = new LinearLayout.LayoutParams(0, 76, 1.0f);
                // Définit les marges du bouton Approuver : 12px à droite pour séparer du bouton
                // Refuser
                bAppLp.setMargins(0, 0, 12, 0);
                btnApprove.setLayoutParams(bAppLp);
                btnApprove.setOnClickListener(v -> {
                    boolean ok = familyDao.approveMoneyRequest(req.getId(), "Validé par le parent");
                    if (ok) {
                        NotificationHelper.showBudgetAlert(
                                FamilyRequestsActivity.this,
                                "Demande d'argent approuvée",
                                "La demande de " + MillimesUtil.formatMillimes(req.getAmountMillimes()) + " pour " + req.getChildName() + " a été approuvée.",
                                false
                        );
                        Toast.makeText(FamilyRequestsActivity.this, "Demande approuvée avec succès !",
                                Toast.LENGTH_SHORT).show();
                        loadRequests();
                    } else {
                        Toast.makeText(FamilyRequestsActivity.this, "Fonds disponibles insuffisants.",
                                Toast.LENGTH_LONG).show();
                    }
                });
                btnRow.addView(btnApprove);

                Button btnReject = new Button(this);
                btnReject.setText("✕ Refuser");
                btnReject.setTextSize(12);
                btnReject.setBackgroundResource(R.drawable.bg_btn_danger);
                btnReject.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams bRejLp = new LinearLayout.LayoutParams(0, 76, 1.0f);
                btnReject.setLayoutParams(bRejLp);
                btnReject.setOnClickListener(v -> showRejectDialog(req));
                btnRow.addView(btnReject);

                card.addView(btnRow);
            }

            layoutRequestsList.addView(card);
        }
    }

    private void showRejectDialog(MoneyRequest req) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Refuser la demande");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        EditText etNote = new EditText(this);
        etNote.setHint("Motif du refus (facultatif)");
        layout.addView(etNote);

        builder.setView(layout);
        builder.setPositiveButton("Confirmer le refus", (dialog, which) -> {
            String note = etNote.getText().toString().trim();
            String noteText = note.isEmpty() ? "Refusé par le parent" : note;
            familyDao.rejectMoneyRequest(req.getId(), noteText);
            NotificationHelper.showBudgetAlert(
                    FamilyRequestsActivity.this,
                    "Demande d'argent refusée",
                    "La demande de " + MillimesUtil.formatMillimes(req.getAmountMillimes()) + " pour " + req.getChildName() + " a été refusée (" + noteText + ").",
                    false
            );
            Toast.makeText(this, "Demande refusée.", Toast.LENGTH_SHORT).show();
            loadRequests();
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
}

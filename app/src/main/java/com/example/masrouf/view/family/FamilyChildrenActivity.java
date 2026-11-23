package com.example.masrouf.view.family;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.masrouf.BaseActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.dao.NotificationDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;
import com.example.masrouf.view.NotificationsActivity;
import com.example.masrouf.view.SettingsActivity;
import com.example.masrouf.view.admin.AdminDrawerHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Gestion des comptes Enfants pour le profil Parent (Images 1, 5, 6, 7, 8).
 * 
 * - Top bar avec retour, titre "Enfants", cloche avec badge et avatar profil.
 * - Sous-titre "FOYER & FAMILLE · GESTION DES ENFANTS".
 * - Ligne de titre avec "Mes Enfants (X)" et bouton "+ Ajouter un enfant" en capsule noire.
 * - État vide dynamique conforme à l'Image 1 avec icône et bouton vert.
 * - Fiches enfants dynamiques conformes à l'Image 7 (Alloué, Dépensé corail, Restant, barre de progression,
 *   bouton "Donner", bouton "Réinitialiser mot de passe", bouton "Progression & Comparaison des mois").
 * - Modales d'ajout, de confirmation des identifiants et d'allocation depuis l'enveloppe Enfant (Images 5, 6, 8).
 */
public class FamilyChildrenActivity extends BaseActivity {

    private DrawerLayout drawerLayoutChildren;
    private ImageView ivBackChildren;
    private FrameLayout frameNotifChildren;
    private TextView tvChildrenNotifBadge;
    private ImageView ivChildrenProfile;

    private TextView tvChildrenCountTitle;
    private Button btnAddNewChildTop;

    private LinearLayout cardEmptyChildren;
    private Button btnAddChildEmpty;
    private LinearLayout layoutChildrenList;

    private FamilyDao familyDao;
    private UserDao userDao;
    private NotificationDao notificationDao;
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
        notificationDao = new NotificationDao(this);

        familyId = sessionManager.getFamilyId();
        parentId = sessionManager.getUserId();

        initViews();
        setupListeners();
        loadChildren();
    }

    private void initViews() {
        drawerLayoutChildren = findViewById(R.id.drawerLayoutChildren);
        ivBackChildren = findViewById(R.id.ivBackChildren);
        frameNotifChildren = findViewById(R.id.frameNotifChildren);
        tvChildrenNotifBadge = findViewById(R.id.tvChildrenNotifBadge);
        ivChildrenProfile = findViewById(R.id.ivChildrenProfile);

        tvChildrenCountTitle = findViewById(R.id.tvChildrenCountTitle);
        btnAddNewChildTop = findViewById(R.id.btnAddNewChildTop);

        cardEmptyChildren = findViewById(R.id.cardEmptyChildren);
        btnAddChildEmpty = findViewById(R.id.btnAddChildEmpty);
        layoutChildrenList = findViewById(R.id.layoutChildrenList);

        if (drawerLayoutChildren != null) {
            AdminDrawerHelper.setupDrawer(this, drawerLayoutChildren, AdminDrawerHelper.Section.CHILDREN);
        }
    }

    private void setupListeners() {
        if (ivBackChildren != null) {
            ivBackChildren.setOnClickListener(v -> finish());
        }

        if (frameNotifChildren != null) {
            frameNotifChildren.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        }

        if (ivChildrenProfile != null) {
            ivChildrenProfile.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }

        if (btnAddNewChildTop != null) {
            btnAddNewChildTop.setOnClickListener(v -> showAddChildModal());
        }

        if (btnAddChildEmpty != null) {
            btnAddChildEmpty.setOnClickListener(v -> showAddChildModal());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildren();
    }

    private void loadChildren() {
        if (layoutChildrenList == null) return;

        // Mise à jour badge notifications
        if (tvChildrenNotifBadge != null) {
            int unread = notificationDao.getUnreadCount(parentId);
            if (unread > 0) {
                tvChildrenNotifBadge.setVisibility(View.VISIBLE);
                tvChildrenNotifBadge.setText(String.valueOf(unread));
            } else {
                tvChildrenNotifBadge.setVisibility(View.GONE);
            }
        }

        layoutChildrenList.removeAllViews();
        List<User> children = userDao.getChildrenByParent(parentId);
        if (children.isEmpty()) {
            children = userDao.getChildrenByFamily(familyId);
        }

        int count = children.size();
        if (tvChildrenCountTitle != null) {
            tvChildrenCountTitle.setText("Mes Enfants (" + count + ")");
        }

        if (count == 0) {
            // État vide conforme à l'Image 1
            if (cardEmptyChildren != null) cardEmptyChildren.setVisibility(View.VISIBLE);
            layoutChildrenList.setVisibility(View.GONE);
            return;
        }

        // État actif conforme à l'Image 7
        if (cardEmptyChildren != null) cardEmptyChildren.setVisibility(View.GONE);
        layoutChildrenList.setVisibility(View.VISIBLE);

        String currentMonthKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        for (final User child : children) {
            View card = createChildCard(child, currentMonthKey);
            layoutChildrenList.addView(card);
        }
    }

    private View createChildCard(final User child, final String monthKey) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_family_child_card, layoutChildrenList, false);

        TextView tvChildName = view.findViewById(R.id.tvChildName);
        TextView tvChildEmail = view.findViewById(R.id.tvChildEmail);
        TextView tvChildCreatedAt = view.findViewById(R.id.tvChildCreatedAt);

        TextView tvChildAllocated = view.findViewById(R.id.tvChildAllocated);
        TextView tvChildSpent = view.findViewById(R.id.tvChildSpent);
        TextView tvChildRemaining = view.findViewById(R.id.tvChildRemaining);

        ProgressBar pbChildUsage = view.findViewById(R.id.pbChildUsage);
        TextView tvChildProgressPct = view.findViewById(R.id.tvChildProgressPct);

        Button btnGiveMoney = view.findViewById(R.id.btnGiveMoney);
        Button btnResetPwd = view.findViewById(R.id.btnResetPwd);
        Button btnProgressHistory = view.findViewById(R.id.btnProgressHistory);

        tvChildName.setText(child.getFullName());
        tvChildEmail.setText(child.getEmail());

        String createdAtStr = child.getCreatedAt();
        String dateFormatted = "2026-11-23";
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            try {
                long ts = Long.parseLong(createdAtStr);
                dateFormatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(ts));
            } catch (Exception ignored) {
                dateFormatted = createdAtStr;
            }
        }
        tvChildCreatedAt.setText("Inscrit le " + dateFormatted);

        // Données financières réelles depuis SQLite
        long allocated = familyDao.getChildTotalAllowancesMillimes(child.getId(), monthKey);
        long spent = familyDao.getChildMonthlySpendingMillimes(child.getId(), monthKey);
        long remaining = Math.max(0, allocated - spent);
        if (allocated == 0 && child.getBalanceMillimes() > 0) {
            remaining = child.getBalanceMillimes();
        }

        tvChildAllocated.setText(MillimesUtil.formatMillimes(allocated));
        tvChildSpent.setText(MillimesUtil.formatMillimes(spent));
        tvChildRemaining.setText(MillimesUtil.formatMillimes(remaining));

        double pct = allocated > 0 ? ((double) spent / allocated) * 100.0 : 0.0;
        int pctInt = (int) Math.min(100, Math.round(pct));
        pbChildUsage.setProgress(pctInt);
        tvChildProgressPct.setText(pctInt + "% consommé");

        if (pct > 100) {
            pbChildUsage.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#DC7770")));
        } else if (pct >= 80) {
            pbChildUsage.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#E9B66B")));
        } else {
            pbChildUsage.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#112B28")));
        }

        // Actions
        btnGiveMoney.setOnClickListener(v -> showGiveMoneyDialog(child));
        btnResetPwd.setOnClickListener(v -> showResetPasswordDialog(child));
        btnProgressHistory.setOnClickListener(v -> {
            Intent intent = new Intent(FamilyChildrenActivity.this, FamilyAnalyticsActivity.class);
            startActivity(intent);
        });

        return view;
    }

    /**
     * Modale d'ajout d'enfant (Image 5).
     */
    private void showAddChildModal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        final EditText etName = dialogView.findViewById(R.id.etAddChildName);
        final EditText etEmail = dialogView.findViewById(R.id.etAddChildEmail);
        final EditText etPassword = dialogView.findViewById(R.id.etAddChildPassword);
        ImageView ivClose = dialogView.findViewById(R.id.ivCloseAddChild);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitAddChild);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelAddChild);

        if (ivClose != null) ivClose.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pwd = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Saisissez une adresse email valide.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pwd.length() < 8) {
                Toast.makeText(this, "Le mot de passe doit comporter au moins 8 caractères.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userDao.findByEmail(email) != null) {
                Toast.makeText(this, "Un compte avec cette adresse email existe déjà.", Toast.LENGTH_SHORT).show();
                return;
            }

            long newChildId = familyDao.addChildWithCredentials(parentId, familyId, name, email, pwd, 0);
            if (newChildId > 0) {
                dialog.dismiss();
                showCredentialsConfirmationModal(name, email, pwd);
                loadChildren();
            } else {
                Toast.makeText(this, "Erreur lors de la création du compte.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /**
     * Modale de confirmation du compte enfant créé (Image 6).
     */
    private void showCredentialsConfirmationModal(final String name, final String email, final String pwd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_child_created, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvName = dialogView.findViewById(R.id.tvCreatedChildName);
        TextView tvEmail = dialogView.findViewById(R.id.tvCreatedChildEmail);
        TextView tvPassword = dialogView.findViewById(R.id.tvCreatedChildPassword);
        Button btnClose = dialogView.findViewById(R.id.btnCloseChildCreated);

        if (tvName != null) tvName.setText(name);
        if (tvEmail != null) tvEmail.setText(email);
        if (tvPassword != null) tvPassword.setText(pwd);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * Modale "Allouer à [childName]" depuis l'enveloppe Enfant (Image 8).
     */
    private void showGiveMoneyDialog(final User child) {
        BudgetAllocation alloc = familyDao.getEnfantCategoryAllocation(parentId);
        long allocatedEnvelope = 0;
        long availableEnvelope = 0;

        if (alloc != null) {
            allocatedEnvelope = alloc.getAllocatedMillimes() > 0 ? alloc.getAllocatedMillimes() : MillimesUtil.dtToMillimes(alloc.getAllocatedAmount());
            availableEnvelope = familyDao.getEnfantEnvelopeAvailableMillimes(parentId);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_allocate_child, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvAllocateTitle);
        TextView tvSubtitle = dialogView.findViewById(R.id.tvAllocateSubtitleEnvelope);
        ImageView ivClose = dialogView.findViewById(R.id.ivCloseAllocate);
        final EditText etAmount = dialogView.findViewById(R.id.etAllocateAmount);
        final EditText etNote = dialogView.findViewById(R.id.etAllocateNote);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmAllocation);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelAllocation);

        if (tvTitle != null) {
            tvTitle.setText("Allouer à " + child.getFullName());
        }

        if (tvSubtitle != null) {
            tvSubtitle.setText("Budget catégorie Enfant : " + MillimesUtil.formatMillimes(allocatedEnvelope) +
                    " · Disponible : " + MillimesUtil.formatMillimes(availableEnvelope));
        }

        if (ivClose != null) ivClose.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        final long maxAvail = availableEnvelope;
        btnConfirm.setOnClickListener(v -> {
            String amtStr = etAmount.getText().toString().trim();
            if (amtStr.isEmpty()) {
                Toast.makeText(this, "Veuillez saisir un montant.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double dtVal = Double.parseDouble(amtStr);
                long millimes = MillimesUtil.dtToMillimes(dtVal);

                if (millimes <= 0) {
                    Toast.makeText(this, "Le montant doit être supérieur à 0.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (maxAvail > 0 && millimes > maxAvail) {
                    Toast.makeText(this, "Montant supérieur à l'enveloppe disponible Enfant (" +
                            MillimesUtil.formatMillimes(maxAvail) + ").", Toast.LENGTH_LONG).show();
                    return;
                }

                String note = etNote.getText().toString().trim();
                boolean success = familyDao.allocateMoneyToChild(familyId, parentId, child.getId(),
                        child.getFullName(), millimes, note.isEmpty() ? "Allocation argent de poche" : note);

                if (success) {
                    // Enregistrer notification pour le parent et pour l'enfant
                    notificationDao.insertNotification(parentId,
                            "Allocation transmise",
                            "Vous avez alloué " + MillimesUtil.formatMillimes(millimes) + " à " + child.getFullName() + ".",
                            "ALLOCATION", "ROUTE_CHILDREN");

                    notificationDao.insertNotification(child.getId(),
                            "Budget reçu de votre parent",
                            "Votre parent vous a alloué " + MillimesUtil.formatMillimes(millimes) + ".",
                            "PARENT_BUDGET", "ROUTE_BUDGET");

                    dialog.dismiss();
                    Toast.makeText(this, "Allocation confirmée avec succès !", Toast.LENGTH_SHORT).show();
                    loadChildren();
                } else {
                    Toast.makeText(this, "Erreur lors de l'allocation.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Montant invalide.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /**
     * Réinitialiser le mot de passe d'un enfant.
     */
    private void showResetPasswordDialog(final User child) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundResource(R.drawable.bg_bottom_sheet);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Réinitialiser le mot de passe");
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(Color.parseColor("#112B28"));
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText("Saisissez un nouveau mot de passe pour " + child.getFullName() + ".");
        tvSub.setTextColor(Color.parseColor("#61746D"));
        tvSub.setTextSize(13);
        tvSub.setPadding(0, 4, 0, 16);
        layout.addView(tvSub);

        final EditText etPwd = new EditText(this);
        etPwd.setHint("Nouveau mot de passe (8+ caractères)");
        etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPwd.setBackgroundResource(R.drawable.bg_input_card);
        etPwd.setPadding(28, 24, 28, 24);
        layout.addView(etPwd);

        Button btnConfirm = new Button(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 110);
        lp.topMargin = 20;
        btnConfirm.setLayoutParams(lp);
        btnConfirm.setBackgroundResource(R.drawable.bg_btn_primary);
        btnConfirm.setText("Confirmer la réinitialisation");
        btnConfirm.setTextColor(Color.WHITE);
        btnConfirm.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(btnConfirm);

        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnConfirm.setOnClickListener(v -> {
            String pwd = etPwd.getText().toString().trim();
            if (pwd.length() < 8) {
                Toast.makeText(this, "Au moins 8 caractères requis.", Toast.LENGTH_SHORT).show();
                return;
            }

            userDao.resetPassword(child.getId(), pwd);
            dialog.dismiss();
            Toast.makeText(this, "Mot de passe réinitialisé pour " + child.getFullName() + ".", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}

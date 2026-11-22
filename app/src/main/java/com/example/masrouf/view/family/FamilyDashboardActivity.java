package com.example.masrouf.view.family;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.masrouf.MainActivity;
import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.FamilyFunds;
import com.example.masrouf.model.MoneyRequest;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.MillimesUtil;
import com.example.masrouf.view.LoginActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Tableau de bord familial pour le profil Parent.
 * COUCHE MVC : Vue (View).
 * Fonctionnalités :
 * - Aperçu des fonds globaux, disponibles et alloués de la famille.
 * - Suivi des soldes et dépenses des enfants.
 * - Traitement en 1 clic (Approuver / Refuser) des demandes d'argent.
 * - Transfert d'argent atomique aux enfants.
 * - Tiroir de navigation dédié et bascule d'espace vers l'Espace personnel.
 */
public class FamilyDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayoutFamily;
    private ImageView ivFamilyMenu;
    private TextView tvFamilyTitle;
    private TextView tvFamilyNameSub;
    private TextView btnSwitchToPersonal;
    private FrameLayout frameNotifFamily;
    private ImageView ivFamilyProfile;

    private TextView tvAvailableFunds;
    private TextView tvAllocatedFunds;
    private TextView tvTotalFunds;
    private Button btnQuickGiveMoney;

    private LinearLayout layoutChildrenContainer;
    private TextView btnAddChildLink;

    private LinearLayout layoutRequestsContainer;
    private TextView btnViewAllRequests;

    private LinearLayout layoutChildExpensesContainer;

    // Drawer views
    private TextView tvDrawerParentName;
    private LinearLayout btnDrawerSwitchPersonal;
    private LinearLayout btnNavFamilyDashboard;
    private LinearLayout btnNavFamilyChildren;
    private LinearLayout btnNavFamilyFinances;
    private LinearLayout btnNavFamilyRequests;
    private LinearLayout btnNavFamilyAnalytics;
    private LinearLayout btnNavFamilyHistory;
    private LinearLayout btnNavFamilyPrivacy;
    private TextView btnDrawerFamilyLangAr;
    private TextView btnDrawerFamilyLangFr;
    private TextView btnDrawerFamilyLangEn;
    private LinearLayout btnDrawerFamilyLogout;

    private FamilyDao familyDao;
    private UserDao userDao;
    private SessionManager sessionManager;

    private long currentFamilyId = 1;
    private long currentParentId = 1;
    private List<User> familyChildren = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isChild()) {
            Toast.makeText(this, "Accès réservé : Seul votre père peut accéder au tableau de bord familial.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_family_dashboard);

        sessionManager = new SessionManager(this);
        familyDao = new FamilyDao(this);
        userDao = new UserDao(this);

        currentParentId = sessionManager.getUserId();
        currentFamilyId = sessionManager.getFamilyId();

        initViews();
        setupNavigationDrawer();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initViews() {
        drawerLayoutFamily = findViewById(R.id.drawerLayoutFamily);
        ivFamilyMenu = findViewById(R.id.ivFamilyMenu);
        tvFamilyTitle = findViewById(R.id.tvFamilyTitle);
        tvFamilyNameSub = findViewById(R.id.tvFamilyNameSub);
        btnSwitchToPersonal = findViewById(R.id.btnSwitchToPersonal);
        frameNotifFamily = findViewById(R.id.frameNotifFamily);
        ivFamilyProfile = findViewById(R.id.ivFamilyProfile);

        tvAvailableFunds = findViewById(R.id.tvAvailableFunds);
        tvAllocatedFunds = findViewById(R.id.tvAllocatedFunds);
        tvTotalFunds = findViewById(R.id.tvTotalFunds);
        btnQuickGiveMoney = findViewById(R.id.btnQuickGiveMoney);

        layoutChildrenContainer = findViewById(R.id.layoutChildrenContainer);
        btnAddChildLink = findViewById(R.id.btnAddChildLink);

        layoutRequestsContainer = findViewById(R.id.layoutRequestsContainer);
        btnViewAllRequests = findViewById(R.id.btnViewAllRequests);

        layoutChildExpensesContainer = findViewById(R.id.layoutChildExpensesContainer);

        // Drawer
        tvDrawerParentName = findViewById(R.id.tvDrawerParentName);
        btnDrawerSwitchPersonal = findViewById(R.id.btnDrawerSwitchPersonal);
        btnNavFamilyDashboard = findViewById(R.id.btnNavFamilyDashboard);
        btnNavFamilyChildren = findViewById(R.id.btnNavFamilyChildren);
        btnNavFamilyFinances = findViewById(R.id.btnNavFamilyFinances);
        btnNavFamilyRequests = findViewById(R.id.btnNavFamilyRequests);
        btnNavFamilyAnalytics = findViewById(R.id.btnNavFamilyAnalytics);
        btnNavFamilyHistory = findViewById(R.id.btnNavFamilyHistory);
        btnNavFamilyPrivacy = findViewById(R.id.btnNavFamilyPrivacy);
        btnDrawerFamilyLangAr = findViewById(R.id.btnDrawerFamilyLangAr);
        btnDrawerFamilyLangFr = findViewById(R.id.btnDrawerFamilyLangFr);
        btnDrawerFamilyLangEn = findViewById(R.id.btnDrawerFamilyLangEn);
        btnDrawerFamilyLogout = findViewById(R.id.btnDrawerFamilyLogout);

        if (sessionManager.getUserName() != null && !sessionManager.getUserName().isEmpty()) {
            tvDrawerParentName.setText(sessionManager.getUserName());
        }
    }

    private void setupNavigationDrawer() {
        // Menu burger -> ouvre le tiroir
        ivFamilyMenu.setOnClickListener(v -> {
            if (drawerLayoutFamily.isDrawerOpen(GravityCompat.START)) {
                drawerLayoutFamily.closeDrawer(GravityCompat.START);
            } else {
                drawerLayoutFamily.openDrawer(GravityCompat.START);
            }
        });

        // Bascule vers l'espace personnel
        View.OnClickListener switchSpaceListener = v -> {
            drawerLayoutFamily.closeDrawer(GravityCompat.START);
            sessionManager.setActiveSpace(SessionManager.SPACE_PERSONAL);
            startActivity(new Intent(FamilyDashboardActivity.this, MainActivity.class));
            finish();
        };

        btnSwitchToPersonal.setOnClickListener(switchSpaceListener);
        btnDrawerSwitchPersonal.setOnClickListener(switchSpaceListener);

        // Liens du tiroir
        btnNavFamilyDashboard.setOnClickListener(v -> drawerLayoutFamily.closeDrawer(GravityCompat.START));

        btnNavFamilyChildren.setOnClickListener(v -> {
            drawerLayoutFamily.closeDrawer(GravityCompat.START);
            startActivity(new Intent(FamilyDashboardActivity.this, FamilyChildrenActivity.class));
        });

        btnNavFamilyFinances.setOnClickListener(v -> {
            drawerLayoutFamily.closeDrawer(GravityCompat.START);
            startActivity(new Intent(FamilyDashboardActivity.this, FamilyFinancesActivity.class));
        });

        btnNavFamilyRequests.setOnClickListener(v -> {
            drawerLayoutFamily.closeDrawer(GravityCompat.START);
            startActivity(new Intent(FamilyDashboardActivity.this, FamilyRequestsActivity.class));
        });

        btnNavFamilyAnalytics.setOnClickListener(v -> {
            drawerLayoutFamily.closeDrawer(GravityCompat.START);
            startActivity(new Intent(FamilyDashboardActivity.this, FamilyAnalyticsActivity.class));
        });

        btnNavFamilyHistory.setOnClickListener(v -> {
            drawerLayoutFamily.closeDrawer(GravityCompat.START);
            startActivity(new Intent(FamilyDashboardActivity.this, FamilyHistoryActivity.class));
        });

        btnNavFamilyPrivacy.setOnClickListener(v -> {
            drawerLayoutFamily.closeDrawer(GravityCompat.START);
            startActivity(new Intent(FamilyDashboardActivity.this, FamilyPrivacyActivity.class));
        });

        // Déconnexion
        btnDrawerFamilyLogout.setOnClickListener(v -> {
            drawerLayoutFamily.closeDrawer(GravityCompat.START);
            sessionManager.clearSession();
            Intent intent = new Intent(FamilyDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Sélecteur de langue
        updateDrawerLanguage(sessionManager.getLanguage());
        if (btnDrawerFamilyLangAr != null)
            btnDrawerFamilyLangAr.setOnClickListener(v -> changeAppLanguage("ar"));
        if (btnDrawerFamilyLangFr != null)
            btnDrawerFamilyLangFr.setOnClickListener(v -> changeAppLanguage("fr"));
        if (btnDrawerFamilyLangEn != null)
            btnDrawerFamilyLangEn.setOnClickListener(v -> changeAppLanguage("en"));
    }

    private void updateDrawerLanguage(String lang) {
        if (btnDrawerFamilyLangAr == null)
            return;
        btnDrawerFamilyLangAr.setBackground(null);
        btnDrawerFamilyLangFr.setBackground(null);
        btnDrawerFamilyLangEn.setBackground(null);
        btnDrawerFamilyLangAr.setTextColor(Color.parseColor("#A0C8BC"));
        btnDrawerFamilyLangFr.setTextColor(Color.parseColor("#A0C8BC"));
        btnDrawerFamilyLangEn.setTextColor(Color.parseColor("#A0C8BC"));

        if ("ar".equalsIgnoreCase(lang)) {
            btnDrawerFamilyLangAr.setBackgroundResource(R.drawable.bg_pill_active);
            btnDrawerFamilyLangAr.setTextColor(Color.WHITE);
        } else if ("en".equalsIgnoreCase(lang)) {
            btnDrawerFamilyLangEn.setBackgroundResource(R.drawable.bg_pill_active);
            btnDrawerFamilyLangEn.setTextColor(Color.WHITE);
        } else {
            btnDrawerFamilyLangFr.setBackgroundResource(R.drawable.bg_pill_active);
            btnDrawerFamilyLangFr.setTextColor(Color.WHITE);
        }
    }

    private void changeAppLanguage(String code) {
        sessionManager.saveLanguage(code);
        LocaleHelper.setLocale(this, code);
        recreate();
    }

    private void setupListeners() {
        btnQuickGiveMoney.setOnClickListener(v -> showGiveMoneyDialog(null));

        btnAddChildLink.setOnClickListener(v -> {
            startActivity(new Intent(FamilyDashboardActivity.this, FamilyChildrenActivity.class));
        });

        btnViewAllRequests.setOnClickListener(v -> {
            startActivity(new Intent(FamilyDashboardActivity.this, FamilyRequestsActivity.class));
        });
    }

    private void loadDashboardData() {
        // 1. Fonds
        FamilyFunds funds = familyDao.getFamilyFunds(currentFamilyId);
        if (funds == null) {
            funds = familyDao.getFamilyFundsByParent(currentParentId);
        }
        if (funds != null) {
            tvFamilyNameSub.setText(funds.getFamilyName());
            tvAvailableFunds.setText(MillimesUtil.formatMillimes(funds.getAvailableFundsMillimes()));
            tvAllocatedFunds.setText(MillimesUtil.formatMillimes(funds.getAllocatedFundsMillimes()));
            tvTotalFunds.setText(MillimesUtil.formatMillimes(funds.getTotalFundsMillimes()));
        }

        // 2. Enfants
        familyChildren = userDao.getChildrenByFamily(currentFamilyId);
        if (familyChildren.isEmpty()) {
            familyChildren = userDao.getChildrenByParent(currentParentId);
        }
        renderChildrenCards();

        // 3. Demandes d'argent récentes
        renderPendingRequests();

        // 4. Dernières dépenses enfants
        renderChildExpenses();
    }

    private void renderChildrenCards() {
        layoutChildrenContainer.removeAllViews();
        if (familyChildren == null || familyChildren.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Aucun compte enfant configuré.");
            tvEmpty.setTextColor(Color.parseColor("#6B7C74"));
            tvEmpty.setPadding(0, 16, 0, 16);
            layoutChildrenContainer.addView(tvEmpty);
            return;
        }

        for (User child : familyChildren) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_input_card);
            card.setPadding(36, 32, 36, 32);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 24;
            card.setLayoutParams(lp);

            // Rangée 1 : Avatar, Nom, Solde
            LinearLayout row1 = new LinearLayout(this);
            row1.setOrientation(LinearLayout.HORIZONTAL);
            row1.setGravity(android.view.Gravity.CENTER_VERTICAL);

            ImageView ivAvatar = new ImageView(this);
            ivAvatar.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
            ivAvatar.setBackgroundResource(R.drawable.bg_circle_mint);
            ivAvatar.setImageResource(R.drawable.ic_profile);
            ivAvatar.setPadding(16, 16, 16, 16);
            ivAvatar.setColorFilter(Color.parseColor("#102B27"));
            row1.addView(ivAvatar);

            LinearLayout infoLayout = new LinearLayout(this);
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);
            infoLp.setMargins(24, 0, 0, 0);
            infoLayout.setLayoutParams(infoLp);

            TextView tvName = new TextView(this);
            tvName.setText(child.getFullName());
            tvName.setTextSize(16);
            tvName.setTextColor(Color.parseColor("#102B27"));
            tvName.setTypeface(null, Typeface.BOLD);
            infoLayout.addView(tvName);

            TextView tvRole = new TextView(this);
            tvRole.setText("Compte Enfant · " + child.getEmail());
            tvRole.setTextSize(12);
            tvRole.setTextColor(Color.parseColor("#6B7C74"));
            infoLayout.addView(tvRole);

            row1.addView(infoLayout);

            // Solde
            LinearLayout balanceLayout = new LinearLayout(this);
            balanceLayout.setOrientation(LinearLayout.VERTICAL);
            balanceLayout.setGravity(android.view.Gravity.END);

            TextView tvBalLabel = new TextView(this);
            tvBalLabel.setText("Solde actuel");
            tvBalLabel.setTextSize(11);
            tvBalLabel.setTextColor(Color.parseColor("#6B7C74"));
            balanceLayout.addView(tvBalLabel);

            TextView tvBalValue = new TextView(this);
            tvBalValue.setText(MillimesUtil.formatMillimes(child.getBalanceMillimes()));
            tvBalValue.setTextSize(16);
            tvBalValue.setTextColor(Color.parseColor("#102B27"));
            tvBalValue.setTypeface(null, Typeface.BOLD);
            balanceLayout.addView(tvBalValue);

            row1.addView(balanceLayout);
            card.addView(row1);

            // Boutons d'action : Donner de l'argent & Voir profil
            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            btnRow.setPadding(0, 20, 0, 0);

            Button btnGive = new Button(this);
            btnGive.setText("＋ Allouer");
            btnGive.setTextSize(13);
            btnGive.setBackgroundResource(R.drawable.bg_btn_secondary);
            btnGive.setTextColor(Color.parseColor("#102B27"));
            LinearLayout.LayoutParams btnGiveLp = new LinearLayout.LayoutParams(0, 84, 1.0f);
            btnGiveLp.setMargins(0, 0, 12, 0);
            btnGive.setLayoutParams(btnGiveLp);
            btnGive.setOnClickListener(v -> showGiveMoneyDialog(child));
            btnRow.addView(btnGive);

            Button btnProfile = new Button(this);
            btnProfile.setText("Profil & Limites");
            btnProfile.setTextSize(13);
            btnProfile.setBackgroundResource(R.drawable.bg_input_card);
            btnProfile.setTextColor(Color.parseColor("#102B27"));
            LinearLayout.LayoutParams btnProfLp = new LinearLayout.LayoutParams(0, 84, 1.0f);
            btnProfile.setLayoutParams(btnProfLp);
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(FamilyDashboardActivity.this, ChildProfileActivity.class);
                intent.putExtra("child_id", child.getId());
                intent.putExtra("child_name", child.getFullName());
                startActivity(intent);
            });
            btnRow.addView(btnProfile);

            card.addView(btnRow);
            layoutChildrenContainer.addView(card);
        }
    }

    private void renderPendingRequests() {
        layoutRequestsContainer.removeAllViews();
        List<MoneyRequest> pending = familyDao.getPendingMoneyRequests(currentFamilyId);
        if (pending == null || pending.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Aucune demande en attente.");
            tvEmpty.setTextColor(Color.parseColor("#6B7C74"));
            tvEmpty.setPadding(0, 16, 0, 16);
            layoutRequestsContainer.addView(tvEmpty);
            return;
        }

        for (MoneyRequest req : pending) {
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
            topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvName = new TextView(this);
            tvName.setText(req.getChildName());
            tvName.setTextSize(15);
            tvName.setTextColor(Color.parseColor("#102B27"));
            tvName.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);
            tvName.setLayoutParams(nameLp);
            topRow.addView(tvName);

            TextView tvAmount = new TextView(this);
            tvAmount.setText(MillimesUtil.formatMillimes(req.getAmountMillimes()));
            tvAmount.setTextSize(16);
            tvAmount.setTextColor(Color.parseColor("#102B27"));
            tvAmount.setTypeface(null, Typeface.BOLD);
            topRow.addView(tvAmount);

            card.addView(topRow);

            TextView tvReason = new TextView(this);
            tvReason.setText("Motif : " + req.getReason());
            tvReason.setTextSize(13);
            tvReason.setTextColor(Color.parseColor("#6B7C74"));
            tvReason.setPadding(0, 8, 0, 16);
            card.addView(tvReason);

            // Boutons Approuver et Refuser
            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);

            Button btnApprove = new Button(this);
            btnApprove.setText("✓ Approuver");
            btnApprove.setTextSize(12);
            btnApprove.setBackgroundResource(R.drawable.bg_btn_primary);
            btnApprove.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams bAppLp = new LinearLayout.LayoutParams(0, 76, 1.0f);
            bAppLp.setMargins(0, 0, 12, 0);
            btnApprove.setLayoutParams(bAppLp);
            btnApprove.setOnClickListener(v -> {
                boolean ok = familyDao.approveMoneyRequest(req.getId(), "Validé par le parent");
                if (ok) {
                    Toast.makeText(FamilyDashboardActivity.this, "Demande approuvée avec succès !", Toast.LENGTH_SHORT)
                            .show();
                    loadDashboardData();
                } else {
                    Toast.makeText(FamilyDashboardActivity.this, "Échec : vérifiez les fonds disponibles.",
                            Toast.LENGTH_SHORT).show();
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
            btnReject.setOnClickListener(v -> {
                familyDao.rejectMoneyRequest(req.getId(), "Refusé par le parent");
                Toast.makeText(FamilyDashboardActivity.this, "Demande refusée.", Toast.LENGTH_SHORT).show();
                loadDashboardData();
            });
            btnRow.addView(btnReject);

            card.addView(btnRow);
            layoutRequestsContainer.addView(card);
        }
    }

    private void renderChildExpenses() {
        layoutChildExpensesContainer.removeAllViews();
        List<Transaction> allExpenses = new ArrayList<>();
        if (familyChildren != null) {
            for (User c : familyChildren) {
                allExpenses.addAll(familyDao.getChildTransactions(c.getId()));
            }
        }

        if (allExpenses.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Aucune dépense récente.");
            tvEmpty.setTextColor(Color.parseColor("#6B7C74"));
            tvEmpty.setPadding(0, 16, 0, 16);
            layoutChildExpensesContainer.addView(tvEmpty);
            return;
        }

        int count = 0;
        for (Transaction t : allExpenses) {
            if (count++ >= 5)
                break; // Afficher les 5 plus récentes

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setBackgroundResource(R.drawable.bg_input_card);
            item.setPadding(32, 24, 32, 24);
            item.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 14;
            item.setLayoutParams(lp);

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(64, 64));
            iv.setBackgroundResource(R.drawable.bg_circle_mint);
            iv.setImageResource(R.drawable.ic_receipt);
            iv.setPadding(14, 14, 14, 14);
            iv.setColorFilter(Color.parseColor("#102B27"));
            item.addView(iv);

            LinearLayout details = new LinearLayout(this);
            details.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);
            dLp.setMargins(20, 0, 0, 0);
            details.setLayoutParams(dLp);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(t.getTitle());
            tvTitle.setTextSize(14);
            tvTitle.setTextColor(Color.parseColor("#102B27"));
            tvTitle.setTypeface(null, Typeface.BOLD);
            details.addView(tvTitle);

            TextView tvSub = new TextView(this);
            tvSub.setText(t.getDate() + (t.getStore() != null && !t.getStore().isEmpty() ? " · " + t.getStore() : ""));
            tvSub.setTextSize(12);
            tvSub.setTextColor(Color.parseColor("#6B7C74"));
            details.addView(tvSub);

            item.addView(details);

            TextView tvPrice = new TextView(this);
            tvPrice.setText("-" + MillimesUtil.formatMillimes(t.getAmountMillimes()));
            tvPrice.setTextSize(15);
            tvPrice.setTextColor(Color.parseColor("#102B27"));
            tvPrice.setTypeface(null, Typeface.BOLD);
            item.addView(tvPrice);

            layoutChildExpensesContainer.addView(item);
        }
    }

    private void showGiveMoneyDialog(User preselectedChild) {
        if (familyChildren == null || familyChildren.isEmpty()) {
            Toast.makeText(this, "Aucun enfant enregistré dans la famille.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Donner de l'argent");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        TextView lblChild = new TextView(this);
        lblChild.setText("Sélectionner l'enfant :");
        lblChild.setTextColor(Color.parseColor("#102B27"));
        lblChild.setTypeface(null, Typeface.BOLD);
        layout.addView(lblChild);

        Spinner spChild = new Spinner(this);
        List<String> names = new ArrayList<>();
        int selectedIndex = 0;
        for (int i = 0; i < familyChildren.size(); i++) {
            names.add(familyChildren.get(i).getFullName() + " (Solde: "
                    + MillimesUtil.formatMillimes(familyChildren.get(i).getBalanceMillimes()) + ")");
            if (preselectedChild != null && familyChildren.get(i).getId() == preselectedChild.getId()) {
                selectedIndex = i;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spChild.setAdapter(adapter);
        spChild.setSelection(selectedIndex);
        spChild.setPadding(0, 8, 0, 24);
        layout.addView(spChild);

        TextView lblAmount = new TextView(this);
        lblAmount.setText("Montant en Dinars (ex: 20.000) :");
        lblAmount.setTextColor(Color.parseColor("#102B27"));
        lblAmount.setTypeface(null, Typeface.BOLD);
        layout.addView(lblAmount);

        EditText etAmount = new EditText(this);
        etAmount.setHint("Montant en DT");
        etAmount.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        TextView lblDesc = new TextView(this);
        lblDesc.setText("Motif / Description :");
        lblDesc.setTextColor(Color.parseColor("#102B27"));
        lblDesc.setTypeface(null, Typeface.BOLD);
        lblDesc.setPadding(0, 16, 0, 0);
        layout.addView(lblDesc);

        EditText etDesc = new EditText(this);
        etDesc.setHint("Argent de poche, récompense...");
        layout.addView(etDesc);

        builder.setView(layout);
        builder.setPositiveButton("Transférer", (dialog, which) -> {
            String amtStr = etAmount.getText().toString().trim();
            if (amtStr.isEmpty()) {
                Toast.makeText(FamilyDashboardActivity.this, "Veuillez saisir un montant.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double dt = Double.parseDouble(amtStr);
                long millimes = MillimesUtil.dtToMillimes(dt);
                int pos = spChild.getSelectedItemPosition();
                User targetChild = familyChildren.get(pos);
                String desc = etDesc.getText().toString().trim();

                boolean ok = familyDao.allocateMoneyToChild(currentFamilyId, currentParentId,
                        targetChild.getId(), targetChild.getFullName(), millimes, desc);
                if (ok) {
                    Toast.makeText(FamilyDashboardActivity.this, "Transfert de " + amtStr + " DT réussi !",
                            Toast.LENGTH_SHORT).show();
                    loadDashboardData();
                } else {
                    Toast.makeText(FamilyDashboardActivity.this, "Fonds disponibles insuffisants !", Toast.LENGTH_LONG)
                            .show();
                }
            } catch (Exception e) {
                Toast.makeText(FamilyDashboardActivity.this, "Montant invalide.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }
}

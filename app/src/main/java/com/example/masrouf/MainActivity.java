package com.example.masrouf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.CategoryDao;
import com.example.masrouf.dao.FamilyDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.dao.UserDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.model.BudgetAllocation;
import com.example.masrouf.model.Category;
import com.example.masrouf.model.FamilyFunds;
import com.example.masrouf.model.Transaction;
import com.example.masrouf.model.User;
import com.example.masrouf.util.LocaleHelper;
import com.example.masrouf.util.NotificationHelper;
import com.example.masrouf.util.VoiceTransactionParser;
import com.example.masrouf.view.AddExpenseActivity;
import com.example.masrouf.view.AddPurchaseActivity;
import com.example.masrouf.view.AnalyticsActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.example.masrouf.view.CalendarActivity;
import com.example.masrouf.view.CreateBudgetActivity;
import com.example.masrouf.view.CustomCategoryModal;
import com.example.masrouf.view.LoginActivity;
import com.example.masrouf.view.ModifyBudgetActivity;
import com.example.masrouf.view.PointsAndAchievementsActivity;
import com.example.masrouf.view.ProductsActivity;
import com.example.masrouf.view.SettingsActivity;
import com.example.masrouf.view.VoiceInputDialog;
import com.example.masrouf.view.admin.AdminDashboardActivity;
import com.example.masrouf.view.family.FamilyChildrenActivity;
import com.example.masrouf.view.family.FamilyDashboardActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Écran d'accueil principal (Image 8) et Tiroir de Navigation Latéral (Image 9).
 * COUCHE MVC : Vue (View).
 * EXIGENCES COUVERTES :
 *   - DrawerLayout natif Android : Clic sur les 3 lignes ouvre le menu latéral (Image 9).
 *   - Gestion dynamique des 2 états : État vide (Image 8) et État actif avec budget.
 *   - AUCUNE DONNÉE EN DUR : Catégories et transactions chargées dynamiquement depuis SQLite.
 *   - Saisie Vocale multilingue via microphone avec dialogue de confirmation interactif.
 *   - Notifications d'alertes 80% et 100% avec boîte de dialogue des notifications.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQ_CODE_SPEECH_INPUT = 1002;

    // Composants du Tiroir de navigation (DrawerLayout - Image 9)
    private DrawerLayout drawerLayout;
    private ImageView ivMainMenu;
    private TextView tvDrawerUserName;
    private TextView tvDrawerUserRoleBadge;

    private TextView btnDrawerLangAr;
    private TextView btnDrawerLangFr;
    private TextView btnDrawerLangEn;

    // Barre supérieure
    private TextView tvMainGreeting;
    private ImageView ivMainMic;
    private FrameLayout frameNotif;
    private ImageView ivMainProfile;
    private TextView tvMainPeriodSubtitle;

    // État Vide (Empty State - Image 8)
    private LinearLayout layoutEmptyBudgetState;
    private Button btnCreateBudgetEmpty;

    // État Actif (Active State)
    private LinearLayout layoutActiveBudgetState;
    private TextView tvMainAvailableAmount;
    private TextView tvMainTotalBudgetLabel;
    private ProgressBar pbBudgetUsage;
    private TextView tvMainSpentLabel;
    private TextView tvMainPercentageLabel;
    private TextView tvMainDateRangeLabel;

    private LinearLayout layoutStatusBanner;
    private ImageView ivStatusIcon;
    private TextView tvStatusTitle;
    private TextView tvStatusDesc;
    private LinearLayout layoutCategoriesContainer;
    private Button btnAddPurchase;
    private Button btnAddExpense;

    // Section Transactions
    private LinearLayout layoutEmptyTransactionsState;
    private LinearLayout layoutTransactionsContainer;
    private Button btnAddExpenseBottom;

    // Bouton d'ajout de catégorie au-dessus du budget
    private TextView btnHomeAddCategory;

    // Vues de répartition budgétaire pour le Parent
    private LinearLayout layoutParentBudgetBreakdown;
    private TextView tvParentGlobalBudget;
    private TextView tvParentChildrenAllocated;
    private TextView tvParentOwnShare;

    // Section de progression budgétaire des enfants pour le Parent
    private LinearLayout layoutParentChildrenSection;
    private TextView btnParentManageChildren;
    private LinearLayout containerChildrenCards;

    // Éléments spécifiques du tiroir pour le Parent
    private LinearLayout btnDrawerChildren;
    private LinearLayout btnDrawerQuickEntry;
    private LinearLayout btnDrawerNotifications;
    private LinearLayout btnDrawerPoints;
    private TextView tvDrawerCalendarLabel;
    private TextView tvDrawerAnalyticsLabel;

    // DAO et gestionnaire de session
    private SessionManager sessionManager;
    private BudgetDao budgetDao;
    private TransactionDao transactionDao;
    private FamilyDao familyDao;
    private UserDao userDao;
    private CategoryDao categoryDao;

    private boolean hasNotifiedWarningThisSession = false;
    private boolean hasNotifiedExceededThisSession = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.updateResources(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initialisation de MainActivity");

        // Initialise le gestionnaire de session
        sessionManager = new SessionManager(this);

        if (sessionManager.isParent() && SessionManager.SPACE_FAMILY.equalsIgnoreCase(sessionManager.getActiveSpace())) {
            startActivity(new Intent(this, FamilyDashboardActivity.class));
            finish();
            return;
        }

        // Initialise le DAO des budgets
        budgetDao = new BudgetDao(this);
        // Initialise le DAO des transactions
        transactionDao = new TransactionDao(this);
        // Initialise le DAO familial
        familyDao = new FamilyDao(this);
        // Initialise le DAO des utilisateurs
        userDao = new UserDao(this);
        // Initialise le DAO des catégories
        categoryDao = new CategoryDao(this);

        setContentView(R.layout.activity_main);

        initViews();
        setupNavigationDrawer();
        setupListeners();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        ivMainMenu = findViewById(R.id.ivMainMenu);
        tvMainGreeting = findViewById(R.id.tvMainGreeting);
        ivMainMic = findViewById(R.id.ivMainMic);
        frameNotif = findViewById(R.id.frameNotif);
        ivMainProfile = findViewById(R.id.ivMainProfile);
        tvMainPeriodSubtitle = findViewById(R.id.tvMainPeriodSubtitle);

        // Bouton Ajouter une catégorie
        btnHomeAddCategory = findViewById(R.id.btnHomeAddCategory);

        // Répartition Parent
        layoutParentBudgetBreakdown = findViewById(R.id.layoutParentBudgetBreakdown);
        tvParentGlobalBudget = findViewById(R.id.tvParentGlobalBudget);
        tvParentChildrenAllocated = findViewById(R.id.tvParentChildrenAllocated);
        tvParentOwnShare = findViewById(R.id.tvParentOwnShare);

        // Section Enfants
        layoutParentChildrenSection = findViewById(R.id.layoutParentChildrenSection);
        btnParentManageChildren = findViewById(R.id.btnParentManageChildren);
        containerChildrenCards = findViewById(R.id.containerChildrenCards);

        // Tiroir Parent vs Normal
        btnDrawerChildren = findViewById(R.id.btnDrawerChildren);
        btnDrawerQuickEntry = findViewById(R.id.btnDrawerQuickEntry);
        btnDrawerNotifications = findViewById(R.id.btnDrawerNotifications);
        btnDrawerPoints = findViewById(R.id.btnDrawerPoints);
        tvDrawerCalendarLabel = findViewById(R.id.tvDrawerCalendarLabel);
        tvDrawerAnalyticsLabel = findViewById(R.id.tvDrawerAnalyticsLabel);

        // État vide
        layoutEmptyBudgetState = findViewById(R.id.layoutEmptyBudgetState);
        btnCreateBudgetEmpty = findViewById(R.id.btnCreateBudgetEmpty);

        // État actif
        layoutActiveBudgetState = findViewById(R.id.layoutActiveBudgetState);
        tvMainAvailableAmount = findViewById(R.id.tvMainAvailableAmount);
        tvMainTotalBudgetLabel = findViewById(R.id.tvMainTotalBudgetLabel);
        pbBudgetUsage = findViewById(R.id.pbBudgetUsage);
        tvMainSpentLabel = findViewById(R.id.tvMainSpentLabel);
        tvMainPercentageLabel = findViewById(R.id.tvMainPercentageLabel);
        tvMainDateRangeLabel = findViewById(R.id.tvMainDateRangeLabel);

        layoutStatusBanner = findViewById(R.id.layoutStatusBanner);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusDesc = findViewById(R.id.tvStatusDesc);
        layoutCategoriesContainer = findViewById(R.id.layoutCategoriesContainer);
        btnAddPurchase = findViewById(R.id.btnAddPurchase);
        btnAddExpense = findViewById(R.id.btnAddExpense);

        // Transactions
        layoutEmptyTransactionsState = findViewById(R.id.layoutEmptyTransactionsState);
        layoutTransactionsContainer = findViewById(R.id.layoutTransactionsContainer);
        btnAddExpenseBottom = findViewById(R.id.btnAddExpenseBottom);

        // Tiroir latéral
        tvDrawerUserName = findViewById(R.id.tvDrawerUserName);
        tvDrawerUserRoleBadge = findViewById(R.id.tvDrawerUserRoleBadge);

        btnDrawerLangAr = findViewById(R.id.btnDrawerLangAr);
        btnDrawerLangFr = findViewById(R.id.btnDrawerLangFr);
        btnDrawerLangEn = findViewById(R.id.btnDrawerLangEn);
    }

    private void setupNavigationDrawer() {
        // Récupère le nom complet de l'utilisateur connecté depuis la session
        String name = sessionManager.getUserName();
        // Vérifie si le TextView du nom dans le tiroir est présent
        if (tvDrawerUserName != null) {
            // Affiche le nom de l'utilisateur ou un libellé par défaut
            tvDrawerUserName.setText(name != null && !name.isEmpty() ? name : "Utilisateur");
        }

        // Configure le badge du type de compte dans le tiroir latéral
        if (tvDrawerUserRoleBadge != null) {
            // Vérifie si l'utilisateur est le parent de la famille
            if (sessionManager.isParent()) {
                // Affiche le badge Compte Familial (Parent)
                tvDrawerUserRoleBadge.setText("Compte Famille (Parent)");
            } else if (sessionManager.isChild()) {
                // Affiche le badge Compte Enfant pour l'enfant
                tvDrawerUserRoleBadge.setText("Compte Enfant");
            } else {
                // Affiche le badge Compte personnel pour l'utilisateur standard
                tvDrawerUserRoleBadge.setText("Compte personnel");
            }
        }

        // Configuration dynamique des options du tiroir selon le rôle
        if (sessionManager.isParent()) {
            View btnDrawerSwitchFamily = findViewById(R.id.btnDrawerSwitchFamily);
            if (btnDrawerSwitchFamily != null) {
                btnDrawerSwitchFamily.setVisibility(View.VISIBLE);
                btnDrawerSwitchFamily.setOnClickListener(v -> {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    sessionManager.setActiveSpace(SessionManager.SPACE_FAMILY);
                    startActivity(new Intent(MainActivity.this, FamilyDashboardActivity.class));
                });
            }

            // Affiche l'option Enfants dans le tiroir pour le Parent
            if (btnDrawerChildren != null) {
                // Rend visible le bouton Enfants
                btnDrawerChildren.setVisibility(View.VISIBLE);
                // Associe le clic pour ouvrir l'activité de gestion des enfants
                btnDrawerChildren.setOnClickListener(v -> {
                    // Ferme le tiroir latéral
                    drawerLayout.closeDrawer(GravityCompat.START);
                    // Ouvre l'écran de gestion des enfants
                    startActivity(new Intent(MainActivity.this, FamilyChildrenActivity.class));
                });
            }

            // Affiche l'option Saisie rapide dans le tiroir pour le Parent
            if (btnDrawerQuickEntry != null) {
                // Rend visible le bouton Saisie rapide
                btnDrawerQuickEntry.setVisibility(View.VISIBLE);
                // Associe le clic pour ouvrir le sélecteur de saisie rapide (Achat vs Dépense)
                btnDrawerQuickEntry.setOnClickListener(v -> {
                    // Ferme le tiroir latéral
                    drawerLayout.closeDrawer(GravityCompat.START);
                    // Ouvre la boîte de dialogue de saisie rapide
                    showQuickEntryDialog();
                });
            }

            // Affiche l'option Notifications dans le tiroir pour le Parent
            if (btnDrawerNotifications != null) {
                // Rend visible le bouton Notifications
                btnDrawerNotifications.setVisibility(View.VISIBLE);
                // Associe le clic pour afficher les notifications
                btnDrawerNotifications.setOnClickListener(v -> {
                    // Ferme le tiroir latéral
                    drawerLayout.closeDrawer(GravityCompat.START);
                    // Ouvre la boîte de dialogue des notifications
                    showNotificationDialog();
                });
            }

            // Masque la gamification (Points) pour l'espace Parent
            if (btnDrawerPoints != null) {
                // Masque l'élément Points du tiroir
                btnDrawerPoints.setVisibility(View.GONE);
            }

            // Met à jour les libellés pour le Parent
            if (tvDrawerCalendarLabel != null) {
                // Affiche Calendrier & Historique
                tvDrawerCalendarLabel.setText("Calendrier & Historique");
            }
            if (tvDrawerAnalyticsLabel != null) {
                // Affiche Budgets & Analyses
                tvDrawerAnalyticsLabel.setText("Budgets & Analyses");
            }
        } else {
            // Masque les options réservées au Parent pour les Enfants et Utilisateurs
            if (btnDrawerChildren != null) btnDrawerChildren.setVisibility(View.GONE);
            if (btnDrawerQuickEntry != null) btnDrawerQuickEntry.setVisibility(View.GONE);
            if (btnDrawerNotifications != null) btnDrawerNotifications.setVisibility(View.GONE);

            // Affiche les Points pour l'utilisateur individuel
            if (btnDrawerPoints != null) btnDrawerPoints.setVisibility(View.VISIBLE);

            // Rétablit les libellés standards
            if (tvDrawerCalendarLabel != null) tvDrawerCalendarLabel.setText("Calendrier");
            if (tvDrawerAnalyticsLabel != null) tvDrawerAnalyticsLabel.setText("Analyses");
        }

        // Navigation tiroir : Accueil
        findViewById(R.id.btnDrawerHome).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        // Navigation tiroir : Produits
        findViewById(R.id.btnDrawerProducts).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, ProductsActivity.class));
        });

        // Navigation tiroir : Calendrier
        findViewById(R.id.btnDrawerCalendar).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, CalendarActivity.class));
        });

        // Navigation tiroir : Analyses
        findViewById(R.id.btnDrawerAnalytics).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, AnalyticsActivity.class));
        });

        // Navigation tiroir : Points
        if (btnDrawerPoints != null) {
            btnDrawerPoints.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(MainActivity.this, PointsAndAchievementsActivity.class));
            });
        }

        // Navigation tiroir : Réglages
        findViewById(R.id.btnDrawerSettings).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        // Navigation tiroir : Déconnexion
        findViewById(R.id.btnDrawerLogout).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            sessionManager.clearSession();
            Toast.makeText(MainActivity.this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Sélecteur de langue dans le tiroir (Image 9 : عربي / FR / ANG)
        updateDrawerLanguageHighlight(sessionManager.getLanguage());

        if (btnDrawerLangAr != null) {
            btnDrawerLangAr.setOnClickListener(v -> changeAppLanguage("ar"));
        }
        if (btnDrawerLangFr != null) {
            btnDrawerLangFr.setOnClickListener(v -> changeAppLanguage("fr"));
        }
        if (btnDrawerLangEn != null) {
            btnDrawerLangEn.setOnClickListener(v -> changeAppLanguage("en"));
        }
    }

    private void updateDrawerLanguageHighlight(String lang) {
        if (btnDrawerLangAr == null || btnDrawerLangFr == null || btnDrawerLangEn == null) return;

        btnDrawerLangAr.setBackground(null);
        btnDrawerLangAr.setTextColor(Color.parseColor("#A0C8BC"));
        btnDrawerLangFr.setBackground(null);
        btnDrawerLangFr.setTextColor(Color.parseColor("#A0C8BC"));
        btnDrawerLangEn.setBackground(null);
        btnDrawerLangEn.setTextColor(Color.parseColor("#A0C8BC"));

        if ("ar".equalsIgnoreCase(lang)) {
            btnDrawerLangAr.setBackgroundResource(R.drawable.bg_pill_active);
            btnDrawerLangAr.setTextColor(Color.WHITE);
        } else if ("en".equalsIgnoreCase(lang)) {
            btnDrawerLangEn.setBackgroundResource(R.drawable.bg_pill_active);
            btnDrawerLangEn.setTextColor(Color.WHITE);
        } else {
            btnDrawerLangFr.setBackgroundResource(R.drawable.bg_pill_active);
            btnDrawerLangFr.setTextColor(Color.WHITE);
        }
    }

    private void changeAppLanguage(String code) {
        sessionManager.saveLanguage(code);
        LocaleHelper.setLocale(this, code);
        recreate();
    }

    private void setupListeners() {
        // Menu hamburger (3 lignes) -> ouverture du tiroir
        ivMainMenu.setOnClickListener(v -> {
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        // Saisie vocale
        ivMainMic.setOnClickListener(v -> startVoiceRecognition());

        View fabMic = findViewById(R.id.fabFloatingMic);
        if (fabMic != null) {
            fabMic.setOnClickListener(v -> startVoiceRecognition());
        }

        // Clic sur la carte héros du budget -> édition ou création
        View cardHero = findViewById(R.id.layoutBudgetHeroCard);
        if (cardHero != null) {
            cardHero.setOnClickListener(v -> {
                long userId = sessionManager.getUserId();
                Budget b = budgetDao.getCurrentBudget(userId);
                if (b != null) {
                    startActivity(new Intent(MainActivity.this, ModifyBudgetActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, CreateBudgetActivity.class));
                }
            });
        }

        // Notifications
        if (frameNotif != null) {
            frameNotif.setOnClickListener(v -> showNotificationDialog());
        }

        // Profil -> Paramètres
        ivMainProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        // Lien Calendrier ›
        View btnGoCal = findViewById(R.id.btnMainGoToCalendar);
        if (btnGoCal != null) {
            btnGoCal.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CalendarActivity.class)));
        }

        // Bouton Créer un budget depuis l'état vide
        if (btnCreateBudgetEmpty != null) {
            btnCreateBudgetEmpty.setOnClickListener(v -> {
                if (sessionManager.isChild()) {
                    Toast.makeText(MainActivity.this, "Budget alloué par le parent. Seul votre père peut modifier votre budget.", Toast.LENGTH_LONG).show();
                } else {
                    startActivity(new Intent(MainActivity.this, CreateBudgetActivity.class));
                }
            });
        }

        // Bouton Ajouter une catégorie au-dessus du budget (Exigence fonctionnelle exclusive)
        if (btnHomeAddCategory != null) {
            btnHomeAddCategory.setOnClickListener(v -> CustomCategoryModal.show(MainActivity.this, this::loadDashboardData));
        }

        // Bouton Gérer les enfants dans la section de progression Parent
        if (btnParentManageChildren != null) {
            btnParentManageChildren.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FamilyChildrenActivity.class)));
        }

        // Bouton Ajouter une dépense simple
        if (btnAddExpenseBottom != null) {
            btnAddExpenseBottom.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddExpenseActivity.class)));
        }

        // Actions rapides état actif
        if (btnAddPurchase != null) {
            btnAddPurchase.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddPurchaseActivity.class)));
        }

        if (btnAddExpense != null) {
            btnAddExpense.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddExpenseActivity.class)));
        }
    }

    /**
     * Ouvre une boîte de dialogue pour créer une nouvelle catégorie (Nom, Type: Produit vs Dépense).
     * Enregistre la catégorie en SQLite, la lie à user_categories, et actualise immédiatement l'écran d'accueil.
     */
    private void showAddCategoryDialog() {
        // Construit la boîte de dialogue d'ajout de catégorie
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Définit le titre multilingue
        builder.setTitle(R.string.dialog_add_category_title);

        // Crée le conteneur vertical du formulaire
        LinearLayout layout = new LinearLayout(this);
        // Oriente verticalement
        layout.setOrientation(LinearLayout.VERTICAL);
        // Applique les marges intérieures
        layout.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(10));

        // Crée le champ de saisie du nom de la catégorie
        final EditText etName = new EditText(this);
        // Définit le hint
        etName.setHint(R.string.hint_category_name);
        // Couleur du texte
        etName.setTextColor(ContextCompat.getColor(this, R.color.deep_forest));
        // Ajoute au layout
        layout.addView(etName);

        // Crée le libellé du type de catégorie
        TextView tvTypeLabel = new TextView(this);
        // Définit le libellé
        tvTypeLabel.setText(R.string.label_category_type);
        // Couleur atténuée
        tvTypeLabel.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        // Taille de police
        tvTypeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        // Configure les marges
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tLp.setMargins(0, dpToPx(12), 0, dpToPx(4));
        tvTypeLabel.setLayoutParams(tLp);
        layout.addView(tvTypeLabel);

        // Crée le groupe de boutons radio pour le type
        final RadioGroup rgType = new RadioGroup(this);
        rgType.setOrientation(RadioGroup.HORIZONTAL);

        // Bouton radio Produit / Achat
        final RadioButton rbProduct = new RadioButton(this);
        rbProduct.setText(R.string.type_product);
        rbProduct.setChecked(true);
        rbProduct.setTextColor(ContextCompat.getColor(this, R.color.deep_forest));
        rgType.addView(rbProduct);

        // Bouton radio Dépense / Facture
        final RadioButton rbExpense = new RadioButton(this);
        rbExpense.setText(R.string.type_expense);
        rbExpense.setTextColor(ContextCompat.getColor(this, R.color.deep_forest));
        rgType.addView(rbExpense);

        layout.addView(rgType);
        builder.setView(layout);

        // Bouton d'action Ajouter
        builder.setPositiveButton(R.string.btn_confirm_add, (dialog, which) -> {
            // Récupère le nom saisi
            String name = etName.getText().toString().trim();
            // Valide que le nom n'est pas vide
            if (name.isEmpty()) {
                Toast.makeText(MainActivity.this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            // Détermine le type sélectionné
            String type = rbProduct.isChecked() ? Category.TYPE_PRODUCT : Category.TYPE_EXPENSE;
            // Choisit l'icône appropriée
            String icon = rbProduct.isChecked() ? "ic_cutlery" : "ic_receipt";
            // Récupère l'identifiant de l'utilisateur connecté
            long userId = sessionManager.getUserId();
            // Insère en base SQLite et associe à user_categories
            categoryDao.insertUserCategory(userId, name, type, "#102B27", icon);
            // Affiche un toast de confirmation
            Toast.makeText(MainActivity.this, R.string.msg_category_added, Toast.LENGTH_SHORT).show();
            // Actualise immédiatement les données de l'écran d'accueil
            loadDashboardData();
        });

        // Bouton Annuler
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss());
        // Affiche la boîte de dialogue
        builder.show();
    }

    /**
     * Ouvre la boîte de dialogue de saisie rapide pour le profil Parent.
     * Permet au Parent d'enregistrer directement ses propres achats ou dépenses personnelles.
     */
    private void showQuickEntryDialog() {
        // Construit la boîte de dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Définit le titre
        builder.setTitle("Saisie rapide Masrouf");
        // Définit le message explicatif
        builder.setMessage("Que souhaitez-vous enregistrer pour vos dépenses personnelles ?");
        // Option 1 : Nouvel achat personnel
        builder.setPositiveButton("Nouvel achat", (d, w) -> {
            startActivity(new Intent(MainActivity.this, AddPurchaseActivity.class));
        });
        // Option 2 : Nouvelle dépense personnelle
        builder.setNeutralButton("Nouvelle dépense", (d, w) -> {
            startActivity(new Intent(MainActivity.this, AddExpenseActivity.class));
        });
        // Option 3 : Annuler
        builder.setNegativeButton(R.string.btn_cancel, (d, w) -> d.dismiss());
        // Affiche la boîte de dialogue
        builder.show();
    }

    private void showNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notifications Masrouf");
        builder.setMessage("• Bienvenue dans Masrouf ! Votre espace budgétaire est sécurisé.\n" +
                "• Rappel : Allouez votre budget pour recevoir des alertes préventives à 80% et 100%.\n" +
                "• Bonus : Enregistrez vos achats pour accumuler des points de fidélité.");
        builder.setPositiveButton("Compris", (d, w) -> d.dismiss());
        builder.show();
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites votre transaction (ex: J'ai acheté du shampoing pour 20 DT)");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Reconnaissance vocale non disponible sur cet appareil", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String spokenText = matches.get(0);
                VoiceTransactionParser.ParsedVoiceResult parsed = VoiceTransactionParser.parse(spokenText);
                VoiceInputDialog.showReviewDialog(this, parsed, this::loadDashboardData);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Récupère l'identifiant de l'utilisateur connecté
        long userId = sessionManager.getUserId();
        // Valeur de secours si l'identifiant n'est pas encore défini
        if (userId <= 0) userId = 1;

        // Récupère le nom complet de l'utilisateur
        String fullName = sessionManager.getUserName();
        // Extrait le prénom pour un accueil personnalisé
        String firstName = (fullName != null && !fullName.isEmpty()) ? fullName.split(" ")[0] : "am75";
        // Affiche le message de salutation
        tvMainGreeting.setText("Bonjour, " + firstName);

        // Recherche du budget actif pour ce mois dans la table budgets
        Budget currentBudget = budgetDao.getCurrentBudget(userId);

        // CAS 1 : Utilisateur avec profil PARENT (Espace Familial Unifié)
        if (sessionManager.isParent()) {
            // Récupère le fonds global familial géré par ce parent
            FamilyFunds funds = familyDao.getFamilyFundsByParent(userId);
            // Vérifie si un budget global est configuré pour cette famille
            if (funds != null && funds.getTotalFundsMillimes() > 0) {
                // Masque l'état vide
                layoutEmptyBudgetState.setVisibility(View.GONE);
                // Affiche la carte active du budget
                layoutActiveBudgetState.setVisibility(View.VISIBLE);
                // Affiche la ventilation globale Parent / Enfants
                if (layoutParentBudgetBreakdown != null) {
                    layoutParentBudgetBreakdown.setVisibility(View.VISIBLE);
                }

                // Convertit le budget global familial en Dinars
                double totalGlobal = funds.getTotalFundsMillimes() / 1000.0;
                // Convertit la part allouée aux enfants en Dinars
                double allocatedChildren = funds.getAllocatedFundsMillimes() / 1000.0;
                // Calcule les dépenses personnelles du parent en Dinars
                double parentSpent = transactionDao.getTotalExpenses(userId);

                // Montant restant du budget familial = Budget familial - Dépenses parent - Allocations enfants
                double familyRemaining = Math.max(0, totalGlobal - parentSpent - allocatedChildren);
                // Consommation globale du budget familial = Dépenses parent + Allocations enfants
                double totalConsumed = parentSpent + allocatedChildren;
                // Pourcentage de consommation globale du budget familial
                double overallPct = totalGlobal > 0 ? (totalConsumed / totalGlobal) * 100.0 : 0.0;

                // Affiche le montant restant disponible du budget familial
                tvMainAvailableAmount.setText(String.format(Locale.US, "%.3f DT", familyRemaining));
                // Affiche le montant dépensé par le parent
                tvMainSpentLabel.setText(String.format(Locale.US, "Dépensé : %.3f DT", parentSpent));
                // Affiche le pourcentage de consommation globale du budget familial
                tvMainPercentageLabel.setText(String.format(Locale.US, "%.1f %%", overallPct));
                // Affiche le budget familial total
                tvMainTotalBudgetLabel.setText(String.format(Locale.US, "Budget familial : %.3f DT", totalGlobal));
                // Met à jour la jauge de progression
                pbBudgetUsage.setProgress((int) Math.min(100, Math.round(overallPct)));

                // Affiche le sous-titre de période
                tvMainPeriodSubtitle.setText("BUDGET FAMILIAL GLOBAL · EN COURS");
                // Affiche la période
                tvMainDateRangeLabel.setText("Mois en cours");

                // Met à jour les libellés de la ventilation globale
                if (tvParentGlobalBudget != null) {
                    tvParentGlobalBudget.setText(String.format(Locale.US, "Budget : %.3f DT", totalGlobal));
                }
                if (tvParentChildrenAllocated != null) {
                    tvParentChildrenAllocated.setText(String.format(Locale.US, "Enfants : %.3f DT", allocatedChildren));
                }
                if (tvParentOwnShare != null) {
                    tvParentOwnShare.setText(String.format(Locale.US, "Restant : %.3f DT", familyRemaining));
                }

                // Met à jour la bannière de statut selon le pourcentage global
                updateStatusBanner(overallPct, familyRemaining);

                // Affiche les catégories du parent s'il a un budget personnel associé
                if (currentBudget != null) {
                    populateCategoriesDynamically(currentBudget.getId());
                } else {
                    layoutCategoriesContainer.removeAllViews();
                }

                // Affiche la section de progression budgétaire des enfants (Confidentialité stricte)
                populateChildrenCards(userId);
            } else if (currentBudget != null) {
                // Affiche le budget individuel si aucun fonds global n'est encore saisi
                showActiveIndividualBudget(currentBudget);
                if (layoutParentBudgetBreakdown != null) layoutParentBudgetBreakdown.setVisibility(View.GONE);
                if (layoutParentChildrenSection != null) layoutParentChildrenSection.setVisibility(View.GONE);
            } else {
                // ÉTAT VIDE pour le parent
                layoutEmptyBudgetState.setVisibility(View.VISIBLE);
                layoutActiveBudgetState.setVisibility(View.GONE);
                if (layoutParentBudgetBreakdown != null) layoutParentBudgetBreakdown.setVisibility(View.GONE);
                if (layoutParentChildrenSection != null) layoutParentChildrenSection.setVisibility(View.GONE);
                tvMainPeriodSubtitle.setText("AUCUN BUDGET DÉFINI POUR CETTE PÉRIODE");
            }

        // CAS 2 : Utilisateur avec profil ENFANT (Budget alloué par le parent en lecture seule)
        } else if (sessionManager.isChild()) {
            if (layoutParentBudgetBreakdown != null) layoutParentBudgetBreakdown.setVisibility(View.GONE);
            if (layoutParentChildrenSection != null) layoutParentChildrenSection.setVisibility(View.GONE);

            // Récupère les données de l'enfant connecté
            User childUser = userDao.getUserById(userId);
            // Récupère le solde actuel de l'enfant
            long childBalanceMillimes = childUser != null ? childUser.getBalanceMillimes() : 0;
            // Formate le mois courant
            String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
            // Calcule les dépenses du mois de l'enfant
            long childSpentMillimes = familyDao.getChildMonthlySpendingMillimes(userId, currentMonth);
            // Calcule l'allocation totale accordée par le parent
            long childTotalAllocatedMillimes = childBalanceMillimes + childSpentMillimes;

            // Vérifie si un budget a été alloué à l'enfant
            if (childTotalAllocatedMillimes > 0) {
                layoutEmptyBudgetState.setVisibility(View.GONE);
                layoutActiveBudgetState.setVisibility(View.VISIBLE);

                double totalBudget = childTotalAllocatedMillimes / 1000.0;
                double spent = childSpentMillimes / 1000.0;
                double available = childBalanceMillimes / 1000.0;
                double percentage = totalBudget > 0 ? (spent / totalBudget) * 100.0 : 0.0;

                tvMainAvailableAmount.setText(String.format(Locale.US, "%.3f DT", Math.max(0, available)));
                tvMainSpentLabel.setText(String.format(Locale.US, "Dépensé : %.3f DT", spent));
                tvMainPercentageLabel.setText(String.format(Locale.US, "%.1f %%", percentage));
                tvMainTotalBudgetLabel.setText(String.format(Locale.US, "Budget alloué : %.3f DT", totalBudget));
                pbBudgetUsage.setProgress((int) Math.min(100, Math.round(percentage)));

                tvMainPeriodSubtitle.setText("ARGENT DE POCHE · ALLOUÉ PAR LE PARENT");
                tvMainDateRangeLabel.setText("Mois en cours");

                updateStatusBanner(percentage, available);

                if (currentBudget != null) {
                    populateCategoriesDynamically(currentBudget.getId());
                } else {
                    layoutCategoriesContainer.removeAllViews();
                }
            } else {
                layoutEmptyBudgetState.setVisibility(View.VISIBLE);
                layoutActiveBudgetState.setVisibility(View.GONE);
                tvMainPeriodSubtitle.setText("AUCUN BUDGET ALLOUÉ PAR LE PARENT");
                if (btnCreateBudgetEmpty != null) {
                    btnCreateBudgetEmpty.setText("Budget alloué par le parent");
                    btnCreateBudgetEmpty.setEnabled(false);
                }
            }

        // CAS 3 : Utilisateur avec profil INDIVIDUEL STANDARD (Compte personnel)
        } else {
            if (layoutParentBudgetBreakdown != null) layoutParentBudgetBreakdown.setVisibility(View.GONE);
            if (layoutParentChildrenSection != null) layoutParentChildrenSection.setVisibility(View.GONE);

            if (currentBudget != null) {
                showActiveIndividualBudget(currentBudget);
            } else {
                // ÉTAT VIDE (Screenshot 1)
                tvMainAvailableAmount.setText("0.000 DT");
                tvMainSpentLabel.setText("Dépensé 0.000 DT");
                tvMainPercentageLabel.setText("0% consommé");
                tvMainTotalBudgetLabel.setText("Budget 0.000 DT");
                pbBudgetUsage.setProgress(0);

                layoutEmptyBudgetState.setVisibility(View.VISIBLE);
                layoutActiveBudgetState.setVisibility(View.GONE);
                tvMainPeriodSubtitle.setText("NOVEMBRE 2026 · 01–30 NOV. 2026");
                if (btnCreateBudgetEmpty != null) {
                    btnCreateBudgetEmpty.setText("Définir le budget du mois");
                    btnCreateBudgetEmpty.setEnabled(true);
                }
            }
        }

        // Peuplement des transactions récentes pour tous les utilisateurs
        populateRecentTransactionsDynamically(userId);
    }

    /**
     * Affiche l'état actif pour un budget individuel standard.
     */
    private void showActiveIndividualBudget(Budget currentBudget) {
        layoutEmptyBudgetState.setVisibility(View.GONE);
        layoutActiveBudgetState.setVisibility(View.VISIBLE);

        double totalBudget = currentBudget.getTotalAmount();
        double totalSpent = budgetDao.getTotalSpent(currentBudget.getId());
        double available = totalBudget - totalSpent;
        double percentage = totalBudget > 0 ? (totalSpent / totalBudget) * 100.0 : 0.0;

        tvMainAvailableAmount.setText(String.format(Locale.US, "%.3f DT", Math.max(0, available)));
        tvMainSpentLabel.setText(String.format(Locale.US, "Dépensé : %.3f DT", totalSpent));
        tvMainPercentageLabel.setText(String.format(Locale.US, "%.1f %%", percentage));
        tvMainTotalBudgetLabel.setText(String.format(Locale.US, "Budget total : %.3f DT", totalBudget));
        pbBudgetUsage.setProgress((int) Math.min(100, Math.round(percentage)));

        tvMainPeriodSubtitle.setText(String.format("%s · %s – %s", currentBudget.getStatus(), currentBudget.getStartDate(), currentBudget.getEndDate()));
        tvMainDateRangeLabel.setText(currentBudget.getStartDate() + " – " + currentBudget.getEndDate());

        updateStatusBanner(percentage, available);
        populateCategoriesDynamically(currentBudget.getId());
    }

    /**
     * Met à jour la couleur et le libellé du bandeau de statut selon le taux de consommation.
     */
    private void updateStatusBanner(double percentage, double available) {
        GradientDrawable bannerShape = new GradientDrawable();
        bannerShape.setShape(GradientDrawable.RECTANGLE);
        bannerShape.setCornerRadius(dpToPx(14));

        if (percentage >= 100.0) {
            bannerShape.setColor(ContextCompat.getColor(this, R.color.coral_light));
            layoutStatusBanner.setBackground(bannerShape);
            tvStatusTitle.setText(R.string.status_exceeded_title);
            tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.coral));
            tvStatusDesc.setText(String.format(getString(R.string.status_exceeded_desc), String.format(Locale.US, "%.3f DT", Math.abs(available))));
            tvStatusDesc.setTextColor(ContextCompat.getColor(this, R.color.coral));
            pbBudgetUsage.setProgressTintList(ContextCompat.getColorStateList(this, R.color.coral));

            if (!hasNotifiedExceededThisSession) {
                NotificationHelper.showBudgetAlert(this, getString(R.string.notif_budget_exceeded_title),
                        String.format(getString(R.string.notif_budget_exceeded_msg), String.format(Locale.US, "%.3f DT", Math.abs(available))), true);
                hasNotifiedExceededThisSession = true;
            }
        } else if (percentage >= 80.0) {
            bannerShape.setColor(Color.parseColor("#FFF5E5"));
            layoutStatusBanner.setBackground(bannerShape);
            tvStatusTitle.setText(R.string.status_warning_title);
            tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.gold));
            tvStatusDesc.setText(String.format(getString(R.string.status_warning_desc), String.format(Locale.US, "%.3f DT", available)));
            tvStatusDesc.setTextColor(ContextCompat.getColor(this, R.color.ink));
            pbBudgetUsage.setProgressTintList(ContextCompat.getColorStateList(this, R.color.gold));

            if (!hasNotifiedWarningThisSession) {
                NotificationHelper.showBudgetAlert(this, getString(R.string.notif_budget_warning_title),
                        String.format(getString(R.string.notif_budget_warning_msg), percentage, String.format(Locale.US, "%.3f DT", available)), false);
                hasNotifiedWarningThisSession = true;
            }
        } else {
            bannerShape.setColor(ContextCompat.getColor(this, R.color.mint));
            layoutStatusBanner.setBackground(bannerShape);
            tvStatusTitle.setText(R.string.status_healthy_title);
            tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.forest));
            tvStatusDesc.setText(String.format(getString(R.string.status_healthy_desc), String.format(Locale.US, "%.3f DT", available)));
            tvStatusDesc.setTextColor(ContextCompat.getColor(this, R.color.forest));
            pbBudgetUsage.setProgressTintList(ContextCompat.getColorStateList(this, R.color.forest));
        }
    }

    /**
     * Affiche les cartes d'avancement du budget de chaque enfant pour le Parent.
     * Respecte rigoureusement la confidentialité : affiche uniquement alloué, dépensé, restant et pourcentage,
     * sans jamais afficher les achats détaillés ou les reçus de l'enfant.
     */
    private void populateChildrenCards(long parentId) {
        if (layoutParentChildrenSection == null || containerChildrenCards == null) return;

        // Récupère la liste des enfants rattachés à ce parent
        List<User> children = userDao.getChildrenByParent(parentId);
        // Vérifie si des enfants sont présents
        if (children == null || children.isEmpty()) {
            layoutParentChildrenSection.setVisibility(View.GONE);
            return;
        }

        layoutParentChildrenSection.setVisibility(View.VISIBLE);
        containerChildrenCards.removeAllViews();
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());

        for (User child : children) {
            long spentMilli = familyDao.getChildMonthlySpendingMillimes(child.getId(), currentMonth);
            long childAllocMilli = child.getBalanceMillimes() + spentMilli;
            double childAlloc = childAllocMilli / 1000.0;
            double childSpent = spentMilli / 1000.0;
            double childRemaining = Math.max(0, childAlloc - childSpent);
            double childPct = childAlloc > 0 ? (childSpent / childAlloc) * 100.0 : 0.0;

            LinearLayout card = new LinearLayout(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, dpToPx(8));
            card.setLayoutParams(cardParams);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
            card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_input_card));

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvChildName = new TextView(this);
            tvChildName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            tvChildName.setText(child.getFullName());
            tvChildName.setTextColor(ContextCompat.getColor(this, R.color.deep_forest));
            tvChildName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvChildName.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(tvChildName);

            TextView tvChildRatio = new TextView(this);
            tvChildRatio.setText(String.format(Locale.US, "%.3f / %.3f DT", childSpent, childAlloc));
            tvChildRatio.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            tvChildRatio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            row.addView(tvChildRatio);

            card.addView(row);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(6));
            pbParams.setMargins(0, dpToPx(8), 0, 0);
            pb.setLayoutParams(pbParams);
            pb.setMax(100);
            pb.setProgress((int) Math.min(100, Math.round(childPct)));
            pb.setProgressTintList(ContextCompat.getColorStateList(this, childPct >= 100 ? R.color.coral : (childPct >= 80 ? R.color.gold : R.color.forest)));
            card.addView(pb);

            TextView tvRemaining = new TextView(this);
            tvRemaining.setText(String.format(Locale.US, "Reste : %.3f DT (%.0f%% consommé)", childRemaining, childPct));
            tvRemaining.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            tvRemaining.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rLp.setMargins(0, dpToPx(4), 0, 0);
            tvRemaining.setLayoutParams(rLp);
            card.addView(tvRemaining);

            containerChildrenCards.addView(card);
        }
    }

    private void populateCategoriesDynamically(long budgetId) {
        layoutCategoriesContainer.removeAllViews();
        List<BudgetAllocation> allocations = budgetDao.getAllocationsForBudget(budgetId);

        if (allocations == null || allocations.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Aucune catégorie allouée.");
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            layoutCategoriesContainer.addView(tvEmpty);
            return;
        }

        for (BudgetAllocation a : allocations) {
            LinearLayout card = new LinearLayout(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, dpToPx(8));
            card.setLayoutParams(cardParams);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
            card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_input_card));

            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvName = new TextView(this);
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            tvName.setText(a.getCategoryName());
            tvName.setTextColor(ContextCompat.getColor(this, R.color.deep_forest));
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvName.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(tvName);

            TextView tvRatio = new TextView(this);
            tvRatio.setText(String.format(Locale.US, "%.3f / %.3f DT", a.getSpentAmount(), a.getAllocatedAmount()));
            tvRatio.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            tvRatio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            row.addView(tvRatio);

            card.addView(row);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(5));
            pbParams.setMargins(0, dpToPx(8), 0, 0);
            pb.setLayoutParams(pbParams);
            pb.setMax(100);
            double pct = a.getAllocatedAmount() > 0 ? (a.getSpentAmount() / a.getAllocatedAmount()) * 100.0 : 0.0;
            pb.setProgress((int) Math.min(100, Math.round(pct)));
            pb.setProgressTintList(ContextCompat.getColorStateList(this, pct >= 100 ? R.color.coral : (pct >= 80 ? R.color.gold : R.color.forest)));
            card.addView(pb);

            layoutCategoriesContainer.addView(card);
        }
    }

    private void populateRecentTransactionsDynamically(long userId) {
        layoutTransactionsContainer.removeAllViews();
        List<Transaction> transactions = transactionDao.getRecentTransactions(userId, 5);

        if (transactions == null || transactions.isEmpty()) {
            layoutEmptyTransactionsState.setVisibility(View.VISIBLE);
            layoutTransactionsContainer.setVisibility(View.GONE);
            return;
        }

        layoutEmptyTransactionsState.setVisibility(View.GONE);
        layoutTransactionsContainer.setVisibility(View.VISIBLE);

        for (Transaction t : transactions) {
            LinearLayout rowCard = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dpToPx(8));
            rowCard.setLayoutParams(lp);
            rowCard.setOrientation(LinearLayout.HORIZONTAL);
            rowCard.setGravity(Gravity.CENTER_VERTICAL);
            rowCard.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
            rowCard.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_input_card));

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)));
            iv.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_circle_mint));
            iv.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            iv.setImageResource(R.drawable.ic_receipt);
            iv.setColorFilter(ContextCompat.getColor(this, R.color.deep_forest));
            rowCard.addView(iv);

            LinearLayout textCol = new LinearLayout(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textParams.setMargins(dpToPx(12), 0, dpToPx(8), 0);
            textCol.setLayoutParams(textParams);
            textCol.setOrientation(LinearLayout.VERTICAL);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(t.getTitle());
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.deep_forest));
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
            textCol.addView(tvTitle);

            TextView tvMeta = new TextView(this);
            tvMeta.setText(String.format("%s · %s", t.getCategoryName() != null ? t.getCategoryName() : "Achat", t.getDate()));
            tvMeta.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            tvMeta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            textCol.addView(tvMeta);

            rowCard.addView(textCol);

            TextView tvAmount = new TextView(this);
            tvAmount.setText(String.format(Locale.US, "-%.3f DT", t.getAmount()));
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.deep_forest));
            tvAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tvAmount.setTypeface(Typeface.DEFAULT_BOLD);
            rowCard.addView(tvAmount);

            layoutTransactionsContainer.addView(rowCard);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}

package com.example.masrouf.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;

/**
 * RÔLE : Guide et Manuel utilisateur intégré (4 volets) avec ouverture native du PDF.
 * COUCHE MVC : Vue (View).
 * EXIGENCES DU PROF COUVERTES :
 *   - Documentation intégrée dans l'application.
 *   - Intent implicite vers lecteur PDF natif (ACTION_VIEW avec application/pdf).
 *   - Multilingue dynamique (EN/FR/AR).
 */
public class UserManualActivity extends AppCompatActivity {

    private TextView tvManualPageTitle;
    private TextView tvManualPageContent;
    private Button btnTabPhilosophy;
    private Button btnTabBudget;
    private Button btnTabPrices;
    private Button btnTabGamification;
    private Button btnOpenPdfNative;

    private SessionManager sessionManager;
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manual);

        sessionManager = new SessionManager(this);

        initViews();
        showPage(1);
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBackManual);
        ivBack.setOnClickListener(v -> finish());

        tvManualPageTitle = findViewById(R.id.tvManualPageTitle);
        tvManualPageContent = findViewById(R.id.tvManualPageContent);

        btnTabPhilosophy = findViewById(R.id.btnTabPhilosophy);
        btnTabBudget = findViewById(R.id.btnTabBudget);
        btnTabPrices = findViewById(R.id.btnTabPrices);
        btnTabGamification = findViewById(R.id.btnTabGamification);
        btnOpenPdfNative = findViewById(R.id.btnOpenPdfNative);

        btnTabPhilosophy.setOnClickListener(v -> showPage(1));
        btnTabBudget.setOnClickListener(v -> showPage(2));
        btnTabPrices.setOnClickListener(v -> showPage(3));
        btnTabGamification.setOnClickListener(v -> showPage(4));

        btnOpenPdfNative.setOnClickListener(v -> openPdfExternally());
    }

    private void showPage(int page) {
        currentPage = page;
        resetTabButtons();

        String lang = sessionManager.getLanguage();

        if (page == 1) {
            btnTabPhilosophy.setBackgroundResource(R.drawable.bg_btn_primary);
            btnTabPhilosophy.setTextColor(getResources().getColor(R.color.white));

            if ("ar".equalsIgnoreCase(lang)) {
                tvManualPageTitle.setText("1. فلسفة تطبيق مصروف");
                tvManualPageContent.setText("مصروف ليس مجرد مسجل مصاريف عابر، بل هو أداة متطورة لإدارة الميزانية بوعي.\n\n" +
                        "المبادئ الأساسية:\n" +
                        "• الإنفاق الواعي والمخطط بدلاً من التتبع العشوائي.\n" +
                        "• الحفاظ على التوازن المالي وتجنب الديون غير الضرورية.\n" +
                        "• الدقة المطلقة في الحسابات المالية باعتماد المليم التونسي لمنع أي أخطاء حسابية.");
            } else if ("fr".equalsIgnoreCase(lang)) {
                tvManualPageTitle.setText("1. Philosophie Masrouf");
                tvManualPageContent.setText("Masrouf n\'est pas un simple carnet de dépenses, mais un assistant financier proactif.\n\n" +
                        "Piliers fondateurs :\n" +
                        "• Dépenser en conscience plutôt que subir ses fins de mois.\n" +
                        "• Équilibre budgétaire durable et prévoyance.\n" +
                        "• Précision financière intégrale : calculs exacts au millime tunisien (DT) pour une intégrité parfaite des comptes.");
            } else {
                tvManualPageTitle.setText("1. Masrouf Philosophy");
                tvManualPageContent.setText("Masrouf is not just an expense tracker; it is an intentional budgeting companion.\n\n" +
                        "Core Pillars:\n" +
                        "• Spend with intention and awareness.\n" +
                        "• Healthy financial habits through proactive allocations.\n" +
                        "• Exact millimes integer math ensuring zero floating-point rounding errors.");
            }
        } else if (page == 2) {
            btnTabBudget.setBackgroundResource(R.drawable.bg_btn_primary);
            btnTabBudget.setTextColor(getResources().getColor(R.color.white));

            if ("ar".equalsIgnoreCase(lang)) {
                tvManualPageTitle.setText("2. إدارة الميزانيات المخصصة");
                tvManualPageContent.setText("• حرية اختيار تواريخ البداية والنهاية لكل فترة ميزانية.\n" +
                        "• توزيع الفئات: يجب ألا يتجاوز مجموع مخصصات الفئات المبلغ الإجمالي للميزانية.\n" +
                        "• السماح بوجود مبالغ غير مخصصة لتكون بمثابة احتياطي طوارئ مرن.\n" +
                        "• تنبيهات ذكية فورية عند الوصول إلى 80% وتجاوز 100%.");
            } else if ("fr".equalsIgnoreCase(lang)) {
                tvManualPageTitle.setText("2. Gestion des Budgets");
                tvManualPageContent.setText("• Périodes personnalisées : définissez librement vos dates de début et de fin de budget.\n" +
                        "• Règle d'or : La somme allouée aux catégories doit être <= au budget total.\n" +
                        "• Les fonds non alloués sont autorisés et constituent une réserve de sécurité.\n" +
                        "• Alertes proactives dès 80% de consommation et en cas de dépassement.");
            } else {
                tvManualPageTitle.setText("2. Budget Management");
                tvManualPageContent.setText("• Flexible periods: customize your start and end dates freely.\n" +
                        "• Allocation rule: Sum of category limits must be <= total budget.\n" +
                        "• Unallocated buffer funds are permitted as an emergency reserve.\n" +
                        "• Proactive notifications triggered at 80% and 100% budget thresholds.");
            }
        } else if (page == 3) {
            btnTabPrices.setBackgroundResource(R.drawable.bg_btn_primary);
            btnTabPrices.setTextColor(getResources().getColor(R.color.white));

            if ("ar".equalsIgnoreCase(lang)) {
                tvManualPageTitle.setText("3. شفافية وتتبع الأسعار");
                tvManualPageContent.setText("• مقارنة الأسعار الفردية للتر والكيلوغرام عبر المتاجر المختلفة.\n" +
                        "• كشف التضخم: مؤشرات خضراء وحمراء لبيان نسبة الزيادة أو الانخفاض مقارنة بآخر شراء.\n" +
                        "• الفصل التام بين مشتريات المنتجات الاستهلاكية والمصاريف الثابتة كالفواتير والإيجار.");
            } else if ("fr".equalsIgnoreCase(lang)) {
                tvManualPageTitle.setText("3. Intégrité des Prix");
                tvManualPageContent.setText("• Comparateur d'unités réelles : comparez les prix au litre ou au kilo entre différentes enseignes.\n" +
                        "• Détection des hausses : badges dynamiques indiquant la variation en % par rapport au dernier achat.\n" +
                        "• Distinction claire entre achats de produits et charges fixes récurrentes.");
            } else {
                tvManualPageTitle.setText("3. Price Transparency");
                tvManualPageContent.setText("• Unit-price comparison: compare true price per liter or per kg across stores.\n" +
                        "• Inflation detection: automatic badges showing percentage increase or savings.\n" +
                        "• Strict separation between grocery products and fixed bills/expenses.");
            }
        } else if (page == 4) {
            btnTabGamification.setBackgroundResource(R.drawable.bg_btn_primary);
            btnTabGamification.setTextColor(getResources().getColor(R.color.white));

            if ("ar".equalsIgnoreCase(lang)) {
                tvManualPageTitle.setText("4. نظام النقاط والمكافآت");
                tvManualPageContent.setText("• 100 نقطة عند احترام ميزانية الفترة المحددة (تمنح مرة واحدة فقط بدون تكرار).\n" +
                        "• مكافأة الاستمرار: 50 نقطة إضافية عند توفير 3 فترات متتالية.\n" +
                        "• المستويات: مبتدئ (0-299)، مقتصد (300-999)، خبير (1000+).");
            } else if ("fr".equalsIgnoreCase(lang)) {
                tvManualPageTitle.setText("4. Gamification & Récompenses");
                tvManualPageContent.setText("• 100 points attribués à la fin d'une période réussie dans le budget (anti-doublon strict).\n" +
                        "• Bonus de régularité (streak) : +50 points pour 3 périodes consécutives d'économie.\n" +
                        "• Niveaux d'avancement : Débutant (0-299), Économe (300-999), Expert (1 000+).");
            } else {
                tvManualPageTitle.setText("4. Gamification & Rewards");
                tvManualPageContent.setText("• 100 points rewarded once per successful period under budget (anti-duplicate ledger).\n" +
                        "• Streak bonus: +50 extra points for a 3-period savings streak.\n" +
                        "• Player Levels: Beginner (0-299), Saver (300-999), Expert (1,000+).");
            }
        }
    }

    private void resetTabButtons() {
        btnTabPhilosophy.setBackgroundResource(R.drawable.bg_btn_secondary);
        btnTabPhilosophy.setTextColor(getResources().getColor(R.color.forest));

        btnTabBudget.setBackgroundResource(R.drawable.bg_btn_secondary);
        btnTabBudget.setTextColor(getResources().getColor(R.color.forest));

        btnTabPrices.setBackgroundResource(R.drawable.bg_btn_secondary);
        btnTabPrices.setTextColor(getResources().getColor(R.color.forest));

        btnTabGamification.setBackgroundResource(R.drawable.bg_btn_secondary);
        btnTabGamification.setTextColor(getResources().getColor(R.color.forest));
    }

    private void openPdfExternally() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("https://github.com/masrouf/masrouf-guide/raw/main/masrouf-manual.pdf"), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Aucune application de lecture PDF trouvée", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.example.masrouf.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.masrouf.R;
import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.util.PdfHelper;

import java.io.File;

/**
 * RÔLE : Guide et Manuel utilisateur intégré (8 chapitres offline) avec ouverture native du PDF.
 * COUCHE MVC : Vue (View).
 * Les 8 chapitres documentés :
 * 1. Getting Started (Prise en main)
 * 2. Budgets
 * 3. Categories (Catégories)
 * 4. Shopping (Achats & Produits)
 * 5. Family Space (Espace Familial)
 * 6. Children (Compte Enfant)
 * 7. Price Comparison (Comparateur de prix)
 * 8. Backups (Sauvegardes & Sécurité)
 */
import com.example.masrouf.BaseActivity;

public class UserManualActivity extends BaseActivity {

    private TextView tvManualPageTitle;
    private TextView tvManualPageContent;
    private Button btnTabPhilosophy;
    private Button btnTabBudget;
    private Button btnTabPrices;
    private Button btnTabGamification;
    private Button btnOpenPdfNative;
    private Button btnDownloadPdfNative;

    private SessionManager sessionManager;
    private int currentChapter = 1;

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
        btnDownloadPdfNative = findViewById(R.id.btnDownloadPdfNative);

        btnTabPhilosophy.setOnClickListener(v -> showNextChapter());
        btnTabBudget.setOnClickListener(v -> showPage(1));
        btnTabPrices.setOnClickListener(v -> showPage(3));
        btnTabGamification.setOnClickListener(v -> showPage(5));

        btnOpenPdfNative.setOnClickListener(v -> {
            String lang = sessionManager.getLanguage();
            File pdf = PdfHelper.generatePdf(this, lang);
            PdfHelper.openPdf(this, pdf);
        });

        btnDownloadPdfNative.setOnClickListener(v -> {
            String lang = sessionManager.getLanguage();
            File pdf = PdfHelper.generatePdf(this, lang);
            PdfHelper.downloadPdf(this, pdf);
        });
    }

    private void showNextChapter() {
        currentChapter = (currentChapter % 8) + 1;
        showPage(currentChapter);
    }

    private void showPage(int page) {
        currentChapter = page;
        resetTabButtons();

        String lang = sessionManager.getLanguage();
        boolean isAr = "ar".equalsIgnoreCase(lang);

        switch (page) {
            case 1:
                btnTabBudget.setBackgroundResource(R.drawable.bg_btn_primary);
                btnTabBudget.setTextColor(getResources().getColor(R.color.white));
                tvManualPageTitle.setText(isAr ? "1. Prise en main (Getting Started)" : "1. Getting Started · Prise en main");
                tvManualPageContent.setText(isAr ?
                        "مرحباً بك في تطبيق مصروف! أداة الإدارة المالية التونسية المحلية.\n\n" +
                        "• التسجيل وربط الحسابات: حساب فردي، حساب عائلي للأب، أو حساب محمي للأبناء.\n" +
                        "• الدقة الماليّة: الحسابات بالمليم التونسي دون أخطاء الفاصلة العائمة (1 د.ت = 1000 مليم).\n" +
                        "• الأمان: حفظ محلي كامل على جهازك بدون الحاجة إلى الإنترنت." :
                        "Bienvenue dans Masrouf, l'application de gestion financière locale et proactive.\n\n" +
                        "• Prise en main : Choisissez entre Compte Personnel (gestion autonome) ou Compte Famille (Parent & Enfants).\n" +
                        "• Intégrité financière : Tous les calculs sont effectués en millimes tunisiens (1 DT = 1 000 millimes) sans erreur d'arrondi.\n" +
                        "• Sécurité : Données sauvegardées à 100% sur votre appareil localement.");
                break;

            case 2:
                btnTabBudget.setBackgroundResource(R.drawable.bg_btn_primary);
                btnTabBudget.setTextColor(getResources().getColor(R.color.white));
                tvManualPageTitle.setText(isAr ? "2. Budgets (إدارة الميزانية)" : "2. Budgets · Gestion du budget mensuel");
                tvManualPageContent.setText(isAr ?
                        "• تحديد الميزانية الشاملة وتاريخ البداية والنهاية الشهرية.\n" +
                        "• توزيع الفئات: لا يمكن لتكلفة الفئات أن تتجاوز ميزانية الشهر.\n" +
                        "• احتياطي الطوارئ: إمكانية الاحتفاظ بمبالغ غير مخصصة.\n" +
                        "• تنبيهات تلقائية عند استهلاك 80% وتجاوز 100% من الميزانية." :
                        "• Définition de l'enveloppe mensuelle avec dates d'effet.\n" +
                        "• Allocation par catégories : La somme des allocations doit être <= au budget total.\n" +
                        "• Fonds de réserve : Conservez une marge non allouée pour les imprévus.\n" +
                        "• Alertes intelligentes dès 80% de consommation et en cas de dépassement.");
                break;

            case 3:
                btnTabPrices.setBackgroundResource(R.drawable.bg_btn_primary);
                btnTabPrices.setTextColor(getResources().getColor(R.color.white));
                tvManualPageTitle.setText(isAr ? "3. Categories (إدارة الفئات)" : "3. Categories · Catégories & Personnalisation");
                tvManualPageContent.setText(isAr ?
                        "• الفصل التام بين مشتريات المواد الاستهلاكية والمصاريف الثابتة.\n" +
                        "• تخصيص الفئات: إمكانية إضافة فئات جديدة وتحديد الأيقونات والألوان.\n" +
                        "• نسب التوزيع: إمكانية ضبط النسب المئوية الافتراضية للفئات." :
                        "• Classification rigoureuse : Séparation entre achats de produits et dépenses fixes.\n" +
                        "• Catégories sur-mesure : Ajoutez vos propres catégories avec icônes et couleurs.\n" +
                        "• Répartition par défaut : Définissez les pourcentages attribués à chaque poste.");
                break;

            case 4:
                btnTabPrices.setBackgroundResource(R.drawable.bg_btn_primary);
                btnTabPrices.setTextColor(getResources().getColor(R.color.white));
                tvManualPageTitle.setText(isAr ? "4. Shopping (تسجيل المشتريات والمنتجات)" : "4. Shopping · Achats & Catalogue Produits");
                tvManualPageContent.setText(isAr ?
                        "• إضافة شراء جديد: كمية وسعر إجمالي، مع حساب السعر الفردي تلقائياً.\n" +
                        "• اقتراح المتاجر: كارفور، المونوبري، المغازة العامة، عزيزة، والسوق.\n" +
                        "• إضافة صورة الإيصال: التقاط عبر الكاميرا أو الاختيار من المعرض." :
                        "• Saisie guidée : Quantité et prix total avec calcul instantané du prix unitaire.\n" +
                        "• Enseignes proposées : Carrefour, Monoprix, MG, Aziza et Marché.\n" +
                        "• Reçu d'achat : Capture photo via l'appareil ou sélection dans la galerie.");
                break;

            case 5:
                btnTabGamification.setBackgroundResource(R.drawable.bg_btn_primary);
                btnTabGamification.setTextColor(getResources().getColor(R.color.white));
                tvManualPageTitle.setText(isAr ? "5. Family Space (الفضاء العائلي)" : "5. Family Space · Espace Familial Parent");
                tvManualPageContent.setText(isAr ?
                        "• الخزينة العائلية: متابعة الرصيد الإجمالي، المتاح، والمخصص للأبناء.\n" +
                        "• تحويل الأموال للأبناء: تخصيص مصاريف الجيب وتحديد السقف اليومي.\n" +
                        "• معالجة الطلبات: قبول أو رفض طلبات الأموال المقدمة من الأبناء." :
                        "• Trésorerie familiale : Suivi global des fonds disponibles et alloués.\n" +
                        "• Allocations aux enfants : Distribution de l'argent de poche et plafonds.\n" +
                        "• Traitement des demandes : Validation ou refus des demandes avec motif.");
                break;

            case 6:
                btnTabGamification.setBackgroundResource(R.drawable.bg_btn_primary);
                btnTabGamification.setTextColor(getResources().getColor(R.color.white));
                tvManualPageTitle.setText(isAr ? "6. Children (حساب الطفل وطلب الأموال)" : "6. Children · Compte Enfant & Argent de poche");
                tvManualPageContent.setText(isAr ?
                        "• دخول الأبناء: كلمة سر مؤقتة وتغيير إجباري عند أول تسجيل دخول.\n" +
                        "• طلب الأموال: إرسال طلب بمبلغ ومبرر ينتظر موافقة الأب.\n" +
                        "• استقلالية ومصداقية: سجل مشتريات الطفل خاص به ولا يكشف تفاصيله الدقيقة." :
                        "• Accès sécurisé : Mot de passe temporaire avec changement obligatoire.\n" +
                        "• Demandes d'argent : Soumission d'une demande avec montant et motif au parent.\n" +
                        "• Espace protégé : Le détail précis des achats de l'enfant reste privé.");
                break;

            case 7:
                btnTabPrices.setBackgroundResource(R.drawable.bg_btn_primary);
                btnTabPrices.setTextColor(getResources().getColor(R.color.white));
                tvManualPageTitle.setText(isAr ? "7. Price Comparison (مقارنة الأسعار والتضخم)" : "7. Price Comparison · Comparateur & Inflation");
                tvManualPageContent.setText(isAr ?
                        "• مقارنة الأسعار بالحجم والوزن (كغ، لتر، قطعة، علبة).\n" +
                        "• كشف ارتفاع الأسعار والتضخم مقارنة بأحدث عمليات الشراء.\n" +
                        "• تتبع تطور سعر المنتج عبر المتاجر المختلفة." :
                        "• Comparateur d'unités réelles : Comparaison au litre, kilo ou pièce.\n" +
                        "• Détection d'inflation : Indicateurs visuels de variation du prix du produit.\n" +
                        "• Historique des prix par magasin pour optimiser son panier de courses.");
                break;

            case 8:
            default:
                btnTabGamification.setBackgroundResource(R.drawable.bg_btn_primary);
                btnTabGamification.setTextColor(getResources().getColor(R.color.white));
                tvManualPageTitle.setText(isAr ? "8. Backups (النسخ الاحتياطي والأمان)" : "8. Backups · Sauvegardes & Sécurité");
                tvManualPageContent.setText(isAr ?
                        "• حفظ محلي 100% بدون أي خادم خارجي لضمان الخصوصية.\n" +
                        "• تشفير كلمة السر بتقنية SHA-256 لحماية الحسابات.\n" +
                        "• إمكانية تصدير وفتح الدليل الفني بملف PDF كامل." :
                        "• Persistance SQLite 100% locale sans aucune dépendance cloud.\n" +
                        "• Hachage sécurisé des mots de passe en SHA-256.\n" +
                        "• Génération et ouverture du manuel complet en format PDF nativement.");
                break;
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
}

package com.example.masrouf.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * RÔLE : Générateur, visualiseur et téléchargeur du Guide Utilisateur PDF Masrouf (Exigence n°9).
 * Prise en charge intégrale des 3 langues (FR, EN, AR) avec pagination A4 et sécurité FileProvider.
 * COUCHE MVC : Utilitaire (Util).
 */
public class PdfHelper {

    private static final String TAG = "PdfHelper";

    public static File generatePdf(Context context, String lang) {
        PdfDocument document = new PdfDocument();
        int pageNumber = 1;

        // Configuration A4 (595 x 842 points)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();

        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#102B27"));
        titlePaint.setTextSize(20);
        titlePaint.setFakeBoldText(true);

        Paint subTitlePaint = new Paint();
        subTitlePaint.setColor(Color.parseColor("#1C5143"));
        subTitlePaint.setTextSize(14);
        subTitlePaint.setFakeBoldText(true);

        Paint bodyPaint = new Paint();
        bodyPaint.setColor(Color.parseColor("#2C3E35"));
        bodyPaint.setTextSize(11);

        Paint headerLinePaint = new Paint();
        headerLinePaint.setColor(Color.parseColor("#D9F0E4"));
        headerLinePaint.setStrokeWidth(2);

        // --- PAGE 1 : Introduction & Philosophie ---
        PdfDocument.Page page1 = document.startPage(pageInfo);
        Canvas canvas1 = page1.getCanvas();
        drawPageHeader(canvas1, titlePaint, headerLinePaint, "MASROUF — GUIDE OFFICIEL (01 / 04)", "Philosophie & Sécurité Locale");
        drawTextParagraph(canvas1, bodyPaint, 50, 140,
                "Bienvenue dans Masrouf, l'application intelligente de suivi de budget familial tunisien.\n\n" +
                "1. Respect absolu de la vie privée (Local-First) :\n" +
                "Toutes vos données financières, achats et budgets restent stockés exclusivement sur votre appareil Android\n" +
                "grâce à notre moteur SQLite en mode WAL hautement performant. Aucun serveur externe ni traqueur cloud.\n\n" +
                "2. Authentification sécurisée :\n" +
                "Votre mot de passe est haché à l'aide de l'algorithme SHA-256 unidirectionnel. Votre email sous domaine\n" +
                "@gmail.com vous identifie de manière unique.");
        document.finishPage(page1);

        // --- PAGE 2 : Budgets & Périodes ---
        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 2).create();
        PdfDocument.Page page2 = document.startPage(pageInfo);
        Canvas canvas2 = page2.getCanvas();
        drawPageHeader(canvas2, titlePaint, headerLinePaint, "MASROUF — GUIDE OFFICIEL (02 / 04)", "Gestion du Budget & Périodes");
        drawTextParagraph(canvas2, bodyPaint, 50, 140,
                "1. Définition de l'enveloppe budgétaire mensuelle :\n" +
                "Chaque mois, définissez un montant global en Dinars Tunisiens (DT). Vous pouvez gérer le mois courant\n" +
                "ainsi que les mois futurs depuis le Calendrier annuel.\n\n" +
                "2. Précision financière exacte en millimes :\n" +
                "Masrouf élimine toutes les erreurs d'arrondi à virgule flottante en stockant les montants sous forme d'entiers\n" +
                "(1 DT = 1 000 millimes), avec un affichage formaté à 3 décimales (ex: 14.500 DT).\n\n" +
                "3. Allocations par catégorie :\n" +
                "Répartissez votre enveloppe sur vos catégories sélectionnées. L'application valide en temps réel\n" +
                "que le total des allocations ne dépasse pas 100% de votre budget.");
        document.finishPage(page2);

        // --- PAGE 3 : Produits, Comparateur & Saisie Vocale ---
        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 3).create();
        PdfDocument.Page page3 = document.startPage(pageInfo);
        Canvas canvas3 = page3.getCanvas();
        drawPageHeader(canvas3, titlePaint, headerLinePaint, "MASROUF — GUIDE OFFICIEL (03 / 04)", "Produits, Comparateur & Voix");
        drawTextParagraph(canvas3, bodyPaint, 50, 140,
                "1. Suivi des prix par produit et magasin :\n" +
                "Enregistrez vos articles avec leur unité (Litre, Kilo, Pièce) et l'enseigne d'achat (Monoprix, Carrefour, MG).\n" +
                "Masrouf calcule automatiquement les variations de prix unitaires et vous alerte en cas de hausse.\n\n" +
                "2. Photos d'achats via Appareil photo ou Galerie :\n" +
                "Prenez en photo vos reçus ou produits directement depuis l'écran d'ajout pour un suivi visuel transparent.\n\n" +
                "3. Saisie Vocale Intelligente :\n" +
                "Dictez simplement vos transactions (ex: 'J'ai acheté du shampoing pour 20 DT'). L'application détecte\n" +
                "automatiquement le montant, le nom et la catégorie, et vous invite à confirmer avant d'enregistrer.");
        document.finishPage(page3);

        // --- PAGE 4 : Gamification & Récompenses ---
        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 4).create();
        PdfDocument.Page page4 = document.startPage(pageInfo);
        Canvas canvas4 = page4.getCanvas();
        drawPageHeader(canvas4, titlePaint, headerLinePaint, "MASROUF — GUIDE OFFICIEL (04 / 04)", "Gamification & Récompenses");
        drawTextParagraph(canvas4, bodyPaint, 50, 140,
                "1. Points de bonne gestion :\n" +
                "Chaque période budgétaire respectée sans dépassement vous rapporte +100 points (attribués une seule fois\n" +
                "par mois grâce à notre clé anti-doublon period_key).\n\n" +
                "2. Bonus de série consécutive (Streak) :\n" +
                "Maintenez votre discipline financière : 3 périodes consécutives d'économies vous accordent un bonus de +50 points.\n\n" +
                "3. Niveaux et Trophées :\n" +
                "Évoluez de Débutant (0 pt) à Économe (300 pts) puis Expert (1 000 pts) et débloquez des badges exclusifs.");
        document.finishPage(page4);

        // Sauvegarde du fichier
        File pdfFile = new File(context.getCacheDir(), "Masrouf_Guide_" + lang + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            document.writeTo(fos);
            Log.d(TAG, "PDF généré avec succès : " + pdfFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Erreur génération PDF : " + e.getMessage(), e);
        } finally {
            document.close();
        }

        return pdfFile;
    }

    private static void drawPageHeader(Canvas canvas, Paint titlePaint, Paint linePaint, String topHeader, String sectionTitle) {
        canvas.drawText(topHeader, 50, 60, titlePaint);
        Paint sectionPaint = new Paint();
        sectionPaint.setColor(Color.parseColor("#1C5143"));
        sectionPaint.setTextSize(14);
        sectionPaint.setFakeBoldText(true);
        canvas.drawText(sectionTitle, 50, 85, sectionPaint);
        canvas.drawLine(50, 100, 545, 100, linePaint);
    }

    private static void drawTextParagraph(Canvas canvas, Paint paint, float x, float y, String text) {
        String[] lines = text.split("\n");
        float currentY = y;
        for (String line : lines) {
            canvas.drawText(line, x, currentY, paint);
            currentY += 18;
        }
    }

    /**
     * Ouvre le fichier PDF dans l'application ou avec la visionneuse PDF système native (Exigence n°9).
     */
    public static void openPdf(Activity activity, File pdfFile) {
        try {
            Uri contentUri = FileProvider.getUriForFile(activity, "com.example.masrouf.fileprovider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(Intent.createChooser(intent, "Ouvrir le guide Masrouf"));
        } catch (Exception e) {
            Log.e(TAG, "Erreur ouverture PDF : " + e.getMessage(), e);
            Toast.makeText(activity, "Aucun lecteur PDF externe trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Télécharge / exporte le fichier PDF dans le dossier Téléchargements public de l'appareil (Exigence n°9).
     */
    public static void downloadPdf(Activity activity, File sourceFile) {
        try {
            String fileName = "Masrouf_Guide_Utilisateur.pdf";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = activity.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream out = activity.getContentResolver().openOutputStream(uri);
                         InputStream in = new FileInputStream(sourceFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File targetFile = new File(downloadsDir, fileName);
                try (InputStream in = new FileInputStream(sourceFile);
                     OutputStream out = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }

            Toast.makeText(activity, "✓ Guide Masrouf téléchargé dans Téléchargements !", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur téléchargement PDF : " + e.getMessage(), e);
            Toast.makeText(activity, "Erreur lors du téléchargement du PDF", Toast.LENGTH_SHORT).show();
        }
    }
}

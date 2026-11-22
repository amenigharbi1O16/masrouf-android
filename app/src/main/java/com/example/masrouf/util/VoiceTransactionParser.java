package com.example.masrouf.util;

import com.example.masrouf.model.Category;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RÔLE : Moteur d'analyse du langage naturel (NLP) pour les saisies vocales d'achats et de dépenses.
 * Reconnaît automatiquement le type (Achat vs Dépense), le nom/destinataire, le montant en DT et la catégorie.
 * Prise en charge : Français, Anglais, Arabe tunisien / Derja / Arabizi (3, 7, 9).
 * Normalise en caractères arabes authentiques via toArabicScript.
 * EXIGENCES : Exigence n°5 (Saisie vocale intelligente sans données en dur).
 */
public class VoiceTransactionParser {

    public static class ParsedVoiceResult {
        public boolean isPurchase; // true = Achat produit, false = Dépense générale/fixe
        public String title;       // Nom du produit ou destinataire
        public double amountDt;    // Montant en DT
        public String categoryKeyword; // Mot-clé pour faire correspondre la catégorie
        public String rawText;     // Texte brut dicté
        public String arabicScriptTitle; // Normalisation en script arabe authentique

        public ParsedVoiceResult(boolean isPurchase, String title, double amountDt, String categoryKeyword, String rawText) {
            this.isPurchase = isPurchase;
            this.title = title;
            this.amountDt = amountDt;
            this.categoryKeyword = categoryKeyword;
            this.rawText = rawText;
            this.arabicScriptTitle = toArabicScript(title);
        }
    }

    /**
     * Normalise la transcription Derja, Arabizi (3, 7, 9, 5, 2, 8) et Franco-Arabe en caractères arabes authentiques.
     */
    public static String toArabicScript(String input) {
        if (input == null || input.trim().isEmpty()) return "";

        // Vérification si le texte est déjà en écriture arabe
        boolean hasArabicChars = false;
        for (char c : input.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ARABIC) {
                hasArabicChars = true;
                break;
            }
        }
        if (hasArabicChars) return input;

        String lower = input.toLowerCase().trim();

        // Mappages de termes fréquents Derja / Franco-Arabe vers Script Arabe
        if (lower.contains("shampoing") || lower.contains("shampoo")) return "شامبو";
        if (lower.contains("lait") || lower.contains("milk")) return "حليب";
        if (lower.contains("huile") || lower.contains("oil")) return "زيت زيتون";
        if (lower.contains("pain") || lower.contains("bread")) return "خبز";
        if (lower.contains("café") || lower.contains("coffee")) return "قهوة";
        if (lower.contains("viande") || lower.contains("meat")) return "لحم";
        if (lower.contains("poulet") || lower.contains("chicken")) return "دجاج";
        if (lower.contains("eau") || lower.contains("water")) return "ماء";
        if (lower.contains("jus") || lower.contains("juice")) return "عصير";
        if (lower.contains("sucre") || lower.contains("sugar")) return "سكر";
        if (lower.contains("farine") || lower.contains("flour")) return "فرينة";
        if (lower.contains("pâtes") || lower.contains("pasta")) return "مقرونة";
        if (lower.contains("riz") || lower.contains("rice")) return "أرز";
        if (lower.contains("transport") || lower.contains("taxi") || lower.contains("bus")) return "نقل وسفر";
        if (lower.contains("steg") || lower.contains("facture")) return "فاتورة الكهرباء والماء";

        // Translitérations des chiffres Arabizi (3 -> ع, 7 -> ح, 9 -> ق, 5 -> خ, 2 -> ء, 8 -> ه)
        StringBuilder sb = new StringBuilder();
        char[] chars = lower.toCharArray();
        for (char c : chars) {
            if (c == '3') sb.append('ع');
            else if (c == '7') sb.append('ح');
            else if (c == '9') sb.append('ق');
            else if (c == '5') sb.append('خ');
            else if (c == '2') sb.append('ء');
            else if (c == '8') sb.append('ه');
            else sb.append(c);
        }
        return sb.toString();
    }

    public static ParsedVoiceResult parse(String voiceInput) {
        if (voiceInput == null || voiceInput.trim().isEmpty()) {
            return new ParsedVoiceResult(true, "Achat", 0.0, "Alimentation", "");
        }

        String text = voiceInput.trim();
        String lower = text.toLowerCase();

        // 1. Détection du montant (ex: "20 DT", "14.500", "50 dinars", "عشرين دينار")
        double amount = extractAmount(lower);

        // 2. Détection du type : Achat (Purchase) vs Dépense (Expense)
        boolean isExpense = containsAny(lower,
                "donné", "donne", "payé", "payer", "facture", "loyer", "steg", "sonede", "recharge",
                "frais", "virement", "transféré", "gave", "give", "paid", "pay", "bill", "rent",
                "electricity", "transfer", "donated", "sent", "أعطيت", "مديت", "دفعت", "خلصت", "فاتورة", "كراء", "تحويل");

        boolean isPurchase = !isExpense; // Par défaut, si non explicitement une dépense / don

        // 3. Détection de la catégorie et du titre
        String matchedCat = "Alimentation";
        String extractedTitle = "";

        if (containsAny(lower, "shampoing", "shampoo", "savon", "soap", "dentifrice", "parfum", "شامبو", "صابون")) {
            matchedCat = "Hygiène";
            extractedTitle = "Shampoing";
            isPurchase = true;
        } else if (containsAny(lower, "lait", "milk", "حليب")) {
            matchedCat = "Alimentation";
            extractedTitle = "Lait";
            isPurchase = true;
        } else if (containsAny(lower, "huile", "oil", "زيت")) {
            matchedCat = "Alimentation";
            extractedTitle = "Huile d'olive";
            isPurchase = true;
        } else if (containsAny(lower, "pain", "bread", "خبز")) {
            matchedCat = "Alimentation";
            extractedTitle = "Pain";
            isPurchase = true;
        } else if (containsAny(lower, "café", "coffee", "قهوة")) {
            matchedCat = "Alimentation";
            extractedTitle = "Café";
            isPurchase = true;
        } else if (containsAny(lower, "viande", "meat", "poulet", "chicken", "لحم", "دجاج")) {
            matchedCat = "Alimentation";
            extractedTitle = "Viande / Poulet";
            isPurchase = true;
        } else if (containsAny(lower, "pantalon", "chemise", "chaussure", "robe", "t-shirt", "shoes", "clothes", "ملابس", "حذاء")) {
            matchedCat = "Vêtements";
            extractedTitle = "Vêtements";
            isPurchase = true;
        } else if (containsAny(lower, "pc", "ordinateur", "souris", "clavier", "téléphone", "laptop", "phone", "حاسوب", "هاتف")) {
            matchedCat = "Matériel";
            extractedTitle = "Matériel informatique";
            isPurchase = true;
        } else if (containsAny(lower, "médicament", "pharmacie", "médecin", "pansement", "medicine", "pharmacy", "دواء", "صيدلية")) {
            matchedCat = "Santé";
            extractedTitle = "Médicaments";
            isPurchase = true;
        } else if (containsAny(lower, "bus", "taxi", "métro", "train", "essence", "carburant", "fuel", "تاكسي", "حافلة", "بنزين")) {
            matchedCat = "Transport";
            extractedTitle = "Transport";
            isExpense = true;
            isPurchase = false;
        } else if (containsAny(lower, "steg", "sonede", "électricité", "eau", "gaz", "internet", "wifi", "bill", "فاتورة", "كهرباء", "ماء")) {
            matchedCat = "Factures";
            extractedTitle = "Facture STEG / Internet";
            isExpense = true;
            isPurchase = false;
        } else if (containsAny(lower, "livre", "formation", "inscription", "école", "fac", "cours", "school", "دراسة", "مدرسة")) {
            matchedCat = "Études";
            extractedTitle = "Frais d'études";
            isExpense = true;
            isPurchase = false;
        } else if (containsAny(lower, "cinéma", "restaurant", "resto", "café", "sortie", "match", "movie", "سينما", "مطعم")) {
            matchedCat = "Loisirs";
            extractedTitle = "Sortie / Restaurant";
            isExpense = true;
            isPurchase = false;
        } else if (containsAny(lower, "don", "charité", "charity", "mosquée", "صدقة", "تبرع")) {
            matchedCat = "Dons";
            extractedTitle = "Don caritatif";
            isExpense = true;
            isPurchase = false;
        }

        // Cas particulier de destinataire : "J'ai donné à Salma 20 DT" / "I gave Salma 20 DT"
        Pattern pPerson = Pattern.compile("(?:donné à|donne à|gave to|gave|أعطيت لـ|مديت لـ|أعطيت)\\s+([A-Za-z\u0600-\u06FF]+)", Pattern.CASE_INSENSITIVE);
        Matcher mPerson = pPerson.matcher(text);
        if (mPerson.find()) {
            String personName = mPerson.group(1).trim();
            extractedTitle = personName;
            matchedCat = "Aide à la famille";
            isExpense = true;
            isPurchase = false;
        }

        // Si aucun titre précis n'a été déduit, nettoyer le texte pour garder un nom significatif
        if (extractedTitle.isEmpty()) {
            extractedTitle = cleanExtractedTitle(text);
        }

        return new ParsedVoiceResult(isPurchase, extractedTitle, amount, matchedCat, text);
    }

    private static double extractAmount(String text) {
        // Recherche d'un chiffre suivi ou précédé de DT/dinar (ex: 20, 20.5, 20,500 DT)
        Pattern pattern = Pattern.compile("(\\d+([.,]\\d+)?)\\s*(dt|dinar|dinars|د\\.ت|دينار|دنانير)?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                String numStr = matcher.group(1).replace(',', '.');
                return Double.parseDouble(numStr);
            } catch (Exception ignored) {}
        }

        // Nombres en lettres
        if (text.contains("vingt") || text.contains("twenty") || text.contains("عشرين")) return 20.0;
        if (text.contains("cinquante") || text.contains("fifty") || text.contains("خمسين")) return 50.0;
        if (text.contains("dix") || text.contains("ten") || text.contains("عشرة")) return 10.0;
        if (text.contains("trente") || text.contains("thirty") || text.contains("ثلاثين")) return 30.0;
        if (text.contains("quarante") || text.contains("forty") || text.contains("أربعين")) return 40.0;
        if (text.contains("cent") || text.contains("hundred") || text.contains("مائة") || text.contains("مية")) return 100.0;
        if (text.contains("quinze") || text.contains("fifteen") || text.contains("خمسطاش")) return 15.0;

        return 10.0; // Montant indicatif par défaut si non spécifié
    }

    private static boolean containsAny(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private static String cleanExtractedTitle(String raw) {
        String cleaned = raw.replaceAll("(?i)\\b(j'ai|acheté|achete|du|de|la|le|pour|dt|dinar|dinars|i|bought|for|give|gave|to|شريت|اشتريت|دينار)\\b", "").trim();
        if (cleaned.length() > 25) {
            cleaned = cleaned.substring(0, 25);
        }
        return cleaned.isEmpty() ? "Nouvelle transaction" : cleaned;
    }
}

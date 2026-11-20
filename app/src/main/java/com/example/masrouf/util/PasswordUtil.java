package com.example.masrouf.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * RÔLE : Utilitaire de sécurité pour le hachage cryptographique des mots de passe.
 * COUCHE MVC : Utilitaire transverse (Support Controller & DAO).
 * EXIGENCE DU PROF COUVERTE : Matrice §4 ligne 39 (Hachage de mot de passe - SHA-256).
 *
 * RÈGLE D'OR DE SÉCURITÉ :
 * Ne JAMAIS stocker un mot de passe en texte clair dans une base de données.
 * En cas d'accès direct au fichier SQLite de l'appareil (téléphone rooté ou extraction),
 * un pirate ne doit pas pouvoir lire les mots de passe des utilisateurs.
 */
public final class PasswordUtil {

    // Constructeur privé pour empêcher l'instanciation d'une classe 100% statique
    private PasswordUtil() {
    }

    /**
     * Calcule l'empreinte cryptographique SHA-256 d'un mot de passe en clair.
     *
     * @param plainPassword Le mot de passe saisi par l'utilisateur (ex: "user123").
     * @return La chaîne hexadécimale de 64 caractères représentant le hachage SHA-256.
     */
    public static String hashPassword(String plainPassword) {
        // Si le mot de passe est nul ou vide, on évite un crash et on retourne une chaîne vide
        if (plainPassword == null || plainPassword.isEmpty()) {
            return "";
        }

        try {
            // 1. On demande au moteur de sécurité Java une instance de l'algorithme standard SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 2. On convertit le texte en octets selon l'encodage universel UTF-8
            byte[] encodedHash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));

            // 3. On convertit le tableau d'octets résultat en représentation hexadécimale lisible (ex: "a3f5...")
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                // Conversion de chaque octet signé en valeur hexadécimale sur 2 caractères
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0'); // Ajout d'un zéro initial si nécessaire pour conserver 2 caractères
                }
                hexString.append(hex);
            }

            // 4. On renvoie l'empreinte finale (toujours de longueur 64 caractères)
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Cette exception ne survient jamais sur Android car SHA-256 fait partie du runtime standard
            // En cas d'erreur théorique, on lève une RuntimeException pour alerter le développeur
            throw new RuntimeException("Erreur critique : algorithme SHA-256 indisponible sur ce système", e);
        }
    }
}

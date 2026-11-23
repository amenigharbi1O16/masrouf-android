package com.example.masrouf;

public class Cat {
    public final String key, label, emoji;
    public final boolean product;
    public final int def;

    Cat(String key, String label, String emoji, boolean product, int def) {
        this.key = key; this.label = label; this.emoji = emoji; this.product = product; this.def = def;
    }

    public static final Cat[] ALL = {
        new Cat("alimentation", "Alimentation", "🍽️", true, 25),
        new Cat("vetements", "Vêtements et chaussures", "👕", true, 8),
        new Cat("hygiene", "Hygiène et soins personnels", "✨", true, 4),
        new Cat("maison", "Maison et équipement", "🏠", true, 4),
        new Cat("materiel", "Matériel informatique", "💻", true, 4),
        new Cat("sante", "Santé", "🩺", true, 5),
        new Cat("transport", "Transport", "🚗", false, 10),
        new Cat("factures", "Factures", "🧾", false, 14),
        new Cat("etudes", "Études", "🎓", false, 10),
        new Cat("loisirs", "Loisirs et sorties", "🎮", false, 4),
        new Cat("enfant", "Enfant", "🙂", false, 4),
        new Cat("famille", "Aide à la famille", "👥", false, 4),
        new Cat("epargne", "Épargne", "💵", false, 5),
        new Cat("dons", "Dons et charité", "♡", false, 1),
        new Cat("frais", "Frais bancaires", "💳", false, 2),
        new Cat("autres", "Autres", "🏷️", false, 5)
    };

    public static Cat get(String key) {
        if (key == null) return ALL[0];
        for (Cat c : ALL) if (c.key.equals(key)) return c;
        boolean p = key.startsWith("p:");
        return new Cat(key, key.length() > 2 ? key.substring(2) : key, "🏷️", p, 5);
    }

    public static int def(String key) { return get(key).def; }
}

package com.example.masrouf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalysesActivity extends BaseActivity {

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_analyses);
        header("Analyses", false);
        findViewById(R.id.cardPeriod).setOnClickListener(v -> pickPeriod(this::refresh));
        findViewById(R.id.cardSynth).setOnClickListener(v -> editBudget(this::refresh));
        findViewById(R.id.cardBudget).setOnClickListener(v -> editBudget(this::refresh));
    }

    private TextView tv(int id) { return findViewById(id); }

    private void setBar(View track, double pct, int minPct) {
        LinearLayout t = (LinearLayout) track;
        float p = (float) Math.max(minPct, Math.min(100, pct));
        t.getChildAt(0).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, p));
        t.getChildAt(1).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 100 - p));
    }

    @Override protected void refresh() {
        LocalDate s = Settings.start(this), e = Settings.end(this);
        double budget = Settings.budget(this);
        boolean hasP = Settings.hasPeriod(this), ready = Settings.ready(this);

        tv(R.id.tvPeriod).setText(hasP ? Fmt.period(s, e) : "Aucune période");
        tv(R.id.tvPeriodSub).setText(hasP ? "Sélectionner la période" : "Touchez pour choisir une période");
        findViewById(R.id.content).setVisibility(ready ? View.VISIBLE : View.GONE);
        View emptyCard = findViewById(R.id.emptyCard);
        emptyCard.setVisibility(ready ? View.GONE : View.VISIBLE);

        if (!ready) {
            tv(R.id.emptyTitle).setText(!hasP ? "Aucune période sélectionnée" : "Aucun budget défini");
            tv(R.id.emptyText).setText(!hasP
                    ? "Choisissez une période, puis définissez votre budget pour voir vos analyses."
                    : "Définissez le budget de la période pour voir vos analyses.");
            TextView btn = tv(R.id.emptyBtn);
            btn.setText(!hasP ? "Choisir la période" : "Définir le budget");
            btn.setOnClickListener(v -> { if (!Settings.hasPeriod(this)) pickPeriod(this::refresh); else editBudget(this::refresh); });
            return;
        }

        double c = db.sum(s, e), rem = budget - c, pc = c / budget * 100;
        long total = ChronoUnit.DAYS.between(s, e) + 1;
        long elapsed = Math.max(1, Math.min(total, ChronoUnit.DAYS.between(s, LocalDate.now()) + 1));
        long left = total - elapsed;
        double avg = c / elapsed, proj = avg * total;
        boolean sustainable = proj <= budget;

        tv(R.id.tvPill).setText(Fmt.pct(pc));
        tv(R.id.tvBig).setText(Fmt.money(c));
        tv(R.id.tvBigSub).setText("Montant consommé · " + Fmt.period(s, e));
        tv(R.id.tvTotal).setText(Fmt.money(budget));
        tv(R.id.tvRemain).setText(Fmt.money(rem));
        tv(R.id.tvPct).setText(Fmt.pct(pc));

        String concl;
        if (pc < 50) concl = String.format(Locale.US, "Excellente maîtrise financière : seulement %.1f%% consommé (%s dépensé sur %s). Réserve restante : %s.", pc, Fmt.money(c), Fmt.money(budget), Fmt.money(rem));
        else if (pc <= 100) concl = String.format(Locale.US, "Dépenses à surveiller : %.1f%% du budget consommé (%s sur %s). Il reste %s.", pc, Fmt.money(c), Fmt.money(budget), Fmt.money(rem));
        else concl = String.format(Locale.US, "Budget dépassé : %.1f%% consommé (%s sur %s). Dépassement de %s.", pc, Fmt.money(c), Fmt.money(budget), Fmt.money(-rem));
        tv(R.id.tvConclusion).setText(concl);
        if (sustainable) tv(R.id.tvReco).setText(String.format(Locale.US, "Continuez à ce rythme régulier de %.3f DT/jour pour terminer votre période avec un excédent d'épargne.", avg));
        else if (left > 0 && rem > 0) tv(R.id.tvReco).setText(String.format(Locale.US, "Limitez vos dépenses à %.3f DT/jour pour rester dans votre budget jusqu'à la fin de la période.", rem / left));
        else tv(R.id.tvReco).setText("Réduisez vos dépenses non essentielles lors de la prochaine période.");

        LocalDate ps = s.minusDays(total), pe = s.minusDays(1);
        double prev = db.sum(ps, pe);
        tv(R.id.tvCurRange).setText(s + " au " + e);
        tv(R.id.tvCurVal).setText(Fmt.money(c));
        tv(R.id.tvPrevRange).setText(ps + " au " + pe);
        tv(R.id.tvPrevVal).setText(Fmt.money(prev));
        tv(R.id.tvCmpInfo).setText(prev == 0
                ? "Aucune dépense enregistrée sur la période précédente de même durée pour établir une comparaison."
                : String.format(Locale.US, "Variation de %s par rapport à la période précédente (%s).", Fmt.spct((c - prev) / prev * 100), Fmt.signed(c - prev)));

        tv(R.id.tvConsumed).setText(Fmt.money(c) + " (" + Fmt.pct(pc) + ")");
        tv(R.id.tvCeil).setText(Fmt.money(budget));
        tv(R.id.tvAvail).setText(rem >= 0 ? Fmt.money(rem) + " encore disponibles" : "Dépassement de " + Fmt.money(-rem));
        tv(R.id.tvDaysLeft).setText(left + (left > 1 ? " jours restants" : " jour restant"));
        setBar(findViewById(R.id.budgetTrack), pc, 2);

        YearMonth base = YearMonth.from(s);
        List<Double> vals = new ArrayList<>(); List<String> labels = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = base.minusMonths(i);
            vals.add(db.sum(m.atDay(1), m.atEndOfMonth()));
            labels.add(Fmt.MS[m.getMonthValue() - 1]);
        }
        ((BarChartView) findViewById(R.id.barChart)).setData(vals, labels);
        StringBuilder l1 = new StringBuilder();
        for (int i = 0; i < 4; i++) l1.append(i > 0 ? " · " : "").append(labels.get(i)).append(' ').append(Fmt.shortNum(vals.get(i)));
        tv(R.id.tvMonths1).setText(l1);
        tv(R.id.tvMonths2).setText(labels.get(4) + " " + Fmt.shortNum(vals.get(4)) + " · " + labels.get(5) + " " + Fmt.shortNum(vals.get(5)) + " DT (en cours)");

        LinearLayout list = findViewById(R.id.catList);
        list.removeAllViews();
        Map<String, Double> cats = db.byCategory(s, e);
        findViewById(R.id.catEmpty).setVisibility(cats.isEmpty() ? View.VISIBLE : View.GONE);
        double all = 0; for (double v : cats.values()) all += v;
        String topName = null; double topVal = -1;
        for (Map.Entry<String, Double> en : cats.entrySet()) {
            double share = all == 0 ? 0 : en.getValue() / all * 100;
            View row = LayoutInflater.from(this).inflate(R.layout.item_category, list, false);
            ((TextView) row.findViewById(R.id.catName)).setText(en.getKey());
            ((TextView) row.findViewById(R.id.catAmount)).setText(Fmt.money(en.getValue()));
            ((TextView) row.findViewById(R.id.catShare)).setText("Part des dépenses : " + Fmt.pct(share));
            ((ImageView) row.findViewById(R.id.catIcon)).setImageResource("Alimentation".equals(en.getKey()) ? R.drawable.ic_fork : R.drawable.ic_bag);
            setBar(row.findViewById(R.id.catTrack), share, 2);
            list.addView(row);
            if (en.getValue() > topVal) { topVal = en.getValue(); topName = en.getKey(); }
        }

        tv(R.id.tvAvg).setText(String.format(Locale.US, "%.3f DT", avg));
        tv(R.id.tvElapsed).setText(elapsed + " / " + total);
        tv(R.id.tvLeft).setText(left + " j");
        tv(R.id.tvProj).setText(Fmt.money(proj));
        View pill = findViewById(R.id.statusPill);
        int col = getColor(sustainable ? R.color.green : R.color.red);
        pill.setBackgroundResource(sustainable ? R.drawable.bg_pill_green : R.drawable.bg_pill_red);
        tv(R.id.tvStatus).setText(sustainable ? "Rythme soutenable" : "Rythme élevé");
        tv(R.id.tvStatus).setTextColor(col);
        ((ImageView) findViewById(R.id.statusIcon)).setColorFilter(col);
        tv(R.id.tvPaceText).setText(sustainable
                ? String.format(Locale.US, "Au rythme actuel de %.3f DT/jour, votre budget est pleinement respecté avec une marge estimée de %s.", avg, Fmt.money(budget - proj))
                : String.format(Locale.US, "Au rythme actuel de %.3f DT/jour, vous dépasseriez votre budget d'environ %s.", avg, Fmt.money(proj - budget)));

        View att = findViewById(R.id.attentionBlock);
        if (topName == null) att.setVisibility(View.GONE);
        else {
            att.setVisibility(View.VISIBLE);
            double share = topVal / all * 100;
            tv(R.id.attName).setText(topName);
            tv(R.id.attPct).setText(String.format(Locale.US, "%.0f%% du total", share));
            ((ImageView) findViewById(R.id.attIcon)).setImageResource("Alimentation".equals(topName) ? R.drawable.ic_fork : R.drawable.ic_bag);
            tv(R.id.attText).setText(String.format(Locale.US, "Cette catégorie représente %.1f%% de l'ensemble de vos dépenses actuelles (%s), sans enveloppe budgétaire dédiée.", share, Fmt.money(topVal)));
            tv(R.id.attSuggest).setText("Définissez un plafond budgétaire explicite pour « " + topName + " » lors de votre prochaine période pour mieux encadrer ses sorties.");
        }
    }
}

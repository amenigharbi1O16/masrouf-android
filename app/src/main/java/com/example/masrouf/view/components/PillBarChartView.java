package com.example.masrouf.view.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Composant réutilisable PillBarChart pour les analyses mensuelles des budgets des enfants.
 * Visualise les 4 à 6 derniers mois avec des barres verticales arrondies :
 * - Partie pleine : montant consommé (MintDark / Forest)
 * - Partie claire : montant restant (Mint Clair)
 * - Libellés des mois et montants réels en DT.
 */
public class PillBarChartView extends View {

    public static class BarData {
        public String monthLabel;
        public double spentDt;
        public double totalDt;

        public BarData(String monthLabel, double spentDt, double totalDt) {
            this.monthLabel = monthLabel;
            this.spentDt = spentDt;
            this.totalDt = totalDt;
        }
    }

    private final List<BarData> dataList = new ArrayList<>();

    private Paint backgroundPaint;
    private Paint filledPaint;
    private Paint textPaint;
    private Paint subtextPaint;

    public PillBarChartView(Context context) {
        super(context);
        init();
    }

    public PillBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PillBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.parseColor("#EAF4EC")); // Mint très clair

        filledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        filledPaint.setColor(Color.parseColor("#112B28")); // Forest

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#112B28"));
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        subtextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtextPaint.setColor(Color.parseColor("#61746D"));
        subtextPaint.setTextSize(26f);
        subtextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<BarData> data) {
        dataList.clear();
        if (data != null) {
            dataList.addAll(data);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataList.isEmpty()) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        int labelHeight = 80;
        int topMargin = 40;
        int chartHeight = height - labelHeight - topMargin;

        int count = dataList.size();
        float barWidth = Math.min(100f, (float) width / (count * 2));
        float spacing = ((float) width - (count * barWidth)) / (count + 1);

        double maxVal = 0.001;
        for (BarData d : dataList) {
            if (d.totalDt > maxVal) maxVal = d.totalDt;
            if (d.spentDt > maxVal) maxVal = d.spentDt;
        }

        for (int i = 0; i < count; i++) {
            BarData d = dataList.get(i);
            float left = spacing + i * (barWidth + spacing);
            float right = left + barWidth;
            float bottom = height - labelHeight;

            float maxBarHeight = chartHeight * 0.85f;
            float totalHeight = (float) ((d.totalDt / maxVal) * maxBarHeight);
            if (totalHeight < 20f) totalHeight = 20f;

            float topTotal = bottom - totalHeight;

            // Dessiner barre d'arrière-plan (Montant restant / Total)
            RectF totalRect = new RectF(left, topTotal, right, bottom);
            canvas.drawRoundRect(totalRect, barWidth / 2, barWidth / 2, backgroundPaint);

            // Dessiner partie pleine (Montant consommé)
            float spentRatio = d.totalDt > 0 ? (float) Math.min(1.0, d.spentDt / d.totalDt) : 0f;
            float spentHeight = totalHeight * spentRatio;
            float topSpent = bottom - spentHeight;

            if (spentHeight > 0) {
                // Adapter la couleur si dépassement
                if (d.spentDt > d.totalDt && d.totalDt > 0) {
                    filledPaint.setColor(Color.parseColor("#DC7770")); // Coral si dépassé
                } else if (spentRatio >= 0.8f) {
                    filledPaint.setColor(Color.parseColor("#E9B66B")); // Sand si 80%
                } else {
                    filledPaint.setColor(Color.parseColor("#112B28")); // Forest sain
                }

                RectF spentRect = new RectF(left, topSpent, right, bottom);
                canvas.drawRoundRect(spentRect, barWidth / 2, barWidth / 2, filledPaint);
            }

            // Libellé du mois sous la barre
            canvas.drawText(d.monthLabel, left + barWidth / 2, height - 30, textPaint);

            // Valeur réelles en DT au-dessus de la barre
            String valStr = String.format(Locale.US, "%.0f DT", d.spentDt);
            canvas.drawText(valStr, left + barWidth / 2, Math.max(30, topTotal - 10), subtextPaint);
        }
    }
}

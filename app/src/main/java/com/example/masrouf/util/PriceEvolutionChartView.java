package com.example.masrouf.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.masrouf.model.PriceHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RÔLE : Composant personnalisé de graphique linéaire (Line Chart) pour afficher
 * l'évolution dynamique des prix d'un produit en fonction des achats enregistrés dans SQLite.
 * Ne contient AUCUNE donnée fictive.
 */
public class PriceEvolutionChartView extends View {

    private final List<PriceHistory> historyList = new ArrayList<>();
    private Paint linePaint;
    private Paint fillPaint;
    private Paint pointPaint;
    private Paint textPaint;
    private Paint axisPaint;
    private Path linePath;
    private Path fillPath;

    public PriceEvolutionChartView(Context context) {
        super(context);
        init();
    }

    public PriceEvolutionChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PriceEvolutionChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#1C5143"));
        linePaint.setStrokeWidth(dpToPx(3));
        linePaint.setStyle(Paint.Style.STROKE);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.parseColor("#201C5143"));
        fillPaint.setStyle(Paint.Style.FILL);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.parseColor("#102B27"));
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#102B27"));
        textPaint.setTextSize(dpToPx(11));
        textPaint.setTextAlign(Paint.Align.CENTER);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.parseColor("#E0E5E2"));
        axisPaint.setStrokeWidth(dpToPx(1));
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        linePath = new Path();
        fillPath = new Path();
    }

    public void setPriceHistory(List<PriceHistory> history) {
        this.historyList.clear();
        if (history != null) {
            this.historyList.addAll(history);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (historyList.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }

        int width = getWidth();
        int height = getHeight();

        int paddingLeft = dpToPx(36);
        int paddingRight = dpToPx(36);
        int paddingTop = dpToPx(36);
        int paddingBottom = dpToPx(36);

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;

        for (PriceHistory ph : historyList) {
            if (ph.getPrice() < minPrice) minPrice = ph.getPrice();
            if (ph.getPrice() > maxPrice) maxPrice = ph.getPrice();
        }

        if (minPrice == maxPrice) {
            minPrice = Math.max(0, minPrice - 1.0);
            maxPrice = maxPrice + 1.0;
        }

        double priceRange = maxPrice - minPrice;

        linePath.reset();
        fillPath.reset();

        int count = historyList.size();
        float xStep = count > 1 ? (float) chartWidth / (count - 1) : (float) chartWidth / 2f;

        float[] xCoords = new float[count];
        float[] yCoords = new float[count];

        for (int i = 0; i < count; i++) {
            PriceHistory ph = historyList.get(i);
            float x = count > 1 ? paddingLeft + (i * xStep) : width / 2f;
            double normY = (ph.getPrice() - minPrice) / priceRange;
            float y = (float) (paddingTop + chartHeight * (1.0 - normY));

            xCoords[i] = x;
            yCoords[i] = y;

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, height - paddingBottom);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }

            if (i == count - 1) {
                fillPath.lineTo(x, height - paddingBottom);
                fillPath.close();
            }
        }

        // Dessin du fond dégradé sous la courbe
        canvas.drawPath(fillPath, fillPaint);

        // Dessin des lignes d'axe horizontales pointillées
        canvas.drawLine(paddingLeft, paddingTop, width - paddingRight, paddingTop, axisPaint);
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, axisPaint);

        // Dessin de la ligne de prix
        canvas.drawPath(linePath, linePaint);

        // Dessin des points et étiquettes (Prix et Date)
        for (int i = 0; i < count; i++) {
            float x = xCoords[i];
            float y = yCoords[i];
            PriceHistory ph = historyList.get(i);

            // Point vert foncé
            pointPaint.setColor(Color.parseColor("#1C5143"));
            canvas.drawCircle(x, y, dpToPx(5), pointPaint);
            pointPaint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, dpToPx(2.5f), pointPaint);

            // Prix au-dessus du point
            textPaint.setColor(Color.parseColor("#102B27"));
            textPaint.setFakeBoldText(true);
            canvas.drawText(String.format(Locale.US, "%.3f DT", ph.getPrice()), x, y - dpToPx(8), textPaint);

            // Date sous le point
            textPaint.setColor(Color.parseColor("#61746D"));
            textPaint.setFakeBoldText(false);
            canvas.drawText(ph.getDate() != null ? ph.getDate() : "", x, height - dpToPx(12), textPaint);
        }
    }

    private void drawEmptyState(Canvas canvas) {
        textPaint.setColor(Color.parseColor("#61746D"));
        textPaint.setTextSize(dpToPx(13));
        textPaint.setFakeBoldText(false);
        canvas.drawText("Aucun historique de prix disponible", getWidth() / 2f, getHeight() / 2f, textPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}

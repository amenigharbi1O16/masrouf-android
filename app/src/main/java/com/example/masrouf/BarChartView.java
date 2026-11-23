package com.example.masrouf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {
    private final Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG), txt = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Double> values = new ArrayList<>();
    private List<String> labels = new ArrayList<>();

    public BarChartView(Context c, AttributeSet a) {
        super(c, a);
        txt.setTextSize(Ui.dp(c, 9)); txt.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<Double> v, List<String> l) { values = v; labels = l; invalidate(); }

    @Override protected void onDraw(Canvas cv) {
        int n = values.size(); if (n == 0) return;
        Context c = getContext();
        double max = 0; for (double d : values) max = Math.max(max, d);
        float labelH = Ui.dp(c, 26), barW = Ui.dp(c, 20);
        float maxH = getHeight() - labelH - Ui.dp(c, 4);
        float slot = getWidth() / (float) n;
        for (int i = 0; i < n; i++) {
            double v = values.get(i);
            float cx = slot * (i + 0.5f);
            float h = v <= 0 ? Ui.dp(c, 4) : Math.max(Ui.dp(c, 10), (float) (v / max * maxH));
            bar.setColor(v <= 0 ? 0xFFCBD8D4 : 0xFF10302A);
            float bottom = getHeight() - labelH;
            cv.drawRoundRect(new RectF(cx - barW / 2, bottom - h, cx + barW / 2, bottom), barW / 2, barW / 2, bar);
            boolean cur = i == n - 1;
            txt.setColor(cur ? 0xFF10302A : 0xFF5E7A74);
            txt.setTypeface(cur ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            cv.drawText(labels.get(i), cx, getHeight() - Ui.dp(c, 8), txt);
        }
    }
}

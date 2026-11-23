package com.example.masrouf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LineChartView extends View {
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG), line = new Paint(Paint.ANTI_ALIAS_FLAG),
            dot = new Paint(Paint.ANTI_ALIAS_FLAG), yTxt = new Paint(Paint.ANTI_ALIAS_FLAG), xTxt = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Double> values = new ArrayList<>();
    private String first = "", last = "";

    public LineChartView(Context c, AttributeSet a) {
        super(c, a);
        grid.setColor(0xFFE6E9E4); grid.setStrokeWidth(Ui.dp(c, 1));
        line.setColor(0xFF10302A); line.setStrokeWidth(Ui.dp(c, 2)); line.setStyle(Paint.Style.STROKE);
        dot.setColor(0xFF10302A);
        yTxt.setColor(0xFF5E7A74); yTxt.setTextSize(Ui.dp(c, 9)); yTxt.setTextAlign(Paint.Align.RIGHT);
        xTxt.setColor(0xFF2E7D46); xTxt.setTextSize(Ui.dp(c, 9)); xTxt.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<Double> v, String firstLabel, String lastLabel) {
        values = v; first = firstLabel; last = lastLabel; invalidate();
    }

    @Override protected void onDraw(Canvas cv) {
        if (values.isEmpty()) return;
        double mn = Double.MAX_VALUE, mx = -Double.MAX_VALUE;
        for (double d : values) { mn = Math.min(mn, d); mx = Math.max(mx, d); }
        double pad = mx == mn ? Math.abs(mx) * 0.015 + 0.01 : (mx - mn) * 0.1;
        double lo = mn - pad, hi = mx + pad;
        float left = Ui.dp(getContext(), 44), right = getWidth() - Ui.dp(getContext(), 12);
        float top = Ui.dp(getContext(), 10), bottom = getHeight() - Ui.dp(getContext(), 24);
        double[] ys = {hi, (hi + lo) / 2, lo};
        for (int i = 0; i < 3; i++) {
            float y = top + i * (bottom - top) / 2f;
            cv.drawLine(left, y, getWidth(), y, grid);
            cv.drawText(String.format(Locale.US, "%.2f", ys[i]), left - Ui.dp(getContext(), 6), y + Ui.dp(getContext(), 3), yTxt);
        }
        int n = values.size();
        float x0 = left + Ui.dp(getContext(), 8), x1 = right;
        float px = 0, py = 0;
        for (int i = 0; i < n; i++) {
            float x = n == 1 ? (x0 + x1) / 2 : x0 + i * (x1 - x0) / (n - 1);
            float y = (float) (bottom - (values.get(i) - lo) / (hi - lo) * (bottom - top));
            if (i > 0) cv.drawLine(px, py, x, y, line);
            px = x; py = y;
        }
        for (int i = 0; i < n; i++) {
            float x = n == 1 ? (x0 + x1) / 2 : x0 + i * (x1 - x0) / (n - 1);
            float y = (float) (bottom - (values.get(i) - lo) / (hi - lo) * (bottom - top));
            cv.drawCircle(x, y, Ui.dp(getContext(), 3.5f), dot);
        }
        float yl = getHeight() - Ui.dp(getContext(), 6);
        cv.drawText(first, n == 1 ? (x0 + x1) / 2 : x0, yl, xTxt);
        if (n > 1) cv.drawText(last, x1, yl, xTxt);
    }
}

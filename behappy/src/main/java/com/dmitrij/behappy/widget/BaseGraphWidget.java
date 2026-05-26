package com.dmitrij.behappy.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.widget.RemoteViews;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.data.StatsManager;
import com.dmitrij.behappy.data.repository.TrueNasRepository;
import com.dmitrij.behappy.model.UsageInfo;
import com.dmitrij.behappy.security.SecurePrefs;
import com.dmitrij.behappy.ui.MainActivity;

import java.util.List;
import java.util.Locale;

public abstract class BaseGraphWidget extends AppWidgetProvider {

    protected abstract String getTitle();
    protected abstract List<Float> getHistory();
    protected abstract int getGraphColor();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetRefreshWorker.enqueue(context);
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_graph);
        views.setTextViewText(R.id.widget_graph_title, getTitle());

        List<Float> history = getHistory();
        if (history != null && !history.isEmpty()) {
            float lastValue = history.get(history.size() - 1);
            views.setTextViewText(R.id.widget_graph_value, String.format(Locale.getDefault(), "%.1f%%", lastValue));
            views.setImageViewBitmap(R.id.widget_graph_image, drawGraph(history, getGraphColor()));
        } else {
            views.setTextViewText(R.id.widget_graph_value, "---");
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_graph_title, pendingIntent);

        SecurePrefs prefs = new SecurePrefs(context);
        if (!prefs.getHost().isEmpty()) {
            TrueNasRepository.getInstance().fetchUsage(context, prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(), new TrueNasRepository.UsageCallback() {
                @Override
                public void onSuccess(UsageInfo info) {
                    boolean isCpu = getTitle().toUpperCase().contains("CPU");
                    float val = (float) (isCpu ? info.getCpuPercent() : info.getRamPercent());
                    if (isCpu) StatsManager.getInstance().addCpuSample(val);
                    else StatsManager.getInstance().addRamSample(val);
                    
                    AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, buildViews(context, getTitle(), getHistory(), getGraphColor(), pendingIntent));
                }
                @Override public void onError(String m) {}
            });
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private RemoteViews buildViews(Context context, String title, List<Float> history, int color, PendingIntent pi) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_graph);
        views.setTextViewText(R.id.widget_graph_title, title);
        if (history != null && !history.isEmpty()) {
            float lastValue = history.get(history.size() - 1);
            views.setTextViewText(R.id.widget_graph_value, String.format(Locale.getDefault(), "%.1f%%", lastValue));
            views.setImageViewBitmap(R.id.widget_graph_image, drawGraph(history, color));
        }
        views.setOnClickPendingIntent(R.id.widget_graph_title, pi);
        return views;
    }

    private Bitmap drawGraph(List<Float> history, int color) {
        int width = 300;
        int height = 150;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        if (history.size() < 2) {
            if (history.size() == 1) {
                Paint p = new Paint();
                p.setColor(color);
                p.setStrokeWidth(5f);
                float y = height - (history.get(0) / 100f * height);
                canvas.drawLine(0, y, width, y, p);
            }
            return bitmap;
        }

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(color);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setShader(new LinearGradient(0, 0, 0, height, 
            adjustAlpha(color, 0.4f), Color.TRANSPARENT, Shader.TileMode.CLAMP));

        Path path = new Path();
        float xStep = (float) width / (history.size() - 1);
        
        for (int i = 0; i < history.size(); i++) {
            float x = i * xStep;
            float y = height - (history.get(i) / 100f * height);
            if (i == 0) path.moveTo(x, y);
            else {
                float prevX = (i - 1) * xStep;
                float prevY = height - (history.get(i - 1) / 100f * height);
                path.cubicTo((prevX + x) / 2, prevY, (prevX + x) / 2, y, x, y);
            }
        }

        Path fillPath = new Path(path);
        fillPath.lineTo(width, height);
        fillPath.lineTo(0, height);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(path, linePaint);
        
        return bitmap;
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}

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
    public void onUpdate(Context appContext, AppWidgetManager widgetManager, int[] widgetIdentifiersList) {
        WidgetRefreshWorker.enqueue(appContext);
        for (int singleWidgetId : widgetIdentifiersList) {
            refreshWidgetDisplay(appContext, widgetManager, singleWidgetId);
        }
    }

    private void refreshWidgetDisplay(Context appContext, AppWidgetManager widgetManager, int singleWidgetId) {
        RemoteViews remoteViewsObject = new RemoteViews(appContext.getPackageName(), R.layout.widget_graph);
        remoteViewsObject.setTextViewText(R.id.widget_graph_title, getTitle());

        List<Float> statsHistoryList = getHistory();
        if (statsHistoryList != null && !statsHistoryList.isEmpty()) {
            float mostRecentValue = statsHistoryList.get(statsHistoryList.size() - 1);
            remoteViewsObject.setTextViewText(R.id.widget_graph_value, String.format(Locale.getDefault(), "%.1f%%", mostRecentValue));
            remoteViewsObject.setImageViewBitmap(R.id.widget_graph_image, renderGraphBitmap(statsHistoryList, getGraphColor()));
        } else {
            remoteViewsObject.setTextViewText(R.id.widget_graph_value, "---");
        }

        Intent launcherActivityIntent = new Intent(appContext, MainActivity.class);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(appContext, 0, launcherActivityIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViewsObject.setOnClickPendingIntent(R.id.widget_graph_title, clickPendingIntent);

        SecurePrefs userPreferences = new SecurePrefs(appContext);
        if (!userPreferences.getHost().isEmpty()) {
            TrueNasRepository.getInstance().fetchUsage(appContext, userPreferences.getHost(), userPreferences.getApiKey(), userPreferences.isAllowSelfSigned(), new TrueNasRepository.UsageCallback() {
                @Override
                public void onSuccess(UsageInfo usageInformation) {
                    boolean isCpuTitle = getTitle().toUpperCase().contains("CPU");
                    float usageValue = (float) (isCpuTitle ? usageInformation.getCpuPercent() : usageInformation.getRamPercent());
                    if (isCpuTitle) StatsManager.getInstance().addCpuSample(usageValue);
                    else StatsManager.getInstance().addRamSample(usageValue);
                    
                    AppWidgetManager.getInstance(appContext).updateAppWidget(singleWidgetId, constructRemoteViews(appContext, getTitle(), getHistory(), getGraphColor(), clickPendingIntent));
                }
                @Override public void onError(String errorMessage) {}
            });
        }

        widgetManager.updateAppWidget(singleWidgetId, remoteViewsObject);
    }

    private RemoteViews constructRemoteViews(Context appContext, String widgetTitle, List<Float> statsHistoryList, int graphColor, PendingIntent clickActionPendingIntent) {
        RemoteViews remoteViewsObject = new RemoteViews(appContext.getPackageName(), R.layout.widget_graph);
        remoteViewsObject.setTextViewText(R.id.widget_graph_title, widgetTitle);
        if (statsHistoryList != null && !statsHistoryList.isEmpty()) {
            float mostRecentValue = statsHistoryList.get(statsHistoryList.size() - 1);
            remoteViewsObject.setTextViewText(R.id.widget_graph_value, String.format(Locale.getDefault(), "%.1f%%", mostRecentValue));
            remoteViewsObject.setImageViewBitmap(R.id.widget_graph_image, renderGraphBitmap(statsHistoryList, graphColor));
        }
        remoteViewsObject.setOnClickPendingIntent(R.id.widget_graph_title, clickActionPendingIntent);
        return remoteViewsObject;
    }

    private Bitmap renderGraphBitmap(List<Float> statsHistoryList, int graphColor) {
        int bitmapWidth = 300;
        int bitmapHeight = 150;
        Bitmap graphBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas graphCanvas = new Canvas(graphBitmap);
        
        if (statsHistoryList.size() < 2) {
            if (statsHistoryList.size() == 1) {
                Paint singlePointPaint = new Paint();
                singlePointPaint.setColor(graphColor);
                singlePointPaint.setStrokeWidth(5f);
                float yCoordinate = bitmapHeight - (statsHistoryList.get(0) / 100f * bitmapHeight);
                graphCanvas.drawLine(0, yCoordinate, bitmapWidth, yCoordinate, singlePointPaint);
            }
            return graphBitmap;
        }

        Paint graphLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphLinePaint.setColor(graphColor);
        graphLinePaint.setStyle(Paint.Style.STROKE);
        graphLinePaint.setStrokeWidth(6f);
        graphLinePaint.setStrokeCap(Paint.Cap.ROUND);

        Paint graphFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphFillPaint.setStyle(Paint.Style.FILL);
        graphFillPaint.setShader(new LinearGradient(0, 0, 0, bitmapHeight, 
            getAlphaAdjustedColor(graphColor, 0.4f), Color.TRANSPARENT, Shader.TileMode.CLAMP));

        Path graphPath = new Path();
        float xCoordinateStep = (float) bitmapWidth / (statsHistoryList.size() - 1);
        
        for (int index = 0; index < statsHistoryList.size(); index++) {
            float xCoordinate = index * xCoordinateStep;
            float yCoordinate = bitmapHeight - (statsHistoryList.get(index) / 100f * bitmapHeight);
            if (index == 0) graphPath.moveTo(xCoordinate, yCoordinate);
            else {
                float previousX = (index - 1) * xCoordinateStep;
                float previousY = bitmapHeight - (statsHistoryList.get(index - 1) / 100f * bitmapHeight);
                graphPath.cubicTo((previousX + xCoordinate) / 2, previousY, (previousX + xCoordinate) / 2, yCoordinate, xCoordinate, yCoordinate);
            }
        }

        Path graphFillPath = new Path(graphPath);
        graphFillPath.lineTo(bitmapWidth, bitmapHeight);
        graphFillPath.lineTo(0, bitmapHeight);
        graphFillPath.close();

        graphCanvas.drawPath(graphFillPath, graphFillPaint);
        graphCanvas.drawPath(graphPath, graphLinePaint);
        
        return graphBitmap;
    }

    private int getAlphaAdjustedColor(int baseColor, float alphaFactor) {
        int adjustedAlpha = Math.round(Color.alpha(baseColor) * alphaFactor);
        return Color.argb(adjustedAlpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
    }
}

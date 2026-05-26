package com.dmitrij.behappy.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.dmitrij.behappy.R;
import com.dmitrij.behappy.data.repository.TrueNasRepository;
import com.dmitrij.behappy.model.PoolInfo;
import com.dmitrij.behappy.model.SystemInfo;
import com.dmitrij.behappy.security.SecurePrefs;
import com.dmitrij.behappy.ui.MainActivity;

import java.util.List;

public class StatusWidget extends AppWidgetProvider {

    private static String lastUptime = "--";
    private static String lastVersion = "--";
    private static int lastPools = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetRefreshWorker.enqueue(context);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SecurePrefs prefs = new SecurePrefs(context);
        String host = prefs.getHost();
        String apiKey = prefs.getApiKey();

        if (!host.isEmpty() && !apiKey.isEmpty()) {
            TrueNasRepository.getInstance().fetchSystemInfo(context, host, apiKey, prefs.isAllowSelfSigned(), new TrueNasRepository.SystemInfoCallback() {
                @Override
                public void onSuccess(SystemInfo info) {
                    lastUptime = info.getUptime();
                    lastVersion = info.getVersion();
                    refresh(context, appWidgetManager, appWidgetId);
                }
                @Override public void onError(String message) {}
            });

            TrueNasRepository.getInstance().fetchPools(context, host, apiKey, prefs.isAllowSelfSigned(), new TrueNasRepository.PoolsCallback() {
                @Override
                public void onSuccess(List<PoolInfo> info) {
                    lastPools = (info != null ? info.size() : 0);
                    refresh(context, appWidgetManager, appWidgetId);
                }
                @Override public void onError(String message) {}
            });
        }

        refresh(context, appWidgetManager, appWidgetId);
    }

    private static void refresh(Context context, AppWidgetManager manager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_status);
        
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_title, pendingIntent);

        views.setTextViewText(R.id.widget_uptime, "Uptime: " + lastUptime);
        views.setTextViewText(R.id.widget_version, lastVersion);
        views.setTextViewText(R.id.widget_pools, "Pools: " + lastPools);

        manager.updateAppWidget(id, views);
    }
}

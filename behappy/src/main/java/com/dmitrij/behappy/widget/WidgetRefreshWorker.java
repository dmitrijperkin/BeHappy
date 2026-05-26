package com.dmitrij.behappy.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class WidgetRefreshWorker extends Worker {

    public WidgetRefreshWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void enqueue(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WidgetRefreshWorker.class)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build();
        
        WorkManager.getInstance(context).enqueueUniqueWork(
                "WidgetRefreshLoop",
                ExistingWorkPolicy.REPLACE,
                request
        );
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        
        checkAlerts(context);
        updateWidget(context, StatusWidget.class);
        updateWidget(context, CpuWidget.class);
        updateWidget(context, RamWidget.class);

        enqueue(context);
        return Result.success();
    }

    private void checkAlerts(Context context) {
        com.dmitrij.behappy.security.SecurePrefs prefs = new com.dmitrij.behappy.security.SecurePrefs(context);
        if (prefs.getHost().isEmpty()) return;

        com.dmitrij.behappy.data.repository.TrueNasRepository.getInstance().fetchAlerts(
            context, prefs.getHost(), prefs.getApiKey(), prefs.isAllowSelfSigned(),
            new com.dmitrij.behappy.data.repository.TrueNasRepository.AlertsCallback() {
                @Override
                public void onSuccess(java.util.List<com.dmitrij.behappy.model.AlertInfo> alerts) {
                    for (com.dmitrij.behappy.model.AlertInfo alert : alerts) {
                        if ("CRITICAL".equals(alert.getLevel()) || "WARNING".equals(alert.getLevel())) {
                            showNotification(context, alert.getLevel(), alert.getFormatted());
                        }
                    }
                }
                @Override
                public void onError(String message) {}
            }
        );
    }

    private void showNotification(Context context, String title, String text) {
        String channelId = "alerts";
        android.app.NotificationManager nm = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new android.app.NotificationChannel(channelId, "System Alerts", android.app.NotificationManager.IMPORTANCE_HIGH));
        }
        androidx.core.app.NotificationCompat.Builder b = new androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(com.dmitrij.behappy.R.drawable.info)
                .setContentTitle("TrueNAS: " + title)
                .setContentText(text)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH);
        nm.notify(text.hashCode(), b.build());
    }

    private void updateWidget(Context context, Class<?> cls) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, cls));
        if (ids.length > 0) {
            Intent intent = new Intent(context, cls);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);
        }
    }
}

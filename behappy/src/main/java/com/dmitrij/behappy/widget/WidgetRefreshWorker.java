package com.dmitrij.behappy.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class WidgetRefreshWorker extends Worker {

    public WidgetRefreshWorker(@NonNull Context appContext, @NonNull WorkerParameters refreshWorkerParameters) {
        super(appContext, refreshWorkerParameters);
    }

    public static void enqueue(Context appContext) {
        OneTimeWorkRequest refreshWorkRequest = new OneTimeWorkRequest.Builder(WidgetRefreshWorker.class)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build();
        
        WorkManager.getInstance(appContext).enqueueUniqueWork(
                "WidgetRefreshLoop",
                ExistingWorkPolicy.REPLACE,
                refreshWorkRequest
        );
    }

    @NonNull
    @Override
    public Result doWork() {
        Context appContext = getApplicationContext();
        
        processSystemAlerts(appContext);
        refreshWidgetComponent(appContext, StatusWidget.class);
        refreshWidgetComponent(appContext, CpuWidget.class);
        refreshWidgetComponent(appContext, RamWidget.class);

        enqueue(appContext);
        return Result.success();
    }

    private void processSystemAlerts(Context appContext) {
        com.dmitrij.behappy.security.SecurePrefs userPreferences = new com.dmitrij.behappy.security.SecurePrefs(appContext);
        if (userPreferences.getHost().isEmpty()) return;

        com.dmitrij.behappy.data.repository.TrueNasRepository.getInstance().fetchAlerts(
            appContext, userPreferences.getHost(), userPreferences.getApiKey(), userPreferences.isAllowSelfSigned(),
            new com.dmitrij.behappy.data.repository.TrueNasRepository.AlertsCallback() {
                @Override
                public void onSuccess(java.util.List<com.dmitrij.behappy.model.AlertInfo> alertsList) {
                    for (com.dmitrij.behappy.model.AlertInfo alertInfo : alertsList) {
                        if ("CRITICAL".equals(alertInfo.getLevel()) || "WARNING".equals(alertInfo.getLevel())) {
                            triggerNotification(appContext, alertInfo.getLevel(), alertInfo.getFormatted());
                        }
                    }
                }
                @Override
                public void onError(String errorMessage) {}
            }
        );
    }

    private void triggerNotification(Context appContext, String alertTitle, String alertMessage) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        String alertChannelId = "alerts";
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new android.app.NotificationChannel(alertChannelId, "System Alerts", android.app.NotificationManager.IMPORTANCE_HIGH));
        }
        androidx.core.app.NotificationCompat.Builder notificationBuilder = new androidx.core.app.NotificationCompat.Builder(appContext, alertChannelId)
                .setSmallIcon(com.dmitrij.behappy.R.drawable.info)
                .setContentTitle("TrueNAS: " + alertTitle)
                .setContentText(alertMessage)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH);
        notificationManager.notify(alertMessage.hashCode(), notificationBuilder.build());
    }

    private void refreshWidgetComponent(Context appContext, Class<?> widgetClass) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(appContext);
        int[] widgetIdentifiers = widgetManager.getAppWidgetIds(new ComponentName(appContext, widgetClass));
        if (widgetIdentifiers.length > 0) {
            Intent updateIntent = new Intent(appContext, widgetClass);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIdentifiers);
            appContext.sendBroadcast(updateIntent);
        }
    }
}

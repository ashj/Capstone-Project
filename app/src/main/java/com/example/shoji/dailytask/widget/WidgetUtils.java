package com.example.shoji.dailytask.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import com.example.shoji.dailytask.intentservice.TaskPendingIntentUtils;

import timber.log.Timber;

public class WidgetUtils {
    // [START] Update app widget
    public static void startTaskWidgetUpdate(Context context) {
        Timber.d("startTaskWidgetUpdate");
        PendingIntent pendingIntent = TaskPendingIntentUtils.getUpdateAppWidgetPendingIntent(context);

        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Timber.e(e.getMessage());
        }
    }

    public static void updateTaskWidget(Context context) {
        Timber.d("updateTaskWidget");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TaskWidgetProvider.class));

        TaskWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetIds);

    }
    // [END] Update app widget
}

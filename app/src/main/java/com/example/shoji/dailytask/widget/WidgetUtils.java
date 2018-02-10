package com.example.shoji.dailytask.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.example.shoji.dailytask.intentservice.TaskIntentService;
import com.example.shoji.dailytask.intentservice.TaskIntentServiceTasks;

import timber.log.Timber;

public class WidgetUtils {
    // [START] Update app widget
    public static void startTaskWidgetUpdate(Context context) {
        Timber.d("startTaskWidgetUpdate");
        Intent intent = new Intent(context, TaskIntentService.class);
        intent.setAction(TaskIntentServiceTasks.ACTION_REFRESH_TASK_WIDGET);
        context.startService(intent);
    }

    public static void updateTaskWidget(Context context) {
        Timber.d("updateTaskWidget");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TaskWidgetProvider.class));

        TaskWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetIds);

    }
    // [END] Update app widget
}

package com.example.shoji.dailytask.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.background.LoaderTaskGetTasks;
import com.example.shoji.dailytask.notification.TaskNotification;
import com.example.shoji.dailytask.provider.TaskContract;

/**
 * Implementation of App Widget functionality.
 */
public class TaskWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, long taskId, String taskTitle) {

        CharSequence widgetText = "Today's task: "+taskTitle+"//(id: "+taskId+")";
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.task_widget_provider);

        // [START] Pending intent to open task
        PendingIntent showTaskById = TaskNotification.getPendingIntentShowTaskById(context, taskId);
        views.setOnClickPendingIntent(R.id.title_text_view, showTaskById);
        // [END] Pending intent to open task
        // [START] Pending intent to mark task as done
        PendingIntent markTaskAsDonePendingIntent = TaskNotification.getMarkTaskAsDonePendingIntent(context, taskId);
        views.setOnClickPendingIntent(R.id.mark_button, markTaskAsDonePendingIntent);
        // [END] Pending intent to mark task as done

        views.setTextViewText(R.id.title_text_view, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // [START] get today's task
    private Cursor getTaskCursor(Context context) {
        Bundle bundle = new Bundle();
        bundle.putString(LoaderTaskGetTasks.EXTRA_WHERE, LoaderTaskGetTasks.NOT_CONCLUDED_TASKS_WHERE);
        bundle.putString(LoaderTaskGetTasks.EXTRA_SORT_BY, LoaderTaskGetTasks.NOT_CONCLUDED_TASKS_SORT_BY);
        Cursor cursor = LoaderTaskGetTasks.queryTasks(context, bundle);

        return cursor;

    }
    // [END] get today's task

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Cursor cursor = getTaskCursor(context);

        long taskId = TaskContract.INVALID_ID;
        String taskTitle = null;
        if(cursor != null && cursor.getCount() >= 1) {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(TaskContract._ID);
            taskId = cursor.getLong(index);
            index = cursor.getColumnIndex(TaskContract.COLUMN_TITLE);
            taskTitle = cursor.getString(index);
            cursor.close();
        }

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, taskId, taskTitle);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


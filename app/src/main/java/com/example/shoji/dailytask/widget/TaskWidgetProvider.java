package com.example.shoji.dailytask.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.background.LoaderTaskGetTasks;
import com.example.shoji.dailytask.intentservice.TaskPendingIntentUtils;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.utils.TimeUtils;

/**
 * Implementation of App Widget functionality.
 */
public class TaskWidgetProvider extends AppWidgetProvider {

    private static void updateAppWidgetAux(Context context, AppWidgetManager appWidgetManager,
                                            int appWidgetId, long taskId,
                                            String taskTitle, String taskDescription) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.task_widget_provider);
        views.setTextViewText(R.id.widget_text_view, context.getString(R.string.widget_todays_task));
        views.setViewVisibility(R.id.action_view, View.VISIBLE);

        CharSequence widgetText = null;
        CharSequence widgetTextDescription = null;
        if(taskId != TaskContract.INVALID_ID) {
            // [START] last task completed timestamp
            if(TimeUtils.isTaskUnderCooldown(context)) {
                views.setTextViewText(R.id.widget_text_view, context.getString(R.string.widget_tomorrows_task));
                views.setViewVisibility(R.id.action_view, View.INVISIBLE);
            }
            // [END] last task completed timestamp
            else {
                // [START] Pending intent to mark task as done
                PendingIntent markTaskAsDonePendingIntent = TaskPendingIntentUtils.getMarkTaskAsDonePendingIntent(context, taskId);
                views.setOnClickPendingIntent(R.id.action_view, markTaskAsDonePendingIntent);
                views.setTextViewText(R.id.action_view, context.getString(R.string.button_task_conclude));
                // [END] Pending intent to mark task as done
            }
            // [START] Pending intent to open task
            PendingIntent showTaskById = TaskPendingIntentUtils.getPendingIntentShowTaskById(context, taskId);
            widgetText = taskTitle;
            widgetTextDescription = taskDescription;
            views.setOnClickPendingIntent(R.id.relative_layout, showTaskById);
            // [END] Pending intent to open task
        }

        else {
            // [START] Pending intent to add a new task
            PendingIntent addTaskPendingIntent = TaskPendingIntentUtils.getPendingIntentAddTasks(context);
            views.setOnClickPendingIntent(R.id.action_view, addTaskPendingIntent);
            views.setTextViewText(R.id.action_view, context.getString(R.string.button_task_add));
            // [END] Pending intent to add a new task

            // [START] Pending intent to show tasks
            widgetText = context.getString(R.string.widget_empty_task_list);
            PendingIntent showTasks = TaskPendingIntentUtils.getPendingIntentShowTasks(context);

            views.setOnClickPendingIntent(R.id.relative_layout, showTasks);
            // [END] Pending intent to show tasks
        }

        views.setTextViewText(R.id.task_title, widgetText);
        views.setTextViewText(R.id.task_description, widgetTextDescription);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // [START] get today's task
    private static Cursor getTaskCursor(Context context) {
        Bundle bundle = new Bundle();
        bundle.putString(LoaderTaskGetTasks.EXTRA_WHERE, LoaderTaskGetTasks.NOT_CONCLUDED_TASKS_WHERE);
        bundle.putString(LoaderTaskGetTasks.EXTRA_SORT_BY, LoaderTaskGetTasks.NOT_CONCLUDED_TASKS_SORT_BY);
        Cursor cursor = LoaderTaskGetTasks.queryTasks(context, bundle);

        return cursor;

    }
    // [END] get today's task

    // [START] Update app widget
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Cursor cursor = getTaskCursor(context);

        long taskId = TaskContract.INVALID_ID;
        String taskTitle = null;
        String taskDescription = null;
        if(cursor != null && cursor.getCount() >= 1) {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(TaskContract._ID);
            taskId = cursor.getLong(index);
            index = cursor.getColumnIndex(TaskContract.COLUMN_TITLE);
            taskTitle = cursor.getString(index);

            index = cursor.getColumnIndex(TaskContract.COLUMN_DESCRIPTION);
            taskDescription = cursor.getString(index);
            cursor.close();
        }

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidgetAux(context, appWidgetManager, appWidgetId, taskId, taskTitle, taskDescription);
        }

    }
    // [END] Update app widget

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidget(context, appWidgetManager, appWidgetIds);
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


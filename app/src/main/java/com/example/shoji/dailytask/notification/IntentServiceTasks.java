package com.example.shoji.dailytask.notification;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.example.shoji.dailytask.background.LoaderTaskSetConcludedById;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.widget.TaskWidgetProvider;

import timber.log.Timber;

public class IntentServiceTasks {
    public static final String ACTION_MARK_TASK_AS_DONE = "action-mark-task-as-done";
    public static final String ACTION_DISMISS_NOTIFICATION = "action-dismiss-notification";

    public static final String ACTION_TASK_REMINDER = "action-task-reminder";

    public static final String ACTION_REFRESH_TASK_WIDGET = "action-refresh-task-widget";

    public static void executeTask(Context context, Intent intent) {
        Timber.d("executeTask");
        if(intent == null)
            return;

        String action = intent.getAction();
        Timber.d("executeTask, action: %s", action);

        if(TextUtils.equals(action, ACTION_MARK_TASK_AS_DONE)) {
            markTaskAsDone(context, intent);
        }

        else if(TextUtils.equals(action, ACTION_DISMISS_NOTIFICATION)) {
            TaskNotification.clearAllNotifications(context);
        }

        else if(TextUtils.equals(action, ACTION_TASK_REMINDER)) {
            TaskNotification.showNotificationTaskReminder(context);
        }

        else if(TextUtils.equals(action, ACTION_REFRESH_TASK_WIDGET)) {
            updateTaskWidget(context);
        }

    }

    // [START] mark test as done
    private static void markTaskAsDone(Context context, Intent intent) {
        long id = intent.getLongExtra(LoaderTaskSetConcludedById.EXTRA_TASK_ID,
                TaskContract.INVALID_ID);

        long concludedState = intent.getLongExtra(LoaderTaskSetConcludedById.EXTRA_TASK_CONCLUDED_STATE,
                -1);

        if(id != TaskContract.INVALID_ID && concludedState != -1) {
            int rows = LoaderTaskSetConcludedById.update(context, id, concludedState);
            //[START] update the widget
            if(rows > 0)
                IntentServiceTasks.startTaskWidgetUpdate(context);
            //[END] update the widget
        }
    }
    // [END] mark test as done

    // [START] Update app widget
    public static void startTaskWidgetUpdate(Context context) {
        Timber.d("startTaskWidgetUpdate");
        Intent intent = new Intent(context, TaskIntentService.class);
        intent.setAction(ACTION_REFRESH_TASK_WIDGET);
        context.startService(intent);
    }

    private static void updateTaskWidget(Context context) {
        Timber.d("updateTaskWidget");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TaskWidgetProvider.class));

        TaskWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetIds);

    }
    // [END] Update app widget
}

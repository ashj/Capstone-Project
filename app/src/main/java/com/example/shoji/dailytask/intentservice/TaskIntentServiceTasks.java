package com.example.shoji.dailytask.intentservice;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.example.shoji.dailytask.BuildConfig;
import com.example.shoji.dailytask.background.LoaderTaskSetConcludedById;
import com.example.shoji.dailytask.notification.TaskNotificationUtils;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.widget.WidgetUtils;

import timber.log.Timber;

public class TaskIntentServiceTasks {
    public static final String ACTION_MARK_TASK_AS_DONE = "action-mark-task-as-done";
    public static final int ACTION_MARK_TASK_AS_DONE_PENDING_INTENT_ID = 6001;

    public static final String ACTION_DISMISS_NOTIFICATIONS = "action-dismiss-notification";
    public static final int ACTION_DISMISS_NOTIFICATIONS_PENDING_INTENT_ID = 6002;

    public static final String ACTION_TASK_REMINDER = BuildConfig.APPLICATION_ID + ".TASK_REMINDER";
    public static final int ACTION_TASK_REMINDER_PENDING_INTENT_ID = 6003;

    public static final String ACTION_REFRESH_TASK_WIDGET = "action-refresh-task-widget";
    public static final int ACTION_REFRESH_WIDGET_PENDING_INTENT_ID = 6004;

    public static final int ACTION_SHOW_TASK_BY_ID_PENDING_INTENT_ID = 60045;
    public static final int ACTION_SHOW_TASKS_PENDING_INTENT_ID = 6006;
    public static final int ACTION_ADD_TASK_PENDING_INTENT_ID = 6007;

    public static void executeTask(Context context, Intent intent) {
        Timber.d("executeTask");
        if(intent == null)
            return;

        String action = intent.getAction();
        Timber.d("executeTask, action: %s", action);

        if(TextUtils.equals(action, ACTION_MARK_TASK_AS_DONE)) {
            markTaskAsDone(context, intent);
        }

        else if(TextUtils.equals(action, ACTION_DISMISS_NOTIFICATIONS)) {
            TaskNotificationUtils.clearAllNotifications(context);
        }

        else if(TextUtils.equals(action, ACTION_TASK_REMINDER)) {
            TaskNotificationUtils.showNotificationTaskReminder(context);
        }

        else if(TextUtils.equals(action, ACTION_REFRESH_TASK_WIDGET)) {
            WidgetUtils.updateTaskWidget(context);
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
                WidgetUtils.startTaskWidgetUpdate(context);
            //[END] update the widget
        }
    }
    // [END] mark test as done


}

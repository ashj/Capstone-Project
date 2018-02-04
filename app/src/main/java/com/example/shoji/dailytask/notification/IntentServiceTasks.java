package com.example.shoji.dailytask.notification;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import timber.log.Timber;

public class IntentServiceTasks {
    public static final String ACTION_MARK_TASK_AS_DONE = "action-mark-task-as-done";
    public static final String ACTION_DISMISS_NOTIFICATION = "action-dismiss-notification";

    public static void executeTask(Context context, Intent intent) {
        Timber.d("executeTask");
        if(intent == null)
            return;

        String action = intent.getAction();
        Timber.d("executeTask, action: %s", action);

        if(TextUtils.equals(action, ACTION_MARK_TASK_AS_DONE)) {
            // do something

        }

        else if(TextUtils.equals(action, ACTION_DISMISS_NOTIFICATION)) {
            TaskNotification.clearAllNotifications(context);
        }

    }
}

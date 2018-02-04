package com.example.shoji.dailytask.notification;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.text.TextUtils;

import com.example.shoji.dailytask.background.LoaderIds;
import com.example.shoji.dailytask.background.LoaderTaskSetConcludedById;
import com.example.shoji.dailytask.background.LoaderUtils;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

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
            markTaskAsDone(context, intent);
        }

        else if(TextUtils.equals(action, ACTION_DISMISS_NOTIFICATION)) {
            TaskNotification.clearAllNotifications(context);
        }

    }

    // [START] mark test as done
    private static void markTaskAsDone(Context context, Intent intent) {
        long id = intent.getLongExtra(LoaderTaskSetConcludedById.EXTRA_TASK_ID,
                TaskContract.INVALID_ID);

        long concludedState = intent.getLongExtra(LoaderTaskSetConcludedById.EXTRA_TASK_CONCLUDED_STATE,
                -1);

        if(id != TaskContract.INVALID_ID && concludedState != -1)
            LoaderTaskSetConcludedById.update(context, id, concludedState);
    }
    // [END] mark test as done
}

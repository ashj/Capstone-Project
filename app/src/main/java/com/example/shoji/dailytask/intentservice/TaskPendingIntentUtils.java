package com.example.shoji.dailytask.intentservice;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.shoji.dailytask.background.LoaderTaskSetConcludedById;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.ui.MainActivity;
import com.example.shoji.dailytask.ui.TaskDetailActivity;
import com.example.shoji.dailytask.ui.TaskEditorActivity;

public class TaskPendingIntentUtils {
    private static final int TASK_PENDING_INTENT_ID = 5000;

    public static PendingIntent getPendingIntentShowTaskById(Context context, long taskId) {

        Intent startActivityIntent = new Intent(context, TaskDetailActivity.class);
        startActivityIntent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);

        return PendingIntent.getActivity(
                context,
                TaskIntentServiceTasks.ACTION_SHOW_TASK_BY_ID_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getPendingIntentShowTasks(Context context) {

        Intent startActivityIntent = new Intent(context, MainActivity.class);

        return PendingIntent.getActivity(
                context,
                TaskIntentServiceTasks.ACTION_SHOW_TASKS_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getPendingIntentAddTasks(Context context) {

        Intent startActivityIntent = new Intent(context, TaskEditorActivity.class);

        return PendingIntent.getActivity(
                context,
                TaskIntentServiceTasks.ACTION_ADD_TASK_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getMarkTaskAsDonePendingIntent(Context context, long taskId) {
        Intent markTaskAsDoneIntent = new Intent(context, TaskIntentService.class);
        markTaskAsDoneIntent.setAction(TaskIntentServiceTasks.ACTION_MARK_TASK_AS_DONE);
        markTaskAsDoneIntent.putExtra(LoaderTaskSetConcludedById.EXTRA_TASK_ID, taskId);
        markTaskAsDoneIntent.putExtra(LoaderTaskSetConcludedById.EXTRA_TASK_CONCLUDED_STATE, TaskContract.CONCLUDED);

        PendingIntent markTaskAsDonePendingIntent = PendingIntent.getService(
                context,
                TaskIntentServiceTasks.ACTION_MARK_TASK_AS_DONE_PENDING_INTENT_ID,
                markTaskAsDoneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return markTaskAsDonePendingIntent;
    }

    public static PendingIntent getUpdateAppWidgetPendingIntent(Context context) {
        Intent intent = new Intent(context, TaskIntentService.class);
        intent.setAction(TaskIntentServiceTasks.ACTION_REFRESH_TASK_WIDGET);

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                TaskIntentServiceTasks.ACTION_REFRESH_WIDGET_PENDING_INTENT_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

//    public static PendingIntent getAlarmPendingIntent(Context context) {
//        // [START] Show the notification
//        Intent intent = new Intent();
//        intent.setAction(TaskIntentServiceTasks.ACTION_TASK_REMINDER);
//        // [END] Show the notification
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                context,
//                TaskIntentServiceTasks.ACTION_TASK_REMINDER_PENDING_INTENT_ID,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        return pendingIntent;
//    }
}

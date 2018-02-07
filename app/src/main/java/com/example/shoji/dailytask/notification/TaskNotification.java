package com.example.shoji.dailytask.notification;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.background.LoaderTaskGetTasks;
import com.example.shoji.dailytask.background.LoaderTaskSetConcludedById;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.ui.MainActivity;
import com.example.shoji.dailytask.ui.TaskDetailActivity;
import com.example.shoji.dailytask.ui.TaskEditorActivity;

import timber.log.Timber;

public class TaskNotification {
    private static final int TASK_NOTIFICATION_ID = 1000;

    private static final int TASK_PENDING_INTENT_ID = 5000;

    private static final String TASK_DAILY_NOTIFICATION_CHANNEL_ID = "task_daily_notification_channel";

    public static final int ACTION_DISMISS_NOTIFICATION_PENDING_INTENT_ID = 6000;
    public static final int ACTION_MARK_TASK_AS_DONE_PENDING_INTENT_ID = 6001;
    public static final int ACTION_TASK_REMINDER_PENDING_INTENT_ID = 6002;
    public static final int ACTION_DISMISS_NOTIFICATIONS_PENDING_INTENT_ID = 6003;

    private static void notifyTodaysTask(Context context, long taskId, String taskTitle) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    TASK_DAILY_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        int resIdSmallIcon = R.drawable.ic_task_notification;
        Bitmap largeIcon = largeIcon(context, resIdSmallIcon);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,TASK_DAILY_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(resIdSmallIcon)
                .setLargeIcon(largeIcon)
                .setContentTitle(context.getString(R.string.notification_todays_task))
                .setContentText(taskTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(taskTitle))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(getPendingIntentShowTaskById(context, taskId))
                .addAction(markTaskAsDoneAction(context, taskId))
                .addAction(clearAllNotificationsAction(context))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        notificationManager.notify(TASK_NOTIFICATION_ID, notificationBuilder.build());
    }


    public static PendingIntent getPendingIntentShowTaskById(Context context, long taskId) {

        Intent startActivityIntent = new Intent(context, TaskDetailActivity.class);
        startActivityIntent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);

        return PendingIntent.getActivity(
                context,
                TASK_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getPendingIntentShowTasks(Context context) {

        Intent startActivityIntent = new Intent(context, MainActivity.class);

        return PendingIntent.getActivity(
                context,
                TASK_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getPendingIntentAddTasks(Context context) {

        Intent startActivityIntent = new Intent(context, TaskEditorActivity.class);

        return PendingIntent.getActivity(
                context,
                TASK_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context, int resIdIcon) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, resIdIcon);
        return largeIcon;
    }

    public static PendingIntent getDismissNotificationsPendingIntent(Context context) {
        String action = IntentServiceTasks.ACTION_DISMISS_NOTIFICATION;
        int requestCode = ACTION_DISMISS_NOTIFICATIONS_PENDING_INTENT_ID;

        Intent intent = new Intent(context, TaskIntentService.class);

        intent.setAction(action);

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }
    private static NotificationCompat.Action clearAllNotificationsAction(Context context) {
        PendingIntent pendingIntent = getDismissNotificationsPendingIntent(context);

        int resIdIcon = android.R.drawable.ic_menu_close_clear_cancel;
        String title = context.getString(R.string.notification_dismiss_action);

        NotificationCompat.Action markTaskAsDoneAction =
                new NotificationCompat.Action(resIdIcon,
                        title,
                        pendingIntent);

        return markTaskAsDoneAction;
    }


    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static PendingIntent getMarkTaskAsDonePendingIntent(Context context, long taskId) {
        Intent markTaskAsDoneIntent = new Intent(context, TaskIntentService.class);
        markTaskAsDoneIntent.setAction(IntentServiceTasks.ACTION_MARK_TASK_AS_DONE);
        markTaskAsDoneIntent.putExtra(LoaderTaskSetConcludedById.EXTRA_TASK_ID, taskId);
        markTaskAsDoneIntent.putExtra(LoaderTaskSetConcludedById.EXTRA_TASK_CONCLUDED_STATE, TaskContract.CONCLUDED);

        PendingIntent markTaskAsDonePendingIntent = PendingIntent.getService(
                context,
                ACTION_MARK_TASK_AS_DONE_PENDING_INTENT_ID,
                markTaskAsDoneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return markTaskAsDonePendingIntent;
    }

    private static NotificationCompat.Action markTaskAsDoneAction(Context context, long taskId) {
        PendingIntent markTaskAsDonePendingIntent = getMarkTaskAsDonePendingIntent(context, taskId);

        int resIdIcon = android.R.drawable.checkbox_on_background;
        String title = context.getString(R.string.notification_mark_task_as_done_action);

        NotificationCompat.Action markTaskAsDoneAction =
                new NotificationCompat.Action(resIdIcon,
                        title,
                        markTaskAsDonePendingIntent);

        return markTaskAsDoneAction;
    }

    // [START] Notify about today's task
    public static void showNotificationTaskReminder(Context context) {
        // [START] Check shared preference for notification
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_daily_notification_key);
        boolean defValue = context.getResources().getBoolean(R.bool.pref_daily_notification_default_value);
        boolean showNotification = sharedPreferences.getBoolean(key, defValue);
        if(!showNotification) {
            Timber.d("Notifications are disabled");
            return;
        }
        // [END] Check shared preference for notification

        // [START] Query today's task
        Bundle bundle = new Bundle();
        bundle.putString(LoaderTaskGetTasks.EXTRA_WHERE, LoaderTaskGetTasks.NOT_CONCLUDED_TASKS_WHERE);
        bundle.putString(LoaderTaskGetTasks.EXTRA_SORT_BY, LoaderTaskGetTasks.NOT_CONCLUDED_TASKS_SORT_BY);
        Cursor cursor = LoaderTaskGetTasks.queryTasks(context, bundle);

        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToPosition(0);
            int index = cursor.getColumnIndex(TaskContract.COLUMN_TITLE);
            String title = cursor.getString(index);

            index = cursor.getColumnIndex(TaskContract._ID);
            long id = cursor.getLong(index);

            TaskNotification.notifyTodaysTask(context, id, title);
        }
        // [END] Query today's task
    }
    // [END] Notify about today's task
}

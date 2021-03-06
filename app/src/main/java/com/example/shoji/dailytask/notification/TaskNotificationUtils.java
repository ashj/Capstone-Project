package com.example.shoji.dailytask.notification;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.example.shoji.dailytask.intentservice.TaskIntentService;
import com.example.shoji.dailytask.intentservice.TaskIntentServiceTasks;
import com.example.shoji.dailytask.intentservice.TaskPendingIntentUtils;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.utils.TimeUtils;

import timber.log.Timber;

public class TaskNotificationUtils {
    private static final int TASK_NOTIFICATION_ID = 1000;



    private static final String TASK_DAILY_NOTIFICATION_CHANNEL_ID = "task_daily_notification_channel";



    // [START] Today's task notification
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

        // [START] last task completed timestamp
        if(TimeUtils.isTaskUnderCooldown(context)) {
            Timber.d("cooldown - No need to show notification");
            return;
        }
        // [END] last task completed timestamp

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

            cursor.close();
            TaskNotificationUtils.notifyTodaysTask(context, id, title);
        }
        // [END] Query today's task
    }
    // [END] Notify about today's task

    @SuppressLint("ObsoleteSdkInt")
    private static void notifyTodaysTask(Context context, long taskId, String taskTitle) {
        Timber.d("notifyTodaysTask");
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    TASK_DAILY_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        int resIdSmallIcon = R.drawable.ic_task_done_all;
        Bitmap largeIcon = largeIcon(context, R.drawable.ic_done_all_black_48dp);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,TASK_DAILY_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, android.R.color.white))
                .setSmallIcon(resIdSmallIcon)
                .setLargeIcon(largeIcon)
                .setContentTitle(context.getString(R.string.notification_todays_task))
                .setContentText(taskTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(taskTitle))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(TaskPendingIntentUtils.getPendingIntentShowTaskById(context, taskId))
                .addAction(markTaskAsDoneAction(context, taskId))
                .addAction(clearAllNotificationsAction(context))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        notificationManager.notify(TASK_NOTIFICATION_ID, notificationBuilder.build());

        // [START] schedule next notification
        TaskReminderUtilities.rescheduleTaskNotificationReminder(context);
        // [END] schedule next notification
    }

    private static Bitmap largeIcon(Context context, int resIdIcon) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, resIdIcon);
        return largeIcon;
    }

    // [START] notification actions
    private static NotificationCompat.Action clearAllNotificationsAction(Context context) {
        PendingIntent pendingIntent = getDismissNotificationsPendingIntent(context);

        int resIdIcon = R.drawable.ic_task_close;
        String title = context.getString(R.string.notification_dismiss_action);

        NotificationCompat.Action markTaskAsDoneAction =
                new NotificationCompat.Action(resIdIcon,
                        title,
                        pendingIntent);

        return markTaskAsDoneAction;
    }

    private static NotificationCompat.Action markTaskAsDoneAction(Context context, long taskId) {
        PendingIntent markTaskAsDonePendingIntent = TaskPendingIntentUtils.getMarkTaskAsDonePendingIntent(context, taskId);

        int resIdIcon = R.drawable.ic_task_check;
        String title = context.getString(R.string.notification_mark_task_as_done_action);

        NotificationCompat.Action markTaskAsDoneAction =
                new NotificationCompat.Action(resIdIcon,
                        title,
                        markTaskAsDonePendingIntent);

        return markTaskAsDoneAction;
    }
    // [END] notification actions
    // [END] Today's task notification





    // [START] dismiss notification
    public static PendingIntent getDismissNotificationsPendingIntent(Context context) {
        String action = TaskIntentServiceTasks.ACTION_DISMISS_NOTIFICATIONS;
        int requestCode = TaskIntentServiceTasks.ACTION_DISMISS_NOTIFICATIONS_PENDING_INTENT_ID;

        Intent intent = new Intent(context, TaskIntentService.class);

        intent.setAction(action);

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
    // [END] dismiss notification



}

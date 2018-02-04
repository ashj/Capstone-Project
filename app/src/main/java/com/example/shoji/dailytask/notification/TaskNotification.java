package com.example.shoji.dailytask.notification;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.ui.MainActivity;
import com.example.shoji.dailytask.ui.TaskDetailActivity;

public class TaskNotification {
    private static final int TASK_NOTIFICATION_ID = 1000;

    private static final int TASK_PENDING_INTENT_ID = 5000;

    private static final String TASK_DAILY_NOTIFICATION_CHANNEL_ID = "task_daily_notification_channel";

    public static final int ACTION_DISMISS_NOTIFICATION_PENDING_INTENT_ID = 6000;
    public static final int ACTION_MARK_TASK_AS_DONE_PENDING_INTENT_ID = 6001;

    public static void notifyTodaysTask(Context context, long taskId, String taskTitle) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    TASK_DAILY_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        int resIdSmallIcon = R.drawable.ic_launcher_background; //TODO: GET IMAGES
        Bitmap largeIcon = largeIcon(context, R.drawable.ic_launcher_background); //TODO: GET IMAGES

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,TASK_DAILY_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(resIdSmallIcon)
                .setLargeIcon(largeIcon)
                .setContentTitle(context.getString(R.string.notification_todays_task))
                .setContentText(taskTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(taskTitle))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context, taskId))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        notificationManager.notify(TASK_NOTIFICATION_ID, notificationBuilder.build());
    }


    private static PendingIntent contentIntent(Context context, long taskId) {

        Intent startActivityIntent = new Intent(context, TaskDetailActivity.class);
        startActivityIntent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);

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


    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}

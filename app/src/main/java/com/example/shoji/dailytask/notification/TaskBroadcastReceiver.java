package com.example.shoji.dailytask.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

public class TaskBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive");
        if(intent == null) return;

        String action = intent.getAction();
        if(action == null || action.length() == 0) return;
        Timber.d("onReceive - action %s", action);


//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        // [START] check notification setting
//        String keyNotification = context.getString(R.string.pref_daily_notification_key);
//        boolean enabledNotification = sharedPreferences.getBoolean(keyNotification,
//                context.getResources().getBoolean(R.bool.pref_daily_notification_default_value));
//        // [END] check notification setting
//        // [START] check location setting
//        boolean enabledLocation = sharedPreferences.getBoolean(context.getString(R.string.pref_location_service_key),
//                context.getResources().getBoolean(R.bool.pref_location_service_default_value));
//        // [END] check location setting



        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Timber.d("onReceive - Setup Task notification");
            TaskReminderUtilities.setupTaskReminderNotification(context);
        }

//        else if(TextUtils.equals(action, TaskIntentServiceTasks.ACTION_TASK_REMINDER)) {
//            Timber.d("Got action: %s", action);
//
//            if(enabledNotification && !enabledLocation) {
//                Timber.d("Case #1");
//                TaskNotificationUtils.showNotificationTaskReminder(context);
//            }
//            else {
//                Timber.d("Case #2");
//            }
//        }
    }

//    public static void enable(Context context) {
//        ComponentName receiver = new ComponentName(context, TaskBroadcastReceiver.class);
//        PackageManager pm = context.getPackageManager();
//
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP);
//    }
//
//    public static void disable(Context context) {
//        ComponentName receiver = new ComponentName(context, TaskBroadcastReceiver.class);
//        PackageManager pm = context.getPackageManager();
//
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP);
//    }
}

package com.example.shoji.dailytask.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.example.shoji.dailytask.R;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class TaskReminderUtilities {

    private static final int REMINDER_INTERVAL_MINUTES = 1;
    private static final int REMINDER_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));

    private static final int REMINDER_INTERVAL_DAILY = (int) (TimeUnit.HOURS.toSeconds(24));
    private static final int SYNC_FLEXTIME_SECONDS = 30;

    private static final String REMINDER_JOB_TAG = "task_notification_reminder_tag";

    private static boolean sInitialized;

    synchronized public static void scheduleTaskNotificationReminder(@NonNull final Context context) {
        Timber.d("scheduleTaskNotificationReminder -- init'd?: %b", sInitialized);
        if (sInitialized) return;
        Timber.d("scheduleTaskNotificationReminder START");

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        int startInterval = REMINDER_INTERVAL_SECONDS;
        int flexTime = SYNC_FLEXTIME_SECONDS;

        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(TaskReminderFirebaseJobService.class)
                .setTag(REMINDER_JOB_TAG)
                //.setConstraints(Constraint.DEVICE_CHARGING)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        startInterval,
                        startInterval + flexTime))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintReminderJob);

        sInitialized = true;
    }

    synchronized public static void unscheduleTaskNotificationReminder(Context context) {
        Timber.d("unscheduleTaskNotificationReminder -- init'd?: %b", sInitialized);
        if (!sInitialized) return;
        Timber.d("unscheduleTaskNotificationReminder START");

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        dispatcher.cancel(REMINDER_JOB_TAG);

        sInitialized = false;
    }

    // [START] shared preference onChange listener
    public static void setupTaskReminderNotification(Context context, SharedPreferences sharedPreferences) {
        String keyNotification = context.getString(R.string.pref_daily_notification_key);
        boolean defValueNotification = context.getResources().getBoolean(R.bool.pref_daily_notification_default_value);
        boolean enabledNotification = sharedPreferences.getBoolean(keyNotification, defValueNotification);

        // [START] consider location service settings as well
        String keyLocationService = context.getString(R.string.pref_location_service_key);
        boolean defValueLocationService = context.getResources().getBoolean(R.bool.pref_location_service_default_value);
        boolean enabledLocationService = sharedPreferences.getBoolean(keyLocationService, defValueLocationService);
        // [END] consider location service settings as well

        if(enabledNotification) {
            // [START] consider location service settings as well
            if(enabledLocationService) {
                Timber.d("setupTaskReminderNotification: Situation #1: noti:ON, locServ:ON");
                TaskReminderUtilities.unscheduleTaskNotificationReminder(context);
            }
            // [END] consider location service settings as well
            else {
                // [START] Start today's day notification reminder
                Timber.d("setupTaskReminderNotification: Situation #2: noti:ON, locServ:OFF");
                TaskReminderUtilities.scheduleTaskNotificationReminder(context);
                // [END] Start today's day notification reminder
            }
        }
        else {
            // [START] Stop today's day notification reminder
            Timber.d("setupTaskReminderNotification: Situation #3: noti:OFF, locServ:ON/OFF");
            TaskReminderUtilities.unscheduleTaskNotificationReminder(context);
            // [END] Stop today's day notification reminder
        }

    }
    // [END] shared preference onChange listener
}

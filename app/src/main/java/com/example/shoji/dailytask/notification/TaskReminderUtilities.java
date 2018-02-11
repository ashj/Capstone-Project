package com.example.shoji.dailytask.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.example.shoji.dailytask.R;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class TaskReminderUtilities {
    private static final int REMINDER_INTERVAL_MINUTES = 5;
    private static final int REMINDER_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));

    private static final int REMINDER_INTERVAL_DAILY = (int) (TimeUnit.HOURS.toSeconds(24));



    private static final int SYNC_FLEXTIME_SECONDS = 30;
    // TODO - set recurrence to REMINDER_INTERVAL_DAILY
    private static final int REMINDER_RECURRENCE_INTERVAL_SECONDS = REMINDER_INTERVAL_DAILY;



    private static final String REMINDER_JOB_TAG = "task_notification_reminder_tag";

    private static boolean sInitialized;


    private static Calendar getNextNotificationStartInterval(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // [START] retrive stored time from picker
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultValue = context.getResources().getInteger(R.integer.pref_time_picker_minutes_default_value);
        int storedTimeMinutes = sharedPreferences.getInt(context.getString(R.string.pref_time_picker_minutes_key),
                defaultValue);

        Timber.d("[SCHEDULE]cooldown - Stored time: %d minutes", storedTimeMinutes);

        int minutes = (storedTimeMinutes % 60);
        int hours = (storedTimeMinutes / 60);

        Timber.d("[SCHEDULE]cooldown - Stored time: %02dh%02dm", hours, minutes);

        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        // [END] retrive stored time from picker

        return calendar;
    }

    synchronized public static void scheduleTaskNotificationReminder(@NonNull final Context context) {
        Timber.d("scheduleTaskNotificationReminder -- init'd?: %b", sInitialized);
        if (sInitialized) return;
        Timber.d("scheduleTaskNotificationReminder START");

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        int flexTime = SYNC_FLEXTIME_SECONDS;

        // [START] use calendar to calculate startInterval
        Calendar calendar = getNextNotificationStartInterval(context);

        long currentTime = System.currentTimeMillis();
        long startIntervalMillis = (calendar.getTimeInMillis() - currentTime);

        long recurrenceIntervalMillis = TimeUnit.SECONDS.toMillis(REMINDER_RECURRENCE_INTERVAL_SECONDS);
        // sum until next time frame
        while (startIntervalMillis <= 0) {
            startIntervalMillis += recurrenceIntervalMillis;
            //Timber.d("[SCHEDULE] RESET to show approx in: %d secs", startInterval);
        }

        int startInterval = (int) TimeUnit.MILLISECONDS.toSeconds(startIntervalMillis);
        // dbg
        int seconds = startInterval;
        int minutes = seconds / 60;
        seconds = seconds - 60 * minutes;
        int hours = minutes / 60;
        minutes = minutes - 60 * hours;
        Timber.d("[SCHEDULE] Set to show approx in: %02dh%02dm%02ds (%d ms)", hours,minutes, seconds, startInterval);
        // [END] use calendar to calculate startInterval

        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(TaskReminderFirebaseJobService.class)
                .setTag(REMINDER_JOB_TAG)
                //.setConstraints(Constraint.DEVICE_CHARGING)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(false)
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

    // [START]
    public static void rescheduleTaskNotificationReminder(Context context) {
        Timber.d("rescheduleTaskNotificationReminder -- START");
        if(sInitialized) {
            unscheduleTaskNotificationReminder(context);
        }
        if(!sInitialized) {
            scheduleTaskNotificationReminder(context);
        }
        Timber.d("rescheduleTaskNotificationReminder -- END");
    }
    // [END]

    // [START] shared preference onChange listener
    public static void setupTaskReminderNotification(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setupTaskReminderNotification(context, sharedPreferences);
    }

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
                TaskReminderUtilities.rescheduleTaskNotificationReminder(context);
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

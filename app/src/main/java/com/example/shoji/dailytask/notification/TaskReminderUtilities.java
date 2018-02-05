package com.example.shoji.dailytask.notification;

import android.content.Context;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
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
        Timber.d("scheduleTaskNotificationReminder -- %b", sInitialized);
        if (sInitialized) return;
        Timber.d("scheduleTaskNotificationReminder START");

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(TaskReminderFirebaseJobService.class)
                .setTag(REMINDER_JOB_TAG)
                //.setConstraints(Constraint.DEVICE_CHARGING)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        REMINDER_INTERVAL_DAILY,
                        REMINDER_INTERVAL_DAILY + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintReminderJob);

        sInitialized = true;
    }

    synchronized public static void unscheduleTaskNotificationReminder(Context context) {
        Timber.d("scheduleTaskNotificationReminder -- %b", sInitialized);
        if (!sInitialized) return;
        Timber.d("unscheduleTaskNotificationReminder START");

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        dispatcher.cancel(REMINDER_JOB_TAG);

        sInitialized = false;
    }

}

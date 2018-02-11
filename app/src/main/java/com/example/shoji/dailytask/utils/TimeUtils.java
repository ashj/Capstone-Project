package com.example.shoji.dailytask.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.example.shoji.dailytask.R;

import timber.log.Timber;

public class TimeUtils {

    // [START] last task completed timestamp
    public static void setLatestTaskCompletedTimestamp(Context context, long timeInMillis) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_latest_task_completed_timestamp_key);
        long storedTimeInMillis = sharedPreferences.getLong(key,
                context.getResources().getInteger(R.integer.pref_latest_task_completed_timestamp_default_value));
        if(timeInMillis > storedTimeInMillis) {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putLong(key, timeInMillis);

            editor.apply();
            Timber.d("setLatestTaskCompletedTimestamp - time: %d", timeInMillis);
        }
        else {
            Timber.d("setLatestTaskCompletedTimestamp not updated. Passed parameter is older than current");
        }
    }

    public static long getLatestTaskCompletedTimestamp(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_latest_task_completed_timestamp_key);

        long timeInMillis = sharedPreferences.getLong(key,
                context.getResources().getInteger(R.integer.pref_latest_task_completed_timestamp_default_value));
        Timber.d("getLatestTaskCompletedTimestamp - time: %d", timeInMillis);
        return timeInMillis;
    }

    public static boolean isTaskUnderCooldown(Context context) {
        long currentTime = System.currentTimeMillis();
        long storedTime = getLatestTaskCompletedTimestamp(context);

        // TODO - set proper cooldown period
        boolean cooldown = currentTime - storedTime < 60000;

        Timber.d("LatestTaskCompletedTimestamp - cooldown?: %b", cooldown);
        return cooldown;
    }
    // [END] last task completed timestamp

    // [START] time in millis to a preformatted string
    public static String timeInMillisToFormattedString(Context context, long time) {
        int flags = DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_SHOW_TIME;
        String dateStr = DateUtils.formatDateTime(context, time, flags);
        return dateStr;
    }
    // [END] time in millis to a preformatted string
}

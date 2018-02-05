package com.example.shoji.dailytask.notification;


import android.app.IntentService;
import android.content.Intent;

import com.example.shoji.dailytask.BuildConfig;


public class TaskIntentService extends IntentService {
    public TaskIntentService() {
        super(BuildConfig.APPLICATION_ID + ".notification.TaskIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        IntentServiceTasks.executeTask(this, intent);
    }
}

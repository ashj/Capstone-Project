package com.example.shoji.dailytask.notification;


import android.app.IntentService;
import android.content.Intent;


public class TaskIntentService extends IntentService {
    public TaskIntentService() {
        super("com.example.shoji.dailytask.notification.TaskIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        IntentServiceTasks.executeTask(this, intent);
    }
}

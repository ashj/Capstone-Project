package com.example.shoji.dailytask.notification;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.example.shoji.dailytask.intentservice.TaskIntentServiceTasks;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import timber.log.Timber;

public class TaskReminderFirebaseJobService extends JobService {

    private AsyncTask mBackgroundTask;


    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Timber.d("onStartJob");
        final Context context = this;
        mBackgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                Timber.d("onStartJob - doInBackground");
                Intent intent = new Intent();
                intent.setAction(TaskIntentServiceTasks.ACTION_TASK_REMINDER);
                TaskIntentServiceTasks.executeTask(context, intent);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(jobParameters, false);
            }
        };

        mBackgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Timber.d("onStopJob");
        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }
}
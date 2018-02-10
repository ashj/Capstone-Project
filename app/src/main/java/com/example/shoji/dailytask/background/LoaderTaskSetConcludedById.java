package com.example.shoji.dailytask.background;


import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;

import com.example.shoji.dailytask.notification.IntentServiceTasks;
import com.example.shoji.dailytask.notification.TaskReminderUtilities;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;
import com.example.shoji.dailytask.utils.TimeUtils;

import timber.log.Timber;

public class LoaderTaskSetConcludedById implements LoaderCallBacksListenersInterface<Integer> {
    public static final String EXTRA_TASK_ID = "extra-task-id";
    public static final String EXTRA_TASK_CONCLUDED_STATE = "extra-task-concluded-state";

    private OnTaskSetStateListener mOnTaskSetStateListener;

    public LoaderTaskSetConcludedById(OnTaskSetStateListener onTaskSetStateListener) {
        mOnTaskSetStateListener = onTaskSetStateListener;
    }

    public interface OnTaskSetStateListener {
        void onTaskSetState(Integer integer);
    }

    @Override
    public void onStartLoading(Context context) { }

    @Override
    public Integer onLoadInBackground(Context context, Bundle args) {
        Integer integer = null;

        if(args != null && args.containsKey(EXTRA_TASK_ID)
                        && args.containsKey(EXTRA_TASK_CONCLUDED_STATE)) {

            long id = args.getLong(EXTRA_TASK_ID);
            long concludedState = args.getLong(EXTRA_TASK_CONCLUDED_STATE);

            int rows = update(context, id, concludedState);

            //[START] update the widget
            if(rows > 0)
                IntentServiceTasks.startTaskWidgetUpdate(context);
            //[END] update the widget

            integer = Integer.valueOf(rows);
        }

        return integer;
    }

    @Override
    public void onLoadFinished(Context context, Integer integer) {
        mOnTaskSetStateListener.onTaskSetState(integer);
    }

    // [START] update task state
    public static int update(Context context, long id, long state) {
        String selection = TaskContract._ID + " IS " + id;

        // [START] Set task conclusion state and date
        ContentValues cv = new ContentValues();
        cv.put(TaskContract.COLUMN_IS_CONCLUDED, state);

        long modificationDate = System.currentTimeMillis();
        cv.put(TaskContract.COLUMN_CONCLUDED_DATE, modificationDate);
        // [END] Set task conclusion tate and date

        int rows = context.getContentResolver().update(TaskProvider.Tasks.CONTENT_URI, cv, selection, null);

        // [START] last task completed timestamp
        if(rows == 1 && state == TaskContract.CONCLUDED) {
            Timber.d("LatestTaskCompletedTimestamp: %d", state);
            TimeUtils.setLatestTaskCompletedTimestamp(context, modificationDate);
        }
        // [END] last task completed timestamp


        return rows;
    }
    // [END] update task state
}

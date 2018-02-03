package com.example.shoji.dailytask.background;


import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;

import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

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
            int concludedState = args.getInt(EXTRA_TASK_CONCLUDED_STATE);

            String selection = TaskContract._ID + " IS " + id;

            // TODO update concluded_date
            ContentValues cv = new ContentValues();
            cv.put(TaskContract.COLUMN_IS_CONCLUDED, concludedState);
            int rows = context.getContentResolver().update(TaskProvider.Tasks.CONTENT_URI, cv, selection, null);

            integer = Integer.valueOf(rows);
        }

        return integer;
    }

    @Override
    public void onLoadFinished(Context context, Integer integer) {
        mOnTaskSetStateListener.onTaskSetState(integer);
    }
}

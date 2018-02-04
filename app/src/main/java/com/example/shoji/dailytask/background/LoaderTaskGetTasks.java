package com.example.shoji.dailytask.background;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

public class LoaderTaskGetTasks implements LoaderCallBacksListenersInterface<Cursor> {
    public static final String EXTRA_WHERE = "extra-where";
    public static final String EXTRA_SORT_BY = "extra-sort-by";

    private OnTaskGetTasksListener mOnTaskGetTasksListener;

    // [START] Select not concluded tasks for main screen and today's task
    public static final String NOT_CONCLUDED_TASKS_WHERE = TaskContract.COLUMN_IS_CONCLUDED + " IS " + TaskContract.NOT_CONCLUDED;
    public static final String NOT_CONCLUDED_TASKS_SORT_BY = TaskContract.COLUMN_PRIORITY + " DESC"
            + " , " + TaskContract.COLUMN_CONCLUDED_DATE + " ASC";
    // [END] Select not concluded tasks for main screen and today's task

    // [START] Select concluded tasks for history
    public static final String CONCLUDED_TASKS_WHERE = TaskContract.COLUMN_IS_CONCLUDED + " IS " + TaskContract.CONCLUDED;
    public static final String CONCLUDED_TASKS_SORT_BY = TaskContract.COLUMN_CONCLUDED_DATE + " DESC";
    // [START] Select concluded tasks for history

    public LoaderTaskGetTasks(OnTaskGetTasksListener onTaskGetByIdListener) {
        mOnTaskGetTasksListener = onTaskGetByIdListener;
    }

    public interface OnTaskGetTasksListener {
        void onStartLoading(Context context);
        void onLoadFinished(Cursor cursor);
    }

    @Override
    public void onStartLoading(Context context) {
        mOnTaskGetTasksListener.onStartLoading(context);
    }

    @Override
    public Cursor onLoadInBackground(Context context, Bundle args) {
        return queryTasks(context, args);
    }

    @Override
    public void onLoadFinished(Context context, Cursor cursor) {
        mOnTaskGetTasksListener.onLoadFinished(cursor);
    }

    public static Cursor queryTasks(Context context) {
        return queryTasks(context, null);
    }

    public static Cursor queryTasks(Context context, Bundle args) {
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        if(args != null) {
            selection = args.getString(EXTRA_WHERE, null);
            sortOrder = args.getString(EXTRA_SORT_BY, null);
        }

        Cursor cursor = context.getContentResolver().query(TaskProvider.Tasks.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);

        return cursor;

    }
}

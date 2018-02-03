package com.example.shoji.dailytask.background;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

public class LoaderTaskGetById implements LoaderCallBacksListenersInterface<Cursor> {
    public static final String EXTRA_TASK_ID = "extra-task-id";

    private OnTaskGetByIdListener mOnTaskGetByIdListener;

    public LoaderTaskGetById(OnTaskGetByIdListener onTaskGetByIdListener) {
        mOnTaskGetByIdListener = onTaskGetByIdListener;
    }

    public interface OnTaskGetByIdListener {
        void onStartLoading(Context context);
        void onLoadFinished(Cursor cursor);
    }

    @Override
    public void onStartLoading(Context context) {
        mOnTaskGetByIdListener.onStartLoading(context);
    }

    @Override
    public Cursor onLoadInBackground(Context context, Bundle args) {
        Cursor cursor = null;
        if(args != null && args.containsKey(EXTRA_TASK_ID)) {
            long id = args.getLong(EXTRA_TASK_ID);
            String[] projection = null;
            // Select by taskId
            String selection = TaskContract._ID + " IS " + id;
            String[] selectionArgs = null;
            String sortOrder = null;

            cursor = context.getContentResolver().query(TaskProvider.Tasks.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder);
        }
        return cursor;
    }

    @Override
    public void onLoadFinished(Context context, Cursor cursor) {
        mOnTaskGetByIdListener.onLoadFinished(cursor);
    }
}

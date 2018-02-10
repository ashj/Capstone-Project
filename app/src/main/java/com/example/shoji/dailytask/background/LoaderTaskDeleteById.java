package com.example.shoji.dailytask.background;


import android.content.Context;
import android.os.Bundle;

import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;
import com.example.shoji.dailytask.widget.WidgetUtils;

public class LoaderTaskDeleteById implements LoaderCallBacksListenersInterface<Integer> {
    public static final String EXTRA_TASK_ID = "extra-task-id";

    private OnTaskDeletedListener mOnTaskDeletedListener;

    public LoaderTaskDeleteById(OnTaskDeletedListener onTaskDeletedListener) {
        mOnTaskDeletedListener = onTaskDeletedListener;
    }

    public interface OnTaskDeletedListener {
        void onTaskDeleted(Integer integer);
    }

    @Override
    public void onStartLoading(Context context) { }

    @Override
    public Integer onLoadInBackground(Context context, Bundle args) {
        Integer integer = null;

        if(args != null && args.containsKey(EXTRA_TASK_ID)) {
            long id = args.getLong(EXTRA_TASK_ID);
            String selection = TaskContract._ID + " IS " + id;
            int rows = context.getContentResolver().delete(TaskProvider.Tasks.CONTENT_URI,
                    selection, null);

            //[START] update the widget
            if(rows > 0)
                WidgetUtils.startTaskWidgetUpdate(context);
            //[END] update the widget

            integer = Integer.valueOf(rows);
        }

        return integer;
    }

    @Override
    public void onLoadFinished(Context context, Integer integer) {
        mOnTaskDeletedListener.onTaskDeleted(integer);
    }
}

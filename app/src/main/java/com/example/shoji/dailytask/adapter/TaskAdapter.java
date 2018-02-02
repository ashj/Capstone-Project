package com.example.shoji.dailytask.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.provider.TaskContract;

import timber.log.Timber;

public class TaskAdapter
             extends RecyclerView.Adapter<RecyclerView.ViewHolder>
             implements TaskViewHolder.OnClickListener {

    private Context mContext;
    private Cursor mCursor;

    public TaskAdapter(Context context) {
        mContext = context;
    }

    // [START] Override Adapter methods
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.d("onCreateViewHolder");

        boolean attachToRoot = false;
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.view_holder_task_main_list,
                        parent,
                        attachToRoot);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Timber.d("onBindViewHolder: %s", holder);

        mCursor.moveToPosition(position);
        int idx = mCursor.getColumnIndex(TaskContract.COLUMN_TITLE);
        String value = mCursor.getString(idx);
        Timber.d(">>>>>>>>>>>>>>>>>>>> %s", value);
        idx = mCursor.getColumnIndex(TaskContract.COLUMN_PRIORITY);
        int priority = mCursor.getInt(idx);
        Timber.d(">>>>>>>>>>>>>>>>>>>> %d", priority);

        TaskViewHolder taskViewHolder = (TaskViewHolder) holder;

        taskViewHolder.bindViewHolder(mContext, mCursor, this);

    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            Timber.d("COUNT IS ZERO");
            return 0;
        }
        Timber.d("COUNT IS %d", mCursor.getCount());
        return mCursor.getCount();
    }

    /*@Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(mCursor.getColumnIndex(TaskContract._ID));
    }*/

    // [END] Override Adapter methods

    public Cursor swapCursor(Cursor cursor) {
        Timber.d("swapCursor");
        if (mCursor == cursor) {
            Timber.d("No need to swap.");
            return null;
        }
        Cursor old = mCursor;
        mCursor = cursor;

        if (cursor != null) {
            Timber.d("swapCursor - notifyDataSetChanged");
            this.notifyDataSetChanged();
        }
        return old;
    }

    @Override
    public void onClick(int position) {
        mCursor.moveToPosition(position);
    }
}

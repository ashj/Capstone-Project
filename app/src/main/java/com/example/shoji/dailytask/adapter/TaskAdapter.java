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
             extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private OnClickListener mOnClickListener;

    private static final int ITEM_VIEW_TYPE_FIRST_TASK = 0;
    private static final int ITEM_VIEW_TYPE_QUEUED_TASK = 1;

    public interface OnClickListener {
        void onClick(long id);
    }

    public TaskAdapter(Context context, OnClickListener onClickListener) {
        mContext = context;
        mOnClickListener = onClickListener;
    }

    // [START] Override Adapter methods
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.d("onCreateViewHolder");

        boolean attachToRoot = false;
        if (viewType == ITEM_VIEW_TYPE_FIRST_TASK) {
            View view = LayoutInflater.from(mContext)
                    .inflate(TaskFirstViewHolder.RES_LAYOUT_ID,
                            parent,
                            attachToRoot);

            return new TaskFirstViewHolder(view);
        }

        else if(viewType == ITEM_VIEW_TYPE_QUEUED_TASK) {
            View view = LayoutInflater.from(mContext)
                    .inflate(TaskViewHolder.RES_LAYOUT_ID,
                            parent,
                            attachToRoot);

            return new TaskViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        int viewType = getItemViewType(position);

        if(viewType == ITEM_VIEW_TYPE_FIRST_TASK) {
            TaskFirstViewHolder taskFirstViewHolder = (TaskFirstViewHolder) holder;
            TaskFirstViewHolderImpl listener = new TaskFirstViewHolderImpl();
            taskFirstViewHolder.bindViewHolder(mContext, mCursor, listener);
        }

        else if(viewType == ITEM_VIEW_TYPE_QUEUED_TASK) {
            TaskViewHolder taskViewHolder = (TaskViewHolder) holder;
            TaskViewHolderImpl listener = new TaskViewHolderImpl();
            taskViewHolder.bindViewHolder(mContext, mCursor, listener);
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(mCursor.getColumnIndex(TaskContract._ID));
    }

    // [START] Item view type
    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return ITEM_VIEW_TYPE_FIRST_TASK;
        else
            return ITEM_VIEW_TYPE_QUEUED_TASK;
    }
    // [END] Item view type
    // [END] Override Adapter methods

    public Cursor swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return null;
        }
        Cursor old = mCursor;
        mCursor = cursor;

        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return old;
    }

    public void onClickedView(int position) {
        mCursor.moveToPosition(position);
        long itemId = getItemId(position);
        Timber.d("Clicked on pos=%3d, ID=%3d", position, itemId);
        mOnClickListener.onClick(itemId);
    }

    // [START] Implement each view holder listener
    private class TaskViewHolderImpl
            implements TaskViewHolder.OnClickListener {
        @Override
        public void onClick(int position) {
            onClickedView(position);
        }
    }

    private class TaskFirstViewHolderImpl
            implements TaskFirstViewHolder.OnClickListener {

        @Override
        public void onClickView(int position) {
            onClickedView(position);
        }

        @Override
        public void onClickButton() {
            Timber.d("MARKED AS FAV.");
        }
    }
    // [END] Implement each view holder listener
}

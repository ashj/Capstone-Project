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
    private OnClickListener mOnClickListener;

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
        View view = LayoutInflater.from(mContext)
                .inflate(TaskViewHolder.RES_LAYOUT_ID,
                        parent,
                        attachToRoot);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        TaskViewHolder taskViewHolder = (TaskViewHolder) holder;

        taskViewHolder.bindViewHolder(mContext, mCursor, this);
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

    @Override
    public void onClick(int position) {
        mCursor.moveToPosition(position);
        long itemId = getItemId(position);
        Timber.d("Clicked on pos=%3d, ID=%3d", position, itemId);
        mOnClickListener.onClick(itemId);
    }
}

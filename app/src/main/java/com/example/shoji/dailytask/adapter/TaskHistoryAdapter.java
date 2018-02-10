package com.example.shoji.dailytask.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shoji.dailytask.provider.TaskContract;

import timber.log.Timber;

public class TaskHistoryAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private OnClickListener mOnClickListener;


    public interface OnClickListener {
        void onClickTask(long id);
    }

    public TaskHistoryAdapter(Context context, OnClickListener onClickListener) {
        mContext = context;
        mOnClickListener = onClickListener;
    }

    // [START] Override Adapter methods
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.d("onCreateViewHolder");
        View view = LayoutInflater.from(mContext)
                .inflate(TaskHistoryViewHolder.RES_LAYOUT_ID,
                        parent,
                        false);

        return new TaskHistoryViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        TaskHistoryViewHolder taskHistoryViewHolder = (TaskHistoryViewHolder) holder;
        TaskHistoryViewHolderImpl listener = new TaskHistoryViewHolderImpl();
        taskHistoryViewHolder.bindViewHolder(mContext, mCursor, listener);

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

    public void swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return;
        }
        Cursor old = mCursor;
        mCursor = cursor;

        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        if(old != null)
            old.close();
    }

    // [START] OnClickListener
    public void onClickedView(int position) {
        mCursor.moveToPosition(position);
        long itemId = getItemId(position);
        Timber.d("Clicked on pos=%3d, ID=%3d", position, itemId);

        mOnClickListener.onClickTask(itemId);
    }
    // [END] OnClickListener



    // [START] Implement each view holder OnClickListener
    private class TaskHistoryViewHolderImpl
            implements TaskHistoryViewHolder.OnClickListener {
        @Override
        public void onClick(int position) {
            onClickedView(position);
        }
    }
    // [END] Implement each view holder OnClickListener
}

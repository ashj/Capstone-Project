package com.example.shoji.dailytask.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.utils.TimeUtils;

import timber.log.Timber;

public class TaskFirstViewHolder extends RecyclerView.ViewHolder
                 implements View.OnClickListener {
    public static final int RES_LAYOUT_ID = R.layout.view_holder_task_main_list_first;
    private Context mContext;
    private View mItemView;
    private TextView mTodaysTaskView;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private Button mButton;

    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClickView(int position);
        void onClickButton(int position);
    }

    public TaskFirstViewHolder(View itemView) {
        super(itemView);

        View.OnClickListener onClickListener = this;
        mItemView = itemView;
        mItemView.setOnClickListener(onClickListener);

        mTodaysTaskView = mItemView.findViewById(R.id.task_of_the_day);
        mTitleTextView = mItemView.findViewById(R.id.title_text_view);
        mDescriptionTextView = mItemView.findViewById(R.id.description_text_view);

        mButton = mItemView.findViewById(R.id.mark_button);
        mButton.setOnClickListener(onClickListener);
    }

    public void bindViewHolder(Context context,
                               Cursor cursor,
                               OnClickListener onClickListener) {
        mContext = context;
        mOnClickListener = onClickListener;


        int columnIndex = cursor.getColumnIndex(TaskContract.COLUMN_TITLE);
        String title = cursor.getString(columnIndex);

        columnIndex = cursor.getColumnIndex(TaskContract.COLUMN_DESCRIPTION);
        String description = cursor.getString(columnIndex);

        // DBG
//        columnIndex = cursor.getColumnIndex(TaskContract._ID);
//        String id = cursor.getString(columnIndex);
//        columnIndex = cursor.getColumnIndex(TaskContract.COLUMN_PRIORITY);
//        String priority = cursor.getString(columnIndex);
//        title = title + " (id:"+id+"//P"+priority+")";

        mTitleTextView.setText(title);

        mDescriptionTextView.setText(description);
        // [START] last task completed timestamp
        if(TimeUtils.isTaskUnderCooldown(mContext)) {
            mButton.setVisibility(View.GONE);
            mTodaysTaskView.setText(R.string.main_activity_task_of_the_day_after);
        }
        // [END] last task completed timestamp
        else {
            mButton.setVisibility(View.VISIBLE);
            mTodaysTaskView.setText(R.string.main_activity_task_of_the_day);
        }
    }

    @Override
    public void onClick(View view) {
        int adapterPosition = getAdapterPosition();
        Timber.d("Clicked at position: %d", adapterPosition);

        int viewId = view.getId();
        if (viewId == mItemView.getId()) {
            mOnClickListener.onClickView(adapterPosition);
        }
        else if (viewId == mButton.getId()) {
            mOnClickListener.onClickButton(adapterPosition);
        }
    }
}

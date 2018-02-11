package com.example.shoji.dailytask.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.utils.TimeUtils;

import timber.log.Timber;

public class TaskHistoryViewHolder extends RecyclerView.ViewHolder
                 implements View.OnClickListener {
    public static final int RES_LAYOUT_ID = R.layout.view_holder_task_history_list;
    private Context mContext;
    private TextView mTitleTextView;
    private TextView mModifiedDate;

    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClick(int position);
    }

    public TaskHistoryViewHolder(View itemView) {
        super(itemView);

        View.OnClickListener onClickListener = this;
        itemView.setOnClickListener(onClickListener);

        mTitleTextView = itemView.findViewById(R.id.title_text_view);
        mModifiedDate = itemView.findViewById(R.id.modified_date_text_view);
    }

    public void bindViewHolder(Context context,
                               Cursor cursor,
                               OnClickListener onClickListener) {
        mContext = context;
        mOnClickListener = onClickListener;


        int columnIndex = cursor.getColumnIndex(TaskContract.COLUMN_TITLE);
        String title = cursor.getString(columnIndex);

        // DBG
//        columnIndex = cursor.getColumnIndex(TaskContract._ID);
//        String id = cursor.getString(columnIndex);
//        columnIndex = cursor.getColumnIndex(TaskContract.COLUMN_PRIORITY);
//        String priority = cursor.getString(columnIndex);
//        title = title + " (id:"+id+"//P"+priority+")";

        mTitleTextView.setText(title);

        columnIndex = cursor.getColumnIndex(TaskContract.COLUMN_CONCLUDED_DATE);
        long modification_date = cursor.getLong(columnIndex);
        String dateStr = TimeUtils.timeInMillisToFormattedString(context, modification_date);

        mModifiedDate.setText(dateStr);
    }

    @Override
    public void onClick(View view) {
        int adapterPosition = getAdapterPosition();
        Timber.d("Clicked at position: %d", adapterPosition);
        mOnClickListener.onClick(adapterPosition);
    }
}

package com.example.shoji.dailytask.adapter;


import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.provider.TaskContract;

import timber.log.Timber;

public class TaskViewHolder extends RecyclerView.ViewHolder
                 implements View.OnClickListener {
    public static final int RES_LAYOUT_ID = R.layout.view_holder_task_main_list;
    private Context mContext;
    private TextView mTitleTextView;

    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClick(int position);
    }

    public TaskViewHolder(View itemView) {
        super(itemView);

        View.OnClickListener onClickListener = this;
        itemView.setOnClickListener(onClickListener);

        mTitleTextView = itemView.findViewById(R.id.title_text_view);
    }

    public void bindViewHolder(Context context,
                               Cursor cursor,
                               OnClickListener onClickListener) {
        mContext = context;
        mOnClickListener = onClickListener;


        int columnIndex = cursor.getColumnIndex(TaskContract.COLUMN_TITLE);
        String title = cursor.getString(columnIndex);
        mTitleTextView.setText(title);
    }

    @Override
    public void onClick(View view) {
        int adapterPosition = getAdapterPosition();
        Timber.d("Clicked at position: %d", adapterPosition);
        mOnClickListener.onClick(adapterPosition);
    }
}

package com.example.shoji.dailytask.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.provider.TaskContract;

import timber.log.Timber;

public class TaskFirstViewHolder extends RecyclerView.ViewHolder
                 implements View.OnClickListener {
    public static final int RES_LAYOUT_ID = R.layout.view_holder_task_main_list_first;
    private Context mContext;
    private View mItemView;
    private TextView mTitleTextView;
    private Button mButton;

    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClickView(int position);
        void onClickButton();
    }

    public TaskFirstViewHolder(View itemView) {
        super(itemView);

        View.OnClickListener onClickListener = this;
        mItemView = itemView;
        mItemView.setOnClickListener(onClickListener);

        mTitleTextView = mItemView.findViewById(R.id.title_text_view);

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

        columnIndex = cursor.getColumnIndex(TaskContract._ID);
        String id = cursor.getString(columnIndex);

        columnIndex = cursor.getColumnIndex(TaskContract.COLUMN_PRIORITY);
        String priority = cursor.getString(columnIndex);

        String text = title + " (id:"+id+"//P"+priority+")";
        mTitleTextView.setText(text);
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
            mOnClickListener.onClickButton();
        }
    }
}

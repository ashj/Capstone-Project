package com.example.shoji.dailytask.adapter;


import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.utils.TimeUtils;

import timber.log.Timber;

public class TaskHistoryViewHolder extends RecyclerView.ViewHolder
                 implements View.OnClickListener {
    public static final int RES_LAYOUT_ID = R.layout.view_holder_task_history_list;
    private Context mContext;
    private ImageView mCheckImage;
    private TextView mTitleTextView;
    private TextView mModifiedDate;

    private OnClickListener mOnClickListener;

    private static int[] sColors;

    public interface OnClickListener {
        void onClick(int position);
    }

    public TaskHistoryViewHolder(View itemView) {
        super(itemView);

        View.OnClickListener onClickListener = this;
        itemView.setOnClickListener(onClickListener);

        mCheckImage = itemView.findViewById(R.id.checked_image);
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
//        String priorityStr = cursor.getString(columnIndex);
//        title = title + " (id:"+id+"//P"+priorityStr+")";

        mTitleTextView.setText(title);

        columnIndex = cursor.getColumnIndex(TaskContract.COLUMN_CONCLUDED_DATE);
        long modification_date = cursor.getLong(columnIndex);
        String dateStr = TimeUtils.timeInMillisToFormattedString(context, modification_date);

        mModifiedDate.setText(dateStr);

        // [START] tint check icon
        if(sColors == null) {
            sColors = initColorMap(context);
        }
        columnIndex = cursor.getColumnIndex(TaskContract.COLUMN_PRIORITY);
        int priority = cursor.getInt(columnIndex);
        if(0 <= priority && priority < sColors.length) {
            int color = sColors[priority];
            ImageViewCompat.setImageTintList(mCheckImage,
                    ColorStateList.valueOf(color));
        }
        // [END] tint check icon
    }

    private int[] initColorMap(Context context) {
        return context.getResources().getIntArray(R.array.priority_color_array);
    }

    @Override
    public void onClick(View view) {
        int adapterPosition = getAdapterPosition();
        Timber.d("Clicked at position: %d", adapterPosition);
        mOnClickListener.onClick(adapterPosition);
    }
}

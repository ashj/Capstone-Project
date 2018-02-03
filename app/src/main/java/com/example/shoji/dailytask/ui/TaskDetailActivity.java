package com.example.shoji.dailytask.ui;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.background.LoaderCallBacksListenersInterface;
import com.example.shoji.dailytask.background.LoaderIds;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

import timber.log.Timber;

public class TaskDetailActivity extends AppCompatActivityEx
                                implements LoaderCallBacksListenersInterface<Cursor> {

    public static final String EXTRA_TASK_ID = "extra-task-id";

    private long mTaskId;
    private ProgressBar mProgressBar;
    private TextView mTaskTitleTextView;
    private FloatingActionButton mFab;

    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        final Context context = this;

        // Set title in action bar
        getSupportActionBar().setTitle(getString(R.string.task_details_activity_title));

        mProgressBar = findViewById(R.id.progressbar);
        mTaskTitleTextView = findViewById(R.id.task_title_text_view);

        // [START] need valid intent to proceed
        mTaskId = getIdFromIntent();
        Timber.d("Got ID: %d", mTaskId);
        if(mTaskId == TaskContract.INVALID_ID)
            return;
        // [END] need valid intent to proceed

        // [START] use FAB to open a task to edit
        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskEditorActivity.class);
                intent.putExtra(TaskEditorActivity.EXTRA_TASK_ID, mTaskId);
                startActivity(intent);
            }
        });
        // [END] use FAB to open a task to edit

        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_DETAIL);
    }

    // [START] need valid intent to proceed
    private long getIdFromIntent() {
        long id = TaskContract.INVALID_ID;

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(EXTRA_TASK_ID))
            id = intent.getLongExtra(EXTRA_TASK_ID, TaskContract.INVALID_ID);

        return id;
    }
    // [END] need valid intent to proceed



    // [START] implements LoaderCallBacksListenersInterface<Cursor>
    @Override
    public void onStartLoading(Context context) {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public Cursor onLoadInBackground(Context context, Bundle args) {
        String[] projection = null;
        // Select by taskId
        String selection = TaskContract._ID + " IS " + mTaskId;
        String[] selectionArgs = null;
        String sortOrder = null;

        Cursor cursor = getContentResolver().query(TaskProvider.Tasks.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);
        return cursor;
    }

    @Override
    public void onLoadFinished(Context context, Cursor cursor) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mCursor = cursor;

        if(mCursor == null || mCursor.getCount() != 1)
            return;

        mCursor.moveToPosition(0);

        int index = mCursor.getColumnIndex(TaskContract.COLUMN_TITLE);
        String title = mCursor.getString(index);

        index = mCursor.getColumnIndex(TaskContract.COLUMN_DESCRIPTION);
        String description = mCursor.getString(index);

        index = mCursor.getColumnIndex(TaskContract.COLUMN_PRIORITY);
        String priority = mCursor.getString(index);

        index = mCursor.getColumnIndex(TaskContract.COLUMN_IS_CONCLUDED);
        int concluded = mCursor.getInt(index);

        index = mCursor.getColumnIndex(TaskContract.COLUMN_CONCLUDED_DATE);
        int concluded_date = mCursor.getInt(index);

        mCursor.close();

        StringBuilder sb = new StringBuilder();
        sb.append("Task: ").append(title)
                .append("\nDescr: ").append(description)
                .append("\nPriority: P").append(priority)
                .append("\nConcluded?: ").append(concluded)
                .append("\nConcluded Date: ").append(concluded_date);
        mTaskTitleTextView.setText(sb.toString());


    }
    // [END] implements LoaderCallBacksListenersInterface<Cursor>
}
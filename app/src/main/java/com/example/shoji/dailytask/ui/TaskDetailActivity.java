package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.background.LoaderCallBacksListenersInterface;
import com.example.shoji.dailytask.background.LoaderIds;
import com.example.shoji.dailytask.provider.TaskContentObserver;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

import timber.log.Timber;

public class TaskDetailActivity extends AppCompatActivityEx
                                implements LoaderCallBacksListenersInterface<Cursor>,
                                           TaskContentObserver.OnChangeListener {

    public static final String EXTRA_TASK_ID = "extra-task-id";

    private long mTaskId;
    private ProgressBar mProgressBar;
    private TextView mTaskTitleTextView;
    private FloatingActionButton mFabEdit;
    private FloatingActionButton mFabDelete;

    private Cursor mCursor;
    private static TaskContentObserver sTaskContentObserver;

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

        // [START] use FAB to open the task to edit
        mFabEdit = findViewById(R.id.fab_edit);
        mFabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskEditorActivity.class);
                intent.putExtra(TaskEditorActivity.EXTRA_TASK_ID, mTaskId);
                startActivity(intent);
            }
        });
        // [END] use FAB to open the task to edit

        // [START] use FAB to delete the task
        mFabDelete = findViewById(R.id.fab_delete);
        mFabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(context);
            }
        });
        // [END] use FAB to delete the task

        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_DETAIL);

        // [START] ContentObserver
        TaskContentObserver.OnChangeListener onChangeListener = this;
        sTaskContentObserver = new TaskContentObserver(getContentResolver(), onChangeListener);
        // [END] ContentObserver
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

    // [START] use FAB to delete the task
    private void showDeleteDialog(Context context) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Do nothing
                        break;
                    default:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.task_details_delete_task_prompt)
                .setPositiveButton(R.string.task_details_delete_task_positive, dialogClickListener)
                .setNegativeButton(R.string.task_details_delete_task_negative, dialogClickListener)
                .show();
    }
    // [END] use FAB to delete the task



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



    // [START] ContentObserver
    @Override
    protected void onStart() {
        super.onStart();

        if(sTaskContentObserver != null) {
            sTaskContentObserver.register();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(sTaskContentObserver != null) {
            sTaskContentObserver.unregister();
        }
    }

    @Override
    public void onChange() {
        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_DETAIL);
    }
    // [END] ContentObserver
}
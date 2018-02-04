package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.adapter.TaskAdapter;
import com.example.shoji.dailytask.adapter.TaskHistoryAdapter;
import com.example.shoji.dailytask.background.LoaderCallBacksListenersInterface;
import com.example.shoji.dailytask.background.LoaderIds;
import com.example.shoji.dailytask.background.LoaderTaskGetTasks;
import com.example.shoji.dailytask.background.LoaderUtils;
import com.example.shoji.dailytask.provider.TaskContentObserver;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

public class TaskHistoryActivity extends AppCompatActivityEx
                                 implements LoaderCallBacksListenersInterface<Cursor>,
                                            TaskHistoryAdapter.OnClickListener,
                                            TaskContentObserver.OnChangeListener {

    private ProgressBar mProgressBar;
    private TaskHistoryAdapter mTaskAdapter;
    private RecyclerView mRecyclerView;
    private static TaskContentObserver sTaskContentObserver;
    private Cursor mCursor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_history);

        // Set title in action bar
        getSupportActionBar().setTitle(getString(R.string.menu_history_label));

        final Context context = this;

        mProgressBar = findViewById(R.id.progressbar);

        mRecyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager lym =
                new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(lym);

        // [START] Adapter initialization
        TaskHistoryAdapter.OnClickListener onClickListener = this;
        mTaskAdapter = new TaskHistoryAdapter(context, onClickListener);
        mTaskAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mTaskAdapter);
        // [END] Adapter initialization

        // [START] implements LoaderCallBacksListenersInterface<Cursor>
        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_HISTORY, this);
        // [END] implements LoaderCallBacksListenersInterface<Cursor>

        // [START] ContentObserver
        TaskContentObserver.OnChangeListener onChangeListener = this;
        sTaskContentObserver = new TaskContentObserver(getContentResolver(), onChangeListener);
        // [END] ContentObserver
    }

    // [START] implements LoaderCallBacksListenersInterface<Cursor>
    @Override
    public void onStartLoading(Context context) {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public Cursor onLoadInBackground(Context context, Bundle args) {
        String[] projection = null;
        // Select concluded tasks
        String selection = LoaderTaskGetTasks.CONCLUDED_TASKS_WHERE;
        String[] selectionArgs = null;
        // Sort by concluded date (most recent concluded tasks on top)
        String sortOrder = LoaderTaskGetTasks.CONCLUDED_TASKS_SORT_BY;

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
        mTaskAdapter.swapCursor(mCursor);
    }
    // [END] implements LoaderCallBacksListenersInterface<Cursor>




    // [START] OnClickListener
    @Override
    public void onClickTask(long id) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, id);
        startActivity(intent);
    }
    // [END] OnClickListener



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
        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_HISTORY, this);
    }
    // [END] ContentObserver
}

package com.example.shoji.dailytask.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shoji.dailytask.BuildConfig;
import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.adapter.TaskAdapter;
import com.example.shoji.dailytask.background.LoaderCallBacksListenersInterface;
import com.example.shoji.dailytask.background.LoaderIds;
import com.example.shoji.dailytask.background.LoaderTaskGetById;
import com.example.shoji.dailytask.background.LoaderTaskGetTasks;
import com.example.shoji.dailytask.background.LoaderTaskSetConcludedById;
import com.example.shoji.dailytask.notification.TaskNotification;
import com.example.shoji.dailytask.provider.TaskContentObserver;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

import timber.log.Timber;

public class MainActivity extends AppCompatActivityEx
                          implements LoaderTaskGetTasks.OnTaskGetTasksListener,
                                     TaskAdapter.OnClickListener,
                                     TaskContentObserver.OnChangeListener,
                                     LoaderTaskSetConcludedById.OnTaskSetStateListener {

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private TaskAdapter mTaskAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private static TaskContentObserver sTaskContentObserver;
    private Cursor mCursor;

    // [START] get tasks
    private Bundle mBundle;
    LoaderTaskGetTasks.OnTaskGetTasksListener mListener;
    LoaderTaskGetTasks mLoaderTaskGetTasks;
    private static final String WHERE = TaskContract.COLUMN_IS_CONCLUDED + " IS " + TaskContract.NOT_CONCLUDED;
    private static final String SORT_BY = TaskContract.COLUMN_PRIORITY + " DESC"
                                + " , " + TaskContract.COLUMN_CONCLUDED_DATE + " ASC";
    // [END] get tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            if(BuildConfig.DEBUG)
                Timber.plant(new Timber.DebugTree());
            Timber.d("Logging With Timber");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mProgressBar = findViewById(R.id.progressbar);

        mRecyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager lym =
                new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(lym);

        mFab = findViewById(R.id.fab);


        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskEditorActivity.class);
                startActivity(intent);
            }
        });

        // [START] Adapter initialization
        TaskAdapter.OnClickListener onClickListener = this;
        mTaskAdapter = new TaskAdapter(context, onClickListener);
        mTaskAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mTaskAdapter);
        // [END] Adapter initialization

        // [START] implements LoaderCallBacksListenersInterface<Cursor>
        //initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_MAIN, this);
        // [END] implements LoaderCallBacksListenersInterface<Cursor>

        // [START] get tasks
        mBundle = new Bundle();
        mBundle.putString(LoaderTaskGetTasks.EXTRA_WHERE, WHERE);
        mBundle.putString(LoaderTaskGetTasks.EXTRA_SORT_BY, SORT_BY);
        mListener = this;
        mLoaderTaskGetTasks = new LoaderTaskGetTasks(mListener);
        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_MAIN, mBundle, mLoaderTaskGetTasks);
        // [END] get tasks

        // [START] ContentObserver
        TaskContentObserver.OnChangeListener onChangeListener = this;
        sTaskContentObserver = new TaskContentObserver(getContentResolver(), onChangeListener);
        // [END] ContentObserver
    }

    // [START] Toolbar - inflate and item selected
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_history) {
            Intent intent = new Intent(this, TaskHistoryActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // [END] Toolbar - inflate and item selected




    // [START] get tasks
    @Override
    public void onStartLoading(Context context) {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadFinished(Cursor cursor) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mCursor = cursor;
        mTaskAdapter.swapCursor(mCursor);

        // [START][TEMP] TODO - temporary notifications
        if(mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToPosition(0);
            int index = mCursor.getColumnIndex(TaskContract.COLUMN_TITLE);
            String title = mCursor.getString(index);
            TaskNotification.notifyTodaysTask(this, title);
        }
        // [END] today'a task notification
    }
    // [END] get tasks




    // [START] OnClickListener
    @Override
    public void onClickTask(long id) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, id);
        startActivity(intent);
    }

    @Override
    public void onClickDoneTask(long id) {
        // [START] mark test as done
        Bundle args = new Bundle();
        args.putLong(LoaderTaskSetConcludedById.EXTRA_TASK_ID, id);
        args.putLong(LoaderTaskSetConcludedById.EXTRA_TASK_CONCLUDED_STATE, TaskContract.CONCLUDED);

        LoaderTaskSetConcludedById.OnTaskSetStateListener listener = this;
        LoaderTaskSetConcludedById loaderTaskSetConcludedById = new LoaderTaskSetConcludedById(listener);

        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_UPDATE_MAIN, args, loaderTaskSetConcludedById);
        // [END] mark test as done
    }

    // [START] mask test as done
    @Override
    public void onTaskSetState(Integer integer) {
        if(integer != null && integer == 1)
            Toast.makeText(this, R.string.main_activity_task_marked_as_done, Toast.LENGTH_SHORT).show();
    }
    // [END] mask test as done
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
        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_MAIN, mBundle, mLoaderTaskGetTasks);
    }
    // [END] ContentObserver



}

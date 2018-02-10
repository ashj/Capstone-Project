package com.example.shoji.dailytask.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shoji.dailytask.BuildConfig;
import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.adapter.TaskAdapter;
import com.example.shoji.dailytask.background.LoaderIds;
import com.example.shoji.dailytask.background.LoaderTaskGetTasks;
import com.example.shoji.dailytask.background.LoaderTaskSetConcludedById;
import com.example.shoji.dailytask.notification.IntentServiceTasks;
import com.example.shoji.dailytask.notification.TaskIntentService;
import com.example.shoji.dailytask.notification.TaskNotification;
import com.example.shoji.dailytask.notification.TaskReminderUtilities;
import com.example.shoji.dailytask.provider.TaskContentObserver;
import com.example.shoji.dailytask.provider.TaskContract;

import timber.log.Timber;

public class MainActivity extends AppCompatActivityEx
                          implements LoaderTaskGetTasks.OnTaskGetTasksListener,
                                     TaskAdapter.OnClickListener,
                                     TaskContentObserver.OnChangeListener,
                                     LoaderTaskSetConcludedById.OnTaskSetStateListener {
    private static boolean sPlantedTimberTree;

    private TextView mTextView;
    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private TaskAdapter mTaskAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private static TaskContentObserver sTaskContentObserver;
    private Cursor mCursor;
    private final static String SAVE_INSTANCE_STATE_LIST_POSITION = "list-position";

    // [START] get tasks
    private Bundle mBundle;
    LoaderTaskGetTasks.OnTaskGetTasksListener mListener;
    LoaderTaskGetTasks mLoaderTaskGetTasks;
    // [END] get tasks


    private static final int NAV_TO_TASK_EDITOR = 350;
    private static final int NAV_TO_TASK_DETAIL = 351;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(sPlantedTimberTree == false) {
            if(BuildConfig.DEBUG)
                Timber.plant(new Timber.DebugTree());
            Timber.d("Logging With Timber");
            sPlantedTimberTree = true;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;

        mTextView = findViewById(R.id.text_view);

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
                startActivityForResult(intent, NAV_TO_TASK_EDITOR);
            }
        });

        // [START] Adapter initialization
        TaskAdapter.OnClickListener onClickListener = this;
        mTaskAdapter = new TaskAdapter(context, onClickListener);
        mTaskAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mTaskAdapter);
        // [END] Adapter initialization

        // [START] Save instance state - restore
        if(savedInstanceState != null &&
                savedInstanceState.containsKey(SAVE_INSTANCE_STATE_LIST_POSITION)) {
            Parcelable listState = savedInstanceState
                    .getParcelable(SAVE_INSTANCE_STATE_LIST_POSITION);
            mRecyclerView.getLayoutManager()
                    .onRestoreInstanceState(listState);
        }
        // [END] Save instance state - restore

        // [START] implements LoaderCallBacksListenersInterface<Cursor>
        //initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_MAIN, this);
        // [END] implements LoaderCallBacksListenersInterface<Cursor>

        // [START] get tasks
        mBundle = new Bundle();
        mBundle.putString(LoaderTaskGetTasks.EXTRA_WHERE, LoaderTaskGetTasks.NOT_CONCLUDED_TASKS_WHERE);
        mBundle.putString(LoaderTaskGetTasks.EXTRA_SORT_BY,  LoaderTaskGetTasks.NOT_CONCLUDED_TASKS_SORT_BY);
        mListener = this;
        mLoaderTaskGetTasks = new LoaderTaskGetTasks(mListener);
        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_MAIN, mBundle, mLoaderTaskGetTasks);
        // [END] get tasks

        // [START] ContentObserver
        TaskContentObserver.OnChangeListener onChangeListener = this;
        sTaskContentObserver = new TaskContentObserver(getContentResolver(), onChangeListener);
        // [END] ContentObserver

        // [START] Start today's day notification reminder - first time run
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        TaskReminderUtilities.setupTaskReminderNotification(context, sharedPreferences);
        // [END] Start today's day notification reminder - first time run

        // TODO, remove later - to test the broadcast receiver
//        GeofenceBroadcastReceiver br = new GeofenceBroadcastReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_POWER_CONNECTED);
//        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
//        this.registerReceiver(br, filter);
//        Intent intent = new Intent();
//        sendBroadcast(intent);
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

        if(mCursor != null && mCursor.getCount() > 0) {
            mTextView.setVisibility(View.INVISIBLE);
        }
        else {
            mTextView.setVisibility(View.VISIBLE);
        }
        mTaskAdapter.swapCursor(mCursor);
    }
    // [END] get tasks




    // [START] OnClickListener
    @Override
    public void onClickTask(long id) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, id);
        startActivityForResult(intent, NAV_TO_TASK_DETAIL);
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
            showSnackBar(mRecyclerView, R.string.main_activity_task_marked_as_done);
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

        IntentServiceTasks.startTaskWidgetUpdate(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(sTaskContentObserver != null) {
            sTaskContentObserver.unregister();
        }

        if(mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public void onChange() {
        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_MAIN, mBundle, mLoaderTaskGetTasks);

        // [START] dismiss notification after a database modification
        Context context = this;
        int requestCode = TaskNotification.ACTION_DISMISS_NOTIFICATION_PENDING_INTENT_ID;
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;

        Intent dismissNotificationIntent = new Intent(context, TaskIntentService.class);
        dismissNotificationIntent.setAction(IntentServiceTasks.ACTION_DISMISS_NOTIFICATION);

        PendingIntent pendingIntent = PendingIntent.getService(
                context, requestCode, dismissNotificationIntent, flag);

        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Timber.e(e.getMessage());
        }
        // [END] dismiss notification after a database modification
    }
    // [END] ContentObserver



    // [START] Save instance state
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_INSTANCE_STATE_LIST_POSITION,
                mRecyclerView.getLayoutManager()
                        .onSaveInstanceState());
    }
    // [END] Save instance state

    // [START] Show snack bar
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;
        switch (requestCode) {
            case NAV_TO_TASK_DETAIL:
            case NAV_TO_TASK_EDITOR:
                showSnackBar(mRecyclerView, data);
                break;
            default:
                break;
        }
    }
    // [END] Show snack bar
}

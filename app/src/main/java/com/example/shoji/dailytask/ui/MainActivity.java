package com.example.shoji.dailytask.ui;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
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
import android.text.TextUtils;
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
import com.example.shoji.dailytask.notification.IntentServiceTasks;
import com.example.shoji.dailytask.notification.TaskIntentService;
import com.example.shoji.dailytask.notification.TaskNotification;
import com.example.shoji.dailytask.notification.TaskReminderUtilities;
import com.example.shoji.dailytask.provider.TaskContentObserver;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

import timber.log.Timber;

public class MainActivity extends AppCompatActivityEx
                          implements LoaderTaskGetTasks.OnTaskGetTasksListener,
                                     TaskAdapter.OnClickListener,
                                     TaskContentObserver.OnChangeListener,
                                     LoaderTaskSetConcludedById.OnTaskSetStateListener {
    private static boolean sPlantedTimberTree;
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

    // [START] shared preference onChange listener
    OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;
    // [END]  shared preference onChange listener

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

        // [START] Start today's day notification reminder
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setupTaskReminderNotification(sharedPreferences);
        // [END] Start today's day notification reminder

        // [START] shared preference onChange listener
        mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener();
        sharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        // [END] shared preference onChange listener
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
//        Context context = this;
//        int requestCode = TaskNotification.ACTION_TASK_REMINDER_PENDING_INTENT_ID;
//        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
//
//        Intent taskReminderIntent = new Intent(context, TaskIntentService.class);
//        taskReminderIntent.setAction(IntentServiceTasks.ACTION_TASK_REMINDER);
//
//        PendingIntent pendingIntent = PendingIntent.getService(
//                context, requestCode, taskReminderIntent, flag);
//
//        try {
//            pendingIntent.send();
//        } catch (PendingIntent.CanceledException e) {
//            Timber.e(e.getMessage());
//        }
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

        // [START] shared preference onChange listener
        mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        // [END] shared preference onChange listener
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


    // [START] shared preference onChange listener
    private void setupTaskReminderNotification(SharedPreferences sharedPreferences) {
        Context context = this;
        String key = getString(R.string.pref_daily_notification_key);
        boolean defValue = getResources().getBoolean(R.bool.pref_daily_notification_default_value);
        boolean enabled = sharedPreferences.getBoolean(key, defValue);

        if(enabled) {
            // [START] Start today's day notification reminder
            Timber.d("Notifications are enabled, so run service to show them");
            TaskReminderUtilities.scheduleTaskNotificationReminder(context);
            // [END] Start today's day notification reminder
        }
        else {
            // [START] Stop today's day notification reminder
            Timber.d("Notifications are disabled, so cancel the service that show them");
            TaskReminderUtilities.unscheduleTaskNotificationReminder(context);
            // [END] Stop today's day notification reminder
        }

    }

    private class OnSharedPreferenceChangeListener
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(TextUtils.equals(key, getString(R.string.pref_daily_notification_key))) {
                setupTaskReminderNotification(sharedPreferences);
            }

        }
    }
    // [END] shared preference onChange listener

    // [START] Save instance state
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_INSTANCE_STATE_LIST_POSITION,
                mRecyclerView.getLayoutManager()
                        .onSaveInstanceState());
    }
    // [END] Save instance state

}

package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.background.LoaderIds;
import com.example.shoji.dailytask.background.LoaderTaskDeleteById;
import com.example.shoji.dailytask.background.LoaderTaskGetById;
import com.example.shoji.dailytask.background.LoaderTaskSetConcludedById;
import com.example.shoji.dailytask.background.LoaderUtils;
import com.example.shoji.dailytask.provider.TaskContentObserver;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.utils.TimeUtils;

import timber.log.Timber;

public class TaskDetailActivity extends AppCompatActivityEx
                                implements TaskContentObserver.OnChangeListener,
                                           LoaderTaskDeleteById.OnTaskDeletedListener,
                                           LoaderTaskGetById.OnTaskGetByIdListener {


    public static final String EXTRA_TASK_ID = "extra-task-id";

    private long mTaskId;
    private Context mContext;

    // [START] get task by id
    private Bundle mBundle;
    LoaderTaskGetById.OnTaskGetByIdListener mListener;
    LoaderTaskGetById mLoaderTaskGetById;
    // [END] get task by id

    private ProgressBar mProgressBar;
    private TextView mTaskTitle;
    private TextView mTaskContents;
    private FloatingActionButton mFab;

    private Cursor mCursor;
    private static TaskContentObserver sTaskContentObserver;

    // [START] Check from which screen we came from
    private static final int DETAIL_FROM_INVALID = -1;
    private static final int DETAIL_FROM_MAIN = 0;
    private static final int DETAIL_FROM_HISTORY = 1;
    private int mDetailFrom = DETAIL_FROM_INVALID;
    // [END] Check from which screen we came from

    // [START] show pretty priority field
    private Button mPriorityButton;
    // [END] show pretty priority field

    private static final int NAV_TO_TASK_EDITOR = 450;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        mContext = this;

        // Set title in action bar
        getSupportActionBar().setTitle(getString(R.string.task_details_activity_title));

        // [START] Check from which screen we came from
        String classname = null;
        if(getCallingActivity() != null)
            classname = getCallingActivity().getClassName();

        if(TextUtils.equals(classname, MainActivity.class.getName())) {
            mDetailFrom = DETAIL_FROM_MAIN;
        }
        else if (TextUtils.equals(classname, TaskHistoryActivity.class.getName())) {
            mDetailFrom = DETAIL_FROM_HISTORY;
        }
        else
            mDetailFrom = DETAIL_FROM_INVALID;
        Timber.d("[MENU] Defined mDetailFrom: %d", mDetailFrom);
        // [END] Check from which screen we came from

        mProgressBar = findViewById(R.id.progressbar);
        mTaskTitle = findViewById(R.id.task_title);
        mTaskContents = findViewById(R.id.task_contents);

        // [START] need valid intent to proceed
        mTaskId = getIdFromIntent();
        Timber.d("Got ID: %d", mTaskId);
        if(mTaskId == TaskContract.INVALID_ID)
            return;
        // [END] need valid intent to proceed

        // [START] use FAB to mark test as done
        mFab = findViewById(R.id.fab);
        if(mDetailFrom == DETAIL_FROM_MAIN) {
            mFab.setVisibility(View.VISIBLE);
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    markTaskAsDone();
                }
            });
        }
        else {
            mFab.setVisibility(View.GONE);
        }
        // [END] use FAB to mark test as done

        // [START] get task by id
        mBundle = new Bundle();
        mBundle.putLong(LoaderTaskGetById.EXTRA_TASK_ID, mTaskId);
        mListener = this;
        mLoaderTaskGetById = new LoaderTaskGetById(mListener);
        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_DETAIL, mBundle, mLoaderTaskGetById);
        // [END] get task by id

        // [START] ContentObserver
        TaskContentObserver.OnChangeListener onChangeListener = this;
        sTaskContentObserver = new TaskContentObserver(getContentResolver(), onChangeListener);
        // [END] ContentObserver


        // [START] show pretty priority field
        mPriorityButton = findViewById(R.id.priorityButton);
        // [END] show pretty priority field
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



    // [START] mark task as done
    private void markTaskAsDone() {
        // [START] temporary unregister content observer
        if(sTaskContentObserver != null) {
            sTaskContentObserver.unregister();
        }
        // [END] temporary unregister content observer

        // [START] mark test as done
        Bundle args = new Bundle();
        args.putLong(LoaderTaskSetConcludedById.EXTRA_TASK_ID, mTaskId);
        args.putLong(LoaderTaskSetConcludedById.EXTRA_TASK_CONCLUDED_STATE, TaskContract.CONCLUDED);

        LoaderTaskSetConcludedById.OnTaskSetStateListener listener = new OnTaskSetStateListener();
        LoaderTaskSetConcludedById loaderTaskSetConcludedById = new LoaderTaskSetConcludedById(listener);

        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_UPDATE_MAIN, args, loaderTaskSetConcludedById);
        // [END] mark test as done
    }
    // [END] mark task as done




    // [START] Toolbar - inflate and item selected
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return customizeMenu(menu);
    }
    // [START] Check from which screen we came from
    private boolean customizeMenu(Menu menu) {
        Timber.d("[MENU] start -- onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.activity_task_detail, menu);

        for (int i = 0; i < menu.size(); i++) {
            Timber.d("menuTitle %s", menu.getItem(i).getTitle());
            int actionId = menu.getItem(i).getItemId();

            if(mDetailFrom == DETAIL_FROM_MAIN) {
                if(actionId == R.id.action_restore) {
                    menu.removeItem(i);
                    Timber.d("Removed restore");
                }
            }
            else if(mDetailFrom == DETAIL_FROM_HISTORY) {
                if(actionId == R.id.action_edit) {
                    menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                }
                else if(actionId == R.id.action_restore) {
                    menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                }
            }
        }
        Timber.d("[MENU] end -- onCreateOptionsMenu");
        return true;
    }
    // [END] Check from which screen we came from

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_delete) {
            showDeleteDialog(item);
            return true;
        }
        else if(id == R.id.action_edit) {
            Intent intent = new Intent(mContext, TaskEditorActivity.class);
            intent.putExtra(TaskEditorActivity.EXTRA_TASK_ID, mTaskId);
            startActivityForResult(intent, NAV_TO_TASK_EDITOR);
            return true;
        }
        else if(id == R.id.action_restore) {
            restoreTask();
        }
        return super.onOptionsItemSelected(item);
    }

    private void restoreTask() {
        if(mDetailFrom != DETAIL_FROM_HISTORY)
            return;
        // [START] temporary unregister content observer
        if(sTaskContentObserver != null) {
            sTaskContentObserver.unregister();
        }
        // [END] temporary unregister content observer
        // [START] unmark test as done
        Bundle args = new Bundle();
        args.putLong(LoaderTaskSetConcludedById.EXTRA_TASK_ID, mTaskId);
        args.putLong(LoaderTaskSetConcludedById.EXTRA_TASK_CONCLUDED_STATE, TaskContract.NOT_CONCLUDED);

        LoaderTaskSetConcludedById.OnTaskSetStateListener listener = new OnTaskSetStateListener();
        LoaderTaskSetConcludedById loaderTaskSetConcludedById = new LoaderTaskSetConcludedById(listener);

        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_UPDATE_MAIN, args, loaderTaskSetConcludedById);
        // [END] unmark test as done
    }
    // [END] Toolbar - inflate and item selected

    // [START] use FAB to delete the task
    private void showDeleteDialog(final MenuItem item) {
        final LoaderTaskDeleteById.OnTaskDeletedListener listener = this;
        final LoaderTaskDeleteById loaderTaskDeleteById = new LoaderTaskDeleteById(listener);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        item.setEnabled(false);
                        // [START] Delete this task in a Loader
                        Bundle args = new Bundle();
                        args.putLong(LoaderTaskDeleteById.EXTRA_TASK_ID, mTaskId);
                        LoaderUtils.initLoader(mContext,
                                               LoaderIds.LOADER_ID_GET_TASKS_DELETE,
                                               args,
                                               getSupportLoaderManager(),
                                               loaderTaskDeleteById);
                        // [END] Delete this task in a Loader
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Do nothing
                        break;
                    default:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.task_details_delete_task_prompt)
                .setPositiveButton(R.string.task_details_delete_task_positive, dialogClickListener)
                .setNegativeButton(R.string.task_details_delete_task_negative, dialogClickListener)
                .show();
    }
    // [END] use FAB to delete the task

    // [START] get task by id
    @Override
    public void onStartLoading(Context context) {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadFinished(Cursor cursor) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mCursor = cursor;

        if(mCursor == null || mCursor.getCount() != 1)
            return;

        mCursor.moveToPosition(0);

        // [START] fill the view with data from cursor
        // set title
        int columnIndex = mCursor.getColumnIndex(TaskContract.COLUMN_TITLE);
        String title = mCursor.getString(columnIndex);
        mTaskTitle.setText(title);

        // set contents
        StringBuffer sb = new StringBuffer();
        columnIndex = mCursor.getColumnIndex(TaskContract.COLUMN_DESCRIPTION);
        String description = mCursor.getString(columnIndex);
        if(description.length() > 0)
            sb.append(description);

        // [START] priority: from value to label
        columnIndex = mCursor.getColumnIndex(TaskContract.COLUMN_PRIORITY);

        int priorityInt = mCursor.getInt(columnIndex);
        Resources resources = getResources();
        int index = -1;

        int[] values = resources.getIntArray(R.array.priority_value_array);
        for(int i= 0; i < values.length; ++i) {
            if(values[i] == priorityInt) {
                // [START] show pretty priority field
                index = i;
                // [END] show pretty priority field
                break;
            }
        }
        // [START] show pretty priority field
        if(index != -1) {
            int[] colors = resources.getIntArray(R.array.priority_color_array);
            String[] labels = resources.getStringArray(R.array.priority_label_array);

            mPriorityButton.setBackgroundColor(colors[index]);
            mPriorityButton.setText(labels[index]);
        }
        // [END] show pretty priority field
        // [END] priority: from value to label


        // [START} convert time in millis to local time
        if(mDetailFrom == DETAIL_FROM_HISTORY) {
            index = mCursor.getColumnIndex(TaskContract.COLUMN_CONCLUDED_DATE);
            long modification_date = mCursor.getLong(index);

            String dateStr = TimeUtils.timeInMillisToFormattedString(mContext, modification_date);

            // break one line if there is text
            if(sb.length() != 0) {
                sb.append("\n");
            }

            sb.append("\n")
                .append(getString(R.string.task_details_modified_date_text, dateStr));
        }
        // [END} convert time in millis to local time
        if(sb.length() == 0) {
            mTaskContents.setVisibility(View.GONE);
        } else {
            mTaskContents.setVisibility(View.VISIBLE);
            mTaskContents.setText(sb.toString());
        }

        mCursor.close();
        // [END] fill the view with data from cursor
    }
    // [END] get task by id




    // [START] Detail screen buttons
    private class OnTaskSetStateListener implements LoaderTaskSetConcludedById.OnTaskSetStateListener {
        @Override
        public void onTaskSetState(Integer integer) {
            int res_id = 0;
            if(mDetailFrom == DETAIL_FROM_MAIN) {
                if (integer != null && integer == 1) {
                    res_id = R.string.task_details_task_marked_as_done;
                }
            }
            else if(mDetailFrom == DETAIL_FROM_HISTORY) {
                if (integer != null && integer == 1) {
                    res_id = R.string.task_details_task_unmarked_as_done;
                }
            }

            if( res_id != 0) {
                setResultOk(res_id);
                finish();
            }
            else {
                // [START] temporary unregister content observer - restore
                if(sTaskContentObserver != null) {
                    sTaskContentObserver.register();
                }
                // [END] temporary unregister content observer - restore
            }
        }
    }
    // [END] Detail screen buttons



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
        initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_DETAIL, mBundle, mLoaderTaskGetById);
    }
    // [END] ContentObserver




    // [START] Delete this task in a Loader
    @Override
    public void onTaskDeleted(Integer integer) {
        if(integer == null || integer != 1) {
            Timber.d("Deleted task failed!");
        }
        else {
            Timber.d("Deleted task!");
            setResultOk(R.string.task_details_delete_task_success);
            finish();
        }
    }
    // [END] Delete this task in a Loader



    // [START] Show snack bar
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;
        switch (requestCode) {
            case NAV_TO_TASK_EDITOR:
                showSnackBar(mTaskContents, data);
                break;
            default:
                break;
        }
    }
    // [END] Show snack bar
}
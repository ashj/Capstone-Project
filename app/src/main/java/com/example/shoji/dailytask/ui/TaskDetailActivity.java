package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.ImageViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
    private static final int DETAIL_FROM_WIDGET = 2;
    private int mDetailFrom = DETAIL_FROM_WIDGET;
    // [END] Check from which screen we came from

    // [START] show tinted check icon
    private ImageView mCheckImage;
    private TextView mFinishedDateTextView;
    private View mSeparatorView;
    private ConstraintLayout mFinishedDateConstraintLayout;
    // [END] show tinted check icon

    private final static String SAVE_INSTANCE_STATE_NESTEDSCROLLVIEW_POSITION = "list-scrollviewposition";

    private NestedScrollView mNestedScrollView;
    private Bundle mSavedInstanceState;

    private static final int NAV_TO_TASK_EDITOR = 450;

    private static final int ACTION_RESTORE_TASK_ID = 800;

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
        else if(mDetailFrom == DETAIL_FROM_WIDGET) {
            mDetailFrom = DETAIL_FROM_MAIN; //behave the same
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


        // [START] show tinted check icon
        mCheckImage = findViewById(R.id.checked_image);
        mFinishedDateTextView = findViewById(R.id.task_modified_date_text_view);
        mSeparatorView = findViewById(R.id.separator);
        mFinishedDateConstraintLayout = findViewById(R.id.history_finished_date);
        // [END] show tinted check icon


        // [START] Save instance state - restore
        mSavedInstanceState = savedInstanceState;
        mNestedScrollView = findViewById(R.id.nested_scroll_view);
        // [END] Save instance state - restore
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

        // [START] add move to to-do list option, put some options in the text menu
        if(mDetailFrom == DETAIL_FROM_HISTORY) {
            menu.add(Menu.NONE, ACTION_RESTORE_TASK_ID, menu.size(), R.string.task_editor_menu_move_to_todo);

            for (int i = 0; i < menu.size(); i++) {
                Timber.d("menuTitle %s", menu.getItem(i).getTitle());
                int actionId = menu.getItem(i).getItemId();

                if (actionId == R.id.action_edit) {
                    menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                } else if (actionId == ACTION_RESTORE_TASK_ID) {
                    menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                }
            }
        }
        // [END] add move to to-do list option, put some options in the text menu
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
        else if(id == ACTION_RESTORE_TASK_ID) {
            restoreTask();
        }
        else if(id == android.R.id.home) {
            if(mDetailFrom == DETAIL_FROM_MAIN) {
                NavUtils.navigateUpFromSameTask(this);
            }
            else {
                finish();
            }
            return true;
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

        if(description != null && !description.isEmpty()) {
            mTaskContents.setVisibility(View.VISIBLE);
            mTaskContents.setText(description);
        }
        else {
            mTaskContents.setVisibility(View.GONE);
        }

        // [START] show tinted check icon
        tintCheckedIconByPriority();
        // [END] show tinted check icon

        // [START} convert time in millis to local time
        if(mDetailFrom == DETAIL_FROM_HISTORY) {
            mFinishedDateConstraintLayout.setVisibility(View.VISIBLE);
            mSeparatorView.setVisibility(View.GONE);

            columnIndex = mCursor.getColumnIndex(TaskContract.COLUMN_CONCLUDED_DATE);
            long modification_date = mCursor.getLong(columnIndex);

            String dateStr = TimeUtils.timeInMillisToFormattedString(mContext, modification_date);

            mFinishedDateTextView.setText(getString(R.string.task_details_modified_date_text, dateStr));
        }
        // [END} convert time in millis to local time
        else {
            mFinishedDateConstraintLayout.setVisibility(View.GONE);
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        mCursor.close();
        // [END] fill the view with data from cursor

        // [START] Save instance state - restore
        if(mSavedInstanceState != null) {

            if(mSavedInstanceState.containsKey(SAVE_INSTANCE_STATE_NESTEDSCROLLVIEW_POSITION)) {
                final int[] position = mSavedInstanceState.getIntArray(SAVE_INSTANCE_STATE_NESTEDSCROLLVIEW_POSITION);
                if (position != null)
                    mNestedScrollView.post(new Runnable() {
                        public void run() {
                            mNestedScrollView.scrollTo(position[0], position[1]);
                        }
                    });
            }

        }
        // [END] Save instance state - restore
    }
    // [END] get task by id



    // [START] Save instance state
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putIntArray(SAVE_INSTANCE_STATE_NESTEDSCROLLVIEW_POSITION,
                new int[]{ mNestedScrollView.getScrollX(), mNestedScrollView.getScrollY()});

    }
    // [END] Save instance state



    // [START] show tinted check icon
    private void tintCheckedIconByPriority() {
        int columnIndex = mCursor.getColumnIndex(TaskContract.COLUMN_PRIORITY);

        int priorityInt = mCursor.getInt(columnIndex);
        Resources resources = getResources();
        int index = -1;

        int[] values = resources.getIntArray(R.array.priority_value_array);
        for(int i= 0; i < values.length; ++i) {
            if(values[i] == priorityInt) {
                index = i;
                break;
            }
        }

        if(index != -1) {
            int[] colors = resources.getIntArray(R.array.priority_color_array);
            String[] labels = resources.getStringArray(R.array.priority_label_array);

            // [START] tint check icon
            ImageViewCompat.setImageTintList(mCheckImage,
                    ColorStateList.valueOf(colors[index]));
            // [END] tint check icon
            mSeparatorView.setBackgroundColor(colors[index]);
        }
        // [END] show pretty priority field
    }
    // [END] show tinted check icon


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
package com.example.shoji.dailytask.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.background.LoaderIds;
import com.example.shoji.dailytask.background.LoaderTaskGetById;
import com.example.shoji.dailytask.notification.IntentServiceTasks;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

import java.util.ArrayList;

import timber.log.Timber;


public class TaskEditorActivity extends AppCompatActivityEx
    implements View.OnClickListener,
               LoaderTaskGetById.OnTaskGetByIdListener{
    public static final String EXTRA_TASK_ID = "extra-task-id";

    private long mTaskId;
    private Context mContext;

    // [START] get task by id
    private Bundle mBundle;
    LoaderTaskGetById.OnTaskGetByIdListener mListener;
    LoaderTaskGetById mLoaderTaskGetById;
    // [END] get task by id

    private ProgressBar mProgressBar;
    private EditText mTitleEditText;
    private EditText mDescriptionEditTask;
    private RadioGroup mRadioGroup;
    private ArrayList<RadioButton> mRadioButtons;
    private Button mButton;

    private int mEditorMode;
    private int mEditorFrom;

    private static final int FORM_ERROR_NO_ERROR = 0;
    private static final int FORM_ERROR_INVALID_TITLE = 1;
    private static final int FORM_ERROR_INVALID_DESCRIPTION = 2;

    private static final int EDITOR_MODE_ADD = 10;
    private static final int EDITOR_MODE_EDIT = 11;

    private static final int EDITOR_FROM_MAIN = 20;
    private static final int EDITOR_FROM_HISTORY = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        // [START] Check if it is a task to edit or a new one
        mTaskId = getIdFromIntent();
        if(mTaskId == TaskContract.INVALID_ID) {
            mEditorMode = EDITOR_MODE_ADD;
        }
        else {
            mEditorMode = EDITOR_MODE_EDIT;
        }
        // [END] Check if it is a task to edit or a new one

        mProgressBar = findViewById(R.id.progressbar);
        mTitleEditText = findViewById(R.id.task_title_edit_text);
        mDescriptionEditTask = findViewById(R.id.task_description_edit_text);
        mRadioGroup = findViewById(R.id.radio_group);
        mButton = findViewById(R.id.button);

        mContext = this;

        createRadioGroup();

        mButton.setOnClickListener(this);

        // [START] Check if it is a task to edit or a new one
        if(mEditorMode == EDITOR_MODE_ADD) {
            // Set title in action bar
            getSupportActionBar().setTitle(getString(R.string.task_editor_activity_title_add));
            mButton.setText(R.string.button_task_add);
        }
        else if (mEditorMode == EDITOR_MODE_EDIT){
            // Set title in action bar
            getSupportActionBar().setTitle(getString(R.string.task_editor_activity_title_edit));
            mButton.setText(R.string.button_task_edit);

            // [START] get task by id
            mBundle = new Bundle();
            mBundle.putLong(LoaderTaskGetById.EXTRA_TASK_ID, mTaskId);
            mListener = this;
            mLoaderTaskGetById = new LoaderTaskGetById(mListener);
            initTaskLoader(LoaderIds.LOADER_ID_GET_TASKS_EDIT, mBundle, mLoaderTaskGetById);
            // [END] get task by id
        }
        // [END] Check if it is a task to edit or a new one

    }


    // [START] Set task priority
    private void createRadioGroup() {
        Context context = this;

        int[] values = getResources().getIntArray(R.array.priority_value_array);
        int defaultValue = getResources().getInteger(R.integer.priority_value_default);
        mRadioButtons = new ArrayList<>();
        mRadioButtons.add((RadioButton) findViewById(R.id.radioButtonLow));
        mRadioButtons.add((RadioButton) findViewById(R.id.radioButtonNormal));
        mRadioButtons.add((RadioButton) findViewById(R.id.radioButtonHigh));

        for(int i=0; i< mRadioButtons.size(); ++i) {
            RadioButton button = mRadioButtons.get(i);
            button.setId(values[i]);
            if(defaultValue == values[i])
                button.setChecked(true);
         }
    }
    // [END] Set task priority



    // [START] Clicked to save or update task
    @Override
    public void onClick(View view) {
        int validation = validateForm();
        if(validation == FORM_ERROR_NO_ERROR) {
            view.setClickable(false);
            performActionIntoDatabase();
        }
        else if(validation == FORM_ERROR_INVALID_TITLE) {
            Toast.makeText(this, R.string.validate_task_error_title, Toast.LENGTH_SHORT).show();
        }
        else if(validation == FORM_ERROR_INVALID_TITLE) {
            Toast.makeText(this, R.string.validate_task_error_description, Toast.LENGTH_SHORT).show();
        }
    }

    private int validateForm() {
        int retValue = FORM_ERROR_NO_ERROR;
        String title = mTitleEditText.getText().toString();
        if(title == null || title.isEmpty()) {
            retValue = FORM_ERROR_INVALID_TITLE;
        }
        String description = mDescriptionEditTask.getText().toString();
        if(description == null) {
            retValue = FORM_ERROR_INVALID_DESCRIPTION;
        }
        return retValue;
    }
    // [END] Clicked to save or update task


    private void performActionIntoDatabase() {
        insertIntoDb();
    }

    private ContentValues createContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(TaskContract.COLUMN_TITLE, mTitleEditText.getText().toString());
        cv.put(TaskContract.COLUMN_DESCRIPTION, mDescriptionEditTask.getText().toString());
        cv.put(TaskContract.COLUMN_PRIORITY, mRadioGroup.getCheckedRadioButtonId());
        if(mEditorMode == EDITOR_MODE_ADD) {
            cv.put(TaskContract.COLUMN_IS_CONCLUDED, TaskContract.NOT_CONCLUDED);
            cv.put(TaskContract.COLUMN_CONCLUDED_DATE, TaskContract.NOT_CONCLUDED);
        }

        return cv;
    }



    // [START] use AsyncTask to add a task into the database
    private void insertIntoDb() {
        TaskDatabaseAsyncTask asyncTask = new TaskDatabaseAsyncTask();
        asyncTask.execute(createContentValues());
    }

    private class TaskDatabaseAsyncTask extends AsyncTask<ContentValues,
                                                         Void,
                                                         Pair<Integer, Uri>> {

        @Override
        protected Pair<Integer, Uri> doInBackground(ContentValues... cvs) {
            ContentValues cv = cvs[0];

            Uri uri = null;
            Integer integer = null;
            if(mEditorMode == EDITOR_MODE_ADD) {
                uri = mContext.getContentResolver().insert(TaskProvider.Tasks.CONTENT_URI, cv);

                //[START] update the widget
                if(uri != null)
                    IntentServiceTasks.startTaskWidgetUpdate(mContext);
                //[END] update the widget
            }
            else {
                String selection = TaskContract._ID + " IS " + mTaskId;
                int rows = mContext.getContentResolver().update(TaskProvider.Tasks.CONTENT_URI,
                        cv, selection, null);

                //[START] update the widget
                if(rows > 0)
                    IntentServiceTasks.startTaskWidgetUpdate(mContext);
                //[END] update the widget

                integer = Integer.valueOf(rows);
            }
            return Pair.create(integer, uri);
        }

        @Override
        protected void onPostExecute(Pair<Integer, Uri> pair) {
            super.onPostExecute(pair);

            if(mEditorMode == EDITOR_MODE_ADD) {
                Uri uri = pair.second;

                if (uri == null) {
                    Timber.d("Failed to insert new task");
                } else {
                    Timber.d("Inserted new task!");
                    Toast.makeText(mContext, R.string.insert_task_success, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else {
                Integer integer = pair.first;
                if(integer == null || integer != 1) {
                    Timber.d("Failed to update task");
                }
                else {
                    Timber.d("Updated task!");
                    Toast.makeText(mContext, R.string.update_task_success, Toast.LENGTH_SHORT).show();
                    finish();
                }

            }
        }
    }
    // [END] use AsyncTask to add a task into the database



    // [START] Check if it is a task to edit or a new one
    private long getIdFromIntent() {
        long id = TaskContract.INVALID_ID;

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(EXTRA_TASK_ID))
            id = intent.getLongExtra(EXTRA_TASK_ID, TaskContract.INVALID_ID);

        return id;
    }
    // [END] Check if it is a task to edit or a new one



    // [START] get task by id
    @Override
    public void onStartLoading(Context context) {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadFinished(Cursor cursor) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mButton.setClickable(true);

        if(cursor == null || cursor.getCount() != 1)
            return;

        cursor.moveToPosition(0);

        int index = cursor.getColumnIndex(TaskContract.COLUMN_TITLE);
        String title = cursor.getString(index);

        index = cursor.getColumnIndex(TaskContract.COLUMN_DESCRIPTION);
        String description = cursor.getString(index);

        index = cursor.getColumnIndex(TaskContract.COLUMN_PRIORITY);
        int priority = cursor.getInt(index);

        index = cursor.getColumnIndex(TaskContract.COLUMN_IS_CONCLUDED);
        int concluded = cursor.getInt(index);

        // [START] Check from which screen we came from
        if(concluded == TaskContract.NOT_CONCLUDED) {
            mEditorFrom = EDITOR_FROM_MAIN;
        }
        else {
            mEditorFrom = EDITOR_FROM_HISTORY;
        }
        // [END] Check from which screen we came from

        cursor.close();

        mTitleEditText.setText(title);
        mDescriptionEditTask.setText(description);
        for(RadioButton button : mRadioButtons) {
            if(button.getId() == priority) {
                button.setChecked(true);
                break;
            }

        }
    }
    // [END] get task by id
}

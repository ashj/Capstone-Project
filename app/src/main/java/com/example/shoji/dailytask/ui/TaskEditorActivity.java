package com.example.shoji.dailytask.ui;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

import timber.log.Timber;


public class TaskEditorActivity extends AppCompatActivity
    implements View.OnClickListener {

    private Context mContext;
    private EditText mTitleEditText;
    private EditText mDescriptionEditTask;
    private RadioGroup mRadioGroup;
    private Button mButton;

    private static final int FORM_ERROR_NO_ERROR = 0;
    private static final int FORM_ERROR_INVALID_TITLE = 1;
    private static final int FORM_ERROR_INVALID_DESCRIPTION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);


        mTitleEditText = findViewById(R.id.task_title_edit_text);
        mDescriptionEditTask = findViewById(R.id.task_description_edit_text);
        mRadioGroup = findViewById(R.id.radio_group);
        mButton = findViewById(R.id.button);

        mContext = this;

        createRadioGroup();

        mButton.setOnClickListener(this);

    }

    private void createRadioGroup() {
        Context context = this;
        String[] labels = getResources().getStringArray(R.array.priority_label_array);
        int[] values = getResources().getIntArray(R.array.priority_value_array);
        int defaultValue = getResources().getInteger(R.integer.priority_value_default);
        int[] colors = getResources().getIntArray(R.array.priority_color_array);
        for(int i=0; i< labels.length; ++i) {
            RadioButton button = new RadioButton(context);
            button.setText(labels[i]);
            button.setId(values[i]);
            button.setBackgroundColor(colors[i]);


            if(defaultValue == values[i])
                button.setChecked(true);

            mRadioGroup.addView(button);
        }
    }

    @Override
    public void onClick(View view) {
        int validation = validateForm();
        if(validation == FORM_ERROR_NO_ERROR) {
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

    private void performActionIntoDatabase() {
        insertIntoDb();
    }

    private ContentValues createContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(TaskContract.COLUMN_TITLE, mTitleEditText.getText().toString());
        cv.put(TaskContract.COLUMN_DESCRIPTION, mDescriptionEditTask.getText().toString());
        cv.put(TaskContract.COLUMN_PRIORITY, mRadioGroup.getCheckedRadioButtonId());
        //TODO : for ADD, values are 0, to do for other modes
        cv.put(TaskContract.COLUMN_IS_CONCLUDED, TaskContract.NOT_CONCLUDED);
        cv.put(TaskContract.COLUMN_CONCLUDED_DATE, TaskContract.NOT_CONCLUDED);

        return cv;
    }

    private void insertIntoDb() {
        // [START] use AsyncTask to add a task into the database
        Pair<Context, ContentValues> pair = Pair.create(mContext, createContentValues());
        TaskDatabaseAsyncTask asyncTask = new TaskDatabaseAsyncTask();
        asyncTask.execute(pair);
        // [END] use AsyncTask to add a task into the database

    }

    // [START] use AsyncTask to add a task into the database
    private class TaskDatabaseAsyncTask extends AsyncTask<Pair<Context, ContentValues>,
                                                         Void,
                                                         Pair<Context, Uri>> {

        @Override
        protected Pair<Context, Uri> doInBackground(Pair<Context, ContentValues>[] pairs) {
            Pair pair = pairs[0];

            Context context = (Context) pair.first;
            ContentValues cv = (ContentValues) pair.second;

            Uri uri = mContext.getContentResolver().insert(TaskProvider.Tasks.CONTENT_URI, cv);

            return Pair.create(context, uri);
        }

        @Override
        protected void onPostExecute(Pair<Context, Uri> pair) {
            super.onPostExecute(pair);

            //Context context = pair.first;
            Uri uri = (Uri) pair.second;

            if (uri == null) {
                Timber.d("Failed to insert new task");
            } else {
                Timber.d("Inserted new task!");
                Toast.makeText(mContext, R.string.insert_task_success, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    // [END] use AsyncTask to add a task into the database
}

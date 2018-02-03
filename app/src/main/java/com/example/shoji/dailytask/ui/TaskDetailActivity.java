package com.example.shoji.dailytask.ui;

import android.app.IntentService;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.shoji.dailytask.R;

import timber.log.Timber;

public class TaskDetailActivity extends AppCompatActivity {
    private static final long INVALID_ID = -1;
    public static final String EXTRA_TASK_ID = "extra-task-id";

    private long mTaskId;
    private TextView mTaskTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        mTaskTitleTextView = findViewById(R.id.task_title_text_view);

        // [START] need valid intent to proceed
        mTaskId = getIdFromIntent();
        Timber.d("Got ID: %d", mTaskId);
        if(mTaskId == INVALID_ID)
            return;
        // [END] need valid intent to proceed


    }

    // [START] need valid intent to proceed
    private long getIdFromIntent() {
        long id = INVALID_ID;

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(EXTRA_TASK_ID))
            id = intent.getLongExtra(EXTRA_TASK_ID, INVALID_ID);

        return id;
    }
    // [END] need valid intent to proceed
}
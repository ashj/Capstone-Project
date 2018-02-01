package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.shoji.dailytask.BuildConfig;
import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.provider.TaskContract;
import com.example.shoji.dailytask.provider.TaskProvider;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            if(BuildConfig.DEBUG)
                Timber.plant(new Timber.DebugTree());
            Timber.d("Logging With Timber");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFab = findViewById(R.id.fab);

        final Context context = this;
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskEditorActivity.class);
                startActivity(intent);
            }
        });

        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        Cursor cursor = getContentResolver().query(TaskProvider.Tasks.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.COLUMN_TITLE);
            String value = cursor.getString(idx);
            Timber.d(">>>>>>>>>>>>>>>>>>>> %s", value);
            idx = cursor.getColumnIndex(TaskContract.COLUMN_PRIORITY);
            int priority = cursor.getInt(idx);
            Timber.d(">>>>>>>>>>>>>>>>>>>> %d", priority);
        }
    }
}

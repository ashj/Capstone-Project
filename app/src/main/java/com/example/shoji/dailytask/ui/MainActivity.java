package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.shoji.dailytask.BuildConfig;
import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.adapter.TaskAdapter;
import com.example.shoji.dailytask.background.LoaderCallBacksListenersInterface;
import com.example.shoji.dailytask.background.Utils;
import com.example.shoji.dailytask.provider.TaskProvider;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity
                          implements LoaderCallBacksListenersInterface<Cursor> {

    private TaskAdapter mTaskAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;


    private Cursor mCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            if(BuildConfig.DEBUG)
                Timber.plant(new Timber.DebugTree());
            Timber.d("Logging With Timber");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager lym =
                new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(lym);

        mFab = findViewById(R.id.fab);

        final Context context = this;
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskEditorActivity.class);
                startActivity(intent);
            }
        });

        mTaskAdapter = new TaskAdapter(context);
        mTaskAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mTaskAdapter);


        //TODO [query in background]
        LoaderManager loaderManager = getSupportLoaderManager();
        LoaderCallBacksListenersInterface<Cursor> loaderCallBacksListenersInterface = this;
        Utils.queryTasks(context, loaderManager, loaderCallBacksListenersInterface);



    }

    // [START] implements LoaderCallBacksListenersInterface<Cursor>
    @Override
    public void onStartLoading(Context context) { }

    @Override
    public Cursor onLoadInBackground(Context context, Bundle args) {        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        Cursor cursor = getContentResolver().query(TaskProvider.Tasks.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);
        return cursor;
    }

    @Override
    public void onLoadFinished(Context context, Cursor cursor) {
        mCursor = cursor;
        mTaskAdapter.swapCursor(mCursor);
    }
    // [END] implements LoaderCallBacksListenersInterface<Cursor>
}

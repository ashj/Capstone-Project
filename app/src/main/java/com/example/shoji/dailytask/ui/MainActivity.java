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
import android.widget.Toast;

import com.example.shoji.dailytask.BuildConfig;
import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.adapter.TaskAdapter;
import com.example.shoji.dailytask.background.LoaderCallBacksListenersInterface;
import com.example.shoji.dailytask.background.Utils;
import com.example.shoji.dailytask.provider.TaskContentObserver;
import com.example.shoji.dailytask.provider.TaskProvider;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity
                          implements LoaderCallBacksListenersInterface<Cursor>,
                                     TaskAdapter.OnClickListener,
                                     TaskContentObserver.OnChangeListener {

    private TaskAdapter mTaskAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;

    private static TaskContentObserver sTaskContentObserver;


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

        final Context context = this;

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
        doQueryTasks();
        // [END] implements LoaderCallBacksListenersInterface<Cursor>

        // [START] ContentObserver
        TaskContentObserver.OnChangeListener onChangeListener = this;
        sTaskContentObserver = new TaskContentObserver(getContentResolver(), onChangeListener);
        // [END] ContentObserver
    }

    // [START] implements LoaderCallBacksListenersInterface<Cursor>
    private void doQueryTasks() {
        Context context = this;
        LoaderManager loaderManager = getSupportLoaderManager();
        LoaderCallBacksListenersInterface<Cursor> loaderCallBacksListenersInterface = this;
        Utils.queryTasks(context, loaderManager, loaderCallBacksListenersInterface);
    }

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

    @Override
    public void onClick(long id) {
        Toast.makeText(this, "Tapped at "+id, Toast.LENGTH_SHORT).show();

    }

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
        doQueryTasks();
    }
    // [END] ContentObserver



}

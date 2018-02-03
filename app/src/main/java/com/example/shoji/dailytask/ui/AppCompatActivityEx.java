package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.shoji.dailytask.background.LoaderCallBacksListenersInterface;
import com.example.shoji.dailytask.background.LoaderUtils;

public abstract class AppCompatActivityEx extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // [START] customize action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // [END] customize action bar
    }

    // [START] customize action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // [END] customize action bar

    // [START] implements LoaderCallBacksListenersInterface<Cursor>
    protected void initTaskLoader(int loaderId, LoaderCallBacksListenersInterface<Cursor> loaderCallBacksListenersInterface) {
        Context context = this;

        LoaderManager loaderManager = getSupportLoaderManager();
        LoaderUtils.initLoader(context, loaderId, loaderManager, loaderCallBacksListenersInterface);
    }
    // [END] implements LoaderCallBacksListenersInterface<Cursor>

}

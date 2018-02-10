package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.example.shoji.dailytask.background.LoaderCallBacksListenersInterface;
import com.example.shoji.dailytask.background.LoaderUtils;

import timber.log.Timber;

public abstract class AppCompatActivityEx extends AppCompatActivity {

    public static final String EXTRA_RES_ID = "extra-res-id";

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
    protected <T> void initTaskLoader(int loaderId,
                                           LoaderCallBacksListenersInterface<T> loaderCallBacksListenersInterface) {
        Context context = this;

        LoaderManager loaderManager = getSupportLoaderManager();
        LoaderUtils.initLoader(context, loaderId, loaderManager, loaderCallBacksListenersInterface);
    }
    protected <T> void initTaskLoader(int loaderId, Bundle args,
                                      LoaderCallBacksListenersInterface<T> loaderCallBacksListenersInterface) {
        Context context = this;

        LoaderManager loaderManager = getSupportLoaderManager();
        LoaderUtils.initLoader(context, loaderId, args, loaderManager, loaderCallBacksListenersInterface);
    }
    // [END] implements LoaderCallBacksListenersInterface<Cursor>

    // [START] Show snackbar
    protected void setResultOk(int resId) {
        Intent intent = new Intent();
        intent.putExtra(AppCompatActivityEx.EXTRA_RES_ID, resId);
        setResult(RESULT_OK, intent);
    }

    protected void showSnackBar(View view, Intent intent) {
        Timber.d("showSnackBar");
        if(intent != null) {
            Timber.d("showSnackBar #2");
            int resId = intent.getIntExtra(EXTRA_RES_ID, -1);
            if(resId != -1) {
                Timber.d("showSnackBar #3");
                showSnackBar(view, resId);
            }
        }
    }
    protected void showSnackBar(View view, int resId) {
        Timber.d("showSnackBar #4");
        showSnackBar(view, resId, Snackbar.LENGTH_SHORT);

    }
    protected void showSnackBar(View view, int resId, int length) {
        Timber.d("showSnackBar #4");
        Snackbar.make(view, resId, length).show();

    }
    // [END] Show snackbar
}

package com.example.shoji.dailytask.background;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import timber.log.Timber;

public class Utils {
    private static void initOrRestartLoader(int loaderId,
                                           Bundle args,
                                           LoaderManager loaderManager,
                                           LoaderManager.LoaderCallbacks callback) {

        if(null == loaderManager.getLoader(loaderId)) {
            loaderManager.initLoader(loaderId,
                    args, callback);
        }
        else {
            loaderManager.restartLoader(loaderId,
                    args, callback);
        }
    }
    synchronized public static void initLoader(Context context,
                                               int loaderId,
                                               Bundle args,
                                               LoaderManager loaderManager,
                                               LoaderCallBacksListenersInterface<Cursor> loaderCallBacksListener) {
        LoaderCallBacksEx<Cursor> loaderCallBacks =
                new LoaderCallBacksEx<>(context, loaderCallBacksListener);

        initOrRestartLoader(
                loaderId,
                args,
                loaderManager,
                loaderCallBacks);
    }

    synchronized public static void queryTasks(Context context,
                                               LoaderManager loaderManager,
                                               LoaderCallBacksListenersInterface<Cursor> loaderCallBacksListener) {
        int loaderId = LoaderID.LOADER_ID_QUERY_TASKS;
        Bundle args = null;
        initLoader(context, loaderId, args, loaderManager, loaderCallBacksListener);
    }

}

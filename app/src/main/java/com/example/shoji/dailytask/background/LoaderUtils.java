package com.example.shoji.dailytask.background;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

public class LoaderUtils {
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
    public static void initTaskDeleteLoader(Context context,
                                  int loaderId,
                                  Bundle args,
                                  LoaderManager loaderManager,
                                  LoaderCallBacksListenersInterface<Integer> loaderCallBacksListener) {
        LoaderCallBacksEx<Integer> loaderCallBacks =
                new LoaderCallBacksEx<>(context, loaderCallBacksListener);

        initOrRestartLoader(
                loaderId,
                args,
                loaderManager,
                loaderCallBacks);
    }
    public static void initLoader(Context context,
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

    public static void initLoader(Context context,
                                  int loaderId,
                                  LoaderManager loaderManager,
                                  LoaderCallBacksListenersInterface<Cursor> loaderCallBacksListener) {

        initLoader(context, loaderId, null, loaderManager, loaderCallBacksListener);
    }

}

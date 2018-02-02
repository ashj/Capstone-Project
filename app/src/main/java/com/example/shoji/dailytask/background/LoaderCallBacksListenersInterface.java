package com.example.shoji.dailytask.background;

import android.content.Context;
import android.os.Bundle;

public interface LoaderCallBacksListenersInterface<Result> {
    void onStartLoading(Context context);
    Result onLoadInBackground(Context context, Bundle args);
    void onLoadFinished(Context context, Result result);
}

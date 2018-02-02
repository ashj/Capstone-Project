package com.example.shoji.dailytask.provider;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class TaskContentObserver extends ContentObserver {
    private ContentResolver mContentResolver;
    private OnChangeListener mOnChangeListener;

    public interface OnChangeListener {
        void onChange();
    }

    protected TaskContentObserver(Handler handler,
                                  ContentResolver contentResolver,
                                  OnChangeListener onChangeListener) {
        super(handler);
        mContentResolver = contentResolver;
        mOnChangeListener = onChangeListener;
    }

    public TaskContentObserver(ContentResolver contentResolver, OnChangeListener onChangeListener) {
        this(new Handler(), contentResolver, onChangeListener);
    }

    public void register() {
        Uri uri = TaskProvider.Tasks.CONTENT_URI;
        boolean notifyForDescendants = false;
        ContentObserver contentObserver = this;
        mContentResolver.registerContentObserver(
                uri, notifyForDescendants, contentObserver);
    }

    public void unregister() {
        ContentObserver contentObserver = this;
        mContentResolver.unregisterContentObserver(contentObserver);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        mOnChangeListener.onChange();
    }
}
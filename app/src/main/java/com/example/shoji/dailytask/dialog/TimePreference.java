package com.example.shoji.dailytask.dialog;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.example.shoji.dailytask.R;

public class TimePreference extends DialogPreference {


    private int mTime;

    private int mDialogLayoutResId = R.layout.pref_dialog_time;

    public TimePreference(Context context) {
        // Delegate to other constructor
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        // Delegate to other constructor
        // Use the preferenceStyle as the default style
        this(context, attrs, R.attr.preferenceStyle);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        // Delegate to other constructor
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // Do custom stuff here
        // ...
        // read attributes etc.
    }

    public int getTime() {
        return mTime;
    }

    public void setTime(int time) {
        mTime = time;

        // Save to SharedPreference
        persistInt(time);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // The type of this preference is Int, so we read the default value from the attributes
        // as Int. Fallback value is set to 0.
        return a.getInt(index, 0);
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // If the value can be restored, do it. If not, use the default value.
        setTime(restorePersistedValue ?
                getPersistedInt(mTime) : (int) defaultValue);
    }

}

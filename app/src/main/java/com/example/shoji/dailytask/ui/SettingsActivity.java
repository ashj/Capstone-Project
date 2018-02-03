package com.example.shoji.dailytask.ui;

import android.os.Bundle;

import com.example.shoji.dailytask.R;

public class SettingsActivity extends AppCompatActivityEx {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set title in action bar
        getSupportActionBar().setTitle(getString(R.string.settings_activity_title));
    }
}

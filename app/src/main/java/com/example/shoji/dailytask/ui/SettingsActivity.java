package com.example.shoji.dailytask.ui;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.shoji.dailytask.R;

public class SettingsActivity extends AppCompatActivityEx {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set title in action bar
        getSupportActionBar().setTitle(getString(R.string.menu_settings_label));
    }
}

package com.example.shoji.dailytask.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.shoji.dailytask.R;


public class SettingsFragment extends PreferenceFragmentCompat
                              implements SharedPreferences.OnSharedPreferenceChangeListener,
                                         Preference.OnPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();

        // [START] update summary
        for (int i = 0; i < count; i++) {
            android.support.v7.preference.Preference p = prefScreen.getPreference(i);

            if (!(p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
        // [END] update summary

        // [START] notification by location
        bindLocationPicker();
        // [END] notification by location
    }

    // [START] notification by location
    private void bindLocationPicker() {
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        Preference preference = findPreference(getString(R.string.pref_location_open_and_set_place_key));

        // [START] enable or disable location selection screen
        String key = getString(R.string.pref_location_service_key);
        boolean defValue = getResources().getBoolean(R.bool.pref_location_service_default_value);
        boolean showNotification = sharedPreferences.getBoolean(key, defValue);

        if(!showNotification) {
            preference.setSelectable(false);
            preference.setSummary(R.string.pref_location_open_and_set_place_summaryOff);
        }

        else {
            preference.setSelectable(true);
            preference.setSummary(sharedPreferences.getString(preference.getKey(),
                                                                getString(R.string.pref_location_open_and_set_place_defaultValue)));
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // TODO open activity
                    Toast.makeText(getContext(), "Open location screen here", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), SettingsLocationActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }
        // [END] enable or disable location selection screen
    }
    // [END] notification by location



    // [START] update summary
    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // [START] enable or disable location selection screen
        if(TextUtils.equals(key, getString(R.string.pref_location_service_key))) {
            bindLocationPicker();
        }
        // [END] enable or disable location selection screen
        else {
            Preference preference = findPreference(key);
            if (null != preference) {
                if (!(preference instanceof CheckBoxPreference)) {
                    String value = sharedPreferences.getString(preference.getKey(),
                            getString(R.string.empty_string));
                    setPreferenceSummary(preference, value);
                }
            }
        }
    }
    // [END] update summary

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    // [START] Change listener
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    // [END] Change listener
}

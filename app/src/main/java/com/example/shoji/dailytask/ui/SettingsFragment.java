package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.dialog.TimePreference;
import com.example.shoji.dailytask.dialog.TimePreferenceDialogFragmentCompat;
import com.example.shoji.dailytask.location.Geofencing;
import com.example.shoji.dailytask.location.LocationUtils;
import com.example.shoji.dailytask.notification.TaskReminderUtilities;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import timber.log.Timber;


public class SettingsFragment extends PreferenceFragmentCompat
                              implements SharedPreferences.OnSharedPreferenceChangeListener,
                                         Preference.OnPreferenceChangeListener,
                                         GoogleApiClient.ConnectionCallbacks,
                                         GoogleApiClient.OnConnectionFailedListener{
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 700;
    private static final int PLACE_PICKER_REQUEST = 750;

    private FragmentActivity mActivity;
    private Context mContext;

    private GoogleApiClient mClient;
    private Geofencing mGeofencing;

    private SharedPreferences mSharedPreferences;
    private CheckBoxPreference mNotificationCheckBox;
    private CheckBoxPreference mLocationCheckBox;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        mActivity = getActivity();
        mContext = getContext();

        // [START] Google API and Geofencing
        mClient = buildGoogleApiClient();
        mGeofencing = new Geofencing(mContext, mClient);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        // [END] Google API and Geofencing

        // [START] update summary
        updateSummary();
        // [END] update summary


        // [START] notification by location
        bindLocationPicker();
        // [END] notification by location

        // [START} enable / disable location service
        mNotificationCheckBox = (CheckBoxPreference) findPreference(getString(R.string.pref_daily_notification_key));

        bindLocationCheckBox();
        mNotificationCheckBox.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                bindLocationCheckBox();
                bindLocationPicker();
                return true;
            }
        });
        // [END} enable / disable location service
    }

    // [START] setup geofencing based on notification setting
    private void configureGeofencing() {
        SharedPreferences sharedPreferences = mSharedPreferences;
        // [START] check notification setting
        String keyNotification = getString(R.string.pref_daily_notification_key);
        boolean enabledNotification = sharedPreferences.getBoolean(keyNotification,
                getResources().getBoolean(R.bool.pref_daily_notification_default_value));
        // [END] check notification setting

       boolean enabledLocation = sharedPreferences.getBoolean(getString(R.string.pref_location_service_key),
                getResources().getBoolean(R.bool.pref_location_service_default_value));

        if (enabledLocation && enabledNotification) {
            Timber.d("setupTaskReminderNotification II: Situation #1: noti:ON, locServ:ON");
            mGeofencing.registerAllGeofences();
        }
        else {
            Timber.d("setupTaskReminderNotification II: Situation #2/#3");
            mGeofencing.unRegisterAllGeofences();
        }
    }
    // [END] setup geofencing based on notification setting

    // [START] update summary
    private void updateSummary() {
        SharedPreferences sharedPreferences = mSharedPreferences;
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();

        for (int i = 0; i < count; i++) {
            android.support.v7.preference.Preference p = prefScreen.getPreference(i);

            if (!(p instanceof CheckBoxPreference)
                    && !(p instanceof com.example.shoji.dailytask.dialog.TimePreference)) {
                String value = sharedPreferences.getString(p.getKey(),
                        getString(R.string.empty_string));
                setPreferenceSummary(p, value);
            }
        }
    }
    // [END] update summary

    // [START] location service
    private boolean isNotificationEnabled() {
        return mSharedPreferences.getBoolean(getString(R.string.pref_daily_notification_key),
                getResources().getBoolean(R.bool.pref_daily_notification_default_value));
    }
    // [END] loscation service
    // [START] location service
    private boolean isLocationServiceEnabled() {
        return mSharedPreferences.getBoolean(getString(R.string.pref_location_service_key),
                getResources().getBoolean(R.bool.pref_location_service_default_value));
    }
    // [END] loscation service

    // [START] notification by location
    private void bindLocationPicker() {
        Preference preference = findPreference(getString(R.string.pref_location_open_and_set_place_key));

        // [START] enable or disable location selection screen
        if(!isNotificationEnabled() || !isLocationServiceEnabled()) {
            preference.setSelectable(false);
            preference.setEnabled(false);
            preference.setSummary(R.string.pref_location_open_and_set_place_summaryOff);
        }

        else {
            preference.setSelectable(true);
            preference.setEnabled(true);
            final Context context = mContext;
            // [START] Retrieve picked place into shared preference
            Pair<String, String> pair = LocationUtils.getPickedPlace(context);
            String summaryAddress = pair.second;
            // [END] Retrieve picked place into shared preference
            preference.setSummary(summaryAddress);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    onAddPlaceButtonClicked();
                    return true;
                }
            });
        }
        // [END] enable or disable location selection screen
    }
    // [END] notification by location




    // [START} enable / disable location service
    private void bindLocationCheckBox() {
        boolean isLocationEnabled = mSharedPreferences.getBoolean(getString(R.string.pref_daily_notification_key),
                getResources().getBoolean(R.bool.pref_daily_notification_default_value));

        mLocationCheckBox = (CheckBoxPreference) findPreference(getString(R.string.pref_location_service_key));
        mLocationCheckBox.setSelectable(isLocationEnabled);
        mLocationCheckBox.setEnabled(isLocationEnabled);
    }
    // [END} enable / disable location service



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
        Timber.d("onSharedPreferenceChanged - key: %s", key);
        // [START] enable or disable location selection screen
        if(TextUtils.equals(key, getString(R.string.pref_location_service_key))
                || TextUtils.equals(key, getString(R.string.pref_picked_place_address_key))
                || TextUtils.equals(key, getString(R.string.pref_daily_notification_key))
                || TextUtils.equals(key, getString(R.string.pref_time_picker_minutes_key)) ) {
            bindLocationPicker();
            configureGeofencing();
            TaskReminderUtilities.setupTaskReminderNotification(mContext, sharedPreferences);
        }
        // [END] enable or disable location selection screen
        else {
            Preference preference = findPreference(key);
            if (null != preference) {
                if (!(preference instanceof CheckBoxPreference)
                        && !(preference instanceof com.example.shoji.dailytask.dialog.TimePreference)) {
                    String value = sharedPreferences.getString(preference.getKey(),
                            getString(R.string.empty_string));
                    setPreferenceSummary(preference, value);
                    TaskReminderUtilities.setupTaskReminderNotification(mContext, sharedPreferences);
                }
            }
        }

    }
    // [END] update summary



    // [START] Build GoogleApiClient
    private GoogleApiClient buildGoogleApiClient() {
        Context context = mContext;
        GoogleApiClient.ConnectionCallbacks connectionCallbacks = this;
        GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = this;
        FragmentActivity fragmentActivity = mActivity;
        GoogleApiClient client = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(fragmentActivity, onConnectionFailedListener)
                .build();
        return client;
    }
    // [END] Build GoogleApiClient

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


    // [START] Place picker
    public void onAddPlaceButtonClicked() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestFineLocationPermission();
            //Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            //showSnackBar(mPickLocationButton, R.string.need_location_permission_message, Snackbar.LENGTH_LONG);
            return;
        }

        startPlacePicker();
    }

    // [START] check if has fine location permission, otherwise request it
    public void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(mActivity,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);
    }
    // [END] check if has fine location permission, otherwise request it

    private void startPlacePicker() {
        try {
            // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
            // when a place is selected or with the user cancels.
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(mActivity);
            startActivityForResult(i, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Timber.e("GooglePlayServicesRepairableException [%s]", e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            Timber.e("GooglePlayServicesNotAvailableException [%s]", e.getMessage());
        } catch (Exception e) {
            Timber.e("startPlacePicker Exception: %s", e.getMessage());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == mActivity.RESULT_OK) {
            Place place = PlacePicker.getPlace(mContext, data);
            if (place == null) {
                Timber.d("onActivityResult - No place selected");
                return;
            }

            // Extract the place information from the API
            String placeName = place.getName().toString();
            String placeAddress = place.getAddress().toString();
            String placeID = place.getId();


            Timber.d("onActivityResult - Picked place - id: %s, name: %s, address: %s", placeID,
                    placeName, placeAddress);

            // [START] Save picked place into shared preference
            LocationUtils.setPickedPlace(mContext, placeID, placeAddress);
            // [END] Save picked place into shared preference

            refreshPlacesData();
        }
    }
    // [END] Place picker
    // [START] Retrieve picked place
    public void refreshPlacesData() {
        Timber.d("setupTaskReminderNotification III - refreshPlacesData");
        Context context = mContext;
        Pair<String, String> pair = LocationUtils.getPickedPlace(context);
        String placeId = pair.first;

        if(!LocationUtils.isPlaceIdValid(context, placeId)) {
            Timber.d("refreshPlacesData -- nothing to do");
            return;
        }

        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, placeId);
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                mGeofencing.updateGeofencesList(places);
                if (isLocationServiceEnabled()) mGeofencing.registerAllGeofences();

                places.release();
            }
        });
    }
    // [END] Rerieve picked place




    // [START] GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("GoogleApiClient -- onConnected");
        refreshPlacesData();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Timber.d("GoogleApiClient -- onSuspended (cause: %d)", cause);
    }
    // [END] GoogleApiClient.ConnectionCallbacks

    // [START} GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("onConnectionFailed -- onConnected");
    }
    // [END} GoogleApiClient.OnConnectionFailedListener


    // [START] custom time picker
    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related
            // Preference
            dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference" +
                    ".PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
    // [END] custom time picker
}

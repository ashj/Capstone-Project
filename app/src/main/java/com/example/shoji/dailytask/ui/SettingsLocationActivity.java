package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.example.shoji.dailytask.R;
import com.example.shoji.dailytask.location.LocationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import timber.log.Timber;


public class SettingsLocationActivity extends AppCompatActivityEx
                              implements GoogleApiClient.ConnectionCallbacks,
                                         GoogleApiClient.OnConnectionFailedListener {
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 700;
    private static final int PLACE_PICKER_REQUEST = 750;

    private Button mPickLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_location);

        mPickLocationButton = findViewById(R.id.button);

        GoogleApiClient client = buildGoogleApiClient();



    }
    // [START] Build GoogleApiClient
    private GoogleApiClient buildGoogleApiClient() {
        Context context = this;
        GoogleApiClient.ConnectionCallbacks connectionCallbacks = this;
        GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = this;
        FragmentActivity fragmentActivity = this;
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

    // [START] GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("GoogleApiClient -- onConnected");
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

    // [START] check if has fine location permission, otherwise request it
    public void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(SettingsLocationActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);
    }
    // [END] check if has fine location permission, otherwise request it

    // [START] Place picker
    public void onAddPlaceButtonClicked(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            showSnackBar(mPickLocationButton, R.string.need_location_permission_message, Snackbar.LENGTH_LONG);
            requestFineLocationPermission();
            return;
        }

        startPlacePicker();
    }

    private void startPlacePicker() {
        try {
            // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
            // when a place is selected or with the user cancels.
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(this);
            startActivityForResult(i, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Timber.e("GooglePlayServicesRepairableException [%s]", e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            Timber.e("GooglePlayServicesNotAvailableException [%s]", e.getMessage());
        } catch (Exception e) {
            Timber.e("startPlacePicker Exception: %s", e.getMessage());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
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
            Context context = this;
            LocationUtils.setPickedPlace(context, placeID, placeAddress);
            // [END] Save picked place into shared preference
        }
    }
    // [END] Place picker
}

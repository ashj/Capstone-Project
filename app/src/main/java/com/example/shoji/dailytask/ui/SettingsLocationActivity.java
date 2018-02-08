package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.example.shoji.dailytask.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import timber.log.Timber;


public class SettingsLocationActivity extends AppCompatActivityEx
                              implements GoogleApiClient.ConnectionCallbacks,
                                         GoogleApiClient.OnConnectionFailedListener {
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 700;

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

    public void onAddPlaceButtonClicked(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            //showSnackBar(mPickLocationButton, R.string.need_location_permission_message);
            requestFineLocationPermission();
            return;
        }
        //Toast.makeText(this, getString(R.string.location_permissions_granted_message), Toast.LENGTH_LONG).show();
        showSnackBar(mPickLocationButton, R.string.location_permissions_granted_message);
    }
}

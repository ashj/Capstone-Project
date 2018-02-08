package com.example.shoji.dailytask.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.example.shoji.dailytask.R;

import timber.log.Timber;

public class LocationUtils {
    // [START] Save picked place into shared preference
    public static final void setPickedPlace(Context context, String placeId, String placeAddress) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String placeIdKey = context.getString(R.string.pref_picked_place_id_key);
        sharedPreferences.getString(placeIdKey, placeId);

        String placeAddressKey = context.getString(R.string.pref_picked_place_adress_key);
        sharedPreferences.getString(placeAddressKey, placeAddress);
        Timber.d("setPickedPlace - id: %s, address: %s", placeId, placeAddress);

    }
    // [END] Save picked place into shared preference
}

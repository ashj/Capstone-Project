package com.example.shoji.dailytask.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.shoji.dailytask.R;

import timber.log.Timber;

public class LocationUtils {
    // [START] Save picked place into shared preference
    public static void setPickedPlace(Context context, String placeId, String placeAddress) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String placeIdKey = context.getString(R.string.pref_picked_place_id_key);
        String placeAddressKey = context.getString(R.string.pref_picked_place_address_key);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(placeIdKey, placeId);
        editor.putString(placeAddressKey, placeAddress);

        editor.apply();
        Timber.d("setPickedPlace - id: %s, address: %s", placeId, placeAddress);

    }
    // [END] Save picked place into shared preference

    // [START] Retrieve picked place into shared preference
    public static Pair<String, String> getPickedPlace(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String placeIdKey = context.getString(R.string.pref_picked_place_id_key);
        String placeId = sharedPreferences.getString(placeIdKey, context.getString(R.string.pref_picked_place_id_default_value));

        String placeAddressKey = context.getString(R.string.pref_picked_place_address_key);
        String placeAddress = sharedPreferences.getString(placeAddressKey, context.getString(R.string.pref_picked_place_address_default_value));

        Timber.d("getPickedPlace - id: %s, address: %s", placeId, placeAddress);
        return new Pair<>(placeId, placeAddress);
    }

    public static boolean isPlaceIdValid(Context context, String placeId) {
        if( placeId == null ||
                TextUtils.equals(placeId, context.getString(R.string.empty_string))) {
            return false;
        }

        return true;
    }
    // [END] Retrieve picked place into shared preference
}

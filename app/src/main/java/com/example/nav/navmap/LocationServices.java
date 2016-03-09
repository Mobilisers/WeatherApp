package com.example.nav.navmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.List;
import java.util.Objects;
import java.util.logging.Handler;

/**
 * Created by nav on 3/9/16.
 */
public class LocationServices implements LocationListener {

    public static final int REQUEST_LOCATION = 100;

    LocationManager locationManager;

    Activity activity;

    LocationListener listener;

    public LocationServices(Activity activity, LocationListener listener) {

        this.activity = activity;

        this.listener = listener;

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            Log.e(getClass().getSimpleName(), "Requesting permission");

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, this);

        }

    }

    public void stopLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            Log.e(getClass().getSimpleName(), "Requesting permission");

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
        }

    }

    public android.location.Location getCurrentLocation() {
        Log.e(getClass().getSimpleName(), "current location");
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            Log.e(getClass().getSimpleName(), "Requesting permission");

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
            Log.e(getClass().getSimpleName(), "location is "+bestLocation);

            return bestLocation;
        }
        return null;
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        this.listener.onLocationChanged(location);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        this.listener.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        this.listener.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        this.listener.onProviderDisabled(provider);
        Log.e(getClass().getSimpleName(), "location provider is disabled");

        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setMessage(getApplicationName(activity.getApplicationContext()) + activity.getApplicationContext().getResources().getString(R.string.gps_network_not_enabled));
        dialog.setPositiveButton(activity.getApplicationContext().getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.getApplicationContext().startActivity(myIntent);
                //get gps
            }
        });
        dialog.setNegativeButton(activity.getApplicationContext().getString(R.string.Cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub

            }
        });
        dialog.show();
    }

    public static String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId) + " ";
    }
}

package com.example.nav.navmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MapsActivity extends FragmentActivity implements LocationListener {

    LocationManager locationManager;
    LocationListener locationListener;
    //just in case as i don't have access to an android device at the moment and testing on the simulator
    Boolean manualOverride;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        //manualOverride =true;

        setUpMapIfNeeded();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {

                // Setting a custom info window adapter for the google map
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    // Use default InfoWindow frame
                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    // Defines the contents of the InfoWindow
                    @Override
                    public View getInfoContents(Marker arg0) {

                        // Getting view from the layout file info_window_layout
                        View v = getLayoutInflater().inflate(R.layout.infowindow, null);

                        // Getting the position from the marker
                        LatLng latLng = arg0.getPosition();

                        // Getting reference to the TextView to set latitude and longitude
                        TextView tvLatLng = (TextView) v.findViewById(R.id.latlong);

                        // Getting reference to the TextView to set timezone
                        TextView timezone = (TextView) v.findViewById(R.id.timezone);
                        TextView utc = (TextView) v.findViewById(R.id.utc);
                        TextView local = (TextView) v.findViewById(R.id.localTime);

                        // Setting the latitude
                        tvLatLng.setText("Latitude:" + latLng.latitude +" and Longitude:"+ latLng.longitude);

                        timezone.setText("TimeZone: "+TimeZone.getDefault().getDisplayName());

                        //utc time

                        DateFormat df = DateFormat.getTimeInstance();
                        df.setTimeZone(TimeZone.getTimeZone("utc"));
                        String gmtTime = df.format(new Date());

                        // Setting the longitude
                        utc.setText("UTC: " + gmtTime);

                        df.setTimeZone(TimeZone.getDefault());
                        String localTime = df.format(new Date());
                        local.setText("Local: "+localTime);

                        // Returning the view containing InfoWindow contents
                        return v;

                    }
                });

                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {

                manualOverride = false;

                Log.e(getLocalClassName(), "location changed");

                LatLng latLng = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                mMap.clear();
                MarkerOptions options = new MarkerOptions().position(latLng).title("Marker");
                mMap.addMarker(options);
                mMap.animateCamera(cameraUpdate);
            }
        });

        Log.i(getLocalClassName(), "location changed");


//        if (manualOverride == true) {
//            mMap.clear();
//            LatLng latLng = new LatLng( -36.722375, 174.707047);
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
//            mMap.clear();
//            MarkerOptions options = new MarkerOptions().position(latLng).title("Marker");
//            mMap.addMarker(options);
//            mMap.animateCamera(cameraUpdate);
//        }
    }

    @Override
    public void onLocationChanged(Location location) {

//        if (mMap!= null){
//
//            setUpMap();
//        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

        Log.e(getLocalClassName(),"location provider is disabled");
    }
}

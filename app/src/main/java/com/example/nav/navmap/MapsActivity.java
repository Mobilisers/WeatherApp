package com.example.nav.navmap;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Debug;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements LocationListener {

    public static final int REQUEST_LOCATION = 100;
    LocationManager locationManager;
    LocationListener locationListener;
    static final String APPID = "c907b713e03148dd24a4ee70c9f83410";
    LocationServices locationServices;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.locationServices.stopLocationUpdates();
        this.locationServices = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.locationServices = new LocationServices(this, this);
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
            // mMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {

                Location location = locationServices.getCurrentLocation();
                if (location != null) {
                    Log.e(getLocalClassName(), "recalculating location");
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                    mMap.clear();
                    MarkerOptions options = new MarkerOptions().position(latLng);//.title("Marker");
                    mMap.addMarker(options);
                    mMap.animateCamera(cameraUpdate);
                }
            }

            setUpMap();
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                MarkerOptions options = new MarkerOptions().position(latLng);//.title("Marker");
                mMap.addMarker(options);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom);
                mMap.animateCamera(cameraUpdate);

                String url[] = new String[1];
                url[0] = "http://api.openweathermap.org/data/2.5/weather?lat=" + latLng.latitude + "&lon=" + latLng.latitude + "&APPID=" + APPID;
                try {
                    String json = new Network(getApplicationContext()).execute(url).get();
                    if (json != null) {
                        Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
                        Log.e(getLocalClassName(), String.valueOf(new JSONObject(json)));
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

 /*       // Setting a custom info window adapter for the google map
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
                tvLatLng.setText("Latitude:" + latLng.latitude + " and Longitude:" + latLng.longitude);

                timezone.setText("TimeZone: " + TimeZone.getDefault().getDisplayName());

                //utc time

                DateFormat df = DateFormat.getTimeInstance();
                df.setTimeZone(TimeZone.getTimeZone("utc"));
                String gmtTime = df.format(new Date());

                // Setting the longitude
                utc.setText("UTC: " + gmtTime);

                df.setTimeZone(TimeZone.getDefault());
                String localTime = df.format(new Date());
                local.setText("Local: " + localTime);

                // Returning the view containing InfoWindow contents
                return v;

            }
        });
*/

//        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//
//            @Override
//            public void onMyLocationChange(LocationServices arg0) {
//
//            }
//        });
    }


    public void drawMarkerAtLocation(Location location) {


    }

    //XXXXXXXXXXXXXX
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

        if (mMap != null) {

            Location location = locationServices.getCurrentLocation();
            if (location != null) {
                Log.e(getLocalClassName(), "recalculating location");
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
                mMap.clear();
                MarkerOptions options = new MarkerOptions().position(latLng);//.title("Marker");
                mMap.addMarker(options);
                mMap.animateCamera(cameraUpdate);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

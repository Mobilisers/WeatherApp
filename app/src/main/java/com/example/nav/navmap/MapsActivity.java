package com.example.nav.navmap;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nav.navmap.models.Root;
import com.example.nav.navmap.models.Weather;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MapsActivity extends FragmentActivity {

    //cache last location of marker
    static LatLng lastKnownMarkerLocation;
    public static final int REQUEST_LOCATION = 100;
    public static final String APPID = "c907b713e03148dd24a4ee70c9f83410";
    LocationServices locationServices;
    public static final int DEFAULT_ZOOM_LEVEL = 5;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(getLocalClassName(), "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.locationServices = new LocationServices(this);
    }

    @Override
    protected void onResume() {
        Log.e(getLocalClassName(), "onResume");
        super.onResume();
        if (!new NetworkServices(this).isNetworkAvailable()) {
            Toast.makeText(this, "NetworkServices Connection Not Availble", Toast.LENGTH_LONG).show();
        } else {
            setUpMapIfNeeded();
        }
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
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    setUpMap();
                }

                @Override
                public void onCancel() {

                }
            });

        }

        Log.e(getLocalClassName(), "last known location " + lastKnownMarkerLocation);

        if (lastKnownMarkerLocation == null) {
            locationServices.getCurrentDeviceLocation(new LocationServicesInterface() {
                @Override
                public void deviceLocation(Location location) {
                    if (location != null) {
                        Log.e(getLocalClassName(), "recalculating location");
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        drawMarkerAtLocation(latLng, mMap.getCameraPosition().zoom);
                    }
                }
            });
        } else {
            drawMarkerAtLocation(lastKnownMarkerLocation, mMap.getCameraPosition().zoom);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

//        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//
//            }
//        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lastKnownMarkerLocation = latLng;
                drawMarkerAtLocation(latLng, mMap.getCameraPosition().zoom);
            }
        });

        // Setting a custom info window adapter for the google map
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.infowindow, null);

                // Getting the position from the marker
                LatLng latLng;
                if (lastKnownMarkerLocation == null) {
                    latLng = arg0.getPosition();
                } else {
                    latLng = lastKnownMarkerLocation;
                }

                // Getting reference to the TextView to set latitude and longitude
                final TextView tvLatLng = (TextView) v.findViewById(R.id.latlong);

                // Getting reference to the TextView to set timezone
                TextView timezone = (TextView) v.findViewById(R.id.timezone);
                TextView utc = (TextView) v.findViewById(R.id.utc);
                TextView local = (TextView) v.findViewById(R.id.localTime);

                // Setting the latitude
//                String latlong = "Latitude:" + latLng.latitude + " and Longitude:" + latLng.longitude;
//                tvLatLng.setText(latlong.trim());

                String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + latLng.latitude + "&lon=" + latLng.latitude + "&APPID=" + APPID;
                new NetworkServices(url, getApplicationContext(), new NetworkServicesInterface() {
                    @Override
                    public void result(JSONObject json) throws JSONException {
                        if (json != null) {
                            Gson gson = new GsonBuilder().create();
                            Root root = gson.fromJson(String.valueOf(json), Root.class);
//                            JSONArray array = json.getJSONArray("weather");
//                            JSONObject object = (JSONObject) array.get(0);
                            //Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
                            Log.e(getLocalClassName(), String.valueOf(json));
                            //tvLatLng.setText("Weather: " + object.getString("description"));
                            Weather weather = root.getWeather()[0];
                            tvLatLng.setText(weather.getMain()+" ("+weather.getDescription()+")");
                        }
                    }
                });

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

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(final Marker arg0) {

                return null;

            }
        });


//        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//            @Override
//            public void onMyLocationChange(Location location) {
//                if (lastKnownMarkerLocation == null) {
//                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    drawMarkerAtLocation(latLng, mMap.getCameraPosition().zoom);
//                }
//            }
//        });

    }


    public void drawMarkerAtLocation(LatLng latLng, float zoom) {
        if (mMap != null) {
            mMap.clear();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            MarkerOptions options = new MarkerOptions().position(latLng);//.title("Marker");
            marker = mMap.addMarker(options);
            mMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    marker.setInfoWindowAnchor(0.3f, -0.1f);
                    marker.showInfoWindow();
                }

                @Override
                public void onCancel() {

                }
            });
        }
    }

}

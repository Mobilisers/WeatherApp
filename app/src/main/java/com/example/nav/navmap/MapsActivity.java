package com.example.nav.navmap;

import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nav.navmap.models.GeographicInformation;
import com.example.nav.navmap.models.Main;
import com.example.nav.navmap.models.Root;
import com.example.nav.navmap.models.Sys;
import com.example.nav.navmap.models.Weather;
import com.example.nav.navmap.models.Wind;
import com.example.nav.serviceslibrary.LocationServices;
import com.example.nav.serviceslibrary.LocationServicesInterface;
import com.example.nav.serviceslibrary.NetworkServices;
import com.example.nav.serviceslibrary.NetworkServicesInterface;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity {

    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
    private static String IMG_URL = "http://openweathermap.org/img/w/";
    //cache last location of marker
    static LatLng lastKnownMarkerLocation;
    public static final int REQUEST_LOCATION = 100;
    public static final String APPID = "c907b713e03148dd24a4ee70c9f83410";
    LocationServices locationServices;
    public static final int DEFAULT_ZOOM_LEVEL = 5;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public static final String DEGREE = "\u00b0";
    GeographicInformation geographicInformation;
    Root root;
    Marker marker;
    static boolean refreshMarkerLocation = true;

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
        if (new NetworkServices(this).isNetworkAvailable()) {
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
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));

            setUpMap();

        }

        Log.e(getLocalClassName(), "last known location " + lastKnownMarkerLocation);

        if (refreshMarkerLocation) {
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
                if (marker == null) {
                    lastKnownMarkerLocation = latLng;
                    drawMarkerAtLocation(latLng, mMap.getCameraPosition().zoom);
                    refreshMarkerLocation = true;
                } else {
                    if (marker.isInfoWindowShown()) {
                        lastKnownMarkerLocation = latLng;
                        drawMarkerAtLocation(latLng, mMap.getCameraPosition().zoom);
                        refreshMarkerLocation = true;
                    } else {
                        refreshMarkerLocation = false;
                        lastKnownMarkerLocation = null;
                        mMap.clear();
                        marker = null;
                    }
                }
            }
        });

        // Setting a custom info window adapter for the google map
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(final Marker arg0) {
                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.infowindow, null);
                // Getting the position from the marker
                LatLng latLng;
                if (lastKnownMarkerLocation == null) {
                    latLng = arg0.getPosition();
                } else {
                    latLng = lastKnownMarkerLocation;
                }
                TextView cityText = (TextView) v.findViewById(R.id.cityText);
                final TextView condDescr = (TextView) v.findViewById(R.id.condDescr);
                final TextView temp = (TextView) v.findViewById(R.id.temp);
                final TextView hum = (TextView) v.findViewById(R.id.hum);
                final TextView press = (TextView) v.findViewById(R.id.press);
                final TextView windSpeed = (TextView) v.findViewById(R.id.windSpeed);
                final TextView windDeg = (TextView) v.findViewById(R.id.windDeg);
                final ImageView imgView = (ImageView) v.findViewById(R.id.condIcon);
                Weather weather = root.getWeather()[0];
                Main main = root.getMain();
                Wind wind = root.getWind();
                Sys sys = root.getSys();
                condDescr.setText(weather.getMain() + " (" + weather.getDescription() + ")");
                temp.setText("" + Math.round(Double.valueOf(main.getTemp()) - 273.15) + DEGREE + "C");
                hum.setText("" + main.getHumidity() + "%");
                press.setText("" + main.getPressure() + " hPa");
                windSpeed.setText("" + wind.getSpeed() + " mps");
                windDeg.setText("" + wind.getDeg() + DEGREE);
                Geocoder geocoder;
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (geographicInformation != null) {
                    if (geographicInformation.country != null) {
                        if (geographicInformation.state == null) {
                            cityText.setText(geographicInformation.country);
                        } else {
                            cityText.setText(geographicInformation.country + " (" + geographicInformation.state + ")");
                        }
                    } else {
                        cityText.setVisibility(View.GONE);
                    }
                } else {
                    cityText.setVisibility(View.GONE);
                }
                //imgView.setImageDrawable(getResources().getDrawable(R.drawable.wally));
                imgView.setImageBitmap(new ImageUtil().StringToBitMap(weather.getIcon()));
                // Returning the view containing InfoWindow contents
                return v;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(final Marker arg0) {
                return null;

            }
        });

    }

    /**
     * Draws marker at the specified latLng and makes network call to retrieve associated information for the geolocation.
     *
     * @param latLng the latLng for the marker
     * @param zoom   the zoom level for the map
     */
    public void drawMarkerAtLocation(LatLng latLng, float zoom) {
        if (mMap != null) {
            mMap.clear();
            //XXXXXXXXXXXX
            lastKnownMarkerLocation = latLng;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            MarkerOptions options = new MarkerOptions().position(latLng);//.title("Marker");
            marker = mMap.addMarker(options);
            String tempUrl = BASE_URL + "lat=" + marker.getPosition().latitude + "&lon=" + marker.getPosition().latitude + "&APPID=" + APPID;
            geographicInformation = new GeoUtil().getGeoDataFromLatLng(getApplicationContext(), marker.getPosition());
            if (geographicInformation != null) {
                if (geographicInformation.countryCode != null & geographicInformation.state != null) {
                    tempUrl = BASE_URL + "lat=" + marker.getPosition().latitude + "&lon=" + marker.getPosition().latitude + "&APPID=" + APPID + "&q=" + geographicInformation.city + "," + geographicInformation.countryCode;
                }
            }
            final String url = tempUrl;
            mMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    new NetworkServices(getApplicationContext()).runAsyncNetworkTask(url, new NetworkServicesInterface() {
                        @Override
                        public void onResult(String string) {
                            if (string != null) {
                                try {
                                    Gson gson = new GsonBuilder().create();
                                    root = gson.fromJson(string, Root.class);
                                    final Weather weather = root.getWeather()[0];
                                    Log.e(getLocalClassName(), string);
                                    final String img_url = IMG_URL + weather.getIcon() + ".png";
                                    Bitmap bmp = new NetworkServices(getApplicationContext()).getBitmapFromUrl(img_url);
                                    weather.setIcon(new ImageUtil().BitMapToString(bmp));
                                    marker.setInfoWindowAnchor(0.3f, -0.1f);
                                    marker.showInfoWindow();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Some error happened, please relaunch the app", Toast.LENGTH_LONG).show();

                                }
                            }
                        }
                    });
                }

                @Override
                public void onCancel() {

                }
            });

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationServices != null) {
            locationServices.stopLocationUpdates();
            locationServices = null;
        }
    }
}

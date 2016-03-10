package com.example.nav.navmap;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.example.nav.navmap.models.GeographicInformation;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by nav on 3/11/16.
 */
public class GeoUtil {

    GeographicInformation geographicInformation;

    /**
     * Gets geo Information from provided latLng
     *
     * @param context the application context preferably
     * @param latLng  the latLng for which to get geo information
     * @return returns GeographicInformation
     */
    public GeographicInformation getGeoDataFromLatLng(Context context, LatLng latLng) {
        //find geo information from latLng received
        Geocoder geocoder;
        geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            String city = addresses.get(0).getLocality();
            if (city == null) {
                city = addresses.get(0).getSubAdminArea();
            }
            String state = addresses.get(0).getAdminArea();
            String zip = addresses.get(0).getPostalCode();
            String country = addresses.get(0).getCountryName();
            String countryCode = addresses.get(0).getCountryCode();
            geographicInformation = new GeographicInformation();
            geographicInformation.city = city;
            geographicInformation.state = state;
            geographicInformation.zip = zip;
            geographicInformation.country = country;
            geographicInformation.countryCode = countryCode;
            Log.e(getClass().getSimpleName(), "Geo Information: " + addresses.get(0));
//            if (countryCode != null & state != null) {
//                tempUrl = BASE_URL + "lat=" + marker.getPosition().latitude + "&lon=" + marker.getPosition().latitude + "&APPID=" + APPID + "&q=" + city + "," + countryCode;
//            }
        } else {
            //fix for cached geo information shown when geo information not available for geolocation such as oceans
            geographicInformation = null;
        }
        return geographicInformation;
    }
}

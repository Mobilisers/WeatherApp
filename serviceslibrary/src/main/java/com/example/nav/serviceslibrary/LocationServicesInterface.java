package com.example.nav.serviceslibrary;

import android.location.Location;

/**
 * Created by nav on 3/9/16.
 */

public interface LocationServicesInterface {

    /** returns the device location
     * @param location
     */
    public void deviceLocation(Location location);

}

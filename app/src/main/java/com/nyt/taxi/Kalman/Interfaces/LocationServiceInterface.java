package com.nyt.taxi.Kalman.Interfaces;

import android.location.Location;

/**
 * Created by lezh1k on 2/13/18.
 */

public interface LocationServiceInterface {
    void locationChanged(Location location);
}

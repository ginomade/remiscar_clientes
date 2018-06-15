package com.nomade.forma.app.utils;

/**
 * Created by Gino on 22/3/2018.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Gino on 03/06/2017.
 */
public class GooglePlayServicesHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String TAG = GooglePlayServicesHelper.class.getSimpleName();
    public static boolean LOG_ON = false;

    /**
     * Velocidade do GPS:
     * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
     */
    public static int LOCATION_GPS_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    public static int LOCATION_GPS_INTERVAL_MILLIS = 30000;
    public static int LOCATION_GPS_FASTEST_INTERVAL_MILLIS = 10000;

    private final GoogleApiClient mGoogleApiClient;
    private Set<LocationListener> locationListeners;
    private boolean gpsOn;

    Context mContext;

    public GooglePlayServicesHelper(Context context, boolean gpsOn) {

        log("GooglePlayServicesHelper(), gpsOn: " + gpsOn);

        this.gpsOn = gpsOn;
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this);

        if (gpsOn) {
            builder.addApi(LocationServices.API);
        }

        mGoogleApiClient = builder.build();
        mContext = context;
    }

    // Conecta no Google Play Services
    public void onResume(LocationListener locationListener) {
        log("connect()");
        mGoogleApiClient.connect();

        if (gpsOn) {
            addLocationListeners(locationListener);
        }
    }

    private void log(String s) {
        if (LOG_ON) {
            Log.v(TAG, s);
        }
    }

    // Desconecta do Google Play Services
    public void onPause() {
        if (mGoogleApiClient.isConnected()) {
            if (gpsOn) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
            log("disconnect()");
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        log("onConnected()");

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_GPS_INTERVAL_MILLIS); // 10 segundos
        mLocationRequest.setFastestInterval(LOCATION_GPS_FASTEST_INTERVAL_MILLIS); // 5 segundos
        mLocationRequest.setPriority(LOCATION_GPS_PRIORITY);

        if (gpsOn) {
            // Start GPS

            if (ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int status) {
        log("onConnectionSuspended(): " + status);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        log("onConnectionFailed(): " + connectionResult);
    }

    private void addLocationListeners(LocationListener locationListeners) {
        if (this.locationListeners == null) {
            this.locationListeners = new LinkedHashSet<>();
        }
        this.locationListeners.add(locationListeners);
    }

    public String getLastLocationString() {
        Location l = getLastLocation();
        if (l != null) {
            double latitude = l.getLatitude();
            double longitude = l.getLongitude();
            return String.format("lat/lng: %s/%s", latitude, longitude);
        } else {
            return "lat/lng: 0/0";
        }

    }

    public Location getLastLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        Location l = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        return l;
    }

    @Override
    public void onLocationChanged(Location location) {
        log("onLocationChanged(): " + location);

        if (locationListeners != null) {
            for (LocationListener listener : locationListeners) {
                listener.onLocationChanged(location);
            }
        }
    }

}
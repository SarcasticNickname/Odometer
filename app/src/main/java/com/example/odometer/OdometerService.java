package com.example.odometer;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class OdometerService extends Service {

    private final IBinder binder = new OdometerBinder();
    public static final String PERMISSION_STRING = Manifest.permission.ACCESS_FINE_LOCATION;
    private LocationListener listener;
    private LocationManager locManager;
    private static double distanceInMeters;
    private static Location lastLocation = null;

    public OdometerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (lastLocation == null) {
                    lastLocation = location;
                }
                distanceInMeters += location.distanceTo(lastLocation);
                lastLocation = location;
            }

            @Override
            public void onProviderDisabled(String arg0){}

            @Override
            public void onProviderEnabled(String arg0){}

            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle bundle){}
        };

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                == PackageManager.PERMISSION_GRANTED) {
            String provider = locManager.getBestProvider(new Criteria(), true);
            if (provider != null) {
                locManager.requestLocationUpdates(provider, 100, 1, listener);
            } else {
                Toast toast = Toast.makeText(this, "Error", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                == PackageManager.PERMISSION_GRANTED) {
            if (listener != null) {
                locManager.removeUpdates(listener);
            }
            listener = null;
            locManager = null;
        }
    }

    public class OdometerBinder extends Binder {
        OdometerService getOdometer() {
            return OdometerService.this;
        }
    }

    public double getDistance() {
        return distanceInMeters;
    }

}
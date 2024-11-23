package com.ronit.eventconnector.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.ronit.eventconnector.models.Event;

public class LocationUtils {
    public static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static boolean hasLocationPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void getCurrentLocation(Context context, OnSuccessListener<Location> listener) {
        if (!hasLocationPermissions(context)) {
            return;
        }

        try {
            FusedLocationProviderClient fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(context);
            fusedLocationClient.getLastLocation().addOnSuccessListener(listener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static double calculateDistance(double startLat, double startLng,
                                           double endLat, double endLng) {
        float[] results = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, results);
        return results[0] / 1000; // Convert meters to kilometers
    }

    public static String formatDistance(double distanceKm) {
        if (distanceKm < 0.1) {
            return "Nearby";
        } else if (distanceKm < 1) {
            return String.format("%.0fm away", distanceKm * 1000);
        } else {
            return String.format("%.1f km away", distanceKm);
        }
    }
}
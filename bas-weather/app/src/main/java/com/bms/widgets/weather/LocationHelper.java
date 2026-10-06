package com.bms.widgets.weather;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;
import java.util.Locale;

public final class LocationHelper {
    public interface Callback {
        void onSuccess(String city, String district);
        void onError(String message);
    }

    private LocationHelper() {}

    public static void refresh(Activity activity, Callback callback) {
        if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("يلزم السماح بالموقع");
            return;
        }

        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            callback.onError("خدمة الموقع غير متاحة");
            return;
        }

        Location best = null;
        try {
            Location gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location net = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            best = newer(gps, net);
        } catch (SecurityException ignored) {}

        if (best != null && System.currentTimeMillis() - best.getTime() < 6 * 60 * 60 * 1000L) {
            resolve(activity, best, callback);
            return;
        }

        LocationListener listener = new LocationListener() {
            @Override public void onLocationChanged(Location location) {
                try { lm.removeUpdates(this); } catch (Throwable ignored) {}
                resolve(activity, location, callback);
            }
            @Override public void onProviderDisabled(String provider) {}
            @Override public void onProviderEnabled(String provider) {}
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        };

        try {
            String provider = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    ? LocationManager.NETWORK_PROVIDER : LocationManager.GPS_PROVIDER;
            lm.requestSingleUpdate(provider, listener, activity.getMainLooper());
        } catch (Throwable e) {
            if (best != null) resolve(activity, best, callback);
            else callback.onError("تعذر تحديد الموقع");
        }
    }

    private static Location newer(Location a, Location b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.getTime() >= b.getTime() ? a : b;
    }

    private static void resolve(Context context, Location location, Callback callback) {
        new Thread(() -> {
            String city = "تبوك";
            String district = "الموقع الحالي";
            try {
                Geocoder g = new Geocoder(context, new Locale("ar", "SA"));
                List<Address> list = g.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (list != null && !list.isEmpty()) {
                    Address a = list.get(0);
                    city = first(a.getLocality(), a.getSubAdminArea(), a.getAdminArea(), "تبوك");
                    district = first(a.getSubLocality(), a.getThoroughfare(), a.getFeatureName(), "الموقع الحالي");
                    if (district.equals(city)) district = "الموقع الحالي";
                }
            } catch (Throwable ignored) {}

            WeatherStore.savePlace(context, city, district, location.getLatitude(), location.getLongitude());
            String finalCity = city, finalDistrict = district;
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> callback.onSuccess(finalCity, finalDistrict));
            } else {
                callback.onSuccess(finalCity, finalDistrict);
            }
        }).start();
    }

    private static String first(String a, String b, String c, String fallback) {
        if (a != null && !a.trim().isEmpty()) return a.trim();
        if (b != null && !b.trim().isEmpty()) return b.trim();
        if (c != null && !c.trim().isEmpty()) return c.trim();
        return fallback;
    }
}

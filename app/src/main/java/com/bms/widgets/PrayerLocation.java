package com.bms.widgets;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import java.util.List;
import java.util.Locale;

/** Shared location for all prayer widgets. GPS is used only with user permission. */
public final class PrayerLocation {
    private static final String PREFS = "bms_widget_prefs";
    private static final String KEY_CITY = "prayer_city";
    private static final String KEY_LAT = "prayer_lat_bits";
    private static final String KEY_LON = "prayer_lon_bits";
    private static final String KEY_SELECTION = "prayer_city_selection";
    private static final double DEFAULT_LAT = 28.3833;
    private static final double DEFAULT_LON = 36.5833;

    public static final String[] OPTIONS = {
        "تلقائي — حسب موقع الجهاز", "سكاكا", "تبوك", "الرياض", "جدة",
        "مكة المكرمة", "المدينة المنورة", "الدمام", "الخبر", "الطائف",
        "أبها", "خميس مشيط", "جازان", "نجران", "حائل", "بريدة",
        "عرعر", "القريات", "ينبع", "العلا", "حفر الباطن"
    };
    private static final double[] LAT = {
        0, 29.9697, 28.3833, 24.7136, 21.5433,
        21.3891, 24.4672, 26.4207, 26.2794, 21.2703,
        18.2164, 18.3000, 16.8892, 17.5656, 27.5114, 26.3592,
        30.9753, 31.3324, 24.0895, 26.6085, 28.4328
    };
    private static final double[] LON = {
        0, 40.2064, 36.5833, 46.6753, 39.1728,
        39.8579, 39.6112, 50.0888, 50.2083, 40.4158,
        42.5053, 42.7292, 42.5511, 44.2289, 41.7208, 43.9818,
        41.0381, 37.3428, 38.0618, 37.9232, 45.9708
    };

    private PrayerLocation() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int selection(Context c) {
        int selected = prefs(c).getInt(KEY_SELECTION, 0);
        return selected >= 0 && selected < OPTIONS.length ? selected : 0;
    }

    public static boolean automatic(Context c) {
        return selection(c) == 0;
    }

    public static boolean permissionGranted(Context c) {
        return c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void select(Context c, int index) {
        index = Math.max(0, Math.min(index, OPTIONS.length - 1));
        if (index == selection(c)) return;
        SharedPreferences.Editor edit = prefs(c).edit().putInt(KEY_SELECTION, index);
        if (index == 0) edit.remove(KEY_LAT).remove(KEY_LON).remove(KEY_CITY);
        else edit.putString(KEY_CITY, OPTIONS[index]);
        edit.apply();
        updateWidgets(c);
    }

    public static double latitude(Context c) {
        int selected = selection(c);
        if (selected > 0) return LAT[selected];
        return Double.longBitsToDouble(prefs(c).getLong(KEY_LAT, Double.doubleToLongBits(DEFAULT_LAT)));
    }

    public static double longitude(Context c) {
        int selected = selection(c);
        if (selected > 0) return LON[selected];
        return Double.longBitsToDouble(prefs(c).getLong(KEY_LON, Double.doubleToLongBits(DEFAULT_LON)));
    }

    public static String city(Context c) {
        int selected = selection(c);
        if (selected > 0) return OPTIONS[selected];
        return prefs(c).getString(KEY_CITY, "تبوك (موقع افتراضي)");
    }

    /** Widgets only query cached fixes; they never trigger a permission prompt. */
    public static void refreshFromLastKnown(Context c) {
        if (!automatic(c) || !permissionGranted(c)) return;
        try {
            LocationManager manager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            if (manager == null) return;
            Location best = null;
            for (String provider : new String[]{
                    LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER}) {
                Location candidate = manager.getLastKnownLocation(provider);
                if (candidate != null && (best == null || candidate.getTime() > best.getTime()))
                    best = candidate;
            }
            if (best != null) accept(c, best);
        } catch (SecurityException | IllegalArgumentException ignored) {
            // Permission and location providers may change at runtime.
        }
    }

    public static void accept(Context c, Location fix) {
        if (!automatic(c) || fix == null) return;
        double lat = fix.getLatitude(), lon = fix.getLongitude();
        if (Double.isNaN(lat) || Double.isNaN(lon)
                || Math.abs(lat) > 90 || Math.abs(lon) > 180) return;
        long age = System.currentTimeMillis() - fix.getTime();
        if (age > 36L * 60L * 60L * 1000L || age < -5L * 60L * 1000L) return;
        SharedPreferences p = prefs(c);
        if (p.contains(KEY_LAT) && p.contains(KEY_LON)) {
            float[] distance = new float[1];
            Location.distanceBetween(latitude(c), longitude(c), lat, lon, distance);
            if (distance[0] < 1000f) return;
        }
        p.edit().putLong(KEY_LAT, Double.doubleToLongBits(lat))
                .putLong(KEY_LON, Double.doubleToLongBits(lon))
                .putString(KEY_CITY, "جارٍ تحديد اسم المدينة…").apply();
        updateWidgets(c);
        resolveCity(c, lat, lon);
    }

    private static void resolveCity(Context c, double lat, double lon) {
        String fallback = String.format(Locale.US, "موقعك الحالي (%.2f، %.2f)", lat, lon);
        if (!Geocoder.isPresent()) {
            resolved(c, lat, lon, fallback);
            return;
        }
        try {
            Geocoder geocoder = new Geocoder(c, new Locale("ar", "SA"));
            if (Build.VERSION.SDK_INT >= 33) {
                geocoder.getFromLocation(lat, lon, 1, new Geocoder.GeocodeListener() {
                    @Override public void onGeocode(List<Address> results) {
                        resolved(c, lat, lon, nameFromAddresses(results, fallback));
                    }
                    @Override public void onError(String message) {
                        resolved(c, lat, lon, fallback);
                    }
                });
            } else {
                new Thread(() -> {
                    try {
                        List<Address> results = geocoder.getFromLocation(lat, lon, 1);
                        resolved(c, lat, lon, nameFromAddresses(results, fallback));
                    } catch (Exception ignored) {
                        resolved(c, lat, lon, fallback);
                    }
                }, "BAS-City-Lookup").start();
            }
        } catch (Exception ignored) {
            resolved(c, lat, lon, fallback);
        }
    }

    private static String nameFromAddresses(List<Address> results, String fallback) {
        if (results == null || results.isEmpty()) return fallback;
        Address address = results.get(0);
        String name = address.getLocality();
        if (name == null || name.trim().isEmpty()) name = address.getSubAdminArea();
        if (name == null || name.trim().isEmpty()) name = address.getAdminArea();
        return name == null || name.trim().isEmpty() ? fallback : name.trim();
    }

    private static void resolved(Context c, double lat, double lon, String name) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!automatic(c) || Math.abs(latitude(c) - lat) > 0.00001
                    || Math.abs(longitude(c) - lon) > 0.00001) return;
            prefs(c).edit().putString(KEY_CITY, name).apply();
            updateWidgets(c);
            if (c instanceof MainActivity) ((MainActivity) c).showLocationStatus();
        });
    }

    public static void updateWidgets(Context c) {
        AppWidgetManager manager = AppWidgetManager.getInstance(c);
        for (int id : manager.getAppWidgetIds(new ComponentName(c, PrayerTimesWidgetProvider.class)))
            PrayerTimesWidgetProvider.updateWidget(c, manager, id);
        for (int id : manager.getAppWidgetIds(new ComponentName(c, CombinedWidgetProvider.class)))
            CombinedWidgetProvider.updateWidget(c, manager, id);
    }
}

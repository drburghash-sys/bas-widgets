package com.bms.widgets.weather;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class WeatherStore {
    private static final String PREFS = "bas_weather";
    private WeatherStore() {}

    public static final class Snapshot {
        public final boolean hasData;
        public final float temperature;
        public final float humidity;
        public final int weatherCode;
        public final boolean isDay;
        public final float dust;
        public final float pm10;
        public final long updatedAt;

        Snapshot(boolean hasData, float temperature, float humidity, int weatherCode,
                 boolean isDay, float dust, float pm10, long updatedAt) {
            this.hasData = hasData;
            this.temperature = temperature;
            this.humidity = humidity;
            this.weatherCode = weatherCode;
            this.isDay = isDay;
            this.dust = dust;
            this.pm10 = pm10;
            this.updatedAt = updatedAt;
        }
    }

    public static Snapshot load(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new Snapshot(
                p.getBoolean("has_data", false),
                p.getFloat("temperature", 0f),
                p.getFloat("humidity", 0f),
                p.getInt("weather_code", 0),
                p.getBoolean("is_day", true),
                p.getFloat("dust", 0f),
                p.getFloat("pm10", 0f),
                p.getLong("updated_at", 0L)
        );
    }

    public static void save(Context context, double temperature, double humidity, int weatherCode,
                            boolean isDay, double dust, double pm10) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean("has_data", true)
                .putFloat("temperature", (float) temperature)
                .putFloat("humidity", (float) humidity)
                .putInt("weather_code", weatherCode)
                .putBoolean("is_day", isDay)
                .putFloat("dust", (float) dust)
                .putFloat("pm10", (float) pm10)
                .putLong("updated_at", System.currentTimeMillis())
                .apply();
    }

    public static boolean isDusty(float dust, float pm10) {
        return dust >= 50f || pm10 >= 150f;
    }

    public static String icon(int code, boolean isDay) {
        if (code == 0) return isDay ? "☀️" : "🌙";
        if (code == 1 || code == 2) return isDay ? "🌤️" : "🌙☁️";
        if (code == 3) return "☁️";
        if (code == 45 || code == 48) return "🌫️";
        if (code >= 51 && code <= 67) return "🌧️";
        if (code >= 71 && code <= 77) return "❄️";
        if (code >= 80 && code <= 82) return "🌦️";
        if (code >= 85 && code <= 86) return "🌨️";
        if (code >= 95) return "⛈️";
        return "☁️";
    }

    public static String condition(int code) {
        if (code == 0) return "صحو";
        if (code == 1) return "صحو غالبًا";
        if (code == 2) return "غائم جزئيًا";
        if (code == 3) return "غائم";
        if (code == 45 || code == 48) return "ضباب";
        if (code >= 51 && code <= 57) return "رذاذ";
        if (code >= 61 && code <= 67) return "أمطار";
        if (code >= 71 && code <= 77) return "ثلوج";
        if (code >= 80 && code <= 82) return "زخات";
        if (code >= 85 && code <= 86) return "زخات ثلجية";
        if (code >= 95) return "عواصف رعدية";
        return "غائم";
    }

    public static String formatTime(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("h:mm a", new Locale("ar", "SA"));
        return arabicDigits(f.format(new Date(millis)).replace("AM", "ص").replace("PM", "م"));
    }

    public static String arabicDigits(String source) {
        final char[] digits = {'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};
        StringBuilder out = new StringBuilder(source.length());
        for (char c : source.toCharArray()) {
            out.append(c >= '0' && c <= '9' ? digits[c - '0'] : c);
        }
        return out.toString();
    }
}

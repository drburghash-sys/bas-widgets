package com.bms.widgets.weather;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WeatherWorker extends Worker {
    private static final double DEFAULT_LAT = 28.3838;
    private static final double DEFAULT_LON = 36.5550;

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            WeatherStore.Place place = WeatherStore.loadPlace(getApplicationContext());
            double lat = place.hasLocation ? place.latitude : DEFAULT_LAT;
            double lon = place.hasLocation ? place.longitude : DEFAULT_LON;

            String weatherUrl =
                    "https://api.open-meteo.com/v1/forecast"
                    + "?latitude=" + lat
                    + "&longitude=" + lon
                    + "&current=temperature_2m,relative_humidity_2m,weather_code,is_day"
                    + "&timezone=Asia%2FRiyadh";

            String airUrl =
                    "https://air-quality-api.open-meteo.com/v1/air-quality"
                    + "?latitude=" + lat
                    + "&longitude=" + lon
                    + "&current=dust,pm10"
                    + "&timezone=Asia%2FRiyadh";

            JSONObject weather = new JSONObject(get(weatherUrl)).getJSONObject("current");
            JSONObject air = new JSONObject(get(airUrl)).optJSONObject("current");

            double temp = weather.getDouble("temperature_2m");
            double humidity = weather.getDouble("relative_humidity_2m");
            int code = weather.getInt("weather_code");
            boolean isDay = weather.optInt("is_day", 1) == 1;
            double dust = air == null ? 0.0 : air.optDouble("dust", 0.0);
            double pm10 = air == null ? 0.0 : air.optDouble("pm10", 0.0);

            WeatherStore.save(getApplicationContext(), temp, humidity, code, isDay, dust, pm10);
            WeatherWidgetProvider.updateAll(getApplicationContext());
            return Result.success();
        } catch (Throwable e) {
            WeatherWidgetProvider.updateAll(getApplicationContext());
            return Result.retry();
        }
    }

    private static String get(String address) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(address).openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("User-Agent", "BAS-Weather/2.0");

        int status = c.getResponseCode();
        if (status < 200 || status >= 300) {
            c.disconnect();
            throw new IllegalStateException("HTTP " + status);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                c.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) out.append(line);
        reader.close();
        c.disconnect();
        return out.toString();
    }
}

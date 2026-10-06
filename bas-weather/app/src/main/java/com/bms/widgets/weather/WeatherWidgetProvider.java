package com.bms.widgets.weather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.view.View;
import android.widget.RemoteViews;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WeatherWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_REFRESH = "com.bms.widgets.weather.REFRESH";
    private static final String PERIODIC_NAME = "bas_weather_periodic";

    @Override public void onEnabled(Context context) {
        super.onEnabled(context);
        schedulePeriodic(context);
        enqueueRefresh(context);
    }

    @Override public void onDisabled(Context context) {
        super.onDisabled(context);
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_NAME);
    }

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) render(context, manager, id);
        enqueueRefresh(context);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_REFRESH.equals(action)) {
            renderAllImmediately(context);
            enqueueRefresh(context);
        } else if (Intent.ACTION_POWER_CONNECTED.equals(action)
                || Intent.ACTION_POWER_DISCONNECTED.equals(action)
                || Intent.ACTION_BATTERY_LOW.equals(action)
                || Intent.ACTION_BATTERY_OKAY.equals(action)) {
            renderAllImmediately(context);
        }
    }

    public static void schedulePeriodic(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WeatherWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints(constraints).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_NAME, ExistingPeriodicWorkPolicy.UPDATE, request);
    }

    public static void enqueueRefresh(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        WorkManager.getInstance(context).enqueue(
                new OneTimeWorkRequest.Builder(WeatherWorker.class)
                        .setConstraints(constraints).build());
    }

    public static void updateAll(Context context) {
        renderAllImmediately(context);
    }

    private static void renderAllImmediately(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, WeatherWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(provider)) render(context, manager, id);
    }

    public static void render(Context context, AppWidgetManager manager, int widgetId) {
        WeatherStore.Snapshot s = WeatherStore.load(context);
        WeatherStore.Place place = WeatherStore.loadPlace(context);
        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_weather);

        v.setTextViewText(R.id.weatherCity, "⌖  " + place.city);
        v.setTextViewText(R.id.weatherDistrict, place.district);

        int[] battery = batteryState(context);
        int level = battery[0];
        int charging = battery[1];
        v.setTextViewText(R.id.batteryPercent,
                level >= 0 ? WeatherStore.arabicDigits(level + "٪") : "--٪");
        v.setTextViewText(R.id.batteryLabel, charging == 1 ? "قيد الشحن" : "البطارية");
        v.setImageViewResource(R.id.batteryIcon, batteryIcon(level, charging == 1));

        v.setTextViewText(R.id.weatherTemp,
                s.hasData ? WeatherStore.arabicDigits(Math.round(s.temperature) + "°") : "--°");
        v.setTextViewText(R.id.weatherHumidity,
                s.hasData ? "💧 الرطوبة " + WeatherStore.arabicDigits(Math.round(s.humidity) + "٪")
                        : "💧 الرطوبة --٪");

        String icon = s.hasData ? WeatherStore.icon(s.weatherCode, s.isDay) : "☀️";
        String condition = s.hasData ? WeatherStore.condition(s.weatherCode) : "جاري التحديث";
        boolean dusty = s.hasData && WeatherStore.isDusty(s.dust, s.pm10);
        if (dusty) condition = "غبار · " + condition;

        v.setTextViewText(R.id.weatherIcon, icon);
        v.setTextViewText(R.id.weatherCondition, condition);
        v.setViewVisibility(R.id.weatherDust, dusty ? View.VISIBLE : View.GONE);
        v.setTextViewText(R.id.weatherUpdated,
                s.updatedAt > 0 ? "آخر تحديث " + WeatherStore.formatTime(s.updatedAt)
                        : "اضغط ↻ للتحديث");

        Intent refresh = new Intent(context, WeatherWidgetProvider.class).setAction(ACTION_REFRESH);
        PendingIntent refreshPi = PendingIntent.getBroadcast(
                context, 9001, refresh, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.weatherRefresh, refreshPi);

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(
                context, 9002, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.weatherRoot, openPi);

        manager.updateAppWidget(widgetId, v);
    }

    private static int[] batteryState(Context context) {
        try {
            Intent b = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (b == null) return new int[]{-1, 0};
            int level = b.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = b.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int pct = (level >= 0 && scale > 0) ? Math.round(level * 100f / scale) : -1;
            int status = b.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;
            return new int[]{pct, charging ? 1 : 0};
        } catch (Throwable ignored) {
            return new int[]{-1, 0};
        }
    }

    private static int batteryIcon(int level, boolean charging) {
        if (charging) return R.drawable.ic_battery_charging;
        if (level < 0) return R.drawable.ic_battery_mid;
        if (level >= 80) return R.drawable.ic_battery_full;
        if (level >= 55) return R.drawable.ic_battery_high;
        if (level >= 25) return R.drawable.ic_battery_mid;
        if (level >= 10) return R.drawable.ic_battery_low;
        return R.drawable.ic_battery_critical;
    }
}

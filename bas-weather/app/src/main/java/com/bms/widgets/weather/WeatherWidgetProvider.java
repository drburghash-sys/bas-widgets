package com.bms.widgets.weather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        schedulePeriodic(context);
        enqueueRefresh(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_NAME);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) render(context, manager, id);
        enqueueRefresh(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) enqueueRefresh(context);
    }

    public static void schedulePeriodic(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WeatherWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request);
    }

    public static void enqueueRefresh(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WeatherWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, WeatherWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(provider)) render(context, manager, id);
    }

    public static void render(Context context, AppWidgetManager manager, int widgetId) {
        WeatherStore.Snapshot s = WeatherStore.load(context);
        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_weather);

        v.setTextViewText(R.id.weatherCity, "تبوك");
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

        Intent refresh = new Intent(context, WeatherWidgetProvider.class)
                .setAction(ACTION_REFRESH);
        PendingIntent refreshPi = PendingIntent.getBroadcast(
                context, 9001, refresh,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.weatherRefresh, refreshPi);

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(
                context, 9002, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.weatherRoot, openPi);

        manager.updateAppWidget(widgetId, v);
    }
}

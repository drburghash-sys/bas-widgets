package com.bms.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RemoteViews;

public class PrayerTimesWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        updateWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context, PrayerTimesWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(provider);
            for (int id : ids) updateWidget(context, manager, id);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_prayer_times);
        PrayerTimesCalculator.Times t = PrayerTimesCalculator.calculateToday();

        views.setTextViewText(R.id.fajrTime, t.fajr);
        views.setTextViewText(R.id.sunriseTime, t.sunrise);
        views.setTextViewText(R.id.dhuhrTime, t.dhuhr);
        views.setTextViewText(R.id.asrTime, t.asr);
        views.setTextViewText(R.id.maghribTime, t.maghrib);
        views.setTextViewText(R.id.ishaTime, t.isha);

        WidgetStyle.applyBackgroundAndWatermark(context, views, R.id.prayerWidgetRoot, R.id.prayerThemeMark);

        int color = WidgetStyle.getTextColor(context);
        int[] labels = {
                R.id.fajrLabel, R.id.sunriseLabel, R.id.dhuhrLabel,
                R.id.asrLabel, R.id.maghribLabel, R.id.ishaLabel
        };
        int[] times = {
                R.id.fajrTime, R.id.sunriseTime, R.id.dhuhrTime,
                R.id.asrTime, R.id.maghribTime, R.id.ishaTime
        };

        float scale = WidgetStyle.getFontScale(context);
        for (int id : labels) {
            views.setTextColor(id, color);
            views.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, 14f * scale);
        }
        for (int id : times) {
            views.setTextColor(id, color);
            views.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, 16f * scale);
        }
        views.setTextColor(R.id.prayerLocation, WidgetStyle.withAlpha(color, 195));
        views.setTextViewTextSize(R.id.prayerLocation, TypedValue.COMPLEX_UNIT_SP, 11f * scale);

        Intent openApp = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 200, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.prayerWidgetRoot, pendingIntent);

        manager.updateAppWidget(appWidgetId, views);
    }
}

package com.bms.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.util.Date;
import java.util.Locale;

public class CombinedWidgetProvider extends AppWidgetProvider {

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
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context, CombinedWidgetProvider.class);
            for (int id : manager.getAppWidgetIds(provider)) {
                updateWidget(context, manager, id);
            }
        }
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_combined);

        Locale hijriLocale = Locale.forLanguageTag("ar-SA-u-ca-islamic-umalqura");
        SimpleDateFormat hijriFormat = new SimpleDateFormat("d MMMM yyyy 'هـ'", hijriLocale);
        views.setTextViewText(R.id.combinedHijriDate, hijriFormat.format(new Date()));

        PrayerTimesCalculator.Times t = PrayerTimesCalculator.calculateToday();
        views.setTextViewText(R.id.combinedFajrTime, t.fajr);
        views.setTextViewText(R.id.combinedSunriseTime, t.sunrise);
        views.setTextViewText(R.id.combinedDhuhrTime, t.dhuhr);
        views.setTextViewText(R.id.combinedAsrTime, t.asr);
        views.setTextViewText(R.id.combinedMaghribTime, t.maghrib);
        views.setTextViewText(R.id.combinedIshaTime, t.isha);

        WidgetStyle.applyBackgroundAndWatermark(
                context, views, R.id.combinedRoot, R.id.combinedThemeMark);

        int color = WidgetStyle.getTextColor(context);
        int secondary = WidgetStyle.withAlpha(color, 210);
        float scale = WidgetStyle.getFontScale(context);

        views.setTextColor(R.id.combinedGregorianDate, color);
        views.setTextColor(R.id.combinedHijriDate, color);
        views.setTextColor(R.id.combinedLocation, secondary);
        views.setTextColor(R.id.combinedCurrentTime, secondary);

        views.setTextViewTextSize(
                R.id.combinedGregorianDate, TypedValue.COMPLEX_UNIT_SP, 18f * scale);
        views.setTextViewTextSize(
                R.id.combinedHijriDate, TypedValue.COMPLEX_UNIT_SP, 18f * scale);
        views.setTextViewTextSize(
                R.id.combinedLocation, TypedValue.COMPLEX_UNIT_SP, 15f * scale);
        views.setTextViewTextSize(
                R.id.combinedCurrentTime, TypedValue.COMPLEX_UNIT_SP, 15f * scale);

        WidgetStyle.applyClockFormat(context, views, R.id.combinedCurrentTime);

        int[] labels = {
                R.id.combinedFajrLabel, R.id.combinedSunriseLabel, R.id.combinedDhuhrLabel,
                R.id.combinedAsrLabel, R.id.combinedMaghribLabel, R.id.combinedIshaLabel
        };
        int[] times = {
                R.id.combinedFajrTime, R.id.combinedSunriseTime, R.id.combinedDhuhrTime,
                R.id.combinedAsrTime, R.id.combinedMaghribTime, R.id.combinedIshaTime
        };

        for (int id : labels) {
            views.setTextColor(id, color);
            views.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, 14f * scale);
        }
        for (int id : times) {
            views.setTextColor(id, color);
            views.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, 16f * scale);
        }

        Intent openApp = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 300, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.combinedRoot, pendingIntent);

        manager.updateAppWidget(appWidgetId, views);
    }
}

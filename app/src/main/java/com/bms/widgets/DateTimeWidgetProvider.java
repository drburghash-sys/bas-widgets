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

public class DateTimeWidgetProvider extends AppWidgetProvider {

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
            ComponentName provider = new ComponentName(context, DateTimeWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(provider);
            for (int id : ids) updateWidget(context, manager, id);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_date_time);

        Locale hijriLocale = Locale.forLanguageTag("ar-SA-u-ca-islamic-umalqura");
        SimpleDateFormat hijriFormat = new SimpleDateFormat("d MMMM yyyy 'هـ'", hijriLocale);
        views.setTextViewText(R.id.hijriDate, hijriFormat.format(new Date()));

        WidgetStyle.applyBackgroundAndWatermark(context, views, R.id.widgetRoot, R.id.themeMark);

        int color = WidgetStyle.getTextColor(context);
        views.setTextColor(R.id.dayName, color);
        views.setTextColor(R.id.gregorianDate, color);
        views.setTextColor(R.id.hijriDate, WidgetStyle.withAlpha(color, 205));
        views.setTextColor(R.id.currentTime, color);

        float scale = WidgetStyle.getFontScale(context);
        views.setTextViewTextSize(R.id.dayName, TypedValue.COMPLEX_UNIT_SP, 18f * scale);
        views.setTextViewTextSize(R.id.gregorianDate, TypedValue.COMPLEX_UNIT_SP, 23f * scale);
        views.setTextViewTextSize(R.id.hijriDate, TypedValue.COMPLEX_UNIT_SP, 15f * scale);
        views.setTextViewTextSize(R.id.currentTime, TypedValue.COMPLEX_UNIT_SP, 30f * scale);

        WidgetStyle.applyClockFormat(context, views, R.id.currentTime);

        Intent openApp = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 100, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);

        manager.updateAppWidget(appWidgetId, views);
    }
}

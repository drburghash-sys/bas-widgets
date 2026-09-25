package com.bms.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.TypedValue;
import android.widget.RemoteViews;

public class BasPlatformWidgetProvider extends AppWidgetProvider {

    private static final String BAS_PACKAGE = "com.drburghash.basfinal";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_bas_platform);

        WidgetStyle.applyBackgroundAndWatermark(
                context, views, R.id.basWidgetRoot, R.id.basThemeMark);

        int color = WidgetStyle.getTextColor(context);
        float scale = WidgetStyle.getFontScale(context);

        views.setTextColor(R.id.basTitle, color);
        views.setTextColor(R.id.basSubtitle, WidgetStyle.withAlpha(color, 205));
        views.setTextColor(R.id.basHint, WidgetStyle.withAlpha(color, 170));

        views.setTextViewTextSize(R.id.basTitle, TypedValue.COMPLEX_UNIT_SP, 22f * scale);
        views.setTextViewTextSize(R.id.basSubtitle, TypedValue.COMPLEX_UNIT_SP, 13f * scale);
        views.setTextViewTextSize(R.id.basHint, TypedValue.COMPLEX_UNIT_SP, 11f * scale);

        Intent launchIntent = null;
        PackageManager pm = context.getPackageManager();
        try {
            launchIntent = pm.getLaunchIntentForPackage(BAS_PACKAGE);
        } catch (Exception ignored) { }

        if (launchIntent == null) {
            launchIntent = new Intent(context, MainActivity.class);
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                400,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.basWidgetRoot, pendingIntent);

        manager.updateAppWidget(appWidgetId, views);
    }
}

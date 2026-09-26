package com.bms.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OnCallWidgetProvider extends AppWidgetProvider {
    private static final ZoneId ZONE = ZoneId.of("Asia/Riyadh");

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateWidget(context, manager, id);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context, OnCallWidgetProvider.class);
            for (int id : manager.getAppWidgetIds(provider)) updateWidget(context, manager, id);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_oncall);
        LocalDate today = LocalDate.now(ZONE);
        LocalDate tomorrow = today.plusDays(1);

        DateTimeFormatter dateFormat =
                DateTimeFormatter.ofPattern("EEEE، d MMMM", new Locale("ar", "SA"));
        v.setTextViewText(R.id.oncallDate, today.format(dateFormat));

        OnCallSchedule.Entry e = OnCallSchedule.forDate(context, today);
        OnCallSchedule.Entry next = OnCallSchedule.forDate(context, tomorrow);

        if (e == null) {
            v.setTextViewText(R.id.oncallConsultant, "لا يوجد جدول لهذا اليوم");
            v.setTextViewText(R.id.oncallResidentDay, "—");
            v.setTextViewText(R.id.oncallResidentNight, "—");
            v.setTextViewText(R.id.oncallSpecialistDay, "—");
            v.setTextViewText(R.id.oncallSpecialistNight, "—");
        } else {
            v.setTextViewText(R.id.oncallConsultant, e.consultant);
            v.setTextViewText(R.id.oncallResidentDay, e.residentDay);
            v.setTextViewText(R.id.oncallResidentNight, e.residentNight);
            v.setTextViewText(R.id.oncallSpecialistDay, e.specialistDay);
            v.setTextViewText(R.id.oncallSpecialistNight, e.specialistNight);
        }

        String tomorrowText = next == null
                ? "استشاري الغد: —"
                : "استشاري الغد: " + next.consultant;
        v.setTextViewText(R.id.oncallTomorrow, tomorrowText);

        WidgetStyle.applyBackgroundAndWatermark(
                context, v, R.id.oncallRoot, R.id.oncallThemeMark);

        int color = WidgetStyle.getTextColor(context);
        int secondary = WidgetStyle.withAlpha(color, 205);
        float scale = WidgetStyle.getFontScale(context);

        int[] primaryIds = {
                R.id.oncallTitle, R.id.oncallConsultant,
                R.id.oncallResidentDay, R.id.oncallResidentNight,
                R.id.oncallSpecialistDay, R.id.oncallSpecialistNight
        };
        int[] secondaryIds = {
                R.id.oncallDate, R.id.oncallConsultantLabel,
                R.id.oncallResidentDayLabel, R.id.oncallResidentNightLabel,
                R.id.oncallSpecialistDayLabel, R.id.oncallSpecialistNightLabel,
                R.id.oncallTomorrow
        };

        for (int id : primaryIds) v.setTextColor(id, color);
        for (int id : secondaryIds) v.setTextColor(id, secondary);

        v.setTextViewTextSize(R.id.oncallTitle, TypedValue.COMPLEX_UNIT_SP, 18f * scale);
        v.setTextViewTextSize(R.id.oncallDate, TypedValue.COMPLEX_UNIT_SP, 12f * scale);
        v.setTextViewTextSize(R.id.oncallConsultantLabel, TypedValue.COMPLEX_UNIT_SP, 12f * scale);
        v.setTextViewTextSize(R.id.oncallConsultant, TypedValue.COMPLEX_UNIT_SP, 18f * scale);
        v.setTextViewTextSize(R.id.oncallTomorrow, TypedValue.COMPLEX_UNIT_SP, 11f * scale);

        int[] roleLabels = {
                R.id.oncallResidentDayLabel, R.id.oncallResidentNightLabel,
                R.id.oncallSpecialistDayLabel, R.id.oncallSpecialistNightLabel
        };
        int[] roleValues = {
                R.id.oncallResidentDay, R.id.oncallResidentNight,
                R.id.oncallSpecialistDay, R.id.oncallSpecialistNight
        };
        for (int id : roleLabels)
            v.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, 11f * scale);
        for (int id : roleValues)
            v.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, 13f * scale);

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                context, 500, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.oncallRoot, pi);

        manager.updateAppWidget(appWidgetId, v);
    }
}

package com.bms.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

public class TaskWidgetProvider extends AppWidgetProvider {
    private static final String HOME_ORGANIZER_PACKAGE = "com.drburghash.homehubx";

    private static final int[] TODAY_ROWS = {
            R.id.taskTodayRow1, R.id.taskTodayRow2, R.id.taskTodayRow3
    };
    private static final int[] TODAY_TITLES = {
            R.id.taskTodayTitle1, R.id.taskTodayTitle2, R.id.taskTodayTitle3
    };
    private static final int[] TODAY_META = {
            R.id.taskTodayMeta1, R.id.taskTodayMeta2, R.id.taskTodayMeta3
    };
    private static final int[] LATE_ROWS = {
            R.id.taskLateRow1, R.id.taskLateRow2, R.id.taskLateRow3
    };
    private static final int[] LATE_TITLES = {
            R.id.taskLateTitle1, R.id.taskLateTitle2, R.id.taskLateTitle3
    };
    private static final int[] LATE_META = {
            R.id.taskLateMeta1, R.id.taskLateMeta2, R.id.taskLateMeta3
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateWidget(context, manager, id);
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_tasks);
        WidgetStyle.applyBackgroundAndWatermark(
                context, v, R.id.taskWidgetRoot, R.id.taskThemeMark);

        int color = WidgetStyle.getTextColor(context);
        int secondary = WidgetStyle.withAlpha(color, 190);
        float scale = WidgetStyle.getFontScale(context);

        v.setTextColor(R.id.taskWidgetTitle, color);
        v.setTextColor(R.id.taskWidgetCount, secondary);
        v.setTextColor(R.id.taskTodayHeader, color);
        v.setTextColor(R.id.taskLateHeader, color);
        v.setTextColor(R.id.taskSyncHint, secondary);
        v.setTextViewTextSize(R.id.taskWidgetTitle, TypedValue.COMPLEX_UNIT_SP, 18f * scale);
        v.setTextViewTextSize(R.id.taskWidgetCount, TypedValue.COMPLEX_UNIT_SP, 12f * scale);
        v.setTextViewTextSize(R.id.taskTodayHeader, TypedValue.COMPLEX_UNIT_SP, 13f * scale);
        v.setTextViewTextSize(R.id.taskLateHeader, TypedValue.COMPLEX_UNIT_SP, 13f * scale);
        v.setTextViewTextSize(R.id.taskSyncHint, TypedValue.COMPLEX_UNIT_SP, 11f * scale);

        PendingIntent openMofakkirati = openHomeOrganizerPendingIntent(context);
        v.setOnClickPendingIntent(R.id.taskWidgetHeader, openMofakkirati);

        if (!MofakkiratiStore.hasSnapshot(context)) {
            v.setTextViewText(R.id.taskWidgetCount, "بانتظار المزامنة");
            v.setTextViewText(R.id.taskTodayHeader, "اليوم");
            v.setTextViewText(R.id.taskLateHeader, "المتأخرة");
            v.setTextViewText(R.id.taskSyncHint, "افتح «مفكرتي» مرة واحدة لتظهر المهام هنا");
            v.setViewVisibility(R.id.taskSyncHint, View.VISIBLE);
            hideRows(v, TODAY_ROWS);
            hideRows(v, LATE_ROWS);
            manager.updateAppWidget(appWidgetId, v);
            return;
        }

        List<MofakkiratiStore.Item> today = MofakkiratiStore.today(context);
        List<MofakkiratiStore.Item> overdue = MofakkiratiStore.overdue(context);

        v.setTextViewText(R.id.taskWidgetCount,
                "اليوم " + today.size() + "  ·  متأخر " + overdue.size());
        v.setTextViewText(R.id.taskTodayHeader, "اليوم (" + today.size() + ")");
        v.setTextViewText(R.id.taskLateHeader, "المتأخرة (" + overdue.size() + ")");
        v.setViewVisibility(R.id.taskSyncHint, View.GONE);

        Bundle options = manager.getAppWidgetOptions(appWidgetId);
        int minHeight = options == null ? 220 :
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 220);
        int maxPerSection = minHeight >= 260 ? 3 : (minHeight >= 190 ? 2 : 1);

        bindRows(v, today, TODAY_ROWS, TODAY_TITLES, TODAY_META,
                maxPerSection, color, secondary, scale, true, openMofakkirati);
        bindRows(v, overdue, LATE_ROWS, LATE_TITLES, LATE_META,
                maxPerSection, color, secondary, scale, false, openMofakkirati);

        manager.updateAppWidget(appWidgetId, v);
    }

    private static void bindRows(
            RemoteViews v,
            List<MofakkiratiStore.Item> tasks,
            int[] rows,
            int[] titles,
            int[] meta,
            int maxVisible,
            int color,
            int secondary,
            float scale,
            boolean today,
            PendingIntent openMofakkirati) {
        for (int i = 0; i < rows.length; i++) {
            if (i < tasks.size() && i < maxVisible) {
                MofakkiratiStore.Item task = tasks.get(i);
                v.setViewVisibility(rows[i], View.VISIBLE);
                v.setTextViewText(titles[i], typeIcon(task.type) + " " + task.title);

                String detail = today
                        ? (task.time.isEmpty() ? typeLabel(task.type) : task.time)
                        : task.effectiveDate() + (task.time.isEmpty() ? "" : " · " + task.time);

                v.setTextViewText(meta[i], detail);
                v.setTextColor(titles[i], color);
                v.setTextColor(meta[i], secondary);
                v.setTextViewTextSize(titles[i], TypedValue.COMPLEX_UNIT_SP, 14f * scale);
                v.setTextViewTextSize(meta[i], TypedValue.COMPLEX_UNIT_SP, 11f * scale);
                v.setOnClickPendingIntent(rows[i], openMofakkirati);
            } else {
                v.setViewVisibility(rows[i], View.GONE);
            }
        }
    }

    private static void hideRows(RemoteViews v, int[] rows) {
        for (int id : rows) v.setViewVisibility(id, View.GONE);
    }

    private static PendingIntent openHomeOrganizerPendingIntent(Context context) {
        Intent launch = context.getPackageManager()
                .getLaunchIntentForPackage(HOME_ORGANIZER_PACKAGE);
        if (launch == null) launch = new Intent(context, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        return PendingIntent.getActivity(
                context,
                8601,
                launch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static String typeIcon(String type) {
        if ("appointment".equals(type)) return "📅";
        if ("followup".equals(type)) return "🔁";
        return "✓";
    }

    private static String typeLabel(String type) {
        if ("appointment".equals(type)) return "موعد";
        if ("followup".equals(type)) return "متابعة";
        return "مهمة";
    }
}

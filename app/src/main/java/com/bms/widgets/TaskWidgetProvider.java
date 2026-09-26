package com.bms.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

public class TaskWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_COMPLETE = "com.bms.widgets.COMPLETE_TASK";
    public static final String EXTRA_TASK_ID = "task_id";

    private static final int[] ROWS = {
            R.id.taskRow1, R.id.taskRow2, R.id.taskRow3
    };
    private static final int[] TIMES = {
            R.id.taskTime1, R.id.taskTime2, R.id.taskTime3
    };
    private static final int[] TITLES = {
            R.id.taskTitle1, R.id.taskTitle2, R.id.taskTitle3
    };
    private static final int[] DONE = {
            R.id.taskDone1, R.id.taskDone2, R.id.taskDone3
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateWidget(context, manager, id);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_COMPLETE.equals(intent.getAction())) {
            String taskId = intent.getStringExtra(EXTRA_TASK_ID);
            if (taskId != null) TaskStore.completeToday(context, taskId);

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context, TaskWidgetProvider.class);
            for (int id : manager.getAppWidgetIds(provider)) updateWidget(context, manager, id);
        }
        super.onReceive(context, intent);
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
        v.setTextViewTextSize(R.id.taskWidgetTitle, TypedValue.COMPLEX_UNIT_SP, 18f * scale);
        v.setTextViewTextSize(R.id.taskWidgetCount, TypedValue.COMPLEX_UNIT_SP, 12f * scale);

        List<TaskStore.Task> pending = TaskStore.getPendingToday(context);
        v.setTextViewText(R.id.taskWidgetCount,
                pending.isEmpty() ? "تم إنجاز جميع مهام اليوم" :
                        "متبقي " + pending.size());

        for (int i = 0; i < 3; i++) {
            if (i < pending.size()) {
                TaskStore.Task task = pending.get(i);
                v.setViewVisibility(ROWS[i], View.VISIBLE);
                v.setTextViewText(TIMES[i], task.time);
                v.setTextViewText(TITLES[i], task.title);
                v.setTextColor(TIMES[i], secondary);
                v.setTextColor(TITLES[i], color);
                v.setTextColor(DONE[i], color);
                v.setTextViewTextSize(TIMES[i], TypedValue.COMPLEX_UNIT_SP, 12f * scale);
                v.setTextViewTextSize(TITLES[i], TypedValue.COMPLEX_UNIT_SP, 15f * scale);
                v.setTextViewTextSize(DONE[i], TypedValue.COMPLEX_UNIT_SP, 18f * scale);

                Intent complete = new Intent(context, TaskWidgetProvider.class);
                complete.setAction(ACTION_COMPLETE);
                complete.putExtra(EXTRA_TASK_ID, task.id);
                PendingIntent pi = PendingIntent.getBroadcast(
                        context,
                        7000 + Math.abs(task.id.hashCode() % 100000),
                        complete,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                v.setOnClickPendingIntent(DONE[i], pi);
            } else {
                v.setViewVisibility(ROWS[i], View.GONE);
            }
        }

        Intent open = new Intent(context, TaskManagerActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(
                context, 600,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.taskWidgetHeader, openPi);

        manager.updateAppWidget(appWidgetId, v);
    }
}

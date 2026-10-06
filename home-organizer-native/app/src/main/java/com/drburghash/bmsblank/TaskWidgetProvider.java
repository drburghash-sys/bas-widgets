package com.drburghash.bmsblank;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import java.util.List;

public class TaskWidgetProvider extends AppWidgetProvider {
    private static final int[] TODAY_IDS = {R.id.today1, R.id.today2, R.id.today3};
    private static final int[] LATE_IDS = {R.id.late1, R.id.late2, R.id.late3};

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) update(context, manager, id);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String a = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(a) || Intent.ACTION_TIME_CHANGED.equals(a)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) {
            refreshAll(context);
        }
    }

    public static void refreshAll(Context context) {
        AppWidgetManager m = AppWidgetManager.getInstance(context);
        android.content.ComponentName c = new android.content.ComponentName(context, TaskWidgetProvider.class);
        for (int id : m.getAppWidgetIds(c)) update(context, m, id);
    }

    public static void update(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_tasks);
        List<TaskSnapshotStore.Item> today = TaskSnapshotStore.today(context);
        List<TaskSnapshotStore.Item> late = TaskSnapshotStore.overdue(context);

        v.setTextViewText(R.id.counts, "اليوم " + today.size() + " · متأخر " + late.size());
        v.setTextViewText(R.id.todayHeader, "اليوم (" + today.size() + ")");
        v.setTextViewText(R.id.lateHeader, "المتأخرة (" + late.size() + ")");

        bind(v, TODAY_IDS, today, true);
        bind(v, LATE_IDS, late, false);
        v.setViewVisibility(R.id.emptyHint,
                TaskSnapshotStore.hasData(context) ? View.GONE : View.VISIBLE);

        Intent open = new Intent(context, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 7401, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.header, pi);
        v.setOnClickPendingIntent(R.id.root, pi);

        manager.updateAppWidget(widgetId, v);
    }

    private static void bind(RemoteViews v, int[] ids, List<TaskSnapshotStore.Item> items, boolean today) {
        for (int i = 0; i < ids.length; i++) {
            if (i >= items.size()) {
                v.setViewVisibility(ids[i], View.GONE);
                continue;
            }
            TaskSnapshotStore.Item x = items.get(i);
            v.setViewVisibility(ids[i], View.VISIBLE);
            String icon = "appointment".equals(x.type) ? "📅 " : "followup".equals(x.type) ? "🔁 " : "• ";
            String meta = today
                    ? (x.time.isEmpty() ? "" : "  " + x.time)
                    : "  " + x.dueDate();
            v.setTextViewText(ids[i], icon + x.title + meta);
        }
    }
}

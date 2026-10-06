package com.drburghash.bmsblank;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TaskSnapshotStore {
    private static final String PREFS = "home_organizer_widget";
    private static final String KEY = "mofakkirati_snapshot_v1";

    private TaskSnapshotStore() {}

    public static final class Item {
        public final String title;
        public final String type;
        public final String date;
        public final String autoDate;
        public final String time;
        public final int priority;
        public final boolean done;
        public final boolean deleted;

        Item(String title, String type, String date, String autoDate, String time,
             int priority, boolean done, boolean deleted) {
            this.title = title;
            this.type = type;
            this.date = date == null ? "" : date;
            this.autoDate = autoDate == null ? "" : autoDate;
            this.time = time == null ? "" : time;
            this.priority = priority;
            this.done = done;
            this.deleted = deleted;
        }

        String dueDate() { return !date.isEmpty() ? date : autoDate; }
        boolean visibleWork() {
            return !done && !deleted && !"idea".equals(type) && !"topic".equals(type);
        }
    }

    public static void save(Context context, String json) {
        try {
            JSONObject root = new JSONObject(json);
            if (root.optJSONArray("items") == null) return;
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putString(KEY, root.toString()).apply();
        } catch (Exception ignored) {}
    }

    public static boolean hasData(Context context) {
        return !context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, "").isEmpty();
    }

    public static List<Item> today(Context context) {
        String today = LocalDate.now().toString();
        List<Item> out = new ArrayList<>();
        for (Item x : all(context)) {
            if (x.visibleWork() && today.equals(x.dueDate())) out.add(x);
        }
        out.sort(Comparator
                .comparing((Item x) -> x.time.isEmpty() ? "99:99" : x.time)
                .thenComparing((Item x) -> -x.priority));
        return out;
    }

    public static List<Item> overdue(Context context) {
        String today = LocalDate.now().toString();
        List<Item> out = new ArrayList<>();
        for (Item x : all(context)) {
            String d = x.dueDate();
            if (x.visibleWork() && !d.isEmpty() && d.compareTo(today) < 0) out.add(x);
        }
        out.sort(Comparator
                .comparing(Item::dueDate)
                .thenComparing((Item x) -> -x.priority));
        return out;
    }

    private static List<Item> all(Context context) {
        List<Item> out = new ArrayList<>();
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, "");
        if (raw.isEmpty()) return out;
        try {
            JSONArray items = new JSONObject(raw).optJSONArray("items");
            if (items == null) return out;
            for (int i = 0; i < items.length(); i++) {
                JSONObject x = items.optJSONObject(i);
                if (x == null) continue;
                out.add(new Item(
                        x.optString("title", ""),
                        x.optString("type", "task"),
                        x.optString("date", ""),
                        x.optString("autoDate", ""),
                        x.optString("time", ""),
                        x.optInt("priority", 1),
                        x.optBoolean("done", false),
                        x.optBoolean("deleted", false)
                ));
            }
        } catch (Exception ignored) {}
        return out;
    }
}

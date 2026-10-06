package com.bms.widgets;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MofakkiratiStore {
    private static final String PREFS = "bms_widget_prefs";
    private static final String KEY_SNAPSHOT = "mofakkirati_synced_tasks_v1";
    private static final String KEY_SYNCED_AT = "mofakkirati_synced_at_v1";

    private MofakkiratiStore() {}

    public static final class Item {
        public final String id;
        public final String type;
        public final String title;
        public final String date;
        public final String autoDate;
        public final String time;
        public final int priority;
        public final boolean done;
        public final boolean deleted;

        Item(String id, String type, String title, String date, String autoDate,
             String time, int priority, boolean done, boolean deleted) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.date = date == null ? "" : date;
            this.autoDate = autoDate == null ? "" : autoDate;
            this.time = time == null ? "" : time;
            this.priority = priority;
            this.done = done;
            this.deleted = deleted;
        }

        public String effectiveDate() {
            return !date.isEmpty() ? date : autoDate;
        }

        public boolean isOpenWork() {
            return !done && !deleted && !"idea".equals(type) && !"topic".equals(type);
        }
    }

    public static void saveSnapshot(Context context, String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray items = root.optJSONArray("items");
        if (items == null) throw new IllegalArgumentException("items missing");

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SNAPSHOT, root.toString())
                .putLong(KEY_SYNCED_AT, System.currentTimeMillis())
                .apply();
    }

    public static boolean hasSnapshot(Context context) {
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_SNAPSHOT, "");
        return raw != null && !raw.isEmpty();
    }

    public static long syncedAt(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getLong(KEY_SYNCED_AT, 0L);
    }

    public static List<Item> today(Context context) {
        String today = LocalDate.now().toString();
        List<Item> out = new ArrayList<>();
        for (Item item : all(context)) {
            if (item.isOpenWork() && today.equals(item.effectiveDate())) out.add(item);
        }
        out.sort((a, b) -> {
            int timeCmp = sortTime(a.time).compareTo(sortTime(b.time));
            if (timeCmp != 0) return timeCmp;
            return Integer.compare(b.priority, a.priority);
        });
        return out;
    }

    public static List<Item> overdue(Context context) {
        String today = LocalDate.now().toString();
        List<Item> out = new ArrayList<>();
        for (Item item : all(context)) {
            String due = item.effectiveDate();
            if (item.isOpenWork() && !due.isEmpty() && due.compareTo(today) < 0) out.add(item);
        }
        out.sort((a, b) -> {
            int priorityCmp = Integer.compare(b.priority, a.priority);
            if (priorityCmp != 0) return priorityCmp;
            int dateCmp = a.effectiveDate().compareTo(b.effectiveDate());
            if (dateCmp != 0) return dateCmp;
            return sortTime(a.time).compareTo(sortTime(b.time));
        });
        return out;
    }

    private static List<Item> all(Context context) {
        List<Item> out = new ArrayList<>();
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_SNAPSHOT, "");
        if (raw == null || raw.isEmpty()) return out;

        try {
            JSONObject root = new JSONObject(raw);
            JSONArray items = root.optJSONArray("items");
            if (items == null) return out;

            for (int i = 0; i < items.length(); i++) {
                JSONObject x = items.optJSONObject(i);
                if (x == null) continue;
                out.add(new Item(
                        x.optString("id", ""),
                        x.optString("type", "task"),
                        x.optString("title", ""),
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

    private static String sortTime(String time) {
        if (time == null || time.trim().isEmpty()) return "99:99";
        try {
            String[] p = time.trim().split(":");
            int h = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            return String.format(Locale.US, "%02d:%02d", h, m);
        } catch (Exception ignored) {
            return "99:99";
        }
    }
}

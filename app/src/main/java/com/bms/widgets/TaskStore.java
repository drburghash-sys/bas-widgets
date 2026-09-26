package com.bms.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class TaskStore {
    private static final String PREFS = "bms_widget_prefs";
    private static final String KEY_TASKS = "daily_tasks_v1";

    private TaskStore() {}

    public static final class Task {
        public final String id;
        public final String time;
        public final String title;
        public final String doneDate;

        public Task(String id, String time, String title, String doneDate) {
            this.id = id;
            this.time = time;
            this.title = title;
            this.doneDate = doneDate == null ? "" : doneDate;
        }

        public boolean isDoneToday() {
            return LocalDate.now().toString().equals(doneDate);
        }
    }

    public static List<Task> getAll(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = p.getString(KEY_TASKS, "");
        List<Task> out = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return out;

        for (String line : raw.split("\n")) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split("\|", -1);
            if (parts.length < 4) continue;
            try {
                String title = new String(
                        Base64.decode(parts[2], Base64.NO_WRAP),
                        StandardCharsets.UTF_8);
                out.add(new Task(parts[0], parts[1], title, parts[3]));
            } catch (Exception ignored) {}
        }

        out.sort(Comparator.comparing(t -> safeTime(t.time)));
        return out;
    }

    public static List<Task> getPendingToday(Context context) {
        List<Task> out = new ArrayList<>();
        for (Task t : getAll(context)) {
            if (!t.isDoneToday()) out.add(t);
        }
        return out;
    }

    public static void add(Context context, String time, String title) {
        List<Task> all = getAll(context);
        all.add(new Task(UUID.randomUUID().toString(), normalizeTime(time), title.trim(), ""));
        saveAll(context, all);
    }

    public static void delete(Context context, String id) {
        List<Task> all = getAll(context);
        all.removeIf(t -> t.id.equals(id));
        saveAll(context, all);
    }

    public static void completeToday(Context context, String id) {
        List<Task> all = getAll(context);
        List<Task> updated = new ArrayList<>();
        String today = LocalDate.now().toString();
        for (Task t : all) {
            if (t.id.equals(id)) updated.add(new Task(t.id, t.time, t.title, today));
            else updated.add(t);
        }
        saveAll(context, updated);
    }

    public static void undoToday(Context context, String id) {
        List<Task> all = getAll(context);
        List<Task> updated = new ArrayList<>();
        for (Task t : all) {
            if (t.id.equals(id)) updated.add(new Task(t.id, t.time, t.title, ""));
            else updated.add(t);
        }
        saveAll(context, updated);
    }

    private static void saveAll(Context context, List<Task> tasks) {
        tasks.sort(Comparator.comparing(t -> safeTime(t.time)));
        StringBuilder sb = new StringBuilder();
        for (Task t : tasks) {
            String encoded = Base64.encodeToString(
                    t.title.getBytes(StandardCharsets.UTF_8),
                    Base64.NO_WRAP);
            sb.append(t.id).append("|")
                    .append(normalizeTime(t.time)).append("|")
                    .append(encoded).append("|")
                    .append(t.doneDate == null ? "" : t.doneDate)
                    .append("\n");
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_TASKS, sb.toString()).apply();
    }

    private static String normalizeTime(String time) {
        try {
            LocalTime t = LocalTime.parse(time);
            return String.format(Locale.US, "%02d:%02d", t.getHour(), t.getMinute());
        } catch (Exception ignored) {
            return "12:00";
        }
    }

    private static LocalTime safeTime(String time) {
        try { return LocalTime.parse(normalizeTime(time)); }
        catch (Exception e) { return LocalTime.NOON; }
    }
}

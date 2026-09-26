package com.bms.widgets;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public final class OnCallSchedule {
    private static final String PREFS = "bms_widget_prefs";
    public static final String KEY_TEXT = "oncall_rota_text";

    private OnCallSchedule() {}

    public static final class Entry {
        public final String residentDay;
        public final String residentNight;
        public final String specialistDay;
        public final String specialistNight;
        public final String consultant;

        Entry(String residentDay, String residentNight,
              String specialistDay, String specialistNight,
              String consultant) {
            this.residentDay = clean(residentDay);
            this.residentNight = clean(residentNight);
            this.specialistDay = clean(specialistDay);
            this.specialistNight = clean(specialistNight);
            this.consultant = clean(consultant);
        }
    }

    public static Entry forDate(Context context, LocalDate date) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return parse(p.getString(KEY_TEXT, "")).get(date);
    }

    public static boolean hasAnyData(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return !p.getString(KEY_TEXT, "").trim().isEmpty();
    }

    public static Map<LocalDate, Entry> parse(String raw) {
        Map<LocalDate, Entry> result = new HashMap<>();
        if (raw == null || raw.trim().isEmpty()) return result;

        YearMonth currentMonth = null;
        String[] lines = raw.replace("\r", "").split("\n");

        for (String original : lines) {
            String line = normalizeDigits(original).trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("الشهر") || line.toLowerCase().startsWith("month")) {
                String value = line.contains(":")
                        ? line.substring(line.indexOf(':') + 1).trim()
                        : line.replaceAll("[^0-9-]", "");
                currentMonth = parseYearMonth(value);
                continue;
            }

            if (!line.contains("|")) continue;
            String[] c = line.split("\\s*\\|\\s*", -1);
            if (c.length < 2) continue;

            LocalDate date = parseDateToken(c[0], currentMonth);
            if (date == null) continue;

            String residentDay = value(c, 1);
            String residentNight = value(c, 2);
            String specialistDay = value(c, 3);
            String specialistNight = value(c, 4);
            String consultant = value(c, 5);

            result.put(date, new Entry(
                    residentDay, residentNight,
                    specialistDay, specialistNight,
                    consultant));
        }
        return result;
    }

    private static LocalDate parseDateToken(String token, YearMonth currentMonth) {
        String t = token.trim();
        try {
            if (t.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                String[] p = t.split("-");
                return LocalDate.of(
                        Integer.parseInt(p[0]),
                        Integer.parseInt(p[1]),
                        Integer.parseInt(p[2]));
            }
            String onlyDigits = t.replaceAll("[^0-9]", "");
            if (!onlyDigits.isEmpty() && currentMonth != null) {
                int day = Integer.parseInt(onlyDigits);
                if (day >= 1 && day <= currentMonth.lengthOfMonth()) {
                    return currentMonth.atDay(day);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static YearMonth parseYearMonth(String value) {
        try {
            String cleaned = value.trim().replace('/', '-');
            String[] p = cleaned.split("-");
            if (p.length >= 2) {
                int year = Integer.parseInt(p[0].replaceAll("[^0-9]", ""));
                int month = Integer.parseInt(p[1].replaceAll("[^0-9]", ""));
                return YearMonth.of(year, month);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String value(String[] c, int i) {
        return i < c.length ? c[i] : "";
    }

    private static String clean(String s) {
        if (s == null) return "—";
        String v = s.trim();
        return v.isEmpty() || v.equals("-") ? "—" : v;
    }

    private static String normalizeDigits(String s) {
        if (s == null) return "";
        String ar = "٠١٢٣٤٥٦٧٨٩";
        String fa = "۰۱۲۳۴۵۶۷۸۹";
        StringBuilder out = new StringBuilder(s.length());
        for (char ch : s.toCharArray()) {
            int i = ar.indexOf(ch);
            if (i >= 0) out.append((char) ('0' + i));
            else {
                i = fa.indexOf(ch);
                if (i >= 0) out.append((char) ('0' + i));
                else out.append(ch);
            }
        }
        return out.toString();
    }
}

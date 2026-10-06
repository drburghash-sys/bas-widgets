package com.bms.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class CalendarWidgetProvider extends AppWidgetProvider {
    private static final String PREFS = "bms_widget_prefs";
    private static final String ACTION_TOGGLE = "com.bms.widgets.CALENDAR_TOGGLE";
    private static final String ACTION_PREV = "com.bms.widgets.CALENDAR_PREV";
    private static final String ACTION_NEXT = "com.bms.widgets.CALENDAR_NEXT";
    private static final String ACTION_TODAY = "com.bms.widgets.CALENDAR_TODAY";

    private static final int[] CELLS = {R.id.calCell1, R.id.calCell2, R.id.calCell3, R.id.calCell4, R.id.calCell5, R.id.calCell6, R.id.calCell7, R.id.calCell8, R.id.calCell9, R.id.calCell10, R.id.calCell11, R.id.calCell12, R.id.calCell13, R.id.calCell14, R.id.calCell15, R.id.calCell16, R.id.calCell17, R.id.calCell18, R.id.calCell19, R.id.calCell20, R.id.calCell21, R.id.calCell22, R.id.calCell23, R.id.calCell24, R.id.calCell25, R.id.calCell26, R.id.calCell27, R.id.calCell28, R.id.calCell29, R.id.calCell30, R.id.calCell31, R.id.calCell32, R.id.calCell33, R.id.calCell34, R.id.calCell35, R.id.calCell36, R.id.calCell37, R.id.calCell38, R.id.calCell39, R.id.calCell40, R.id.calCell41, R.id.calCell42};
    private static final int[] PRIMARY = {R.id.calPrimary1, R.id.calPrimary2, R.id.calPrimary3, R.id.calPrimary4, R.id.calPrimary5, R.id.calPrimary6, R.id.calPrimary7, R.id.calPrimary8, R.id.calPrimary9, R.id.calPrimary10, R.id.calPrimary11, R.id.calPrimary12, R.id.calPrimary13, R.id.calPrimary14, R.id.calPrimary15, R.id.calPrimary16, R.id.calPrimary17, R.id.calPrimary18, R.id.calPrimary19, R.id.calPrimary20, R.id.calPrimary21, R.id.calPrimary22, R.id.calPrimary23, R.id.calPrimary24, R.id.calPrimary25, R.id.calPrimary26, R.id.calPrimary27, R.id.calPrimary28, R.id.calPrimary29, R.id.calPrimary30, R.id.calPrimary31, R.id.calPrimary32, R.id.calPrimary33, R.id.calPrimary34, R.id.calPrimary35, R.id.calPrimary36, R.id.calPrimary37, R.id.calPrimary38, R.id.calPrimary39, R.id.calPrimary40, R.id.calPrimary41, R.id.calPrimary42};
    private static final int[] SECONDARY = {R.id.calSecondary1, R.id.calSecondary2, R.id.calSecondary3, R.id.calSecondary4, R.id.calSecondary5, R.id.calSecondary6, R.id.calSecondary7, R.id.calSecondary8, R.id.calSecondary9, R.id.calSecondary10, R.id.calSecondary11, R.id.calSecondary12, R.id.calSecondary13, R.id.calSecondary14, R.id.calSecondary15, R.id.calSecondary16, R.id.calSecondary17, R.id.calSecondary18, R.id.calSecondary19, R.id.calSecondary20, R.id.calSecondary21, R.id.calSecondary22, R.id.calSecondary23, R.id.calSecondary24, R.id.calSecondary25, R.id.calSecondary26, R.id.calSecondary27, R.id.calSecondary28, R.id.calSecondary29, R.id.calSecondary30, R.id.calSecondary31, R.id.calSecondary32, R.id.calSecondary33, R.id.calSecondary34, R.id.calSecondary35, R.id.calSecondary36, R.id.calSecondary37, R.id.calSecondary38, R.id.calSecondary39, R.id.calSecondary40, R.id.calSecondary41, R.id.calSecondary42};
    private static final int[] DOW = {R.id.calDow1,R.id.calDow2,R.id.calDow3,R.id.calDow4,R.id.calDow5,R.id.calDow6,R.id.calDow7};

    private static final String[] GREG_MONTHS = {
            "يناير","فبراير","مارس","أبريل","مايو","يونيو",
            "يوليو","أغسطس","سبتمبر","أكتوبر","نوفمبر","ديسمبر"
    };
    private static final String[] HIJRI_MONTHS = {
            "محرم","صفر","ربيع الأول","ربيع الآخر","جمادى الأولى","جمادى الآخرة",
            "رجب","شعبان","رمضان","شوال","ذو القعدة","ذو الحجة"
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateWidget(context, manager, id);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action == null) return;
        if (!ACTION_TOGGLE.equals(action) && !ACTION_PREV.equals(action)
                && !ACTION_NEXT.equals(action) && !ACTION_TODAY.equals(action)) return;

        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return;

        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean hijri = p.getBoolean("cal_hijri_" + widgetId, false);
        int offset = p.getInt("cal_offset_" + widgetId, 0);

        if (ACTION_TOGGLE.equals(action)) {
            hijri = !hijri;
            offset = 0;
        } else if (ACTION_PREV.equals(action)) {
            offset = Math.max(-120, offset - 1);
        } else if (ACTION_NEXT.equals(action)) {
            offset = Math.min(120, offset + 1);
        } else if (ACTION_TODAY.equals(action)) {
            offset = 0;
        }

        p.edit()
                .putBoolean("cal_hijri_" + widgetId, hijri)
                .putInt("cal_offset_" + widgetId, offset)
                .apply();

        updateWidget(context, AppWidgetManager.getInstance(context), widgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences.Editor e = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        for (int id : appWidgetIds) {
            e.remove("cal_hijri_" + id);
            e.remove("cal_offset_" + id);
        }
        e.apply();
        super.onDeleted(context, appWidgetIds);
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean hijriMode = p.getBoolean("cal_hijri_" + widgetId, false);
        int offset = p.getInt("cal_offset_" + widgetId, 0);
        int theme = p.getInt("calendar_theme_index", 0);

        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_calendar);
        applyTheme(v, theme);

        LocalDate today = LocalDate.now();

        if (hijriMode) {
            renderHijri(v, today, offset, theme);
        } else {
            renderGregorian(v, today, offset, theme);
        }

        v.setTextViewText(R.id.calToggle, hijriMode ? "م ⇄ هـ" : "هـ ⇄ م");
        v.setOnClickPendingIntent(R.id.calToggle, actionPi(context, widgetId, ACTION_TOGGLE, 1));
        v.setOnClickPendingIntent(R.id.calPrev, actionPi(context, widgetId, ACTION_PREV, 2));
        v.setOnClickPendingIntent(R.id.calNext, actionPi(context, widgetId, ACTION_NEXT, 3));
        v.setOnClickPendingIntent(R.id.calToday, actionPi(context, widgetId, ACTION_TODAY, 4));

        manager.updateAppWidget(widgetId, v);
    }

    private static void renderGregorian(RemoteViews v, LocalDate today, int offset, int theme) {
        YearMonth ym = YearMonth.from(today).plusMonths(offset);
        LocalDate first = ym.atDay(1);
        int start = sundayIndex(first.getDayOfWeek());
        int days = ym.lengthOfMonth();

        LocalDate middle = ym.atDay(Math.min(15, days));
        HijrahDate hmid = HijrahDate.from(middle);

        v.setTextViewText(R.id.calTitle,
                GREG_MONTHS[ym.getMonthValue() - 1] + " " + arabicDigits(String.valueOf(ym.getYear())));
        v.setTextViewText(R.id.calSubtitle,
                HIJRI_MONTHS[hmid.get(ChronoField.MONTH_OF_YEAR) - 1] + " "
                        + arabicDigits(String.valueOf(hmid.get(ChronoField.YEAR))) + " هـ");

        clearCells(v);
        for (int day = 1; day <= days; day++) {
            int cell = start + day - 1;
            LocalDate date = ym.atDay(day);
            HijrahDate hd = HijrahDate.from(date);
            setCell(v, cell, date,
                    arabicDigits(String.valueOf(day)),
                    arabicDigits(String.valueOf(hd.get(ChronoField.DAY_OF_MONTH))),
                    date.equals(today), theme);
        }
    }

    private static void renderHijri(RemoteViews v, LocalDate today, int offset, int theme) {
        HijrahDate htoday = HijrahDate.from(today);
        HijrahDate first = htoday.with(ChronoField.DAY_OF_MONTH, 1).plus(offset, ChronoUnit.MONTHS);
        int hMonth = first.get(ChronoField.MONTH_OF_YEAR);
        int hYear = first.get(ChronoField.YEAR);
        int days = first.lengthOfMonth();

        LocalDate firstIso = LocalDate.from(first);
        int start = sundayIndex(firstIso.getDayOfWeek());
        LocalDate middleIso = LocalDate.from(first.plus(Math.min(14, days - 1), ChronoUnit.DAYS));

        v.setTextViewText(R.id.calTitle,
                HIJRI_MONTHS[hMonth - 1] + " " + arabicDigits(String.valueOf(hYear)) + " هـ");
        v.setTextViewText(R.id.calSubtitle,
                GREG_MONTHS[middleIso.getMonthValue() - 1] + " "
                        + arabicDigits(String.valueOf(middleIso.getYear())));

        clearCells(v);
        for (int day = 1; day <= days; day++) {
            HijrahDate hd = first.with(ChronoField.DAY_OF_MONTH, day);
            LocalDate date = LocalDate.from(hd);
            setCell(v, start + day - 1, date,
                    arabicDigits(String.valueOf(day)),
                    arabicDigits(String.valueOf(date.getDayOfMonth())),
                    date.equals(today), theme);
        }
    }

    private static void clearCells(RemoteViews v) {
        for (int i = 0; i < 42; i++) {
            v.setTextViewText(PRIMARY[i], "");
            v.setTextViewText(SECONDARY[i], "");
            v.setInt(CELLS[i], "setBackgroundResource", android.R.color.transparent);
        }
    }

    private static void setCell(RemoteViews v, int cell, LocalDate date,
                                String primary, String secondary, boolean isToday, int theme) {
        if (cell < 0 || cell >= 42) return;
        int col = cell % 7;
        int main = primaryColor(theme);
        int sub = secondaryColor(theme);

        if (col == 5) {
            main = 0xFFFFCF76;
            sub = 0xFFF1A95B;
        } else if (col == 6) {
            main = 0xFFE7EEF7;
            sub = 0xFFB8C5D6;
        }

        if (isToday) {
            v.setInt(CELLS[cell], "setBackgroundResource", R.drawable.calendar_today_cell);
            main = 0xFFFFFFFF;
            sub = 0xFFFFD77A;
        }

        v.setTextViewText(PRIMARY[cell], primary);
        v.setTextViewText(SECONDARY[cell], secondary);
        v.setTextColor(PRIMARY[cell], main);
        v.setTextColor(SECONDARY[cell], sub);
    }

    private static int sundayIndex(DayOfWeek d) {
        return d.getValue() % 7;
    }

    private static PendingIntent actionPi(Context context, int widgetId, String action, int actionId) {
        Intent i = new Intent(context, CalendarWidgetProvider.class)
                .setAction(action)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return PendingIntent.getBroadcast(
                context,
                widgetId * 10 + actionId,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void applyTheme(RemoteViews v, int theme) {
        int bg;
        int primary;
        int secondary;
        String mark = "";
        int markColor = 0x00FFFFFF;

        switch (theme) {
            case 1:
                bg = R.drawable.calendar_bg_glass;
                primary = 0xFFFFFFFF;
                secondary = 0xFFD4D9E1;
                break;
            case 2:
                bg = R.drawable.calendar_bg_green;
                primary = 0xFFFFFFFF;
                secondary = 0xFFA8E5CC;
                mark = "🇸🇦";
                markColor = 0x16FFFFFF;
                break;
            case 3:
                bg = R.drawable.calendar_bg_blue;
                primary = 0xFFF5FAFF;
                secondary = 0xFFAFCFFF;
                break;
            case 4:
                bg = R.drawable.calendar_bg_gold;
                primary = 0xFFFFF7E4;
                secondary = 0xFFE7C46C;
                break;
            default:
                bg = R.drawable.calendar_bg_dark;
                primary = 0xFFFFFFFF;
                secondary = 0xFFD6C4F0;
        }

        v.setInt(R.id.calendarRoot, "setBackgroundResource", bg);
        v.setTextViewText(R.id.calendarWatermark, mark);
        v.setTextColor(R.id.calendarWatermark, markColor);
        v.setTextColor(R.id.calTitle, primary);
        v.setTextColor(R.id.calSubtitle, secondary);

        for (int id : DOW) v.setTextColor(id, WidgetStyle.withAlpha(primary, 155));
    }

    private static int primaryColor(int theme) {
        switch (theme) {
            case 2: return 0xFFF5FFF9;
            case 3: return 0xFFF5FAFF;
            case 4: return 0xFFFFF7E4;
            default: return Color.WHITE;
        }
    }

    private static int secondaryColor(int theme) {
        switch (theme) {
            case 1: return 0xFFC7CED8;
            case 2: return 0xFFA8E5CC;
            case 3: return 0xFFAFCFFF;
            case 4: return 0xFFE7C46C;
            default: return 0xFFC9B6E7;
        }
    }

    private static String arabicDigits(String s) {
        char[] a = {'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};
        StringBuilder b = new StringBuilder(s.length());
        for (char c : s.toCharArray()) b.append(c >= '0' && c <= '9' ? a[c - '0'] : c);
        return b.toString();
    }
}

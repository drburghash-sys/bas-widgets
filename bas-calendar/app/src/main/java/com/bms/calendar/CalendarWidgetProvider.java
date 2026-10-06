package com.bms.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class CalendarWidgetProvider extends AppWidgetProvider {
    private static final String PREFS = "bas_calendar_prefs";
    private static final String ACTION_TOGGLE = "com.bms.calendar.TOGGLE";
    private static final String ACTION_PREV = "com.bms.calendar.PREV";
    private static final String ACTION_NEXT = "com.bms.calendar.NEXT";
    private static final String ACTION_TODAY = "com.bms.calendar.TODAY";

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

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateWidget(context, manager, id);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action == null) return;
        if (!ACTION_TOGGLE.equals(action) && !ACTION_PREV.equals(action)
                && !ACTION_NEXT.equals(action) && !ACTION_TODAY.equals(action)) return;

        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return;

        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean hijri = p.getBoolean("hijri_" + widgetId, false);
        int offset = p.getInt("offset_" + widgetId, 0);

        if (ACTION_TOGGLE.equals(action)) {
            hijri = !hijri;
            offset = 0;
        } else if (ACTION_PREV.equals(action)) {
            offset = Math.max(-120, offset - 1);
        } else if (ACTION_NEXT.equals(action)) {
            offset = Math.min(120, offset + 1);
        } else {
            offset = 0;
        }

        p.edit().putBoolean("hijri_" + widgetId, hijri)
                .putInt("offset_" + widgetId, offset).apply();
        updateWidget(context, AppWidgetManager.getInstance(context), widgetId);
    }

    @Override public void onDeleted(Context context, int[] ids) {
        SharedPreferences.Editor e = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        for (int id : ids) {
            e.remove("hijri_" + id);
            e.remove("offset_" + id);
        }
        e.apply();
        super.onDeleted(context, ids);
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        android.content.ComponentName c = new android.content.ComponentName(context, CalendarWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(c)) updateWidget(context, manager, id);
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean hijriMode = p.getBoolean("hijri_" + widgetId, false);
        int offset = p.getInt("offset_" + widgetId, 0);
        int theme = p.getInt("theme", 0);

        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_calendar);
        applyTheme(v, theme);

        LocalDate today = LocalDate.now();
        if (hijriMode) renderHijri(v, today, offset, theme);
        else renderGregorian(v, today, offset, theme);

        v.setTextViewText(R.id.calToggle, hijriMode ? "م ⇄ هـ" : "هـ ⇄ م");
        v.setOnClickPendingIntent(R.id.calToggle, pi(context, widgetId, ACTION_TOGGLE, 1));
        v.setOnClickPendingIntent(R.id.calPrev, pi(context, widgetId, ACTION_PREV, 2));
        v.setOnClickPendingIntent(R.id.calNext, pi(context, widgetId, ACTION_NEXT, 3));
        v.setOnClickPendingIntent(R.id.calToday, pi(context, widgetId, ACTION_TODAY, 4));

        manager.updateAppWidget(widgetId, v);
    }

    private static void renderGregorian(RemoteViews v, LocalDate today, int offset, int theme) {
        YearMonth ym = YearMonth.from(today).plusMonths(offset);
        int start = sundayIndex(ym.atDay(1).getDayOfWeek());
        int days = ym.lengthOfMonth();
        HijrahDate mid = HijrahDate.from(ym.atDay(Math.min(15, days)));

        v.setTextViewText(R.id.calTitle,
                GREG_MONTHS[ym.getMonthValue()-1] + " " + ar(String.valueOf(ym.getYear())));
        v.setTextViewText(R.id.calSubtitle,
                HIJRI_MONTHS[mid.get(ChronoField.MONTH_OF_YEAR)-1] + " "
                        + ar(String.valueOf(mid.get(ChronoField.YEAR))) + " هـ");

        clear(v);
        for (int day=1; day<=days; day++) {
            int cell=start+day-1;
            LocalDate date=ym.atDay(day);
            HijrahDate hd=HijrahDate.from(date);
            setCell(v,cell,date,ar(String.valueOf(day)),
                    ar(String.valueOf(hd.get(ChronoField.DAY_OF_MONTH))),
                    date.equals(today),theme);
        }
    }

    private static void renderHijri(RemoteViews v, LocalDate today, int offset, int theme) {
        HijrahDate current=HijrahDate.from(today);
        HijrahDate first=current.with(ChronoField.DAY_OF_MONTH,1).plus(offset,ChronoUnit.MONTHS);
        int hMonth=first.get(ChronoField.MONTH_OF_YEAR);
        int hYear=first.get(ChronoField.YEAR);
        int days=first.lengthOfMonth();
        LocalDate firstIso=LocalDate.from(first);
        int start=sundayIndex(firstIso.getDayOfWeek());
        LocalDate midIso=LocalDate.from(first.plus(Math.min(14,days-1),ChronoUnit.DAYS));

        v.setTextViewText(R.id.calTitle,
                HIJRI_MONTHS[hMonth-1] + " " + ar(String.valueOf(hYear)) + " هـ");
        v.setTextViewText(R.id.calSubtitle,
                GREG_MONTHS[midIso.getMonthValue()-1] + " " + ar(String.valueOf(midIso.getYear())));

        clear(v);
        for(int day=1;day<=days;day++){
            HijrahDate hd=first.with(ChronoField.DAY_OF_MONTH,day);
            LocalDate date=LocalDate.from(hd);
            setCell(v,start+day-1,date,ar(String.valueOf(day)),
                    ar(String.valueOf(date.getDayOfMonth())),
                    date.equals(today),theme);
        }
    }

    private static void clear(RemoteViews v){
        for(int i=0;i<42;i++){
            v.setTextViewText(PRIMARY[i],"");
            v.setTextViewText(SECONDARY[i],"");
            v.setInt(CELLS[i],"setBackgroundResource",android.R.color.transparent);
        }
    }

    private static void setCell(RemoteViews v,int cell,LocalDate date,String primary,String secondary,
                                boolean today,int theme){
        if(cell<0||cell>=42)return;
        int col=cell%7;
        int main=primaryColor(theme), sub=secondaryColor(theme);

        if(col==5){ main=0xFFFFD477; sub=0xFFF0A85B; }
        else if(col==6){ main=0xFFEAF0F7; sub=0xFFB5C1D1; }

        if(today){
            v.setInt(CELLS[cell],"setBackgroundResource",R.drawable.calendar_today_cell);
            main=Color.WHITE;
            sub=0xFFFFD477;
        }

        v.setTextViewText(PRIMARY[cell],primary);
        v.setTextViewText(SECONDARY[cell],secondary);
        v.setTextColor(PRIMARY[cell],main);
        v.setTextColor(SECONDARY[cell],sub);
    }

    private static int sundayIndex(DayOfWeek d){ return d.getValue()%7; }

    private static PendingIntent pi(Context context,int id,String action,int code){
        Intent i=new Intent(context,CalendarWidgetProvider.class).setAction(action)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
        return PendingIntent.getBroadcast(context,id*10+code,i,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }

    private static void applyTheme(RemoteViews v,int theme){
        int bg, main, sub, markColor=0x00FFFFFF;
        String mark="";
        switch(theme){
            case 1:
                bg=R.drawable.calendar_bg_glass; main=Color.WHITE; sub=0xFFD4DAE2; break;
            case 2:
                bg=R.drawable.calendar_bg_green; main=Color.WHITE; sub=0xFFA8E5CC;
                mark="🇸🇦"; markColor=0x16FFFFFF; break;
            case 3:
                bg=R.drawable.calendar_bg_blue; main=0xFFF5FAFF; sub=0xFFAFCFFF; break;
            case 4:
                bg=R.drawable.calendar_bg_gold; main=0xFFFFF7E4; sub=0xFFE7C46C; break;
            default:
                bg=R.drawable.calendar_bg_dark; main=Color.WHITE; sub=0xFFD6C4F0;
        }
        v.setInt(R.id.calendarRoot,"setBackgroundResource",bg);
        v.setTextViewText(R.id.calendarWatermark,mark);
        v.setTextColor(R.id.calendarWatermark,markColor);
        v.setTextColor(R.id.calTitle,main);
        v.setTextColor(R.id.calSubtitle,sub);
        for(int id:DOW)v.setTextColor(id,alpha(main,170));
        v.setTextColor(R.id.calDow6,0xFFFFD477);
    }

    private static int primaryColor(int theme){
        switch(theme){
            case 2:return 0xFFF5FFF9;
            case 3:return 0xFFF5FAFF;
            case 4:return 0xFFFFF7E4;
            default:return Color.WHITE;
        }
    }
    private static int secondaryColor(int theme){
        switch(theme){
            case 1:return 0xFFC7CED8;
            case 2:return 0xFFA8E5CC;
            case 3:return 0xFFAFCFFF;
            case 4:return 0xFFE7C46C;
            default:return 0xFFC9B6E7;
        }
    }
    private static int alpha(int color,int a){
        return Color.argb(a,Color.red(color),Color.green(color),Color.blue(color));
    }
    private static String ar(String s){
        char[] a={'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};
        StringBuilder b=new StringBuilder();
        for(char c:s.toCharArray())b.append(c>='0'&&c<='9'?a[c-'0']:c);
        return b.toString();
    }
}

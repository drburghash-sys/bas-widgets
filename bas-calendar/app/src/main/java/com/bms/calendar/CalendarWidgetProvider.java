package com.bms.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
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
        for (int id : ids) {
            try { updateWidget(context, manager, id); }
            catch (Throwable t) { showFallback(context, manager, id); }
        }
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action=intent.getAction();
        if(action==null) return;
        if(!ACTION_TOGGLE.equals(action)&&!ACTION_PREV.equals(action)
                &&!ACTION_NEXT.equals(action)&&!ACTION_TODAY.equals(action)) return;

        int id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
        if(id==AppWidgetManager.INVALID_APPWIDGET_ID) return;

        SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        boolean hijri=p.getBoolean("hijri_"+id,false);
        int offset=p.getInt("offset_"+id,0);

        if(ACTION_TOGGLE.equals(action)){ hijri=!hijri; offset=0; }
        else if(ACTION_PREV.equals(action)) offset=Math.max(-120,offset-1);
        else if(ACTION_NEXT.equals(action)) offset=Math.min(120,offset+1);
        else offset=0;

        p.edit().putBoolean("hijri_"+id,hijri).putInt("offset_"+id,offset).apply();
        try { updateWidget(context,AppWidgetManager.getInstance(context),id); }
        catch(Throwable t){ showFallback(context,AppWidgetManager.getInstance(context),id); }
    }

    public static void updateAll(Context context){
        AppWidgetManager m=AppWidgetManager.getInstance(context);
        ComponentName c=new ComponentName(context,CalendarWidgetProvider.class);
        for(int id:m.getAppWidgetIds(c)){
            try{ updateWidget(context,m,id); }catch(Throwable t){ showFallback(context,m,id); }
        }
    }

    public static void updateWidget(Context context,AppWidgetManager manager,int id){
        SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        boolean hijri=p.getBoolean("hijri_"+id,false);
        int offset=p.getInt("offset_"+id,0);
        int theme=p.getInt("theme",0);

        RemoteViews v=new RemoteViews(context.getPackageName(),layoutForTheme(theme));
        LocalDate today=LocalDate.now();

        if(hijri) renderHijri(v,today,offset,theme);
        else renderGregorian(v,today,offset,theme);

        v.setTextViewText(R.id.calToggle,hijri?"م ⇄ هـ":"هـ ⇄ م");
        v.setOnClickPendingIntent(R.id.calToggle,pi(context,id,ACTION_TOGGLE,1));
        v.setOnClickPendingIntent(R.id.calPrev,pi(context,id,ACTION_PREV,2));
        v.setOnClickPendingIntent(R.id.calNext,pi(context,id,ACTION_NEXT,3));
        v.setOnClickPendingIntent(R.id.calToday,pi(context,id,ACTION_TODAY,4));
        manager.updateAppWidget(id,v);
    }

    private static int layoutForTheme(int theme){
        switch(theme){
            case 1:return R.layout.widget_calendar_glass;
            case 2:return R.layout.widget_calendar_green;
            case 3:return R.layout.widget_calendar_blue;
            case 4:return R.layout.widget_calendar_gold;
            default:return R.layout.widget_calendar_dark;
        }
    }

    private static void renderGregorian(RemoteViews v,LocalDate today,int offset,int theme){
        YearMonth ym=YearMonth.from(today).plusMonths(offset);
        int start=sundayIndex(ym.atDay(1).getDayOfWeek());
        int days=ym.lengthOfMonth();
        HijrahDate mid=HijrahDate.from(ym.atDay(Math.min(15,days)));

        v.setTextViewText(R.id.calTitle,GREG_MONTHS[ym.getMonthValue()-1]+" "+ar(String.valueOf(ym.getYear())));
        v.setTextViewText(R.id.calSubtitle,HIJRI_MONTHS[mid.get(ChronoField.MONTH_OF_YEAR)-1]+" "+ar(String.valueOf(mid.get(ChronoField.YEAR)))+" هـ");

        clear(v,theme);
        for(int day=1;day<=days;day++){
            int cell=start+day-1;
            LocalDate date=ym.atDay(day);
            HijrahDate hd=HijrahDate.from(date);
            setCell(v,cell,date,ar(String.valueOf(day)),ar(String.valueOf(hd.get(ChronoField.DAY_OF_MONTH))),date.equals(today),theme);
        }
    }

    private static void renderHijri(RemoteViews v,LocalDate today,int offset,int theme){
        HijrahDate current=HijrahDate.from(today);
        HijrahDate first=current.with(ChronoField.DAY_OF_MONTH,1).plus(offset,ChronoUnit.MONTHS);
        int hm=first.get(ChronoField.MONTH_OF_YEAR), hy=first.get(ChronoField.YEAR), days=first.lengthOfMonth();
        LocalDate firstIso=LocalDate.from(first);
        int start=sundayIndex(firstIso.getDayOfWeek());
        LocalDate mid=LocalDate.from(first.plus(Math.min(14,days-1),ChronoUnit.DAYS));

        v.setTextViewText(R.id.calTitle,HIJRI_MONTHS[hm-1]+" "+ar(String.valueOf(hy))+" هـ");
        v.setTextViewText(R.id.calSubtitle,GREG_MONTHS[mid.getMonthValue()-1]+" "+ar(String.valueOf(mid.getYear())));

        clear(v,theme);
        for(int day=1;day<=days;day++){
            HijrahDate hd=first.with(ChronoField.DAY_OF_MONTH,day);
            LocalDate date=LocalDate.from(hd);
            setCell(v,start+day-1,date,ar(String.valueOf(day)),ar(String.valueOf(date.getDayOfMonth())),date.equals(today),theme);
        }
    }

    private static void clear(RemoteViews v,int theme){
        int sub=secondaryColor(theme);
        for(int i=0;i<42;i++){
            v.setTextViewText(PRIMARY[i],"");
            v.setTextViewText(SECONDARY[i],"");
            v.setTextColor(PRIMARY[i],primaryColor(theme));
            v.setTextColor(SECONDARY[i],sub);
        }
        for(int id:DOW)v.setTextColor(id,0xAFFFFFFF);
        v.setTextColor(R.id.calDow6,0xFFFFD477);
    }

    private static void setCell(RemoteViews v,int cell,LocalDate date,String primary,String secondary,boolean today,int theme){
        if(cell<0||cell>=42)return;
        int col=cell%7;
        int main=primaryColor(theme),sub=secondaryColor(theme);
        if(col==5){main=0xFFFFD477;sub=0xFFF0A85B;}
        else if(col==6){main=0xFFEAF0F7;sub=0xFFB5C1D1;}

        if(today){
            primary="● "+primary;
            main=0xFFFFE29A;
            sub=0xFFFFC65A;
        }
        v.setTextViewText(PRIMARY[cell],primary);
        v.setTextViewText(SECONDARY[cell],secondary);
        v.setTextColor(PRIMARY[cell],main);
        v.setTextColor(SECONDARY[cell],sub);
    }

    private static void showFallback(Context context,AppWidgetManager manager,int id){
        RemoteViews v=new RemoteViews(context.getPackageName(),R.layout.widget_calendar_fallback);
        v.setTextViewText(R.id.fallbackTitle,"BAS Calendar");
        v.setTextViewText(R.id.fallbackText,"اضغط «اليوم» لإعادة تحديث التقويم");
        v.setOnClickPendingIntent(R.id.fallbackToday,pi(context,id,ACTION_TODAY,4));
        manager.updateAppWidget(id,v);
    }

    private static PendingIntent pi(Context c,int id,String action,int code){
        Intent i=new Intent(c,CalendarWidgetProvider.class).setAction(action)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
        return PendingIntent.getBroadcast(c,id*10+code,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }

    private static int sundayIndex(DayOfWeek d){return d.getValue()%7;}
    private static int primaryColor(int t){
        switch(t){case 2:return 0xFFF5FFF9;case 3:return 0xFFF5FAFF;case 4:return 0xFFFFF7E4;default:return Color.WHITE;}
    }
    private static int secondaryColor(int t){
        switch(t){case 1:return 0xFFC7CED8;case 2:return 0xFFA8E5CC;case 3:return 0xFFAFCFFF;case 4:return 0xFFE7C46C;default:return 0xFFC9B6E7;}
    }
    private static String ar(String s){
        char[] a={'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};StringBuilder b=new StringBuilder();
        for(char c:s.toCharArray())b.append(c>='0'&&c<='9'?a[c-'0']:c);return b.toString();
    }
}

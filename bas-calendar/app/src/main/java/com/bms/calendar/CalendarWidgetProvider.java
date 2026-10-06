package com.bms.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.io.InputStream;
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
        for (int id : ids) safeUpdate(context, manager, id);
    }

    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int id, Bundle options) {
        super.onAppWidgetOptionsChanged(context, manager, id, options);
        safeUpdate(context, manager, id);
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

        if(ACTION_TOGGLE.equals(action)){hijri=!hijri;offset=0;}
        else if(ACTION_PREV.equals(action))offset=Math.max(-120,offset-1);
        else if(ACTION_NEXT.equals(action))offset=Math.min(120,offset+1);
        else offset=0;

        p.edit().putBoolean("hijri_"+id,hijri).putInt("offset_"+id,offset).apply();
        safeUpdate(context,AppWidgetManager.getInstance(context),id);
    }

    public static void updateAll(Context context){
        AppWidgetManager m=AppWidgetManager.getInstance(context);
        ComponentName c=new ComponentName(context,CalendarWidgetProvider.class);
        for(int id:m.getAppWidgetIds(c))safeUpdate(context,m,id);
    }

    private static void safeUpdate(Context context,AppWidgetManager manager,int id){
        try{updateWidget(context,manager,id);}
        catch(Throwable t){showFallback(context,manager,id);}
    }

    public static void updateWidget(Context context,AppWidgetManager manager,int id){
        SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        boolean hijri=p.getBoolean("hijri_"+id,false);
        int offset=p.getInt("offset_"+id,0);
        int theme=p.getInt("theme",0);
        int glass=p.getInt("glass",1);
        int fontPreset=p.getInt("font_preset",2);
        String backgroundUri=p.getString("background_uri","");

        boolean hasPhoto=backgroundUri!=null&&!backgroundUri.isEmpty();
        RemoteViews v=new RemoteViews(context.getPackageName(),layoutFor(theme,hasPhoto,glass));

        if(hasPhoto){
            Bitmap bg=loadBitmap(context,backgroundUri,1200);
            if(bg!=null)v.setImageViewBitmap(R.id.calBackgroundImage,bg);
        }

        LocalDate today=LocalDate.now();
        if(hijri)renderHijri(v,today,offset,theme);
        else renderGregorian(v,today,offset,theme);

        applySizing(v,manager,id,fontPreset);

        v.setTextViewText(R.id.calToggle,hijri?"م ⇄ هـ":"هـ ⇄ م");
        v.setOnClickPendingIntent(R.id.calToggle,pi(context,id,ACTION_TOGGLE,1));
        v.setOnClickPendingIntent(R.id.calPrev,pi(context,id,ACTION_PREV,2));
        v.setOnClickPendingIntent(R.id.calNext,pi(context,id,ACTION_NEXT,3));
        v.setOnClickPendingIntent(R.id.calToday,pi(context,id,ACTION_TODAY,4));
        manager.updateAppWidget(id,v);
    }

    private static void applySizing(RemoteViews v,AppWidgetManager manager,int id,int preset){
        Bundle o=manager.getAppWidgetOptions(id);
        int h=o==null?500:o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,500);
        float user=preset==0?1.00f:(preset==1?1.15f:1.30f);
        float area=Math.max(0.88f,Math.min(1.22f,h/430f));
        float scale=user*area;

        v.setTextViewTextSize(R.id.calTitle,TypedValue.COMPLEX_UNIT_SP,22f*scale);
        v.setTextViewTextSize(R.id.calSubtitle,TypedValue.COMPLEX_UNIT_SP,12f*scale);
        v.setTextViewTextSize(R.id.calToggle,TypedValue.COMPLEX_UNIT_SP,12f*scale);
        v.setTextViewTextSize(R.id.calPrev,TypedValue.COMPLEX_UNIT_SP,12f*scale);
        v.setTextViewTextSize(R.id.calToday,TypedValue.COMPLEX_UNIT_SP,12f*scale);
        v.setTextViewTextSize(R.id.calNext,TypedValue.COMPLEX_UNIT_SP,12f*scale);
        for(int dow:DOW)v.setTextViewTextSize(dow,TypedValue.COMPLEX_UNIT_SP,11f*scale);
        for(int i=0;i<42;i++){
            v.setTextViewTextSize(PRIMARY[i],TypedValue.COMPLEX_UNIT_SP,20f*scale);
            v.setTextViewTextSize(SECONDARY[i],TypedValue.COMPLEX_UNIT_SP,9.5f*scale);
        }
    }

    private static int layoutFor(int theme,boolean photo,int glass){
        if(photo){
            if(glass==0)return R.layout.widget_calendar_photo_light;
            if(glass==2)return R.layout.widget_calendar_photo_dark;
            return R.layout.widget_calendar_photo_medium;
        }
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
        LocalDate first=ym.atDay(1);
        int start=sundayIndex(first.getDayOfWeek());
        LocalDate gridStart=first.minusDays(start);
        HijrahDate mid=HijrahDate.from(ym.atDay(Math.min(15,ym.lengthOfMonth())));

        v.setTextViewText(R.id.calTitle,GREG_MONTHS[ym.getMonthValue()-1]+" "+ar(String.valueOf(ym.getYear())));
        v.setTextViewText(R.id.calSubtitle,HIJRI_MONTHS[mid.get(ChronoField.MONTH_OF_YEAR)-1]+" "+ar(String.valueOf(mid.get(ChronoField.YEAR)))+" هـ");

        for(int i=0;i<42;i++){
            LocalDate date=gridStart.plusDays(i);
            HijrahDate hd=HijrahDate.from(date);
            boolean inMonth=date.getMonthValue()==ym.getMonthValue()&&date.getYear()==ym.getYear();
            setCell(v,i,date,ar(String.valueOf(date.getDayOfMonth())),
                    ar(String.valueOf(hd.get(ChronoField.DAY_OF_MONTH))),
                    date.equals(today),inMonth,theme);
        }
        styleHeaders(v);
    }

    private static void renderHijri(RemoteViews v,LocalDate today,int offset,int theme){
        HijrahDate current=HijrahDate.from(today);
        HijrahDate first=current.with(ChronoField.DAY_OF_MONTH,1).plus(offset,ChronoUnit.MONTHS);
        int hm=first.get(ChronoField.MONTH_OF_YEAR),hy=first.get(ChronoField.YEAR);
        LocalDate firstIso=LocalDate.from(first);
        int start=sundayIndex(firstIso.getDayOfWeek());
        LocalDate gridStart=firstIso.minusDays(start);
        LocalDate mid=LocalDate.from(first.plus(Math.min(14,first.lengthOfMonth()-1),ChronoUnit.DAYS));

        v.setTextViewText(R.id.calTitle,HIJRI_MONTHS[hm-1]+" "+ar(String.valueOf(hy))+" هـ");
        v.setTextViewText(R.id.calSubtitle,GREG_MONTHS[mid.getMonthValue()-1]+" "+ar(String.valueOf(mid.getYear())));

        for(int i=0;i<42;i++){
            LocalDate date=gridStart.plusDays(i);
            HijrahDate hd=HijrahDate.from(date);
            boolean inMonth=hd.get(ChronoField.MONTH_OF_YEAR)==hm&&hd.get(ChronoField.YEAR)==hy;
            setCell(v,i,date,ar(String.valueOf(hd.get(ChronoField.DAY_OF_MONTH))),
                    ar(String.valueOf(date.getDayOfMonth())),
                    date.equals(today),inMonth,theme);
        }
        styleHeaders(v);
    }

    private static void styleHeaders(RemoteViews v){
        for(int id:DOW)v.setTextColor(id,0xB8FFFFFF);
        v.setTextColor(R.id.calDow6,0xFFFFD477);
    }

    private static void setCell(RemoteViews v,int cell,LocalDate date,String primary,String secondary,
                                boolean today,boolean inMonth,int theme){
        if(cell<0||cell>=42)return;
        int col=cell%7;
        int main=primaryColor(theme),sub=secondaryColor(theme);

        if(!inMonth){main=0x66FFFFFF;sub=0x55C9B6E7;}
        else if(col==5){main=0xFFFFD477;sub=0xFFF0A85B;}
        else if(col==6){main=0xFFEAF0F7;sub=0xFFB5C1D1;}

        if(today){
            primary="●"+primary;
            main=0xFFFFE29A;
            sub=0xFFFFC65A;
        }
        v.setTextViewText(PRIMARY[cell],primary);
        v.setTextViewText(SECONDARY[cell],secondary);
        v.setTextColor(PRIMARY[cell],main);
        v.setTextColor(SECONDARY[cell],sub);
    }

    private static Bitmap loadBitmap(Context context,String raw,int maxSide){
        try{
            Uri uri=Uri.parse(raw);
            BitmapFactory.Options bounds=new BitmapFactory.Options();
            bounds.inJustDecodeBounds=true;
            try(InputStream in=context.getContentResolver().openInputStream(uri)){
                BitmapFactory.decodeStream(in,null,bounds);
            }
            int sample=1;
            int largest=Math.max(bounds.outWidth,bounds.outHeight);
            while(largest/sample>maxSide)sample*=2;
            BitmapFactory.Options opts=new BitmapFactory.Options();
            opts.inSampleSize=Math.max(1,sample);
            try(InputStream in=context.getContentResolver().openInputStream(uri)){
                return BitmapFactory.decodeStream(in,null,opts);
            }
        }catch(Throwable ignored){return null;}
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

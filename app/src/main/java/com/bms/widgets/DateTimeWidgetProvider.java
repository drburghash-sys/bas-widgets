package com.bms.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import java.util.Date;
import java.util.Locale;

public class DateTimeWidgetProvider extends AppWidgetProvider {
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids){ for(int id:ids) updateWidget(c,m,id); }
    @Override public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m,int id,Bundle o){ updateWidget(c,m,id); }
    @Override public void onReceive(Context c, Intent i){
        super.onReceive(c,i);
        String a=i.getAction();
        if(Intent.ACTION_DATE_CHANGED.equals(a)||Intent.ACTION_TIME_CHANGED.equals(a)||Intent.ACTION_TIMEZONE_CHANGED.equals(a)||Intent.ACTION_LOCALE_CHANGED.equals(a)){
            AppWidgetManager m=AppWidgetManager.getInstance(c);
            for(int id:m.getAppWidgetIds(new ComponentName(c,DateTimeWidgetProvider.class))) updateWidget(c,m,id);
        }
    }
    public static void updateWidget(Context c, AppWidgetManager m,int id){
        RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget_date_time);
        SimpleDateFormat hf=new SimpleDateFormat("d MMMM yyyy 'هـ'",Locale.forLanguageTag("ar-SA-u-ca-islamic-umalqura"));
        v.setTextViewText(R.id.hijriDate,hf.format(new Date()));
        WidgetStyle.applyBackgroundAndWatermark(c,v,R.id.widgetRoot,R.id.themeMark);
        int color=WidgetStyle.getTextColor(c); float s=WidgetStyle.getFontScale(c);
        int[] colored={R.id.dayName,R.id.gregorianDate,R.id.currentTime12,R.id.currentTime24};
        for(int x:colored)v.setTextColor(x,color);
        v.setTextColor(R.id.hijriDate,WidgetStyle.withAlpha(color,205));
        v.setTextViewTextSize(R.id.dayName,TypedValue.COMPLEX_UNIT_SP,18*s);
        v.setTextViewTextSize(R.id.gregorianDate,TypedValue.COMPLEX_UNIT_SP,23*s);
        v.setTextViewTextSize(R.id.hijriDate,TypedValue.COMPLEX_UNIT_SP,15*s);
        v.setTextViewTextSize(R.id.currentTime12,TypedValue.COMPLEX_UNIT_SP,30*s);
        v.setTextViewTextSize(R.id.currentTime24,TypedValue.COMPLEX_UNIT_SP,30*s);
        boolean h24=WidgetStyle.use24Hour(c);
        v.setViewVisibility(R.id.currentTime12,h24?View.GONE:View.VISIBLE);
        v.setViewVisibility(R.id.currentTime24,h24?View.VISIBLE:View.GONE);
        PendingIntent p=PendingIntent.getActivity(c,100,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.widgetRoot,p); m.updateAppWidget(id,v);
    }
}

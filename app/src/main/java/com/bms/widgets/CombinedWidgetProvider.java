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

public class CombinedWidgetProvider extends AppWidgetProvider {
    @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)updateWidget(c,m,id);}
    @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,Bundle o){updateWidget(c,m,id);}
    @Override public void onReceive(Context c,Intent i){
        super.onReceive(c,i);String a=i.getAction();
        if(Intent.ACTION_DATE_CHANGED.equals(a)||Intent.ACTION_TIME_CHANGED.equals(a)||Intent.ACTION_TIMEZONE_CHANGED.equals(a)||Intent.ACTION_LOCALE_CHANGED.equals(a)){
            AppWidgetManager m=AppWidgetManager.getInstance(c);
            for(int id:m.getAppWidgetIds(new ComponentName(c,CombinedWidgetProvider.class)))updateWidget(c,m,id);
        }
    }
    public static void updateWidget(Context c,AppWidgetManager m,int id){
        RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget_combined);
        SimpleDateFormat hf=new SimpleDateFormat("d MMMM yyyy 'هـ'",Locale.forLanguageTag("ar-SA-u-ca-islamic-umalqura"));
        v.setTextViewText(R.id.combinedHijriDate,hf.format(new Date()));
        PrayerTimesCalculator.Times t=PrayerTimesCalculator.calculateToday();
        int[] tids={R.id.combinedFajrTime,R.id.combinedSunriseTime,R.id.combinedDhuhrTime,R.id.combinedAsrTime,R.id.combinedMaghribTime,R.id.combinedIshaTime};
        String[] tv={t.fajr,t.sunrise,t.dhuhr,t.asr,t.maghrib,t.isha};
        for(int x=0;x<tids.length;x++)v.setTextViewText(tids[x],WidgetStyle.formatPrayerTime(c,tv[x]));
        WidgetStyle.applyBackgroundAndWatermark(c,v,R.id.combinedRoot,R.id.combinedThemeMark);
        int color=WidgetStyle.getTextColor(c),secondary=WidgetStyle.withAlpha(color,210);float s=WidgetStyle.getFontScale(c);
        v.setTextColor(R.id.combinedGregorianDate,color);v.setTextColor(R.id.combinedHijriDate,color);
        v.setTextColor(R.id.combinedLocation,secondary);v.setTextColor(R.id.combinedCurrentTime12,secondary);v.setTextColor(R.id.combinedCurrentTime24,secondary);
        v.setTextViewTextSize(R.id.combinedGregorianDate,TypedValue.COMPLEX_UNIT_SP,18*s);
        v.setTextViewTextSize(R.id.combinedHijriDate,TypedValue.COMPLEX_UNIT_SP,18*s);
        v.setTextViewTextSize(R.id.combinedLocation,TypedValue.COMPLEX_UNIT_SP,15*s);
        v.setTextViewTextSize(R.id.combinedCurrentTime12,TypedValue.COMPLEX_UNIT_SP,15*s);
        v.setTextViewTextSize(R.id.combinedCurrentTime24,TypedValue.COMPLEX_UNIT_SP,15*s);
        boolean h24=WidgetStyle.use24Hour(c);v.setViewVisibility(R.id.combinedCurrentTime12,h24?View.GONE:View.VISIBLE);v.setViewVisibility(R.id.combinedCurrentTime24,h24?View.VISIBLE:View.GONE);
        int[] labels={R.id.combinedFajrLabel,R.id.combinedSunriseLabel,R.id.combinedDhuhrLabel,R.id.combinedAsrLabel,R.id.combinedMaghribLabel,R.id.combinedIshaLabel};
        for(int x:labels){v.setTextColor(x,color);v.setTextViewTextSize(x,TypedValue.COMPLEX_UNIT_SP,14*s);}
        for(int x:tids){v.setTextColor(x,color);v.setTextViewTextSize(x,TypedValue.COMPLEX_UNIT_SP,16*s);}
        PendingIntent p=PendingIntent.getActivity(c,300,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.combinedRoot,p);m.updateAppWidget(id,v);
    }
}

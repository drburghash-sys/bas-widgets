package com.bms.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RemoteViews;

public class PrayerTimesWidgetProvider extends AppWidgetProvider {
    @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)updateWidget(c,m,id);}
    @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,Bundle o){updateWidget(c,m,id);}
    @Override public void onReceive(Context c,Intent i){
        super.onReceive(c,i); String a=i.getAction();
        if(Intent.ACTION_DATE_CHANGED.equals(a)||Intent.ACTION_TIME_CHANGED.equals(a)||Intent.ACTION_TIMEZONE_CHANGED.equals(a)||Intent.ACTION_LOCALE_CHANGED.equals(a)){
            AppWidgetManager m=AppWidgetManager.getInstance(c);
            for(int id:m.getAppWidgetIds(new ComponentName(c,PrayerTimesWidgetProvider.class)))updateWidget(c,m,id);
        }
    }
    public static void updateWidget(Context c,AppWidgetManager m,int id){
        RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget_prayer_times);
        PrayerTimesCalculator.Times t=PrayerTimesCalculator.calculateToday();
        int[] ids={R.id.fajrTime,R.id.sunriseTime,R.id.dhuhrTime,R.id.asrTime,R.id.maghribTime,R.id.ishaTime};
        String[] val={t.fajr,t.sunrise,t.dhuhr,t.asr,t.maghrib,t.isha};
        for(int x=0;x<ids.length;x++)v.setTextViewText(ids[x],WidgetStyle.formatPrayerTime(c,val[x]));
        WidgetStyle.applyBackgroundAndWatermark(c,v,R.id.prayerWidgetRoot,R.id.prayerThemeMark);
        int color=WidgetStyle.getTextColor(c); float s=WidgetStyle.getFontScale(c);
        int[] labels={R.id.fajrLabel,R.id.sunriseLabel,R.id.dhuhrLabel,R.id.asrLabel,R.id.maghribLabel,R.id.ishaLabel};
        for(int x:labels){v.setTextColor(x,color);v.setTextViewTextSize(x,TypedValue.COMPLEX_UNIT_SP,14*s);}
        for(int x:ids){v.setTextColor(x,color);v.setTextViewTextSize(x,TypedValue.COMPLEX_UNIT_SP,16*s);}
        v.setTextColor(R.id.prayerLocation,WidgetStyle.withAlpha(color,195));
        v.setTextViewTextSize(R.id.prayerLocation,TypedValue.COMPLEX_UNIT_SP,11*s);
        PendingIntent p=PendingIntent.getActivity(c,200,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.prayerWidgetRoot,p);m.updateAppWidget(id,v);
    }
}

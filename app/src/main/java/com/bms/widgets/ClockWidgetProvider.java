package com.bms.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

public class ClockWidgetProvider extends AppWidgetProvider {
    private static final String PREFS="bms_widget_prefs";
    @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)updateWidget(c,m,id);}
    @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,android.os.Bundle o){updateWidget(c,m,id);}

    public static void updateWidget(Context c,AppWidgetManager m,int id){
        SharedPreferences p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        int type=p.getInt("clock_widget_type",0);
        int ai=clamp(p.getInt("analog_style_index",0));
        int di=clamp(p.getInt("digital_style_index",0));
        RemoteViews v;int root;
        if(type==0){
            v=new RemoteViews(c.getPackageName(),analogLayout(ai));root=R.id.clockRoot;
            v.setTextViewText(R.id.clockLogo,WidgetStyle.clockLogo(c));
            v.setTextColor(R.id.clockLogo,WidgetStyle.clockLogoColor(c));
        }else{
            v=new RemoteViews(c.getPackageName(),R.layout.widget_clock_digital);root=R.id.clockDigitalRoot;
            v.setInt(root,"setBackgroundResource",digitalBackground(di));
            int color=digitalTextColor(di);float size=digitalTextSize(di);
            v.setTextColor(R.id.clockDigitalTime12,color);v.setTextColor(R.id.clockDigitalTime24,color);
            v.setTextViewTextSize(R.id.clockDigitalTime12,TypedValue.COMPLEX_UNIT_SP,size);
            v.setTextViewTextSize(R.id.clockDigitalTime24,TypedValue.COMPLEX_UNIT_SP,size);
            v.setTextViewText(R.id.clockDigitalLogo,WidgetStyle.clockLogo(c));
            v.setTextColor(R.id.clockDigitalLogo,WidgetStyle.clockLogoColor(c));
            boolean h24=WidgetStyle.use24Hour(c);
            v.setViewVisibility(R.id.clockDigitalTime12,h24?View.GONE:View.VISIBLE);
            v.setViewVisibility(R.id.clockDigitalTime24,h24?View.VISIBLE:View.GONE);
        }
        PendingIntent pi=PendingIntent.getActivity(c,400+id,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(root,pi);m.updateAppWidget(id,v);
    }

    private static int analogLayout(int i){
        int[] r={R.layout.widget_clock_analog_01,R.layout.widget_clock_analog_02,R.layout.widget_clock_analog_03,R.layout.widget_clock_analog_04,R.layout.widget_clock_analog_05,
                R.layout.widget_clock_analog_06,R.layout.widget_clock_analog_07,R.layout.widget_clock_analog_08,R.layout.widget_clock_analog_09,R.layout.widget_clock_analog_10,
                R.layout.widget_clock_analog_11,R.layout.widget_clock_analog_12,R.layout.widget_clock_analog_13,R.layout.widget_clock_analog_14,R.layout.widget_clock_analog_15};
        return r[clamp(i)];
    }
    private static int digitalBackground(int i){
        int[] r={R.drawable.clock_digital_bg_01,R.drawable.clock_digital_bg_02,R.drawable.clock_digital_bg_03,R.drawable.clock_digital_bg_04,R.drawable.clock_digital_bg_05,
                R.drawable.clock_digital_bg_06,R.drawable.clock_digital_bg_07,R.drawable.clock_digital_bg_08,R.drawable.clock_digital_bg_09,R.drawable.clock_digital_bg_10,
                R.drawable.clock_digital_bg_11,R.drawable.clock_digital_bg_12,R.drawable.clock_digital_bg_13,R.drawable.clock_digital_bg_14,R.drawable.clock_digital_bg_15};
        return r[clamp(i)];
    }
    private static int digitalTextColor(int i){
        int[] c={0xFFFFFFFF,0xFF151515,0xFFFFFFFF,0xFFFFFFFF,0xFFFFD54A,0xFF9DD8FF,0xFFFFD66B,0xFFFFD7A3,0xFF29251E,0xFFC6FFE5,0xFFFFFFFF,0xFFE4EEFF,0xFFE8E8E8,0xFFF2D9FF,0xFFC8FAFF};
        return c[clamp(i)];
    }
    private static float digitalTextSize(int i){
        float[] s={48,44,50,48,46,52,45,47,43,49,52,50,46,48,51};return s[clamp(i)];
    }
    private static int clamp(int v){return Math.max(0,Math.min(14,v));}
}

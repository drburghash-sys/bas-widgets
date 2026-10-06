package com.bms.calendar;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private Spinner theme;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("bas_calendar_prefs",MODE_PRIVATE);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24),dp(38),dp(24),dp(30));
        root.setBackgroundColor(0xFF10151E);

        TextView title=text("BAS Calendar",28,0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title,full(0,8));

        TextView sub=text("تقويم هجري + ميلادي من الشاشة الرئيسية",16,0xFFC3CDD9);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub,full(0,24));

        TextView label=text("خلفية التقويم",15,0xFFFFFFFF);
        label.setGravity(Gravity.RIGHT);
        root.addView(label,full(0,6));

        theme=new Spinner(this);
        theme.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,
                new String[]{"داكن فخم","زجاجي شفاف","أخضر سعودي","أزرق ليلي","ذهبي دافئ"}));
        theme.setSelection(prefs.getInt("theme",0));
        root.addView(theme,full(0,16));

        Button save=new Button(this);
        save.setText("حفظ التصميم");
        save.setAllCaps(false);
        save.setTextSize(17);
        save.setOnClickListener(v->{
            prefs.edit().putInt("theme",theme.getSelectedItemPosition()).apply();
            CalendarWidgetProvider.updateAll(this);
            Toast.makeText(this,"تم تحديث تصميم التقويم",Toast.LENGTH_SHORT).show();
        });
        root.addView(save,full(0,10));

        Button add=new Button(this);
        add.setText("إضافة ويدجت التقويم");
        add.setAllCaps(false);
        add.setTextSize(17);
        add.setOnClickListener(v->pin());
        root.addView(add,full(0,14));

        TextView note=text("من الويدجت نفسه: هـ ⇄ م للتبديل، السابق/التالي للتنقل، واليوم للعودة مباشرة.",13,0xFFA7B4C2);
        note.setGravity(Gravity.CENTER);
        root.addView(note,full(8,0));

        setContentView(root);
    }

    private void pin(){
        AppWidgetManager manager=getSystemService(AppWidgetManager.class);
        ComponentName provider=new ComponentName(this,CalendarWidgetProvider.class);
        if(manager!=null&&manager.isRequestPinAppWidgetSupported()){
            manager.requestPinAppWidget(provider,null,null);
        }else{
            Toast.makeText(this,"أضف BAS Calendar من قائمة Widgets",Toast.LENGTH_LONG).show();
        }
    }

    private TextView text(String s,int size,int color){
        TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); return v;
    }
    private LinearLayout.LayoutParams full(int top,int bottom){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(top),0,dp(bottom)); return p;
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}

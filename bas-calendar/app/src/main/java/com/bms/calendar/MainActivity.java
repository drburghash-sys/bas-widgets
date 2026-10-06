package com.bms.calendar;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_BG=61;
    private SharedPreferences prefs;
    private Spinner theme,glass,font;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("bas_calendar_prefs",MODE_PRIVATE);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22),dp(30),dp(22),dp(24));
        root.setBackgroundColor(0xFF10151E);

        TextView title=text("BAS Calendar V2",28,0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title,full(0,4));

        TextView sub=text("Full Widget · هجري + ميلادي",15,0xFFC3CDD9);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub,full(0,20));

        addLabel(root,"حجم الخط");
        font=spinner(new String[]{"كبير","كبير جدًا","ضخم"});
        font.setSelection(prefs.getInt("font_preset",2));
        root.addView(font,full(0,12));

        addLabel(root,"ألوان التقويم عند عدم استخدام صورة");
        theme=spinner(new String[]{"داكن فخم","زجاجي شفاف","أخضر سعودي","أزرق ليلي","ذهبي دافئ"});
        theme.setSelection(prefs.getInt("theme",0));
        root.addView(theme,full(0,12));

        addLabel(root,"شفافية الزجاج فوق صورة الخلفية");
        glass=spinner(new String[]{"خفيف","متوسط","داكن"});
        glass.setSelection(prefs.getInt("glass",1));
        root.addView(glass,full(0,12));

        Button choose=new Button(this);
        choose.setText("اختيار صورة خلفية");
        choose.setAllCaps(false);
        choose.setTextSize(16);
        choose.setOnClickListener(v->pickBackground());
        root.addView(choose,full(0,8));

        Button clear=new Button(this);
        clear.setText("إزالة صورة الخلفية");
        clear.setAllCaps(false);
        clear.setTextSize(15);
        clear.setOnClickListener(v->{
            prefs.edit().remove("background_uri").apply();
            CalendarWidgetProvider.updateAll(this);
            Toast.makeText(this,"تمت إزالة الصورة",Toast.LENGTH_SHORT).show();
        });
        root.addView(clear,full(0,14));

        Button save=new Button(this);
        save.setText("حفظ وتطبيق");
        save.setAllCaps(false);
        save.setTextSize(17);
        save.setOnClickListener(v->save());
        root.addView(save,full(0,8));

        Button add=new Button(this);
        add.setText("إضافة ويدجت التقويم الكبير");
        add.setAllCaps(false);
        add.setTextSize(17);
        add.setOnClickListener(v->pin());
        root.addView(add,full(0,12));

        TextView note=text("يفضل وضعه على مساحة ٥×٦ تقريبًا. ويمكن تكبيره حتى يملأ معظم الصفحة. كل التحكم بعد ذلك من الويدجت نفسه.",12,0xFFA7B4C2);
        note.setGravity(Gravity.CENTER);
        root.addView(note,full(6,0));

        setContentView(root);
    }

    private void save(){
        prefs.edit()
                .putInt("theme",theme.getSelectedItemPosition())
                .putInt("glass",glass.getSelectedItemPosition())
                .putInt("font_preset",font.getSelectedItemPosition())
                .apply();
        CalendarWidgetProvider.updateAll(this);
        Toast.makeText(this,"تم تطبيق التصميم",Toast.LENGTH_SHORT).show();
    }

    private void pickBackground(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,REQ_BG);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=REQ_BG||resultCode!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        try{
            getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }catch(Exception ignored){}
        prefs.edit().putString("background_uri",uri.toString()).apply();
        CalendarWidgetProvider.updateAll(this);
        Toast.makeText(this,"تم اختيار الخلفية",Toast.LENGTH_SHORT).show();
    }

    private Spinner spinner(String[] values){
        Spinner s=new Spinner(this);
        s.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,values));
        return s;
    }

    private void addLabel(LinearLayout root,String label){
        TextView v=text(label,14,0xFFFFFFFF);
        v.setGravity(Gravity.RIGHT);
        root.addView(v,full(0,5));
    }

    private void pin(){
        AppWidgetManager manager=getSystemService(AppWidgetManager.class);
        ComponentName provider=new ComponentName(this,CalendarWidgetProvider.class);
        if(manager!=null&&manager.isRequestPinAppWidgetSupported()){
            Bundle extras=new Bundle();
            extras.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,320);
            extras.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,480);
            manager.requestPinAppWidget(provider,extras,null);
        }else{
            Toast.makeText(this,"أضف BAS Calendar من قائمة Widgets ثم كبّره ليملأ الصفحة",Toast.LENGTH_LONG).show();
        }
    }

    private TextView text(String s,int size,int color){
        TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);return v;
    }
    private LinearLayout.LayoutParams full(int top,int bottom){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(top),0,dp(bottom));return p;
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}

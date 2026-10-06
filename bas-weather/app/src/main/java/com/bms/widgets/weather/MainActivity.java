package com.bms.widgets.weather;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(38), dp(24), dp(30));
        root.setBackgroundColor(0xFF101722);

        TextView title = text("BAS Weather", 28, 0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, full(0, 8));

        TextView sub = text("ويدجت طقس تبوك", 19, 0xFFD6E8F7);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub, full(0, 20));

        TextView info = text(
                "يعرض حالة الجو بالشمس أو القمر والغائم والغائم جزئيًا، ويظهر الغبار فوق الرمز عند رصده، مع درجة الحرارة والرطوبة.",
                15, 0xFFB7C5D3);
        info.setGravity(Gravity.CENTER);
        root.addView(info, full(0, 24));

        Button add = new Button(this);
        add.setText("إضافة ويدجت الطقس");
        add.setAllCaps(false);
        add.setTextSize(17);
        add.setOnClickListener(v -> pinWidget());
        root.addView(add, full(0, 10));

        Button refresh = new Button(this);
        refresh.setText("تحديث الطقس الآن");
        refresh.setAllCaps(false);
        refresh.setTextSize(17);
        refresh.setOnClickListener(v -> {
            WeatherWidgetProvider.enqueueRefresh(this);
            Toast.makeText(this, "جارٍ تحديث الطقس", Toast.LENGTH_SHORT).show();
        });
        root.addView(refresh, full(0, 12));

        TextView note = text("التحديث التلقائي كل نحو 30 دقيقة · الموقع: تبوك", 12, 0xFF8FA8BD);
        note.setGravity(Gravity.CENTER);
        root.addView(note, full(6, 0));

        setContentView(root);
        WeatherWidgetProvider.schedulePeriodic(this);
    }

    private void pinWidget() {
        AppWidgetManager manager = getSystemService(AppWidgetManager.class);
        ComponentName provider = new ComponentName(this, WeatherWidgetProvider.class);
        if (manager != null && manager.isRequestPinAppWidgetSupported()) {
            manager.requestPinAppWidget(provider, null, null);
        } else {
            Toast.makeText(this, "أضف BAS Weather من قائمة Widgets في الشاشة الرئيسية", Toast.LENGTH_LONG).show();
        }
    }

    private TextView text(String value, int size, int color) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setLineSpacing(0f, 1.15f);
        return v;
    }

    private LinearLayout.LayoutParams full(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(top), 0, dp(bottom));
        return p;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}

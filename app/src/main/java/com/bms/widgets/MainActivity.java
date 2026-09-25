package com.bms.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(36), dp(24), dp(24));
        root.setBackgroundColor(0xFF101318);

        TextView title = text("BMS Widgets", 28, 0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap(dp(0), dp(14)));

        TextView subtitle = text("ويدجت التاريخ والوقت", 20, 0xFFE7E9ED);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, matchWrap(dp(0), dp(8)));

        TextView desc = text("يعرض اليوم والتاريخ الميلادي والهجري والوقت على سطح المكتب.\n\nاضغط الزر أدناه ثم اختر BMS Widgets من قائمة الويدجت.", 16, 0xFFB9C0CA);
        desc.setGravity(Gravity.CENTER);
        root.addView(desc, matchWrap(dp(0), dp(28)));

        Button addButton = new Button(this);
        addButton.setText("إضافة الويدجت");
        addButton.setAllCaps(false);
        addButton.setTextSize(17);
        addButton.setOnClickListener(v -> requestWidgetPin());
        root.addView(addButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)));

        TextView note = text("إذا لم يفتح الاختيار تلقائيًا: اضغط مطولًا على سطح المكتب ← Widgets ← BMS Widgets.", 14, 0xFF8F98A5);
        note.setGravity(Gravity.CENTER);
        root.addView(note, matchWrap(dp(0), dp(16)));

        setContentView(root);
    }

    private void requestWidgetPin() {
        AppWidgetManager manager = getSystemService(AppWidgetManager.class);
        ComponentName provider = new ComponentName(this, DateTimeWidgetProvider.class);
        if (manager != null && manager.isRequestPinAppWidgetSupported()) {
            manager.requestPinAppWidget(provider, null, null);
        } else {
            Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
            try { startActivity(intent); } catch (Exception ignored) {}
        }
    }

    private TextView text(String value, int sizeSp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setLineSpacing(0f, 1.15f);
        return view;
    }

    private LinearLayout.LayoutParams matchWrap(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, top, 0, bottom);
        return p;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

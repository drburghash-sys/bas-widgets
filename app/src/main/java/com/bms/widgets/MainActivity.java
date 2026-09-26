package com.bms.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PREFS = "bms_widget_prefs";
    private SharedPreferences prefs;
    private SeekBar fontSize;
    private Spinner colorSpinner, themeSpinner, clockSpinner;
    private Spinner clockWidgetTypeSpinner, analogStyleSpinner, digitalStyleSpinner, clockLogoSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xFF101318);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(36));
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        TextView title = text("BMS Widgets", 28, 0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, fullWrap(0, 6));

        TextView subtitle = text("إعدادات التاريخ والصلاة", 18, 0xFFC7CED8);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, fullWrap(0, 22));

        root.addView(section("حجم الخط"), fullWrap(0, 4));
        fontSize = new SeekBar(this);
        fontSize.setMax(80);
        int savedPercent = prefs.getInt("font_percent", 100);
        fontSize.setProgress(Math.max(0, Math.min(80, savedPercent - 70)));
        root.addView(fontSize, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(text("70%                                      150%", 12, 0xFF8F98A5), fullWrap(0, 16));

        root.addView(section("نظام الوقت"), fullWrap(0, 6));
        clockSpinner = spinner(new String[]{"12 ساعة", "24 ساعة"}, prefs.getInt("clock_format_index", 0));
        root.addView(clockSpinner, fullWrap(0, 18));

        root.addView(section("لون الخط"), fullWrap(0, 6));
        colorSpinner = spinner(new String[]{"أبيض", "كريمي", "أزرق فاتح", "أخضر فاتح", "ذهبي", "أحمر فاتح"},
                prefs.getInt("text_color_index", 0));
        root.addView(colorSpinner, fullWrap(0, 18));

        root.addView(section("الخلفية"), fullWrap(0, 6));
        themeSpinner = spinner(new String[]{"داكنة", "شفافة", "ليفربول — أحمر", "النصر", "السعودية — أخضر"},
                prefs.getInt("theme_index", 0));
        root.addView(themeSpinner, fullWrap(0, 20));

        Button save = button("حفظ إعدادات التاريخ والصلاة");
        save.setOnClickListener(v -> saveAndApply());
        root.addView(save, fullButton(0));

        Button addCombined = button("إضافة ويدجت التاريخ + الصلاة");
        addCombined.setOnClickListener(v -> pinWidget(CombinedWidgetProvider.class));
        root.addView(addCombined, fullButton(14));

        Button addDate = button("إضافة ويدجت التاريخ فقط");
        addDate.setOnClickListener(v -> pinWidget(DateTimeWidgetProvider.class));
        root.addView(addDate, fullButton(8));

        Button addPrayer = button("إضافة ويدجت الصلاة فقط");
        addPrayer.setOnClickListener(v -> pinWidget(PrayerTimesWidgetProvider.class));
        root.addView(addPrayer, fullButton(8));

        TextView tasksTitle = text("ويدجت المهام اليومية", 21, 0xFFFFFFFF);
        tasksTitle.setGravity(Gravity.CENTER);
        root.addView(tasksTitle, fullWrap(30, 12));

        TextView tasksInfo = text(
                "تعرض أقرب 3 مهام حسب الوقت. الضغط على ✓ ينهي المهمة لليوم فقط، وتعود تلقائيًا في اليوم التالي.",
                13, 0xFFAAB2BE);
        tasksInfo.setGravity(Gravity.CENTER);
        root.addView(tasksInfo, fullWrap(0, 12));

        Button manageTasks = button("إدارة المهام اليومية");
        manageTasks.setOnClickListener(v -> startActivity(new Intent(this, TaskManagerActivity.class)));
        root.addView(manageTasks, fullButton(0));

        Button addTasksWidget = button("إضافة ويدجت المهام");
        addTasksWidget.setOnClickListener(v -> pinWidget(TaskWidgetProvider.class));
        root.addView(addTasksWidget, fullButton(8));

        TextView clockTitle = text("ويدجت الساعة المستقلة", 21, 0xFFFFFFFF);
        clockTitle.setGravity(Gravity.CENTER);
        root.addView(clockTitle, fullWrap(30, 14));

        root.addView(section("نوع الساعة"), fullWrap(0, 6));
        clockWidgetTypeSpinner = spinner(new String[]{"عقارب", "رقمية"}, prefs.getInt("clock_widget_type", 0));
        root.addView(clockWidgetTypeSpinner, fullWrap(0, 16));

        root.addView(section("تصميم العقارب — 15 شكل"), fullWrap(0, 6));
        analogStyleSpinner = spinner(new String[]{
                "01 كلاسيكي بالأرقام","02 أبيض بسيط بلا أرقام","03 ليفربول بالأرقام",
                "04 سعودي بلا أرقام","05 النصر بالأرقام","06 ليلي بلا أرقام",
                "07 ذهبي روماني","08 برونزي بلا أرقام","09 عاجي بالأرقام",
                "10 زمردي بلا أرقام","11 رياضي بالأرقام","12 أزرق ملكي بلا أرقام",
                "13 كربوني روماني","14 بنفسجي بلا أرقام","15 زجاجي بالأرقام"
        }, prefs.getInt("analog_style_index", 0));
        root.addView(analogStyleSpinner, fullWrap(0, 16));

        root.addView(section("تصميم الرقمي — 15 شكل"), fullWrap(0, 6));
        digitalStyleSpinner = spinner(new String[]{
                "01 أسود أبيض","02 أبيض أسود","03 ليفربول","04 سعودي","05 النصر",
                "06 أزرق ليلي","07 ذهبي","08 برونزي","09 عاجي","10 زمردي",
                "11 أحمر رياضي","12 أزرق ملكي","13 كربوني","14 بنفسجي","15 زجاجي"
        }, prefs.getInt("digital_style_index", 0));
        root.addView(digitalStyleSpinner, fullWrap(0, 16));

        root.addView(section("الشعار في الساعة"), fullWrap(0, 6));
        clockLogoSpinner = spinner(new String[]{"بدون شعار", "ليفربول", "النصر", "السعودية"},
                prefs.getInt("clock_logo_index", 0));
        root.addView(clockLogoSpinner, fullWrap(0, 18));

        Button saveClock = button("حفظ شكل الساعة");
        saveClock.setOnClickListener(v -> saveClockAndApply());
        root.addView(saveClock, fullButton(0));

        Button addClock = button("إضافة ويدجت الساعة");
        addClock.setOnClickListener(v -> pinWidget(ClockWidgetProvider.class));
        root.addView(addClock, fullButton(10));

        setContentView(scroll);
    }

    private Spinner spinner(String[] items, int selection) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items));
        s.setSelection(Math.max(0, Math.min(items.length - 1, selection)));
        return s;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(17);
        return b;
    }

    private LinearLayout.LayoutParams fullButton(int top) {
        LinearLayout.LayoutParams p = fullWrap(top, 0);
        p.height = dp(52);
        return p;
    }

    private void saveAndApply() {
        prefs.edit()
                .putInt("font_percent", fontSize.getProgress() + 70)
                .putInt("clock_format_index", clockSpinner.getSelectedItemPosition())
                .putInt("text_color_index", colorSpinner.getSelectedItemPosition())
                .putInt("theme_index", themeSpinner.getSelectedItemPosition())
                .apply();

        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        updateAll(manager, DateTimeWidgetProvider.class, 1);
        updateAll(manager, PrayerTimesWidgetProvider.class, 2);
        updateAll(manager, CombinedWidgetProvider.class, 3);
        updateAll(manager, ClockWidgetProvider.class, 4);

        ComponentName taskProvider = new ComponentName(this, TaskWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(taskProvider))
            TaskWidgetProvider.updateWidget(this, manager, id);

        Toast.makeText(this, "تم تطبيق الإعدادات", Toast.LENGTH_SHORT).show();
    }

    private void saveClockAndApply() {
        prefs.edit()
                .putInt("clock_widget_type", clockWidgetTypeSpinner.getSelectedItemPosition())
                .putInt("analog_style_index", analogStyleSpinner.getSelectedItemPosition())
                .putInt("digital_style_index", digitalStyleSpinner.getSelectedItemPosition())
                .putInt("clock_logo_index", clockLogoSpinner.getSelectedItemPosition())
                .apply();

        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, ClockWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(provider))
            ClockWidgetProvider.updateWidget(this, manager, id);
        Toast.makeText(this, "تم تطبيق شكل الساعة", Toast.LENGTH_SHORT).show();
    }

    private void updateAll(AppWidgetManager manager, Class<?> cls, int type) {
        ComponentName provider = new ComponentName(this, cls);
        for (int id : manager.getAppWidgetIds(provider)) {
            if (type == 1) DateTimeWidgetProvider.updateWidget(this, manager, id);
            else if (type == 2) PrayerTimesWidgetProvider.updateWidget(this, manager, id);
            else if (type == 3) CombinedWidgetProvider.updateWidget(this, manager, id);
            else ClockWidgetProvider.updateWidget(this, manager, id);
        }
    }

    private void pinWidget(Class<?> providerClass) {
        AppWidgetManager manager = getSystemService(AppWidgetManager.class);
        ComponentName provider = new ComponentName(this, providerClass);
        if (manager != null && manager.isRequestPinAppWidgetSupported()) {
            manager.requestPinAppWidget(provider, null, null);
        } else {
            Toast.makeText(this, "أضف الويدجت من قائمة Widgets في الشاشة الرئيسية", Toast.LENGTH_LONG).show();
        }
    }

    private TextView section(String value) {
        TextView view = text(value, 16, 0xFFFFFFFF);
        view.setGravity(Gravity.RIGHT);
        view.setTextDirection(View.TEXT_DIRECTION_RTL);
        return view;
    }

    private TextView text(String value, int sizeSp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setLineSpacing(0f, 1.15f);
        return view;
    }

    private LinearLayout.LayoutParams fullWrap(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(top), 0, dp(bottom));
        return p;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

package com.bms.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String PREFS = "bms_widget_prefs";
    private SharedPreferences prefs;
    private SeekBar fontSize;
    private Spinner colorSpinner;
    private Spinner themeSpinner;
    private Spinner clockSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(28));
        root.setBackgroundColor(0xFF101318);

        TextView title = text("BMS Widgets", 28, 0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, fullWrap(0, 6));

        TextView subtitle = text("إعدادات الويدجت", 18, 0xFFC7CED8);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, fullWrap(0, 22));

        root.addView(section("حجم الخط"), fullWrap(0, 4));

        fontSize = new SeekBar(this);
        fontSize.setMax(80);
        int savedPercent = prefs.getInt("font_percent", 100);
        fontSize.setProgress(Math.max(0, Math.min(80, savedPercent - 70)));
        root.addView(fontSize, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView sizeHint = text("70%                                      150%", 12, 0xFF8F98A5);
        root.addView(sizeHint, fullWrap(0, 16));

        TextView unifiedHint = text(
                "حجم الخط يطبّق معًا على التاريخ والهجري والميلادي وأوقات الصلاة.",
                13, 0xFF9DA6B2);
        unifiedHint.setGravity(Gravity.RIGHT);
        root.addView(unifiedHint, fullWrap(0, 14));

        root.addView(section("نظام الساعة"), fullWrap(0, 6));
        clockSpinner = new Spinner(this);
        String[] clockModes = {"12 ساعة", "24 ساعة"};
        clockSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clockModes));
        clockSpinner.setSelection(prefs.getInt("clock_format_index", 0));
        root.addView(clockSpinner, fullWrap(0, 18));

        root.addView(section("لون الخط"), fullWrap(0, 6));
        colorSpinner = new Spinner(this);
        String[] colors = {"أبيض", "كريمي", "أزرق فاتح", "أخضر فاتح", "ذهبي", "أحمر فاتح"};
        colorSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, colors));
        colorSpinner.setSelection(prefs.getInt("text_color_index", 0));
        root.addView(colorSpinner, fullWrap(0, 18));

        root.addView(section("الخلفية"), fullWrap(0, 6));
        themeSpinner = new Spinner(this);
        String[] themes = {
                "داكنة",
                "شفافة",
                "ليفربول — أحمر وأبيض",
                "النصر",
                "السعودية — أخضر وأبيض"
        };
        themeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, themes));
        themeSpinner.setSelection(prefs.getInt("theme_index", 0));
        root.addView(themeSpinner, fullWrap(0, 10));

        TextView backgroundHint = text(
                "الخلفيات تتكيف تلقائيًا مع تمديد الويدجت عرضًا وطولًا بدون تغيير حجم النص.",
                13, 0xFF9DA6B2);
        backgroundHint.setGravity(Gravity.RIGHT);
        root.addView(backgroundHint, fullWrap(0, 20));

        Button save = new Button(this);
        save.setText("حفظ وتطبيق على كل الويدجت");
        save.setAllCaps(false);
        save.setTextSize(17);
        save.setOnClickListener(v -> saveAndApply());
        root.addView(save, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54)));

        Button addCombined = new Button(this);
        addCombined.setText("إضافة ويدجت التاريخ + الصلاة");
        addCombined.setAllCaps(false);
        addCombined.setOnClickListener(v -> pinWidget(CombinedWidgetProvider.class));
        LinearLayout.LayoutParams combinedParams = fullWrap(14, 0);
        combinedParams.height = dp(52);
        root.addView(addCombined, combinedParams);

        Button addDate = new Button(this);
        addDate.setText("إضافة ويدجت التاريخ فقط");
        addDate.setAllCaps(false);
        addDate.setOnClickListener(v -> pinWidget(DateTimeWidgetProvider.class));
        LinearLayout.LayoutParams addDateParams = fullWrap(8, 0);
        addDateParams.height = dp(48);
        root.addView(addDate, addDateParams);

        Button addPrayer = new Button(this);
        addPrayer.setText("إضافة ويدجت الصلاة فقط");
        addPrayer.setAllCaps(false);
        addPrayer.setOnClickListener(v -> pinWidget(PrayerTimesWidgetProvider.class));
        LinearLayout.LayoutParams addPrayerParams = fullWrap(8, 0);
        addPrayerParams.height = dp(48);
        root.addView(addPrayer, addPrayerParams);

        TextView note = text(
                "يمكن تغيير حجم كل ويدجت من الشاشة الرئيسية، وتبقى النصوص مستقلة عن أبعاد الخلفية.",
                13, 0xFF8F98A5);
        note.setGravity(Gravity.CENTER);
        root.addView(note, fullWrap(16, 0));

        setContentView(root);
    }

    private void saveAndApply() {
        int fontPercent = fontSize.getProgress() + 70;
        prefs.edit()
                .putInt("font_percent", fontPercent)
                .putInt("clock_format_index", clockSpinner.getSelectedItemPosition())
                .putInt("text_color_index", colorSpinner.getSelectedItemPosition())
                .putInt("theme_index", themeSpinner.getSelectedItemPosition())
                .apply();

        AppWidgetManager manager = AppWidgetManager.getInstance(this);

        ComponentName dateProvider = new ComponentName(this, DateTimeWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(dateProvider)) {
            DateTimeWidgetProvider.updateWidget(this, manager, id);
        }

        ComponentName prayerProvider = new ComponentName(this, PrayerTimesWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(prayerProvider)) {
            PrayerTimesWidgetProvider.updateWidget(this, manager, id);
        }

        ComponentName combinedProvider = new ComponentName(this, CombinedWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(combinedProvider)) {
            CombinedWidgetProvider.updateWidget(this, manager, id);
        }

        Toast.makeText(this, "تم تطبيق الإعدادات على كل الويدجت", Toast.LENGTH_SHORT).show();
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
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(top), 0, dp(bottom));
        return p;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

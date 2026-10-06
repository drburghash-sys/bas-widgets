package com.bms.widgets.weather;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_LOCATION = 77;
    private TextView locationText;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(38), dp(24), dp(30));
        root.setBackgroundColor(0xFF101722);

        TextView title = text("BAS Weather", 28, 0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, full(0, 8));

        TextView sub = text("الطقس · الموقع · البطارية", 18, 0xFFD6E8F7);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub, full(0, 18));

        locationText = text(placeLine(), 15, 0xFFB7C5D3);
        locationText.setGravity(Gravity.CENTER);
        root.addView(locationText, full(0, 20));

        Button location = button("تحديث المدينة والحي");
        location.setOnClickListener(v -> ensureLocation());
        root.addView(location, full(0, 8));

        Button refresh = button("تحديث الطقس الآن");
        refresh.setOnClickListener(v -> {
            WeatherWidgetProvider.updateAll(this);
            WeatherWidgetProvider.enqueueRefresh(this);
            Toast.makeText(this, "جارٍ تحديث الطقس", Toast.LENGTH_SHORT).show();
        });
        root.addView(refresh, full(0, 8));

        Button add = button("إضافة ويدجت الطقس");
        add.setOnClickListener(v -> pinWidget());
        root.addView(add, full(0, 12));

        TextView note = text("البطارية تُقرأ من الجهاز مباشرة · الطقس يتحدث تلقائيًا كل نحو 30 دقيقة", 12, 0xFF8FA8BD);
        note.setGravity(Gravity.CENTER);
        root.addView(note, full(6, 0));

        setContentView(root);
        WeatherWidgetProvider.schedulePeriodic(this);

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateLocation();
        }
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(17);
        return b;
    }

    private String placeLine() {
        WeatherStore.Place p = WeatherStore.loadPlace(this);
        return "📍 " + p.city + " · " + p.district;
    }

    private void ensureLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateLocation();
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQ_LOCATION);
        }
    }

    private void updateLocation() {
        Toast.makeText(this, "جارٍ تحديد الموقع", Toast.LENGTH_SHORT).show();
        LocationHelper.refresh(this, new LocationHelper.Callback() {
            @Override public void onSuccess(String city, String district) {
                locationText.setText("📍 " + city + " · " + district);
                WeatherWidgetProvider.updateAll(MainActivity.this);
                WeatherWidgetProvider.enqueueRefresh(MainActivity.this);
                Toast.makeText(MainActivity.this, "تم تحديث المدينة والحي", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_LOCATION && results.length > 0) {
            boolean granted = false;
            for (int r : results) if (r == PackageManager.PERMISSION_GRANTED) granted = true;
            if (granted) updateLocation();
            else Toast.makeText(this, "لن يظهر اسم الحي بدون إذن الموقع", Toast.LENGTH_LONG).show();
        }
    }

    private void pinWidget() {
        AppWidgetManager manager = getSystemService(AppWidgetManager.class);
        ComponentName provider = new ComponentName(this, WeatherWidgetProvider.class);
        if (manager != null && manager.isRequestPinAppWidgetSupported()) {
            manager.requestPinAppWidget(provider, null, null);
        } else {
            Toast.makeText(this, "أضف BAS Weather من قائمة Widgets", Toast.LENGTH_LONG).show();
        }
    }

    private TextView text(String value, int size, int color) {
        TextView v = new TextView(this);
        v.setText(value); v.setTextSize(size); v.setTextColor(color); v.setLineSpacing(0f, 1.15f);
        return v;
    }

    private LinearLayout.LayoutParams full(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(top), 0, dp(bottom)); return p;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}

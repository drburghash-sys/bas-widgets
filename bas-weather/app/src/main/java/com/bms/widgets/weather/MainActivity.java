package com.bms.widgets.weather;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_LOCATION = 77;
    private static final int REQUEST_BACKGROUND_IMAGE = 2023;
    private TextView locationText;
    private TextView fontStatus;
    private TextView backgroundStatus;
    private ImageView backgroundPreview;

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

        TextView fontTitle = text("حجم خط الويدجت", 19, 0xFFFFFFFF);
        fontTitle.setGravity(Gravity.CENTER);
        root.addView(fontTitle, full(0, 8));

        int initialPercent = WeatherStore.fontPercent(this);
        fontStatus = text("حجم الخط: " + WeatherStore.arabicDigits(initialPercent + "٪"), 16, 0xFFD6E8F7);
        fontStatus.setGravity(Gravity.CENTER);
        root.addView(fontStatus, full(0, 6));

        SeekBar fontSlider = new SeekBar(this);
        fontSlider.setMax(80);
        fontSlider.setProgress(initialPercent - 70);
        fontSlider.setContentDescription("تعديل حجم خط ويدجت الطقس من سبعين إلى مئة وخمسين بالمئة");
        fontSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar slider, int progress, boolean fromUser) {
                int percent = 70 + progress;
                fontStatus.setText("حجم الخط: " + WeatherStore.arabicDigits(percent + "٪"));
                if (fromUser) {
                    WeatherStore.saveFontPercent(MainActivity.this, percent);
                    WeatherWidgetProvider.updateAll(MainActivity.this);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar slider) {}
            @Override public void onStopTrackingTouch(SeekBar slider) {}
        });
        root.addView(fontSlider, full(0, 4));

        Button defaultFont = button("إعادة حجم الخط إلى ١٠٠٪");
        defaultFont.setOnClickListener(v -> fontSlider.setProgress(30));
        root.addView(defaultFont, full(0, 14));

        TextView backgroundHeading = text("خلفية ويدجت الطقس", 19, 0xFFFFFFFF);
        backgroundHeading.setGravity(Gravity.CENTER);
        root.addView(backgroundHeading, full(8, 8));

        backgroundStatus = text("", 15, 0xFFD6E8F7);
        backgroundStatus.setGravity(Gravity.CENTER);
        root.addView(backgroundStatus, full(0, 8));

        backgroundPreview = new ImageView(this);
        backgroundPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        backgroundPreview.setBackgroundColor(0xFF17283B);
        root.addView(backgroundPreview, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(120)));

        Button chooseBackground = button("اختيار صورة خلفية من المعرض");
        chooseBackground.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("image/*");
            try {
                startActivityForResult(pick, REQUEST_BACKGROUND_IMAGE);
            } catch (android.content.ActivityNotFoundException error) {
                Toast.makeText(this, "تعذر فتح معرض الصور", Toast.LENGTH_LONG).show();
            }
        });
        root.addView(chooseBackground, full(12, 8));

        int initialTransparency = WeatherStore.backgroundTransparency(this);
        TextView transparencyLabel = text(
                "شفافية الصورة: " + WeatherStore.arabicDigits(initialTransparency + "٪"),
                16, 0xFFD6E8F7);
        transparencyLabel.setGravity(Gravity.CENTER);
        root.addView(transparencyLabel, full(0, 5));

        SeekBar transparencySlider = new SeekBar(this);
        transparencySlider.setMax(100);
        transparencySlider.setProgress(initialTransparency);
        transparencySlider.setContentDescription("شفافية صورة خلفية الطقس: صفر يعني واضحة تمامًا، مئة يعني مخفية");
        transparencySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar slider, int percent, boolean fromUser) {
                transparencyLabel.setText("شفافية الصورة: " +
                        WeatherStore.arabicDigits(percent + "٪"));
                if (fromUser) WeatherStore.saveBackgroundTransparency(MainActivity.this, percent);
            }
            @Override public void onStartTrackingTouch(SeekBar slider) {}
            @Override public void onStopTrackingTouch(SeekBar slider) {
                WeatherStore.saveBackgroundTransparency(MainActivity.this, slider.getProgress());
                WeatherWidgetProvider.updateAll(MainActivity.this);
            }
        });
        root.addView(transparencySlider, full(0, 4));
        TextView alphaGuide = text(
                "٠٪ = الصورة واضحة بالكامل   ·   ١٠٠٪ = الصورة مخفية",
                12, 0xFF9BAFC2);
        alphaGuide.setGravity(Gravity.CENTER);
        root.addView(alphaGuide, full(0, 10));

        Button clearBackground = button("حذف الصورة واستعادة الخلفية الأصلية");
        clearBackground.setOnClickListener(v -> {
            WeatherBackground.clear(this);
            refreshBackgroundPreview();
            WeatherWidgetProvider.updateAll(this);
            Toast.makeText(this, "تمت استعادة الخلفية الأصلية", Toast.LENGTH_SHORT).show();
        });
        root.addView(clearBackground, full(0, 18));
        refreshBackgroundPreview();

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

        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(true);
        scroller.addView(root);
        setContentView(scroller);
        WeatherWidgetProvider.schedulePeriodic(this);

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateLocation();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_BACKGROUND_IMAGE
                || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        android.net.Uri selectedImage = data.getData();
        Toast.makeText(this, "جارٍ تجهيز صورة الخلفية", Toast.LENGTH_SHORT).show();
        // Decoding large gallery photos must not block the UI thread.
        new Thread(() -> {
            try {
                WeatherBackground.savePhoto(getApplicationContext(), selectedImage);
                runOnUiThread(() -> {
                    refreshBackgroundPreview();
                    WeatherWidgetProvider.updateAll(MainActivity.this);
                    Toast.makeText(MainActivity.this,
                            "تم تطبيق الصورة على ويدجت الطقس", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception exception) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "تعذر استخدام الصورة: " + exception.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        }, "BAS-Weather-Photo").start();
    }

    private void refreshBackgroundPreview() {
        if (backgroundStatus == null || backgroundPreview == null) return;
        if (!WeatherBackground.hasPhoto(this)) {
            backgroundStatus.setText("الخلفية الأصلية مفعّلة");
            backgroundPreview.setImageDrawable(null);
            backgroundPreview.setVisibility(View.GONE);
            return;
        }
        backgroundStatus.setText("صورة مخصصة محفوظة في التطبيق");
        Bitmap image = WeatherBackground.preview(this);
        if (image != null) {
            backgroundPreview.setImageBitmap(image);
            backgroundPreview.setVisibility(View.VISIBLE);
        } else {
            backgroundPreview.setImageDrawable(null);
            backgroundPreview.setVisibility(View.GONE);
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

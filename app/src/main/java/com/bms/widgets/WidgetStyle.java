package com.bms.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;

public final class WidgetStyle {

    private static final String PREFS = "bms_widget_prefs";

    private WidgetStyle() {}

    public static float getFontScale(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int percent = p.getInt("font_percent", 100);
        return Math.max(0.70f, Math.min(1.50f, percent / 100f));
    }

    public static int getTextColor(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        switch (p.getInt("text_color_index", 0)) {
            case 1: return Color.rgb(255, 244, 214);
            case 2: return Color.rgb(184, 220, 255);
            case 3: return Color.rgb(185, 245, 200);
            case 4: return Color.rgb(255, 216, 110);
            case 5: return Color.rgb(255, 175, 175);
            default: return Color.WHITE;
        }
    }

    public static void applyBackgroundAndWatermark(
            Context context, RemoteViews views, int rootId, int watermarkId) {

        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int theme = p.getInt("theme_index", 0);

        int background;
        String mark;
        int markColor;

        switch (theme) {
            case 1:
                background = R.drawable.widget_bg_transparent;
                mark = "";
                markColor = 0x00FFFFFF;
                break;
            case 2:
                background = R.drawable.widget_bg_liverpool;
                mark = "LFC";
                markColor = 0x20FFFFFF;
                break;
            case 3:
                background = R.drawable.widget_bg_nassr;
                mark = "NASSR";
                markColor = 0x2500306B;
                break;
            case 4:
                background = R.drawable.widget_bg_saudi;
                mark = "🇸🇦";
                markColor = 0x25FFFFFF;
                break;
            default:
                background = R.drawable.widget_background;
                mark = "";
                markColor = 0x00FFFFFF;
        }

        views.setInt(rootId, "setBackgroundResource", background);
        views.setTextViewText(watermarkId, mark);
        views.setTextColor(watermarkId, markColor);
    }

    public static int withAlpha(int color, int alpha) {
        return Color.argb(
                Math.max(0, Math.min(255, alpha)),
                Color.red(color),
                Color.green(color),
                Color.blue(color));
    }
}

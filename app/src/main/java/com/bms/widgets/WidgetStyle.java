package com.bms.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;
import java.util.Locale;

public final class WidgetStyle {
    private static final String PREFS = "bms_widget_prefs";
    private WidgetStyle() {}

    public static float getFontScale(Context context) {
        int percent = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("font_percent", 100);
        return Math.max(0.70f, Math.min(1.50f, percent / 100f));
    }

    public static boolean use24Hour(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("clock_format_index", 0) == 1;
    }

    public static int getTextColor(Context context) {
        switch (context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("text_color_index", 0)) {
            case 1: return Color.rgb(255,244,214);
            case 2: return Color.rgb(184,220,255);
            case 3: return Color.rgb(185,245,200);
            case 4: return Color.rgb(255,216,110);
            case 5: return Color.rgb(255,175,175);
            default: return Color.WHITE;
        }
    }

    public static String formatPrayerTime(Context context, String source) {
        if (source == null || use24Hour(context)) return source;
        try {
            String ascii = source
                    .replace('٠','0').replace('١','1').replace('٢','2').replace('٣','3').replace('٤','4')
                    .replace('٥','5').replace('٦','6').replace('٧','7').replace('٨','8').replace('٩','9');
            String[] parts = ascii.split(":");
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim());
            String period = h < 12 ? "ص" : "م";
            int h12 = h % 12;
            if (h12 == 0) h12 = 12;
            return arabicDigits(String.format(Locale.US, "%d:%02d %s", h12, m, period));
        } catch (Throwable ignored) {
            return source;
        }
    }

    public static String clockLogo(Context context) {
        switch (context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("clock_logo_index", 0)) {
            case 1: return "LFC";
            case 2: return "النصر";
            case 3: return "🇸🇦";
            default: return "";
        }
    }

    public static int clockLogoColor(Context context) {
        switch (context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("clock_logo_index", 0)) {
            case 2: return Color.rgb(255,213,0);
            case 1:
            case 3: return Color.WHITE;
            default: return Color.TRANSPARENT;
        }
    }

    public static void applyBackgroundAndWatermark(Context context, RemoteViews views, int rootId, int watermarkId) {
        int theme = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("theme_index", 0);
        int background;
        String mark;
        int markColor;
        switch (theme) {
            case 1: background=R.drawable.widget_bg_transparent; mark=""; markColor=0x00FFFFFF; break;
            case 2: background=R.drawable.widget_bg_liverpool; mark="LFC"; markColor=0x28FFFFFF; break;
            case 3: background=R.drawable.widget_bg_nassr; mark="NASSR"; markColor=0x2500306B; break;
            case 4: background=R.drawable.widget_bg_saudi; mark="🇸🇦"; markColor=0x30FFFFFF; break;
            default: background=R.drawable.widget_background; mark=""; markColor=0x00FFFFFF;
        }
        views.setInt(rootId, "setBackgroundResource", background);
        views.setTextViewText(watermarkId, mark);
        views.setTextColor(watermarkId, markColor);
    }

    public static int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0,Math.min(255,alpha)), Color.red(color), Color.green(color), Color.blue(color));
    }

    private static String arabicDigits(String source) {
        final char[] digits={'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};
        StringBuilder out=new StringBuilder(source.length());
        for(char c:source.toCharArray()) out.append(c>='0'&&c<='9'?digits[c-'0']:c);
        return out.toString();
    }
}

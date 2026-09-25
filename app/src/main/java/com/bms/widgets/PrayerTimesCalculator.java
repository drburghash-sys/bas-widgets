package com.bms.widgets;

import android.icu.util.Calendar;
import android.icu.util.IslamicCalendar;
import android.icu.util.TimeZone;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

public final class PrayerTimesCalculator {

    // Tabuk city center. V1.1 uses Tabuk as the default prayer-time location.
    private static final double LATITUDE = 28.3833;
    private static final double LONGITUDE = 36.5833;
    private static final ZoneId RIYADH_ZONE = ZoneId.of("Asia/Riyadh");

    private PrayerTimesCalculator() {}

    public static final class Times {
        public final String fajr;
        public final String sunrise;
        public final String dhuhr;
        public final String asr;
        public final String maghrib;
        public final String isha;

        Times(String fajr, String sunrise, String dhuhr,
              String asr, String maghrib, String isha) {
            this.fajr = fajr;
            this.sunrise = sunrise;
            this.dhuhr = dhuhr;
            this.asr = asr;
            this.maghrib = maghrib;
            this.isha = isha;
        }
    }

    public static Times calculateToday() {
        LocalDate date = LocalDate.now(RIYADH_ZONE);
        int n = date.getDayOfYear();

        double gamma = 2.0 * Math.PI / 365.0 * (n - 1);
        double eqTime = 229.18 * (
                0.000075
                        + 0.001868 * Math.cos(gamma)
                        - 0.032077 * Math.sin(gamma)
                        - 0.014615 * Math.cos(2 * gamma)
                        - 0.040849 * Math.sin(2 * gamma));

        double decl = 0.006918
                - 0.399912 * Math.cos(gamma)
                + 0.070257 * Math.sin(gamma)
                - 0.006758 * Math.cos(2 * gamma)
                + 0.000907 * Math.sin(2 * gamma)
                - 0.002697 * Math.cos(3 * gamma)
                + 0.00148 * Math.sin(3 * gamma);

        ZonedDateTime noon = date.atTime(12, 0).atZone(RIYADH_ZONE);
        double tzHours = noon.getOffset().getTotalSeconds() / 3600.0;

        double solarNoon = 720.0 - 4.0 * LONGITUDE - eqTime + tzHours * 60.0;

        double sunriseHA = hourAngleForZenith(90.833, decl);
        double fajrHA = hourAngleForZenith(108.5, decl);

        double sunrise = solarNoon - 4.0 * sunriseHA;
        double maghrib = solarNoon + 4.0 * sunriseHA;
        double fajr = solarNoon - 4.0 * fajrHA;
        double dhuhr = solarNoon + 1.0;

        double asrAltitude = Math.toDegrees(Math.atan(
                1.0 / (1.0 + Math.tan(Math.toRadians(
                        Math.abs(LATITUDE - Math.toDegrees(decl)))))));

        double asrHA = hourAngleForAltitude(asrAltitude, decl);
        double asr = solarNoon + 4.0 * asrHA;

        double isha = maghrib + (isRamadan() ? 120.0 : 90.0);

        return new Times(
                formatMinutes(fajr),
                formatMinutes(sunrise),
                formatMinutes(dhuhr),
                formatMinutes(asr),
                formatMinutes(maghrib),
                formatMinutes(isha));
    }

    private static double hourAngleForZenith(double zenithDegrees, double declinationRad) {
        double latRad = Math.toRadians(LATITUDE);
        double cosH =
                (Math.cos(Math.toRadians(zenithDegrees)) /
                        (Math.cos(latRad) * Math.cos(declinationRad)))
                        - Math.tan(latRad) * Math.tan(declinationRad);
        cosH = Math.max(-1.0, Math.min(1.0, cosH));
        return Math.toDegrees(Math.acos(cosH));
    }

    private static double hourAngleForAltitude(double altitudeDegrees, double declinationRad) {
        double latRad = Math.toRadians(LATITUDE);
        double sinAlt = Math.sin(Math.toRadians(altitudeDegrees));
        double cosH =
                (sinAlt - Math.sin(latRad) * Math.sin(declinationRad))
                        / (Math.cos(latRad) * Math.cos(declinationRad));
        cosH = Math.max(-1.0, Math.min(1.0, cosH));
        return Math.toDegrees(Math.acos(cosH));
    }

    private static boolean isRamadan() {
        try {
            IslamicCalendar cal = new IslamicCalendar(
                    TimeZone.getTimeZone("Asia/Riyadh"),
                    new Locale("ar", "SA"));
            cal.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC_UMALQURA);
            cal.setTimeInMillis(System.currentTimeMillis());
            return cal.get(Calendar.MONTH) == 8;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static String formatMinutes(double minutes) {
        int total = (int) Math.round(minutes);
        total = ((total % 1440) + 1440) % 1440;
        int h = total / 60;
        int m = total % 60;
        return arabicDigits(String.format(Locale.US, "%02d:%02d", h, m));
    }

    private static String arabicDigits(String source) {
        final char[] digits = {'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};
        StringBuilder out = new StringBuilder(source.length());
        for (char c : source.toCharArray()) {
            if (c >= '0' && c <= '9') out.append(digits[c - '0']);
            else out.append(c);
        }
        return out.toString();
    }
}

package com.bms.widgets.weather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Private, persistent widget image. The user selects a picture through Android's
 * document picker. A compressed copy is stored inside BAS Weather so deleting
 * or moving the original gallery photo does not break the widget.
 */
public final class WeatherBackground {
    private static final String FILE_NAME = "bas_weather_background.jpg";
    private WeatherBackground() {}

    private static File file(Context context) {
        return new File(context.getFilesDir(), FILE_NAME);
    }

    public static boolean hasPhoto(Context context) {
        File photo = file(context);
        return photo.isFile() && photo.length() > 0;
    }

    public static void clear(Context context) {
        File photo = file(context);
        if (photo.exists()) photo.delete();
    }

    public static void savePhoto(Context context, Uri uri) throws IOException {
        if (uri == null) throw new IOException("لم يتم تحديد صورة");
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            if (stream == null) throw new IOException("تعذر قراءة الصورة");
            BitmapFactory.decodeStream(stream, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0)
            throw new IOException("صيغة الصورة غير مدعومة");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        while (bounds.outWidth / options.inSampleSize > 1500
                || bounds.outHeight / options.inSampleSize > 1500) {
            options.inSampleSize *= 2;
        }
        Bitmap decoded;
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            if (stream == null) throw new IOException("تعذر فتح الصورة");
            decoded = BitmapFactory.decodeStream(stream, null, options);
        }
        if (decoded == null) throw new IOException("تعذر معالجة الصورة");

        int max = Math.max(decoded.getWidth(), decoded.getHeight());
        Bitmap scaled = decoded;
        if (max > 1200) {
            float factor = 1200f / max;
            scaled = Bitmap.createScaledBitmap(decoded,
                    Math.max(1, Math.round(decoded.getWidth() * factor)),
                    Math.max(1, Math.round(decoded.getHeight() * factor)), true);
        }
        File temp = new File(context.getFilesDir(), FILE_NAME + ".new");
        boolean saved;
        try (FileOutputStream output = new FileOutputStream(temp)) {
            saved = scaled.compress(Bitmap.CompressFormat.JPEG, 88, output);
            output.flush();
        } finally {
            if (scaled != decoded) scaled.recycle();
            decoded.recycle();
        }
        if (!saved || temp.length() == 0) {
            temp.delete();
            throw new IOException("تعذر حفظ الصورة");
        }
        try {
            Files.move(temp.toPath(), file(context).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            temp.delete();
            throw e;
        }
    }

    /** Produces a bounded-size alpha bitmap, safe to send to a RemoteViews widget. */
    public static Bitmap widgetPhoto(Context context, int transparency) {
        if (!hasPhoto(context) || transparency >= 100) return null;
        File photo = file(context);
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photo.getAbsolutePath(), bounds);
        if (bounds.outWidth < 1 || bounds.outHeight < 1) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        while (bounds.outWidth / options.inSampleSize > 550
                || bounds.outHeight / options.inSampleSize > 360) {
            options.inSampleSize *= 2;
        }
        Bitmap source = BitmapFactory.decodeFile(photo.getAbsolutePath(), options);
        if (source == null) return null;
        int width = Math.max(1, Math.min(source.getWidth(), 550));
        int height = Math.max(1, Math.min(source.getHeight(), 360));
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha(Math.max(0, Math.min(255,
                Math.round(255f * (100 - transparency) / 100f))));
        canvas.drawBitmap(source, null, new android.graphics.Rect(0, 0, width, height), paint);
        source.recycle();
        return result;
    }

    public static Bitmap preview(Context context) {
        if (!hasPhoto(context)) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        return BitmapFactory.decodeFile(file(context).getAbsolutePath(), options);
    }
}

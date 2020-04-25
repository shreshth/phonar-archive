package com.phonar.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

public class ImageUtils {

    // Image dimensions
    public static final float MAX_WIDTH = 80;
    public static final float MAX_HEIGHT = 80;

    /**
     * Compress image
     * 
     * @param image
     * @return
     */
    public static Bitmap compressBitmap(Bitmap image) {
        int width;
        int height;
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        if (imageWidth < MAX_WIDTH && imageHeight < MAX_HEIGHT) {
            return image;
        }
        if (imageWidth > imageHeight) {
            width = (int) MAX_WIDTH;
            height = (int) (imageHeight / imageWidth * MAX_HEIGHT);
        } else {
            height = (int) MAX_HEIGHT;
            width = (int) (imageWidth / imageHeight * MAX_WIDTH);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /**
     * Convert image to PNG and then encode as base-64
     */
    public static String encodeBitmap(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    public static Bitmap decodeBitmap(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    /**
     * Create a grayscale representation of a bitmap
     * 
     * @param bmpOriginal
     *            Original bitmap
     * @return Grayscale version of bmpOriginal
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap getPaddedBitmap(Bitmap bitmap) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            RectF targetRect = new RectF(10, 10, width + 10, height + 10);
            Bitmap dest = Bitmap.createBitmap(width + 20, height + 20, bitmap.getConfig());
            Canvas canvas = new Canvas(dest);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, null, targetRect, null);

            Bitmap output =
                Bitmap.createBitmap(dest.getWidth(), dest.getHeight(), Config.ARGB_8888);
            canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final float roundPx = 12;
            final Rect rect = new Rect(0, 0, dest.getWidth(), dest.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(dest, rect, rect, paint);

            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }

    private static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor =
            context.getContentResolver().query(
                photoUri, new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null,
                null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public static Bitmap getImageFromURI(Context context, Uri photoUri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_WIDTH || rotatedHeight > MAX_HEIGHT) {
            float widthRatio = rotatedWidth / MAX_WIDTH;
            float heightRatio = rotatedHeight / MAX_HEIGHT;
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap =
                Bitmap.createBitmap(
                    srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

}
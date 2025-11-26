package ai.tech5.pheonix.capture.controller;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

public class ImageUtils {


    public static Bitmap flipBitmap(Bitmap src) {
        Matrix matrix = new Matrix();


        matrix.preScale(-1.0f, 1.0f);


        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }


    public static Bitmap padAndscaleDown(Bitmap realImage, float maxImagewidth, float maxImageHeight,
                                         boolean pad, boolean scaleDown,
                                         boolean filter) {
        if (pad) {
            float w_h_ratio = (float) ((float) realImage.getWidth() / (float) realImage.getHeight());
            if (w_h_ratio < 0.75) {
                int required_width = (int) ((float) realImage.getHeight() * 0.75);
                int offset = (required_width - realImage.getWidth()) / 2;
                Bitmap outputImage = Bitmap.createBitmap(realImage.getWidth() + offset * 2,
                        realImage.getHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas can = new Canvas(outputImage);
                can.drawColor(Color.WHITE);
                can.drawBitmap(realImage, 0, 0, null);
                realImage = outputImage;
            } else if (w_h_ratio > 0.75) {
                int required_height = (int) ((float) realImage.getWidth() / 0.75);
                int offset = (required_height - realImage.getHeight()) / 2;
                Bitmap outputImage = Bitmap.createBitmap(realImage.getWidth(),
                        realImage.getHeight() + offset * 2,
                        Bitmap.Config.ARGB_8888);
                Canvas can = new Canvas(outputImage);
                can.drawColor(Color.WHITE);
                can.drawBitmap(realImage, 0, 0, null);
                realImage = outputImage;
            }
        }


        if (scaleDown) {
            float scale_ratio = Math.min(
                    maxImagewidth / realImage.getWidth(),
                    maxImageHeight / realImage.getHeight());
            if (scale_ratio < 1.0) {
                int width = Math.round(scale_ratio * realImage.getWidth());
                int height = Math.round(scale_ratio * realImage.getHeight());

                return Bitmap.createScaledBitmap(realImage, width,
                        height, filter);

            }

        }
        return realImage;
    }


    public static Bitmap rotateBitmap(Bitmap bInput, int degrees) {

        Matrix matrix = new Matrix();
        matrix.setRotate(degrees);
        return Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);

    }


    public static byte[] compress(Bitmap image, int compressRate) {

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            image.compress(Bitmap.CompressFormat.JPEG, compressRate, stream);
            return stream.toByteArray();

        } catch (Exception ignored) {

        }

        return new byte[0];
    }


    public static byte[] compressImageBelowTargetSize(Bitmap bitmap, int targetSizeInKbs) {
        return compressImageBelowTargetSize(bitmap, 100, targetSizeInKbs);
    }


    private static byte[] compressImageBelowTargetSize(Bitmap bitmap, int compressionRate, int targetSizeInKbs) {
        byte[] compressedData = compress(bitmap, compressionRate);
        int targetSizeInBytes = targetSizeInKbs * 1024;

        if (compressedData.length > targetSizeInBytes && compressionRate > 5) {
            return compressImageBelowTargetSize(bitmap, compressionRate - 1, targetSizeInKbs);
        }
        return compressedData;
    }


}

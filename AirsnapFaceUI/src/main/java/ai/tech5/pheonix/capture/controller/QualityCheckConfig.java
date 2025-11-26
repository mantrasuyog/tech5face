package ai.tech5.pheonix.capture.controller;

import android.os.Parcel;
import android.os.Parcelable;

public class QualityCheckConfig implements Parcelable {

    private float blur_min = 0.0f;
    private float blur_max = 0.5f;
    private float exposure_min = 0.2f;
    private float expoure_max = 0.7f;
    private float brightness_min = 0.2f;
    private float brightness_max = 0.7f;

    public float getBlur_min() {
        return blur_min;
    }

    public void setBlur_min(float blur_min) {
        this.blur_min = blur_min;
    }

    public float getBlur_max() {
        return blur_max;
    }

    public void setBlur_max(float blur_max) {
        this.blur_max = blur_max;
    }

    public float getExposure_min() {
        return exposure_min;
    }

    public void setExposure_min(float exposure_min) {
        this.exposure_min = exposure_min;
    }

    public float getExpoure_max() {
        return expoure_max;
    }

    public void setExpoure_max(float expoure_max) {
        this.expoure_max = expoure_max;
    }

    public float getBrightness_min() {
        return brightness_min;
    }

    public void setBrightness_min(float brightness_min) {
        this.brightness_min = brightness_min;
    }

    public float getBrightness_max() {
        return brightness_max;
    }

    public void setBrightness_max(float brightness_max) {
        this.brightness_max = brightness_max;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.blur_min);
        dest.writeFloat(this.blur_max);
        dest.writeFloat(this.exposure_min);
        dest.writeFloat(this.expoure_max);
        dest.writeFloat(this.brightness_min);
        dest.writeFloat(this.brightness_max);
    }

    public void readFromParcel(Parcel source) {
        this.blur_min = source.readFloat();
        this.blur_max = source.readFloat();
        this.exposure_min = source.readFloat();
        this.expoure_max = source.readFloat();
        this.brightness_min = source.readFloat();
        this.brightness_max = source.readFloat();
    }

    public QualityCheckConfig() {
    }

    protected QualityCheckConfig(Parcel in) {
        this.blur_min = in.readFloat();
        this.blur_max = in.readFloat();
        this.exposure_min = in.readFloat();
        this.expoure_max = in.readFloat();
        this.brightness_min = in.readFloat();
        this.brightness_max = in.readFloat();
    }

    public static final Creator<QualityCheckConfig> CREATOR = new Creator<QualityCheckConfig>() {
        @Override
        public QualityCheckConfig createFromParcel(Parcel source) {
            return new QualityCheckConfig(source);
        }

        @Override
        public QualityCheckConfig[] newArray(int size) {
            return new QualityCheckConfig[size];
        }
    };
}

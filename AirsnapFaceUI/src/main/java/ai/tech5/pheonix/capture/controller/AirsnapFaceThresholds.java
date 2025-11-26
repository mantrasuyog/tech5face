package ai.tech5.pheonix.capture.controller;

import android.os.Parcel;
import android.os.Parcelable;

public class AirsnapFaceThresholds implements Parcelable {


    private int PITCH_THRESHOLD = 15;
    private int YAW_THRESHOLD = 15;
    public int ROLL_THRESHOLD = 10;
    private double MASK_THRESHOLD = 0.5;
    private double ANYGLASS_THRESHOLD = 0.5;
    private double SUNGLASS_THRESHOLD = 0.5;
    private int BRISQUE_THRESHOLD = 60;
    private double LIVENESS_THRESHOLD = 0.5;
    private double EYE_CLOSE_THRESHOLD = 0.8;


    private float FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE = 10;

    private float FACE_TO_IMAGE_WIDTH_TOLERANCE = 10;


    public int getPITCH_THRESHOLD() {
        return PITCH_THRESHOLD;
    }

    public void setPITCH_THRESHOLD(int PITCH_THRESHOLD) {
        this.PITCH_THRESHOLD = PITCH_THRESHOLD;
    }

    public int getYAW_THRESHOLD() {
        return YAW_THRESHOLD;
    }

    public void setYAW_THRESHOLD(int YAW_THRESHOLD) {
        this.YAW_THRESHOLD = YAW_THRESHOLD;
    }


    public int getRollThreshold() {
        return ROLL_THRESHOLD;
    }

    public void setRollThreshold(int rollThreshold) {

        ROLL_THRESHOLD = rollThreshold;

    }

    public double getMASK_THRESHOLD() {
        return MASK_THRESHOLD;
    }

    public void setMASK_THRESHOLD(double MASK_THRESHOLD) {
        this.MASK_THRESHOLD = MASK_THRESHOLD;
    }

    public double getANYGLASS_THRESHOLD() {
        return ANYGLASS_THRESHOLD;
    }

    public void setANYGLASS_THRESHOLD(double ANYGLASS_THRESHOLD) {
        this.ANYGLASS_THRESHOLD = ANYGLASS_THRESHOLD;
    }

    public double getSUNGLASS_THRESHOLD() {
        return SUNGLASS_THRESHOLD;
    }

    public void setSUNGLASS_THRESHOLD(double SUNGLASS_THRESHOLD) {
        this.SUNGLASS_THRESHOLD = SUNGLASS_THRESHOLD;
    }

    public int getBRISQUE_THRESHOLD() {
        return BRISQUE_THRESHOLD;
    }

    public void setBRISQUE_THRESHOLD(int BRISQUE_THRESHOLD) {
        this.BRISQUE_THRESHOLD = BRISQUE_THRESHOLD;
    }

    public double getLIVENESS_THRESHOLD() {
        return LIVENESS_THRESHOLD;
    }

    public void setLIVENESS_THRESHOLD(double LIVENESS_THRESHOLD) {
        this.LIVENESS_THRESHOLD = LIVENESS_THRESHOLD;
    }

    public double getEYE_CLOSE_THRESHOLD() {
        return EYE_CLOSE_THRESHOLD;
    }

    public void setEYE_CLOSE_THRESHOLD(double EYE_CLOSE_THRESHOLD) {
        this.EYE_CLOSE_THRESHOLD = EYE_CLOSE_THRESHOLD;
    }


    public void setFaceWidthToImageWidthRatioTolerance(Float tolerance) {
        FACE_TO_IMAGE_WIDTH_TOLERANCE = tolerance;
    }

    public Float getFaceWidthToImageWidthRatioTolerance() {
        return FACE_TO_IMAGE_WIDTH_TOLERANCE;
    }


    public void setFaceCentreToImageCentreTolerance(Float tolerance) {
        FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE = tolerance;
    }


    public Float getFaceCentreToImageCentreTolerance() {
        return FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE;
    }

    public AirsnapFaceThresholds() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.PITCH_THRESHOLD);
        dest.writeInt(this.YAW_THRESHOLD);
        dest.writeInt(this.ROLL_THRESHOLD);
        dest.writeDouble(this.MASK_THRESHOLD);
        dest.writeDouble(this.ANYGLASS_THRESHOLD);
        dest.writeDouble(this.SUNGLASS_THRESHOLD);
        dest.writeInt(this.BRISQUE_THRESHOLD);
        dest.writeDouble(this.LIVENESS_THRESHOLD);
        dest.writeDouble(this.EYE_CLOSE_THRESHOLD);
        dest.writeFloat(this.FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE);
        dest.writeFloat(this.FACE_TO_IMAGE_WIDTH_TOLERANCE);
    }

    public void readFromParcel(Parcel source) {
        this.PITCH_THRESHOLD = source.readInt();
        this.YAW_THRESHOLD = source.readInt();
        this.ROLL_THRESHOLD = source.readInt();
        this.MASK_THRESHOLD = source.readDouble();
        this.ANYGLASS_THRESHOLD = source.readDouble();
        this.SUNGLASS_THRESHOLD = source.readDouble();
        this.BRISQUE_THRESHOLD = source.readInt();
        this.LIVENESS_THRESHOLD = source.readDouble();
        this.EYE_CLOSE_THRESHOLD = source.readDouble();
        this.FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE = source.readFloat();
        this.FACE_TO_IMAGE_WIDTH_TOLERANCE = source.readFloat();
    }

    protected AirsnapFaceThresholds(Parcel in) {
        this.PITCH_THRESHOLD = in.readInt();
        this.YAW_THRESHOLD = in.readInt();
        this.ROLL_THRESHOLD = in.readInt();
        this.MASK_THRESHOLD = in.readDouble();
        this.ANYGLASS_THRESHOLD = in.readDouble();
        this.SUNGLASS_THRESHOLD = in.readDouble();
        this.BRISQUE_THRESHOLD = in.readInt();
        this.LIVENESS_THRESHOLD = in.readDouble();
        this.EYE_CLOSE_THRESHOLD = in.readDouble();
        this.FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE = in.readFloat();
        this.FACE_TO_IMAGE_WIDTH_TOLERANCE = in.readFloat();
    }

    public static final Creator<AirsnapFaceThresholds> CREATOR = new Creator<AirsnapFaceThresholds>() {
        @Override
        public AirsnapFaceThresholds createFromParcel(Parcel source) {
            return new AirsnapFaceThresholds(source);
        }

        @Override
        public AirsnapFaceThresholds[] newArray(int size) {
            return new AirsnapFaceThresholds[size];
        }
    };
}

package ai.tech5.pheonix.capture.controller;


import android.os.Parcel;
import android.os.Parcelable;

public class FullFrontalCropConfig implements Parcelable {


    private double portalWidth = 1200;
    private ImageType imageType = ImageType.IMAGE_TYPE_JPG;
    private double  compression = 0.0;

    private boolean getSegmentedImage = true;

    private int[] segmentedImageBackgroundColor = {125,125,125};

    public boolean isGetSegmentedImage() {
        return getSegmentedImage;
    }

    public int[] getSegmentedImageBackgroundColor() {
        return segmentedImageBackgroundColor;
    }

    public void getSegmentedImage(boolean getSegmentedImage) {
        this.getSegmentedImage = getSegmentedImage;
    }

    public void setSegmentedImageBackgroundColor(int r, int g, int b) {
        this.segmentedImageBackgroundColor[0] = r;
        this.segmentedImageBackgroundColor[1] = g;
        this.segmentedImageBackgroundColor[2] = b;
    }

    public double getPortalWidth() {
        return portalWidth;
    }

    public void setPortalWidth(double portalWidth) {
        this.portalWidth = portalWidth;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public double getCompression() {
        return compression;
    }

    public void setCompression(double compression) {
        this.compression = compression;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.portalWidth);
        dest.writeInt(this.imageType == null ? -1 : this.imageType.ordinal());
        dest.writeDouble(this.compression);
        dest.writeByte(this.getSegmentedImage ? (byte) 1 : (byte) 0);
        dest.writeIntArray(this.segmentedImageBackgroundColor);
    }

    public void readFromParcel(Parcel source) {
        this.portalWidth = source.readDouble();
        int tmpImageType = source.readInt();
        this.imageType = tmpImageType == -1 ? null : ImageType.values()[tmpImageType];
        this.compression = source.readDouble();
        this.getSegmentedImage = source.readByte() != 0;
        this.segmentedImageBackgroundColor = source.createIntArray();
    }

    public FullFrontalCropConfig() {
    }

    protected FullFrontalCropConfig(Parcel in) {
        this.portalWidth = in.readDouble();
        int tmpImageType = in.readInt();
        this.imageType = tmpImageType == -1 ? null : ImageType.values()[tmpImageType];
        this.compression = in.readDouble();
        this.getSegmentedImage = in.readByte() != 0;
        this.segmentedImageBackgroundColor = in.createIntArray();
    }

    public static final Creator<FullFrontalCropConfig> CREATOR = new Creator<FullFrontalCropConfig>() {
        @Override
        public FullFrontalCropConfig createFromParcel(Parcel source) {
            return new FullFrontalCropConfig(source);
        }

        @Override
        public FullFrontalCropConfig[] newArray(int size) {
            return new FullFrontalCropConfig[size];
        }
    };
}

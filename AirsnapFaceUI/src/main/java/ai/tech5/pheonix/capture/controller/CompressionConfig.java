package ai.tech5.pheonix.capture.controller;

import android.os.Parcel;
import android.os.Parcelable;

public class CompressionConfig implements Parcelable {

    public CompressBy compressBy;
    public int compressionRate;
    public int targetSizeInKbs;

    public CompressionConfig() {
        this.compressBy = CompressBy.COMPRESS_BY_COMPRESSION_RATE;
        this.compressionRate = 80;
        this.targetSizeInKbs = 1024;
    }

    // Parcelable implementation
    protected CompressionConfig(Parcel in) {
        compressBy = (CompressBy) in.readSerializable();
        compressionRate = in.readInt();
        targetSizeInKbs = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(compressBy);
        dest.writeInt(compressionRate);
        dest.writeInt(targetSizeInKbs);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CompressionConfig> CREATOR = new Creator<CompressionConfig>() {
        @Override
        public CompressionConfig createFromParcel(Parcel in) {
            return new CompressionConfig(in);
        }

        @Override
        public CompressionConfig[] newArray(int size) {
            return new CompressionConfig[size];
        }
    };

    public CompressBy getCompressBy() {
        return compressBy;
    }

    public void setCompressBy(CompressBy compressBy) {
        this.compressBy = compressBy;
    }

    public int getCompressionRate() {
        return compressionRate;
    }

    public void setCompressionRate(int compressionRate) {
        this.compressionRate = compressionRate;
    }

    public int getTargetSizeInKbs() {
        return targetSizeInKbs;
    }

    public void setTargetSizeInKbs(int targetSizeInKbs) {
        this.targetSizeInKbs = targetSizeInKbs;
    }
}

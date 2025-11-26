package ai.tech5.pheonix.capture.controller;

import android.os.Parcel;
import android.os.Parcelable;

  public class AirsnapConfig implements Parcelable {

    public String license = "";
    public boolean enableOcclusion = true;
    public GlassDetection glassDetection = GlassDetection.SUN_GLASSES;
    public boolean enableEyeClose = true;
    public boolean enableLiveness = false;
    public boolean useBackCamera = false;
    public boolean isAutoCapture = true;
    public int enableCaptureAfter;
    public String title;
    public boolean showBackButton = false;
    public int captureTimeoutInSecs;
    public int messagesFrequency;
    public AirsnapFaceThresholds thresholds = null;
    public boolean isCompress = false;
    public CompressionConfig compressionConfig = null;
    public boolean isICAOCheckEnabled = false;
    public boolean isQualityCheckEnabled = false;
    public QualityCheckConfig qualityCheckConfig = null;
    public boolean isGetPortalImage = false;
    public FullFrontalCropConfig fullFrontalCropConfig = null;
    public boolean isFastCapture = true;
    public int fontSize;
    public boolean writeLogs = false;
    public boolean restrictScreenShot = false;
    public String url;
    public boolean enableCameraSwitching = false;

    // Default constructor
    public AirsnapConfig() {
    }

    // Parcelable constructor
    protected AirsnapConfig(Parcel in) {
        license = in.readString();
        enableOcclusion = in.readByte() != 0;
        glassDetection = GlassDetection.valueOf(in.readString());
        enableEyeClose = in.readByte() != 0;
        enableLiveness = in.readByte() != 0;
        useBackCamera = in.readByte() != 0;
        isAutoCapture = in.readByte() != 0;
        enableCaptureAfter = in.readInt();
        title = in.readString();
        showBackButton = in.readByte() != 0;
        captureTimeoutInSecs = in.readInt();
        messagesFrequency = in.readInt();
        thresholds = in.readParcelable(AirsnapFaceThresholds.class.getClassLoader());
        isCompress = in.readByte() != 0;
        compressionConfig = in.readParcelable(CompressionConfig.class.getClassLoader());
        isICAOCheckEnabled = in.readByte() != 0;
        isQualityCheckEnabled = in.readByte() != 0;
        qualityCheckConfig = in.readParcelable(QualityCheckConfig.class.getClassLoader());
        isGetPortalImage = in.readByte() != 0;
        fullFrontalCropConfig = in.readParcelable(FullFrontalCropConfig.class.getClassLoader());
        isFastCapture = in.readByte() != 0;
        fontSize = in.readInt();
        writeLogs = in.readByte() != 0;
        restrictScreenShot = in.readByte() != 0;
        url = in.readString();
        enableCameraSwitching = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(license);
        dest.writeByte((byte) (enableOcclusion ? 1 : 0));
        dest.writeString(glassDetection.name());
        dest.writeByte((byte) (enableEyeClose ? 1 : 0));
        dest.writeByte((byte) (enableLiveness ? 1 : 0));
        dest.writeByte((byte) (useBackCamera ? 1 : 0));
        dest.writeByte((byte) (isAutoCapture ? 1 : 0));
        dest.writeInt(enableCaptureAfter);
        dest.writeString(title);
        dest.writeByte((byte) (showBackButton ? 1 : 0));
        dest.writeInt(captureTimeoutInSecs);
        dest.writeInt(messagesFrequency);
        dest.writeParcelable(thresholds, flags);
        dest.writeByte((byte) (isCompress ? 1 : 0));
        dest.writeParcelable(compressionConfig, flags);
        dest.writeByte((byte) (isICAOCheckEnabled ? 1 : 0));
        dest.writeByte((byte) (isQualityCheckEnabled ? 1 : 0));
        dest.writeParcelable(qualityCheckConfig, flags);
        dest.writeByte((byte) (isGetPortalImage ? 1 : 0));
        dest.writeParcelable(fullFrontalCropConfig, flags);
        dest.writeByte((byte) (isFastCapture ? 1 : 0));
        dest.writeInt(fontSize);
        dest.writeByte((byte) (writeLogs ? 1 : 0));
        dest.writeByte((byte) (restrictScreenShot ? 1 : 0));
        dest.writeString(url);
        dest.writeByte((byte) (enableCameraSwitching ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AirsnapConfig> CREATOR = new Creator<AirsnapConfig>() {
        @Override
        public AirsnapConfig createFromParcel(Parcel in) {
            return new AirsnapConfig(in);
        }

        @Override
        public AirsnapConfig[] newArray(int size) {
            return new AirsnapConfig[size];
        }
    };
}

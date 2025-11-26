package ai.tech5.pheonix.capture.controller;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;

import com.phoenixcapture.camerakit.AirsnapFace;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.tech5.pheonix.capture.activity.CaptureImageActivity;


public class FaceCaptureController {

    private static Handler handler;
    private static ExecutorService executorService;

    private static FaceCaptureController controller = null;
    private FaceCaptureListener listener = null;

    private boolean isUseBackCamera = false;
    private boolean isAutoCapture = false;

    private boolean isOcclusionEnabled = true;
    private boolean isEyeClosedEnabled = true;
    private final boolean isLivenessEnabled = false;

    private boolean writeLogs = false;
    private boolean isCompression = false;
    private int enableCaptureAfter = 6;
    private AirsnapFaceThresholds thresholds = null;
    private CompressionConfig compressionConfig;
    private boolean isICAOCheckEnabled = true;
    private boolean isQualityCheckEnabled = false;
    private boolean isGetPortalImage = false;
    private FullFrontalCropConfig portalImageConfig = null;
    private QualityCheckConfig qualityCheckConfig = null;
    private int timeoutinSeconds = 60;
    private String title;
    private boolean showBackButton = false;
    public boolean enableCameraSwitching = false;

    private int messagesFrequency = 5;

    private int fontSize = 23;

    private boolean isFastCapture = true;

    private boolean restrictScreenshot = false;

    private String url;
    private GlassDetection glassDetection = GlassDetection.SUN_GLASSES;

    public GlassDetection getGlassDetection() {
        return glassDetection;
    }

    public void setGlassDetection(GlassDetection glassDetection) {
        this.glassDetection = glassDetection;
    }

    public void setRestrictScreenshot(boolean restrictScreenshot) {
        this.restrictScreenshot = restrictScreenshot;
    }


    public void setOcclusionEnabled(boolean occlusionEnabled) {
        isOcclusionEnabled = occlusionEnabled;
    }

    public void setEyeClosedEnabled(boolean eyeClosedEnabled) {
        isEyeClosedEnabled = eyeClosedEnabled;
    }


    public void setIsISOEnabled(boolean ISOCheckEnabled) {
        isICAOCheckEnabled = ISOCheckEnabled;
    }


    public void setQualityCheckEnabled(boolean qualityCheckEnabled) {
        isQualityCheckEnabled = qualityCheckEnabled;
    }

    public void setQualityCheckConfig(QualityCheckConfig config) {
        this.qualityCheckConfig = config;
    }

    public void setIsGetFullFrontalCrop(boolean getFullFrontalCrop) {
        isGetPortalImage = getFullFrontalCrop;
    }

    public void setFullFrontalCropConfig(FullFrontalCropConfig portalImageConfig) {
        this.portalImageConfig = portalImageConfig;
    }


    public void setCompressionConfig(CompressionConfig compressionConfig) {
        this.compressionConfig = compressionConfig;
    }


    public void setUrl(String url) {

        this.url = url;

    }


    public void setCaptureTimeoutInSecs(int timeout) {
        if (timeout >= 0) {
            this.timeoutinSeconds = timeout;
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setShowBackButton(boolean showBackButton) {
        this.showBackButton = showBackButton;
    }

    public void setEnableCameraSwitching(boolean enableCameraSwitching) {
        this.enableCameraSwitching = enableCameraSwitching;
    }


    public void setCompression(boolean compression) {
        isCompression = compression;
    }

    public void writeLogs(boolean writeLogs) {
        this.writeLogs = writeLogs;
    }


    public void setUseBackCamera(boolean useBackCamera) {
        isUseBackCamera = useBackCamera;
    }


    public void setAutoCapture(boolean autoCapture) {
        isAutoCapture = autoCapture;
    }


    public void setEnableCaptureAfter(int enableCaptureAfter) {
        this.enableCaptureAfter = enableCaptureAfter;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }


    public void setFrameCapture(boolean fastCapture) {
        isFastCapture = fastCapture;
    }


    public void setMessagesFrequency(int frequency) {

        messagesFrequency = frequency;
    }

    public void setAirsnapFaceThresholds(AirsnapFaceThresholds thresholds) {
        this.thresholds = thresholds;
    }


    public static FaceCaptureController getInstance() {
        if (controller == null) {

            handler = new Handler(Looper.getMainLooper());
            executorService = Executors.newFixedThreadPool(1);

            controller = new FaceCaptureController(handler, executorService);
            controller.setAirsnapFaceThresholds(new AirsnapFaceThresholds());
        }

        return controller;
    }

    private FaceCaptureController(Handler handler, ExecutorService executorService) {

        FaceCaptureController.handler = handler;
        FaceCaptureController.executorService = executorService;
    }


    public void startFaceCapture(String license, Context context, FaceCaptureListener listener) {
        this.listener = listener;


        AirsnapConfig config = new AirsnapConfig();

        config.license = license;
        config.enableOcclusion = isOcclusionEnabled;
        config.glassDetection = glassDetection;
        config.enableEyeClose = isEyeClosedEnabled;
        config.enableLiveness = isLivenessEnabled;
        config.useBackCamera = isUseBackCamera;
        config.isAutoCapture = isAutoCapture;
        config.enableCaptureAfter = enableCaptureAfter;
        config.title = title;
        config.showBackButton = showBackButton;
        config.captureTimeoutInSecs = timeoutinSeconds;
        config.messagesFrequency = messagesFrequency;
        config.thresholds = thresholds;
        config.isCompress = isCompression;
        config.compressionConfig = compressionConfig;
        config.isICAOCheckEnabled = isICAOCheckEnabled;
        config.isQualityCheckEnabled = isQualityCheckEnabled;
        config.qualityCheckConfig = qualityCheckConfig;
        config.isGetPortalImage = isGetPortalImage;
        config.fullFrontalCropConfig = portalImageConfig;
        config.isFastCapture = isFastCapture;
        config.fontSize = fontSize;
        config.writeLogs = writeLogs;
        config.restrictScreenShot = restrictScreenshot;
        config.url = url;
        config.enableCameraSwitching = enableCameraSwitching;


        CaptureImageActivity.start(context, config);
    }

    public FaceCaptureListener getCaptureListener() {
        return listener;
    }


    public boolean initSDK(Context context, String licenseString) {

        try {

            String binDirectory = Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath();

            AirsnapFace.Factory.create(context, binDirectory);


            int result = -1;
            if (!AirsnapFace.Factory.isSDKInitialized()) {

                AssetManager assetManager = context.getAssets();
                result = AirsnapFace.Factory.loadModel(licenseString, binDirectory + File.separatorChar + "runtime_data", assetManager, isOcclusionEnabled, isEyeClosedEnabled, isLivenessEnabled);


            } else {
                result = 0;
            }


            return result == 0;

        } catch (Exception e) {

        }

        return false;

    }


    public String getDeviceIdentifier(Context context) {

        AirsnapFace.Factory.create(context, Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath());
        return AirsnapFace.Factory.getDeviceIdentifier();

    }


    public void deregisterDevice(Context context, DeviceDeregistrationListener listener) {


        executorService.execute(() -> {


            try {

                AirsnapFace.Factory.create(context, Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath());

                if (url != null && !url.isEmpty()) {

                    AirsnapFace.Factory.setURL(url);
                }

                boolean isDeviceDeregistered = AirsnapFace.Factory.deregisterDevice();


                notifyInitCompletion(isDeviceDeregistered, listener);


            } catch (Exception e) {

                notifyInitCompletion(false, listener);
            }


        });

    }


    private void notifyInitCompletion(boolean isDeregistered, DeviceDeregistrationListener completion) {
        handler.post(() -> completion.onDeregisterCompletion(isDeregistered));
    }


    public void closeSDK() {

        AirsnapFace.Factory.closeSDK();
    }

}

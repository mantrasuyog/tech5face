package ai.tech5.pheonix.capture.activity;

import static androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST;
import static ai.tech5.pheonix.capture.controller.ImageUtils.compress;
import static ai.tech5.pheonix.capture.controller.ImageUtils.compressImageBelowTargetSize;
import static ai.tech5.pheonix.capture.controller.ImageUtils.flipBitmap;
import static ai.tech5.pheonix.capture.controller.ImageUtils.padAndscaleDown;
import static ai.tech5.pheonix.capture.controller.ImageUtils.rotateBitmap;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.utils.Exif;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.phoenixcapture.camerakit.AirsnapFace;
import com.phoenixcapture.camerakit.FaceBox;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.tech5.pheonix.capture.controller.AirsnapConfig;
import ai.tech5.pheonix.capture.controller.AirsnapFaceThresholds;
import ai.tech5.pheonix.capture.controller.CompressBy;
import ai.tech5.pheonix.capture.controller.CompressionConfig;
import ai.tech5.pheonix.capture.controller.FaceCaptureController;
import ai.tech5.pheonix.capture.controller.FullFrontalCropConfig;
import ai.tech5.pheonix.capture.controller.GlassDetection;
import ai.tech5.pheonix.capture.controller.GraphicOverlay;
import ai.tech5.pheonix.capture.controller.ImageType;
import ai.tech5.pheonix.capture.controller.Logger;
import ai.tech5.pheonix.capture.controller.QualityCheckConfig;
import ai.tech5.ui.R;

public class CaptureImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = CaptureImageActivity.class.getSimpleName();
    private static final Boolean SHOW_LIVE_FEEDBACK = false;
    private static final String EXTRA_AIRSNAP_FACE_CONFIG = "AIRSNAP_FACE_CONFIG";

    private ProgressDialog dialog = null;

    private boolean isOcclusionEnabled = true;
    private boolean isEyeClosedEnabled = true;
    private boolean isLivenessEnabled = true;


    private boolean isUseBackCamera = false;
    private boolean isAutoCapture = false;
    private boolean isCompressionEnabled = false;

    private boolean writeLogs = false;

    private int enableCaptureAfter = 6;
    private String licenseString = "";

    private int PITCH_THRESHOLD = 15;
    private int YAW_THRESHOLD = 15;
    private int ROLL_THRESHOLD = 10;
    private double MASK_THRESHOLD = 0.5;
    private double ANY_GLASS_THRESHOLD = 0.5;
    private double SUN_GLASS_THRESHOLD = 0.5;
    private int BRISQUE_THRESHOLD = 60;
    private double EYE_CLOSE_THRESHOLD = 0.4;

    private boolean captureModeFast = false;


    public static float FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE = 10;

    public static float BASE_IMAGE_WIDTH_PERCENTAGE = 45;
    public static float FACE_TO_IMAGE_WIDTH_TOLERANCE = 10;

    private float blur_min = 0.0f;
    private float blur_max = 0.5f;
    private float exposure_min = 0.2f;
    private float exposure_max = 0.7f;
    private float brightness_min = 0.2f;
    private float brightness_max = 0.7f;


    boolean getTokenImage = true; //whether to get portal Image
    float tokenWidth = (float) 1200.0; //the width of the portal image
    float saveToken = (float) 1.0; //1.0 = save as JPEG, 0.0 = save as BMP
    float compression = (float) 0.0; //(only if saveportal = 1.0) 0 means no compression and 1 means maximum compression

    boolean get_segmented_image = true;
    int segmented_image_R = 125;
    int segmented_image_G = 125;
    int segmented_image_B = 125;

    boolean do_ICAO = true; //whether to extract ICAO features
    boolean do_QualityCheck = false; //whether to perform quality check before doing anything

    private CompressionConfig compressionConfig;
    private int compressionRate = 80;

    private GlassDetection glassDetection = GlassDetection.SUN_GLASSES;


    private int successCount = 0;


    public int m_lensFacing = CameraSelector.LENS_FACING_FRONT;


    private ToneGenerator toneGenerator;


    /**
     * Blocking camera operations are performed using this executor
     */

    private PreviewView m_viewFinder;


    private Preview preview = null;
    private ImageCapture m_imageCapture;
    private ImageAnalysis m_imageAnalyzer;
    private Camera m_camera = null;
    CameraSelector m_cameraSelector = null;
    private ExecutorService cameraExecutor = null;

    private TextView txtStatus;

    TextView labelMask, labelGlass, labelSunGlasses, labelMaxFaceWidth, labelMinFaceWidth, labelEyeY, labeleyeX,
            labelYaw, labelPitch, labelRoll, labelBrisque, labelLeftEyeClose, labelRightEyeClose, labelEyeDistance;


    private ProcessCameraProvider m_cameraProvider = null;
    private ImageView btnCapture = null;
    private ImageView btnSwitchCam;
    private GraphicOverlay graphicOverlay = null;

    private File mLogFile;


    private static final String[] APP_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private CountDownTimer countDownTimer = null;
    private int timeout = 60;
    private boolean isTimedout = false;
    private boolean showBackButton = false;
    private String title = null;
    private int fontSize = 20;
    private String url;


    AirsnapFace phoenixSDK = null;


    private Bitmap lastBestFrame = null;
    private int numParamsMetByLastFrame = 0;


    int counterNoFace = 0, counterMultiFace = 0, counterMask = 0, counterAnyGlasses = 0, counterSunGlasses = 0, counterClose = 0, counterFar = 0, counterCenter_Hold = 0, counterPose = 0, counterEyeClose = 0, counterBrisque = 0;

    int frameCounter = 0;
    String lastMessage = null;
    int changeMessagefor = 5;

    private boolean isSwitchingCamera = false;

    public static void start(Context context, AirsnapConfig config) {

        Intent intent = new Intent(context, CaptureImageActivity.class);
        intent.putExtra(EXTRA_AIRSNAP_FACE_CONFIG, config);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setScreenBrightnessFul();
        super.onCreate(savedInstanceState);


        AirsnapConfig airsnapConfig = getIntent().getParcelableExtra(EXTRA_AIRSNAP_FACE_CONFIG);

        if (airsnapConfig == null) {
            airsnapConfig = new AirsnapConfig();
        }

        if (airsnapConfig.restrictScreenShot) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }


        setContentView(R.layout.activity_face_capture);


        File rootDirectory = getExternalFilesDir(null);
        mLogFile = new File(rootDirectory, "airsnap_face_log.txt");


        txtStatus = findViewById(R.id.txt_status);

        labelMask = findViewById(R.id.txt_mask);
        labelGlass = findViewById(R.id.txt_glasses);
        labelSunGlasses = findViewById(R.id.txt_sun_glasses);
        labelMaxFaceWidth = findViewById(R.id.txt_max_face_width);
        labelMinFaceWidth = findViewById(R.id.txt_min_face_width);
        labelEyeY = findViewById(R.id.txt_eye_eyes_y);
        labeleyeX = findViewById(R.id.txt_eye_eyes_x);
        labelYaw = findViewById(R.id.txt_yaw);
        labelPitch = findViewById(R.id.txt_pitch);
        labelRoll = findViewById(R.id.txt_roll);
        labelBrisque = findViewById(R.id.txt_brisque);
        labelLeftEyeClose = findViewById(R.id.txt_left_eye_closed);
        labelRightEyeClose = findViewById(R.id.txt_right_eye_closed);
        labelEyeDistance = findViewById(R.id.txt_eye_distance);


        graphicOverlay = findViewById(R.id.overlay);


        m_viewFinder = findViewById(R.id.view_finder);

        //
        m_viewFinder.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);

        btnCapture = findViewById(R.id.btn_capture);
        btnSwitchCam = findViewById(R.id.btn_flip_camera);
        btnCapture.setOnClickListener(this);
        btnSwitchCam.setOnClickListener(this);

        btnSwitchCam.setVisibility(airsnapConfig.enableCameraSwitching ? View.VISIBLE : View.GONE);


        btnCapture.setVisibility(View.GONE);


        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor();


        licenseString = airsnapConfig.license;
        isAutoCapture = airsnapConfig.isAutoCapture;
        isUseBackCamera = airsnapConfig.useBackCamera;
        isOcclusionEnabled = airsnapConfig.enableOcclusion;
        glassDetection = airsnapConfig.glassDetection;
        isEyeClosedEnabled = airsnapConfig.enableEyeClose;
        isLivenessEnabled = airsnapConfig.enableLiveness;
        enableCaptureAfter = airsnapConfig.enableCaptureAfter;

        url = airsnapConfig.url;

        if (isUseBackCamera) {
            BASE_IMAGE_WIDTH_PERCENTAGE = 40;
        } else {
            BASE_IMAGE_WIDTH_PERCENTAGE = 45;
        }

        graphicOverlay.init(isUseBackCamera ? 1 : 0);


        captureModeFast = airsnapConfig.isFastCapture;

        isCompressionEnabled = airsnapConfig.isCompress;

        if (this.isCompressionEnabled) {
            this.compressionConfig = airsnapConfig.compressionConfig;
            if (this.compressionConfig == null) {
                this.compressionConfig = new CompressionConfig();
            }
            this.compressionRate = this.compressionConfig.getCompressionRate();
        } else {
            this.compressionRate = 100;
        }


        if (licenseString == null) {
            licenseString = "";
        }


        timeout = airsnapConfig.captureTimeoutInSecs;
        changeMessagefor = airsnapConfig.messagesFrequency;
        title = airsnapConfig.title;
        showBackButton = airsnapConfig.showBackButton;

        // getSupportActionBar().setTitle("Select Image");
        if (showBackButton && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (title != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        do_ICAO = airsnapConfig.isICAOCheckEnabled;
        writeLogs = airsnapConfig.writeLogs;
        do_QualityCheck = airsnapConfig.isQualityCheckEnabled;

        QualityCheckConfig qualityCheckConfig = airsnapConfig.qualityCheckConfig;

        if (qualityCheckConfig != null) {
            blur_min = qualityCheckConfig.getBlur_min();
            blur_max = qualityCheckConfig.getBlur_max();
            exposure_min = qualityCheckConfig.getExposure_min();
            exposure_max = qualityCheckConfig.getExpoure_max();
            brightness_min = qualityCheckConfig.getBrightness_min();
            brightness_max = qualityCheckConfig.getBrightness_max();
        }

        fontSize = airsnapConfig.fontSize;

        txtStatus.setTextSize(fontSize);

        getTokenImage = airsnapConfig.isGetPortalImage;
        FullFrontalCropConfig fullFrontalCropConfig = airsnapConfig.fullFrontalCropConfig;

        Logger.addToLog(TAG, "portalImageConfig " + fullFrontalCropConfig, mLogFile, writeLogs);
        if (fullFrontalCropConfig != null) {

            tokenWidth = (float) fullFrontalCropConfig.getPortalWidth(); //the width of the portal image

            if (fullFrontalCropConfig.getImageType() == ImageType.IMAGE_TYPE_JPG) {
                saveToken = 1.0f;
            } else {
                saveToken = 0.0f;
            }

            compression = (float) fullFrontalCropConfig.getCompression(); //(only if saveportal = 1.0) 0 means no compression and 1 means maximum compression

            get_segmented_image = fullFrontalCropConfig.isGetSegmentedImage();

            segmented_image_R = fullFrontalCropConfig.getSegmentedImageBackgroundColor()[0];
            segmented_image_G = fullFrontalCropConfig.getSegmentedImageBackgroundColor()[1];
            segmented_image_B = fullFrontalCropConfig.getSegmentedImageBackgroundColor()[2];
        }

        AirsnapFaceThresholds thresholds = airsnapConfig.thresholds;

        if (thresholds != null) {

            Logger.addToLog(TAG, "thresholds " + thresholds, mLogFile, writeLogs);

            PITCH_THRESHOLD = thresholds.getPITCH_THRESHOLD();
            YAW_THRESHOLD = thresholds.getYAW_THRESHOLD();
            ROLL_THRESHOLD = thresholds.getRollThreshold();
            MASK_THRESHOLD = thresholds.getMASK_THRESHOLD();
            ANY_GLASS_THRESHOLD = thresholds.getANYGLASS_THRESHOLD();
            SUN_GLASS_THRESHOLD = thresholds.getSUNGLASS_THRESHOLD();
            BRISQUE_THRESHOLD = thresholds.getBRISQUE_THRESHOLD();
            EYE_CLOSE_THRESHOLD = thresholds.getEYE_CLOSE_THRESHOLD();
            FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE = thresholds.getFaceCentreToImageCentreTolerance();
            FACE_TO_IMAGE_WIDTH_TOLERANCE = thresholds.getFaceWidthToImageWidthRatioTolerance();


        }


        m_lensFacing = isUseBackCamera ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;


        if (hasAllPermissionsGranted()) {

            initSDKs();

        } else {

            FaceCaptureController.getInstance().getCaptureListener().OnFaceCaptureFailed("Camera permission required");
            finish();
        }


    }

    private void initCountDownTimer() {

        countDownTimer = new CountDownTimer(timeout * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Logger.addToLog("TAG", "millisUntilFinished " + millisUntilFinished, mLogFile, writeLogs);

            }

            @Override
            public void onFinish() {

                isTimedout = true;

                try {
                    m_cameraProvider.unbindAll();
                } catch (Exception e) {
                    Logger.logException("TAG", e, mLogFile, writeLogs);

                }


                byte[] faceImage = null;

                if (lastBestFrame != null) {
                    faceImage = compressImage(lastBestFrame);

                }


                FaceCaptureController.getInstance().getCaptureListener().onTimedout(faceImage);
                finish();


            }
        };

        countDownTimer.start();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {// app icon in action bar clicked; go home
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {

        super.onResume();
    }


    private boolean hasAllPermissionsGranted() {

        for (String permission : APP_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    void initSDKs() {

        new Thread(() -> {


            showDialog(true);

            int sdkInitRetCode = initPFCMSDK();


            runOnUiThread(() -> {

                showDialog(false);

                if (sdkInitRetCode == 0) {
                    initCountDownTimer();
                    init();


                } else {

                    String erroMessage = "";

                    if (sdkInitRetCode == 1) {
                        erroMessage = "AppId not registered";
                    } else if (sdkInitRetCode == 2) {
                        erroMessage = "License expired";
                    } else if (sdkInitRetCode == 3) {
                        erroMessage = "AppId not registered for unlimited";
                    } else if (sdkInitRetCode == 4) {
                        erroMessage = "Max device count reached";
                    } else if (sdkInitRetCode == 5) {
                        erroMessage = "Invalid req";
                    } else if (sdkInitRetCode == 6) {
                        erroMessage = "Network error";
                    } else if (sdkInitRetCode == 7) {
                        erroMessage = "Internal error";
                    }

                    FaceCaptureController.getInstance().getCaptureListener().OnFaceCaptureFailed("SDK initialization failed " + erroMessage);

                    finish();
                }


            });


        }).start();

    }


    int initPFCMSDK() {
        int result = 7;
        try {


            String binDirectory = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();

            phoenixSDK = AirsnapFace.Factory.create(CaptureImageActivity.this, binDirectory);


            if (!AirsnapFace.Factory.isSDKInitialized()) {


                if (url != null && !url.isEmpty()) {
                    AirsnapFace.Factory.setURL(url);
                }


                Logger.addToLog(TAG, "license string  " + licenseString, mLogFile, writeLogs);

                Context context = getBaseContext();
                AssetManager assetManager = getAssets();
                result = AirsnapFace.Factory.loadModel(licenseString, binDirectory + File.separatorChar + "runtime_data", assetManager, isOcclusionEnabled, isEyeClosedEnabled, isLivenessEnabled);

                Logger.addToLog(TAG, "init sdk retcode1 " + result, mLogFile, writeLogs);


            } else {
                result = 0;
            }


            Logger.addToLog("TAG", "license init result " + result, mLogFile, writeLogs);


        } catch (Exception e) {
            Logger.addToLog("TAG", "license init failed ", mLogFile, writeLogs);
            Logger.logException("TAG", e, mLogFile, writeLogs);
        }

        return result;


    }


    void init() {


        m_viewFinder.post(this::setUpCamera);


    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_capture) {

            m_cameraProvider.unbind(m_imageAnalyzer);


            captureFaceImage();

        } else if (view.getId() == R.id.btn_flip_camera) {

            if (isSwitchingCamera) {
                return;
            }

            m_cameraProvider.unbindAll();

            switchCamera();

        }


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        FaceCaptureController.getInstance().getCaptureListener().onCancelled();
        finish();
    }


    void captureFaceImage() {

        long startTime = System.currentTimeMillis();

        Logger.addToLog("TAG", "captureFaceImage() called count " + successCount + "-------" + new Date(), mLogFile, writeLogs);


        btnCapture.setEnabled(false);
        btnSwitchCam.setEnabled(false);

        if (!captureModeFast) {


            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            File file = new File(getExternalCacheDir(), mDateFormat.format(new Date()) + ".jpg");

            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

            m_imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {

                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Logger.addToLog("TAG", "time taken to save Image captureFaceImage()", mLogFile, writeLogs);

                    showDialog(true);
                    runOnUiThread(() -> {
                        playBeep();
                        btnCapture.setEnabled(true);
                        btnSwitchCam.setEnabled(true);
                        m_cameraProvider.unbindAll();
                    });
                    Logger.addToLog("TAG", "captureFaceImage() path " + file.getAbsolutePath(), mLogFile, writeLogs);
                    if (file.exists()) {

                        try {
                            Logger.addToLog("TAG", "captureFaceImage()---3", mLogFile, writeLogs);

                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                            @SuppressLint("RestrictedApi") Exif exif = Exif.createFromFile(file);

                            @SuppressLint("RestrictedApi") int rotation = exif.getRotation();

                            @SuppressLint("RestrictedApi") boolean isFlipped = exif.isFlippedHorizontally();

                            Logger.addToLog(TAG, "capture image exif " + exif, mLogFile, writeLogs);
                            Logger.addToLog(TAG, "capture image flipped horizantally " + isFlipped, mLogFile, writeLogs);

                            if (rotation != 0) {
                                bitmap = rotateBitmap(bitmap, rotation);
                            }

                            if (isFlipped) {
                                bitmap = flipBitmap(bitmap);

                            }

                            if (bitmap != null) {

                                if (getTokenImage) {

                                    bitmap = padAndscaleDown(bitmap, 1200, 1600, true, false, false);

                                } else {
                                    bitmap = padAndscaleDown(bitmap, 1200, 1600, true, true, false);
                                }
                                processImage(bitmap, true);
                            }

                        } catch (Exception e) {
                            Logger.addToLog(TAG, "capture face failed  ", mLogFile, writeLogs);
                            Logger.logException("TAG", e, mLogFile, writeLogs);
                            showDialog(false);
                        }

                    } else {
                        showDialog(false);
                    }


                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Logger.addToLog("TAG", "captureFaceImage()---ERR " + exception, mLogFile, writeLogs);
                    runOnUiThread(() -> {
                        btnCapture.setEnabled(true);
                        btnSwitchCam.setEnabled(true);
                    });
                }


            });


        } else {

            Bitmap bitmap = getPreviewViewBitmap();


            try {


                m_cameraProvider.unbindAll();

                bitmap = flipBitmap(bitmap);

                Bitmap finalBitmap = bitmap;
                btnSwitchCam.setEnabled(true);
                new Thread(() -> {

                    showDialog(true);

                    processImage(finalBitmap, true);

                }).start();


            } catch (Exception e) {

                Logger.addToLog(TAG, "get preview bitmap failed  ", mLogFile, writeLogs);
                Logger.logException("TAG", e, mLogFile, writeLogs);
            }

        }
    }


    private Bitmap getPreviewViewBitmap() {
        View previewChildView = m_viewFinder.getChildAt(0);


        if (previewChildView instanceof TextureView) {

            if (getTokenImage) {
                return ((TextureView) previewChildView).getBitmap(m_viewFinder.getWidth(), (m_viewFinder.getWidth() / 3) * 4);
            } else {
                return ((TextureView) previewChildView).getBitmap(1200, 1600);

            }

        }

        return m_viewFinder.getBitmap();
    }


    /**
     * Initialize CameraX, and prepare to bind the camera use cases
     */
    private void setUpCamera() {


        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(CaptureImageActivity.this);

        cameraProviderFuture.addListener(() -> {

            // CameraProvider
            try {
                m_cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                Logger.addToLog(TAG, "get camera provider failed " + e.getLocalizedMessage(), mLogFile, writeLogs);
                Logger.logException(TAG, e, mLogFile, writeLogs);
            }


            // Build and bind the camera use cases
            bindCameraUseCases();


            @SuppressLint("RestrictedApi") Size size = m_imageAnalyzer.getAttachedSurfaceResolution();
            @SuppressLint("RestrictedApi") Size previewSize = preview.getAttachedSurfaceResolution();

            Logger.addToLog(TAG, "analyzer size analyzer " + size, mLogFile, writeLogs);
            Logger.addToLog(TAG, "analyzer size preview " + previewSize, mLogFile, writeLogs);

        }, ContextCompat.getMainExecutor(CaptureImageActivity.this));


    }


    private void setScreenBrightnessFul() {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        getWindow().setAttributes(params);
    }

    /**
     * Declare and bind preview, capture and analysis use cases
     */
    @SuppressLint({"RestrictedApi", "UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
    private void bindCameraUseCases() {

        isSwitchingCamera = false;

        lastMessage = null;
        resetAllCounters();
        lastBestFrame = null;
        btnCapture.setEnabled(true);
        btnSwitchCam.setEnabled(true);


        int rotation = m_viewFinder.getDisplay().getRotation();

        m_cameraSelector = new CameraSelector.Builder().requireLensFacing(m_lensFacing).build();

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .build();


        m_imageAnalyzer = new ImageAnalysis.Builder()
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                // Set initial target rotation, we will have to call
                // this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)
                .build();

        m_imageAnalyzer.setAnalyzer(cameraExecutor, new FaceImageAnalyzer());

        ImageCapture.Builder builder = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY);


//        if (isUseBackCamera) {
//            builder.setFlashMode(ImageCapture.FLASH_MODE_ON);
//        }


        m_imageCapture =
                builder.build();


        // Must unbind the use-cases before rebinding them
        m_cameraProvider.unbindAll();

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            m_camera = m_cameraProvider.bindToLifecycle(
                    this, m_cameraSelector, preview, m_imageCapture, m_imageAnalyzer);

            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(m_viewFinder.getSurfaceProvider());


            final CameraInfo cameraInfo = m_camera.getCameraInfo();

            final CameraControl cameraControl = m_camera.getCameraControl();

            if (isUseBackCamera && cameraInfo.hasFlashUnit()) {
                cameraControl.enableTorch(true);
            }


        } catch (Exception exc) {
            Logger.addToLog(TAG, "bind camera use cases failed  ", mLogFile, writeLogs);
            Logger.logException("TAG", exc, mLogFile, writeLogs);
        }

        Logger.addToLog(TAG, "bindCameraUseCases() done", mLogFile, writeLogs);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();


        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        showDialog(false);
    }


    class FaceImageAnalyzer implements ImageAnalysis.Analyzer {


        @Nullable
        @Override
        public Size getDefaultTargetResolution() {
            return new Size(480, 640);
        }


        @Override
        public void analyze(@NonNull ImageProxy image) {
            try {


                Logger.addToLog(TAG, "image format is " + image.getFormat(), mLogFile, writeLogs);
                Logger.addToLog(TAG, "image size is " + image.getWidth() + "x" + image.getHeight() + " rotation " + image.getImageInfo().getRotationDegrees(), mLogFile, writeLogs);


                Bitmap bitmap =
                        image.toBitmap();

                if (image.getImageInfo().getRotationDegrees() != 0) {

                    bitmap = rotateBitmap(bitmap, image.getImageInfo().getRotationDegrees());

                }

                processImage(bitmap, false);

            } catch (Exception e) {


                Logger.logException("TAG", e, mLogFile, writeLogs);

            } finally {
                image.close();
            }

        }
    }


    private void processImage(Bitmap bitmap, boolean isFinalCapture) {

        float FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE_TEMP;
        float FACE_TO_IMAGE_WIDTH_TOLERANCE_TEMP;

        if (!isUseBackCamera) {

            if (!isFinalCapture) {
                FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE_TEMP = FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE - 1;
                FACE_TO_IMAGE_WIDTH_TOLERANCE_TEMP = FACE_TO_IMAGE_WIDTH_TOLERANCE - 3;
            } else {
                FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE_TEMP = FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE;
                FACE_TO_IMAGE_WIDTH_TOLERANCE_TEMP = FACE_TO_IMAGE_WIDTH_TOLERANCE;
            }

        } else {
            if (!isFinalCapture) {
                FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE_TEMP = FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE;
                FACE_TO_IMAGE_WIDTH_TOLERANCE_TEMP = FACE_TO_IMAGE_WIDTH_TOLERANCE;
            } else {
                FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE_TEMP = FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE + 5;
                FACE_TO_IMAGE_WIDTH_TOLERANCE_TEMP = FACE_TO_IMAGE_WIDTH_TOLERANCE + 5;
            }
        }

        frameCounter += 1;

        int numParamsMetByThisFrame = 0;

        boolean passed = false;
        FaceBox faceBox = null;

        long startTime = System.currentTimeMillis();
        int color = getResources().getColor(R.color.border_color_fail);
        List<FaceBox> faceBoxes = null;

        float[] QualityCheckRange = {blur_min
                , blur_max, exposure_min, exposure_max, brightness_min, brightness_max};
        float[] PortalImageParams = {tokenWidth, saveToken, compression};


        if (isSwitchingCamera) {
            return;
        }


        if (!isFinalCapture) {
            faceBoxes = AirsnapFace.Factory.detectFace(bitmap);
            Logger.addToLog(TAG, "time taken for face detectFace " + (System.currentTimeMillis() - startTime), mLogFile, writeLogs);
        } else {
            boolean get_reference_image_MRTD = getTokenImage;
            float[] ReferenceImageMRTDImageParams = PortalImageParams;

            int[] SegmentedImageColor = {segmented_image_R, segmented_image_G, segmented_image_B};

            Logger.addToLog(TAG, "params " + ReferenceImageMRTDImageParams[0] + " - " + ReferenceImageMRTDImageParams[1] + " - " + ReferenceImageMRTDImageParams[2], mLogFile, writeLogs);
            faceBoxes = AirsnapFace.Factory.evalFace(bitmap, do_ICAO, do_QualityCheck, QualityCheckRange, get_reference_image_MRTD, ReferenceImageMRTDImageParams, get_segmented_image, SegmentedImageColor);
            Logger.addToLog(TAG, "time taken for face evalFace " + (System.currentTimeMillis() - startTime), mLogFile, writeLogs);


        }


        if (isSwitchingCamera) {
            return;
        }


        float pan = -1, pitch = -1, roll = -1, brisque = -1, eyeDist = -1, liveness = -0.01f, mask = 0.0f, anyglasses = 0.0f, sunglasses = 0.0f;
        //int faceState = 0;
        float lefteyeClosed = 0.0f;
        float righteyeClosed = 0.0f;

        String strMsg = "";

        if (faceBoxes == null) {
            strMsg = getResources().getString(R.string.detection_failed_message);

            counterNoFace += 1;
            clearLiveFeed();
        } else if (faceBoxes.isEmpty()) {

            counterNoFace += 1;
            strMsg = getResources().getString(R.string.no_faces_detected_message);
            clearLiveFeed();
        } else if (faceBoxes.size() > 1) {

            numParamsMetByThisFrame = 1;
            counterMultiFace += 1;
            strMsg = getResources().getString(R.string.multiple_faces_detected);
            clearLiveFeed();
        } else {

            faceBox = faceBoxes.get(0);
            Logger.addToLog(TAG, faceBox.toString(), mLogFile, writeLogs);


            FaceBox finalFaceBox1 = faceBox;


            pan = faceBox.mPan;
            pitch = faceBox.mPitch;
            roll = faceBox.mRoll;
            brisque = faceBox.mBrisque;
            eyeDist = faceBox.mEyeDist;
            lefteyeClosed = faceBox.mLeftEyeClose;
            righteyeClosed = faceBox.mRightEyeClose;
            liveness = faceBox.mLiveness;

            mask = faceBox.mMask;

            sunglasses = faceBox.mSunGlass;

            if (glassDetection == GlassDetection.ANY_GLASSES) {
                anyglasses = faceBox.mAnyGlass;
            } else {
                anyglasses = 0;
            }

            float facewidth = (faceBox.mRight - faceBox.mLeft);
            float faceHeight = (faceBox.mBottom - faceBox.mTop);


            float imageCentreX = bitmap.getWidth() / 2;
            float imageCentreY = bitmap.getHeight() * 0.55f;


            float faceCentreX = (faceBox.mLeft) + (facewidth / 2);
            float faceCentreY = (faceBox.mTop) + (faceHeight * 0.5F);

            float diffx = Math.abs(faceCentreX - imageCentreX);
            float diffy = Math.abs(faceCentreY - imageCentreY);

            float toleranceX = bitmap.getWidth() * (FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE_TEMP / 100);
            float toleranceY = bitmap.getHeight() * (FACE_CENTRE_TO_IMAGE_CENTRE_TOLERANCE_TEMP / 100);

            float maxFaceWidth = (bitmap.getWidth() * ((BASE_IMAGE_WIDTH_PERCENTAGE + FACE_TO_IMAGE_WIDTH_TOLERANCE_TEMP) / 100));
            float minFaceWidth = (bitmap.getWidth() * ((BASE_IMAGE_WIDTH_PERCENTAGE - FACE_TO_IMAGE_WIDTH_TOLERANCE_TEMP) / 100));

            if (isUseBackCamera) {
                maxFaceWidth = (bitmap.getWidth() * ((BASE_IMAGE_WIDTH_PERCENTAGE + Math.min(FACE_TO_IMAGE_WIDTH_TOLERANCE_TEMP, 20)) / 100));
                minFaceWidth = (bitmap.getWidth() * ((BASE_IMAGE_WIDTH_PERCENTAGE - Math.min(FACE_TO_IMAGE_WIDTH_TOLERANCE_TEMP, 10)) / 100));
            }


            float finalMinFaceWidth = minFaceWidth;
            float finalMaxFaceWidth = maxFaceWidth;
            runOnUiThread(() -> {
                if (SHOW_LIVE_FEEDBACK) {
                    setStatus(finalFaceBox1, bitmap.getWidth(), facewidth, finalMinFaceWidth, finalMaxFaceWidth, diffx, diffy, toleranceX, toleranceY);
                }
            });


            if (mask > MASK_THRESHOLD) {
                counterMask += 1;
                strMsg = getResources().getString(R.string.mask_detected_message);
                numParamsMetByThisFrame = 2;
            } else if (sunglasses > SUN_GLASS_THRESHOLD) {
                strMsg = getResources().getString(R.string.sunglasses_detected_message);
                counterSunGlasses += 1;
                numParamsMetByThisFrame = 3;
            } else if (anyglasses > ANY_GLASS_THRESHOLD) {
                strMsg = getResources().getString(R.string.anyglasses_detected_message);
                counterAnyGlasses += 1;
                numParamsMetByThisFrame = 4;
            } else if (facewidth > maxFaceWidth) {
                numParamsMetByThisFrame = 5;
                counterFar += 1;
                strMsg = (getString(R.string.camera_far_message));
            } else if (facewidth < minFaceWidth) {
                numParamsMetByThisFrame = 6;
                counterClose += 1;
                strMsg = (getString(R.string.camera_close_message));
            } else if (diffx > toleranceX) {
                numParamsMetByThisFrame = 7;
                counterCenter_Hold += 1;
                strMsg = getString(R.string.hold_still_and_center);
            } else if (diffy > toleranceY) {
                numParamsMetByThisFrame = 8;
                counterCenter_Hold += 1;
                strMsg = getString(R.string.hold_still_and_center);
            } else if (Math.abs(pan) > YAW_THRESHOLD) {
                numParamsMetByThisFrame = 9;
                counterPose += 1;
                strMsg = getResources().getString(R.string.look_straight_message);
            } else if (Math.abs(pitch) > PITCH_THRESHOLD) {
                numParamsMetByThisFrame = 10;
                counterPose += 1;
                strMsg = getResources().getString(R.string.look_straight_message);
            } else if (Math.abs(roll) > ROLL_THRESHOLD) {
                numParamsMetByThisFrame = 11;
                counterPose += 1;
                strMsg = getResources().getString(R.string.look_straight_message);
            } else if (lefteyeClosed < EYE_CLOSE_THRESHOLD) {
                numParamsMetByThisFrame = 12;
                counterEyeClose += 1;
                strMsg = getResources().getString(R.string.eyes_closed_message);
            } else if (righteyeClosed < EYE_CLOSE_THRESHOLD) {
                numParamsMetByThisFrame = 13;
                counterEyeClose += 1;
                strMsg = getResources().getString(R.string.eyes_closed_message);
            } else if (brisque > BRISQUE_THRESHOLD) {
                numParamsMetByThisFrame = 14;
                counterCenter_Hold += 1;
                strMsg = getResources().getString(R.string.hold_still_message);
            } else {
                numParamsMetByThisFrame = 15;
                counterCenter_Hold += 1;
                strMsg = getResources().getString(R.string.ok);
                passed = true;
                color = getResources().getColor(R.color.border_color_pass);
            }

        }


        if (numParamsMetByLastFrame <= numParamsMetByThisFrame) {
            lastBestFrame = bitmap;
            numParamsMetByLastFrame = numParamsMetByThisFrame;
        }

        if (passed) {
            successCount++;
        } else {
            successCount = 0;

        }


        Logger.addToLog(TAG, "strMsg " + strMsg, mLogFile, writeLogs);
        Logger.addToLog(TAG, "success count " + successCount + " isFinalCapture " + isFinalCapture + " strMsg " + strMsg + " eyclose left" + lefteyeClosed
                + " right " + righteyeClosed + " roll " + roll, mLogFile, writeLogs);
        String finalStrMsg = strMsg;
        FaceBox finalFaceBox = faceBox;
        int finalColor = color;


        runOnUiThread(() -> {

            graphicOverlay.drawBorder(finalColor);


            if (lastMessage == null) {
                lastMessage = finalStrMsg;
                Logger.addToLog(TAG, "message first " + lastMessage + " no face counter " + counterNoFace, mLogFile, writeLogs);
            } else {
                String msg = getDisplayMessage(finalStrMsg);

                if (msg != null) {

                    Logger.addToLog(TAG, "message changing", mLogFile, writeLogs);

                    lastMessage = msg;
                    resetAllCounters();

                } else {

                    Logger.addToLog(TAG, "message not changing", mLogFile, writeLogs);
                }

            }

            txtStatus.setText(lastMessage);

            if (!isFinalCapture) {


                if (successCount >= enableCaptureAfter) {
                    if (isAutoCapture) {

                        btnSwitchCam.setEnabled(false);

                        m_cameraProvider.unbind(m_imageAnalyzer);
                        successCount = 0;
                        lastMessage = null;
                        captureFaceImage();

                    } else {
                        btnCapture.setVisibility(View.VISIBLE);
                    }

                } else {

                    btnCapture.setVisibility(View.GONE);
                }

            } else {

                if (successCount > 0) {


                    new Thread(() -> {


                        byte[] capturedImage;
                        byte[] originalImage;

                        if (finalFaceBox.mHasPortalImage == 1 && finalFaceBox.mPortalImage != null) {

                            Logger.addToLog(TAG, "portal image dims " + finalFaceBox.mPortalImage.getWidth() + "x " + finalFaceBox.mPortalImage.getHeight(), mLogFile, writeLogs);
                            capturedImage = compressImage(finalFaceBox.mPortalImage);
                            originalImage = compress(finalFaceBox.mPortalImage, 100);

                            finalFaceBox.mPortalImage = null;


                        } else {
                            capturedImage = compressImage(bitmap);
                            originalImage = compress(bitmap, 100);
                        }


                        byte[] finalCaptueredImage = capturedImage;
                        byte[] finalOriginalImage = originalImage;
                        runOnUiThread(() -> {
                            showDialog(false);
                            if (!isTimedout) {
                                FaceCaptureController.getInstance().getCaptureListener().onFaceCaptured(finalCaptueredImage, finalOriginalImage, finalFaceBox);
                                finish();
                            }
                        });
                    }).start();


                } else {
                    showDialog(false);
                    btnCapture.setEnabled(true);
                    bindCameraUseCases();


                }

            }

        });


    }


    private byte[] compressImage(Bitmap bitmap) {


        if (!isCompressionEnabled || compressionConfig.compressBy == CompressBy.COMPRESS_BY_COMPRESSION_RATE) {
            return compress(bitmap, compressionRate);
        } else if (compressionConfig.getCompressBy() == CompressBy.COMPRESS_BY_TARGET_SIZE) {
            return compressImageBelowTargetSize(bitmap, compressionConfig.targetSizeInKbs);
        }
        return compress(bitmap, compressionRate);
    }


    private void playBeep() {
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 200);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (toneGenerator != null) {
                toneGenerator.release();
                toneGenerator = null;
            }
        }, 100);
    }


    private void showDialog(boolean isShow) {

        runOnUiThread(() -> {
            if (dialog == null) {
                dialog = new ProgressDialog(CaptureImageActivity.this);
                dialog.setMessage(getString(R.string.please_wait));
                dialog.setCancelable(false);
            }
            if (isShow)
                dialog.show();
            else
                dialog.dismiss();

        });

    }


    private void clearLiveFeed() {

        runOnUiThread(() -> {

            if (SHOW_LIVE_FEEDBACK) {
                labelMask.setText("Mask : -");
                labelGlass.setText("AnyGlasses : -");
                labelSunGlasses.setText("SunGlasses : -");

                labeleyeX.setText("Face centreX : -");
                labelEyeY.setText("Face centreY : -");
                labelMaxFaceWidth.setText("Max Face Width : -");
                labelMinFaceWidth.setText("Min Face Width : -");


                labelYaw.setText("Yaw : -");
                labelPitch.setText("Pitch : -");
                labelRoll.setText("Roll : -");
                labelBrisque.setText("Brisque : -");
                labelLeftEyeClose.setText("Left eye : -");
                labelRightEyeClose.setText("Right eye : -");
                labelEyeDistance.setText("Eye Distance: -");
            } else {
                labelMask.setText("");
                labelGlass.setText("");
                labelSunGlasses.setText("");

                labeleyeX.setText("");
                labelEyeY.setText("");
                labelMaxFaceWidth.setText("");
                labelMinFaceWidth.setText("");


                labelYaw.setText("");
                labelPitch.setText("");
                labelRoll.setText("");
                labelBrisque.setText("");
                labelLeftEyeClose.setText("");
                labelRightEyeClose.setText("");
                labelEyeDistance.setText("");
            }


        });


    }


    private void resetAllCounters() {

        frameCounter = 0;
        counterNoFace = 0;
        counterMultiFace = 0;
        counterMask = 0;
        counterSunGlasses = 0;
        counterAnyGlasses = 0;
        counterClose = 0;
        counterFar = 0;
        counterCenter_Hold = 0;
        counterPose = 0;
        counterEyeClose = 0;
        counterBrisque = 0;

    }


    private String getDisplayMessage(String currentMessage) {


        int counter = (changeMessagefor / 2) + 1;

        // print("message \(currentMessage) count no face \(counterNoFace) counter \(counter)")

        if (counterNoFace >= counter) {
            return getString(R.string.no_faces_detected_message);
        }

        if (counterMultiFace >= counter) {

            return getString(R.string.multiple_faces_detected);

        }
        if (counterMask >= counter) {
            return getString(R.string.mask_detected_message);
        }
        if (counterSunGlasses >= counter) {
            //  return Localization.labelSunglassesDetected
            return getString(R.string.sunglasses_detected_message);
        }

        if (counterAnyGlasses >= counter) {
            //  return Localization.labelSunglassesDetected
            return getString(R.string.anyglasses_detected_message);
        }

        if (counterFar >= counter) {
            // return Localization.labelCameraFar
            return getString(R.string.camera_far_message);
        }

        if (counterClose >= counter) {
            // return Localization.labelCameraClose
            return getString(R.string.camera_close_message);
        }

        if (counterEyeClose >= counter) {
            //return Localization.labelEyesClosed
            return getString(R.string.eyes_closed_message);
        }


        if (counterCenter_Hold >= counter) {
            // return Localization.labelHoldStill
            return getString(R.string.hold_still_and_center);
        }


        if (frameCounter >= changeMessagefor) {

            return currentMessage;

        } else {
            return null;
        }

    }


    @SuppressLint("SetTextI18n")
    private void setStatus(FaceBox faceBox, float imageWidth, float facewidth, float minFaceWidth, float maxFaceWidth, float diffx, float diffy, float toleranceX, float toleranceY) {


        if (faceBox != null) {


            float pan = -1, pitch = -1, roll = -1, brisque = -1, eyeDist = -1, liveness = -0.01f, mask = 0.0f, anyglasses = 0.0f, sunglasses = 0.0f;

            float lefteyeClosed = 0.0f;
            float righteyeClosed = 0.0f;

            pan = faceBox.mPan;
            pitch = faceBox.mPitch;
            roll = faceBox.mRoll;
            brisque = faceBox.mBrisque;
            eyeDist = faceBox.mEyeDist;
            lefteyeClosed = faceBox.mLeftEyeClose;
            righteyeClosed = faceBox.mRightEyeClose;
            liveness = faceBox.mLiveness;

            mask = faceBox.mMask;
            sunglasses = faceBox.mSunGlass;
            anyglasses = faceBox.mAnyGlass;


            float widthPercentage = (facewidth / imageWidth) * 100;

            @SuppressLint("DefaultLocale") String widthPercentageString = String.format("%.2f", widthPercentage);

            if (mask > MASK_THRESHOLD) {
                labelMask.setTextColor(getResources().getColor(R.color.border_color_fail));
            } else {
                labelMask.setTextColor(getResources().getColor(R.color.border_color_pass));
            }


            if (sunglasses > SUN_GLASS_THRESHOLD) {
                labelSunGlasses.setTextColor(getResources().getColor(R.color.border_color_fail));

            } else {
                labelSunGlasses.setTextColor(getResources().getColor(R.color.border_color_pass));

            }

            if ((glassDetection == GlassDetection.ANY_GLASSES) && anyglasses > ANY_GLASS_THRESHOLD) {
                labelGlass.setTextColor(getResources().getColor(R.color.border_color_fail));

            } else {
                labelGlass.setTextColor(getResources().getColor(R.color.border_color_pass));
            }

            if (facewidth > maxFaceWidth) {

                labelMaxFaceWidth.setTextColor(getResources().getColor(R.color.border_color_fail));

            } else {

                labelMaxFaceWidth.setTextColor(getResources().getColor(R.color.border_color_pass));

            }

            if (facewidth < minFaceWidth) {

                labelMinFaceWidth.setTextColor(getResources().getColor(R.color.border_color_fail));


            } else {

                labelMinFaceWidth.setTextColor(getResources().getColor(R.color.border_color_pass));

            }


            if (diffx > toleranceX) {
                labeleyeX.setTextColor(getResources().getColor(R.color.border_color_fail));

            } else {
                labeleyeX.setTextColor(getResources().getColor(R.color.border_color_pass));
            }


            if (diffy > toleranceY) {
                labelEyeY.setTextColor(getResources().getColor(R.color.border_color_fail));

            } else {
                labelEyeY.setTextColor(getResources().getColor(R.color.border_color_pass));
            }

            if (Math.abs(pan) > YAW_THRESHOLD) {
                labelYaw.setTextColor(getResources().getColor(R.color.border_color_fail));
            } else {
                labelYaw.setTextColor(getResources().getColor(R.color.border_color_pass));
            }


            if (Math.abs(pitch) > PITCH_THRESHOLD) {
                labelPitch.setTextColor(getResources().getColor(R.color.border_color_fail));
            } else {
                labelPitch.setTextColor(getResources().getColor(R.color.border_color_pass));
            }


            if (Math.abs(roll) > ROLL_THRESHOLD) {
                labelRoll.setTextColor(getResources().getColor(R.color.border_color_fail));
            } else {
                labelRoll.setTextColor(getResources().getColor(R.color.border_color_pass));
            }


            if (brisque > BRISQUE_THRESHOLD) {
                labelBrisque.setTextColor(getResources().getColor(R.color.border_color_fail));
            } else {
                labelBrisque.setTextColor(getResources().getColor(R.color.border_color_pass));
            }

            if (lefteyeClosed < EYE_CLOSE_THRESHOLD) {
                labelLeftEyeClose.setTextColor(getResources().getColor(R.color.border_color_fail));

            } else {
                labelLeftEyeClose.setTextColor(getResources().getColor(R.color.border_color_pass));
            }

            if (righteyeClosed < EYE_CLOSE_THRESHOLD) {
                labelRightEyeClose.setTextColor(getResources().getColor(R.color.border_color_fail));

            } else {
                labelRightEyeClose.setTextColor(getResources().getColor(R.color.border_color_pass));

            }


            labelEyeDistance.setTextColor(getResources().getColor(R.color.border_color_pass));


            labelMask.setText("Mask : " + mask);
            labelSunGlasses.setText("Sunglasses : " + sunglasses);
            labelGlass.setText("AnyGlasses : " + anyglasses);
            labeleyeX.setText("Face centreX : " + diffx + ", <" + toleranceX);
            labelEyeY.setText("Face centreY : " + diffy + ", <" + toleranceY);
            labelMaxFaceWidth.setText("Max Face Width : " + facewidth + "(" + widthPercentageString + "%) ,  < " + maxFaceWidth);
            labelMinFaceWidth.setText("Min Face Width : " + facewidth + "(" + widthPercentageString + "%) , >" + minFaceWidth);

            labelYaw.setText("Yaw : " + pan);
            labelPitch.setText("Pitch : " + pitch);
            labelRoll.setText("Roll : " + roll);
            labelBrisque.setText("Brisque : " + brisque);
            labelLeftEyeClose.setText("Left eye : " + lefteyeClosed);
            labelRightEyeClose.setText("Right eye : " + righteyeClosed);
            labelEyeDistance.setText("Eye Distance: " + faceBox.mEyeDist);

        }


    }


    private void switchCamera() {

        isSwitchingCamera = true;

        isUseBackCamera = !isUseBackCamera;

        if (isUseBackCamera) {
            BASE_IMAGE_WIDTH_PERCENTAGE = 40;
        } else {
            BASE_IMAGE_WIDTH_PERCENTAGE = 45;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer.start();
        }

        showDialog(false);

        graphicOverlay.init(isUseBackCamera ? 1 : 0);
        graphicOverlay.invalidate();

        m_lensFacing = isUseBackCamera ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;

        bindCameraUseCases();


    }


}

package ai.tech5.pheonix.capture;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

import com.phoenixcapture.camerakit.AirsnapFace;
import com.phoenixcapture.camerakit.FaceBox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;


import ai.tech5.pheonix.capture.controller.AirsnapFaceThresholds;
import ai.tech5.pheonix.capture.controller.CompressBy;
import ai.tech5.pheonix.capture.controller.CompressionConfig;
import ai.tech5.pheonix.capture.controller.FaceCaptureController;
import ai.tech5.pheonix.capture.controller.FaceCaptureListener;
import ai.tech5.pheonix.capture.controller.FullFrontalCropConfig;
import ai.tech5.pheonix.capture.controller.GlassDetection;
import ai.tech5.pheonix.capture.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements FaceCaptureListener, TextWatcher {

    private ActivityMainBinding binding;
    private ProgressDialog progressDialog = null;
    private AppSharedPreference sharedPreference = null;

    private static String[] APP_PERMISSIONS = null;

    private int colorR = 125, colorG = 125, colorB = 125;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.txtVersion.setText("SDK " + AirsnapFace.Factory.getVersion());

        sharedPreference = new AppSharedPreference(MainActivity.this);
        binding.btnCapture.setEnabled(false);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            APP_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

        } else {
            APP_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }

        if (hasAllPermissionsGranted()) {

            binding.btnCapture.setEnabled(true);
        } else {

            requestPermissionLauncher.launch(APP_PERMISSIONS);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        binding.btnCapture.setOnClickListener((View v) -> {


            binding.txtLivenessStatus.setText("");
            binding.imgFace.setImageBitmap(null);

            binding.txtImageType.setText("");

            binding.statusLayout.removeAllViews();


            startFaceCapture();

        });


        binding.chkBoxGetMrtd.setOnCheckedChangeListener((CompoundButton compoundButton, boolean isChecked) -> {

            if (isChecked) {
                binding.viewSegmented.setVisibility(View.VISIBLE);
            } else {
                binding.viewSegmented.setVisibility(View.GONE);
                binding.chkBoxGetSegmentedImage.setChecked(false);
            }


        });


        binding.edtTextR.addTextChangedListener(this);
        binding.edtTextG.addTextChangedListener(this);
        binding.edtTextB.addTextChangedListener(this);

        binding.viewSegmentedImageBackground.setBackgroundColor(Color.rgb(colorR, colorG, colorB));


    }


    private void startFaceCapture() {
        FaceCaptureController controller = FaceCaptureController.getInstance();


        //No need to call this if Tech5 license portal is used to get the  license. Only needed in case of  license portal url chanhged or license portal is hosted on customer premise
        //controller.setUrl("https://pheonix-lic.tech5.tech");


        controller.setUseBackCamera(binding.radBtnBackCam.isChecked());
        controller.setAutoCapture(binding.radBtnAutoCapture.isChecked());

        controller.setOcclusionEnabled(sharedPreference.getIsOcculusionEnabled());
        controller.setEyeClosedEnabled(sharedPreference.getIsEyeClosedEnabled());

        controller.setGlassDetection(sharedPreference.getIsSunGlassesDetection() ? GlassDetection.SUN_GLASSES : GlassDetection.ANY_GLASSES);

        controller.writeLogs(false);

        controller.setMessagesFrequency(6);
        controller.setFontSize(23);


        // controller.setTitle("Face Capture");
        controller.setCaptureTimeoutInSecs(sharedPreference.getCaptureTimeout());
        controller.setShowBackButton(true);

        //to enable/disable camera switching in face capture screen, by default disabled
        controller.setEnableCameraSwitching(sharedPreference.getEnableCameraSwitching());

        controller.setFrameCapture(binding.radBtnCaptureModeFast.isChecked());


        controller.setCompression(binding.chkBoxIsCompress.isChecked());

        if (binding.chkBoxIsCompress.isChecked()) {

            CompressionConfig compressionConfig = getCompressionConfig();

            controller.setCompressionConfig(compressionConfig);
        }


        controller.setIsISOEnabled(binding.chkBoxIcao.isChecked());

        controller.setIsGetFullFrontalCrop(binding.chkBoxGetMrtd.isChecked());

        FullFrontalCropConfig config = new FullFrontalCropConfig();

        config.getSegmentedImage(binding.chkBoxGetSegmentedImage.isChecked());
        config.setSegmentedImageBackgroundColor(colorR, colorG, colorB);

        controller.setFullFrontalCropConfig(config);


        AirsnapFaceThresholds thresholds = getAirsnapFaceThresholds();
        controller.setAirsnapFaceThresholds(thresholds);


        controller.setEnableCaptureAfter(sharedPreference.getEnableCaptureAfter());

        controller.startFaceCapture("", MainActivity.this, this);
    }

    @NonNull
    private CompressionConfig getCompressionConfig() {
        CompressionConfig compressionConfig = new CompressionConfig();
        if (sharedPreference.getIsCompressByCompressionRate()) {
            compressionConfig.setCompressBy(CompressBy.COMPRESS_BY_COMPRESSION_RATE);
            compressionConfig.setCompressionRate(sharedPreference.getCompressionQuality());
        } else {
            compressionConfig.setCompressBy(CompressBy.COMPRESS_BY_TARGET_SIZE);
            compressionConfig.setTargetSizeInKbs(sharedPreference.getTargetSize());
        }
        return compressionConfig;
    }

    @NonNull
    private AirsnapFaceThresholds getAirsnapFaceThresholds() {
        AirsnapFaceThresholds thresholds = new AirsnapFaceThresholds();
        thresholds.setPITCH_THRESHOLD(sharedPreference.getPitchThreshold());
        thresholds.setYAW_THRESHOLD(sharedPreference.getYawThreshold());
        thresholds.setRollThreshold(sharedPreference.getRollThreshold());
        thresholds.setBRISQUE_THRESHOLD(sharedPreference.getBrisqueThreshold());
        thresholds.setMASK_THRESHOLD(sharedPreference.getMaskThreshold());
        thresholds.setANYGLASS_THRESHOLD(sharedPreference.getAnySunGlassThreshold());
        thresholds.setSUNGLASS_THRESHOLD(sharedPreference.getAnySunGlassThreshold());
        thresholds.setEYE_CLOSE_THRESHOLD(sharedPreference.getEyeCloseThreshold());
        thresholds.setLIVENESS_THRESHOLD(sharedPreference.getLivenessThreshold());

        thresholds.setFaceCentreToImageCentreTolerance(sharedPreference.getImageCentreToFaceCentreTolerance());

        thresholds.setFaceWidthToImageWidthRatioTolerance(sharedPreference.getFaceWidthTolerance());
        return thresholds;
    }


    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {

        boolean isAllPermissionsGranted = true;

        for (Boolean isGranted : result.values()) {

            if (Boolean.FALSE.equals(isGranted)) {
                isAllPermissionsGranted = false;
                break;

            }
        }

        if (isAllPermissionsGranted) {
            binding.btnCapture.setEnabled(true);
        }


    });


    @Override
    public void onFaceCaptured(byte[] image, byte[] originalImage, FaceBox faceBox) {
        Log.d("TAG", "onFaceCaptured()....");
        long time = System.currentTimeMillis();

        if (faceBox != null && faceBox.mHasPortalImageSegmented == 1 && faceBox.mPortalImageSegmented != null) {

            byte[] portalImageSegmented = compressFaceImage(faceBox.mPortalImageSegmented, 100);

            new Thread(() -> {

                try {
                    saveImage(portalImageSegmented, time + "_portal_segmented.jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }

        if (originalImage != null && originalImage.length > 0) {

            Log.d("TAG", "original face image size : " + originalImage.length + " bytes");

            new Thread(() -> {

                try {
                    saveImage(originalImage, time + "_original.jpg");
                } catch (IOException ignore) {

                }
            }).start();
        }

        if (image != null && image.length > 0) {

            Log.d("TAG", "compressed face image size : " + image.length + " bytes");

            new Thread(() -> {

                try {
                    saveImage(image, time + ".jpg");
                } catch (IOException ignore) {

                }
            }).start();


            try {

                Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length, null);

                String imageResolution = bmp.getWidth() + "X" + bmp.getHeight();
                int originalImageSize = originalImage == null ? 0 : originalImage.length;

                binding.txtImageType.setText(getResources().getString(R.string.image_dims_size, imageResolution, formatByteSize(image.length, true), formatByteSize(originalImageSize, false)));


                Log.d("TAG", "image width " + bmp.getWidth() + "x" + bmp.getHeight());
                GlideApp.with(MainActivity.this).load(image).into(binding.imgFace);

            } catch (Exception ignored) {

            }

        } else {
            binding.imgFace.setImageBitmap(null);
        }

        if (faceBox != null) {


            ArrayList<String> passedParams = new ArrayList<>();
            ArrayList<String> failedParams = new ArrayList<>();


            if (binding.chkBoxIcao.isChecked()) {


                if (faceBox.mBlurScore >= sharedPreference.getMinBlur() && faceBox.mBlurScore <= sharedPreference.getMaxBlur()) {

                    passedParams.add("Blur : " + faceBox.mBlurScore);

                } else {
                    failedParams.add("Blur : " + faceBox.mBlurScore);
                }


                if (faceBox.mExposureScore >= sharedPreference.getMinExposure() && faceBox.mExposureScore <= sharedPreference.getMaxExposure()) {

                    passedParams.add("Exposure : " + faceBox.mExposureScore);

                } else {
                    failedParams.add("Exposure : " + faceBox.mExposureScore);
                }


                if (faceBox.mBrightnessScore >= sharedPreference.getMinBrightness() && faceBox.mBrightnessScore <= sharedPreference.getMaxBrightness()) {

                    passedParams.add("Brightness : " + faceBox.mBrightnessScore);

                } else {
                    failedParams.add("Brightness : " + faceBox.mBrightnessScore);
                }

                if (faceBox.mSkinToneScore >= sharedPreference.getSkinToneThreshold()) {
                    passedParams.add("SkinTone : :" + faceBox.mSkinToneScore);
                } else {
                    failedParams.add("SkinTone : :" + faceBox.mSkinToneScore);
                }


                if (faceBox.mHotspotScore <= sharedPreference.getHotspotsThreshold()) {
                    passedParams.add("Hotspots : " + faceBox.mHotspotScore);
                } else {
                    failedParams.add("Hotspots : " + faceBox.mHotspotScore);
                }


                if (faceBox.mRedEyesScore <= sharedPreference.getRedEyesThreshold()) {
                    passedParams.add("Red/White Eyes : " + faceBox.mRedEyesScore);
                } else {
                    failedParams.add("Red/White Eyes : " + faceBox.mRedEyesScore);
                }


                if (faceBox.mMouthOpenScore <= sharedPreference.getMouthOpenThreshold()) {
                    passedParams.add("Mouth Open : " + faceBox.mMouthOpenScore);
                } else {
                    failedParams.add("Mouth Open : " + faceBox.mMouthOpenScore);
                }


                if (faceBox.mLaughScore <= sharedPreference.getLaughThreshold()) {
                    passedParams.add("Laugh : " + faceBox.mLaughScore);
                } else {
                    failedParams.add("Laugh : " + faceBox.mLaughScore);
                }


                if (faceBox.mUniformBackgroundScore <= sharedPreference.getUniformBackgroundThreshold()) {
                    passedParams.add("Uniform Background : " + faceBox.mUniformBackgroundScore);
                } else {
                    failedParams.add("Uniform Background : " + faceBox.mUniformBackgroundScore);
                }


                if (faceBox.mUniformBackgroundColorScore <= sharedPreference.getUniformBackgroundColorThreshold()) {
                    passedParams.add("Uniform BackgroundColor : Dark (" + faceBox.mUniformBackgroundColorScore + " )");
                } else {
                    //passed = false
                    passedParams.add("Uniform BackgroundColor : Dark (" + faceBox.mUniformBackgroundColorScore + " )");
                }


                if (faceBox.mUniformIlluminationScore <= sharedPreference.getIlluminationThreshold()) {
                    passedParams.add("Uniform Illumination : " + faceBox.mUniformIlluminationScore);
                } else {
                    failedParams.add("Uniform Illumination : " + faceBox.mUniformIlluminationScore);
                }

                if (faceBox.mFaceBackDiffScore >= sharedPreference.getFaceBackDiffThreshold()) {
                    passedParams.add("Face-Background Difference : " + faceBox.mFaceBackDiffScore);
                } else {
                    failedParams.add("Face-Background Difference : " + faceBox.mFaceBackDiffScore);
                }


            }

            passedParams.add("Yaw : " + faceBox.mPan);
            passedParams.add("Pitch : " + faceBox.mPitch);
            passedParams.add("Roll :  " + faceBox.mRoll);
            passedParams.add("Eye Distance : " + faceBox.mEyeDist);
            passedParams.add("Horizontal Gaze: " + faceBox.mHorizontalGaze);
            passedParams.add("Vertical Gaze: " + faceBox.mVerticalGaze);


            if (sharedPreference.getIsEyeClosedEnabled()) {

                passedParams.add("Left Eye Closed : " + faceBox.mLeftEyeClose);
                passedParams.add("Right Eye Closed : " + faceBox.mRightEyeClose);
            }

            if (sharedPreference.getIsOcculusionEnabled()) {

                passedParams.add("Mask : " + faceBox.mMask);
                passedParams.add("Any Glasses : " + faceBox.mAnyGlass);
                passedParams.add("Sun Glasses : " + faceBox.mSunGlass);

                if(binding.chkBoxIcao.isChecked()) {

                    if (faceBox.mHeadphonesScore < sharedPreference.getHeadPhonesThreshold()) {
                        passedParams.add("Head Phones : " + faceBox.mHeadphonesScore);
                    } else {
                        failedParams.add("Head Phones  : " + faceBox.mHeadphonesScore);
                    }

                    if (faceBox.mHatScore < sharedPreference.getHatThreshold()) {
                        passedParams.add("Hat : " + faceBox.mHatScore);
                    } else {
                        failedParams.add("Hat : " + faceBox.mHatScore);
                    }

                    if (faceBox.mHandOcclusion < sharedPreference.getHandOcclusionThreshold()) {
                        passedParams.add("Hand Occlusion : " + faceBox.mHandOcclusion);
                    } else {
                        failedParams.add("Hand Occlusion : " + faceBox.mHandOcclusion);
                    }

                }
            }


            final float scale = getResources().getDisplayMetrics().density;

            int dpHeightInPx = (int) (20 * scale);

            if (!passedParams.isEmpty()) {

                for (String pass : passedParams) {

                    TextView nameLabel = new TextView(MainActivity.this);
                    nameLabel.setTextColor(getResources().getColor(R.color.green));
                    nameLabel.setText(pass);

                    nameLabel.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, dpHeightInPx));


                    binding.statusLayout.addView(nameLabel);

                }


                View space = new View(MainActivity.this);
                space.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, dpHeightInPx));
                binding.statusLayout.addView(space);
//                icaoScoresStack.addArrangedSubview(view)


            }


            if (!failedParams.isEmpty()) {


                for (String failed : failedParams) {

                    TextView nameLabel = new TextView(MainActivity.this);
                    nameLabel.setTextColor(getResources().getColor(R.color.red));
                    nameLabel.setText(failed);

                    nameLabel.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, dpHeightInPx));


                    binding.statusLayout.addView(nameLabel);

                }

            }


        }


    }

    @Override
    public void OnFaceCaptureFailed(String errorMessage) {

        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();


    }

    @Override
    public void onCancelled() {
        Toast.makeText(MainActivity.this, "User cancelled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTimedout(byte[] faceImageBytes) {
        Toast.makeText(MainActivity.this, "Capture timed out", Toast.LENGTH_LONG).show();

        if (faceImageBytes != null && faceImageBytes.length > 0) {


            try {

                Bitmap bmp = BitmapFactory.decodeByteArray(faceImageBytes, 0, faceImageBytes.length, null);
                Log.d("TAG", "image width " + bmp.getWidth() + "x" + bmp.getHeight());
                GlideApp.with(MainActivity.this).load(faceImageBytes).into(binding.imgFace);


            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            binding.imgFace.setImageBitmap(null);
        }
    }


    public static void showMessage(Context context, String message) {
        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }


    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgress();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean hasAllPermissionsGranted() {

        for (String permission : APP_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private void saveImage(byte[] bytes, @NonNull String name) throws IOException {
        boolean saved = false;
        OutputStream fos = null;
        String IMAGES_FOLDER_NAME = "AirsnapFace";


        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();

                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME);

                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;

                File file = new File(imagesDir);


                if (!file.exists()) {
                    file.mkdir();
                }

                File image = new File(imagesDir, name);
                fos = new FileOutputStream(image);


            }


            fos.write(bytes);

        } catch (Exception e) {

        } finally {
            if (fos != null) {
                fos.close();
            }
        }

    }


    public static byte[] compressFaceImage(Bitmap image, int compressRate) {
        ByteArrayOutputStream stream = null;

        try {
            stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, compressRate, stream);
            return stream.toByteArray();

        } catch (Exception e) {

        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void afterTextChanged(Editable editable) {


        if (binding.edtTextR.length() > 0) {
            try {
                int r = Integer.parseInt(binding.edtTextR.getText().toString());

                if (r >= 0 && r <= 255) {
                    colorR = r;
                } else {
                    binding.edtTextR.setText("");
                }

            } catch (Exception e) {

            }
        }


        if (binding.edtTextG.length() > 0) {
            try {
                int g = Integer.parseInt(binding.edtTextG.getText().toString());

                if (g >= 0 && g <= 255) {
                    colorG = g;
                } else {
                    binding.edtTextG.setText("");
                }

            } catch (Exception e) {

            }
        }

        if (binding.edtTextB.length() > 0) {
            try {
                int b = Integer.parseInt(binding.edtTextB.getText().toString());

                if (b >= 0 && b <= 255) {
                    colorB = b;
                } else {
                    binding.edtTextB.setText("");
                }

            } catch (Exception e) {

            }
        }

        Log.d("TAG", "rgb" + colorR + "-" + colorG + "-" + colorB);


        binding.viewSegmentedImageBackground.setBackgroundColor(Color.rgb(colorR, colorG, colorB));

    }


    @SuppressLint("DefaultLocale")
    public static String formatByteSize(long size, boolean useBinaryPrefixes) {
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }

        double value = size;
        int unit = useBinaryPrefixes ? 1024 : 1000;
        // int power = 0;

        for (String prefix : new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB"}) {
            if (value < unit) {
                return String.format("%.1f %s", value, prefix);
            }
            value /= unit;
            // power++;
        }

        return String.format("%.1f %s", value, "EB");
    }
}
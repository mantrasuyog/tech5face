package ai.tech5.pheonix.capture;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.phoenixcapture.camerakit.AirsnapFace;

import ai.tech5.pheonix.capture.controller.FaceCaptureController;
import ai.tech5.pheonix.capture.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivitySettingsBinding binding;

    private String errorMessage = "";
    boolean validFields = true;

    private AppSharedPreference sharedPreference = null;

    private ProgressDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreference = new AppSharedPreference(SettingsActivity.this);

        binding.chkBoxOcculusion.setChecked(sharedPreference.getIsOcculusionEnabled());
        binding.chkBoxEyeClosed.setChecked(sharedPreference.getIsEyeClosedEnabled());

        binding.edtEnableAfter.setText(String.valueOf(sharedPreference.getEnableCaptureAfter()));
        binding.edtPitchThreshold.setText(String.valueOf(sharedPreference.getPitchThreshold()));
        binding.edtYawThreshold.setText(String.valueOf(sharedPreference.getYawThreshold()));
        binding.edtRollThreshold.setText(String.valueOf(sharedPreference.getRollThreshold()));
        binding.edtMaskThreshold.setText(String.valueOf(sharedPreference.getMaskThreshold()));
        binding.edtSunglassThreshold.setText(String.valueOf(sharedPreference.getAnySunGlassThreshold()));
        binding.edtBrisqueThreshold.setText(String.valueOf(sharedPreference.getBrisqueThreshold()));
        binding.edtLivenessThreshold.setText(String.valueOf(sharedPreference.getLivenessThreshold()));
        binding.edtEyeCloseThreshold.setText(String.valueOf(sharedPreference.getEyeCloseThreshold()));

        binding.radBtnByCompRate.setChecked(sharedPreference.getIsCompressByCompressionRate());
        binding.radBtnByTargetSize.setChecked(!sharedPreference.getIsCompressByCompressionRate());

        if (sharedPreference.getIsCompressByCompressionRate()) {
            binding.radBtnByCompRate.setChecked(true);
            binding.radBtnByTargetSize.setChecked(false);
            binding.txtLayoutCompQuality.setVisibility(View.VISIBLE);
            binding.txtLayoutTargetSize.setVisibility(View.GONE);
        } else {
            binding.radBtnByCompRate.setChecked(false);
            binding.radBtnByTargetSize.setChecked(true);
            binding.txtLayoutCompQuality.setVisibility(View.GONE);
            binding.txtLayoutTargetSize.setVisibility(View.VISIBLE);
        }

        binding.radGroupCompressBy.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rad_btn_by_comp_rate) {
                binding.txtLayoutCompQuality.setVisibility(View.VISIBLE);
                binding.txtLayoutTargetSize.setVisibility(View.GONE);
            } else {
                binding.txtLayoutCompQuality.setVisibility(View.GONE);
                binding.txtLayoutTargetSize.setVisibility(View.VISIBLE);
            }
        });

        if (sharedPreference.getIsOcculusionEnabled()) {
            binding.radGroupGlasses.setVisibility(View.VISIBLE);
            binding.glassDetectionLabel.setVisibility(View.VISIBLE);
        } else {
            binding.radGroupGlasses.setVisibility(View.GONE);
            binding.glassDetectionLabel.setVisibility(View.GONE);
        }

        binding.radBtnOnlySunGlass.setChecked(sharedPreference.getIsSunGlassesDetection());
        binding.radBtnAnyGlass.setChecked(!sharedPreference.getIsSunGlassesDetection());

        binding.chkBoxOcculusion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.radGroupGlasses.setVisibility(View.VISIBLE);
                binding.glassDetectionLabel.setVisibility(View.VISIBLE);
            } else {
                binding.radGroupGlasses.setVisibility(View.GONE);
                binding.glassDetectionLabel.setVisibility(View.GONE);
            }
        });

        binding.edtCompressionQuality.setText(String.valueOf(sharedPreference.getCompressionQuality()));
        binding.edtMinBlur.setText(String.valueOf(sharedPreference.getMinBlur()));
        binding.edtMaxBlur.setText(String.valueOf(sharedPreference.getMaxBlur()));
        binding.edtMinExposure.setText(String.valueOf(sharedPreference.getMinExposure()));
        binding.edtMaxExposure.setText(String.valueOf(sharedPreference.getMaxExposure()));
        binding.edtMinBrightness.setText(String.valueOf(sharedPreference.getMinBrightness()));
        binding.edtMaxBrightness.setText(String.valueOf(sharedPreference.getMaxBrightness()));


        binding.edtSkinTone.setText(String.valueOf(sharedPreference.getSkinToneThreshold()));
        binding.edtHotspots.setText(String.valueOf(sharedPreference.getHotspotsThreshold()));
        binding.edtRedEyes.setText(String.valueOf(sharedPreference.getRedEyesThreshold()));
        binding.edtMouthOpen.setText(String.valueOf(sharedPreference.getMouthOpenThreshold()));
        binding.edtLaugh.setText(String.valueOf(sharedPreference.getLaughThreshold()));
        binding.edtUniformBackground.setText(String.valueOf(sharedPreference.getUniformBackgroundThreshold()));
        binding.edtUniformBackgroundColor.setText(String.valueOf(sharedPreference.getUniformBackgroundColorThreshold()));
        binding.edtUniformIllumination.setText(String.valueOf(sharedPreference.getIlluminationThreshold()));

        binding.edtFaceBackgroundDifference.setText(String.valueOf(sharedPreference.getFaceBackDiffThreshold()));
        binding.edtCaptureTimeout.setText(String.valueOf(sharedPreference.getCaptureTimeout()));
        binding.edtFaceWidthTolerance.setText(String.valueOf(sharedPreference.getFaceWidthTolerance()));
        binding.edtFaceCentreToImageCentreTolerance.setText(String.valueOf(sharedPreference.getImageCentreToFaceCentreTolerance()));


        binding.switchCameraSwitching.setChecked(sharedPreference.getEnableCameraSwitching());

        binding.btnSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        errorMessage = "";
        validFields = true;

        validateFields();

        if (validFields) {
            sharedPreference.setIsOcculusionEnabled(binding.chkBoxOcculusion.isChecked());
            sharedPreference.setIsEyeClosedEnabled(binding.chkBoxEyeClosed.isChecked());
            sharedPreference.setIsLivenessEnabled(binding.chkBoxLiveness.isChecked());

            sharedPreference.setEnableCaptureAfter(getInt(binding.edtEnableAfter));
            sharedPreference.setPitchThreshold(getInt(binding.edtPitchThreshold));
            sharedPreference.setYawThreshold(getInt(binding.edtYawThreshold));
            sharedPreference.setRollThreshold(getInt(binding.edtRollThreshold));
            sharedPreference.setMaskThreshold(getFloat(binding.edtMaskThreshold));
            sharedPreference.setAnySunGlassThreshold(getFloat(binding.edtSunglassThreshold));
            sharedPreference.setBrisqueThreshold(getInt(binding.edtBrisqueThreshold));
            sharedPreference.setLivenessThreshold(getFloat(binding.edtLivenessThreshold));
            sharedPreference.setEyeCloseThreshold(getFloat(binding.edtEyeCloseThreshold));

            sharedPreference.setCompressionQuality(getInt(binding.edtCompressionQuality));
            sharedPreference.setMinBlur(getFloat(binding.edtMinBlur));
            sharedPreference.setMaxBlur(getFloat(binding.edtMaxBlur));
            sharedPreference.setMinExposure(getFloat(binding.edtMinExposure));
            sharedPreference.setMaxExposure(getFloat(binding.edtMaxExposure));
            sharedPreference.setMinBrightness(getFloat(binding.edtMinBrightness));
            sharedPreference.setMaxBrightness(getFloat(binding.edtMaxBrightness));

            sharedPreference.setSkinToneThreshold(getFloat(binding.edtSkinTone));
            sharedPreference.setHotspotsThreshold(getFloat(binding.edtHotspots));
            sharedPreference.setRedEyesThreshold(getFloat(binding.edtRedEyes));
            sharedPreference.setMouthOpenThreshold(getFloat(binding.edtMouthOpen));
            sharedPreference.setLaughThreshold(getFloat(binding.edtLaugh));
            sharedPreference.setUniformBackgroundThreshold(getFloat(binding.edtUniformBackground));
            sharedPreference.setUniformBackgroundColorThreshold(getFloat(binding.edtUniformBackgroundColor));
            sharedPreference.setIlluminationThreshold(getFloat(binding.edtUniformIllumination));

            sharedPreference.setFaceBackDiffThreshold(getFloat(binding.edtFaceBackgroundDifference));
            sharedPreference.setCaptureTimeout(getInt(binding.edtCaptureTimeout));

            sharedPreference.setIsCompressByCompressionRate(binding.radBtnByCompRate.isChecked());
            if (binding.radBtnByCompRate.isChecked()) {
                sharedPreference.setCompressionQuality(getInt(binding.edtCompressionQuality));
            } else {
                sharedPreference.setTargetSize(getInt(binding.edtTargetSize));
            }

            sharedPreference.setIsSunGlassesDetection(binding.radBtnOnlySunGlass.isChecked());

            sharedPreference.setFaceWidthTolerance(getFloat(binding.edtFaceWidthTolerance));
            sharedPreference.setImageCentreToFaceCentreTolerance(getFloat(binding.edtFaceCentreToImageCentreTolerance));

            sharedPreference.setEnableCameraSwitching(binding.switchCameraSwitching.isChecked());

            AirsnapFace.Factory.closeSDK();
            finish();
        } else {
            Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    void validateFields() {
        if (!isValidFloat(binding.edtMaskThreshold) || getFloat(binding.edtMaskThreshold) > 1) {
            errorMessage = "Invalid Mask Threshold";
            validFields = false;
            return;
        }

        if (!isValidFloat(binding.edtSunglassThreshold) || getFloat(binding.edtSunglassThreshold) > 1) {
            errorMessage = "Invalid Sunglasses Threshold";
            validFields = false;
            return;
        }

        if (!isValidFloat(binding.edtLivenessThreshold) || getFloat(binding.edtLivenessThreshold) > 1) {
            errorMessage = "Invalid Liveness Threshold";
            validFields = false;
            return;
        }

        if (!isValidFloat(binding.edtEyeCloseThreshold) || getFloat(binding.edtEyeCloseThreshold) > 2) {
            errorMessage = "Invalid eyeClosed Threshold";
            validFields = false;
            return;
        }
    }

    public float getFloat(EditText editText) {
        try {
            return Float.valueOf(editText.getText().toString().trim());
        } catch (Exception e) {
            return 0.0f;
        }
    }

    public int getInt(EditText editText) {
        try {
            return Integer.parseInt(editText.getText().toString().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isValidFloat(EditText editText) {
        if (editText.length() > 0) {
            try {
                Float.valueOf(editText.getText().toString().trim());
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_deregister) {
            deregisterDevice();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deregisterDevice() {
        showDialog(true);
        FaceCaptureController controller = FaceCaptureController.getInstance();
        controller.deregisterDevice(SettingsActivity.this, isDeviceDeregistred -> {
            showDialog(false);
            if (isDeviceDeregistred) {
                showToast("Device Deregistration success");
            } else {
                showToast("Device Deregistration failed");
            }
        });
    }

    private void showDialog(boolean isShow) {
        runOnUiThread(() -> {
            if (isShow) {
                if (dialog == null) {
                    dialog = new ProgressDialog(SettingsActivity.this);
                    dialog.setMessage(getString(ai.tech5.ui.R.string.please_wait));
                    dialog.setCancelable(false);
                    dialog.show();
                }
            } else {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show();
        });
    }
}
package com.projectx.eyemusic;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class EyeDetectionActivity extends AppCompatActivity {
    private static final String TAG = EyeDetectionActivity.class.getSimpleName();
    private Camera mCamera;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eye_detection);
        Button btn_preview = findViewById(R.id.btn_eye_detection_preview);

        requestPermissionLauncher = registerForActivityResult(new RequestPermission(), isGranted -> {
            if(isGranted){
                // Permission is granted. Continue the action or workflow in your
                // app.
                initCamera();
            }else{
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                showError("Permission not granted");
            }
        });
        // check for camera on device just in case manifest check doesn't execute
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            showError("No camera on this device, can't use app");
        }else{
            // check if permissions were granted
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                initCamera();
            }else{
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }

        btn_preview.setOnClickListener(view -> {
            if(!isCameraNull()){
                // TODO Enable Camera preview
                // this currently crashes
                // mCamera.startPreview();

            }
        });

    }

    private void initCamera() {
        int id = getFrontFacingCamera();
        if (id >= 0) {
            mCamera = getCameraInstance(id);
        } else showError("No front facing camera found");
    }


    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(!isCameraNull()){
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * Only get front facing camera instance*/
    private Camera getCameraInstance(int id){
        Camera c = null;
        try{
            c = Camera.open(id);
        }catch (Exception e){
            // Camera is not available (in use or non-existent)
            showError(e.getMessage());
        }
        return c;
    }

    private int getFrontFacingCamera(){
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if(info.facing == CameraInfo.CAMERA_FACING_FRONT){
                showInfo("Found front facing camera :" + info.facing);
                cameraId = i;
                return cameraId;
            }
        }
        return cameraId;
    }

    private void showError(String message) {
        Toast.makeText(EyeDetectionActivity.this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }

    private void showInfo(String message){
        Toast.makeText(EyeDetectionActivity.this, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message);
    }

    private boolean isCameraNull(){
        return mCamera == null;
    }
}
package com.projectx.eyemusic;

import android.hardware.Camera;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CameraExtractionFragment extends Fragment {
    private static final String TAG = "CameraExtractionFragmen";

    private CameraExtraction mCameraExtraction;
    Camera mCamera;
    int mNumberOfCameras;
    int cameraId;
    int rotation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + this.getActivity());

        mCameraExtraction = new CameraExtraction(
                this.getActivity().getBaseContext(),
                this.getActivity().getWindowManager().getDefaultDisplay().getRotation()
        );

        // Find the total number of cameras available
        mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the rear-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)               {
        Log.d(TAG, "onCreateView: ");
        return mCameraExtraction;
//        View myLayout = inflater.inflate(R.layout.fragment_layout, container, false);
//        return myLayout;

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        // Use mCurrentCamera to select the camera desired to safely restore
        // the fragment after the camera has been changed
        mCamera = Camera.open(cameraId);
        mCameraExtraction.setCamera(mCamera);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");

        if (mCamera != null)
        {
            mCamera.stopPreview();
            mCamera.release();
        }
    }


    // Modo en el que se pinta la cámara: encajada por dentro o saliendo los bordes por fuera.
    public enum CameraViewMode {

        /**
         * Inner mode
         */
        Inner,
        /**
         * Outer mode
         */
        Outer
    }
}
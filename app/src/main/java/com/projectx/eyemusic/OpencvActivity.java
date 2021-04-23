package com.projectx.eyemusic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.Collections;
import java.util.List;

public class OpencvActivity extends CameraActivity implements CvCameraViewListener2 {
    private final String TAG = OpencvActivity.class.getSimpleName();
    // openCV variables
    private Mat mRgba;
    // define views
    private CameraBridgeViewBase mOpenCVCameraView;
    private Mat mGray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv);
        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.i(TAG, "OpenCVLoader.initDebug();");
        OpenCVLoader.initDebug();


        // initialize views
        mOpenCVCameraView = findViewById(R.id.opencv_camera_view);
        mOpenCVCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCVCameraView.setCvCameraViewListener(this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(mOpenCVCameraView != null){
            mOpenCVCameraView.disableView();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    @Override
    public void onResume()
    {
        Log.i(TAG, "called onResume");
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Internal OpenCV library not found.");
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");

            mOpenCVCameraView.enableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOpenCVCameraView != null){
            mOpenCVCameraView.disableView();
        }
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat(height, width, CvType.CV_8UC4);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped(){
        mGray.release();
        mRgba.release();
    }

    /**
     * Implementation of CvCameraViewListener interface allows you to add processing steps after
     * frame grabbing from camera and before its rendering on screen.
     * The most important function is onCameraFrame.
     * It is callback function and it is called on retrieving frame from camera.
     * The callback input is object of CvCameraViewFrame class that represents frame from camera.*/
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){

        mRgba = inputFrame.rgba();
        Core.flip(mRgba, mRgba, -1);
        return mRgba;
    }

    /**
     * override of Camera Activity class
     */
    @Override
    protected List<?extends CameraBridgeViewBase> getCameraViewList () {
        return Collections.singletonList(mOpenCVCameraView);
    }
}
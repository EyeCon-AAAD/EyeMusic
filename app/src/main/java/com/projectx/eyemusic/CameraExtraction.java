package com.projectx.eyemusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

public class CameraExtraction extends ViewGroup implements SurfaceHolder.Callback {

    private final String TAG = "CameraExtraction";

    Camera mCamera;
    SurfaceHolder mHolder;
    SurfaceView mSurfaceView;
    int mNumberOfCameras;
    int cameraId;
    Rect desiredSize;
    CameraExtractionFragment.CameraViewMode cameraViewMode;
    boolean mSurfaceCreated = false;
    List<Camera.Size> mSupportedPreviewSizes;
    int rotation;
    Camera.Size mPreviewSize;

    public CameraExtraction(Context context, int rotation) {
        super(context);

        this.rotation = rotation;

        mSurfaceView = new SurfaceView(context);

        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraViewMode = CameraExtractionFragment.CameraViewMode.Inner;
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            if (mSurfaceCreated) requestLayout();
        }
    }

    public void switchCamera(Camera camera) {
        setCamera(camera);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mSurfaceView == null ||mSurfaceView.getHolder() == null) return;

        if (mSurfaceView.getHolder().getSurface() == null) {
            // preview surface does not exist
            return;
        }

        final int width = resolveSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {

            Log.d(TAG, "onMeasure: mSupportedPreviewSizes is not null.");
            mPreviewSize = getNearestPreviewSize(mCamera.new Size(widthMeasureSpec,heightMeasureSpec));
        }

        /* was giving error
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            Log.d(TAG, "onMeasure: mPreviewSize.width, mPreviewSize.height " + mPreviewSize.width+" , "+ mPreviewSize.height);
            mCamera.setParameters(parameters);
        }*/
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height
                        / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width
                        / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2, width,
                        (height + scaledChildHeight) / 2);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                    }
                });
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
        if (mSurfaceView == null || mSurfaceView.getHolder() == null) return;

        if (mSurfaceView.getHolder().getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters param = mCamera.getParameters();
        Point previewSize = new Point(640,480);

        Camera.Size size = getNearestPreviewSize(mCamera.new Size(previewSize.x,previewSize.y));
        param.setPreviewSize(size.width, size.height);
        mCamera.setParameters(param);
        rotation = setCameraDisplayOrientation(cameraId, mCamera);

        // start preview with new settings
        try {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    // TODO Auto-generated method stub

                }
            });
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d("AndroidControlSurfaceView",
                    "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null)
        {
//          mCamera.stopPreview();
//          mCamera.release();
        }
    }


    protected Rect getCameraViewSizeCompensated(Camera.Size cameraPreviewSize, Point hostViewSize) {
        Rect toReturn=null;

        float ratioWidth = hostViewSize.x / (float)cameraPreviewSize.width;
        float ratioHeight = hostViewSize.y / (float)cameraPreviewSize.height;

        switch (cameraViewMode){
            case Inner:
                if (ratioWidth < ratioHeight) {
                    int newHeight = (int)(cameraPreviewSize.height*ratioWidth);
                    int y = (hostViewSize.y - newHeight) / 2;
                    toReturn = new Rect(0, y, hostViewSize.x, y+newHeight);
                } else {
                    int newWidth = (int)(cameraPreviewSize.width*ratioHeight);
                    int x = (hostViewSize.x - newWidth) / 2;
                    toReturn = new Rect(x, 0, x+newWidth,hostViewSize.y);
                }
                break;
            case Outer:
                if (ratioWidth < ratioHeight) {
                    int newWidth = (int)(cameraPreviewSize.width*ratioHeight);
                    int x = (hostViewSize.x - newWidth) / 2;
                    toReturn = new Rect(x, 0, x+newWidth,hostViewSize.y);
                } else {
                    int newHeight = (int)(cameraPreviewSize.height*ratioWidth);
                    int y = (hostViewSize.y - newHeight) / 2;
                    toReturn = new Rect(0, y, hostViewSize.x, y+newHeight);
                }
                break;
        }
        return toReturn;
    }

    private Camera.Size getNearestPreviewSize(Camera.Size size) {
        List<Camera.Size> availableSizes =  mCamera.getParameters().getSupportedPreviewSizes();
        if (availableSizes == null || availableSizes.size() <= 0) return null;

        Camera.Size toReturn = availableSizes.get(0);
        int distance = Math.abs(size.width*size.height - toReturn.width*toReturn.height);
        for (int a=1; a<availableSizes.size(); a++) {
            int temp = Math.abs(size.width*size.height - availableSizes.get(a).width*availableSizes.get(a).height);
            if (temp < distance) {
                distance = temp;
                toReturn = availableSizes.get(a);
            }
        }
        return toReturn;
    }


    public int setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        return result/90;
    }

}

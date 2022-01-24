/*
 * TODO: ISSUES:
 *
 * 1. when in calibration, when the configuration of the page changes then activities gets shut down and restarts again but the calibration is till
 * going on but the activity is not in the calibration phase case is calibration is set to false again.
 *
 * 2. synchronizing the x and y coordinates

 * 3. adding a white blank background for the calibration.
 *
 * 4. saving feature
 *
 * 5. performance: look at the object leakage
 *
 * 6. better coding: change the static variables to object ones
 *
 * 7. performace: make the feature extraction in another thread
 *
 * 8. clean the code for warning: the variables, the extra code from facedetection
 *
 * 9. thread managment: add locks or synchronizations when neccessary
 * */



package com.projectx.eyemusic;

import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.projectx.eyemusic.Features.FeatureExtractor;
import com.projectx.eyemusic.Fragments.CalibrationFragment;
import com.projectx.eyemusic.FaceDetection.CameraXViewModel;
import com.projectx.eyemusic.FaceDetection.FaceDetectorProcessor;
import com.projectx.eyemusic.FaceDetection.PreferenceUtils;
import com.projectx.eyemusic.FaceDetection.VisionImageProcessor;
import com.projectx.eyemusic.Graphics.GraphicOverlay;
import com.projectx.eyemusic.Model.GazeModelManager;
import com.projectx.eyemusic.Model.OriginalModel;

import java.util.ArrayList;
import java.util.List;

public class CalibrationActivity extends BaseActivity {
    private static final String TAG = "CalibrationActivity";
    public SharedPreferences preferences = null; // made this public to access from fragment
    // Views
    TextView tmpInstructionMessage;
    //graphics
    static int[] graphicOverlayGazeLocationLocation = new int[2];
    public static volatile GraphicOverlay graphicOverlayGazeLocation;

    //Thread
    //TODO: replace new GazeModelManager() with the actual model
    private PredictionThread predictionThread;

    // camera and features
    private static final int PERMISSION_REQUESTS = 1;
    private PreviewView previewView;
    private GraphicOverlay graphicOverlayFace;
    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private Preview previewUseCase;
    @Nullable
    private ImageAnalysis analysisUseCase;
    @Nullable
    private VisionImageProcessor imageProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT; //the front camera
    private String selectedModel = "Face Detection";
    private CameraSelector cameraSelector;
    private TextView textViewReport;
    private TextView textViewInstructions;

    //Calibration
    private Button btn_start_calibration;
    private ImageButton btn_calibration_back;
    private GraphicOverlay graphicOverlayCalibration;
    private CalibrationRunnable calibrationRunnable;
    private Thread calibrationThread;
    private boolean isCalibration;
    public boolean setCalibration(boolean value) {
        return value;
    }

    // Original Model
    private OriginalModel gazePredictionModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_calibration);
        {
            tmpInstructionMessage = findViewById(R.id.calibration_instructions_textview);
            tmpInstructionMessage.setVisibility(View.VISIBLE);
        }
        // fetch latest model
        gazePredictionModel = OriginalModel.getInstance();

        graphicOverlayGazeLocation = findViewById(R.id.graphic_overlay_gaze_location);
        if (graphicOverlayGazeLocation == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        //Gaze thread you can uncomment to have prediction also in calibration activity
        /*predictionThread = new PredictionThread(new GazeModelManager(), graphicOverlayGazeLocation, this);
        predictionThread.start();*/



        // Camera and features
        textViewReport = findViewById(R.id.text_view_report);
        textViewInstructions = findViewById(R.id.calibration_instructions_textview);
        textViewReport.setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(
                    getApplicationContext(),
                    "CameraX is only supported on SDK version >=21. Current SDK version is "
                            + Build.VERSION.SDK_INT,
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        previewView = findViewById(R.id.preview_view);

        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        graphicOverlayFace = findViewById(R.id.graphic_overlay_face);
        if (graphicOverlayFace == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        this,
                        provider -> {
                            cameraProvider = provider;
                            if (allPermissionsGranted()) {
                                bindAllCameraUseCases();
                            }
                        });

        isCalibration = false;
        graphicOverlayCalibration = findViewById(R.id.graphic_overlay_calibration);
        calibrationRunnable = new CalibrationRunnable(graphicOverlayCalibration, this, findViewById(R.id.calibration_title_textview),textViewInstructions);

        btn_calibration_back = findViewById(R.id.btn_calibration_back);
        btn_calibration_back.setOnClickListener(view -> {
            if (! GazeModelManager.isIsCalibratedAtAll()) {
                tmpInstructionMessage.setText("You cannot go back until you perform the calibration!");
            }
            else {
                finish();
            }
        });

        btn_start_calibration = findViewById(R.id.btn_start_calibration);
        btn_start_calibration.setOnClickListener (view -> {
            FeatureExtractor.setCalibrationMode(true);
            tmpInstructionMessage.setVisibility(View.INVISIBLE);
            calibrationThread  = new Thread(calibrationRunnable);
            calibrationThread.start();

            // set the calibration fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.calibration_fragment_container,
                    new CalibrationFragment()).commit();

            //change the buttons
            btn_start_calibration.setVisibility(View.INVISIBLE);
            btn_calibration_back.setVisibility(View.INVISIBLE);

            //change the preview opacity
            graphicOverlayFace.setAlpha(0.4f);

            graphicOverlayGazeLocation.clear();
        });

        textViewInstructions.setText(Html.fromHtml(
                "<ul>"+
                "<li>   Multiple dots will be shown on the screen to be gazed at.</li>" +
                "<li>   The dots will appear on different places on the screen</li>" +
                "<li>   You will have enough time to look at them.</li>" +
                "<li>   <b>Please be sure to:</b>" +
                    "<ul>"+
                    "<li>Have a good lighting.</li>"+
                    "<li>Look straight at the dots.</li>" +
                    "<li>Stabilize your head.</li>"+
                    "</ul>"+
                "</li>"+
                "<li> Should you decide to calibrate again, press the camera view on the top menu.</li>" +
                "</ul>"));
        textViewInstructions.setMovementMethod(new ScrollingMovementMethod());

    }

    public void calibrationFinished(){
        Log.d(TAG, "CalibrationRun: ");
        isCalibration = false;
        btn_start_calibration.post( () -> {btn_start_calibration.setVisibility(View.VISIBLE);} );
        btn_calibration_back.post( () -> {btn_calibration_back.setVisibility(View.VISIBLE);});
    }

    public static GraphicOverlay getGraphicOverlayGazeLocation() {
        return graphicOverlayGazeLocation;
    }

    public int[] getGraphicOverlayGazeLocationLocation() {
        return graphicOverlayGazeLocationLocation;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        graphicOverlayGazeLocation.getLocationOnScreen(graphicOverlayGazeLocationLocation);
        Log.i(TAG, "onWindowFocusChanged:Location of overlay " + graphicOverlayGazeLocationLocation[0] + " " + graphicOverlayGazeLocationLocation[1]);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
//        // check if EyeMusic is launched for the first time
        Utilities.isOnboardingFinished();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if(predictionThread != null )
            predictionThread.quit(); // it will destroy all the messages that has not been started yet and are in the message queue

        FeatureExtractor.setCalibrationMode(false);

        if (imageProcessor != null) {
            imageProcessor.stop();}
        Log.d(TAG, "Calibration Activity: closing");
        if (GazeModelManager.isIsCalibratedAtAll()) {
            Log.d(TAG, "Calibration succeeded");
        }
        else {
            Log.d(TAG, "Calibration failed");
        }
        Log.d(TAG, "Calibration Activity: closed");
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBackPressed() {
        if (! GazeModelManager.isIsCalibratedAtAll()) {
            tmpInstructionMessage.setText("You cannot go back until you perform the calibration!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        else {
            super.onBackPressed();
        }
    }

    private boolean isOnBoardingFinished() {
          // TODO: initialization in it's own activity later on
           gazePredictionModel = OriginalModel.getInstance();
            return true;
    }

    //-----------------------------CAMERA AND FEATURES --------------------------------------------

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            //bindPreviewUseCase(); //aisan
            bindAnalysisUseCase();
        }
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }

        try {
            FaceDetectorOptions faceDetectorOptions =
                    new FaceDetectorOptions.Builder()
                            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                            .build();
            imageProcessor = (VisionImageProcessor) new FaceDetectorProcessor(
                    this, faceDetectorOptions);

        }catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + selectedModel, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getLocalizedMessage(),
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        /*TODO: well... in the quick-start the target resolution is gotten from the shared preferences
           is used, so check if there is any errors and if and the taget resolution for the preview
           is gotten from the shared preferences.  -> do the target resolution you want
         */

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }
        analysisUseCase = builder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the
                // detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(this), //uses the main thread/UI thread
                imageProxy -> {
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            graphicOverlayFace.setImageSourceInfo(
                                    imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            graphicOverlayFace.setImageSourceInfo(
                                    imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }
                    try {
                        imageProcessor.processImageProxy(imageProxy, graphicOverlayFace,
                                predictionThread, textViewReport, isCalibration);
                    } catch (MlKitException e) {
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this,
                cameraSelector, analysisUseCase);
    }
    //-----------------------------CAMERA AND FEATURES FINISH --------------------------------------


    //------------------------------- PERMISSIONS -------------------------------------

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
    //--------------------------- PERMISSIONS FINISH ---------------------------------------------------

    //--------------------------- GUI ---------------------------------------------------
    public static void buttoneffect (View button){
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0x69696969, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }

}

// new
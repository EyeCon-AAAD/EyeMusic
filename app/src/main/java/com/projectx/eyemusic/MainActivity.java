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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.projectx.eyemusic.Authentication.Authentication;
import com.projectx.eyemusic.FaceDetection.CameraXViewModel;
import com.projectx.eyemusic.FaceDetection.FaceDetectorProcessor;
import com.projectx.eyemusic.FaceDetection.PreferenceUtils;
import com.projectx.eyemusic.FaceDetection.VisionImageProcessor;
import com.projectx.eyemusic.Fragments.PlaylistFragment;
import com.projectx.eyemusic.Graphics.GraphicOverlay;
import com.projectx.eyemusic.Model.GazeModelManager;
import com.projectx.eyemusic.Model.GazePoint;
import com.projectx.eyemusic.Model.OriginalModel;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.*;

import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    private String CLIENT_ID;
    private String CLIENT_SECRET;
    private String REDIRECT_URI;
    private String SPOTIFY_PACKAGE_NAME;
    private String APP_PACKAGE_NAME;
    private String PLAY_STORE_URI;
    private String REFERRER;
    private static final int SPOTIFY_TOKEN_REQUEST_CODE = 777;
    public static final int SPOTIFY_AUTH_CODE_REQUEST_CODE = 0x11;
    public static SpotifyAppRemote mSpotifyAppRemote;
    public RequestQueue requestQueue; // made this public to access from fragment
    private String mAccessToken;
    private String mAccessCode;

    public SharedPreferences preferences = null; // made this public to access from fragment

    // Views
    Button btn_play, btn_pause;
    TextView tv_message;
    TextView tv_artist, tv_auth_token;
    RecyclerView rv_main_playlists;
    public ProgressBar pb_main;
    boolean flag = false;
    PackageManager packageManager;


    public Authentication authentication; // made this public to access from fragment

    //graphics
    static int[] graphicOverlayGazeLocationLocation = new int[2];
    public static volatile GraphicOverlay graphicOverlayGazeLocation;

    //Thread
    //TODO: replace new GazeModelManager() with the actual model
    private GazeModelManager mainGazeModelManager;
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

    //Calibration
    private Button btn_calibration;
    private GraphicOverlay graphicOverlayCalibration;
    private CalibrationRunnable calibrationRunnable;
    private Thread calibrationThread;
    private boolean isCalibration;

    // Original Model
    private OriginalModel gazePredictionModel = null;

    public static boolean playerAccessable;
    public static Fragment playerFragment;
    public static Fragment currentFragment;

    static ImageButton btn_main_show_player;
    static ImageButton btn_main_back;

    static GazePoint[] locations_menu_buttons = new GazePoint[2];
    static ImageButton[] references_menu_button = new ImageButton[2];
    private int backcounter;

    // face feedback message
    public static TextView faceFeedback;

    public static GazePoint[] getLocationsMenuButtons() {
        return locations_menu_buttons;
    }

    public static ImageButton[] getReferencesMenuButtons() {
        return references_menu_button;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        {
            CLIENT_ID = getString(R.string.CLIENT_ID);
            CLIENT_SECRET = getString(R.string.CLIENT_SECRET);
            REDIRECT_URI = getString(R.string.REDIRECT_URI);
            SPOTIFY_PACKAGE_NAME = getString(R.string.SPOTIFY_PACKAGE_NAME);
            APP_PACKAGE_NAME = getString(R.string.APP_PACKAGE_NAME);
            PLAY_STORE_URI = getString(R.string.PLAY_STORE_URI);
            REFERRER = getString(R.string.REFERRER);
        }
        pb_main = findViewById(R.id.pb_load_main);
        rv_main_playlists = findViewById(R.id.rv_playlists);


        packageManager = getPackageManager();

        // use sharedPreferences to determine first time launch
        preferences = getSharedPreferences(APP_PACKAGE_NAME, MODE_PRIVATE);

        // create  singleton request queue
        requestQueue = Volley.newRequestQueue(MainActivity.this);

        // create Authentication Object
        authentication = new Authentication(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, preferences, requestQueue, this);

        // fetch latest model
        gazePredictionModel = OriginalModel.getInstance();


        // --------------------------------------------------------------------------------------------------------------------------

        //just for testing
        /*Activity mActivity = this;
        findViewById(R.id.btn_main_stimulateTouch).setOnClickListener(view -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SimulatedTouch.click(mActivity, 500, 500);
                        //SimulatedTouch.swap(100 ,100,1000 ,500, 5);
                    } catch (Exception e) {
                        Log.e(TAG, "When pressed simulatedTouch btn: ", e);
                    }
                }
            }).start();

        });*/

        graphicOverlayGazeLocation = findViewById(R.id.graphic_overlay_gaze_location);
        if (graphicOverlayGazeLocation == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        //Gaze thread

        mainGazeModelManager = new GazeModelManager();
        GazeModelManager.initializeGazeModelManager(getApplicationContext());

        predictionThread = new PredictionThread(mainGazeModelManager, graphicOverlayGazeLocation, this);
        predictionThread.start();

        // Camera and features
        textViewReport = findViewById(R.id.text_view_report);
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

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

        //Calibration
        btn_main_back = findViewById(R.id.btn_main_back);

//        btn_main_back.setVisibility(View.INVISIBLE);
//        btn_main_reconnect_spotify = findViewById(R.id.btn_main_reconnect_spotify);

        isCalibration = false;
        graphicOverlayCalibration = findViewById(R.id.graphic_overlay_calibration);
//        calibrationRunnable = new CalibrationRunnable(graphicOverlayCalibration, this);
        btn_calibration = findViewById(R.id.btn_main_calibration);
        btn_calibration.setOnClickListener (view -> {

//            isCalibration = true;
            resetBackCounter();
            Intent openCalibrationIntent = new Intent(this, CalibrationActivity.class);
            startActivity(openCalibrationIntent);


//            FeatureExtractor.setCalibrationMode(isCalibration);
//            calibrationThread  = new Thread(calibrationRunnable);
//            calibrationThread.start();
//
//            // set the calibration fragment
//            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
//                    new CalibrationFragment()).commit();
//
//            //change the buttons
//            //btn_calibration.setEnabled(false);
//            btn_calibration.setVisibility(View.INVISIBLE);
//            //btn_main_back.setVisibility(View.INVISIBLE);
//            btn_main_reconnect_spotify.setVisibility(View.INVISIBLE);
//
//            //change the preview opacity
//            //previewView.setVisibility(View.INVISIBLE);
//            //graphicOverlayFace.setAlpha(0.4f);


        });
        buttoneffect(btn_calibration);

        btn_main_show_player = findViewById(R.id.btn_main_show_player);
        btn_main_show_player.setOnClickListener(view -> {
            if (playerFragment!=null){
                resetBackCounter();
                if (!playerFragment.isHidden()) {

                getSupportFragmentManager().beginTransaction()
                        .show(currentFragment).hide(playerFragment).commit();
                }
                else{
                    getSupportFragmentManager().beginTransaction()
                            .show(playerFragment).hide(currentFragment).commit();
                }
            }
            else
                Toast.makeText(getApplicationContext(), "No song has been played yet.", Toast.LENGTH_SHORT).show();
        });
        buttoneffect(btn_main_show_player);

        backcounter = 0;
        btn_main_back.setOnClickListener(view ->{
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("Tracks Fragment");
            if (playerFragment != null && !playerFragment.isHidden()){
                btn_main_show_player.post(() -> btn_main_show_player.performClick());
            }
            else if (fragment != null && fragment.isVisible()){
                Fragment playlistFragment = new PlaylistFragment();
                getSupportFragmentManager().beginTransaction().remove(fragment)
                        .add(R.id.main_fragment_container, playlistFragment, "Playlist Fragment")
                        .commit();
                currentFragment = playlistFragment;
            }
            else {
                backcounter++;
                if (backcounter == 2) {
                    mSpotifyAppRemote.getPlayerApi().pause();
                    finish();
                    System.exit(0);
                }
                else
                    Toast.makeText(getApplicationContext(), "One more time to exit!", Toast.LENGTH_SHORT).show();
            }
        });
        buttoneffect(btn_main_back);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                btn_main_back.post(() -> btn_main_back.performClick());
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        //face feedback
        faceFeedback = findViewById(R.id.face_feedback);
    }

    public void resetBackCounter(){
        backcounter = 0;
    }

    public void calibrationFinished(){
        Log.d(TAG, "CalibrationRun: ");
        isCalibration = false;
//        for (Feature1 feature : features){
//            Log.d(TAG, "CalibrationRun: RESULTS-> " + feature);
//        }


//        btn_calibration.post( () -> {btn_calibration.setVisibility(View.VISIBLE);} );
        //btn_main_back.post( () -> {btn_main_back.setVisibility(View.VISIBLE);} );
//        btn_main_reconnect_spotify.post( () -> {btn_main_reconnect_spotify.setVisibility(View.VISIBLE);} );


        // start the playlist fragment
        currentFragment = new PlaylistFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                currentFragment, "Playlist Fragment").commit();
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

        int[] point = new int[2];
        btn_main_back.getLocationOnScreen(point);
        int width = btn_main_back.getWidth();
        int height = btn_main_back.getHeight();
        locations_menu_buttons[0] = new GazePoint(point[0]+((float)width/2), point[1]+((float)height/2));
        references_menu_button[0] = btn_main_back;

        btn_main_show_player.getLocationOnScreen(point);
        width = btn_main_back.getWidth();
        height = btn_main_back.getHeight();
        locations_menu_buttons[1] = new GazePoint(point[0]+((float)width/2), point[1]+((float)height/2));
        references_menu_button[1] = btn_main_show_player;

        Log.d("location_back", "onWindowFocusChanged: location of back button "+ locations_menu_buttons[0].toString());
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
        // error occurred
        if (response.getError() != null && response.getError().isEmpty()) {
            Log.e(TAG, response.getError());
        }
        if (requestCode == SPOTIFY_TOKEN_REQUEST_CODE) {
            mAccessToken = response.getAccessToken();
            if (mAccessToken != null) {
                // store the access token
                authentication.storeToken(mAccessToken);
                Utilities.showProgressBar(pb_main, rv_main_playlists, true);
            }
        } else if (requestCode == SPOTIFY_AUTH_CODE_REQUEST_CODE) {
            mAccessCode = response.getCode();

            if (mAccessCode != null) {
                // store Access code
                authentication.storeAccessCode(mAccessCode);
                // fetch access token and refresh token and store them
                authentication.fetchTokens();
                // call on start after successfully authenticating --> only happens on first launch
                onStart();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

        if (! GazeModelManager.isIsCalibratedAtAll()) {
            Intent openCalibrationIntent = new Intent(this, CalibrationActivity.class);
            startActivity(openCalibrationIntent);
        }else{
            // check if EyeMusic is launched for the first time
            if (Utilities.isOnboardingFinished()) {
                // order matters
                mAccessCode = authentication.getAccessCode();
                mAccessToken = authentication.getAccessToken();

                // ------------------- perform error check for when the access is denied but launch is not first time ----------
            }
            // Check if Spotify is installed each time the app is launched. Requirement!
            if (!Utilities.isSpotifyInstalled()) {
                Utilities.directUserToPlayStore(MainActivity.this);
            } else {
                if (authentication.isAuthenticated()) {
                    ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                            .setRedirectUri(REDIRECT_URI)
                            .showAuthView(true)
                            .build();
                    if (mSpotifyAppRemote == null) {
                        connectSpotifyRemote(connectionParams);
                    } else if (!mSpotifyAppRemote.isConnected()) {
                        connectSpotifyRemote(connectionParams);
                    }
                }
            }
        }



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        // disconnect from AppRemote
        // mSpotifyAppRemote.getPlayerApi().pause();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        predictionThread.quit(); // it will destroy all the messages that has not been started yet and are in the message queue
        if (imageProcessor != null) {
            imageProcessor.stop();}
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
        predictionThread = new PredictionThread(mainGazeModelManager, graphicOverlayGazeLocation, this);
        predictionThread.start();
    }

    //------------------------------------ SPOTIFY -------------------------------------------------
    private void connectSpotifyRemote(ConnectionParams connectionParams) {
        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                // connection was successful
                Toast.makeText(getApplicationContext(), "Successfully Connected", Toast.LENGTH_SHORT).show();
                // refresh access token before fetching playlists if token has expired
                authentication.refreshAccessToken();
                // load playlist fragment
                // changed PlaylistFragment to Calibrate\ionFragment
                Fragment playlistFragment = new PlaylistFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                        playlistFragment, "Playlist Fragment").commit();
                currentFragment = playlistFragment;
                // fetchPlaylists(requestQueue, mSpotifyAppRemote);
            }

            @Override
            public void onFailure(Throwable throwable) {
                // handle connection error here
                Toast.makeText(getApplicationContext(), "Couldn't connect", Toast.LENGTH_LONG).show();
                Log.e(TAG, throwable.getMessage(), throwable);
            }
        });
    }

    private boolean isOnBoardingFinished() {
        if (preferences.getBoolean("firstTime", true)) {
            // Do authentication once
            authentication.authenticate(MainActivity.this, SPOTIFY_AUTH_CODE_REQUEST_CODE);

            // Do initial download of the model.
            // TODO: initialization in it's own activity later on
            gazePredictionModel = OriginalModel.getInstance();

            // set first time to false
            preferences.edit().putBoolean("firstTime", false).apply();
            return true;
        } else {
            return false;
        }
    }

    //-----------------------------CAMERA AND FEATURES --------------------------------------------

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            //bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void bindPreviewUseCase(){
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();

        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }

        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this,
                cameraSelector, previewUseCase);
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


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int X = (int) event.getX();
//        int Y = (int) event.getY();
//        int eventAction = event.getAction();
//        switch (eventAction) {
//            case MotionEvent.ACTION_DOWN:
//                Toast.makeText(this, "ACTION_DOWN " + "X: " + X + " Y: " + Y, Toast.LENGTH_SHORT).show();
//                Log.i(TAG, "action_down: " + X + " " + Y);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                Toast.makeText(this, "MOVE " + "X: " + X + " Y: " + Y,
//                        Toast.LENGTH_SHORT).show();
//                Log.i(TAG, "action_move: " + X + " " + Y);
//                break;
//            case MotionEvent.ACTION_UP:
//                Toast.makeText(this, "ACTION_UP " + "X: " + X + " Y: " + Y, Toast.LENGTH_SHORT).show();
//                Log.i(TAG, "action_up: " + X + " " + Y);
//                break;
//        }
//        return false;
//    }



    //--------------------------- GUI ---------------------------------------------------
    public static void  buttoneffect (View button){
        button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (v.getBackground() != null) {
                            v.getBackground().setColorFilter(0x99999999, PorterDuff.Mode.SRC_ATOP);
                        }
                        else {
                            ((ImageView) v).setColorFilter(0x66666666, PorterDuff.Mode.SRC_ATOP);
                        }
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if (v.getBackground() != null) {
                            v.getBackground().clearColorFilter();
                        }
                        else {
                            ((ImageView) v).clearColorFilter();
                        }
                        v.invalidate();
                        break;

                    }
                }
                return false;
            }
        });
    }

    //---------------------------- Face Feedback ---------------------------------------------------
    static public void updateFaceFeedback(Boolean faceInBounds){
        if(faceInBounds){
            faceFeedback.setText("face: ok");
            faceFeedback.setTextColor(Color.GREEN);
            //faceFeedback.setBackgroundColor(Color.GREEN);
        }else{
            faceFeedback.setText("face: not ok\nBe in front of the camera");
            faceFeedback.setTextColor(Color.RED);
            //faceFeedback.setBackgroundColor(Color.RED);
        }
    }


}

//new

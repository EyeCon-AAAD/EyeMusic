package com.projectx.eyemusic.Fragments.OnBoarding;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.OnBoardingActivity;
import com.projectx.eyemusic.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PermissionsRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PermissionsRequestFragment extends Fragment {
    Button btnRequestPermissions;
    private static final String TAG = "PermissionsRequest";
    private static final int PERMISSION_REQUESTS = 1;
    Context context;
    ActivityResultLauncher<Intent> mIntent;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PermissionsRequestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PermissionsRequestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PermissionsRequestFragment newInstance(String param1, String param2) {
        PermissionsRequestFragment fragment = new PermissionsRequestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_permissions_request, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnRequestPermissions = view.findViewById(R.id.btn_fragment_permission_request);
        context = getContext();

        // settings Activity Result Callback
        mIntent = registerForActivityResult(new StartActivityForResult(),
                result -> startMainActivity());

        MultiplePermissionsListener listener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if(multiplePermissionsReport.areAllPermissionsGranted()) {
                    startMainActivity();
                } else if(multiplePermissionsReport.isAnyPermissionPermanentlyDenied()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Camera & Storage Permissions")
                            .setMessage("Camera and Storage permissions are needed for calibration and gaze prediction")
                            .setPositiveButton("Go to SETTINGS", (dialogInterface, i) -> {
                                showSettings();
                            })
                            .setNegativeButton("No Thanks", ((dialogInterface, i) -> {
                                // proceed without granting permissions
                                startMainActivity();
                            }))
                            .setIcon(R.mipmap.ic_launcher_eye_music)
                            .show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Camera & Storage Permissions")
                            .setMessage("Camera and Storage permissions are needed for calibration and gaze prediction")
                            .setPositiveButton("Request Again", (dialogInterface, i) -> {

                            })
                            .setNegativeButton("No Thanks", ((dialogInterface, i) -> {
                                // proceed without granting permissions
                                startMainActivity();
                            }))
                            .setIcon(R.mipmap.ic_launcher_eye_music)
                            .show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                // User previously denied the permission, request them again
                permissionToken.continuePermissionRequest();
            }
        };
        btnRequestPermissions.setOnClickListener(v -> {
            // finish onBoarding
            onBoardingFinished();

            Dexter.withContext(getContext())
                    .withPermissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.INTERNET
                    ).withListener(listener)
                    .onSameThread()
                    .check();
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void onBoardingFinished(){
        String appPackageName = getString(R.string.APP_PACKAGE_NAME);
        SharedPreferences preferences = requireActivity().getSharedPreferences(appPackageName,
                Context.MODE_PRIVATE);
        preferences.edit().putBoolean("finished", true).apply();
    }

    public void showSettings(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        mIntent.launch(intent);
    }

}

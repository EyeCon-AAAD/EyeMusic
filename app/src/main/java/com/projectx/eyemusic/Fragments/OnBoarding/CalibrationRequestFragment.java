package com.projectx.eyemusic.Fragments.OnBoarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.projectx.eyemusic.CalibrationActivity;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalibrationRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalibrationRequestFragment extends Fragment {

    // fragment views
    Button btn_calibrate;

    public CalibrationRequestFragment() {
        // Required empty public constructor
    }

    public static CalibrationRequestFragment newInstance() {
        return new CalibrationRequestFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calibration_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_calibrate = view.findViewById(R.id.btn_fragment_calibrate);

        // go to calibration activity
        btn_calibrate.setOnClickListener(view1 -> {
            // finish onBoarding
            onBoardingFinished();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }
    private void onBoardingFinished(){
        String appPackageName = getString(R.string.APP_PACKAGE_NAME);
        SharedPreferences preferences = requireActivity().getSharedPreferences(appPackageName,
                Context.MODE_PRIVATE);
        preferences.edit().putBoolean("finished", true).apply();
    }
}
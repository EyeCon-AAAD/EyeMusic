package com.projectx.eyemusic.Fragments.OnBoarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.R;
import com.projectx.eyemusic.Utilities;

/**
 * A simple {@link Fragment} subclass.
 */
public class SplashFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // wait a few seconds after showing the splash screen
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Utilities.isOnboardingFinished()){
                    // start mainActivity
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }else{
                    // navigate to the viewpager fragment
                    NavHostFragment.findNavController(SplashFragment.this)
                            .navigate(R.id.action_splashFragment_to_viewPagerFragment);
                }
            }
        }, 3000);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

}
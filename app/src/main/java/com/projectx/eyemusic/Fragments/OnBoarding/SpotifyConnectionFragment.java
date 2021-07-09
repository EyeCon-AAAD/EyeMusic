package com.projectx.eyemusic.Fragments.OnBoarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.projectx.eyemusic.Authentication.Authentication;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.OnBoardingActivity;
import com.projectx.eyemusic.R;
import com.projectx.eyemusic.Utilities;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationResponse;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SpotifyConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpotifyConnectionFragment extends Fragment {
    Button btn_connect;
    public static ViewPager2 viewPager2;

    private OnBoardingActivity onBoardingActivity;

    private static final int SPOTIFY_AUTH_CODE_REQUEST_CODE = 0x11;
    private static final String TAG = "SpotifyFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SpotifyConnectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SpotifyConnectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpotifyConnectionFragment newInstance(String param1, String param2) {
        SpotifyConnectionFragment fragment = new SpotifyConnectionFragment();
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
        return inflater.inflate(R.layout.fragment_spotify_connection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_connect = view.findViewById(R.id.btn_fragment_spotify_connect);
        viewPager2 = getActivity().findViewById(R.id.view_pager);
        onBoardingActivity = (OnBoardingActivity) getActivity();

        // define strings for authentication
        String CLIENT_ID;
        String CLIENT_SECRET;
        String REDIRECT_URI;
        String APP_PACKAGE_NAME;
        {
            CLIENT_ID = getString(R.string.CLIENT_ID);
            CLIENT_SECRET = getString(R.string.CLIENT_SECRET);
            REDIRECT_URI = getString(R.string.REDIRECT_URI);
            APP_PACKAGE_NAME = getString(R.string.APP_PACKAGE_NAME);
        }

        // create  singleton request queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        // shared preferences
        SharedPreferences preferences = getActivity().getSharedPreferences(APP_PACKAGE_NAME, Context.MODE_PRIVATE);

        // create Authentication object
        onBoardingActivity.authentication = new Authentication(CLIENT_ID, CLIENT_SECRET,
                REDIRECT_URI, preferences, requestQueue, getActivity());


        btn_connect.setOnClickListener(view1 -> {
            // navigate to the Permissions fragment(through view pager) for now
            if(!Utilities.isSpotifyInstalled()){
                Utilities.directUserToPlayStore(getActivity());
            } else {
                // authenticate the user
                onBoardingActivity.authentication.authenticate(getActivity(), SPOTIFY_AUTH_CODE_REQUEST_CODE);
            }
        });
    }
}
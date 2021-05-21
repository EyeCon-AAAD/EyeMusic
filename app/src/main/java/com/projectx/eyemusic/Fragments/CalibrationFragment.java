package com.projectx.eyemusic.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.projectx.eyemusic.Fragments.PlaylistFragment;
import androidx.appcompat.app.AppCompatActivity;

import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalibrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalibrationFragment extends Fragment {


    // fragment views
    Button btn_next_fragment;

    public CalibrationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalibrationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalibrationFragment newInstance(String param1, String param2) {
        return new CalibrationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calibration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_next_fragment = view.findViewById(R.id.btn_fragment_calibrate);

        // go to next fragment
        btn_next_fragment.setOnClickListener(view1 -> {
            AppCompatActivity activity = (MainActivity) view1.getContext();
            Fragment playlistFragment = PlaylistFragment.newInstance();
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, playlistFragment, "Playlist Fragment")
                    .addToBackStack(null)
                    .commit();
        });
    }
}
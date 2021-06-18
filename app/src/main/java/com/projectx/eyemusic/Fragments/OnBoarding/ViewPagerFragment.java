package com.projectx.eyemusic.Fragments.OnBoarding;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.projectx.eyemusic.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewPagerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);

        ArrayList<Fragment> fragmentScreens = new ArrayList<>();
        fragmentScreens.add(0, new SpotifyConnectionFragment());
        fragmentScreens.add(1, new PermissionsRequestFragment());

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(
                fragmentScreens,
                requireActivity().getSupportFragmentManager(),
                getLifecycle());

        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setUserInputEnabled(false); // disable scrolling

        return view;
    }
}
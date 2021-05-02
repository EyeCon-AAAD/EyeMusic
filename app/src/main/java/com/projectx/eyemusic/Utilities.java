package com.projectx.eyemusic;

import android.content.res.Resources;
import android.view.View;
import android.widget.ProgressBar;

public class Utilities {
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static void showProgressBar(ProgressBar progressBar, View view, Boolean show){
        if (show) {
            view.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}

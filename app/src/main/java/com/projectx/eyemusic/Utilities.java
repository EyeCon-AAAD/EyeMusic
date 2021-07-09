package com.projectx.eyemusic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Utilities {
    private static final String SPOTIFY_PACKAGE_NAME = App.getContext()
            .getString(R.string.SPOTIFY_PACKAGE_NAME);

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
    public static boolean isSpotifyInstalled(){
        PackageManager packageManager = App.getContext().getPackageManager();

        try {
            packageManager.getPackageInfo(SPOTIFY_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void directUserToPlayStore(Context context){
        String REFERRER = App.getContext().getString(R.string.REFERRER);
        String PLAY_STORE_URI = App.getContext().getString(R.string.PLAY_STORE_URI);
        // Send user to Play Store to install if available in current market
        // TO-DO: If Spotify is not in user's market --> potentially can't make use of our app
        Log.w("Utilities", "Spotify isn't installed! Going to play store");
        // Alert Dialog for good UX
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Please install Spotify from Play Store then launch app again.")
                .setCancelable(true) // may change this
                .setPositiveButton("Install", (dialogInterface, i) -> {
                    try {
                        Uri uri = Uri.parse("market://details")
                                .buildUpon()
                                .appendQueryParameter("id", SPOTIFY_PACKAGE_NAME)
                                .appendQueryParameter("referrer", REFERRER)
                                .build();
                        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (android.content.ActivityNotFoundException ignored) {
                        Uri uri = Uri.parse(PLAY_STORE_URI)
                                .buildUpon()
                                .appendQueryParameter("id", SPOTIFY_PACKAGE_NAME)
                                .appendQueryParameter("referrer", REFERRER)
                                .build();
                        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                .create().show();
    }

    public static boolean isOnboardingFinished(){
        String appPackageName = App.getContext().getString(R.string.APP_PACKAGE_NAME);
        SharedPreferences preferences = App.getContext().getSharedPreferences(appPackageName,
                Context.MODE_PRIVATE);
        return preferences.getBoolean("finished", false);
    }

    private static boolean isConnectedToNetwork(){

        ConnectivityManager cm = (ConnectivityManager)
                App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    private static boolean isConnectedToInternet() throws InterruptedException {
        // ping random server to see if we are able to send and receive packets
        final boolean[] pinged = {false};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress address = InetAddress.getByName("google.com");
                    pinged[0] = !address.equals("");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
        return pinged[0];
    }

    public static boolean isConnected(){
        try {
            return isConnectedToNetwork() && isConnectedToInternet();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}

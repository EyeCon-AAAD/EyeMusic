package com.projectx.eyemusic;


import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GazeRunnable extends AppCompatActivity implements Runnable {
    private static final String TAG = "GazeRunnable";
    Button btn = findViewById(R.id.btn_main_startGazeCaptureThread);

    @Override
    public void run() {
        int i = 0;
        while (true){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "run: " + i++);
            if (i == 10){
                btn.setText("aisan");
            }

        }
    }

}

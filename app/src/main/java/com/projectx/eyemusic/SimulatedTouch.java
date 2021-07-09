package com.projectx.eyemusic;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

public class SimulatedTouch {
    private static final String TAG = "SimulatedTouch";

    public static void click(Activity activity, float x, float y){
        Log.i(TAG, "simulateClick:click on activity " + x + " " + y);
        long downTime = SystemClock.uptimeMillis();
        final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,MotionEvent.ACTION_DOWN, x, y, 0);
        downTime += 1000;
        final MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,MotionEvent.ACTION_UP, x, y, 0);
        activity.dispatchTouchEvent(downEvent);
        activity.dispatchTouchEvent(upEvent);
        downEvent.recycle();
        upEvent.recycle();
    }

    /**
     *
     * @param x horizontal coordinate in the display
     * @param y vertical coordinate in the display
     */
    public static void click(float x, float y) {
        Log.i(TAG, "simulateClick:click on device " + x + " " + y);
        Instrumentation inst = new Instrumentation();
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        MotionEvent event = MotionEvent.obtain(
                downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, 0
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        }
        inst.sendPointerSync(event);

        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(
                downTime, eventTime + new Long(10),
                MotionEvent.ACTION_UP, x, y, 0
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        }
        inst.sendPointerSync(event);

    }

    public static void swap(float fromX, float toX, float fromY, float toY, int stepCount){
        Log.i(TAG, "simulateClick:swap on device ");
        Instrumentation inst = new Instrumentation();
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        float y = fromY;
        float x = fromX;

        float yStep = (toY - fromY) / stepCount;
        float xStep = (toX - fromX) / stepCount;

        MotionEvent event = MotionEvent.obtain(
                downTime, eventTime,
                MotionEvent.ACTION_DOWN, fromX, fromY, 0
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        }
        inst.sendPointerSync(event);



        for (int i=0; i < stepCount; i++) {
            y += yStep;
            x += xStep;
            eventTime = SystemClock.uptimeMillis();
            event = MotionEvent.obtain(
                    downTime, eventTime + new Long(stepCount),
                    MotionEvent.ACTION_MOVE, x, y, 0
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            }
            inst.sendPointerSync(event);
        }

        eventTime = SystemClock.uptimeMillis() + new Long(stepCount) + 2;
        event = MotionEvent.obtain(
                downTime, eventTime,
                MotionEvent.ACTION_UP, toX, toY, 0
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        }
        inst.sendPointerSync(event);
    }
}

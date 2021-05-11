package com.projectx.eyemusic.graphics;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.graphics.GraphicOverlay;
import com.projectx.eyemusic.graphics.GraphicOverlay.Graphic;

public class DotGraphic extends Graphic{
    private static final String TAG = "DotGraphic";
    private  float radius = 20.0f; //default values
    private  int color = Color.RED;

    private Paint paint;
    private float x;
    private float y;

    public DotGraphic(MainActivity activity, GraphicOverlay overlay, float x, float y){
        super(overlay);
        paint = new Paint();
        paint.setColor(color);
        this.x = x;
        this.y = y - activity.getGraphicOverlayGazeLocationLocation()[1];
    }

    public void setColor(int color){
        this.color = color;
        paint.setColor(color);
    }

    public void setRadius(float r){
        this.radius = r;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radius, paint);
    }


}

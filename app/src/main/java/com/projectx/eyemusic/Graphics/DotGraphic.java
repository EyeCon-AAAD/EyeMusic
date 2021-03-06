package com.projectx.eyemusic.Graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.projectx.eyemusic.BaseActivity;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.Graphics.GraphicOverlay.Graphic;

public class DotGraphic extends Graphic{
    private static final String TAG = "DotGraphic";
    private  float radius = 40.0f; //default values
    private  int color = Color.RED;

    private Paint paint;
    private float x;
    private float y;

    public DotGraphic(BaseActivity activity, GraphicOverlay overlay, float x, float y){
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

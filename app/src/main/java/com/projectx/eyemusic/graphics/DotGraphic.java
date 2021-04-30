package com.projectx.eyemusic.graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.projectx.eyemusic.graphics.GraphicOverlay;
import com.projectx.eyemusic.graphics.GraphicOverlay.Graphic;

public class DotGraphic extends Graphic{
    private static final float DOT_RADIUS = 8.0f;
    private static final int COLOR = Color.CYAN;

    private Paint paint;
    private float x;
    private float y;

    DotGraphic(GraphicOverlay overlay, float x, float y){
        super(overlay);

        paint = new Paint();
        paint.setColor(COLOR);

        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, DOT_RADIUS, paint);
    }


}

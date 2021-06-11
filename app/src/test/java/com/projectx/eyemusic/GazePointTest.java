package com.projectx.eyemusic;


import com.projectx.eyemusic.Model.GazePoint;

import org.junit.Assert;
import org.junit.Test;

public class GazePointTest {

    @Test
    public void test1() {
        float x = 1;
        float y = 3;

        GazePoint gp = new GazePoint(x, y);

        Assert.assertEquals(1, gp.getX(), 0.01);
        Assert.assertEquals(3, gp.getY(), 0.01);
    }
}

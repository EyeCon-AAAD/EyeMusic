package com.projectx.eyemusic;


import org.junit.Assert;
import org.junit.Test;

public class GazePointTest {

    @Test
    public void test1() {
        float x = 1;
        float y = 3;

        GazePoint gp = new GazePoint(x, y);

        Assert.assertEquals(1, gp.getX());
        Assert.assertEquals(3, gp.getY());
    }
}

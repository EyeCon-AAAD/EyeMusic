package com.projectx.eyemusic;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CalibrationErrorTest {

    @Test
    // Testing empty constructor
    public void emptyConstructorTest() {

        CalibrationError ce = new CalibrationError();

        Assert.assertEquals(-1, ce.getX_error());
        Assert.assertEquals(-1, ce.getY_error());
        Assert.assertEquals(-1, ce.getXY_error());
        Assert.assertEquals(-1, ce.getXY_error());
    }

    @Test
    // When one of the lists is not initialized
    public void constructorTest1() {

        List<GazePoint> gazePointsOriginal = null;
        List<GazePoint> gazePointsCalibration = null;

        CalibrationError ce = new CalibrationError(gazePointsOriginal, gazePointsCalibration);
    }


    @Test
    // When both lists are initialized but empty.
    public void constructorTest2() {

        List<GazePoint> gazePointsOriginal = new ArrayList<>();
        List<GazePoint> gazePointsCalibration = new ArrayList<>();

        CalibrationError ce = new CalibrationError(gazePointsOriginal, gazePointsCalibration);
    }

    @Test
    // testing two identical lists
    public void constructorTest3() {

        List<GazePoint> gazePointsOriginal = new ArrayList<>();

        gazePointsOriginal.add(new GazePoint(0, 0));
        gazePointsOriginal.add(new GazePoint(1, 1));
        gazePointsOriginal.add(new GazePoint(2, 2));

        List<GazePoint> gazePointsCalibration = new ArrayList<>();
        gazePointsCalibration.add(new GazePoint(0, 0));
        gazePointsCalibration.add(new GazePoint(1, 1));
        gazePointsCalibration.add(new GazePoint(2, 2));

        CalibrationError ce = new CalibrationError(gazePointsOriginal, gazePointsCalibration);

        Assert.assertEquals(3, ce.getSample_size());
        Assert.assertEquals(0, ce.getX_error());
        Assert.assertEquals(0, ce.getY_error());
        Assert.assertEquals(0, ce.getXY_error());
    }

    @Test
    // testing two identical lists (here we test x_error and y_error)
    public void constructorTest4() {

        List<GazePoint> gazePointsOriginal = new ArrayList<>();

        gazePointsOriginal.add(new GazePoint(1, 1));
        gazePointsOriginal.add(new GazePoint(1, 1));
        gazePointsOriginal.add(new GazePoint(1, 1));

        List<GazePoint> gazePointsCalibration = new ArrayList<>();
        gazePointsCalibration.add(new GazePoint(4, 5));
        gazePointsCalibration.add(new GazePoint(1, 1));
        gazePointsCalibration.add(new GazePoint(5, 4));

        CalibrationError ce = new CalibrationError(gazePointsOriginal, gazePointsCalibration);

        Assert.assertEquals(3, ce.getSample_size());
        Assert.assertEquals(5.0, ce.getX_error());
        Assert.assertEquals(5.0, ce.getY_error());
//        Assert.assertEquals(0, ce.getXY_error());
    }

    @Test
    // testing two identical lists (here we test xyerror)
    public void constructorTest5() {

        List<GazePoint> gazePointsOriginal = new ArrayList<>();

        gazePointsOriginal.add(new GazePoint(1, 1));
        gazePointsOriginal.add(new GazePoint(1, 1));
        gazePointsOriginal.add(new GazePoint(1, 1));

        List<GazePoint> gazePointsCalibration = new ArrayList<>();
        gazePointsCalibration.add(new GazePoint(5, 5));
        gazePointsCalibration.add(new GazePoint(1, 1));
        gazePointsCalibration.add(new GazePoint(5, 5));

        CalibrationError ce = new CalibrationError(gazePointsOriginal, gazePointsCalibration);

        Assert.assertEquals(3, ce.getSample_size());
        Assert.assertEquals(8, ce.getXY_error());
    }


    @Test
    // testing two identical lists (one including negative numbers)
    public void constructorTest6() {

        List<GazePoint> gazePointsOriginal = new ArrayList<>();

        gazePointsOriginal.add(new GazePoint(-1, -1));
        gazePointsOriginal.add(new GazePoint(1, 1));
        gazePointsOriginal.add(new GazePoint(1, 1));

        List<GazePoint> gazePointsCalibration = new ArrayList<>();
        gazePointsCalibration.add(new GazePoint(2, 3));
        gazePointsCalibration.add(new GazePoint(1, 1));
        gazePointsCalibration.add(new GazePoint(5, 4));

        CalibrationError ce = new CalibrationError(gazePointsOriginal, gazePointsCalibration);

        Assert.assertEquals(3, ce.getSample_size());
        Assert.assertEquals(5.0, ce.getX_error());
        Assert.assertEquals(5.0, ce.getY_error());
//        Assert.assertEquals(0, ce.getXY_error());
    }


    @Test
    // testing when original list is smaller in size
    public void constructorTest7() {

        List<GazePoint> gazePointsOriginal = new ArrayList<>();

        gazePointsOriginal.add(new GazePoint(1, 1));
        gazePointsOriginal.add(new GazePoint(1, 1));

        List<GazePoint> gazePointsCalibration = new ArrayList<>();
        gazePointsCalibration.add(new GazePoint(4, 5));
        gazePointsCalibration.add(new GazePoint(1, 1));
        gazePointsCalibration.add(new GazePoint(5, 4));

        CalibrationError ce = new CalibrationError(gazePointsOriginal, gazePointsCalibration);

        Assert.assertEquals(2, ce.getSample_size());
        Assert.assertEquals(3.0, ce.getX_error());
        Assert.assertEquals(4.0, ce.getY_error());
        Assert.assertEquals(5.0, ce.getXY_error());
    }

    @Test
    // testing when calibration list is smaller in size
    // out of bounds needs handling
    public void constructorTest8() {

        List<GazePoint> gazePointsOriginal = new ArrayList<>();

        gazePointsOriginal.add(new GazePoint(1, 1));
        gazePointsOriginal.add(new GazePoint(1, 1));
        gazePointsOriginal.add(new GazePoint(1, 1));

        List<GazePoint> gazePointsCalibration = new ArrayList<>();
        gazePointsCalibration.add(new GazePoint(4, 5));
        gazePointsCalibration.add(new GazePoint(1, 1));

        CalibrationError ce = new CalibrationError(gazePointsOriginal, gazePointsCalibration);

        Assert.assertEquals(-1, ce.getSample_size());
        Assert.assertEquals(-1, ce.getX_error());
        Assert.assertEquals(-1, ce.getY_error());
        Assert.assertEquals(-1, ce.getXY_error());
    }

}

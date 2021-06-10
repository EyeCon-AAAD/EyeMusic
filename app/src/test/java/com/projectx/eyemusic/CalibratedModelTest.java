package com.projectx.eyemusic;



import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CalibratedModelTest {

    @Test
    // Testing the empty constructor
    public void emptyConstructorTest() {

        CalibratedModel cm = new CalibratedModel();

        Assert.assertNotNull(cm.getX_model());
        Assert.assertNotNull(cm.getY_model());
        Assert.assertNotNull(cm.getTrainingError());

        Assert.assertEquals(-1, cm.getTrainingError().getX_error());
        Assert.assertEquals(-1, cm.getTrainingError().getY_error());
        Assert.assertEquals(-1, cm.getTrainingError().getXY_error());
        Assert.assertEquals(-1, cm.getTrainingError().getSample_size());
    }


    @Test
    // testing the constructor (checking that attributes are not null)
    public void constructorTest1() {

        List<GazePoint> predictionsList = new ArrayList<>();

        predictionsList.add(new GazePoint(0, 0));
        predictionsList.add(new GazePoint(1, 1));
        predictionsList.add(new GazePoint(2, 2));

        List<GazePoint> coordinatesList = new ArrayList<>();
        coordinatesList.add(new GazePoint(0, 0));
        coordinatesList.add(new GazePoint(1, 1));
        coordinatesList.add(new GazePoint(2, 2));

        CalibratedModel cm = new CalibratedModel(predictionsList, coordinatesList);

        Assert.assertNotNull(cm.getX_model());
        Assert.assertNotNull(cm.getY_model());
        Assert.assertNotNull(cm.getTrainingError());
    }

    @Test
    // testing the constructor (when one of Lists is null)
    // needs handling (make a decision)
    public void constructorTest2() {

        List<GazePoint> predictionsList = null;

        List<GazePoint> coordinatesList = new ArrayList<>();
        coordinatesList.add(new GazePoint(0, 0));
        coordinatesList.add(new GazePoint(1, 1));
        coordinatesList.add(new GazePoint(2, 2));

        CalibratedModel cm = new CalibratedModel(predictionsList, coordinatesList);

        Assert.assertNotNull(cm.getX_model());
        Assert.assertNotNull(cm.getY_model());
        Assert.assertNotNull(cm.getTrainingError());
    }

    @Test
    // Testing when the inputs produces null
    // It gives an error because the inputs are giving null values (check predict function in the CalibratedModel.java)
    public void predictTest1() {

        GazePoint gp = new GazePoint(3, 4);

        List<GazePoint> predictionsList = new ArrayList<>();

        predictionsList.add(new GazePoint(2, 2));
        predictionsList.add(new GazePoint(3, 3));
        predictionsList.add(new GazePoint(0, 0));
        predictionsList.add(new GazePoint(1, 1));

        List<GazePoint> coordinatesList = new ArrayList<>();
        coordinatesList.add(new GazePoint(0, 0));
        coordinatesList.add(new GazePoint(1, 1));
        coordinatesList.add(new GazePoint(2, 2));
        coordinatesList.add(new GazePoint(3, 3));

        CalibratedModel cm = new CalibratedModel(predictionsList, coordinatesList);


    }

    // We can add another test for actual known inputs and outputs

}

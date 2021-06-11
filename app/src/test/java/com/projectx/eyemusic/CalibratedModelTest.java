package com.projectx.eyemusic;



import com.projectx.eyemusic.Features.Feature1;
import com.projectx.eyemusic.Model.CalibratedModel;
import com.projectx.eyemusic.Model.GazePoint;

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

        System.out.println(cm.getTrainingError().getX_error());
        Assert.assertEquals(-1.0, cm.getTrainingError().getX_error(), 0.1);

        System.out.println(cm.getTrainingError().getY_error());
        Assert.assertEquals(-1.0, cm.getTrainingError().getY_error(), 0.1);

        System.out.println(cm.getTrainingError().getXY_error());
        Assert.assertEquals(-1.0, cm.getTrainingError().getXY_error(), 0.1);

        System.out.println(cm.getTrainingError().getSample_size());
        Assert.assertEquals(-1.0, cm.getTrainingError().getSample_size(), 0.1);
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

        Assert.assertThrows (NullPointerException.class, () -> new CalibratedModel(predictionsList, coordinatesList));

        /*Assert.assertNotNull(cm.getX_model());
        Assert.assertNotNull(cm.getY_model());
        Assert.assertNotNull(cm.getTrainingError());*/
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

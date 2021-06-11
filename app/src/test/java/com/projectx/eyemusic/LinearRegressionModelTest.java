package com.projectx.eyemusic;

import com.projectx.eyemusic.Model.LinearRegressionModel;
import com.projectx.eyemusic.Model.MatrixUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class LinearRegressionModelTest {

    @Test
    // Testing the prediction of the linear regression which can be trained with Normal Equation: 3 points
    public void Test1() {

        List<Float> X1 = new ArrayList<Float>();
        X1.add(3f);
        X1.add(5f);
        X1.add(8f);

        List<Float> X2 = new ArrayList<Float>();
        X2.add(8f);
        X2.add(5f);
        X2.add(1f);

        List<Float> Y = new ArrayList<Float>();
        Y.add(4f);
        Y.add(8f);
        Y.add(9f);

        Float x1 = 4f;
        Float x2 = 7f;
        Float y_true = 1f;


        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Float offset = 1f;
        Assert.assertTrue(y_true - offset <= y_predicted && y_predicted <= y_true + offset);
    }

    @Test
    // linear regression model that cannot be trained: all 3 the points are same
    public void Test2() {

        List<Float> X1 = new ArrayList<Float>();
        X1.add(3f);
        X1.add(3f);
        X1.add(3f);

        List<Float> X2 = new ArrayList<Float>();
        X2.add(3f);
        X2.add(3f);
        X2.add(3f);

        List<Float> Y = new ArrayList<Float>();
        Y.add(2f);
        Y.add(2f);
        Y.add(2f);

        Float x1 = 4f;
        Float x2 = 7f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);
        Assert.assertNull(y_predicted);

    }

    @Test
    // Linear Regression that cannot be trained: 2 points (at least 3 points is needed)
    public void Test3() {
        List<Float> X1 = new ArrayList<Float>();
        X1.add(6f);
        X1.add(3f);

        List<Float> X2 = new ArrayList<Float>();
        X2.add(8f);
        X2.add(1f);

        List<Float> Y = new ArrayList<Float>();
        Y.add(5f);
        Y.add(2f);

        Float x1 = 4f;
        Float x2 = 7f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);
        Assert.assertNull(y_predicted);
    }

    @Test
    // Linear Regression that can be trained 4 points
    public void Test4() {
        List<Float> X1 = new ArrayList<Float>();
        X1.add(6f);
        X1.add(-3f);
        X1.add(1f);
        X1.add(0f);

        List<Float> X2 = new ArrayList<Float>();
        X2.add(1f);
        X2.add(-2f);
        X2.add(3f);
        X2.add(5f);

        List<Float> Y = new ArrayList<Float>();
        Y.add(5f);
        Y.add(-2f);
        Y.add(7f);
        Y.add(-4f);

        Float x1 = -4f;
        Float x2 = 7f;
        Float y_true = -4.7605f;


        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Float offset = 1f;
        Assert.assertTrue(y_true - offset <= y_predicted && y_predicted <= y_true + offset);
    }

    @Test
    // Linear Regression that can be trained with big values
    public void Test5() {
        List<Float> X1 = new ArrayList<Float>();
        X1.add(100f);
        X1.add(-565f);
        X1.add(600f);

        List<Float> X2 = new ArrayList<Float>();
        X2.add(-1090f);
        X2.add(2000f);
        X2.add(400f);

        List<Float> Y = new ArrayList<Float>();
        Y.add(600f);
        Y.add(3000f);
        Y.add(-5655f);

        Float x1 = 4f;
        Float x2 = 7f;
        Float y_true = 186.7758f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Float offset = 1f;
        Assert.assertTrue(y_true - offset <= y_predicted && y_predicted <= y_true + offset);
    }

    @Test
    // Linear Regression that can be trained with small values
    public void Test6() {
        List<Float> X1 = new ArrayList<Float>();
        X1.add(0.001f);
        X1.add(-0.26846f);
        X1.add(0.0008f);

        List<Float> X2 = new ArrayList<Float>();
        X2.add(-0.00048f);
        X2.add(0.005146f);
        X2.add(0.68842f);

        List<Float> Y = new ArrayList<Float>();
        Y.add(0.54842f);
        Y.add(0.5842f);
        Y.add(-0.56164f);

        Float x1 = 4f;
        Float x2 = 7f;
        Float y_true = -11.3977f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Float offset = 1f;
        Assert.assertTrue(y_true - offset <= y_predicted && y_predicted <= y_true + offset);
    }

    @Test
    // one of the features is empty:
    public void Test7() {
        List<Float> X1 = new ArrayList<Float>();

        List<Float> X2 = new ArrayList<Float>();
        X2.add(-0.00048f);
        X2.add(0.005146f);
        X2.add(0.68842f);

        List<Float> Y = new ArrayList<Float>();
        Y.add(0.54842f);
        Y.add(0.5842f);
        Y.add(-0.56164f);

        Float x1 = 4f;
        Float x2 = 7f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Assert.assertNull(y_predicted);
    }

    @Test
    // both of the features are empty:
    public void Test8() {
        List<Float> X1 = new ArrayList<Float>();

        List<Float> X2 = new ArrayList<Float>();

        List<Float> Y = new ArrayList<Float>();
        Y.add(0.54842f);
        Y.add(0.5842f);
        Y.add(-0.56164f);

        Float x1 = 4f;
        Float x2 = 7f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Assert.assertNull(y_predicted);
    }

    @Test
    // if the Y is empty
    public void Test9() {
        List<Float> X1 = new ArrayList<Float>();
        X1.add(0.001f);
        X1.add(-0.26846f);
        X1.add(0.0008f);

        List<Float> X2 = new ArrayList<Float>();
        X2.add(-0.00048f);
        X2.add(0.005146f);
        X2.add(0.68842f);

        List<Float> Y = new ArrayList<Float>();


        Float x1 = 4f;
        Float x2 = 7f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Float offset = 1f;
        Assert.assertNull(y_predicted);
    }

    @Test
    // if the Y is null
    public void Test10() {
        List<Float> X1 = new ArrayList<Float>();
        X1.add(0.001f);
        X1.add(-0.26846f);
        X1.add(0.0008f);

        List<Float> X2 = new ArrayList<Float>();
        X2.add(-0.00048f);
        X2.add(0.005146f);
        X2.add(0.68842f);

        List<Float> Y = null;


        Float x1 = 4f;
        Float x2 = 7f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Float offset = 1f;
        Assert.assertNull(y_predicted);
    }

    @Test
    // if the X2 is empty
    public void Test11() {
        List<Float> X1 = new ArrayList<Float>();
        X1.add(0.001f);
        X1.add(-0.26846f);
        X1.add(0.0008f);

        List<Float> X2 = null;

        List<Float> Y = new ArrayList<Float>();


        Float x1 = 4f;
        Float x2 = 7f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Float offset = 1f;
        Assert.assertNull(y_predicted);
    }

    @Test
    // if the X1 is empty
    public void Test12() {
        List<Float> X1 = null;

        List<Float> X2 = new ArrayList<Float>();
        X2.add(0.001f);
        X2.add(-0.26846f);
        X2.add(0.0008f);

        List<Float> Y = new ArrayList<Float>();


        Float x1 = 4f;
        Float x2 = 7f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Float offset = 1f;
        Assert.assertNull(y_predicted);
    }

    @Test
    // if the all are empty
    public void Test13() {
        List<Float> X1 = null;

        List<Float> X2 = null;

        List<Float> Y = null;


        Float x1 = 4f;
        Float x2 = 7f;

        LinearRegressionModel model = new LinearRegressionModel(X1, X2, Y, LinearRegressionModel.TRAIN_NORM_EQUATION);
        Float y_predicted = model.predict(x1, x2);

        System.out.println("y_predict: " +  y_predicted);

        Float offset = 1f;
        Assert.assertNull(y_predicted);
    }
}

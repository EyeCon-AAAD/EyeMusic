package com.projectx.eyemusic.Model;


import android.util.Log;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


// TODO: you can make it more general
public class LinearRegressionModel {
    private static final String TAG = "LinearRegressionModel";
    private  List<Float> X1_array; // for x coordinates
    private  List<Float> X2_array; // for y coordinates
    private  List<Float> Y_array; // result
    private static final int FEATURE_SIZE = 3;

    public static final int TRAIN_NORM_EQUATION = 0;
    public static final int TRAIN_GRADIENT_DESCENT = 1;

    private float a2 = 0;
    private float a1 = 0;
    private float a0 = 0;
    private boolean trained = false;

    public LinearRegressionModel(){ };

    public LinearRegressionModel(List<Float> x1_array, List<Float> x2_array, List<Float> y_array, int mode) {
        X1_array = x1_array;
        X2_array = x2_array;
        Y_array = y_array;

        if (mode == TRAIN_NORM_EQUATION){
            boolean success = trainNormEquation();
            Log.d(TAG, "LinearRegressionModel: " + success);
            if (!success){
                trained = trainGradientDescent();
            }
            else{
                trained = true;
            }
        }
        else{
            trained = trainGradientDescent();
        }
    }

    public Float predict(Float x1, Float x2){
        if (trained)
            return a2*x2 + a1*x1 + a0;
        else{
            Log.e(TAG, "predict: the model is not trained");
            return null;
        }
    }

    private boolean trainNormEquation(){
        // ref: https://www.coursera.org/learn/machine-learning/supplement/bjjZW/normal-equation

        if (X1_array.size() != X2_array.size()){
            Log.e(TAG, "trainNormEquation: the size of the features are not same");
            return false;
        }
        int SAMPLE_SIZE = X1_array.size();


        //building the XT
        Float[] feature0 = new Float[SAMPLE_SIZE];
        for (int i = 0; i < feature0.length; i++)
            feature0[i] = 1f;

        Float[] feature1 = new Float[SAMPLE_SIZE];
        X1_array.toArray(feature1);

        Float[] feature2 = new Float[SAMPLE_SIZE];
        X2_array.toArray(feature2);

        Float[][] XT = new Float[][]{
                feature0,
                feature1,
                feature2
        };
        showMatrix(XT, FEATURE_SIZE, SAMPLE_SIZE, "XT");
        //creating the X
//        Float[][] X = new Float[SAMPLE_SIZE][FEATURE_SIZE];
//        MatrixUtils.transpose(XT, X, FEATURE_SIZE, SAMPLE_SIZE);

        Float[][] X;
        X = MatrixUtils.transpose(XT, FEATURE_SIZE, SAMPLE_SIZE);
        showMatrix(X, SAMPLE_SIZE, FEATURE_SIZE, "X");

        // creating X*XT
        Float[][] A1;
        A1 = MatrixUtils.multiplyMatrix(FEATURE_SIZE, SAMPLE_SIZE, XT, SAMPLE_SIZE, FEATURE_SIZE, X);
        showMatrix(A1, FEATURE_SIZE, FEATURE_SIZE, "A1");

        //TODO: check the size of the A1 or test the function

        //creating inverse(XT*X)
        Float[][] A2;
        A2 = MatrixUtils.inverse(A1, A1.length, A1[0].length);
        if (A2 == null){
            Log.e(TAG, "trainNormEquation: the inverse is null");
            return false;
        }

        //TODO: check the size of the A2 or test the function

        //creating inverse(XT*X)*XT
        Float[][] A3;
        A3 = MatrixUtils.multiplyMatrix(A2.length, A2[0].length, A2, XT.length, XT[0].length, XT);

        //TODO: check the size of the A3 or test the function

        // building y
        Float[] y = new Float[Y_array.size()];
        Y_array.toArray(y);

        Float[][] YT = new Float[][]{y};
//        Float[][] Y = new Float[SAMPLE_SIZE][1];
//        MatrixUtils.transpose(YT, Y, 1, SAMPLE_SIZE);

        Float[][] Y;
        Y = MatrixUtils.transpose(YT,1, SAMPLE_SIZE);

        //creating coefs = inverse(XT*X)*XT*y
        Float[][] coefs;
        coefs = MatrixUtils.multiplyMatrix(A3.length, A3[0].length, A3, Y.length, Y[0].length, Y);

        if (coefs.length != FEATURE_SIZE || coefs[0].length !=1){
            Log.e(TAG, "trainNormEquation: the coef size is wrong :" + coefs.length + "x:"+ coefs[1].length +
                    "instead of "+ FEATURE_SIZE + "x" + 1);
            return false;
        }

        this.a0 = coefs[0][0];
        this.a1 = coefs[1][0];
        this.a2 = coefs[2][0];
        return true;
    }

    //TODO: complete
    private boolean trainGradientDescent(){
        if (X1_array.size() != X2_array.size()){
            Log.e(TAG, "trainNormEquation: the size of the features are not same");
            return false;
        }
        Log.e(TAG, "trainNormEquation: it is not implemented yet");
        return false;
    }

    public boolean isTrained() {
        return trained;
    }

    private void showMatrix(Float[][] A, int rows, int cols, String name){

        for ( int i = 0; i < rows; i++ ){
            StringBuilder strbul = new StringBuilder();
            for(Float a : A[i])
            {
                strbul.append(a);
                //for adding comma between elements
                strbul.append(",");
            }

            String str=strbul.toString();
            Log.i("NormalLinearRegression", name + ": " + str);
            Log.i("nothing", ".");
        }
    }
}
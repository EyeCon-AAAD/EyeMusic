/*
 * Author: David T. Auna */
package com.projectx.eyemusic.Model;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.impl.CaptureProcessor;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.projectx.eyemusic.App;
import com.projectx.eyemusic.Features.Feature1;
import com.projectx.eyemusic.R;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class OriginalModel {

    private File modelFile;
    private Interpreter interpreter;
    private Boolean isModelDownloaded;
    private static String TAG = "OriginalModel";

    private static OriginalModel gazePredictionModel = null;

    private OriginalModel() {
        this.modelFile = null;
        this.interpreter = null;
        this.isModelDownloaded = false;

        // can get model in constructor for now
        getModel();
    }

    public static OriginalModel getInstance(){
        if(gazePredictionModel == null)
            gazePredictionModel = new OriginalModel();

        return gazePredictionModel;
    }

    // TODO implement inference with multiple inputs
    public GazePoint Predict(Feature1 feature){
        if(isModelDownloaded()){
            // DO prediction
            // declare tensors
            
            TensorImage eyeLeftTensor = new TensorImage(DataType.UINT8);
            TensorImage eyeRightTensor = new TensorImage(DataType.UINT8);
            TensorImage faceTensor = new TensorImage(DataType.UINT8);
            ByteBuffer faceMask = getFaceMask();

            // load and pre-process images
            eyeLeftTensor = processAndLoadImage(feature.getEyeLeftImage(), eyeLeftTensor);
            eyeRightTensor = processAndLoadImage(feature.getEyeRightImage(), eyeRightTensor);
            faceTensor = processAndLoadImage(feature.getFaceImage(), faceTensor);


            // create container for prediction result
            TensorBuffer coordinateBuffer = TensorBuffer.createFixedSize(new int[]{1, 2},
                    DataType.FLOAT32);

            // order of inputs matters
            Object[] inputs = new Object[]{eyeLeftTensor.getBuffer(),
                    eyeRightTensor.getBuffer(),
                    faceTensor.getBuffer(),
                    faceMask};
            // outputs
            Map<Integer, Object> output = new HashMap<>();
            output.put(0, coordinateBuffer.getBuffer());

            // infer
            try{
                interpreter.runForMultipleInputsOutputs(inputs, output);
                // need to test inference
                ByteBuffer out = coordinateBuffer.getBuffer();
                out.rewind();
                float x_coordinate = out.get(0);
                float y_coordinate = out.get(1);
                Log.d(TAG, String.format("Coordinates (x, y) : (%1.4f, %1.4f)", x_coordinate, y_coordinate));

                return new GazePoint(x_coordinate, y_coordinate);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
            return null;
        }
        return null;
    }

    private void getModel(){
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .build(); // can add .requireWifi() but model isn't too big (13 MB)

        FirebaseModelDownloader.getInstance()
                .getModel(App.getContext().getString(R.string.GazeModelName),
                        DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(@NonNull CustomModel customModel) {
                        // Download Complete. Enable Model dependent functionality
                        // The CustomModel object contains the local path of the model file,
                        // which you can use to instantiate a TensorFlow Lite interpreter.
                        Log.d(TAG, "Model downloaded!");

                        setModelDownloaded(true);
                        setModelFile(customModel.getFile());
                        if (modelFile != null){
                            interpreter = new Interpreter(modelFile);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error " + e.toString());
                        // Show Toast for now
                        Toast.makeText(App.getContext(), "Error Downloading Model",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void setModelFile(File modelFile) {
        this.modelFile = modelFile;
    }

    public Boolean isModelDownloaded() {
        return isModelDownloaded;
    }

    private void setModelDownloaded(Boolean modelDownloaded) {
        isModelDownloaded = modelDownloaded;
    }

    private TensorImage processAndLoadImage(Bitmap image, TensorImage tensorImage){
        // should be 64x64
        float mean = getMean(image);
        // pre-processing pipeline
        ImageProcessor imageProcessor;
        imageProcessor = new ImageProcessor.Builder()
                // Resize with Bilinear method
                .add(new ResizeOp(64, 64, ResizeOp.ResizeMethod.BILINEAR))
                // not sure if this normalizes to [0.0 - 1.0]
                .add(new NormalizeOp(mean, 127.0f))
                .build();

        // load bitmap image to tensor
        tensorImage.load(image);

        // resize and normalize image

        tensorImage = imageProcessor.process(tensorImage);
        return tensorImage;
    }

    private float getMean(Bitmap image) {
        float sum = 0.0f, mean;
        float totalPixels = 64.0f * 64.0f * 3.0f;
        for (int y = 0; y < image.getWidth(); y++) {
            for (int x = 0; x < image.getHeight(); x++) {
                int px = image.getPixel(x, y);

                // get channel values fom the pixel value
                int r = Color.red(px);
                int g = Color.green(px);
                int b = Color.blue(px);

                sum += ((r / 255.0f) + (g / 255.0f) + (b / 255.0f));
            }
        }
        mean = sum / totalPixels;
        return mean;
    }

    // need to explicitly create ByteBuffer for face mask to match input shape for model
    // used ALPHA_8 for gray-scale
    private ByteBuffer getFaceMask(){
        Bitmap bitmap = Bitmap.createBitmap(25, 25, Bitmap.Config.ALPHA_8);
        ByteBuffer faceMaskInput = ByteBuffer.allocateDirect(25 * 25 * 4).order(ByteOrder.nativeOrder());
        int c = 0;
        for (int y = 0; y < 25; y++) {
            for (int x = 0; x < 25; x++) {
                if(x % 2 == 0 && y % 2 == 0){
                    bitmap.setPixel(x, y, Color.alpha(50));
                    Log.d(TAG, "pixel val: " + bitmap.getPixel(x, y));
                }
                else{
                    bitmap.setPixel(x, y, Color.alpha(255));
                    Log.d(TAG, "pixel val: " + bitmap.getPixel(x, y));
                }

                int px = bitmap.getPixel(x, y);
                int a = Color.alpha(px);
                // Log.d(TAG, "Face mask px: " + px);
                faceMaskInput.putFloat(px);
                c++;
            }
        }
        Log.d(TAG, "pixel count: " + c);
        return faceMaskInput;
    }
}
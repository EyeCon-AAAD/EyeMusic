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
import com.projectx.eyemusic.ml.GazePredictorModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private GazePredictorModel model;
    private OriginalModel() throws IOException {
        this.modelFile = null;
        this.interpreter = null;
        this.isModelDownloaded = false;
        this.model = GazePredictorModel.newInstance(App.getContext());
        // can get model in constructor for now
        // TODO: Add creation of Original Model Object and Model download when the
        //  application launches the first time
        // getModel();
    }

    public static OriginalModel getInstance(){
        if(gazePredictionModel == null) {
            try {
                gazePredictionModel = new OriginalModel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gazePredictionModel;
    }

    public GazePoint Predict(Feature1 feature) throws IOException {

    //if(isModelDownloaded()){
        // DO prediction
        // create byte buffers from bitmap image while pre-processing them
        ByteBuffer eyeLeft = processBitmap(feature.getLeftEyeImage(), "left_eye");
        ByteBuffer eyeRight = processBitmap(feature.getRightEyeImage(), "right_eye");
        ByteBuffer face = processBitmap(feature.getFaceImage(), "face");
        ByteBuffer faceMask = faceGridToByteBuffer(feature.getFaceGrid());

        // Add offline setup of TFLite
        // Creates inputs for reference.
        TensorBuffer eyeLeftTensor = TensorBuffer.createFixedSize(new int[]{1, 64, 64, 3}, DataType.FLOAT32);
        eyeLeftTensor.loadBuffer(eyeLeft);
        TensorBuffer eyeRightTensor = TensorBuffer.createFixedSize(new int[]{1, 64, 64, 3}, DataType.FLOAT32);
        eyeRightTensor.loadBuffer(eyeRight);
        TensorBuffer faceTensor = TensorBuffer.createFixedSize(new int[]{1, 64, 64, 3}, DataType.FLOAT32);
        faceTensor.loadBuffer(face);
        TensorBuffer faceMaskTensor = TensorBuffer.createFixedSize(new int[]{1, 625}, DataType.FLOAT32);
        faceMaskTensor.loadBuffer(faceMask);

        /*// create container for prediction result
        TensorBuffer coordinateBuffer = TensorBuffer.createFixedSize(new int[]{1, 2},
                DataType.FLOAT32);*/

        // Runs model inference and gets result.
        GazePredictorModel.Outputs outputs = model.process(eyeLeftTensor, eyeRightTensor, faceTensor, faceMaskTensor);
        TensorBuffer outputFeatures = outputs.getOutputFeature0AsTensorBuffer();
        /*// order of inputs matters
        Object[] inputs = new Object[]{eyeLeft,
                eyeRight,
                face,
                faceMask};*/
        // outputs
        /*Map<Integer, Object> output = new HashMap<>();
        output.put(0, coordinateBuffer.getBuffer());*/
        ByteBuffer out = outputFeatures.getBuffer();
        out.rewind();
        float x_coordinate = out.get(0);
        float y_coordinate = out.get(1);
        Log.d(TAG, String.format("Coordinates (x, y) : (%1.4f, %1.4f)", x_coordinate, y_coordinate));
        return new GazePoint(x_coordinate, y_coordinate);

        // infer
        /*try{
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
        }*/
    }
    //}

    /*private void getModel(){
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
                        Log.e(TAG, "Error " + e.toString());
                        // Show Toast for now
                        Toast.makeText(App.getContext(), "Error Downloading Model",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }*/

    public void setModelFile(File modelFile) {
        this.modelFile = modelFile;
    }

    public Boolean isModelDownloaded() {
        return isModelDownloaded;
    }

    private void setModelDownloaded(Boolean modelDownloaded) {
        isModelDownloaded = modelDownloaded;
    }

    // need to explicitly create ByteBuffer for face mask to match input shape for model
    // used ALPHA_8 for gray-scale
    private ByteBuffer faceGridToByteBuffer(int[][] faceGrid){
        Bitmap bitmap = Bitmap.createBitmap(25, 25, Bitmap.Config.ALPHA_8);
        ByteBuffer faceMaskInput = ByteBuffer.allocateDirect(25 * 25 * 4).order(ByteOrder.nativeOrder());
        int c = 0;
        for (int y = 0; y < 25; y++) {
            for (int x = 0; x < 25; x++) {
                bitmap.setPixel(x, y, Color.alpha(faceGrid[x][y]));
                int px = bitmap.getPixel(x, y);
                faceMaskInput.putFloat(px);
            }
        }
        return faceMaskInput;
    }

    private ByteBuffer processBitmap(Bitmap image, String type) throws IOException {
        // Bitmap should be size 64 * 64 * 3 = 12288 * 4 bytes(float32)
        // read raw binary file containing means used during training
        InputStream is = null;
        switch (type){
            case "left_eye":
                is = App.getContext().getResources().openRawResource(R.raw.mean_eye_left);
                break;
            case "right_eye":
                is = App.getContext().getResources().openRawResource(R.raw.mean_eye_right);
                break;
            case "face":
                is = App.getContext().getResources().openRawResource(R.raw.mean_face);
                break;
            default:
                break;
        }
        if(is == null){
            throw new NullPointerException("InputStream for raw file is null");
        }
        DataInputStream meanInputStream = new DataInputStream(is);
        ByteBuffer inputImage = ByteBuffer.allocateDirect(64 * 64 * 3 * 4).order(ByteOrder.nativeOrder());
        for(int y = 0; y < 64; y++){
            for (int x = 0; x < 64; x++) {
                int px = image.getPixel(x, y);

                // get channel values from the pixel value
                int r = Color.red(px);
                int g = Color.green(px);
                int b = Color.blue(px);

                // read from data input stream
                float r_mean = meanInputStream.readFloat();
                float g_mean = meanInputStream.readFloat();
                float b_mean = meanInputStream.readFloat();

                // pre-process
                // TODO: Need to add (- mean) appropriately
                float rf = (r / 255.0f) ;
                float gf = (g / 255.0f) ;
                float bf = (b / 255.0f) ;

                // add to byte buffer
                inputImage.putFloat(rf);
                inputImage.putFloat(gf);
                inputImage.putFloat(bf);
            }
        }
        return inputImage;
    }
}
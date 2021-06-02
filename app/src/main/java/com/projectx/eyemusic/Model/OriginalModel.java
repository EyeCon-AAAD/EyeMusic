/*
 * Author: David T. Auna */
package com.projectx.eyemusic.Model;

import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.projectx.eyemusic.App;
import com.projectx.eyemusic.Features.Feature1;
import com.projectx.eyemusic.R;

import org.tensorflow.lite.Interpreter;

import java.io.File;

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
            Toast.makeText(App.getContext(), "Model downloaded",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "File: " + modelFile.getAbsolutePath());
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
                        Toast.makeText(App.getContext(), "Model downloaded",
                                Toast.LENGTH_SHORT).show();

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

    public void setModelDownloaded(Boolean modelDownloaded) {
        isModelDownloaded = modelDownloaded;
    }
}
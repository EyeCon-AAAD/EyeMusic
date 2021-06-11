package com.projectx.eyemusic;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.mlkit.vision.face.Face;
import com.projectx.eyemusic.Features.RawFeature;

import org.junit.Assert;
import org.junit.Test;

public class RawFeatureTest {

    @Test
    //checks if the exception is thrown when null arguments are passed
    public void Test1(){
        //both are null
        Assert.assertThrows (NullPointerException.class, () -> new RawFeature(null, null));

        //face is null
        Bitmap bitmap = Bitmap.createBitmap(new int[]{Color.RED}, 64, 64, Bitmap.Config.ARGB_8888);
        Assert.assertThrows (NullPointerException.class, () -> new RawFeature(bitmap, null));
    }
}

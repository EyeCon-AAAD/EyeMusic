package com.projectx.eyemusic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.FaceDetector;

import com.google.mlkit.vision.face.Face;
import com.projectx.eyemusic.Features.Feature1;
import com.projectx.eyemusic.Features.RawFeature;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

public class Feature1Test {
    /*@Rule
    public final ExpectedException exception = ExpectedException.none();*/

    @Test
    // throw an exception when the arguments are null
    public void Test1(){
        Assert.assertThrows (NullPointerException.class, () -> new Feature1(null, null));

        //face is null
        Bitmap bitmap = Bitmap.createBitmap(new int[]{Color.RED}, 64, 64, Bitmap.Config.ARGB_8888);
        Assert.assertThrows (NullPointerException.class, () -> new RawFeature(bitmap, null));
    }
}

/*
 * Author: David T. Auna
 *
 * Class for making the Application Context available to all classes*/
package com.projectx.eyemusic;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

public class App extends Application {
    // Use WeakReference to avoid Memory Leak refer to
    // https://stackoverflow.com/questions/4391720/how-can-i-get-a-resource-content-from-a-static-context/4391811#4391811
    private static WeakReference<Context> context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = new WeakReference<Context>(this);
    }

    public static Context getContext(){
        return context.get();
    }
}
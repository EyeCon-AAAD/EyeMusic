package com.projectx.eyemusic.Model;

public class GazePoint{
    float x;
    float y;
    public GazePoint(float x, float y){
        this.x = x;
        this.y = y;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "GazePoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

package com.mobilecomputing.mandarandash.activityrecongnition;

/**
 * Created by Mandar on 3/10/2015.
 */
public class SensorDataModel {
    private float x;
    private float y;
    private float z;
    private float magnitude;

    public float getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(float magnitude) {
        this.magnitude = magnitude;
    }

    public float getX() {
        return x;
    }

    public SensorDataModel(float x, float y, float z, float magnitude) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.magnitude = magnitude;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }
}

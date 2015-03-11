package com.mobilecomputing.mandarandash.activityrecongnition;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

public class DataCollectorService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private float xAccl, yAccl, zAccl;
    private IBinder myBinder = new LocalBinder();
    private Handler myHandler = new Handler();
    private float sensorData[];
    public DataCollectorService() {
        sensorData = new float[3];
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myBinder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {

            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                xAccl = event.values[0];
                yAccl = event.values[1];
                zAccl = event.values[2];
                sensorData[0] = xAccl;
                sensorData[1] = yAccl;
                sensorData[2] = zAccl;
//                myHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),"Returning sensor data")
//                    }
//                });

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class LocalBinder extends Binder {

        DataCollectorService getService()
        {
            return DataCollectorService.this;
        }

    }

    public String test_method(){
        return "JUNK";
    }
    public float[] getSensorData(){
        return sensorData;
    }
    public void startSensors(){

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this,accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,gyroscope, SensorManager.SENSOR_DELAY_NORMAL);

    }

}

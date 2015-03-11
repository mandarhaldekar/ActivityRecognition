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

import java.util.ArrayList;

public class DataCollectorService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private float xAccl, yAccl, zAccl;
    private IBinder myBinder = new LocalBinder();
    private Handler myHandler = new Handler();
    private float sensorData[];
    private ArrayList<SensorDataModel> accelerometer_data; //This will hold accelerometer data
    private long currentTime;
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
                /**
                 * TO-DO: Collect data continuously and at the end of two min interval, call the algorithm to detect activity
                 */
                if(currentTime > System.currentTimeMillis() ) {
                    //Add reading to the results

                    xAccl = event.values[0];
                    yAccl = event.values[1];
                    zAccl = event.values[2];
                    sensorData[0] = xAccl;
                    sensorData[1] = yAccl;
                    sensorData[2] = zAccl;
                    SensorDataModel sensorDataModel = new SensorDataModel(xAccl,yAccl,zAccl);
                    accelerometer_data.add(sensorDataModel);

                } else{
                    /**
                     * TO-DO: Call activity recognition algorithm here. Pass accelerometer_data arraylist and display activity on Main UI
                     */
                    accelerometer_data = new ArrayList<SensorDataModel>();
                    currentTime = System.currentTimeMillis() + 1*30*1000;
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Returning sensor data",Toast.LENGTH_LONG).show();
                        }
                });
                }
            }
            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                //TO-DO
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

        currentTime = System.currentTimeMillis() + 1*30*1000; //Half minute. Make it two later
        accelerometer_data = new ArrayList<SensorDataModel>();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this,accelerometer);
        sensorManager.unregisterListener(this,gyroscope);
    }
}

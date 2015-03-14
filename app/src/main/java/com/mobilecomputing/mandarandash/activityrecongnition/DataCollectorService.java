package com.mobilecomputing.mandarandash.activityrecongnition;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class DataCollectorService extends Service implements SensorEventListener {

    private static final long INTERVAL = 1*30*1000 ;
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private float xAccl, yAccl, zAccl;
    private IBinder myBinder = new LocalBinder();
    private Handler myHandler = new Handler();
    private float sensorData[],gravity[],linear_acceleration[];
    private ArrayList<SensorDataModel> accelerometer_data; //This will hold accelerometer data
    private long timeOutInterval;
    private ArrayList<ActivityRecord> activityRecordArrayList;
    private Integer count=0;

    public DataCollectorService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myBinder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this) {

            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                /**
                 * TO-DO: Collect data continuously and at the end of two min interval, call the algorithm to detect activity
                 */
                count++;
                if (timeOutInterval > System.currentTimeMillis()) {
                    //Add reading to the results

                    //Filtering
                    final float alpha = 0.8f;

                    // Isolate the force of gravity with the low-pass filter.
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                    // Remove the gravity contribution with the high-pass filter.
                    linear_acceleration[0] = event.values[0] - gravity[0];
                    linear_acceleration[1] = event.values[1] - gravity[1];
                    linear_acceleration[2] = event.values[2] - gravity[2];

                    //Magnitude
                    linear_acceleration[3] = (float) Math.sqrt(linear_acceleration[0]*linear_acceleration[0] + linear_acceleration[1]*linear_acceleration[1] + linear_acceleration[2]*linear_acceleration[2]);


                    SensorDataModel sensorDataModel = new SensorDataModel(linear_acceleration[0],linear_acceleration[1],linear_acceleration[2],linear_acceleration[3]);

                    accelerometer_data.add(sensorDataModel);

                } else {
                    /**
                     * TO-DO: Call activity recognition algorithm here. Pass accelerometer_data arraylist and display activity on Main UI
                     */
                    detectActivity(timeOutInterval - INTERVAL);
                    accelerometer_data = new ArrayList<SensorDataModel>();
                    timeOutInterval = System.currentTimeMillis() + INTERVAL;
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {


                            Toast.makeText(getApplicationContext(), "Returning sensor data:"+count.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                //TO-DO
            }
        }

    }

    private void convertDateToTimestamp(Date date){
        java.util.Date utilDate = new java.util.Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(utilDate);
        cal.set(Calendar.MILLISECOND, 0);
        System.out.println(new java.sql.Timestamp(utilDate.getTime()));
        System.out.println(new java.sql.Timestamp(cal.getTimeInMillis()));
    }



    private void publishProgress(String text) {
        Log.v("ABC", "reporting back from the Random Number Thread");
        Bundle msgBundle = new Bundle();
        msgBundle.putString("result", text);
        Message msg = new Message();
        msg.setData(msgBundle);
        myHandler.sendMessage(msg);
    }



    public String detectActivity(long lastMilliSeconds){
        int i;
        int numberOfSamples = 0;
        for (i=accelerometer_data.size()-1; i>=0 ; i--) {
            if (accelerometer_data.get(i).getMagnitude() > 0.3)
            {
                numberOfSamples++;
                continue;

            }
            else
            {
                //Add walking activity to the data structure
                if (numberOfSamples > 5*5*10) {  //TO BE tested
                    ActivityRecord obj = new ActivityRecord(new Date(lastMilliSeconds + i * 1000), "Walking");
                    activityRecordArrayList.add(obj);
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {


                            Toast.makeText(getApplicationContext(), "Wrote activity walking", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                }
                numberOfSamples = 0; //If number of samples are not enough to detect that activity is "Walking"

            }


        }
        //Write to SD Card

        return "Done";
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
    public  float[] getSensorData(){
//        sensorData[0] = xAccl;
//        sensorData[1] = yAccl;
//        sensorData[2] = zAccl;
//        return  sensorData;
        return  linear_acceleration;
    }
    public void startSensors(){

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this,accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorData = new float[3];
        gravity = new float[3];
        linear_acceleration = new float[4]; //Fourth one for magnitude
        timeOutInterval = System.currentTimeMillis() + INTERVAL; //Half minute. Make it two later
        accelerometer_data = new ArrayList<SensorDataModel>();
        activityRecordArrayList = new ArrayList<ActivityRecord>();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this,accelerometer);
        sensorManager.unregisterListener(this,gyroscope);
    }
}

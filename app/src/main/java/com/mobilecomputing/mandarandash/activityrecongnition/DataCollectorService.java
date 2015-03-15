package com.mobilecomputing.mandarandash.activityrecongnition;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
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
    private ArrayList<SensorDataModel> unfiltered_accelerometer_data; //This will hold accelerometer data
    private long timeOutInterval;
    private ArrayList<ActivityRecord> activityRecordArrayList;
    private Integer count; //To count number of samples in one INTERVAL
    private File file;

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

                if (timeOutInterval > System.currentTimeMillis()) {
                    //Add reading to the results
                    count++;
                   //Unfiltered Data
                    sensorData[0] = event.values[0];
                    sensorData[1] = event.values[1];
                    sensorData[2] = event.values[2];

                    //Filtering
                    final float alpha = 0.8f;


//                    Isolate the force of gravity with the low-pass filter.
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
                    SensorDataModel sensorDataModel_unfiltered = new SensorDataModel(sensorData[0],sensorData[1],sensorData[2],0);

                    accelerometer_data.add(sensorDataModel);
                    unfiltered_accelerometer_data.add(sensorDataModel_unfiltered);

                } else {
                    /**
                     * TO-DO: Call activity recognition algorithm here. Pass accelerometer_data arraylist and display activity on Main UI
                     */
                    detectActivity(timeOutInterval - INTERVAL,accelerometer_data,unfiltered_accelerometer_data,count);
                    accelerometer_data = new ArrayList<SensorDataModel>();
                    unfiltered_accelerometer_data = new ArrayList<SensorDataModel>();
                    timeOutInterval = System.currentTimeMillis() + INTERVAL;

                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {


                            Toast.makeText(getApplicationContext(), "Returning sensor data:"+count.toString(), Toast.LENGTH_LONG).show();
                            count = 0;
                        }
                    });

                }
            }

            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                //TO-DO
            }
        }

    }

    private String convertDateToString(long millisec){
        Date date = new Date(millisec);
        SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = localDateFormat.format(date);
        return time;
    }



    private void publishProgress(String text) {
        Log.v("ABC", "reporting back from the Random Number Thread");
        Bundle msgBundle = new Bundle();
        msgBundle.putString("result", text);
        Message msg = new Message();
        msg.setData(msgBundle);
        myHandler.sendMessage(msg);
    }

    public void detectActivity(final long lastMilliSeconds,ArrayList<SensorDataModel> accelerometer_data_temp,ArrayList<SensorDataModel> unfiltered_accelerometer_data_temp,int count_activity){

        int size = accelerometer_data_temp.size();
        int numberofSamplesWalking = 0;
        int numberofSamplesSleeping = 0;
        int numberofSamplesSitting = 0;

        for(int i=0;i<size;i++)
        {
            if(accelerometer_data_temp.get(i).getMagnitude() > 0.5f)
                numberofSamplesWalking++;
            else{
                if(unfiltered_accelerometer_data_temp.get(i).getY() > 7.5f)
                    numberofSamplesSitting++;
                else if(unfiltered_accelerometer_data_temp.get(i).getZ() > 7.5f)
                    numberofSamplesSleeping++;
            }
        }
        if(numberofSamplesWalking >= numberofSamplesSleeping && numberofSamplesWalking >= numberofSamplesSitting)
        {
            //walking
            storeActivity("Walking",lastMilliSeconds);
        }
        else if(numberofSamplesSitting >= numberofSamplesWalking && numberofSamplesSitting >= numberofSamplesSleeping)
        {
            //sitting
            storeActivity("Sitting",lastMilliSeconds);
        } else if(numberofSamplesSleeping >= numberofSamplesWalking && numberofSamplesSleeping >= numberofSamplesWalking) {
            //sleeping
            storeActivity("Sleeping", lastMilliSeconds);
        }

    }
    public void storeActivity(final String activity,long lastMilliSeconds){
        //walking
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(convertDateToString(lastMilliSeconds)).append( " - ").append(convertDateToString(lastMilliSeconds+INTERVAL));
        ActivityRecord obj = new ActivityRecord(stringBuffer.toString(), activity);
        write_to_file(obj);
        activityRecordArrayList.add(obj);
        myHandler.post(new Runnable() {
            @Override
            public void run() {

                //Log.e("Counts",numberOfSamples.toString());

                Toast.makeText(getApplicationContext(), "Wrote activity "+ activity, Toast.LENGTH_LONG).show();
            }
        });


    }
/*
    public String detectActivity(long lastMilliSeconds,ArrayList<SensorDataModel> accelerometer_data_temp,ArrayList<SensorDataModel> unfiltered_accelerometer_data_temp,int count_activity){
        int i;
        int numberOfSamples = 0;
        int isWalking = 0;
        int lastAcitivity_count_threshold = (int) ((count_activity / INTERVAL) * 10 * 1000);
        for (i=accelerometer_data_temp.size()-1; i>=0 ; i--) {

            if ((accelerometer_data_temp.get(i).getMagnitude()) > 0.5f)
            {
                numberOfSamples++;
                continue;

            }
            else
            {
                final Integer mysamples=numberOfSamples;
                //Add walking activity to the data structure
                if (numberOfSamples > ((count_activity / INTERVAL) * 10 * 1000)) // Number of sample/sec = (count /INTERVAL in ms ) * 5 (as we want last activity for 5 sec) * 1000 (ms to s)
                {  //TO BE tested
                    isWalking = 1;
                    ActivityRecord obj = new ActivityRecord(new Date(lastMilliSeconds + i * 1000), "Walking");
                    write_to_file(obj);
                    activityRecordArrayList.add(obj);
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            //Log.e("Counts",numberOfSamples.toString());
                            Toast.makeText(getApplicationContext(), "Wrote activity walking"+mysamples.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
//                    return "walking";

                    break;

                }
                numberOfSamples = 0; //If number of samples are not enough to detect that activity is "Walking"

            }


        }
        if(isWalking == 1)
            return "Walking";
        i = 0;
        int numberOfSamplesY = 0;
        int numberOfSamplesZ = 0;
        for (i=unfiltered_accelerometer_data_temp.size()-1; i>=0 ; i--) {

            if (unfiltered_accelerometer_data_temp.get(i).getY() > 7.5f) {
                numberOfSamplesY++;
                continue;

            }
            if (unfiltered_accelerometer_data_temp.get(i).getZ() > 7.5f) {
                numberOfSamplesZ++;
                continue;

            }
        }
//            else
//            {
                //Add walking activity to the data structure
        if (numberOfSamplesY > numberOfSamplesZ) {


            if (numberOfSamplesY > (count_activity / INTERVAL * 10 * 1000)) // Number of sample/sec = (count /INTERVAL in ms ) * 5 (as we want last activity for 5 sec) * 1000 (ms to s)
            {  //TO BE tested
                ActivityRecord obj = new ActivityRecord(new Date(lastMilliSeconds + i * 1000), "Sitting");
                write_to_file(obj);
                activityRecordArrayList.add(obj);
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {


                        Toast.makeText(getApplicationContext(), "Wrote activity sitting", Toast.LENGTH_LONG).show();
                    }
                });
                return "sitting";
//                    break;
            }
        }
        else {
            //Add walking activity to the data structure
            if (numberOfSamplesZ > ((count_activity / INTERVAL) * 10 * 1000)) // Number of sample/sec = (count /INTERVAL in ms ) * 5 (as we want last activity for 5 sec) * 1000 (ms to s)
            {  //TO BE tested
                ActivityRecord obj = new ActivityRecord(new Date(lastMilliSeconds + i * 1000), "Sleeping");
                write_to_file(obj);
                activityRecordArrayList.add(obj);
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {


                        Toast.makeText(getApplicationContext(), "Wrote activity Sleeping", Toast.LENGTH_LONG).show();
                    }
                });
                return "sleeping";
//                    break;
            }
        }

                numberOfSamplesZ = 0; //If number of samples are not enough to detect that activity is "Walking"
                numberOfSamplesY = 0; //If number of samples are not enough to detect that activity is "Walking"







        //Write to SD Card

        return "Done";
    }
    */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class LocalBinder extends Binder {

        DataCollectorService getService()
        {
            return DataCollectorService.this;
        }

    }
    public void initExternalStorage(){
        File sdDir;
        String state = Environment.getExternalStorageState();

        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        //sdDir=this.getExternalFilesDir();
        File dir = new File (sdDir.getAbsolutePath() + "/project1");
        Log.d("FILE_PATH", sdDir.getAbsolutePath().toString());
        dir.mkdirs();
        file = new File(dir, "myData.txt");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void write_to_file(ActivityRecord activityRecordObj){


        try {

            FileWriter fw = new FileWriter(file.getPath(),true);
            fw.write(activityRecordObj.getTimeStamp() + "  " + activityRecordObj.getActivity()+"\n" );
            fw.flush();
            fw.close();

            Log.e("WRITING_TOFILE","written to file");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("File_Not_Found", "File not found. Add permissions if not added");
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
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
        unfiltered_accelerometer_data = new ArrayList<SensorDataModel>();
        activityRecordArrayList = new ArrayList<ActivityRecord>();
        count = 0;
        initExternalStorage();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this,accelerometer);
        sensorManager.unregisterListener(this,gyroscope);
    }
}

package com.mobilecomputing.mandarandash.activityrecongnition;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{

    private static final String FILEPATH = "" ;
    private DataCollectorService dataCollectorService;
    private Button buttonBind,buttonUnBind;
    private TextView textView;
    private EditText editTextX,editTextY,editTextZ,editTextM;
    private Boolean isBound;
    private Button buttonGetData;
    private Handler myHandler;
    private ArrayList<String> listOfRecords;
    private ListView listActivityView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonBind = (Button)findViewById(R.id.buttonBind);
        buttonUnBind = (Button)findViewById(R.id.buttonUnBind);
        buttonGetData = (Button)findViewById(R.id.buttonGetData);
        myHandler = new Handler();

        editTextX = (EditText)findViewById(R.id.editTextX);
        editTextY = (EditText)findViewById(R.id.editTextY);
        editTextZ = (EditText)findViewById(R.id.editTextZ);
        editTextM = (EditText)findViewById(R.id.editTextM);
        listActivityView = (ListView)findViewById(R.id.listActivityView);
        isBound = false;

        buttonBind.setOnClickListener(this);
        buttonUnBind.setOnClickListener(this);
        buttonGetData.setOnClickListener(this);
        listOfRecords = new ArrayList<String>();
        readActivityFile(FILEPATH);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,listOfRecords);
        listActivityView.setAdapter(adapter);


    }

    private void readActivityFile(String filePath) {
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
        File file = new File(dir, "myData.txt");
        Scanner in = null;
        String record;

        try
        {
            in = new Scanner(new FileReader(file.getPath()));
            while(in.hasNextLine())
            {
                record = in.nextLine();
                listOfRecords.add(0,record);
                if(listOfRecords.size() > 10)
                {
                    //remove last as we want list to hold most recent 10 activities
                    listOfRecords.remove(listOfRecords.size()-1);

                }

                //Show on UI
            }
        }

        catch(Exception e){
            e.printStackTrace();
        }finally {
            if(in != null)
            in.close();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.buttonBind:
                Log.d("Button clicked...Binding Service","Mandar");
                Intent intent = new Intent(this,DataCollectorService.class);
                bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
                break;
            case R.id.buttonGetData:
                if(isBound){

                    myHandler = new Handler(){

                        @Override
                        public void handleMessage(Message msg) {
                            // TODO Auto-generated method stub
                            super.handleMessage(msg);
                            if(msg != null) {
                                Bundle bundle = msg.getData();
                                float[] data = bundle.getFloatArray("data");
//                                String activityString = bundle.getString("activity");
//                                listOfRecords.add(0,activityString);
//                                listActivityView.setAdapter(adapter);
                                editTextX.setText(Float.toString(data[0]));
                                editTextY.setText(Float.toString(data[1]));
                                editTextZ.setText(Float.toString(data[2]));
                                editTextM.setText(Float.toString(data[3]));
//                            Toast.makeText(protectionService.this, "5 secs has passed", Toast.LENGTH_SHORT).show();
                            }
                        }

                    };
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {

                        @Override
                        public void run() {
                            // Do your task
                            ActivityRecord activityObj = dataCollectorService.getActivity();
                            StringBuffer buffer = new StringBuffer();
                            buffer.append(activityObj.getTimeStamp()).append(" ").append(activityObj.getActivity());
                            listOfRecords.add(0,buffer.toString());
                            if(listOfRecords.size() > 10){
                                listOfRecords.remove(listOfRecords.size() - 1);
                            }
                            myHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listActivityView.setAdapter(adapter);
                                }
                            });


                        }

                    }, 30*1000, 30*1000);

                    new Thread(new Runnable(){
                        public void run() {
                            // TODO Auto-generated method stub
                            while(true)
                            {
                                try {
                                    Thread.sleep(500);
                                    Message msg =  new Message();
                                    Bundle bundle = new Bundle();
                                    float data[] = dataCollectorService.getSensorData();

                                    bundle.putFloatArray("data",data);

                                    msg.setData(bundle);
                                    myHandler.sendMessage(msg);


                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            }

                        }
                    }).start();
//                        myHandler.post(new Runnable() {
//
//                            @Override
//                            public void run() {
//
//                                for (int i=0;i<10000;i++) {
//                                    float data[] = dataCollectorService.getSensorData();
////                                editTextX.setText(data.get(i).getX());
//                                    editTextX.setText(Float.toString(data[0]));
//                                    editTextY.setText(Float.toString(data[1]));
//                                    editTextZ.setText(Float.toString(data[2]));
//                                    myHandler.postDelayed(this,1000);
//                                }
//                            }
//                        });

                }
                break;
            case R.id.buttonUnBind:
                if(isBound){
                    unbindService(serviceConnection);
                    isBound = false;
                }

                break;
        }

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DataCollectorService.LocalBinder localBinder = (DataCollectorService.LocalBinder) service;
            dataCollectorService = localBinder.getService();
            isBound = true;
            //Set Text view
            Log.d("Connecetd","Mandar");
            dataCollectorService.startSensors();




        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

}

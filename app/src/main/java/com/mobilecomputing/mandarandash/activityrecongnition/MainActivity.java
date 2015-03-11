package com.mobilecomputing.mandarandash.activityrecongnition;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{

    private DataCollectorService dataCollectorService;
    private Button buttonBind,buttonUnBind;
    private TextView textView;
    private EditText editTextX,editTextY,editTextZ;
    private Boolean isBound;
    private Button buttonGetData;
    private Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonBind = (Button)findViewById(R.id.buttonBind);
        buttonUnBind = (Button)findViewById(R.id.buttonUnBind);
        buttonGetData = (Button)findViewById(R.id.buttonGetData);
        myHandler = new Handler();
        textView = (TextView)findViewById(R.id.textView);
        editTextX = (EditText)findViewById(R.id.editTextX);
        editTextY = (EditText)findViewById(R.id.editTextY);
        editTextZ = (EditText)findViewById(R.id.editTextZ);
        isBound = false;

        buttonBind.setOnClickListener(this);
        buttonUnBind.setOnClickListener(this);
        buttonGetData.setOnClickListener(this);
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
                    dataCollectorService.startSensors();

                    final float data[] = dataCollectorService.getSensorData();
                    myHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            editTextX.setText(Float.toString(data[0]));
                            editTextY.setText(Float.toString(data[1]));
                            editTextZ.setText(Float.toString(data[2]));
                        }
                    });

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
            textView.setText(dataCollectorService.test_method());

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

}

package com.mobilecomputing.mandarandash.activityrecongnition;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class DataCollectorService extends Service {

    public IBinder myBinder = new LocalBinder();
    public DataCollectorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myBinder;
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
}

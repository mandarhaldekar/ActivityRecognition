package com.mobilecomputing.mandarandash.activityrecongnition;

import java.util.Date;

/**
 * Created by Mandar on 3/14/2015.
 */
public class ActivityRecord {
    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public ActivityRecord(String timeStamp, String activity) {
        this.timeStamp = timeStamp;
        this.activity = activity;
    }

    private String timeStamp;
    private String activity;
}

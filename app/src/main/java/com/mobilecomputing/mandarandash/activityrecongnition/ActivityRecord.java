package com.mobilecomputing.mandarandash.activityrecongnition;

import java.util.Date;

/**
 * Created by Mandar on 3/14/2015.
 */
public class ActivityRecord {
    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public ActivityRecord(Date timeStamp, String activity) {
        this.timeStamp = timeStamp;
        this.activity = activity;
    }

    private Date timeStamp;
    private String activity;
}

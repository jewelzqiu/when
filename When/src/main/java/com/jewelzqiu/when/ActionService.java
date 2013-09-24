package com.jewelzqiu.when;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by jewelzqiu on 9/24/13.
 */
public class ActionService extends Service {

    public static final int TURN_AIRPLANE_MODE_ON = 0;
    public static final int TURN_AIRPLANE_MODE_OFF = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int eventType = intent.getIntExtra(EventsFragment.EVENT_TYPE, -1);
        int action = intent.getIntExtra(EventsFragment.ACTION_TYPE, -1);
        System.out.println(eventType + " " + action);
        final Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

            switch (action) {
                case TURN_AIRPLANE_MODE_ON:
                    outputStream.writeBytes("settings put global airplane_mode_on 1\n");
                    outputStream.flush();
                    outputStream.writeBytes(
                            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true\n");
                    outputStream.flush();
                    break;

                case TURN_AIRPLANE_MODE_OFF:
                    outputStream.writeBytes("settings put global airplane_mode_on 0\n");
                    outputStream.flush();
                    outputStream.writeBytes(
                            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false\n");
                    outputStream.flush();
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (action != -1 && eventType == TimeEventsFragment.EVENT_TYPE_TIME) {
            TimeEventsFragment.calculateNextEvent(getApplicationContext());
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}

package com.teardesign.awear;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class MyService extends Service {

    private static final int REQUEST_CODE = 0;
    private final Handler handler = new Handler();
    private BroadcastReceiver screenOnReceiver;

    public MyService() {
        handler.postDelayed(refresher, 5000);

        screenOnReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Some action
                Log.d("SERVICEonReceive", "Running...");
            }
        };

        //registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));

        //registerAlarm(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(refresher);
        unregisterReceiver(screenOnReceiver);
    }

    private final Runnable refresher = new Runnable() {
        public void run() {
            Log.d("SERVICErun", "Running...");
        }
    };

    public static void registerAlarm(Context context) {
        Intent i = new Intent(context, ServiceAutoStarter.class);

        PendingIntent sender = PendingIntent.getBroadcast(context, REQUEST_CODE, i, 0);

        // We want the alarm to go off 3 seconds from now.
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 3 * 1000;//start 3 seconds after first register.

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 10000, sender);//10sec interval

    }

    public class ServiceAutoStarter extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, MyService.class));
            Log.d("SERVICEauto", "Running...");
        }
    }
}

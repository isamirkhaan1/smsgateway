package data;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.samirkhan.apps.autosender.R;

public class DownloadService extends Service {

    //Context context;
    public static Boolean isThreadRunning = false;
    int SYNC_THREAD_TIMER = 1000 * 30;
    int SMS_THREAD_TIMER = 1000 * 5;
    ServerManager serverManager;
    data.DBHelper dbHelper;
    String[] data;
    String tempQuery;
    int smsNotSent = 0;

    public DownloadService() {
        // this.context = context;
    }

    /*  THIS IS THE MAIN POOL,
    *   2 threads are created, one to retreive
    *   new records from server and put it in a local...
    *   second one get one by one sms from local and send it
    *   to the given number..  */
    @Override
    public void onCreate() {

        isThreadRunning = true;

        Thread syncThread = new Thread() {
            @Override
            public void run() {

                while (isThreadRunning) {

                    // startHttpRequest in background, also put those new records
                    // to local also..
                    serverManager = new ServerManager(getBaseContext());
                    serverManager.execute();
                    Log.d("Thread 1", "Running");

                    // Sleep the thread for ..
                    try {
                        Thread.sleep(SYNC_THREAD_TIMER);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // play a tune if thread gonna stop, i-e: while loop ended..
                MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.tune);
                mediaPlayer.start();
                mediaPlayer.release();
            }
        };
        // start thread to start syncing data..
        syncThread.start();

        Thread sendThread = new Thread() {
            @Override
            public void run() {

                dbHelper = new data.DBHelper(getBaseContext());

                while (isThreadRunning) {

                    // get sms from local
                    data = dbHelper.topSMS();

                    // check if record isn't empty, i-e: no new sms
                    if (data != null && data[0] != null) {

                        // smsManager instance and SentPI which fire when sms sent..
                        SmsManager smsManager = SmsManager.getDefault();
                        PendingIntent sentPI = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent("SENT"), 0);

                        // check whether sms sent successfully or not..
                        registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                switch (getResultCode()) {

                                    // if it sent the okay, :)
                                    case Activity.RESULT_OK:
                                        break;

                                    default:

                                        /* if sms have tried for 4 time and had not sent,
                                        *  that means, there is an error, either in network
                                        *  or credablity.. so stop thread and show toast   */
                                        if (smsNotSent > 4) {

                                            DownloadService.isThreadRunning = false;
                                        }

                                        tempQuery = "update sms set status = 0 where id = " + data[0];
                                        dbHelper.execute(tempQuery);
                                        Toast.makeText(getBaseContext(), "error ", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }, new IntentFilter("SENT"));

                        // send sms
                        smsManager.sendTextMessage(data[1], null, data[2], sentPI, null);

                    }
                    Log.d("Thread 2", "Running");

                    // stop thread for..
                    try {
                        Thread.sleep(SMS_THREAD_TIMER);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                // play a tune when the thread stopped
                MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.tune);
                mediaPlayer.start();
            }
        };
        sendThread.start();
    }


    // show intent when service started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, getResources().getString(R.string.app_name) + " started", Toast.LENGTH_SHORT).show();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    // show intent when service  ended..
    @Override
    public void onDestroy() {
        isThreadRunning = false;
        Toast.makeText(this, getResources().getString(R.string.app_name) + " finished", Toast.LENGTH_SHORT).show();
    }

    // actually i do not end binding..
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}

package com.rosalinesrandoms.thingspeak;

        import android.app.Service;
        import android.content.Intent;
        import android.os.IBinder;

/**
 * Created by Diane on 21/02/2015.
 */
public class BatteryInfoService extends Service {

    SendBatteryMessaging sendBatteryMessaging = new SendBatteryMessaging();

    /**
     * Create the service - call super create
     */
    public void onCreate() {
        super.onCreate();
    }

    /**
     * When the Service is started, start the first alarm to send data
     *
     * @param intent - How it was started
     * @param flags - Any flags to start
     * @param startId - Id of the start
     *                All params are set when Intent is called
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendBatteryMessaging.SetAlarm(BatteryInfoService.this);
        return START_STICKY;
    }

    /**
     * Stop the alarm sending when the service is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBatteryMessaging.CancelAlarm(BatteryInfoService.this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
package com.rosalinesrandoms.thingspeak;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A class that is woken up regularly even if the Activity is not in focus to send data to the server.
 * As a note of caution, the class is killed and rebuild every time onResume is called, and all variables are cleared.
 */
public class SendBatteryMessaging extends BroadcastReceiver  implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{

    //Classes to find the location of the cell phone
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    //Location of the cell phone
    private static Location location;

    //Location variables
    private static final long INTERVAL = 1000 * 30;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final long ONE_MIN = 1000 * 60;
    private static final long REFRESH_TIME = ONE_MIN * 5;
    private static final float MINIMUM_ACCURACY = 50.0f;

    //Variables for controlling the alarm
    private AlarmManager am;
    private PendingIntent pi;

    private static final String API = "8K4VLBWHREM9K04X";

    /**
     * Because the class is removed every call, re-initialize the Location services
     * @param context - the context the Service is running in
     */
    public void findLocation(Context context){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement((float).001);

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    /**
     * Called every time the alarm times out.  Re-finds the location, and sends the sensor information to the cloud
      * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //Get power to wake the phone up temperarily
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        //Re-find the location on every call as it has been destroyed
        findLocation(context);

        send(context);

        if(location!= null)
            System.out.println("onRecieve longitude is "+location.getLatitude());
        else
            System.out.println("onRecieve longitude is null");

        wl.release();
    }

    public void send(Context context){

        //TODO: why is location always null here?
        //if(location == null)
        //    return;
        int screenOn = 0;

        //find out if the screen is on
        PowerManager powerManager;
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
// If you use API20 or more:
        if (powerManager.isInteractive()){ screenOn = 1; }

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = (level / (float)scale) * 100;

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int isCharging = 0;
        if(status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL)
            isCharging = 1;

        RequestParams params = new RequestParams();
        params.put("api_key", API);
        params.put("field1", batteryPct);
        params.put("field2", isCharging);
        params.put("field3", screenOn);
        if(location != null) {
            params.put("long", location.getLongitude());
            params.put("lat", location.getLatitude());
            params.put("elevation", location.getAltitude());
        }
        AsyncHttpClient client = new AsyncHttpClient();

        client.post("https://api.thingspeak.com/update", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    System.out.println("Success! - sent data" );
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error)
            {
                error.printStackTrace(System.out);
            }
        });
    }

    public void SetAlarm(Context context)
    {

        am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent("com.rosalinesrandoms.thingspeak.START_ALARM");

        pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 1 , pi); // Millisec * Second * Minute
        Toast.makeText(context, "Capture", Toast.LENGTH_LONG).show(); // For example

        findLocation(context);
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, SendBatteryMessaging.class);
        am.cancel(pi);
    }


    @Override
    public void onConnected(Bundle bundle) {
        Location currentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);
        if (currentLocation != null && currentLocation.getTime() > REFRESH_TIME) {
            this.location = currentLocation;
        } else {
            fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            // Schedule a Thread to unregister location listeners
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                @Override
                public void run() {
                    fusedLocationProviderApi.removeLocationUpdates(googleApiClient,
                            SendBatteryMessaging.this);
                }
            }, ONE_MIN, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //if the existing location is empty or
        //the current location accuracy is greater than existing accuracy
        //then store the current location
        if (null == this.location || location.getAccuracy() < this.location.getAccuracy()) {
            this.location = location;
            //send();

            //if the accuracy is not better, remove all location updates for this listener
            if (this.location.getAccuracy() < MINIMUM_ACCURACY) {
                fusedLocationProviderApi.removeLocationUpdates(googleApiClient, this);
            }

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
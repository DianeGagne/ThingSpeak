package com.rosalinesrandoms.thingspeak;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Calendar;


public class StartActivity extends ActionBarActivity {

    Button StartButton;
    Button StopButton;

    PendingIntent intent;
    AlarmManager alarmMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        //Get access to our two buttons and bind them to their actions
        StartButton = (Button) findViewById(R.id.start_button);
        StopButton = (Button) findViewById(R.id.stop_button);

        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // use this to start and trigger a service
                startSending();
            }
        });
        StopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // use this to stop the service
                stopSending();
            }
        });


    }

    /**
     * Initialize and start the Service to send all data to thingspeak
     */
    private void startSending() {
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(this, BatteryInfoService.class);
        this.intent = PendingIntent.getService(this, 0, intent, 0);
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                cal.getTimeInMillis(), 30 * 1000, this.intent);
        this.getBaseContext().startService(intent);
    }

    /**
     * Take the initialized data and stop the Service
     */
    private void stopSending() {
        Intent intent = new Intent(this, BatteryInfoService.class);
        this.intent = PendingIntent.getService(this, 0, intent, 0);
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        stopService(intent);
        alarmMgr.cancel(this.intent);
    }

    /**
     * Check if Google Play Services is around
     *
     * @return true if it is available
     */
    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    /**
     * Setup the menu for the activity (is empty)
     *
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    /**
     * Deal with any memu items - must be here
     *
     * @param item
     * @return true if item was connected
     */
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
}

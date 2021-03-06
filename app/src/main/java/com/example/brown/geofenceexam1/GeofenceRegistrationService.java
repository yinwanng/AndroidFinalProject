package com.example.brown.geofenceexam1;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeofenceRegistrationService extends IntentService {
    private static final String TAG = "GeofenceRegistrationSer";
    public GeofenceRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "GeofencingEvent error " + geofencingEvent.getErrorCode());
        } else {
            int transaction = geofencingEvent.getGeofenceTransition();
//            List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
//            Geofence geofence = geofences.get(0);
//            if (transaction == Geofence.GEOFENCE_TRANSITION_ENTER && geofence.getRequestId().equals(Constants.GEOFENCE_ID_BCIT)) {
//                Log.d(TAG, "You are inside.");
//                Handler mHandler = new Handler(getMainLooper());
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), "You are inside of BCIT!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } else {
//                Log.d(TAG, "You are outside.");
//                Handler mHandler = new Handler(getMainLooper());
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), "You are outside of BCIT", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }

            if (transaction == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(TAG, "You are inside.");
                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MapsActivity.enterNotification();
                        Toast toast = Toast.makeText(getApplicationContext(), "ENTERED a Speed Limit Zone.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                    }
                });
            } else {
                Log.d(TAG, "You are outside.");
                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MapsActivity.exitNotification();
                        Toast toast = Toast.makeText(getApplicationContext(), "EXITED from a Speed Limit Zone.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();

                    }
                });
            }
        }
    }
}

//outside
//49.2339127,-122.8943625
//inside
//49.232880839794674
//-122.89251090019533
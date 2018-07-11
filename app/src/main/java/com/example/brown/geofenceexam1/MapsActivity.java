package com.example.brown.geofenceexam1;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
                GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener,
                GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsActivity";
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 101;

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private MarkerOptions markerOptions;
    private Marker currentLocationMarker;
    private PendingIntent pendingIntent;
    private GeofencingRequest geofencingRequest;

    private RequestQueue mQueue;
    private List<Geofence> geofences;
    private List<LatLng> coordinates;
    private int numberOfGeofences = 0;
    private int GEOFENCE_LIMIT = 100;

    private static TextToSpeech textToSpeech;
    private Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mQueue = Volley.newRequestQueue(this);
        jsonParse();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
        }

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        LatLng latLng = Constants.AREA_PLACES.get(Constants.GEOFENCE_ID_BCIT);
        LatLng latLng2 = Constants.AREA_PLACES.get(Constants.GEOFENCE_ID_KNOWLEDGE_NETWORK);
        mMap.addMarker(new MarkerOptions().position(latLng).title("BCIT"));
        mMap.addMarker(new MarkerOptions().position(latLng2).title("Knowledge Networks"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));

        mMap.setMyLocationEnabled(true);

//        Circle circle = googleMap.addCircle(new CircleOptions()
//                .center(new LatLng(latLng.latitude, latLng.longitude))
//                .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
//                .strokeColor(Color.RED)
//                .strokeWidth(4f));
//
//        googleMap.addCircle(new CircleOptions()
//                .center(new LatLng(latLng2.latitude, latLng2.longitude))
//                .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
//                .strokeColor(Color.BLUE)
//                .strokeWidth(4f));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google Api Client Connected.");
        startGeofencing();
        startLocationMonitor();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Connection Suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection Failed:" + connectionResult.getErrorMessage());
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.reconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopGeofencing();
        googleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);
        if (response != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Service Is Not Available");
            GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, response, 1).show();
        } else {
            Log.d(TAG, "Google Play Service Is Available");
        }
    }

    private void startLocationMonitor() {
        Log.d(TAG, "Start Location Monitor");

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(2000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (currentLocationMarker != null) {
                        currentLocationMarker.remove();
                    }
                    markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                    markerOptions.title("Current Location");
                    currentLocationMarker = mMap.addMarker(markerOptions);
                    Log.d(TAG, "Location Change Lat Lng " + location.getLatitude() + " " + location.getLongitude());
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17f));
                }
            });
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @NonNull
    private Geofence getGeofence() {
        LatLng latLng = Constants.AREA_PLACES.get(Constants.GEOFENCE_ID_BCIT);
        return new Geofence.Builder()
                .setRequestId(Constants.GEOFENCE_ID_BCIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(latLng.latitude, latLng.longitude, Constants.GEOFENCE_RADIUS_IN_METERS)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private List<Geofence> getList() {
        LatLng latLng = Constants.AREA_PLACES.get(Constants.GEOFENCE_ID_BCIT);
        LatLng latLng2 = Constants.AREA_PLACES.get(Constants.GEOFENCE_ID_KNOWLEDGE_NETWORK);

        List<Geofence> geofences = new ArrayList<>();
        geofences.add(new Geofence.Builder()
                .setRequestId(Constants.GEOFENCE_ID_BCIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(latLng.latitude, latLng.longitude, Constants.GEOFENCE_RADIUS_IN_METERS)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        geofences.add(new Geofence.Builder()
                .setRequestId(Constants.GEOFENCE_ID_KNOWLEDGE_NETWORK)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(latLng2.latitude, latLng2.longitude, Constants.GEOFENCE_RADIUS_IN_METERS)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
        return geofences;
    }

    private PendingIntent getGeofencePendingIntent() {
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceRegistrationService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private void startGeofencing() {
        Log.d(TAG, "Start Geofencing Monitoring Call");
        pendingIntent = getGeofencePendingIntent();
        //geofencingRequest = new GeofencingRequest.Builder().setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER).addGeofences(geofences).build();
        Log.d(TAG, "startGeofencing errors: " + Arrays.toString(geofences.toArray()));
        Log.d(TAG, "startGeofencing errors size: " + geofences.size());
        geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                .addGeofences(geofences)
                .build();

        for(LatLng latLng : coordinates) {
            mMap.addMarker(new MarkerOptions()
                    .position(latLng).title("Speed Sign Information: \n" + "Latitude: " + latLng.latitude + "\nLongitude: " + latLng.longitude))
                    .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latLng.latitude, latLng.longitude))
                    .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
                    .strokeColor(Color.RED)
                    .strokeWidth(4f));
        }



        Log.d(TAG, "startGeofencing: geofence size " + geofencingRequest.getGeofences().size());

//        Log.d(TAG, "startGeofencing: " + Arrays.toString(geofences.toArray()));
        if (!googleApiClient.isConnected()) {
            Log.d(TAG, "Google API client not connected");
        } else {
            try {
                LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "Successfully Geofencing Connected");

                        } else {
                            Log.d(TAG, "Failed to add Geofencing " + status.getStatus());
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    private void jsonParse() {
        Log.d(TAG, "jsonParse starting: ");
        geofences = new ArrayList<>();
        coordinates = new ArrayList<>();
        String url = "http://opendata.newwestcity.ca/downloads/speed-signs/SPEED_SIGNS_AND_TABS.json";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("features");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject locations = jsonArray.getJSONObject(i);
                                JSONObject properties = locations.getJSONObject("properties");
                                String xCoordinate = properties.getString("X");
                                String yCoordinate = properties.getString("Y");
                                //Log.d(TAG, "onResponse: " + xCoordinate + ", " + yCoordinate);

                                LatLng place = new LatLng(Double.parseDouble(yCoordinate), Double.parseDouble(xCoordinate));
//                                mMap.addMarker(new MarkerOptions().position(place).title("Speed Signs" + place.latitude +", " + place.longitude));

                                if (numberOfGeofences < GEOFENCE_LIMIT) {

//                                    mMap.addMarker(new MarkerOptions().position(place).title("Speed Signs" + place.latitude + ", " + place.longitude));
                                    geofences.add(new Geofence.Builder()
                                            .setRequestId(UUID.randomUUID().toString())
                                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                            .setCircularRegion(place.latitude, place.longitude, Constants.GEOFENCE_RADIUS_IN_METERS)
                                            .setNotificationResponsiveness(1000)
                                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                                            .build());

                                    coordinates.add(new LatLng(place.latitude, place.longitude));


//                                    Log.d(TAG, "onResponse: check geofence" + geofences.get(counter));
//                                    mMap.addCircle(new CircleOptions()
//                                            .center(new LatLng(place.latitude, place.longitude))
//                                            .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
//                                            .strokeColor(Color.BLUE)
//                                            .strokeWidth(4f));
                                }
                                numberOfGeofences++;

                                //Log.d(TAG, "onResponse: " + Arrays.toString(geofences.toArray()));
//                                mMap.addCircle(new CircleOptions()
//                                        .center(new LatLng(place.latitude, place.longitude))
//                                        .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
//                                        .strokeColor(Color.BLUE)
//                                        .strokeWidth(4f));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    private void stopGeofencing() {
        pendingIntent = getGeofencePendingIntent();
        LocationServices.GeofencingApi.removeGeofences
                (googleApiClient, pendingIntent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess())
                            Log.d(TAG, "Stop geofencing");
                        else
                            Log.d(TAG, "Not stop geofencing");
                    }
                });
    }

    public static void enterNotification() {
        String enter = "You have entered a speed limit zone.";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(enter, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(enter, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public static void exitNotification() {
        String exit = "You have exited from the speed limit zone.";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(exit, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(exit, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: you did it!" );


        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(marker.getTitle())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();

        return false;
    }
}

package com.example.brown.geofenceexam1;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class Constants {
    public static final String GEOFENCE_ID_BCIT= "BCIT";
    public static final String GEOFENCE_ID_KNOWLEDGE_NETWORK = "KNOWLEDGENETWORKS";
    // For best results, min radius of geofence should be set between 100 - 150 meters
    // When Wi-Fi is available, location accuracy is usually between 20 - 50 meters
    public static final float GEOFENCE_RADIUS_IN_METERS = 60;

    public static final HashMap<String, LatLng> AREA_PLACES = new HashMap<String, LatLng>();

    static {
        // bcit area
        AREA_PLACES.put(GEOFENCE_ID_BCIT, new LatLng(49.2502195, -123.0031086));
        AREA_PLACES.put(GEOFENCE_ID_KNOWLEDGE_NETWORK, new LatLng(49.2456216,-122.998905));

        // inside bcit
        // -123.0031086
        // 49.2502195

        // outside bcit
        // -123.0039377
        // 49.251539

        //49.2042206997765
        //-122.94272939995822

        //49.20407268040233
        //-122.94347820051226

        // lord tweedsmuir elementary
        //-122.9425536
        //49.2056697

        //49.232881, -122.892511
        //49.236368, -122.892408

    }
}

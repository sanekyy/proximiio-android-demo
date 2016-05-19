package io.proximi.proximiiodemo;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import io.proximi.proximiiolibrary.Proximiio;
import io.proximi.proximiiolibrary.ProximiioFactory;
import io.proximi.proximiiolibrary.ProximiioGeofence;
import io.proximi.proximiiolibrary.ProximiioListener;

/**
 * Proximiio Demo
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;
    private boolean zoomed;
    private LocationSource.OnLocationChangedListener locationListener;
    private boolean locationEnabled;
    private Proximiio proximiio;
    private ProximiioListener listener;

    private final static String TAG = "ProximiioDemo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Create a Proximiio instance
        proximiio = ProximiioFactory.getProximiio(this, this);

        // Create a ProximiioListener and add it to Proximiio
        listener = new ProximiioListener() {
            @Override
            public void geofenceEnter(ProximiioGeofence geofence) {
                Log.d(TAG, "Geofence enter: " + geofence.getName());
            }

            @Override
            public void geofenceExit(ProximiioGeofence geofence) {
                Log.d(TAG, "Geofence exit: " + geofence.getName());
            }

            @Override
            public void position(double lat, double lon, double accuracy) {
                setPosition(lat, lon, accuracy);
            }

            @Override
            public void loginFailed(LoginError loginError) {
                Log.e(TAG, "LoginError! (" + loginError.toString() + ")");
            }
        };
        proximiio.addListener(listener);

        // Login to Proximi.io
        proximiio.setLogin(EMAIL, PASSWORD);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        proximiio.removeListener(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        proximiio.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        proximiio.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Called when the map is ready to use
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        tryEnableLocation();

        // Set the location source of the map to Proximiio instead of the native positioning
        map.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                locationListener = onLocationChangedListener;
            }

            @Override
            public void deactivate() {
                locationListener = null;
            }
        });
    }

    // Make sure we have sufficient permissions under Android 6.0 and later
    private void tryEnableLocation() {
        try {
            map.setMyLocationEnabled(true);
            locationEnabled = true;
        }
        catch (SecurityException e) {
            Log.e(TAG, "No permissions for positioning! (ProximiioService should be asking for them!)");
        }
    }

    // Set the position obtained from Proximiio to be our current position on the map
    private void setPosition(double lat, double lon, double accuracy) {
        if (map != null) {

            if (!locationEnabled) {
                tryEnableLocation();
            }

            // First time zoom to focus the map on the current location
            if (!zoomed) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 18));
                zoomed = true;
            }

            // Update our location on the map
            if (locationListener != null && locationEnabled) {
                Location location = new Location("Proximiio");
                location.setLatitude(lat);
                location.setLongitude(lon);
                location.setAccuracy((float)accuracy);
                locationListener.onLocationChanged(location);
            }
        }
    }
}

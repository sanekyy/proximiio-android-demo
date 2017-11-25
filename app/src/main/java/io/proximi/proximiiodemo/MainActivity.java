package io.proximi.proximiiodemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.concurrent.TimeUnit;

import io.proximi.proximiiolibrary.ProximiioAPI;
import io.proximi.proximiiolibrary.ProximiioFloor;
import io.proximi.proximiiolibrary.ProximiioGeofence;
import io.proximi.proximiiolibrary.ProximiioGoogleMapHelper;
import io.proximi.proximiiolibrary.ProximiioListener;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Proximiio Demo
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ProximiioAPI proximiioAPI;
    @Nullable private ProximiioGoogleMapHelper mapHelper;
    private Toolbar toolbar;

    MyService mService;

    private static final String TAG = "ProximiioDemo";

    public static final String AUTH = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImlzcyI6IjRiMGQ2MmJhLWMxOTctNGI4Ni04OWZlLWE4OTRhMGU4MzBhZCIsInR5cGUiOiJhcHBsaWNhdGlvbiIsImFwcGxpY2F0aW9uX2lkIjoiMjc2MzU5NmUtMmQyYi00YWM5LWE0OTctOGFiZGM0YTY1NTMyIn0.OWQKKNaJHRWGg6uaGhx2ufsq_eGVbjaNs-UjVyJL8_U"; // TODO: Replace with your own!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create our Proximi.io listener
        proximiioAPI = new ProximiioAPI(TAG, this);
        proximiioAPI.setListener(new ProximiioListener() {
            @Override
            public void geofenceEnter(final ProximiioGeofence geofence) {
                final RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), geofence.getName().getBytes());
                mService.enter(requestBody).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        makeNovitiate("Enter in " + geofence.getName() + " OK");
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        makeNovitiate("Enter in " + geofence.getName() + " FAIL");
                        mService.enter(requestBody).enqueue(this);
                    }
                });

            }

            @Override
            public void geofenceExit(final ProximiioGeofence geofence, @Nullable final Long dwellTime) {
                final RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), geofence.getName().getBytes());
                mService.exit(requestBody).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        makeNovitiate("Exit in " + geofence.getName() + " OK");
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        makeNovitiate("Exit in " + geofence.getName() + " FAIL");
                        mService.exit(requestBody).enqueue(this);
                    }
                });
            }

            @Override
            public void loginFailed(LoginError loginError) {
                Log.e(TAG, "LoginError! (" + loginError.toString() + ")");
            }
        });
        proximiioAPI.setAuth(AUTH);
        proximiioAPI.setActivity(this);

        // Set toolbar buttons to change the current floor up and down
        findViewById(R.id.floorUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapHelper != null) {
                    mapHelper.floorUp();
                }
            }
        });
        findViewById(R.id.floorDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapHelper != null) {
                    mapHelper.floorDown();
                }
            }
        });

        // Set the toolbar title
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(TAG);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        mService = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://31.134.153.175:8080")
                .build().create(MyService.class);
    }



    @Override
    protected void onDestroy() {
        System.out.println("destroy");
        super.onDestroy();
        if (mapHelper != null) {
            mapHelper.destroy();
        }
        proximiioAPI.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        proximiioAPI.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        proximiioAPI.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Called when the map is ready to use
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapHelper = new ProximiioGoogleMapHelper.Builder(this, googleMap)
                .listener(new ProximiioGoogleMapHelper.Listener() {
                    @Override
                    public void changedFloor(@Nullable ProximiioFloor floor) {
                        toolbar.setTitle(floor != null ? floor.getName() : TAG);
                    }
                }).build();

        googleMap.setOnMyLocationButtonClickListener(mapHelper);
        googleMap.setOnMapClickListener(mapHelper);
        googleMap.setOnCameraIdleListener(mapHelper);
        googleMap.setOnMarkerClickListener(mapHelper);
        googleMap.setOnCameraMoveStartedListener(mapHelper);
    }

    public void makeNovitiate(String text){
        Intent intent2 = new Intent(this.getApplicationContext(), MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager notificationManager = (NotificationManager) this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.getApplicationContext())
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        Notification notification = notificationBuilder.build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, notification);
    }
}

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
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.proximi.proximiiolibrary.ProximiioAPI;
import io.proximi.proximiiolibrary.ProximiioGeofence;
import io.proximi.proximiiolibrary.ProximiioListener;

class MainActivityListener extends ProximiioListener {
    private ProximiioAPI proximiioAPI;
    @Nullable private MainActivity main;
    private Context context; // This one is ok to keep on background because it's the application context.

    private static final String ID = "MainActivityListener";

    // Create a new API object with our ID.
    // This ID will be used to replace any existing listener with the same ID.
    // This is useful when we exit to background and come back to create a new listener.
    MainActivityListener(@NonNull MainActivity mainActivity) {
        main = mainActivity;
        context = mainActivity.getApplicationContext();
        proximiioAPI = new ProximiioAPI(ID, mainActivity);
        proximiioAPI.setActivity(mainActivity);
        proximiioAPI.setListener(this);
        proximiioAPI.setAuth(AUTH_KEY);
    }

    void onDestroy() {
        // Set to null to avoid memory leaks.
        main = null;

        // Destroy this API object.
        // Parameter true indicates that we'd like to keep our listener working on the background.
        proximiioAPI.destroy(true);
    }

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        proximiioAPI.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        proximiioAPI.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void geofenceEnter(ProximiioGeofence geofence) {
        Log.d(ID, "Geofence enter: " + geofence.getName());
    }

    @Override
    public void geofenceExit(ProximiioGeofence geofence, @Nullable Long dwellTime) {
        Log.d(ID, "Geofence exit: " + geofence.getName() + ", dwell time: " + String.valueOf(dwellTime));
    }

    @Override
    public void loginFailed(LoginError loginError) {
        Log.e(ID, "LoginError! (" + loginError.toString() + ")");
    }

    // This callback implementation demonstrates push notifications.
    @Override
    public void output(JSONObject json) {
        String title = null;
        try {
            if (!json.isNull("type") && !json.isNull("title")) {
                if (json.getString("type").equals("push")) {
                    title = json.getString("title");
                }
            }
        }
        catch (JSONException e) {
            // Not a push
        }

        if (title != null) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(title);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            }

            Notification notification = notificationBuilder.build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(1, notification);
        }
    }
}

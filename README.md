# Proximiio Demo

Welcome!

This is a simple working demonstration of Proximi.io platform in action on Android.

### How to use

Clone or download this repo and open it with Android Studio as an existing project. After that you need to replace the placeholders in the following section with your credentials (MainActivity.java):

```
// Login to Proximi.io
proximiio.setLogin(EMAIL, PASSWORD);
```

You also need to edit the AndroidManifest.xml and replace the Google API Key:

```
 <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value=YOUR_APIKEY_HERE (https://developers.google.com/maps/documentation/android-api/signup)/>
```

Instructions for obtaining the API key can be found from the following page: https://developers.google.com/maps/documentation/android-api/signup

Feel free to email support@proximi.io.

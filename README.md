# Proximiio Demo

Welcome!

This is a simple working demonstration of Proximi.io platform in action on Android,
that demonstrates basic usage of Proximi.io, usage of Google Maps with Proximi.io, and generic push notifications through Proximi.io.
Our complete Android documentation can be found [here](https://proximi.io/docs/android/).

### How to use

Note: The following two steps are required and the errors you get during the first build are intentional! If you don't define the email/password & Google API key, the build will result in an error. In case you don't want to obtain the Google API key, you can use empty value to get the project to build. In this case the map tiles will not show up but the project will build without error.

Clone or download this repo and open it with Android Studio as an existing project. After that you need to replace the placeholders in the following section with your credentials (MainActivityListener.java):

```
proximiioAPI.setAuth(AUTH_KEY);
```

As an alternative way for logging in, you can also use email & password instead of an auth token:

```
proximiioAPI.setLogin(EMAIL, PASSWORD);
```

You also need to edit the AndroidManifest.xml and replace the Google API Key:

```
 <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value=YOUR_APIKEY_HERE (https://developers.google.com/maps/documentation/android-api/signup)/>
```

Instructions for obtaining the API key can be found from the following page: https://developers.google.com/maps/documentation/android-api/signup

Feel free to email support@proximi.io.

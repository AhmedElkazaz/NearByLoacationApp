package com.example.ahkazzaz.foursquare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.webkit.PermissionRequest;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;

import butterknife.OnClick;
import butterknife.internal.Utils;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {
    JSONObject groupsItem ,venue,location,icon,response= new JSONObject();

    JSONArray categories,itemsArray,groups = new JSONArray();
    String []name,photosId,photo,address;

    JSONObject item = new JSONObject();
    AccessTokenResponse tokenResponse;
    final int REQUEST_CODE_FSQ_CONNECT = 20, REQUEST_CODE_FSQ_TOKEN_EXCHANGE = 10;
    LoaderManager loaderManger;
    Bundle photoBundle = new Bundle();
    Bundle bundle= new Bundle();
    RecyclerView recycler;
    Loader<String> venuaLoader;
    private static final int VENUE_LOADER = 38;
    Loader<String> photoLoader;
    private static final int PHOTO_LOADER = 39;
    private String mLastUpdateTime;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    private static final int REQUEST_CHECK_SETTINGS = 100;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;
    private static final String TAG = MainActivity.class.getSimpleName();
    ImageView img;
    Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        c=this;
         img =findViewById(R.id.load);
        Glide.with(this).load(R.drawable.loading).into(img);

        Intent intent = FoursquareOAuth.getConnectIntent(getApplicationContext(), "PFG3QNOFU1JAEGJSQNI1MYMACAA5U3RZ1CTKXIWJADZ3CPVW");
        startActivityForResult(intent, REQUEST_CODE_FSQ_CONNECT);


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FSQ_CONNECT:
                AuthCodeResponse codeResponse = FoursquareOAuth.getAuthCodeFromResult(resultCode, data);
                Intent intent = FoursquareOAuth.getTokenExchangeIntent(getApplicationContext(), "PFG3QNOFU1JAEGJSQNI1MYMACAA5U3RZ1CTKXIWJADZ3CPVW", "B3STKR4XNPLNP4HQOFXMQWMKHDTTZR4VAT4ASPTBGD2L0MBJ", codeResponse.getCode());
                startActivityForResult(intent, REQUEST_CODE_FSQ_TOKEN_EXCHANGE);
            /* ... */
                break;
            case REQUEST_CODE_FSQ_TOKEN_EXCHANGE: {
                 tokenResponse = FoursquareOAuth.getTokenFromResult(resultCode, data);
                init();
                start();

            /* ... */
                break;
            }

            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                default:
                    break;
        }
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        if(id==VENUE_LOADER)
        {
            DataLoader t1 = new DataLoader(this, args);
            return t1;
        }
        else if(id==PHOTO_LOADER)
        {
            PhotoLoader t1 = new PhotoLoader(this, args);
            return t1;
        }
        else
            return null;
    }

    @Override
    public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {

        if (loader.getId() == VENUE_LOADER) {
            if (data != null) {
                try {

                    response = data.getJSONObject("response");
                    groups = response.getJSONArray("groups");
                    groupsItem = groups.getJSONObject(0);
                    groupsItem = groups.getJSONObject(0);
                    itemsArray = groupsItem.getJSONArray("items");
                    photosId = new String[itemsArray.length()];
                    photo = new String[itemsArray.length()];

                    address = new String[itemsArray.length()];
                    name = new String[itemsArray.length()];
                    if (itemsArray.length() > 0) {
                        for (int i = 0; i < itemsArray.length(); i++) {
                            item = itemsArray.getJSONObject(i);
                            venue = item.getJSONObject("venue");
                            location = venue.getJSONObject("location");
                            name[i] = venue.getString("name");
                            address[i] = location.getString("address");
                            categories = venue.getJSONArray("categories");
                            photosId[i] = venue.getString("id");


                        }

                        photoLoader = loaderManger.getLoader(PHOTO_LOADER);
                        photoBundle.putStringArray("id", photosId);

                        if (photoLoader == null)

                        {
                            loaderManger.initLoader(PHOTO_LOADER, photoBundle, MainActivity.this);

                        } else

                        {
                            loaderManger.restartLoader(PHOTO_LOADER, photoBundle, MainActivity.this);

                        }


                    } else {
                        Glide.with(c).load(R.drawable.no_record).into(img);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Glide.with(c).load(R.drawable.error).into(img);


            }
        } else if (loader.getId() == PHOTO_LOADER) {
            if (data != null) {


                try {
                    itemsArray = data.getJSONArray("res");
                    for (int i = 0; i < itemsArray.length(); i++) {
                        photo[i] = itemsArray.getString(i);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent i = new Intent(MainActivity.this, PlacesActivity.class);
                Bundle dataBundle = new Bundle();
                dataBundle.putStringArray("name", name);
                dataBundle.putStringArray("address", address);
                dataBundle.putStringArray("photo", photo);
                i.putExtra("data", dataBundle);
                startActivity(i);
                finish();
            }

        }




    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader)
    {

    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }

        updateLocationUI();
    }


    /**
     * Update the UI displaying the location data
     * and toggling the buttons
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            bundle.putString("token", tokenResponse.getAccessToken());
            bundle.putString("ll",mCurrentLocation.getLatitude() + "," +
                    + mCurrentLocation.getLongitude());
            loaderManger = getSupportLoaderManager();
            venuaLoader = loaderManger.getLoader(VENUE_LOADER);

            if (venuaLoader == null)

            {
                loaderManger.initLoader(VENUE_LOADER, bundle, MainActivity.this);

            } else

            {
                loaderManger.restartLoader(VENUE_LOADER, bundle, MainActivity.this);

            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putString("last_updated_on", mLastUpdateTime);
        */

    }
/*
    private void toggleButtons() {
        if (mRequestingLocationUpdates) {
            btnStartUpdates.setEnabled(false);
            btnStopUpdates.setEnabled(true);
        } else {
            btnStartUpdates.setEnabled(true);
            btnStopUpdates.setEnabled(false);
        }
    }
    */

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Glide.with(c).clear(img);

                                Glide.with(c).load(R.drawable.error).into(img);
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI();
                    }
                });
    }

    public void start() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();

                    }
                }).check();
    }

    /*
    @OnClick(R.id.btn_stop_location_updates)
    public void stopLocationButtonClick() {
        mRequestingLocationUpdates = false;
        stopLocationUpdates();
    }
    */

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                       // toggleButtons();
                    }
                });
    }






    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
/*
        // Resuming location updates depending on button state and
        // allowed permissions
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }

        updateLocationUI();
        */
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    protected void onPause() {
        super.onPause();

        /*
        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
        */
    }


}

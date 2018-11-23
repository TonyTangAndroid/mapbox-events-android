package com.mapbox.android.events.testapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.telemetry.MapboxTelemetry;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionsListener {
  private final String LOG_TAG = "MainActivity";
  private MapboxTelemetry mapboxTelemetry;
  private PermissionsManager permissionsManager;

  LocationEngine locationEngine;
  private LocationEngineRequest locationEngineRequest =
    new LocationEngineRequest.Builder(1000)
      .setFastestInterval(1000)
      .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
      .build();

  @NonNull
  private LocationEngineCallback<LocationEngineResult> currentLocationEngineListener =
    new LocationEngineCallback<LocationEngineResult>() {
      @Override
      public void onSuccess(LocationEngineResult result) {
        Log.e("LEAK_TEST", "current location: " + result.getLocations());
      }

      @Override
      public void onFailure(@NonNull Exception exception) {
        Log.e("LEAK_TEST", "current location - failure - " + exception);
      }
    };

  @NonNull
  private LocationEngineCallback<LocationEngineResult> lastLocationEngineListener =
    new LocationEngineCallback<LocationEngineResult>() {
      @Override
      public void onSuccess(LocationEngineResult result) {
        Log.e("LEAK_TEST", "last location: " + result.getLastLocation());
      }

      @Override
      public void onFailure(@NonNull Exception exception) {
        Log.e("LEAK_TEST", "last location - failure - " + exception);
      }
    };

  @SuppressLint("MissingPermission")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String accessTokenTelemetry = obtainAccessToken();
    String userAgentTelemetry = "MapboxEventsAndroid/3.1.0";
    mapboxTelemetry = new MapboxTelemetry(this, accessTokenTelemetry, userAgentTelemetry);

    checkPermissions();
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    locationEngine.removeLocationUpdates(currentLocationEngineListener);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapboxTelemetry.disable();
  }

  private String obtainAccessToken() {
    String accessToken = getString(R.string.mapbox_access_token);
    return accessToken;
  }

  private void checkPermissions() {
    boolean permissionsGranted = PermissionsManager.areLocationPermissionsGranted(this);

    if (permissionsGranted) {
      test();
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  @SuppressLint("MissingPermission")
  private void test() {
    locationEngine = LocationEngineProvider.getBestLocationEngine(this);
    locationEngine.requestLocationUpdates(locationEngineRequest, currentLocationEngineListener, Looper.getMainLooper());
//    locationEngine.getLastLocation(lastLocationEngineListener);
    mapboxTelemetry.enable();
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {

  }

  @SuppressLint("MissingPermission")
  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      test();
    }
  }
}

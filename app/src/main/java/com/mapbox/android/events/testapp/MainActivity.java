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

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionsListener {
  private final String LOG_TAG = "MainActivity";
  private MapboxTelemetry mapboxTelemetry;
  private PermissionsManager permissionsManager;

  private LocationEngineCallback<LocationEngineResult> currentLocationEngineListener;

  LocationEngine locationEngine;
  private LocationEngineRequest locationEngineRequest =
    new LocationEngineRequest.Builder(1000)
      .setFastestInterval(1000)
      .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
      .build();

  @SuppressLint("MissingPermission")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String accessTokenTelemetry = obtainAccessToken();
    String userAgentTelemetry = "MapboxEventsAndroid/3.1.0";
    //mapboxTelemetry = new MapboxTelemetry(this, accessTokenTelemetry, userAgentTelemetry);
    currentLocationEngineListener = new LocationUpdateCallback(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    checkPermissions();
  }

  @Override
  protected void onPause() {
    super.onPause();
    locationEngine.removeLocationUpdates(currentLocationEngineListener);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    //mapboxTelemetry.disable();
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
    //mapboxTelemetry.enable();
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

  private static final class LocationUpdateCallback implements LocationEngineCallback<LocationEngineResult>  {
    private final WeakReference<AppCompatActivity> weakRef;

    LocationUpdateCallback(AppCompatActivity activity) {
      this.weakRef = new WeakReference<>(activity);
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
      AppCompatActivity activity = weakRef.get();
      if (activity == null) {
        return;
      }

      // Do something with activity
      Log.e("LEAK_TEST", "current location: " + result.getLocations());
    }

    @Override
    public void onFailure(@NonNull Exception e) {
      Log.e("LEAK_TEST", "current location - failure - " + e);
    }
  }
}

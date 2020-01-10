package com.wnet.imageconvertor.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.wnet.imageconvertor.R;
import com.wnet.imageconvertor.dialog.TransparentProgressDialog;
import com.wnet.imageconvertor.interfaces.OnRetryListener;
import com.wnet.imageconvertor.util.PermissionUtils;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public abstract class BaseActivity extends Activity implements PermissionUtils.PermissionResultCallback {
    PermissionUtils permissionUtils = PermissionUtils.getInstance();
    private TransparentProgressDialog dialog;
    private boolean shouldICheckForPermissions;

    public static boolean verifyPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permission != null) {
            try {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            } catch (StackOverflowError e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(getContentResource());
            ButterKnife.bind(this);
            init(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideKeyboard();
//        checkForUpdatedAppVersion();
    }

//    private void checkForUpdatedAppVersion() {
//        if (Utils.isNetworkConnected(this)){
//            ApiUtils.getApiService(this).getAppVersion().enqueue(new Callback<GetAppVersionResponse>() {
//                @Override
//                public void onResponse(Call<GetAppVersionResponse> call, Response<GetAppVersionResponse> response) {
//                    GetAppVersionResponse appVersionResponse = response.body();
//                    onAppVersionSuccessResponse(appVersionResponse);
//                }
//
//                @Override
//                public void onFailure(Call<GetAppVersionResponse> call, Throwable t) {
//                    t.printStackTrace();
//                }
//            });
//        }
//    }

//    private void onAppVersionSuccessResponse(GetAppVersionResponse appVersionResponse) {
//        try {
//            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//            int version = pInfo.versionCode;
//            int versionFromApi = Integer.parseInt(appVersionResponse.getData().getAppVersion());
//            if (version < versionFromApi) {
//                redirectToPlayStore();
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    private void redirectToPlayStore() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.inc.vidya")));
    }

    public void toastMsg(String msg, boolean isLong) {
        if (isLong)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        else Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Layout resource to be inflated
     *
     * @return layout resource
     */
    @LayoutRes
    protected abstract int getContentResource();

    /**
     * Initialisations
     */
    protected abstract void init(@Nullable Bundle state);

    protected void showLoader() {
        if (dialog == null) {
            dialog = new TransparentProgressDialog(this);
            dialog.show();
        } else {
            dialog.show();
        }
    }

    protected void hideLoader() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        hideLoader();
        super.onDestroy();
    }

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!shouldICheckForPermissions) {
            //checkForRuntimePermissions();

            /*
            These permissions are required
             */
            ArrayList<String> requiredPermissions = new ArrayList<>();
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            requiredPermissions.add(Manifest.permission.RECEIVE_SMS);

//            if (!isLocationPermissionAvailable())
//                permissionUtils.check_permission(this, requiredPermissions, getString(R.string.permission_message), 101, this);
//            else checkForGps();
        }
    }

    protected Boolean isLocationPermissionAvailable() {
        return verifyPermission(getApplicationContext(), ACCESS_COARSE_LOCATION) && verifyPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
    }

    protected void checkForPermissions(boolean shouldICheckForPermissions) {
        this.shouldICheckForPermissions = shouldICheckForPermissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void permissionGranted(int request_code) {
        checkForGps();
    }

    @Override
    public void partialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        //goToAppPermissionScreen();
    }

    @Override
    public void permissionDenied(int request_code) {
        //goToAppPermissionScreen();
    }

    @Override
    public void neverAskAgain(int request_code) {
        goToAppPermissionScreen();
    }

    public void checkForGps() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
//                final LocationSettingsStates state = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:


                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    BaseActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private void goToAppPermissionScreen() {
        Toast.makeText(this, "Please allow the permissions", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}

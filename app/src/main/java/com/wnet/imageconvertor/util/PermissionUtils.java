/*
 * Copyright (c) 2018 - A Sayan Porya Code
 */

package com.wnet.imageconvertor.util;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class PermissionUtils {
    private static PermissionUtils sINSTANCE;
    private final ArrayList<String> permission_list = new ArrayList<>();
    private final ArrayList<String> listPermissionsNeeded = new ArrayList<>();
    private WeakReference<Activity> activityWeakReference;
    private PermissionResultCallback permissionResultCallback;
    private String dialog_content = "";
    private int req_code;

    /*public PermissionUtils(Context context, PermissionResultCallback callback) {
        //this.context=context;
        //this.current_activity = (Activity) context;
        this.activityWeakReference= new WeakReference<>((Activity) context);
        permissionResultCallback = callback;


    }*/

    private PermissionUtils() {
        //Don't you dare to make an object out of it
    }

    public static PermissionUtils getInstance() {
        if (sINSTANCE == null) {
            synchronized (PermissionUtils.class) {
                if (sINSTANCE == null) {
                    sINSTANCE = new PermissionUtils();
                }
            }
        }
        return sINSTANCE;
    }


    /**
     * @param activity       = reference of activity class to handle permissions
     * @param permissions    = list of permissions required
     * @param dialog_content = message to show if user denied the permission permanently
     * @param request_code   = permissions request code
     * @param callback       = permission events callback
     */

    public void check_permission(Activity activity, ArrayList<String> permissions, String dialog_content, int request_code, PermissionResultCallback callback) {
        if (activity != null) this.activityWeakReference = new WeakReference<>(activity);
        if (callback != null) this.permissionResultCallback = callback;
        this.permission_list.addAll(permissions);
        this.dialog_content = dialog_content;
        this.req_code = request_code;

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkAndRequestPermissions(permissions, request_code)) {
                if (permissionResultCallback != null)
                    permissionResultCallback.permissionGranted(request_code);
            }
        } else {
            if (permissionResultCallback != null)
                permissionResultCallback.permissionGranted(request_code);

        }

    }

    private void check_permission(ArrayList<String> permissions, String dialog_content, int request_code) {
        check_permission(null, permissions, dialog_content, request_code, null);

    }


    /**
     * Check and request the Permissions
     *
     * @param permissions  = permissions list
     * @param request_code = permission request code
     * @return = if true (all permissions granted) false (some of the permissions are not granted)
     */

    private boolean checkAndRequestPermissions(@NonNull ArrayList<String> permissions, int request_code) {

        if (permissions.size() > 0) {
            for (int i = 0; i < permissions.size(); i++) {
                int hasPermission = ContextCompat.checkSelfPermission(activityWeakReference.get(), permissions.get(i));

                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permissions.get(i));
                }

            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(activityWeakReference.get(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), request_code);
                return false;
            }
        }

        return true;
    }

    /**
     * Usually called for the activity in #onRequestPermissionsResult to check permissions result
     *
     * @param requestCode  = permisions request code
     * @param permissions  = list of permissions
     * @param grantResults =
     */

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            Map<String, Integer> perms = new HashMap<>();

            for (int i = 0; i < permissions.length; i++) {
                perms.put(permissions[i], grantResults[i]);
            }

            final ArrayList<String> pending_permissions = new ArrayList<>();

            for (int i = 0; i < listPermissionsNeeded.size(); i++) {
                if (perms.get(listPermissionsNeeded.get(i)) != null && perms.get(listPermissionsNeeded.get(i)) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activityWeakReference.get(), listPermissionsNeeded.get(i)))
                        pending_permissions.add(listPermissionsNeeded.get(i));
                    else {
//                        if (permissionResultCallback != null)
//                            permissionResultCallback.neverAskAgain(req_code);
                        return;
                    }
                }

            }

//            if (pending_permissions.size() > 0) {
//                showMessageOKCancel(dialog_content,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                                switch (which) {
//                                    case DialogInterface.BUTTON_POSITIVE:
//                                        check_permission(permission_list, dialog_content, req_code);
//                                        break;
//                                    case DialogInterface.BUTTON_NEGATIVE:
//                                        System.exit(0);
//                                        /*if (permission_list.size() == pending_permissions.size())
//                                            if (permissionResultCallback != null) permissionResultCallback.permissionDenied(req_code);
//                                        else
//                                            if (permissionResultCallback != null) permissionResultCallback.partialPermissionGranted(req_code, pending_permissions);
//                                        break;*/
//                                }
//
//
//                            }
//                        });
//
//            } else {
            if (permissionResultCallback != null)
                permissionResultCallback.permissionGranted(req_code);
//
//            }


        }
    }


    /**
     * Explains why the app needs permissions
     *
     * @param message
     * @param okListener
     */
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activityWeakReference.get())
                .setMessage(message)
                .setPositiveButton("Ok", okListener)
                .setNegativeButton("Exit", okListener)
                .create()
                .show();
    }

    public interface PermissionResultCallback {
        void permissionGranted(int request_code);

        void partialPermissionGranted(int request_code, ArrayList<String> granted_permissions);

        void permissionDenied(int request_code);

        void neverAskAgain(int request_code);
    }
}
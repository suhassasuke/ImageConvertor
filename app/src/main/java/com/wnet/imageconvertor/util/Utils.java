package com.wnet.imageconvertor.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.wnet.imageconvertor.dialog.TransparentProgressDialog;

import java.util.Date;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class Utils {
    public static TransparentProgressDialog dialog;

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }


    public static void backgroundThreadShortToast(final Activity getActivity,
                                                  final String msg) {
        try{
            if (getActivity != null && msg != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void showProgressLoader(final Activity getActivity) {
        try{
            if (getActivity != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog == null) {
                            dialog = new TransparentProgressDialog(getActivity);
                            dialog.show();
                        } else {
                            dialog.show();
                        }
                    }
                });
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void hideProgressLoader(Activity getActivity) {
        try{
            if (getActivity != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateUniqueImageFileName() {
        String filename = "";
        String datetime = new Date().toGMTString();
        datetime = datetime.replace(" ", "");
        datetime = datetime.replace(":", "");
        filename = "ImageConvertor_" + datetime;
        return filename;
    }
}

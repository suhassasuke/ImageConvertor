package com.wnet.imageconvertor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.wnet.imageconvertor.R;


public class TransparentProgressDialog extends Dialog {

    public TransparentProgressDialog(@NonNull Context context) {
        super(context, R.style.TransparentProgressDialog);
        setContentView(R.layout.dialog_transparent_loader);
        WindowManager.LayoutParams wlmp = getWindow().getAttributes();
        wlmp.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(wlmp);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
    }
}

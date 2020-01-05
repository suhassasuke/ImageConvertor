package com.wnet.imageconvertor.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.wnet.imageconvertor.Screens.MainActivity;

import java.io.File;
import java.io.FileOutputStream;

import static com.wnet.imageconvertor.util.Utils.generateUniqueImageFileName;

public class ImageConverter extends AsyncTask<Void, Void, String> {

    private MainActivity activity;
    private String type;
    private Bitmap imageBitmap;
    private int quality;

    public ImageConverter(MainActivity activity, String type, int quality, Bitmap imageBitmap) {
        this.activity = activity;
        this.type = type;
        this.quality = quality;
        this.imageBitmap = imageBitmap;
    }

    @Override
    protected String doInBackground(Void... params) {

        try {
            String filename = generateUniqueImageFileName();
//            FileOutputStream out = new FileOutputStream(filename);
            if(type.equals("JPEG")){
                filename = filename+".jpg";
            }else if(type.equals("PNG")){
                filename = filename+".png";
            }else if(type.equals("WEBP")){
                filename = filename+".webp";
            }
            FileOutputStream out = new FileOutputStream(new File(android.os.Environment.getExternalStorageDirectory(), filename));
            if(type.equals("JPEG")){
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out); //100-best quality
            }else if(type.equals("PNG")){
                imageBitmap.compress(Bitmap.CompressFormat.PNG, quality, out); //100-best quality
            }else if(type.equals("WEBP")){
                imageBitmap.compress(Bitmap.CompressFormat.WEBP, quality, out); //100-best quality
            }
            out.close();
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    @Override
    protected void onPostExecute(String filePath) {
        activity.convertedImage(filePath);
    }
}

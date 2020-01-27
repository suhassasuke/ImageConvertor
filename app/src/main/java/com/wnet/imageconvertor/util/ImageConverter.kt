package com.wnet.imageconvertor.util

import android.graphics.Bitmap
import android.os.AsyncTask

import com.wnet.imageconvertor.ui.homescreen.HomeFragment

import java.io.File
import java.io.FileOutputStream

import com.wnet.imageconvertor.util.Utils.generateUniqueImageFileName

class ImageConverter(val context: HomeFragment, private val type: String, private val quality: Int, private val imageBitmap: Bitmap) : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void): String? {
        try {
            var filename = generateUniqueImageFileName()
            //            FileOutputStream out = new FileOutputStream(filename);
            if (type == "JPEG") {
                filename = "$filename.jpg"
            } else if (type == "PNG") {
                filename = "$filename.png"
            } else if (type == "WEBP") {
                filename = "$filename.webp"
            }
            val out = FileOutputStream(File(android.os.Environment.getExternalStorageDirectory(), filename))
            if (type == "JPEG") {
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out) //100-best quality
            } else if (type == "PNG") {
                imageBitmap.compress(Bitmap.CompressFormat.PNG, quality, out) //100-best quality
            } else if (type == "WEBP") {
                imageBitmap.compress(Bitmap.CompressFormat.WEBP, quality, out) //100-best quality
            }
            out.close()
            return filename
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }


    override fun onPostExecute(filePath: String) {
        context.convertedImage(filePath)
    }


    interface ImageConvertorCallback {
        fun imageConvertorCallback(filePath: String)
    }
}

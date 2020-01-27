package com.wnet.imageconvertor.ui

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wnet.imageconvertor.R

class OnBoardActivity : AppCompatActivity() {

    var imageBitmap: Bitmap? = null
    var initialLoad = true
    var modifiedImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_board)
    }
}
